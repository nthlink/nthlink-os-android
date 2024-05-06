package com.nthlink.android.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.net.VpnService
import com.nthlink.android.core.R

private const val CHANNEL_ID = "RootVpn"
var channelName = "VPN"
var channelImportance = NotificationManager.IMPORTANCE_DEFAULT

var notificationTitle = "VPN"
var notificationMessage = "connected"
var notificationIcon = R.drawable.ic_baseline_android_24

internal fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        channelName,
        channelImportance
    )

    // Register the channel with the system; you can't change the importance
    // or other notification behaviors after this
    val manager = context.getSystemService(VpnService.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
}

internal fun createNotification(context: Context, contentIntent: PendingIntent): Notification {
    return Notification.Builder(context, CHANNEL_ID)
        .setContentTitle(notificationTitle)
        .setContentText(notificationMessage)
        .setSmallIcon(notificationIcon)
        .setContentIntent(contentIntent)
        .build()
}