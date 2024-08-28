package com.nthlink.android.client.storage.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.nthlink.android.client.utils.ZERO

// Keys
private val keyAgreePrivacy = booleanPreferencesKey("agreePrivacy")
private val keyConnectedCount = intPreferencesKey("connectedCount")
private val keyHasLandingPageShown = booleanPreferencesKey("hasLandingPageShown")

// Preferences DataStore
internal suspend fun saveAgreePrivacy(context: Context, agreePrivacy: Boolean) {
    context.prefs.save(keyAgreePrivacy, agreePrivacy)
}

internal suspend fun readAgreePrivacy(context: Context): Boolean {
    return context.prefs.read(keyAgreePrivacy) ?: false
}

internal suspend fun saveConnectedCount(context: Context) {
    val count = readConnectedCount(context)
    context.prefs.save(keyConnectedCount, count + 1)
}

internal suspend fun readConnectedCount(context: Context): Int {
    return context.prefs.read(keyConnectedCount) ?: ZERO
}

internal suspend fun saveHasLandingPageShown(context: Context, hasShown: Boolean) {
    context.prefs.save(keyHasLandingPageShown, hasShown)
}

internal suspend fun readHasLandingPageShown(context: Context): Boolean {
    return context.prefs.read(keyHasLandingPageShown) ?: false
}