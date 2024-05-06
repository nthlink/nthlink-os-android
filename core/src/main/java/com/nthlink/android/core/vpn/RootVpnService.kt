package com.nthlink.android.core.vpn

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
import android.net.VpnService
import android.os.Build
import android.util.Log
import com.nthlink.android.core.RootVpn
import com.nthlink.android.core.model.Outline
import com.nthlink.android.core.model.Proxy
import com.nthlink.android.core.notification.createNotification
import com.nthlink.android.core.notification.createNotificationChannel
import com.nthlink.android.core.storage.saveVpnError
import com.nthlink.android.core.storage.saveVpnStatus
import com.nthlink.android.core.utils.TAG
import com.nthlink.android.core.vpn.leaf.LeafClient
import com.nthlink.android.core.vpn.outline.OutlineClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class RootVpnService : VpnService() {
    companion object {
        private const val ACTION_START = "start"
        private const val ACTION_STOP = "stop"
        private const val KEY_PROXIES = "proxies"

        fun start(context: Context, proxies: Array<out Proxy>) {
            context.startService(getIntent(context, ACTION_START).apply {
                putExtra(KEY_PROXIES, proxies)
            })
        }

        fun stop(context: Context) {
            context.startService(getIntent(context, ACTION_STOP))
        }

        private fun getIntent(context: Context, action: String): Intent {
            return Intent(context, RootVpnService::class.java).apply {
                this.action = action
            }
        }
    }

    private var isRunning = false
    private var vpnClient: RootVpnClient? = null
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        return when {
            action == ACTION_START && !isRunning -> {
                Log.i(TAG, "RootVpnService onStartCommand: run")
                isRunning = true
                start(getProxies(intent))
                START_STICKY
            }

            action == ACTION_STOP -> {
                Log.i(TAG, "RootVpnService onStartCommand: stop")
                stop()
                isRunning = false
                START_NOT_STICKY
            }

            else -> START_STICKY
        }
    }

    private fun getProxies(intent: Intent): Array<out Proxy> {
        val proxies = intent.getParcelableArrayExtra(KEY_PROXIES)
            ?.filterIsInstance(Proxy::class.java)
            ?.toTypedArray()

        return proxies ?: emptyArray()
    }

    private fun start(proxies: Array<out Proxy>) {
        val outlineProxies = proxies.filterIsInstance(Outline::class.java)
        // TODO add new branches for your VPN clients
        vpnClient = if (outlineProxies.isNotEmpty()) {
            OutlineClient(this, scope).apply {
                start(outlineProxies.toTypedArray())
            }
        } else {
            LeafClient(this, scope).apply {
                start(proxies)
            }
        }
    }

    private fun stop() = vpnClient?.stop() ?: stopService()

    private fun stopService() = scope.launch {
        updateVpnStatus(RootVpn.Status.DISCONNECTING)
        delay(200)
        updateVpnStatus(RootVpn.Status.DISCONNECTED)
        stopSelf()
    }

    override fun onRevoke() {
        Log.i(TAG, "RootVpnService onRevoke: ")
        stop()
    }

    override fun onDestroy() {
        Log.i(TAG, "RootVpnService onDestroy: ")
        scope.cancel()
    }

    fun startForegroundWithNotification() {
        // Set notification content intent, start launch activity
        val clickIntent = packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } ?: throw NullPointerException("Get launch intent failed!")

        // Set notification
        createNotificationChannel(this)
        val notification = createNotification(this, clickIntent)

        // Move background service to foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(Int.MAX_VALUE, notification, FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
        } else {
            startForeground(Int.MAX_VALUE, notification)
        }
    }

    suspend fun updateVpnStatus(status: RootVpn.Status) {
        saveVpnStatus(this, status)
    }

    suspend fun updateVpnError(error: RootVpn.Error) {
        saveVpnError(this, error)
    }
}