package com.nthlink.android.client.updates

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nthlink.android.client.App
import com.nthlink.android.client.storage.datastore.ApkDownloadDataStore
import com.nthlink.android.client.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ApkDownloadReceiver : BroadcastReceiver(), KoinComponent {
    companion object {
        const val ACTION_OPEN_APK_UPDATE = "action_open_apk_update"
    }

    private val dataStore: ApkDownloadDataStore by inject()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
                Log.d(App.Companion.TAG, "Download notification clicked")

                // goAsync() extends BroadcastReceiver execution time for async operations
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (dataStore.readApkDownloadId() == null) return@launch

                        val intent = Intent(context, MainActivity::class.java).apply {
                            action = ACTION_OPEN_APK_UPDATE
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        context.startActivity(intent)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}