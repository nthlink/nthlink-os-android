package com.nthlink.android.client.ui.web

import android.webkit.WebChromeClient
import android.webkit.WebView

class WebChrome(private val callback: Callback) : WebChromeClient() {
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