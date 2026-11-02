package com.gaurav.devagent

import android.app.Application
import com.gaurav.devagent.data.AgentRepository
import com.gaurav.devagent.notify.Notifier
import com.gaurav.devagent.work.AgentScheduler

class AgentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AgentRepository.init(this)
        Notifier.createChannels(this)
        AgentScheduler.scheduleAll(this, AgentRepository.state.value.settings)
    }
}
