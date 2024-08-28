package com.nthlink.android.client.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

internal object JsonParser {

    private val format = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true  // setting default value if it's null
    }

    fun toJsonElement(json: String): JsonElement = format.parseToJsonElement(json)
}