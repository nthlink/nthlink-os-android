package com.nthlink.android.client.updates

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.nthlink.android.client.App.Companion.TAG
import com.nthlink.android.client.BuildConfig
import com.nthlink.android.client.utils.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL
import kotlin.math.max

class ApkUpdate(private val scope: CoroutineScope) : InAppUpdate() {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        checkForUpdate(true)
    }

    override fun checkForUpdate(silent: Boolean) {
        scope.launch(IO) {
            try {
                // call API
                val json = URL("https://www.downloadnth.com/versions.json").readText()

                // parse JSON
                val jsonElement = JsonParser.toJsonElement(json)
                val androidObject = jsonElement.jsonObject["android"]!!
                val remoteVersion = androidObject.jsonObject["version"]!!.jsonPrimitive.content

                // result
                if (hasNewUpdate(remoteVersion)) {
                    _updateResultFlow.emit(UpdateResult.CheckHasNewUpdate(silent))
                } else {
                    _updateResultFlow.emit(UpdateResult.CheckUpToDate(silent))
                }
            } catch (e: Throwable) {
                Log.e(TAG, "checkForUpdate error:", e)
                _updateResultFlow.emit(UpdateResult.CheckFailed(silent))
            }
        }
    }

    private fun hasNewUpdate(remoteVersion: String): Boolean {
        val remoteVersionArr = remoteVersion.split('.')
        val localVersionArr = BuildConfig.VERSION_NAME.split('.')

        val maxLength = max(remoteVersionArr.size, localVersionArr.size)

        for (i in 0 until maxLength) {
            val remoteVersionCode = remoteVersionArr.getOrNull(i)?.toInt() ?: 0
            val localVersionCode = localVersionArr.getOrNull(i)?.toInt() ?: 0

            return when {
                remoteVersionCode > localVersionCode -> true
                remoteVersionCode < localVersionCode -> false
                else -> continue
            }
        }

        return false
    }
}