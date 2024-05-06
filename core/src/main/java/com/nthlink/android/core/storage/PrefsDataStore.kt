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

private val Context.prefs: DataStore<Preferences> by preferencesDataStore(name = "root-prefs")

val Context.prefsFlow get() = prefs.data

suspend fun <T> save(context: Context, key: Preferences.Key<T>, value: T) {
    context.prefs.edit { prefs -> prefs[key] = value }
}

suspend fun <T> read(context: Context, key: Preferences.Key<T>): T? {
    return context.prefsFlow.first()[key]
}

suspend fun secureSave(context: Context, key: Preferences.Key<String>, value: String) {
    val encryptedValue = Core.encrypt(value)
    save(context, key, encryptedValue)
}

suspend fun secureRead(context: Context, key: Preferences.Key<String>): String? {
    val encryptedValue = read(context, key)
    return encryptedValue?.let { Core.decrypt(it) }
}