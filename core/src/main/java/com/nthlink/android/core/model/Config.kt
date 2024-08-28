package com.nthlink.android.core.model

import com.nthlink.android.core.utils.EMPTY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("servers")
    val servers: List<Server> = emptyList(),
    @SerialName("redirectUrl")
    val redirectUrl: String = EMPTY,
    @SerialName("headlineNews")
    val headlineNews: List<HeadlineNews> = emptyList(),
    @SerialName("notifications")
    val notifications: List<Notification> = emptyList(),
    @SerialName("domainKeys")
    val domainKeys: List<String> = emptyList(),
    @SerialName("static")
    val static: Boolean = false,
    @SerialName("use_custom")
    val useCustom: Boolean = false,
    @SerialName("custom")
    val custom: String = EMPTY
) {
    @Serializable
    data class Server(
        @SerialName("protocol")
        val protocol: String,
        @SerialName("host")
        val host: String,
        @SerialName("ips")
        val ips: List<String> = emptyList(),
        @SerialName("port")
        val port: Int,
        @SerialName("password")
        val password: String,
        @SerialName("encrypt_method")
        val encryptMethod: String = EMPTY,
        @SerialName("sni")
        val sni: String = EMPTY,
        @SerialName("ws")
        val ws: Boolean = false,
        @SerialName("ws_path")
        val wsPath: String = EMPTY,
        @SerialName("ws_host")
        val wsHost: String = EMPTY
    )

    @Serializable
    data class HeadlineNews(
        @SerialName("title")
        val title: String,
        @SerialName("excerpt")
        val excerpt: String,
        @SerialName("image")
        val image: String,
        @SerialName("url")
        val url: String,
        @SerialName("categories")
        val categories: List<String> = emptyList()
    )

    @Serializable
    data class Notification(
        @SerialName("title")
        val title: String,
        @SerialName("url")
        val url: String
    )
}