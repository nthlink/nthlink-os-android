package com.nthlink.android.client.updates

import android.app.DownloadManager
import android.app.DownloadManager.Request.NETWORK_MOBILE
import android.app.DownloadManager.Request.NETWORK_WIFI
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Environment
import android.widget.Toast
import androidx.core.net.toUri
import com.nthlink.android.client.storage.datastore.ApkDownloadDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Wrapper class for Android DownloadManager to handle APK downloads
 * Provides progress tracking via StateFlow and handles download lifecycle
 */
class ApkDownloadManager(context: Context, private val dataStore: ApkDownloadDataStore) {
    companion object {
        fun getApksDir(): File {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            return File(dir, "nthlink_updates")
        }

        fun getApkFileName(version: String): String {
            return "nthlink-${version}-release.apk"
        }

        fun getApkFile(version: String): File {
            return File(getApksDir(), getApkFileName(version))
        }

        fun viewDownloads(context: Context) {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "Unable to open Downloads.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    private val scope = CoroutineScope(IO)

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private var currentDownloadId: Long? = null
    private var progressMonitorJob: Job? = null

    init {
        scope.launch {
            dataStore.readApkDownloadId()?.let { downloadId ->
                currentDownloadId = downloadId
                resumeProgressMonitoring(downloadId)
            }
        }
    }

    /**
     * Start downloading an APK file
     */
    fun startDownload(version: String, downloadUrl: String) {
        cancelDownload()

        // Remove the existing APK files.
        val dir = getApksDir()
        if (dir.exists() && dir.isDirectory) dir.deleteRecursively()

        val request = DownloadManager.Request(downloadUrl.toUri()).apply {
            setTitle("nthlink Update")
            setDescription("Downloading version $version")
            setNotificationVisibility(VISIBILITY_VISIBLE)
            setDestinationUri(getApkFile(version).toUri())
            setAllowedNetworkTypes(NETWORK_WIFI or NETWORK_MOBILE)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
        }

        val downloadId = downloadManager.enqueue(request)
        startProgressMonitoring(downloadId)
        saveDownloadId(downloadId)
    }

    /**
     * Cancel the current download
     */
    fun cancelDownload() {
        currentDownloadId?.let { downloadId ->
            downloadManager.remove(downloadId)
            stopProgressMonitoring()
            removeDownloadId()
            _downloadState.value = DownloadState.Cancelled
        }
    }

    /**
     * Resume progress monitoring if a download is in progress
     */
    private fun resumeProgressMonitoring(downloadId: Long) {
        if (queryDownloadStatus(downloadId) is DownloadState.Downloading) {
            startProgressMonitoring(downloadId)
        }
    }

    /**
     * Start monitoring download progress
     */
    private fun startProgressMonitoring(downloadId: Long) {
        stopProgressMonitoring()

        progressMonitorJob = scope.launch {
            while (isActive) {
                val state = queryDownloadStatus(downloadId)
                _downloadState.value = state

                // Stop monitoring if download is not downloading
                if (state !is DownloadState.Downloading) break

                delay(500) // Update every 500ms
            }
        }
    }

    /**
     * Stop monitoring download progress
     */
    private fun stopProgressMonitoring() {
        progressMonitorJob?.cancel()
        progressMonitorJob = null
    }

    /**
     * Query the current download status
     */
    private fun queryDownloadStatus(downloadId: Long): DownloadState {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor: Cursor? = downloadManager.query(query)

        cursor?.use {
            if (it.moveToFirst()) {
                return parseDownloadStatus(it)
            }
        }

        return DownloadState.Idle
    }

    /**
     * Parse download status from cursor
     */
    private fun parseDownloadStatus(cursor: Cursor): DownloadState {
        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = cursor.getInt(statusIndex)

        return when (status) {
            DownloadManager.STATUS_PENDING, DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED -> {
                val bytesDownloadedIndex =
                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                val bytesTotal = cursor.getLong(bytesTotalIndex)

                DownloadState.Downloading(calculateProgress(bytesDownloaded, bytesTotal))
            }

            DownloadManager.STATUS_SUCCESSFUL -> {
                removeDownloadId()
                DownloadState.Completed
            }


            DownloadManager.STATUS_FAILED -> {
                removeDownloadId()
                val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                val reason = cursor.getInt(reasonIndex)
                DownloadState.Failed(getFailureReason(reason))
            }

            else -> DownloadState.Idle
        }
    }

    private fun calculateProgress(bytesDownloaded: Long, bytesTotal: Long): Int {
        return if (bytesTotal > 0) ((bytesDownloaded * 100) / bytesTotal).toInt() else 0
    }

    /**
     * Get human-readable failure reason
     */
    private fun getFailureReason(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "No external storage device found"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            DownloadManager.ERROR_FILE_ERROR -> "Storage error"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient storage space"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP response"
            DownloadManager.ERROR_UNKNOWN -> "Unknown error"
            else -> "Download failed (code: $reason)"
        }
    }

    private fun saveDownloadId(downloadId: Long) {
        scope.launch {
            dataStore.saveApkDownloadId(downloadId)
        }
        currentDownloadId = downloadId
    }

    private fun removeDownloadId() {
        scope.launch {
            dataStore.removeApkDownloadId()
        }
        currentDownloadId = null
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopProgressMonitoring()
        scope.cancel()
    }
}
