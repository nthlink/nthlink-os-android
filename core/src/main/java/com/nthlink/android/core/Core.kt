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
                  "ips": [
                    "1.1.1.1",
                    "2.2.2.2",
                    "3.3.3.3"
                  ],
                  "ws_host": "www.abc.com"
                }
              ],
              "redirectUrl": "https://www.persagg.com/zh/?utm_medium=proxy&utm_source=nthlink",
              "headlineNews": [
                {
                  "title": "台湾明年国防预算再创新高,美专家称在遏阻中国侵台方面仍不足以缓解担忧",
                  "excerpt": "",
                  "image": "",
                  "url": "https://www.voachinese.com/a/experts-debate-on-taiwans-defense-spending-as-president-lai-annouced-largest-increase-20240813/7741584.html?utm_medium=proxy&utm_campaign=persagg&utm_source=nthlink&utm_content=image"
                },
                {
                  "title": "缅甸军方官员：中国外长王毅东南亚之行期间将会晤军政府首脑",
                  "excerpt": "",
                  "image": "",
                  "url": "https://www.voachinese.com/a/china-fm-to-meet-myanmar-junta-chief-on-se-asia-trip-military-official-20240813/7741556.html?utm_medium=proxy&utm_campaign=persagg&utm_source=nthlink&utm_content=image"
                }
              ],
              "notifications": [
                {
                  "title": "Download nthLink",
                  "url": "https://www.downloadnth.com/download.html"
                }
              ],
              "domainKeys": [],
              "static": false,
              "use_custom": false,
              "custom": ""
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
}