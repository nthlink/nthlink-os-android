package com.nthlink.android.core.model


import android.util.Log
import com.google.gson.annotations.SerializedName
import com.nthlink.android.core.utils.TAG
import com.nthlink.android.core.utils.gson

data class DsConfig(
    @SerializedName("servers")
    val servers: List<Server>,
    @SerializedName("redirectUrl")
    val redirectUrl: String,
    @SerializedName("headlineNews")
    val headlineNews: List<HeadlineNew>,
    @SerializedName("notifications")
    val notifications: List<Notification>?
) {
    class Server(
        @SerializedName("protocol")
        val protocol: String,
        @SerializedName("host")
        val host: String,
        @SerializedName("port")
        val port: String,
        @SerializedName("password")
        val password: String,
        @SerializedName("encrypt_method")
        val encryptMethod: String,
        @SerializedName("sni")
        val sni: String,
        @SerializedName("ws")
        val ws: Boolean,
        @SerializedName("ws_path")
        val wsPath: String
    )

    data class HeadlineNew(
        @SerializedName("title")
        val title: String,
        @SerializedName("excerpt")
        val excerpt: String,
        @SerializedName("image")
        val image: String,
        @SerializedName("url")
        val url: String
    )

    data class Notification(
        @SerializedName("title")
        val title: String,
        @SerializedName("url")
        val url: String
    )

    companion object {
        fun get(json: String): DsConfig = gson.fromJson(json, DsConfig::class.java)

        fun getOrNull(json: String): DsConfig? {
            return try {
                get(json)
            } catch (e: Throwable) {
                Log.e(TAG, "parse JSON to DsConfig failed", e)
                null
            }
        }
    }
}