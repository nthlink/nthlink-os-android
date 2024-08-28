package com.nthlink.android.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.nthlink.android.core.Core
import kotlinx.coroutines.flow.first

/**
 * Preferences DataStore
 */

internal val Context.rootPrefs: DataStore<Preferences> by preferencesDataStore(name = "root-prefs")

internal suspend fun <T> DataStore<Preferences>.save(key: Preferences.Key<T>, value: T) {
    edit { prefs -> prefs[key] = value }
}

internal suspend fun <T> DataStore<Preferences>.read(key: Preferences.Key<T>): T? {
    return data.first()[key]
}

internal suspend fun <T> DataStore<Preferences>.remove(key: Preferences.Key<T>) {
    edit { prefs -> prefs.remove(key) }
}

internal suspend fun DataStore<Preferences>.secureSave(
    key: Preferences.Key<String>,
    value: String
) {
    val encryptedValue = Core.encrypt(value)
    save(key, encryptedValue)
}

internal suspend fun DataStore<Preferences>.secureRead(key: Preferences.Key<String>): String? {
    val encryptedValue = read(key)
    return encryptedValue?.let { Core.decrypt(it) }
}