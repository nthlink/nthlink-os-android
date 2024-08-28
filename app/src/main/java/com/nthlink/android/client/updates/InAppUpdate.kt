package com.nthlink.android.client.updates

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.lifecycleScope
import com.nthlink.android.client.utils.installFromGooglePlay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed interface UpdateResult {
    data class CheckHasNewUpdate(val silent: Boolean) : UpdateResult
    data class CheckUpToDate(val silent: Boolean) : UpdateResult
    data class CheckFailed(val silent: Boolean) : UpdateResult
    data object UpdateOk : UpdateResult
    data object UpdateCanceled : UpdateResult
    data object UpdateFailed : UpdateResult
}

abstract class InAppUpdate : DefaultLifecycleObserver {
    companion object {
        fun getInstance(activity: AppCompatActivity): InAppUpdate {
            val inAppUpdate = if (installFromGooglePlay(activity)) {
                PlayImmediateUpdate(
                    activity,
                    activity.activityResultRegistry,
                    activity.lifecycleScope
                )
            } else {
                ApkUpdate(activity.lifecycleScope)
            }

            activity.lifecycle.addObserver(inAppUpdate)
            return inAppUpdate
        }
    }

    protected val _updateResultFlow = MutableSharedFlow<UpdateResult>()
    val updateResultFlow: SharedFlow<UpdateResult> get() = _updateResultFlow

    abstract fun checkForUpdate(silent: Boolean = false)
}