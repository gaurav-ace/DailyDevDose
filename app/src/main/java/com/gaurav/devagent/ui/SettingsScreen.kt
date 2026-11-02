package com.gaurav.devagent.ui

import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gaurav.devagent.data.AgentRepository
import com.gaurav.devagent.data.AgentState
import com.gaurav.devagent.data.Feed
import com.gaurav.devagent.data.Settings
import com.gaurav.devagent.data.Topic
import com.gaurav.devagent.notify.Notifier
import com.gaurav.devagent.work.AgentScheduler

private val CHECK_INTERVALS = listOf(15, 30, 60, 180, 360)

@Composable
fun SettingsScreen(state: AgentState) {
    val context = LocalContext.current
    val settings = state.settings

    fun save(newSettings: Settings, reschedule: Boolean = false) {
        AgentRepository.edit(refreshWidget = false) { it.copy(settings = newSettings) }
        if (reschedule) AgentScheduler.scheduleAll(context, newSettings, replace = true)
    }

    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle("Schedule")
        SettingRow("Morning headlines", hourLabel(settings.headlineHour)) {
            pickTime(context, settings.headlineHour, 0) { h, _ -> save(settings.copy(headlineHour = h), reschedule = true) }
        }
        SettingRow("Evening goal nudge", hourLabel(settings.nudgeHour)) {
            pickTime(context, settings.nudgeHour, 0) { h, _ -> save(settings.copy(nudgeHour = h), reschedule = true) }
        }
        Text("Periodic checks every", style = MaterialTheme.typography.bodyLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CHECK_INTERVALS.forEach { m ->
                FilterChip(
                    selected = settings.checkIntervalMinutes == m,
                    onClick = { save(settings.copy(checkIntervalMinutes = m), reschedule = true) },
                    label = { Text(if (m < 60) "$m min" else "${m / 60} h") },
                )
            }
        }

        HorizontalDivider()
        SectionTitle("Headline feeds")
        settings.feeds.forEach { feed ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(feed.name, style = MaterialTheme.typography.bodyLarge)
                    Text("${feed.topic.label} · ${feed.url}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                IconButton(onClick = { save(settings.copy(feeds = settings.feeds - feed)) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove feed")
                }
            }
        }
        AddFeedForm(onAdd = { save(settings.copy(feeds = settings.feeds + it)) })

        HorizontalDivider()
        SectionTitle("Keywords for \"Mixed\" feeds")
        KeywordsEditor("AI keywords", settings.aiKeywords) { save(settings.copy(aiKeywords = it)) }
        KeywordsEditor("System design keywords", settings.systemDesignKeywords) { save(settings.copy(systemDesignKeywords = it)) }

        HorizontalDivider()
        SectionTitle("Reliability")
        Text(
            "Some phones (Xiaomi, Samsung, OnePlus…) kill background work aggressively. " +
                "Set DevAgent's battery usage to \"Unrestricted\" so checks and headlines run on time.",
            style = MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                runCatching { context.startActivity(Intent(AndroidSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
            }) { Text("Battery settings") }
            Button(onClick = { Notifier.test(context) }) { Text("Test notification") }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = onClick) { Text(value) }
    }
}

@Composable
private fun AddFeedForm(onAdd: (Feed) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf(Topic.MIXED) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Feed name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("RSS / Atom URL") }, modifier = Modifier.fillMaxWidth())
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Topic.entries.forEach {
                FilterChip(selected = topic == it, onClick = { topic = it }, label = { Text(it.label) })
            }
        }
        Button(
            enabled = name.isNotBlank() && url.startsWith("http"),
            onClick = {
                onAdd(Feed(url.trim(), name.trim(), topic))
                name = ""; url = ""; topic = Topic.MIXED
            },
        ) { Text("Add feed") }
    }
}

@Composable
private fun KeywordsEditor(label: String, keywords: List<String>, onSave: (List<String>) -> Unit) {
    var text by remember(keywords) { mutableStateOf(keywords.joinToString(", ")) }
    val parsed = text.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
        if (parsed != keywords) {
            Button(onClick = { onSave(parsed) }) { Text("Save") }
        }
    }
}
