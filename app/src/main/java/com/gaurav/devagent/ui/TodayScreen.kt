package com.gaurav.devagent.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.gaurav.devagent.data.AgentRepository
import com.gaurav.devagent.data.AgentState
import com.gaurav.devagent.data.Curriculum
import com.gaurav.devagent.data.DailyItem
import com.gaurav.devagent.data.Goal
import com.gaurav.devagent.data.GoalCategory
import com.gaurav.devagent.data.Headline
import com.gaurav.devagent.work.AgentScheduler
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(state: AgentState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    val refreshWork by remember {
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(AgentScheduler.HEADLINES_NOW)
    }.collectAsState(initial = emptyList())
    val refreshing = refreshWork.any { it.state == WorkInfo.State.RUNNING }
    val waiting = refreshWork.any { it.state == WorkInfo.State.ENQUEUED }

    ScreenWithFab(onAdd = { showAdd = true }) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val plan = Curriculum.todayItems(state)
            item {
                ProgressCard(
                    done = plan.count { it.done } + state.goals.count { it.isDoneToday },
                    total = plan.size + state.goals.size,
                )
            }
            item(key = "plan-title") { SectionTitle("Today's plan", Modifier.padding(top = 8.dp)) }
            items(plan, key = { "plan-" + it.track }) { item ->
                PlanCard(
                    item,
                    onToggle = { scope.launch { AgentRepository.toggleTrack(item.track) } },
                    onMove = { delta -> scope.launch { AgentRepository.moveTrack(item.track, delta) } },
                )
            }
            GoalCategory.entries.forEach { category ->
                val goals = state.goals.filter { it.category == category }
                if (goals.isNotEmpty()) {
                    item(key = "title-$category") { SectionTitle("My goals · ${category.label}", Modifier.padding(top = 8.dp)) }
                }
                items(goals, key = { it.id }) { goal ->
                    GoalRow(
                        goal,
                        onToggle = { scope.launch { AgentRepository.toggleGoal(goal.id) } },
                        onDelete = { AgentRepository.edit { s -> s.copy(goals = s.goals - goal) } },
                    )
                }
            }
            if (state.goals.isEmpty()) {
                item(key = "goals-empty") {
                    Text("Tap + to add your own daily goals", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
            }
            item(key = "headlines-title") {
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        SectionTitle("Top 3 reads")
                        Text(
                            when {
                                refreshing -> "Fetching…"
                                waiting -> "Waiting for network…"
                                else -> "Updated ${relative(state.headlinesFetchedAt)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (refreshing) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    } else {
                        IconButton(onClick = { AgentScheduler.refreshHeadlinesNow(context, notify = false) }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh headlines")
                        }
                    }
                }
            }
            if (state.headlineStatus.isNotBlank()) {
                item(key = "headline-status") {
                    Text(state.headlineStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            items(state.headlines, key = { "h-" + it.url }) { HeadlineCard(it) }
        }
    }

    if (showAdd) {
        AddGoalDialog(
            onDismiss = { showAdd = false },
            onAdd = { category, text ->
                AgentRepository.edit { s -> s.copy(goals = s.goals + Goal(category = category, text = text)) }
                showAdd = false
            },
        )
    }
}

@Composable
private fun ProgressCard(done: Int, total: Int) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                style = MaterialTheme.typography.titleLarge,
            )
            Text("$done of $total done today", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else done.toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PlanCard(item: DailyItem, onToggle: () -> Unit, onMove: (Int) -> Unit) {
    val context = LocalContext.current
    val lesson = item.lesson
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.done, onCheckedChange = { onToggle() })
                Column(Modifier.weight(1f)) {
                    Text(
                        "${item.track.label} · ${item.number} of ${item.total}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        lesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (item.done) TextDecoration.LineThrough else null,
                    )
                    Text(lesson.tag, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(Modifier.padding(start = 12.dp)) {
                if (lesson.summary.isNotBlank()) {
                    Text(lesson.summary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                }
                if (lesson.task.isNotBlank()) {
                    Text("Task: ${lesson.task}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { openUrl(context, lesson.url) }) { Text("Open") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { onMove(-1) }) { Text("Previous") }
                    TextButton(onClick = { onMove(1) }) { Text("Skip") }
                }
            }
        }
    }
}

@Composable
private fun GoalRow(goal: Goal, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.clickable(onClick = onToggle).padding(end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = goal.isDoneToday, onCheckedChange = { onToggle() })
            Text(
                goal.text,
                modifier = Modifier.weight(1f),
                textDecoration = if (goal.isDoneToday) TextDecoration.LineThrough else null,
            )
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete goal") }
        }
    }
}

@Composable
private fun HeadlineCard(h: Headline) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth().clickable { openUrl(context, h.url) }) {
        Column(Modifier.padding(16.dp)) {
            Text(h.title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            val date = if (h.publishedAt > 0) " · ${relative(h.publishedAt)}" else ""
            Text("${h.topic.label} · ${h.source}$date", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AddGoalDialog(onDismiss: () -> Unit, onAdd: (GoalCategory, String) -> Unit) {
    var category by remember { mutableStateOf(GoalCategory.CODING) }
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New daily goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalCategory.entries.forEach {
                        FilterChip(selected = category == it, onClick = { category = it }, label = { Text(it.label) })
                    }
                }
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Goal") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(category, text.trim()) }, enabled = text.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
