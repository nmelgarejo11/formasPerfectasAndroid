package com.spa.appointments

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.spa.appointments.core.security.TokenStorage
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SpaAppointmentsApp : Application() {

    @Inject
    lateinit var tokenStorage: TokenStorage

    override fun onCreate() {
        super.onCreate()

        // Firebase ya se inicializa automáticamente con el plugin google-services
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("SpaApp", "Token FCM obtenido: $token")
                tokenStorage.saveFcmToken(token)
            }
            .addOnFailureListener { e ->
                Log.e("SpaApp", "Error obteniendo token FCM: ${e.message}")
            }
    }
}