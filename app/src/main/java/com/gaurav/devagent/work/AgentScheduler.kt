package com.gaurav.devagent.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.gaurav.devagent.data.Settings
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object AgentScheduler {
    const val HEADLINES_NOW = "headlines-now"
    const val CHECKS_NOW = "checks-now"

    private val network = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    /** Schedules the daily headline fetch, evening goal nudge and periodic checks. */
    fun scheduleAll(context: Context, settings: Settings, replace: Boolean = false) {
        val wm = WorkManager.getInstance(context)
        val policy = if (replace) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP

        wm.enqueueUniquePeriodicWork(
            "headlines-daily", policy,
            PeriodicWorkRequestBuilder<HeadlinesWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(millisUntil(settings.headlineHour), TimeUnit.MILLISECONDS)
                .setConstraints(network)
                .build(),
        )
        wm.enqueueUniquePeriodicWork(
            "goals-nudge", policy,
            PeriodicWorkRequestBuilder<NudgeWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(millisUntil(settings.nudgeHour), TimeUnit.MILLISECONDS)
                .build(),
        )
        wm.enqueueUniquePeriodicWork(
            "checks-periodic", policy,
            PeriodicWorkRequestBuilder<ChecksWorker>(settings.checkIntervalMinutes.coerceAtLeast(15).toLong(), TimeUnit.MINUTES)
                .setConstraints(network)
                .build(),
        )
    }

    fun refreshHeadlinesNow(context: Context, notify: Boolean) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            HEADLINES_NOW, ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<HeadlinesWorker>()
                .setInputData(workDataOf(HeadlinesWorker.KEY_NOTIFY to notify))
                .setConstraints(network)
                .build(),
        )
    }

    fun runChecksNow(context: Context, checkId: String? = null) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            CHECKS_NOW, ExistingWorkPolicy.APPEND_OR_REPLACE,
            OneTimeWorkRequestBuilder<ChecksWorker>()
                .setInputData(workDataOf(ChecksWorker.KEY_CHECK_ID to checkId))
                .build(),
        )
    }

    private fun millisUntil(hour: Int): Long {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(hour, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }
}
