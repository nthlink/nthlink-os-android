package com.nthlink.android.client.updates

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import com.nthlink.android.client.App.Companion.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PlayImmediateUpdate(
    context: Context,
    private val registry: ActivityResultRegistry,
    private val scope: CoroutineScope
) : InAppUpdate(), ActivityResultCallback<ActivityResult> {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)
    private val appUpdateType = AppUpdateType.IMMEDIATE

    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        launcher = registry.register(
            "immediateUpdate",
            owner,
            StartIntentSenderForResult(),
            this
        )

        checkForUpdate(true)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        // Checks that the update is not stalled during 'onResume()'.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startAnUpdate(info)
            }
        }
    }

    override fun checkForUpdate(silent: Boolean) {
        scope.launch(IO) {
            try {
                val info = appUpdateManager.appUpdateInfo.await()
                if (hasNewUpdate(info)) {
                    // always be silent
                    _updateResultFlow.emit(UpdateResult.CheckHasNewUpdate(true))
                    if (!silent) startAnUpdate(info)
                } else {
                    _updateResultFlow.emit(UpdateResult.CheckUpToDate(silent))
                }
            } catch (e: Throwable) {
                Log.e(TAG, "checkForUpdate error:", e)
                _updateResultFlow.emit(UpdateResult.CheckFailed(silent))
            }
        }
    }

    private fun hasNewUpdate(info: AppUpdateInfo): Boolean {
        val isUpdateAvailable = info.updateAvailability() == UPDATE_AVAILABLE
        val isUpdateAllowed = info.isUpdateTypeAllowed(appUpdateType)
        return isUpdateAvailable && isUpdateAllowed
    }

    private fun startAnUpdate(info: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            info,
            launcher,
            AppUpdateOptions.newBuilder(appUpdateType).build()
        )
    }

    override fun onActivityResult(result: ActivityResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                // The user has accepted the update.
                // For immediate updates, you might not receive this callback because the update should already be finished by the time control is given back to your app.
                scope.launch { _updateResultFlow.emit(UpdateResult.UpdateOk) }
            }

            RESULT_CANCELED -> {
                // The user has denied or canceled the update.
                scope.launch { _updateResultFlow.emit(UpdateResult.UpdateCanceled) }
            }

            RESULT_IN_APP_UPDATE_FAILED -> {
                // Some other error prevented either the user from providing consent or the update from proceeding.
                scope.launch { _updateResultFlow.emit(UpdateResult.UpdateFailed) }
            }
        }
    }
}