package com.nthlink.android.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestResult(
    @SerialName("timestamp") val timestamp: String,
    @SerialName("domainName") val domainName: String,
    @SerialName("message") val message: String,
    @SerialName("responseStatusCode") val responseStatusCode: Int
)

@Serializable
data class VerifyResult(
    @SerialName("timestamp") val timestamp: String,
    @SerialName("message") val message: String
)

