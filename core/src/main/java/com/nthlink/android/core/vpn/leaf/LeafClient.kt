package com.nthlink.android.core.vpn.leaf

import android.app.Service.STOP_FOREGROUND_REMOVE
import android.util.Log
import com.nthlink.android.core.RootVpn
import com.nthlink.android.core.model.Proxy
import com.nthlink.android.core.utils.TAG
import com.nthlink.android.core.utils.createLeafConfig
import com.nthlink.android.core.vpn.RootVpnClient
import com.nthlink.android.core.vpn.RootVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class LeafClient(
    private val vpnService: RootVpnService,
    private val scope: CoroutineScope
) : RootVpnClient {
    init {
        // TODO add a JNI layer to call leaf
        System.loadLibrary("leafandroid")
    }

    private external fun leafStart(config: String): Int
    private external fun leafStop()
    private external fun leafSetProtectSocketCallback(name: String)

    private var leafJob: Job? = null

    override fun start(proxies: Array<out Proxy>) {
        scope.launch(Dispatchers.IO) {
            vpnService.updateVpnStatus(RootVpn.Status.CONNECTING)

            if (proxies.isEmpty()) {
                vpnService.updateVpnError(RootVpn.Error.VPN_SERVICE_ERROR)
                vpnService.updateVpnStatus(RootVpn.Status.DISCONNECTED)
                vpnService.stopSelf()
                return@launch
            }

            // Sets the socket protect callback, the name should match the the protect function defined in this class.
            leafSetProtectSocketCallback("protectSocket")

            // Leaf VPN connection
            val tunFd = vpnService.Builder().setSession("leaf")
                .addAddress("10.255.0.1", 24)
                .addDnsServer("1.1.1.1")
                .addRoute("0.0.0.0", 0)
                .establish()

            val leafConfig = createLeafConfig(tunFd!!.detachFd(), proxies)
            Log.i(TAG, "leaf config: $leafConfig")

            leafJob = scope.launch(Dispatchers.IO) {
                Log.i(TAG, "leaf coroutine started")

                val errCode = leafStart(leafConfig)
                if (errCode != 0) {
                    stop()
                    Log.i(TAG, "Start VPN failed")
                }

                Log.i(TAG, "leaf coroutine exit")
            }

            vpnService.updateVpnStatus(RootVpn.Status.CONNECTED)
            vpnService.startForegroundWithNotification()
        }
    }

    override fun stop() {
        scope.launch {
            vpnService.updateVpnStatus(RootVpn.Status.DISCONNECTING)
            leafStop()

            leafJob?.cancel()
            leafJob = null

            vpnService.stopForeground(STOP_FOREGROUND_REMOVE)
            vpnService.updateVpnStatus(RootVpn.Status.DISCONNECTED)
            vpnService.stopSelf()
        }
    }

    // Socket protect callback
    fun protectSocket(fd: Int): Boolean {
        return vpnService.protect(fd)
    }
}