package com.nthlink.android.core.vpn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity.RESULT_CANCELED
import androidx.activity.ComponentActivity.RESULT_OK
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import com.nthlink.android.core.Core
import com.nthlink.android.core.RootVpn
import com.nthlink.android.core.model.DsConfig
import com.nthlink.android.core.model.Proxy
import com.nthlink.android.core.notification.createNotificationChannel
import com.nthlink.android.core.storage.getDsConfigFlow
import com.nthlink.android.core.storage.getVpnErrorFlow
import com.nthlink.android.core.storage.getVpnStatusFlow
import com.nthlink.android.core.storage.saveDsConfigJson
import com.nthlink.android.core.storage.saveVpnError
import com.nthlink.android.core.storage.saveVpnStatus
import com.nthlink.android.core.utils.TAG
import com.nthlink.android.core.utils.getProxiesFromDsConfig
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

internal class RootVpnImp(
    private val context: Context,
    private val scope: LifecycleCoroutineScope,
    private val registry: ActivityResultRegistry
) : RootVpn, DefaultLifecycleObserver {

    companion object {
        private const val KEY_VPN_SERVICE = "vpnServicePreparation"
        private const val KEY_POST_NOTIFICATION = "postNotificationPermission"
    }

    private lateinit var vpnServicePreparation: ActivityResultLauncher<Intent>
    private lateinit var postNotificationPermission: ActivityResultLauncher<String>

    private var statusJob: Job? = null
    private var errorJob: Job? = null
    private var dsConfigJob: Job? = null

    private var proxies: Array<out Proxy> = emptyArray()

    override var status: RootVpn.Status by Delegates.observable(RootVpn.Status.DISCONNECTED) { _, oldValue, newValue ->
        if (newValue != oldValue) onStatusChanged?.invoke(newValue)
    }
        private set
    override var onStatusChanged: ((vpnStatus: RootVpn.Status) -> Unit)? = null

    override var error: RootVpn.Error by Delegates.observable(RootVpn.Error.NO_ERROR) { _, oldValue, newValue ->
        if (newValue != oldValue && newValue != RootVpn.Error.NO_ERROR) {
            onErrorOccurred?.invoke(newValue)
            scope.launch { saveVpnError(context, RootVpn.Error.NO_ERROR) }
        }
    }
        private set
    override var onErrorOccurred: ((error: RootVpn.Error) -> Unit)? = null

    override var dsConfig: DsConfig? by Delegates.observable(null) { _, oldValue, newValue ->
        if (newValue != oldValue) onDsConfigChanged?.invoke(newValue)
    }
        private set
    override var onDsConfigChanged: ((config: DsConfig?) -> Unit)? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.i(TAG, "RootVpnImp onCreate: ")

        vpnServicePreparation = registry.register(
            KEY_VPN_SERVICE,
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK -> requirePermission()
                RESULT_CANCELED -> scope.launch {
                    saveVpnError(context, RootVpn.Error.NO_PERMISSION)
                }
            }
        }

        postNotificationPermission = registry.register(
            KEY_POST_NOTIFICATION, owner, ActivityResultContracts.RequestPermission()
        ) {
            // run VPN service no matter granted or not
            runVpnService()
        }

        // Create notification channel
        createNotificationChannel(context)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.i(TAG, "RootVpnImp onStart: ")

        dsConfigJob = scope.launch {
            getDsConfigFlow(context).collect { dsConfig = it }
        }

        errorJob = scope.launch {
            getVpnErrorFlow(context).collect { error = it }
        }

        statusJob = scope.launch {
            getVpnStatusFlow(context).collect { status = it }
        }
        onStatusChanged?.invoke(status)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.i(TAG, "RootVpnImp onStop: ")

        statusJob?.cancel()
        statusJob = null

        errorJob?.cancel()
        errorJob = null

        dsConfigJob?.cancel()
        dsConfigJob = null
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.i(TAG, "RootVpnImp onDestroy: ")

        onStatusChanged = null
        onErrorOccurred = null
        onDsConfigChanged = null
    }

    override fun connect(proxies: Array<out Proxy>) {
        this.proxies = proxies
        prepareVpnService { requirePermission() }
    }

    private fun prepareVpnService(ready: () -> Unit) {
        VpnService.prepare(context)?.let { vpnServicePreparation.launch(it) } ?: ready()
    }

    private fun requirePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requirePostNotificationPermission { runVpnService() }
        } else {
            runVpnService()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requirePostNotificationPermission(granted: () -> Unit) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val result = ContextCompat.checkSelfPermission(context, permission)

        if (result == PackageManager.PERMISSION_GRANTED) {
            granted()
        } else {
            postNotificationPermission.launch(permission)
        }
    }

    private fun runVpnService() = scope.launch(IO) {
        saveVpnStatus(context, RootVpn.Status.CONNECTING)

        // check internet
        if (!isOnline()) {
            saveVpnError(context, RootVpn.Error.NO_INTERNET)
            saveVpnStatus(context, RootVpn.Status.DISCONNECTED)
            return@launch
        }

        // start VPN
        if (proxies.isEmpty()) runWithRoot() else RootVpnService.start(context, proxies)
    }

    private suspend fun runWithRoot() {
        try {
            val configJson = Core.getConfig()
            saveDsConfigJson(context, configJson)
            RootVpnService.start(context, getProxiesFromDsConfig(configJson))
        } catch (e: Throwable) {
            Log.e(TAG, "get config error: ", e)
            saveVpnError(context, RootVpn.Error.DIRECTORY_SERVER_ERROR)
            saveVpnStatus(context, RootVpn.Status.DISCONNECTED)
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            // for other device how are able to connect with Ethernet
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            // for check internet over Bluetooth
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    override fun disconnect() = prepareVpnService { RootVpnService.stop(context) }

    override fun toggle() {
        if (status == RootVpn.Status.DISCONNECTED) connect() else disconnect()
    }
}