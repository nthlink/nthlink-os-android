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
        NO_PERMISSION,
        NO_INTERNET,
        INVALID_CONFIG,
        VPN_SERVICE_NOT_EXISTS,
        CREATE_TUN_FAILED
    }

    val statusFlow: StateFlow<Status>
    val errorFlow: SharedFlow<Error>
    val status: Status get() = statusFlow.value

    fun connect()
    fun disconnect()
    fun toggle() = if (status == Status.DISCONNECTED) connect() else disconnect()
    suspend fun getConfig(): Config

    class Builder {
        fun build(context: Context, lifecycle: Lifecycle): Root {
            val rootVpnClient = RootVpnClient(context)
            lifecycle.addObserver(rootVpnClient)
            return rootVpnClient
        }

        fun build(activity: ComponentActivity): Root {
            return build(activity, activity.lifecycle)
        }

        fun build(fragment: Fragment): Root {
            return build(fragment.requireContext(), fragment.lifecycle)
        }
    }

    companion object {
        fun feedback(
            feedbackType: String,
            description: String = EMPTY,
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