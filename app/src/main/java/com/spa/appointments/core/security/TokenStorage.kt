package com.spa.appointments.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Log

class TokenStorage(context: Context) {

    companion object {
        private const val PREF_NAME = "secure_prefs"
        private const val ACCESS_TOKEN_KEY  = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_KEY          = "logged_user"
        private const val ID_EMPRESA_KEY    = "id_empresa"
        private const val TAG = "TokenStorage"
        private const val LICENCIA_ESTADO_KEY  = "licencia_estado"
        private const val LICENCIA_MENSAJE_KEY = "licencia_mensaje"
        private const val LICENCIA_DIAS_KEY    = "licencia_dias"
        private const val FCM_TOKEN_KEY = "fcm_token"
    }

    fun saveLicencia(estado: String, mensaje: String, diasRestantes: Int) {
        prefs.edit()
            .putString(LICENCIA_ESTADO_KEY,  estado)
            .putString(LICENCIA_MENSAJE_KEY, mensaje)
            .putInt(LICENCIA_DIAS_KEY,       diasRestantes)
            .apply()
    }

    fun getLicenciaEstado():  String = prefs.getString(LICENCIA_ESTADO_KEY,  "") ?: ""
    fun getLicenciaMensaje(): String = prefs.getString(LICENCIA_MENSAJE_KEY, "") ?: ""
    fun getLicenciaDias():    Int    = prefs.getInt(LICENCIA_DIAS_KEY,       0)

    // Guardamos el contexto para poder recrear las prefs si fallan
    private val appContext = context.applicationContext

    private val prefs: SharedPreferences = crearPrefs()

    private fun crearPrefs(): SharedPreferences {
        return try {
            // Intento normal de crear las EncryptedSharedPreferences
            crearEncryptedPrefs()
        } catch (e: Exception) {
            // Si falla (clave del Keystore borrada, datos corruptos, etc.)
            // borramos el archivo de prefs y lo recreamos limpio
            Log.w(TAG, "EncryptedSharedPreferences corrompido, limpiando...", e)
            limpiarArchivoPrefs()
            crearEncryptedPrefs()
        }
    }

    private fun crearEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            appContext,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun limpiarArchivoPrefs() {
        // Borra el archivo físico de SharedPreferences del disco
        // Esto es seguro: solo borra la sesión guardada, no datos del usuario
        try {
            val prefsFile = appContext.getDatabasePath("../shared_prefs/$PREF_NAME.xml")
            if (prefsFile.exists()) prefsFile.delete()

            // Forma alternativa más confiable en todos los dispositivos
            appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply()
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo limpiar el archivo de prefs", e)
        }
    }

    fun saveFcmToken(token: String) {
        prefs.edit().putString(FCM_TOKEN_KEY, token).apply()
    }

    fun getFcmToken(): String? = prefs.getString(FCM_TOKEN_KEY, null)

    fun saveSession(
        accessToken: String,
        refreshToken: String,
        user: String,
        idEmpresa:    Int
    ) {
        prefs.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .putString(USER_KEY, user)
            .putInt(ID_EMPRESA_KEY,       idEmpresa)
            .apply()
    }

    fun getAccessToken(): String?  = prefs.getString(ACCESS_TOKEN_KEY, null)
    fun getRefreshToken(): String? = prefs.getString(REFRESH_TOKEN_KEY, null)
    fun getUser(): String?         = prefs.getString(USER_KEY, null)
    fun getIdEmpresa():    Int     = prefs.getInt(ID_EMPRESA_KEY,       0)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}