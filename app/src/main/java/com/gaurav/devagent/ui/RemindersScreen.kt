package com.gaurav.devagent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gaurav.devagent.data.AgentRepository
import com.gaurav.devagent.data.AgentState
import com.gaurav.devagent.data.Reminder
import com.gaurav.devagent.reminders.ReminderScheduler
import java.util.Calendar

@Composable
fun RemindersScreen(state: AgentState) {
    val context = LocalContext.current
    var showAdd by remember { mutableStateOf(false) }
    val now = System.currentTimeMillis()

    ScreenWithFab(onAdd = { showAdd = true }) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.reminders.isEmpty()) {
                item { Text("No reminders. Tap + to get notified about a task at a specific time.") }
            }
            items(state.reminders.sortedWith(compareBy({ !it.enabled }, { it.triggerAt })), key = { it.id }) { r ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(r.title, style = MaterialTheme.typography.titleMedium)
                            if (r.note.isNotBlank()) Text(r.note, style = MaterialTheme.typography.bodySmall)
                            Text(
                                if (r.repeatDaily) "Daily at ${formatTime(r.triggerAt)}" else formatDateTime(r.triggerAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Switch(
                            checked = r.enabled,
                            enabled = r.repeatDaily || r.triggerAt > now,
                            onCheckedChange = { on ->
                                val updated = ReminderScheduler.nextOccurrence(r.copy(enabled = on))
                                AgentRepository.edit(refreshWidget = false) { s ->
                                    s.copy(reminders = s.reminders.map { if (it.id == r.id) updated else it })
                                }
                                ReminderScheduler.schedule(context, updated)
                            },
                        )
                        IconButton(onClick = {
                            ReminderScheduler.cancel(context, r.id)
                            AgentRepository.edit(refreshWidget = false) { s -> s.copy(reminders = s.reminders.filterNot { it.id == r.id }) }
                        }) { Icon(Icons.Filled.Delete, contentDescription = "Delete reminder") }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddReminderDialog(
            onDismiss = { showAdd = false },
            onAdd = { r ->
                AgentRepository.edit(refreshWidget = false) { s -> s.copy(reminders = s.reminders + r) }
                ReminderScheduler.schedule(context, r)
                showAdd = false
            },
        )
    }
}

@Composable
private fun AddReminderDialog(onDismiss: () -> Unit, onAdd: (Reminder) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var repeatDaily by remember { mutableStateOf(false) }
    var at by remember {
        mutableLongStateOf(
            Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        )
    }
    val cal = Calendar.getInstance().apply { timeInMillis = at }
    val resolved = ReminderScheduler.nextOccurrence(Reminder(title = title, triggerAt = at, repeatDaily = repeatDaily))
    val inPast = resolved.triggerAt <= System.currentTimeMillis()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!repeatDaily) {
                        OutlinedButton(onClick = {
                            pickDate(context, cal) { at = it.timeInMillis }
                        }) { Text(formatDateTime(at).substringBefore(",")) }
                    }
                    OutlinedButton(onClick = {
                        pickTime(context, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)) { h, m ->
                            at = (cal.clone() as Calendar).apply {
                                set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m)
                            }.timeInMillis
                        }
                    }) { Text(formatTime(at)) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Repeat daily", Modifier.weight(1f))
                    Switch(checked = repeatDaily, onCheckedChange = { repeatDaily = it })
                }
                if (inPast) Text("That time is in the past", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && !inPast,
                onClick = { onAdd(resolved.copy(title = title.trim(), note = note.trim())) },
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
