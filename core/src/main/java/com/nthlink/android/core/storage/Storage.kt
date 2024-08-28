package com.nthlink.android.core.storage

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nthlink.android.core.model.Config
import com.nthlink.android.core.utils.EMPTY
import com.nthlink.android.core.utils.JsonParser
import java.util.UUID

// Keys
private val keyConfig = stringPreferencesKey("config")
private val keyClientId = stringPreferencesKey("clientId")

// Preferences DataStore
internal suspend fun saveConfig(context: Context, config: Config) {
    val json = JsonParser.toJson(config.copy(servers = emptyList(), custom = EMPTY))
    context.rootPrefs.secureSave(keyConfig, json)
}

internal suspend fun readConfig(context: Context): Config {
    val json = context.rootPrefs.secureRead(keyConfig) ?: EMPTY
    return JsonParser.toConfig(json)
}

internal suspend fun saveClientId(context: Context, clientId: String) {
    context.rootPrefs.secureSave(keyClientId, clientId)
}

internal suspend fun readClientId(context: Context): String {
    return context.rootPrefs.secureRead(keyClientId) ?: run {
        val clientId = UUID.randomUUID().toString()
        saveClientId(context, clientId)
        clientId
    }
}