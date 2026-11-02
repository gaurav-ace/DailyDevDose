package com.gaurav.devagent.data

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.gaurav.devagent.widget.AgentWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Single source of truth for the app, persisted as one JSON file.
 * The UI, workers, alarm receivers and the widget all run in the same process and share this object.
 */
object AgentRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var appContext: Context
    private lateinit var file: File

    private val _state = MutableStateFlow(AgentState())
    val state: StateFlow<AgentState> = _state.asStateFlow()

    @Synchronized
    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        file = File(appContext.filesDir, "agent_state.json")
        val loaded = runCatching { json.decodeFromString<AgentState>(file.readText()) }.getOrNull()
        if (loaded != null) _state.value = loaded else write(_state.value)
    }

    suspend fun update(refreshWidget: Boolean = true, transform: (AgentState) -> AgentState) {
        mutex.withLock {
            val newState = transform(_state.value)
            if (newState == _state.value) return
            _state.value = newState
            withContext(Dispatchers.IO) { write(newState) }
        }
        if (refreshWidget) AgentWidget().updateAll(appContext)
    }

    /** Fire-and-forget variant for UI callbacks. */
    fun edit(refreshWidget: Boolean = true, transform: (AgentState) -> AgentState) {
        scope.launch { update(refreshWidget, transform) }
    }

    suspend fun toggleGoal(id: String) = update { s ->
        s.copy(goals = s.goals.map {
            if (it.id == id) it.copy(doneOn = if (it.isDoneToday) null else today()) else it
        })
    }

    suspend fun toggleTrack(track: Track) = update { s ->
        s.copy(progress = s.progress + (track to Curriculum.toggled(track, s.progress[track] ?: TrackProgress())))
    }

    /** Skip forward (delta = 1) or go back (delta = -1) in a track. */
    suspend fun moveTrack(track: Track, delta: Int) = update { s ->
        s.copy(progress = s.progress + (track to Curriculum.moved(track, s.progress[track] ?: TrackProgress(), delta)))
    }

    private fun write(s: AgentState) {
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(json.encodeToString(AgentState.serializer(), s))
        tmp.renameTo(file)
    }
}
