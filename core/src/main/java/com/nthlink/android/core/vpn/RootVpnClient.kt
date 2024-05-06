package com.nthlink.android.core.vpn

import com.nthlink.android.core.model.Proxy

interface RootVpnClient {
    fun start(proxies: Array<out Proxy>)
    fun stop()
}