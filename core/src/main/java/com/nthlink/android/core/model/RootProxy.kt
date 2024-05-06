package com.nthlink.android.core.model

import android.os.Parcelable
import com.nthlink.android.core.model.Proxy.Companion.SHADOWSOCKS
import com.nthlink.android.core.model.Proxy.Companion.TROJAN
import com.nthlink.android.core.utils.EMPTY
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface Proxy : Parcelable {
    companion object {
        const val SHADOWSOCKS = "ss"
        const val TROJAN = "trojan"
        const val OUTLINE = "outline"
    }

    fun getConfig(): String
}

// Shadowsocks
@Parcelize
data class Shadowsocks(
    val host: String,
    val port: Int,
    val encryptMethod: String,
    val password: String
) : Proxy {
    override fun getConfig(): String {
        return "$SHADOWSOCKS, $host, $port, encrypt-method=$encryptMethod, password=$password"
    }
}

// Trojan-Go
@Parcelize
data class Trojan(
    val host: String,
    val port: Int,
    val password: String,
    val sni: String,
    val ws: Boolean = false,
    val wsPath: String = EMPTY
) : Proxy {
    override fun getConfig(): String {
        return "$TROJAN, $host, $port, password=$password, sni=$sni, ws=$ws, ws-path=$wsPath"
    }
}

// Outline
@Parcelize
data class Outline(
    val host: String,
    val port: Int,
    val encryptMethod: String,
    val password: String
) : Proxy {
    override fun getConfig(): String {
        return EMPTY
    }
}