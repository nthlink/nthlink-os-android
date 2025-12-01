package com.nthlink.android.client.ui.update

import androidx.lifecycle.ViewModel
import com.nthlink.android.client.updates.ApkDownloadManager
import com.nthlink.android.client.updates.DownloadState
import kotlinx.coroutines.flow.StateFlow

class ApkUpdateViewModel(private val downloadManager: ApkDownloadManager) : ViewModel() {

    val downloadState: StateFlow<DownloadState> = downloadManager.downloadState

    fun startDownload(version: String, downloadUrl: String) {
        downloadManager.startDownload(version, downloadUrl)
    }

    fun cancelDownload() {
        downloadManager.cancelDownload()
    }

    override fun onCleared() {
        super.onCleared()
        downloadManager.cleanup()
    }
}