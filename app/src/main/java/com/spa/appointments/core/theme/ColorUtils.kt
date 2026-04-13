package com.spa.appointments.core.theme

import androidx.compose.ui.graphics.Color

fun hexToColor(hex: String): Color {
    return try {
        val cleanHex = hex.trimStart('#').padStart(6, '0')
        Color(android.graphics.Color.parseColor("#$cleanHex"))
    } catch (e: Exception) {
        Color(0xFF6650A4) // morado Material por defecto
    }
}