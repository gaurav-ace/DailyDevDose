package com.gaurav.devagent.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.gaurav.devagent.data.AgentRepository
import com.gaurav.devagent.data.AgentState
import com.gaurav.devagent.data.Curriculum
import com.gaurav.devagent.data.Track
import com.gaurav.devagent.ui.MainActivity

class AgentWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        AgentRepository.init(context)
        provideContent {
            val state by AgentRepository.state.collectAsState()
            GlanceTheme { WidgetContent(state) }
        }
    }
}

class AgentWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AgentWidget()
}

val GoalIdKey = ActionParameters.Key<String>("goal_id")

class ToggleGoalAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AgentRepository.init(context)
        val id = parameters[GoalIdKey] ?: return
        AgentRepository.toggleGoal(id)
    }
}

val TrackKey = ActionParameters.Key<String>("track")

class ToggleTrackAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AgentRepository.init(context)
        val track = parameters[TrackKey]?.let { name -> Track.entries.firstOrNull { it.name == name } } ?: return
        AgentRepository.toggleTrack(track)
    }
}

/** ~60% opaque so the wallpaper shows through while text stays readable. */
private val WidgetBackground = ColorProvider(day = Color(0x99FFFFFF), night = Color(0x99121212))

@Composable
private fun WidgetContent(state: AgentState) {
    val goals = state.goals
    val plan = Curriculum.todayItems(state)
    val done = plan.count { it.done } + goals.count { it.isDoneToday }
    val total = plan.size + goals.size
    val onSurface = GlanceTheme.colors.onSurface
    val muted = GlanceTheme.colors.onSurfaceVariant

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBackground)
            .cornerRadius(16.dp)
            .padding(12.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Today's plan",
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurface),
            )
            Text(
                "$done/$total",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GlanceTheme.colors.primary),
            )
        }
        Spacer(GlanceModifier.height(4.dp))
        LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            items(plan, itemId = { -1L - it.track.ordinal }) { item ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CheckBox(
                        checked = item.done,
                        onCheckedChange = actionRunCallback<ToggleTrackAction>(actionParametersOf(TrackKey to item.track.name)),
                    )
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .clickable(actionStartActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.lesson.url)))),
                    ) {
                        Text(
                            item.lesson.title,
                            maxLines = 2,
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (item.done) muted else onSurface),
                        )
                        Text(
                            "${item.track.short} ${item.number}/${item.total} · ${item.lesson.tag}",
                            maxLines = 1,
                            style = TextStyle(fontSize = 11.sp, color = muted),
                        )
                    }
                }
            }
            if (goals.isNotEmpty()) {
                item {
                    Text(
                        "My goals",
                        modifier = GlanceModifier.padding(top = 8.dp, bottom = 2.dp),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface),
                    )
                }
            }
            items(goals, itemId = { it.id.hashCode().toLong() }) { goal ->
                CheckBox(
                    checked = goal.isDoneToday,
                    onCheckedChange = actionRunCallback<ToggleGoalAction>(actionParametersOf(GoalIdKey to goal.id)),
                    text = "${goal.category.short} · ${goal.text}",
                    style = TextStyle(fontSize = 13.sp, color = if (goal.isDoneToday) muted else onSurface),
                    maxLines = 2,
                )
            }
            item {
                Text(
                    "Top reads",
                    modifier = GlanceModifier.padding(top = 10.dp, bottom = 4.dp),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface),
                )
            }
            if (state.headlines.isEmpty()) {
                item {
                    Text(
                        "No headlines yet — open the app to refresh",
                        style = TextStyle(fontSize = 12.sp, color = muted),
                    )
                }
            }
            items(state.headlines, itemId = { it.url.hashCode().toLong() }) { h ->
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(actionStartActivity(Intent(Intent.ACTION_VIEW, Uri.parse(h.url)))),
                ) {
                    Text(h.title, maxLines = 2, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = onSurface))
                    Text("${h.topic.label} · ${h.source}", style = TextStyle(fontSize = 11.sp, color = muted))
                }
            }
        }
    }
}
