package com.nthlink.android.core.utils

import android.util.Log
import com.nthlink.android.core.model.Config
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object JsonParser {

    private val format = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true  // setting default value if it's null
    }

    fun toJson(config: Config): String = format.encodeToString(config)

    fun toConfig(json: String): Config {
        return try {
            format.decodeFromString<Config>(json)
        } catch (e: Throwable) {
            Log.e(TAG, "toConfig err: ", e)
            Config()
        }
    }
}