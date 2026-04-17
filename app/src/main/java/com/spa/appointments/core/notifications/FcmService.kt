package com.spa.appointments.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.spa.appointments.MainActivity
import com.spa.appointments.R
import com.spa.appointments.core.security.TokenStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenStorage: TokenStorage

    companion object {
        const val CHANNEL_ID   = "citas_channel"
        const val CHANNEL_NAME = "Citas"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        tokenStorage.saveFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title   = message.notification?.title ?: message.data["title"] ?: return
        val body    = message.notification?.body  ?: message.data["body"]  ?: return
        val destino = message.data["destino"]     ?: "mis_citas"

        Log.d("FcmService", "Mensaje recibido: title=$title destino=$destino")
        showNotification(title, body, destino)
    }

    private fun showNotification(title: String, body: String, destino: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("destino", destino)
            action = "NOTIFICACION_$destino"
        }

        Log.d("FcmService", "Creando notificación con destino=$destino")

        val pendingIntent = PendingIntent.getActivity(
            this,
            destino.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}