package com.nthlink.android.client.storage.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nthlink.android.core.utils.ZERO

private val Context.commonDataStore: DataStore<Preferences> by preferencesDataStore(name = "nthlink-prefs")

class CommonDataStore(private val context: Context) {

    companion object {
        private val AGREE_PRIVACY = booleanPreferencesKey("agreePrivacy")
        private val CONNECTED_COUNT = intPreferencesKey("connectedCount")
        private val HAS_LANDING_PAGE_SHOWN = booleanPreferencesKey("hasLandingPageShown")
    }

    // Preferences DataStore
    suspend fun saveAgreePrivacy(agreePrivacy: Boolean) {
        context.commonDataStore.save(AGREE_PRIVACY, agreePrivacy)
    }

    suspend fun readAgreePrivacy(): Boolean {
        return context.commonDataStore.read(AGREE_PRIVACY) ?: false
    }

    suspend fun saveConnectedCount() {
        val count = readConnectedCount()
        context.commonDataStore.save(CONNECTED_COUNT, count + 1)
    }

    suspend fun readConnectedCount(): Int {
        return context.commonDataStore.read(CONNECTED_COUNT) ?: ZERO
    }

    suspend fun saveHasLandingPageShown(hasShown: Boolean) {
        context.commonDataStore.save(HAS_LANDING_PAGE_SHOWN, hasShown)
    }

    suspend fun readHasLandingPageShown(): Boolean {
        return context.commonDataStore.read(HAS_LANDING_PAGE_SHOWN) ?: false
    }
}