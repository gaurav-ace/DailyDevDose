package com.gaurav.devagent.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gaurav.devagent.R
import com.gaurav.devagent.data.Check
import com.gaurav.devagent.data.DailyItem
import com.gaurav.devagent.data.Headline
import com.gaurav.devagent.data.Reminder
import com.gaurav.devagent.ui.MainActivity

object Notifier {
    const val CH_REMINDERS = "reminders"
    const val CH_CHECKS = "checks"
    const val CH_HEADLINES = "headlines"
    const val CH_GOALS = "goals"

    private const val ID_HEADLINES = 1001
    private const val ID_GOALS = 1002
    private const val ID_PLAN = 1003

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(CH_REMINDERS, "Task reminders", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CH_CHECKS, "Periodic check alerts", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CH_HEADLINES, "Daily headlines", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CH_GOALS, "Goal nudges", NotificationManager.IMPORTANCE_DEFAULT),
            )
        )
    }

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun headlines(context: Context, list: List<Headline>) {
        val style = NotificationCompat.InboxStyle()
        list.forEach { style.addLine("• ${it.title}") }
        post(context, ID_HEADLINES, builder(context, CH_HEADLINES, "Today's top ${list.size} reads", list.first().title)
            .setStyle(style)
            .setContentIntent(openApp(context)))
    }

    /** Evening nudge; [remaining] are display lines for unfinished plan items and goals. */
    fun goalsNudge(context: Context, remaining: List<String>) {
        val style = NotificationCompat.InboxStyle()
        remaining.forEach { style.addLine("• $it") }
        val title = "${remaining.size} item${if (remaining.size == 1) "" else "s"} left today"
        post(context, ID_GOALS, builder(context, CH_GOALS, title, remaining.first())
            .setStyle(style)
            .setContentIntent(openApp(context)))
    }

    fun dailyPlan(context: Context, items: List<DailyItem>) {
        val style = NotificationCompat.InboxStyle()
        items.forEach { style.addLine("${it.track.short}: ${it.lesson.title}") }
        post(context, ID_PLAN, builder(context, CH_GOALS, "Today's plan", items.joinToString(" · ") { it.lesson.title })
            .setStyle(style)
            .setContentIntent(openApp(context)))
    }

    fun reminder(context: Context, r: Reminder, missed: Boolean = false) {
        val title = if (missed) "Missed: ${r.title}" else r.title
        post(context, r.id.hashCode(), builder(context, CH_REMINDERS, title, r.note.ifBlank { "Task reminder" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openApp(context)))
    }

    fun checkAlert(context: Context, check: Check, message: String) {
        val open = PendingIntent.getActivity(
            context, check.id.hashCode(),
            Intent(Intent.ACTION_VIEW, Uri.parse(check.url)),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        post(context, check.id.hashCode(), builder(context, CH_CHECKS, check.name, message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open))
    }

    fun test(context: Context) {
        post(context, 999, builder(context, CH_REMINDERS, "DevAgent is working", "Notifications are set up correctly."))
    }

    private fun builder(context: Context, channel: String, title: String, text: String) =
        NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)

    private fun openApp(context: Context): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    @SuppressLint("MissingPermission")
    private fun post(context: Context, id: Int, builder: NotificationCompat.Builder) {
        if (!canPost(context)) return
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }
}
