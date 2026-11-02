package com.gaurav.devagent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.gaurav.devagent.data.Check
import com.gaurav.devagent.data.CheckType
import com.gaurav.devagent.work.AgentScheduler

@Composable
fun ChecksScreen(state: AgentState) {
    val context = LocalContext.current
    var showAdd by remember { mutableStateOf(false) }

    ScreenWithFab(onAdd = { showAdd = true }) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Runs every ${state.settings.checkIntervalMinutes} min (change in Settings)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(onClick = { AgentScheduler.runChecksNow(context) }, enabled = state.checks.isNotEmpty()) {
                        Text("Run all now")
                    }
                }
            }
            if (state.checks.isEmpty()) {
                item {
                    Text("No checks yet. Tap + to watch a URL: get alerted when a site goes down, a page changes, or some text appears (e.g. \"In stock\", a job title, a release version).")
                }
            }
            items(state.checks, key = { it.id }) { c -> CheckCard(c) }
        }
    }

    if (showAdd) {
        AddCheckDialog(
            onDismiss = { showAdd = false },
            onAdd = { c ->
                AgentRepository.edit(refreshWidget = false) { s -> s.copy(checks = s.checks + c) }
                AgentScheduler.runChecksNow(context, c.id)
                showAdd = false
            },
        )
    }
}

@Composable
private fun CheckCard(c: Check) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(c.name, style = MaterialTheme.typography.titleMedium)
                Text(c.url, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                val what = if (c.type == CheckType.CONTAINS_TEXT) "${c.type.label}: \"${c.query}\"" else c.type.label
                Text(what, style = MaterialTheme.typography.bodySmall)
                Text(
                    "${c.lastStatus} · ${relative(c.lastRunAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (c.alerting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = { AgentScheduler.runChecksNow(context, c.id) }) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Run now")
            }
            Switch(checked = c.enabled, onCheckedChange = { on ->
                AgentRepository.edit(refreshWidget = false) { s ->
                    s.copy(checks = s.checks.map { if (it.id == c.id) it.copy(enabled = on) else it })
                }
            })
            IconButton(onClick = {
                AgentRepository.edit(refreshWidget = false) { s -> s.copy(checks = s.checks.filterNot { it.id == c.id }) }
            }) { Icon(Icons.Filled.Delete, contentDescription = "Delete check") }
        }
    }
}

@Composable
private fun AddCheckDialog(onDismiss: () -> Unit, onAdd: (Check) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://") }
    var type by remember { mutableStateOf(CheckType.SITE_DOWN) }
    var query by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && url.trim().matches(Regex("https?://\\S+\\.\\S+")) &&
        (type != CheckType.CONTAINS_TEXT || query.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New periodic check") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL") }, modifier = Modifier.fillMaxWidth())
                CheckType.entries.forEach { t ->
                    Row(
                        Modifier.fillMaxWidth().selectable(selected = type == t, onClick = { type = t }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = type == t, onClick = { type = t })
                        Text(t.label)
                    }
                }
                if (type == CheckType.CONTAINS_TEXT) {
                    OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Text to look for") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                onAdd(Check(name = name.trim(), url = url.trim(), type = type, query = query.trim()))
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
