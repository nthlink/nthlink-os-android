package com.nthlink.android.core.model

import com.nthlink.android.core.utils.EMPTY
import com.nthlink.android.core.utils.nowInUtc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class DiagnosisReport {
    @SerialName("timestamp")
    val timestamp: String = nowInUtc()

    @SerialName("message")
    var message: String = EMPTY

    @SerialName("countryCode")
    var countryCode: String = EMPTY

    @SerialName("carrierName")
    var carrierName: String = EMPTY

    @SerialName("networkType")
    var networkType: String = EMPTY

    @SerialName("ipBeforeConnecting")
    var ipBeforeConnecting: String = EMPTY

    @Transient
    private val _getConfigErrors: MutableList<RequestResult> = mutableListOf()

    @SerialName("getConfigErrors")
    val getConfigErrors: List<RequestResult> get() = _getConfigErrors

    @SerialName("ipAfterConnecting")
    var ipAfterConnecting: String = EMPTY

    fun add(error: RequestResult) {
        _getConfigErrors.add(error)
    }
}