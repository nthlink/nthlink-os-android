package com.nthlink.android.core.utils

import android.content.Context
import android.net.NetworkCapabilities.TRANSPORT_BLUETOOTH
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// Constants
internal const val TAG = "RootVpn"
const val EMPTY = ""
const val ZERO = 0
const val NO_RESOURCE = ZERO

// Patterns for formatting
internal const val PATTERN_LOCALIZED_ZONE_OFFSET = "O"
internal const val PATTERN_DATE_TIME = "yyyy-MM-dd HH:mm:ss"

internal fun getTimeZoneAbbreviation(): String {
    return ZonedDateTime.now().format(DateTimeFormatter.ofPattern(PATTERN_LOCALIZED_ZONE_OFFSET))
}

internal fun nowInUtc(pattern: String = PATTERN_DATE_TIME): String {
    return OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(pattern))
}

fun isOnline(context: Context): Boolean = context.getConnectivityManager().run {
    val network = activeNetwork ?: return false
    val networkCapabilities = getNetworkCapabilities(network) ?: return false
    return@run when {
        networkCapabilities.hasTransport(TRANSPORT_WIFI) -> true
        networkCapabilities.hasTransport(TRANSPORT_CELLULAR) -> true
        // for other device how are able to connect with Ethernet
        networkCapabilities.hasTransport(TRANSPORT_ETHERNET) -> true
        // for check internet over Bluetooth
        networkCapabilities.hasTransport(TRANSPORT_BLUETOOTH) -> true
        else -> false
    }
}
