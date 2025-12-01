package com.nthlink.android.client.storage.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private val Context.apkDownloadDataStore: DataStore<Preferences> by preferencesDataStore(name = "apk-download-prefs")

class ApkDownloadDataStore(private val context: Context) {
    companion object {
        private val APK_DOWNLOAD_ID = longPreferencesKey("apkDownloadId")
    }

    suspend fun saveApkDownloadId(downloadId: Long) {
        context.apkDownloadDataStore.save(APK_DOWNLOAD_ID, downloadId)
    }

    suspend fun readApkDownloadId(): Long? {
        return context.apkDownloadDataStore.read(APK_DOWNLOAD_ID)
    }

    suspend fun removeApkDownloadId() {
        context.apkDownloadDataStore.remove(APK_DOWNLOAD_ID)
    }
}