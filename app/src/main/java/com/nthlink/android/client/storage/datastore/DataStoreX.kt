package com.nthlink.android.client.storage.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

/**
 * Preferences DataStore Extension Functions
 */

internal suspend fun <T> DataStore<Preferences>.save(key: Preferences.Key<T>, value: T) {
    edit { prefs -> prefs[key] = value }
}

internal suspend fun <T> DataStore<Preferences>.read(key: Preferences.Key<T>): T? {
    return data.first()[key]
}

internal suspend fun <T> DataStore<Preferences>.remove(key: Preferences.Key<T>) {
    edit { prefs -> prefs.remove(key) }
}