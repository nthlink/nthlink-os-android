package com.nthlink.android.core

import android.content.Context
import com.nthlink.android.core.model.Config
import kotlinx.coroutines.delay

internal class RootVpnClient(context: Context) : RootVpn(context) {

    override suspend fun runVpn(servers: List<Config.Server>) {
        // TODO Not yet implemented
        runVpn("")
    }

    override suspend fun runVpn(config: String) {
        // TODO Not yet implemented
        updateStatus(Root.Status.CONNECTING)
        delay(1000)
        updateStatus(Root.Status.CONNECTED)
    }

    override fun disconnect() {
        // TODO Not yet implemented
        updateStatus(Root.Status.DISCONNECTING)
        updateStatus(Root.Status.DISCONNECTED)
    }
}