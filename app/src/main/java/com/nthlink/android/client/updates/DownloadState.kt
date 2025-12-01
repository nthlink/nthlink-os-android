package com.nthlink.android.client.updates

/**
 * Sealed class representing download states
 */
sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progress: Int) : DownloadState
    data object Cancelled : DownloadState
    data object Completed : DownloadState
    data class Failed(val error: String) : DownloadState
}