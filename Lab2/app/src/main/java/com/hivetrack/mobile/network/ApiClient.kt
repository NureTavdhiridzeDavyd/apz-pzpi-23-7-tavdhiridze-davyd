package com.hivetrack.mobile.network

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiException(message: String, val statusCode: Int) : Exception(message)

object ApiClient {
    const val BASE_URL = "https://ark-pzpi-23-7-tavdhiridze-davyd.onrender.com"

    fun get(path: String, token: String? = null): String {
        return request("GET", path, null, token)
    }

    fun post(path: String, body: JSONObject, token: String? = null): String {
        return request("POST", path, body.toString(), token)
    }

    fun put(path: String, body: JSONObject = JSONObject(), token: String? = null): String {
        return request("PUT", path, body.toString(), token)
    }

    fun delete(path: String, token: String? = null): String {
        return request("DELETE", path, null, token)
    }

    private fun request(method: String, path: String, body: String?, token: String?): String {
        val url = URL(BASE_URL + path)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Accept", "application/json")
            if (token != null) setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
        }

        if (body != null) {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body) }
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.let {
            BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use { reader -> reader.readText() }
        } ?: ""

        if (code !in 200..299) {
            val message = try {
                JSONObject(text).optString("message", text.ifBlank { "HTTP $code" })
            } catch (_: Exception) {
                text.ifBlank { "HTTP $code" }
            }
            throw ApiException(message, code)
        }

        return text
    }

    fun prettyJson(raw: String): String {
        return try {
            when {
                raw.trim().startsWith("[") -> JSONArray(raw).toString(2)
                raw.trim().startsWith("{") -> JSONObject(raw).toString(2)
                else -> raw
            }
        } catch (_: Exception) {
            raw
        }
    }
}
