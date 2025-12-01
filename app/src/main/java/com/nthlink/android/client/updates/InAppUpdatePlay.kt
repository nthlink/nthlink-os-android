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
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
import com.nthlink.android.client.App.Companion.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InAppUpdatePlay(
    context: Context,
    private val registry: ActivityResultRegistry,
    private val scope: CoroutineScope
) : InAppUpdate(), ActivityResultCallback<ActivityResult> {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        launcher = registry.register(
            "immediateUpdate",
            owner,
            StartIntentSenderForResult(),
            this
        )
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        // Checks that the update is not stalled during 'onResume()'.
        scope.launch(IO) {
            val appUpdateInfo = appUpdateManager.appUpdateInfo.await()
            if (appUpdateInfo.updateAvailability() == DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdate(appUpdateInfo)
            }
        }
    }

    override fun checkUpdate(updateIfAvailable: Boolean) {
        scope.launch(IO) {
            try {
                val appUpdateInfo = appUpdateManager.appUpdateInfo.await()
                if (appUpdateInfo.isNewUpdateAvailable()) {
                    _inAppUpdateFlow.emit(InAppUpdateMessage.NewUpdateAvailable)
                    if (updateIfAvailable) startUpdate(appUpdateInfo)
                } else {
                    _inAppUpdateFlow.emit(InAppUpdateMessage.UpToDate(updateIfAvailable))
                }
            } catch (e: Throwable) {
                Log.e(TAG, "checkUpdate error:", e)
                _inAppUpdateFlow.emit(InAppUpdateMessage.CheckFailed(updateIfAvailable))
            }
        }
    }

    private fun AppUpdateInfo.isNewUpdateAvailable(): Boolean {
        return updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
    }

    private fun startUpdate(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            launcher,
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        )
    }

    override fun onActivityResult(result: ActivityResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                // The user has accepted the update.
                // For immediate updates, you might not receive this callback because the update should already be finished by the time control is given back to your app.
                scope.launch { _inAppUpdateFlow.emit(InAppUpdateMessage.UpdateOk) }
            }

            RESULT_CANCELED -> {
                // The user has denied or canceled the update.
                scope.launch { _inAppUpdateFlow.emit(InAppUpdateMessage.UpdateCanceled) }
            }

            RESULT_IN_APP_UPDATE_FAILED -> {
                // Some other error prevented either the user from providing consent or the update from proceeding.
                scope.launch { _inAppUpdateFlow.emit(InAppUpdateMessage.UpdateFailed) }
            }
        }
    }
}
