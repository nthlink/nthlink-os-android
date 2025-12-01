package com.nthlink.android.client.updates

import androidx.lifecycle.DefaultLifecycleObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface InAppUpdateMessage {
    data object NewUpdateAvailable : InAppUpdateMessage
    data class UpToDate(val notifyUser: Boolean) : InAppUpdateMessage
    data class CheckFailed(val notifyUser: Boolean) : InAppUpdateMessage
    data object UpdateOk : InAppUpdateMessage
    data object UpdateCanceled : InAppUpdateMessage
    data object UpdateFailed : InAppUpdateMessage
}

abstract class InAppUpdate : DefaultLifecycleObserver {
    protected val _inAppUpdateFlow = MutableSharedFlow<InAppUpdateMessage>()
    val inAppUpdateFlow = _inAppUpdateFlow.asSharedFlow()

    abstract fun checkUpdate(updateIfAvailable: Boolean)
}