package com.nthlink.android.core.utils

import com.nthlink.android.core.model.Config
import com.nthlink.android.core.model.DiagnosisReport
import kotlinx.serialization.json.Json

internal object JsonParser {

    private val format = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true  // setting default value if it's null
    }

    fun toJson(config: Config): String = format.encodeToString(config)
    fun toJson(report: DiagnosisReport): String = format.encodeToString(report)

    fun toConfig(json: String): Config = format.decodeFromString<Config>(json)
}