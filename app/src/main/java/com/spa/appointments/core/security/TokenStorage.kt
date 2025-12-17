package com.spa.appointments.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStorage(context: Context) {

    companion object {
        private const val PREF_NAME = "secure_prefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_KEY = "logged_user"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSession(
        accessToken: String,
        refreshToken: String,
        user: String
    ) {
        prefs.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .putString(USER_KEY, user)
            .apply()
    }

    fun getAccessToken(): String? =
        prefs.getString(ACCESS_TOKEN_KEY, null)

    fun getRefreshToken(): String? =
        prefs.getString(REFRESH_TOKEN_KEY, null)

    fun getUser(): String? =
        prefs.getString(USER_KEY, null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
