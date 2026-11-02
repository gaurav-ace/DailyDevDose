package com.gaurav.devagent.news

import java.net.HttpURLConnection
import java.net.URL

data class HttpResult(val code: Int, val body: String)

object Http {
    private const val MAX_BODY_CHARS = 3_000_000

    /** Blocking GET; call from a background dispatcher. */
    fun get(url: String): HttpResult {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 20_000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) DevAgent/1.0")
        try {
            val code = conn.responseCode
            val stream = if (code >= 400) conn.errorStream else conn.inputStream
            val body = stream?.bufferedReader()?.use { reader ->
                val sb = StringBuilder()
                val buf = CharArray(8192)
                while (sb.length < MAX_BODY_CHARS) {
                    val n = reader.read(buf)
                    if (n < 0) break
                    sb.append(buf, 0, n)
                }
                sb.toString()
            } ?: ""
            return HttpResult(code, body)
        } finally {
            conn.disconnect()
        }
    }
}
