package com.nthlink.android.core

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.nthlink.android.core.model.Config
import com.nthlink.android.core.utils.EMPTY
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface Root {
    enum class Status {
        DISCONNECTED,
        INITIALIZING,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    enum class Error {
        GET_CONFIG_ERROR,
        NO_PROXY_AVAILABLE,

        // leaf
        NO_PERMISSION,
        NO_INTERNET,
        INVALID_CONFIG,
        VPN_SERVICE_NOT_EXISTS,
        CREATE_TUN_FAILED,
        START_LEAF_FAILED
    }

    sealed interface DiagnosticResult {
        data object ErrNoInternet : DiagnosticResult
        data class Ok(val reportId: String) : DiagnosticResult
    }

    val statusFlow: StateFlow<Status>
    val errorFlow: SharedFlow<Error>
    val status: Status get() = statusFlow.value
    val diagnosticResultFlow: SharedFlow<DiagnosticResult>

    fun connect(config: String = EMPTY)
    fun disconnect()
    fun toggle() = if (status == Status.DISCONNECTED) connect() else disconnect()
    fun startDiagnostics()
    suspend fun getConfig(): Config?

    class Builder {
        fun build(context: Context, lifecycle: Lifecycle): Root {
            val rootVpnLeaf = RootVpnClient(context)
            lifecycle.addObserver(rootVpnLeaf)
            return rootVpnLeaf
        }

        fun build(activity: ComponentActivity): Root {
            return build(activity, activity.lifecycle)
        }

        fun build(fragment: Fragment): Root {
            return build(fragment.requireContext(), fragment.lifecycle)
        }
    }

    companion object {
        fun getConfig(): String {
            return Core.getConfig()
        }

        fun feedback(
            context: Context,
            feedbackType: String,
            description: String = EMPTY,
            errorCode: String = EMPTY,
            errorMessage: String = EMPTY,
            appVersion: String = EMPTY,
            email: String = EMPTY
        ) {
            Core.feedback(
                feedbackType,
                description,
                appVersion,
                email
            )
        }
    }
}