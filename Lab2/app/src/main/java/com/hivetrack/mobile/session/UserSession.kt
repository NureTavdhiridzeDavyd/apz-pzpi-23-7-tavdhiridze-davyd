package com.hivetrack.mobile.session

import android.content.Context

class UserSession(context: Context) {
    private val prefs = context.getSharedPreferences("hivetrack_session", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(value) = prefs.edit().putString("token", value).apply()

    var userId: Int
        get() = prefs.getInt("userId", 0)
        set(value) = prefs.edit().putInt("userId", value).apply()

    var fullName: String
        get() = prefs.getString("fullName", "") ?: ""
        set(value) = prefs.edit().putString("fullName", value).apply()

    var email: String
        get() = prefs.getString("email", "") ?: ""
        set(value) = prefs.edit().putString("email", value).apply()

    var role: String
        get() = prefs.getString("role", "") ?: ""
        set(value) = prefs.edit().putString("role", value).apply()

    fun isLoggedIn(): Boolean = !token.isNullOrBlank()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
