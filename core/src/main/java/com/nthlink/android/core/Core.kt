package com.nthlink.android.core

import com.nthlink.android.core.utils.EMPTY

internal object Core {
    fun encrypt(text: String): String {
        // TODO Not yet implemented
        return text
    }

    fun decrypt(text: String): String {
        // TODO Not yet implemented
        return text
    }

    fun getConfig(): String {
        // TODO Not yet implemented
        return """
{
    "servers": [
        {
            "protocol": "",
            "host": "www.abc.com",
            "port": "443",
            "password": "password",
            "sni": "www.abc.com",
            "ws": true,
            "ws_path": "/abc",
            "ws_host": "www.abc.com"
        }
    ],
    "redirectUrl": "https://www.persagg.com/zh/?utm_medium=proxy\u0026utm_source=nthlink",
    "headlineNews": [
        {
            "title": "致读者：廿九载灯火暂熄，我们与时代的故事未完待续",
            "excerpt": "",
            "image": "",
            "url": "https://rfa.org/mandarin/zhengzhi/2025/10/31/rfa-mandarin-closure-history-us-china/?utm_medium=proxy\u0026utm_campaign=persagg\u0026utm_source=nthlink\u0026utm_content=image",
            "pinToTop": false
        },
        {
            "title": "美国政府看来即将面临七年来首次关门",
            "excerpt": "",
            "image": "",
            "url": "https://www.voachinese.com/a/us-government-appears-headed-for-first-shutdown-in-7-years-20250930/8070174.html?utm_medium=proxy\u0026utm_campaign=persagg\u0026utm_source=nthlink\u0026utm_content=image",
            "pinToTop": false
        },
        {
            "title": "特朗普和海格塞斯罕见召集美军将领，承诺2026年对军队投入超过1万亿美元",
            "excerpt": "",
            "image": "",
            "url": "https://www.voachinese.com/a/trump-hegseth-address-rare-gathering-of-us-army-commanders-commit-to-over-1-trillion-investment-in-the-military-in-2026-20250930/8070165.html?utm_medium=proxy\u0026utm_campaign=persagg\u0026utm_source=nthlink\u0026utm_content=image",
            "pinToTop": false
        }
    ],
    "notifications": [
        {
            "title": "Download nthLink",
            "url": "https://www.downloadnth.com/download.html"
        }
    ],
    "data": "",
    "static": false,
    "use_custom_config": false,
    "custom_config": "",
    "current_versions": [
        {
            "app_name": "nthlink",
            "platforms": [
                {
                    "os": "android",
                    "version": "6.8.0",
                    "url": "https://www.downloadnth.com/nthlink-6.8.0-release.apk"
                },
                {
                    "os": "windows32",
                    "version": "6.8.0.1",
                    "url": "https://www.downloadnth.com/nthLink_Installer_x86_6.8.0.1.exe"
                },
                {
                    "os": "windows64",
                    "version": "6.8.0.1",
                    "url": "https://www.downloadnth.com/nthLink_Installer_x64_6.8.0.1.exe"
                },
                {
                    "os": "ios",
                    "version": "6.7.9",
                    "url": ""
                },
                {
                    "os": "macos",
                    "version": "6.7.9",
                    "url": ""
                }
            ]
        }
    ]
}
        """.trimIndent()
    }

    fun feedback(
        feedbackType: String,
        description: String = EMPTY,
        appVersion: String = EMPTY,
        email: String = EMPTY
    ) {
        // TODO Not yet implemented
    }

    fun startDiagnostics(): String {
        // TODO Not yet implemented
        return "Report ID"
    }
}