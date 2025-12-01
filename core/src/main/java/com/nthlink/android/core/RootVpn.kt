package com.nthlink.android.core

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.nthlink.android.core.Root.DiagnosticResult
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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal abstract class RootVpn(private val context: Context) : Root, DefaultLifecycleObserver {
    private val _statusFlow = MutableStateFlow(Status.DISCONNECTED)
    override val statusFlow: StateFlow<Status> get() = _statusFlow

    private val _errorFlow = MutableSharedFlow<Error>()
    override val errorFlow: SharedFlow<Error> get() = _errorFlow

    private val _diagnosticResultFlow = MutableSharedFlow<DiagnosticResult>()
    override val diagnosticResultFlow: SharedFlow<DiagnosticResult> get() = _diagnosticResultFlow.asSharedFlow()

    private lateinit var scope: LifecycleCoroutineScope

    override fun onCreate(owner: LifecycleOwner) {
        scope = owner.lifecycleScope
    }

    override fun connect(config: String) {
        scope.launch(IO) {
            updateStatus(Status.INITIALIZING)

            // check internet
            if (!isOnline(context)) {
                updateStatus(Status.DISCONNECTED)
                emitError(Error.NO_INTERNET)
                return@launch
            }

            // check if there is a config from client
            if (config.isNotEmpty()) {
                runVpn(config)
            } else {
                getConfigFromDirectoryServer()
            }
        }
    }

    private suspend fun getConfigFromDirectoryServer() {
        try {
            // get config from Directory Server
            val config = JsonParser.toConfig(Core.getConfig())

            // save config
            saveConfig(context, config)

            // run VPN
            if (config.useCustomConfig) {
                if (config.customConfig.isEmpty()) {
                    updateStatus(Status.DISCONNECTED)
                    emitError(Error.INVALID_CONFIG)
                    return
                }

                runVpn(config.customConfig)
            } else {
                if (config.servers.isEmpty()) {
                    updateStatus(Status.DISCONNECTED)
                    emitError(Error.NO_PROXY_AVAILABLE)
                    return
                }

                runVpn(config.servers)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "get config error: ", e)
            updateStatus(Status.DISCONNECTED)
            emitError(Error.GET_CONFIG_ERROR)
        }
    }

    abstract suspend fun runVpn(servers: List<Config.Server>)

    abstract suspend fun runVpn(config: String)

    protected fun updateStatus(status: Status) {
        _statusFlow.value = status
    }

    protected suspend fun emitError(error: Error) = _errorFlow.emit(error)

    override suspend fun getConfig(): Config? = readConfig(context)

    override fun startDiagnostics() {
        scope.launch(IO) {
            // disconnect VPN
            if (status != Status.DISCONNECTED) disconnect()

            // check internet
            if (!isOnline(context)) {
                _diagnosticResultFlow.emit(DiagnosticResult.ErrNoInternet)
                return@launch
            }

            val reportId = Core.startDiagnostics()

            _diagnosticResultFlow.emit(DiagnosticResult.Ok(reportId))
        }
    }
}