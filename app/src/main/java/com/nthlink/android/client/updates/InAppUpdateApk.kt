package com.nthlink.android.client.updates

import android.util.Log
import com.nthlink.android.client.App.Companion.TAG
import com.nthlink.android.client.BuildConfig
import com.nthlink.android.core.Root
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class InAppUpdateApk(
    private val scope: CoroutineScope,
    private val root: Root,
    private val navigateToApkUpdate: (version: String, url: String) -> Unit
) : InAppUpdate() {

    override fun checkUpdate(updateIfAvailable: Boolean) {
        scope.launch(IO) {
            try {
                val androidPlatform = root.getConfig()
                    ?.currentVersions?.find { it.appName.lowercase() == "nthlink" }
                    ?.platforms?.find { it.os.lowercase() == "android" }

                val latestVersion = androidPlatform?.version
                    ?: throw IllegalStateException("latest version not found")

                if (isNewUpdateAvailable(latestVersion)) {
                    _inAppUpdateFlow.emit(InAppUpdateMessage.NewUpdateAvailable)
                    if (updateIfAvailable) withContext(Main) {
                        navigateToApkUpdate(latestVersion, androidPlatform.url)
                    }
                } else {
                    _inAppUpdateFlow.emit(InAppUpdateMessage.UpToDate(updateIfAvailable))
                }
            } catch (e: Throwable) {
                Log.e(TAG, "checkUpdate error:", e)
                _inAppUpdateFlow.emit(InAppUpdateMessage.CheckFailed(updateIfAvailable))
            }
        }
    }

    private fun isNewUpdateAvailable(latestVersion: String): Boolean {
        val latestVersionArr = latestVersion.split('.')
        val currentVersionArr = BuildConfig.VERSION_NAME.split('.')

        val maxLength = max(latestVersionArr.size, currentVersionArr.size)

        for (i in 0 until maxLength) {
            val latestVersionCode = latestVersionArr.getOrNull(i)?.toInt() ?: 0
            val currentVersionCode = currentVersionArr.getOrNull(i)?.toInt() ?: 0

            return when {
                latestVersionCode > currentVersionCode -> true
                latestVersionCode < currentVersionCode -> false
                else -> continue
            }
        }

        return false
    }
}