package com.gaurav.devagent.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.gaurav.devagent.data.AgentRepository
import com.gaurav.devagent.data.Reminder
import com.gaurav.devagent.notify.Notifier
import com.gaurav.devagent.work.AgentScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ReminderScheduler {
    private const val DAY_MS = 24 * 60 * 60 * 1000L
    private const val EXTRA_ID = "reminder_id"

    fun schedule(context: Context, r: Reminder) {
        if (!r.enabled) return cancel(context, r.id)
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = pendingIntent(context, r.id)
        if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.triggerAt, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.triggerAt, pi)
        }
    }

    fun cancel(context: Context, id: String) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, id))
    }

    /** Moves a daily reminder to its next future occurrence. */
    fun nextOccurrence(r: Reminder, now: Long = System.currentTimeMillis()): Reminder {
        if (!r.repeatDaily || r.triggerAt > now) return r
        val days = (now - r.triggerAt) / DAY_MS + 1
        return r.copy(triggerAt = r.triggerAt + days * DAY_MS)
    }

    /** Re-arms all alarms (after boot or app start). One-off reminders missed while off are shown now. */
    suspend fun rescheduleAll(context: Context) {
        val now = System.currentTimeMillis()
        val missed = mutableListOf<Reminder>()
        AgentRepository.update(refreshWidget = false) { st ->
            st.copy(reminders = st.reminders.map { r ->
                when {
                    !r.enabled -> r
                    r.repeatDaily -> nextOccurrence(r, now)
                    r.triggerAt <= now -> r.copy(enabled = false).also { missed += r }
                    else -> r
                }
            })
        }
        missed.forEach { Notifier.reminder(context, it, missed = true) }
        AgentRepository.state.value.reminders.forEach { schedule(context, it) }
    }

    internal fun idFrom(intent: Intent): String? = intent.getStringExtra(EXTRA_ID)

    private fun pendingIntent(context: Context, id: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setData(Uri.parse("devagent://reminder/$id"))
            .putExtra(EXTRA_ID, id)
        return PendingIntent.getBroadcast(
            context, id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = ReminderScheduler.idFrom(intent) ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                AgentRepository.init(context)
                val r = AgentRepository.state.value.reminders.firstOrNull { it.id == id } ?: return@launch
                if (!r.enabled) return@launch
                Notifier.reminder(context, r)
                val next = if (r.repeatDaily) ReminderScheduler.nextOccurrence(r) else r.copy(enabled = false)
                AgentRepository.update(refreshWidget = false) { st ->
                    st.copy(reminders = st.reminders.map { if (it.id == id) next else it })
                }
                if (next.enabled) ReminderScheduler.schedule(context, next)
            } finally {
                pending.finish()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                AgentRepository.init(context)
                ReminderScheduler.rescheduleAll(context)
                AgentScheduler.scheduleAll(context, AgentRepository.state.value.settings)
            } finally {
                pending.finish()
            }
        }
    }
}
