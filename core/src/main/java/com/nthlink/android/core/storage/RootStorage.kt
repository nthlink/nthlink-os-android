package com.nthlink.android.core.storage

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nthlink.android.core.Core
import com.nthlink.android.core.RootVpn
import com.nthlink.android.core.model.DsConfig
import com.nthlink.android.core.utils.EMPTY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// Keys
private val keyConfig = stringPreferencesKey("config")
private val keyVpnStatus = intPreferencesKey("vpnStatus")
private val keyVpnError = intPreferencesKey("vpnError")
private val keyClientId = stringPreferencesKey("clientId")

// Preferences DataStore
internal suspend fun saveDsConfigJson(context: Context, configJson: String) {
    secureSave(context, keyConfig, configJson)
}

internal suspend fun readDsConfigJson(context: Context): String {
    return secureRead(context, keyConfig) ?: EMPTY
}

internal suspend fun readDsConfig(context: Context): DsConfig? {
    return DsConfig.getOrNull(readDsConfigJson(context))
}

internal fun getDsConfigFlow(context: Context): Flow<DsConfig?> {
    return context.prefsFlow
        .map { preferences -> preferences[keyConfig] }
        .map { encryptedConfig -> encryptedConfig?.let { Core.decrypt(it) } }
        .map { json -> json?.let { DsConfig.getOrNull(it) } }
}

internal suspend fun saveVpnStatus(context: Context, status: RootVpn.Status) {
    save(context, keyVpnStatus, status.code)
}

internal suspend fun readVpnStatus(context: Context): RootVpn.Status {
    return read(context, keyVpnStatus)?.let { RootVpn.getStatus(it) } ?: RootVpn.Status.DISCONNECTED
}

internal fun getVpnStatusFlow(context: Context): Flow<RootVpn.Status> {
    return context.prefsFlow
        .map { preferences -> preferences[keyVpnStatus] }
        .map { code -> code?.let { RootVpn.getStatus(it) } ?: RootVpn.Status.DISCONNECTED }
}

internal suspend fun saveVpnError(context: Context, error: RootVpn.Error) {
    save(context, keyVpnError, error.code)
}

internal suspend fun readVpnError(context: Context): RootVpn.Error {
    return read(context, keyVpnError)?.let { RootVpn.getError(it) } ?: RootVpn.Error.NO_ERROR
}

internal fun getVpnErrorFlow(context: Context): Flow<RootVpn.Error> {
    return context.prefsFlow
        .map { preferences -> preferences[keyVpnError] }
        .map { code -> code?.let { RootVpn.getError(it) } ?: RootVpn.Error.NO_ERROR }
}

internal suspend fun saveClientId(context: Context, clientId: String) {
    secureSave(context, keyClientId, clientId)
}

internal suspend fun readClientId(context: Context): String {
    return secureRead(context, keyClientId) ?: run {
        val clientId = UUID.randomUUID().toString()
        saveClientId(context, clientId)
        clientId
    }
}