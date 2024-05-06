package com.nthlink.android.core

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nthlink.android.core.model.DsConfig
import com.nthlink.android.core.model.Proxy
import com.nthlink.android.core.notification.channelImportance
import com.nthlink.android.core.notification.channelName
import com.nthlink.android.core.notification.notificationIcon
import com.nthlink.android.core.notification.notificationMessage
import com.nthlink.android.core.notification.notificationTitle
import com.nthlink.android.core.utils.EMPTY
import com.nthlink.android.core.vpn.RootVpnImp

interface RootVpn {

    companion object {

        fun getStatus(code: Int): Status {
            return Status.entries.find { status ->
                status.code == code
            } ?: throw IllegalArgumentException("Unknown RootVpn Status code")
        }

        fun getError(code: Int): Error {
            return Error.entries.find { error ->
                error.code == code
            } ?: throw IllegalArgumentException("Unknown RootVpn Error code")
        }

        suspend fun feedback(
            feedbackType: String,
            description: String = EMPTY,
            appVersion: String = EMPTY,
            email: String = EMPTY
        ) {
            Core.feedback(feedbackType, description, appVersion, email)
        }
    }

    val status: Status

    var onStatusChanged: ((vpnStatus: Status) -> Unit)?

    val error: Error

    var onErrorOccurred: ((error: Error) -> Unit)?

    val dsConfig: DsConfig?

    var onDsConfigChanged: ((config: DsConfig?) -> Unit)?

    fun connect(proxies: Array<out Proxy> = emptyArray())

    fun disconnect()

    fun toggle()

    class Builder {

        fun setChannelName(name: String) = apply { channelName = name }

        fun setChannelImportance(importance: Int) = apply { channelImportance = importance }

        fun setNotificationTitle(title: String) = apply { notificationTitle = title }

        fun setNotificationMessage(message: String) = apply { notificationMessage = message }

        fun setNotificationIcon(icon: Int) = apply { notificationIcon = icon }

        fun build(activity: ComponentActivity): RootVpn {
            return RootVpnImp(
                activity,
                activity.lifecycleScope,
                activity.activityResultRegistry
            ).also { activity.lifecycle.addObserver(it) }
        }

        fun build(fragment: Fragment): RootVpn {
            return RootVpnImp(
                fragment.requireContext(),
                fragment.lifecycleScope,
                fragment.requireActivity().activityResultRegistry
            ).also { fragment.lifecycle.addObserver(it) }
        }
    }

    enum class Status(val code: Int) {
        DISCONNECTED(0),
        CONNECTING(1),
        CONNECTED(2),
        DISCONNECTING(3)
    }

    enum class Error(val code: Int) {
        NO_ERROR(0),
        NO_PERMISSION(1),
        NO_INTERNET(2),
        DIRECTORY_SERVER_ERROR(3),
        VPN_SERVICE_ERROR(4)
    }
}