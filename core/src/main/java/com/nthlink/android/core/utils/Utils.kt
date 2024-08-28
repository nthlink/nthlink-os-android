package com.nthlink.android.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

// Constants
internal const val TAG = "RootVpn"
internal const val EMPTY = ""

internal fun Context.isOnline(): Boolean {
    val connectivityManager = getSystemService(ConnectivityManager::class.java)

    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities =
        connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        // for other device how are able to connect with Ethernet
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        // for check internet over Bluetooth
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
    }
}