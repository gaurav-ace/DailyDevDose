package com.gaurav.devagent.work

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gaurav.devagent.data.AgentRepository
import com.gaurav.devagent.data.Check
import com.gaurav.devagent.data.CheckType
import com.gaurav.devagent.data.Curriculum
import com.gaurav.devagent.news.HeadlineEngine
import com.gaurav.devagent.news.Http
import com.gaurav.devagent.notify.Notifier
import com.gaurav.devagent.widget.AgentWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class HeadlinesWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        AgentRepository.init(applicationContext)
        val s = AgentRepository.state.value
        val notify = inputData.getBoolean(KEY_NOTIFY, true)
        // The scheduled morning run also announces today's study plan (only on the first attempt).
        if (notify && runAttemptCount == 0) {
            Notifier.dailyPlan(applicationContext, Curriculum.todayItems(s))
            AgentWidget().updateAll(applicationContext)
        }
        val result = HeadlineEngine.fetch(s.settings, s.seenHeadlineUrls.toSet())

        if (result.itemCount == 0 && result.failedFeeds > 0) {
            AgentRepository.update { it.copy(headlineStatus = "Couldn't reach any feed — will retry") }
            return if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
        if (result.picked.isEmpty()) {
            AgentRepository.update { it.copy(headlineStatus = "No new matching headlines right now") }
            return Result.success()
        }
        AgentRepository.update { st ->
            st.copy(
                headlines = result.picked,
                headlinesFetchedAt = System.currentTimeMillis(),
                headlineStatus = if (result.failedFeeds > 0) "${result.failedFeeds} feed(s) failed" else "",
                seenHeadlineUrls = (result.picked.map { it.url } + st.seenHeadlineUrls).distinct().take(300),
            )
        }
        if (notify) Notifier.headlines(applicationContext, result.picked)
        return Result.success()
    }

    companion object {
        const val KEY_NOTIFY = "notify"
    }
}

class NudgeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        AgentRepository.init(applicationContext)
        val state = AgentRepository.state.value
        val remaining = Curriculum.todayItems(state).filterNot { it.done }.map { "${it.track.label}: ${it.lesson.title}" } +
            state.goals.filterNot { it.isDoneToday }.map { "${it.category.label}: ${it.text}" }
        if (remaining.isNotEmpty()) Notifier.goalsNudge(applicationContext, remaining)
        AgentWidget().updateAll(applicationContext)
        return Result.success()
    }
}

class ChecksWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        AgentRepository.init(applicationContext)
        val onlyId = inputData.getString(KEY_CHECK_ID)
        val checks = AgentRepository.state.value.checks.filter {
            if (onlyId != null) it.id == onlyId else it.enabled
        }
        for (check in checks) {
            val (updated, alert) = withContext(Dispatchers.IO) { evaluate(check) }
            if (alert != null) Notifier.checkAlert(applicationContext, check, alert)
            AgentRepository.update(refreshWidget = false) { st ->
                st.copy(checks = st.checks.map {
                    if (it.id != check.id) it
                    else it.copy(
                        lastRunAt = updated.lastRunAt,
                        lastStatus = updated.lastStatus,
                        alerting = updated.alerting,
                        lastHash = updated.lastHash,
                    )
                })
            }
        }
        return Result.success()
    }

    /** Returns the check with updated status fields, plus a notification message if it should alert. */
    private fun evaluate(c: Check): Pair<Check, String?> {
        val now = System.currentTimeMillis()
        val res = runCatching { Http.get(c.url) }
        return when (c.type) {
            CheckType.SITE_DOWN -> {
                val code = res.getOrNull()?.code
                val up = code != null && code in 200..399
                val status = if (up) "Up (HTTP $code)"
                else "Down (${code?.let { "HTTP $it" } ?: res.exceptionOrNull()?.javaClass?.simpleName})"
                val alert = when {
                    !up && !c.alerting -> "Site is DOWN: $status"
                    up && c.alerting -> "Site is back up"
                    else -> null
                }
                c.copy(lastRunAt = now, lastStatus = status, alerting = !up) to alert
            }
            CheckType.CONTAINS_TEXT -> {
                val body = res.getOrNull()?.body
                    ?: return c.copy(lastRunAt = now, lastStatus = "Error: ${res.exceptionOrNull()?.message}") to null
                val found = visibleText(body).contains(c.query, ignoreCase = true)
                val alert = if (found && !c.alerting) "Found \"${c.query}\" on the page" else null
                c.copy(lastRunAt = now, lastStatus = if (found) "Found" else "Not found", alerting = found) to alert
            }
            CheckType.PAGE_CHANGED -> {
                val body = res.getOrNull()?.body
                    ?: return c.copy(lastRunAt = now, lastStatus = "Error: ${res.exceptionOrNull()?.message}") to null
                val hash = sha256(visibleText(body))
                val changed = c.lastHash != null && c.lastHash != hash
                val status = when {
                    c.lastHash == null -> "Baseline saved"
                    changed -> "Changed"
                    else -> "No change"
                }
                c.copy(lastRunAt = now, lastStatus = status, lastHash = hash) to
                    (if (changed) "The page content changed" else null)
            }
        }
    }

    private fun visibleText(html: String): String = html
        .replace(Regex("(?is)<(script|style|noscript)[^>]*>.*?</\\1>"), " ")
        .replace(Regex("<[^>]+>"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun sha256(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    companion object {
        const val KEY_CHECK_ID = "check_id"
    }
}
