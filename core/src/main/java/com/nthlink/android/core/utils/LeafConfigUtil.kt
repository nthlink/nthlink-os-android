package com.nthlink.android.core.utils

import com.nthlink.android.core.model.Proxy


private const val PROXY = "proxy_"

internal fun createLeafConfig(tunFd: Int, proxies: Array<out Proxy>): String {
    return StringBuilder().apply {
        appendLine("[General]")
        appendLine("loglevel = error")
        appendLine("dns-server = 8.8.8.8, 4.4.4.4")
        appendLine("tun-fd = $tunFd")
        appendLine()
        appendLine("[Proxy]")
        appendLine("Direct = direct")
        appendLine()
        for ((index, proxy) in proxies.withIndex()) {
            appendLine("$PROXY$index = ${proxy.getConfig()}")
        }
        appendLine()
        appendLine("[Proxy Group]")
        appendLine(getProxyGroup(proxies.size))
        appendLine()
        appendLine("[Rule]")
        appendLine("FINAL, Proxy")
    }.toString()
}

private fun getProxyGroup(serverSize: Int): String {
    var index = 0
    val builder = StringBuilder()
    while (index < serverSize) {
        builder.append(" $PROXY$index,")
        ++index
    }

    return "Proxy = failover,${builder} health-check=true, check-interval=600, fail-timeout=3, health-check-timeout=10, health-check-delay=20"
}