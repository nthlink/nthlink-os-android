package com.nthlink.android.core.utils

import android.content.Context
import android.net.ConnectivityManager

fun Context.getConnectivityManager() = getSystemService(ConnectivityManager::class.java)