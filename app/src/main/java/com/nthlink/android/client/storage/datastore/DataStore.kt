package com.nthlink.android.client.storage.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

/**
 * Preferences DataStore
 */

internal val Context.prefs: DataStore<Preferences> by preferencesDataStore(name = "nthlink-prefs")

internal suspend fun <T> DataStore<Preferences>.save(key: Preferences.Key<T>, value: T) {
    edit { prefs -> prefs[key] = value }
}

internal suspend fun <T> DataStore<Preferences>.read(key: Preferences.Key<T>): T? {
    return data.first()[key]
}

internal suspend fun <T> DataStore<Preferences>.remove(key: Preferences.Key<T>) {
    edit { prefs -> prefs.remove(key) }
}