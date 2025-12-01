package com.nthlink.android.client.ui.web

import android.webkit.WebChromeClient
import android.webkit.WebView

const val CUSTOM_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
val customExtraHeaders = mapOf("X-Requested-With" to "XMLHttpRequest")

class CustomWebChromeClient(private val callback: Callback) : WebChromeClient() {
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        callback.onProgressChanged(view, newProgress)

        when (newProgress) {
            in 0..49 -> callback.onStartLoading(view)
            in 50..99 -> callback.onLoading(view)
            100 -> callback.onFinishLoading(view)
        }
    }

    interface Callback {
        fun onProgressChanged(view: WebView, newProgress: Int) {}
        fun onStartLoading(view: WebView) {}
        fun onLoading(view: WebView) {}
        fun onFinishLoading(view: WebView) {}
    }
}
