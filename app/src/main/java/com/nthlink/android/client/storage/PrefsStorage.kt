package com.nthlink.android.client.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.nthlink.android.core.storage.read
import com.nthlink.android.core.storage.save
import tw.hankli.brookray.core.constant.ZERO

// Keys
private val keyAgreePrivacy = booleanPreferencesKey("agreePrivacy")
private val keyConnectedCount = intPreferencesKey("connectedCount")
private val keyHasLandingPageShown = booleanPreferencesKey("hasLandingPageShown")

// Preferences DataStore
internal suspend fun saveAgreePrivacy(context: Context, agreePrivacy: Boolean) {
    save(context, keyAgreePrivacy, agreePrivacy)
}

internal suspend fun readAgreePrivacy(context: Context): Boolean {
    return read(context, keyAgreePrivacy) ?: false
}

internal suspend fun saveConnectedCount(context: Context) {
    val count = readConnectedCount(context)
    save(context, keyConnectedCount, count + 1)
}

internal suspend fun readConnectedCount(context: Context): Int {
    return read(context, keyConnectedCount) ?: ZERO
}

internal suspend fun saveHasLandingPageShown(context: Context, hasShown: Boolean) {
    save(context, keyHasLandingPageShown, hasShown)
}

internal suspend fun readHasLandingPageShown(context: Context): Boolean {
    return read(context, keyHasLandingPageShown) ?: false
}