package com.nthlink.android.core.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nthlink.android.core.model.DsConfig
import com.nthlink.android.core.model.Outline
import com.nthlink.android.core.model.Proxy
import com.nthlink.android.core.model.Shadowsocks
import com.nthlink.android.core.model.Trojan

// Constants
internal const val TAG = "RootVpn"
internal const val EMPTY = ""

internal val gson: Gson by lazy { GsonBuilder().create() }

fun getProxiesFromDsConfig(json: String): Array<out Proxy> {
    val dsConfig = DsConfig.get(json)
    return dsConfig.servers.mapNotNull<DsConfig.Server, Proxy> { server ->
        when (server.protocol) {
            Proxy.SHADOWSOCKS -> Shadowsocks(
                server.host,
                server.port.toInt(),
                server.encryptMethod,
                server.password
            )

            Proxy.TROJAN -> Trojan(
                server.host,
                server.port.toInt(),
                server.password,
                server.sni,
                server.ws,
                server.wsPath
            )

            Proxy.OUTLINE -> Outline(
                server.host,
                server.port.toInt(),
                server.encryptMethod,
                server.password
            )

            // TODO add new branches for your protocols

            else -> null
        }
    }.toTypedArray()
}