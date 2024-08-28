package com.nthlink.android.core

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.nthlink.android.core.Root.Error
import com.nthlink.android.core.Root.Status
import com.nthlink.android.core.model.Config
import com.nthlink.android.core.storage.readConfig
import com.nthlink.android.core.storage.saveConfig
import com.nthlink.android.core.utils.JsonParser
import com.nthlink.android.core.utils.TAG
import com.nthlink.android.core.utils.isOnline
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal abstract class RootVpn(private val context: Context) : Root, DefaultLifecycleObserver {
    private val _statusFlow = MutableStateFlow(Status.DISCONNECTED)
    override val statusFlow: StateFlow<Status> = _statusFlow

    private val _errorFlow = MutableSharedFlow<Error>()
    override val errorFlow: SharedFlow<Error> = _errorFlow

    private lateinit var scope: LifecycleCoroutineScope

    override fun onCreate(owner: LifecycleOwner) {
        scope = owner.lifecycleScope
    }

    override fun connect() {
        scope.launch(IO) {
            updateStatus(Status.INITIALIZING)

            // check internet
            if (!context.isOnline()) {
                updateStatus(Status.DISCONNECTED)
                emitError(Error.NO_INTERNET)
                return@launch
            }

            try {
                // get config from Directory Server
                val config = JsonParser.toConfig(Core.getConfig())

                // save config
                saveConfig(context, config)

                // run VPN
                if (config.useCustom) runVpn(config.custom) else runVpn(config.servers)
            } catch (e: Throwable) {
                Log.e(TAG, "get config error: ", e)
                updateStatus(Status.DISCONNECTED)
                emitError(Error.GET_CONFIG_ERROR)
            }
        }
    }

    abstract suspend fun runVpn(servers: List<Config.Server>)

    abstract suspend fun runVpn(config: String)

    protected fun updateStatus(status: Status) {
        _statusFlow.value = status
    }

    protected suspend fun emitError(error: Error) = _errorFlow.emit(error)

    override suspend fun getConfig(): Config = readConfig(context)
}