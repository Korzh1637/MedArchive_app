package com.example.medarchive.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SessionManager(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "medarchive_session",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    // ---------- Базовая сессия пользователя ----------
    fun saveUserSession(userId: Int, email: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun saveBaseUrl(url: String) {
        prefs.edit().putString("base_url", url).apply()
    }

    fun getBaseUrl(): String {
        return prefs.getString("base_url", "https://willpower-unknown-unmolded.ngrok-free.dev") ?: "https://willpower-unknown-unmolded.ngrok-free.dev"
    }
    // ---------- JWT токен ----------
    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    // ---------- Очистка сессии ----------
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}