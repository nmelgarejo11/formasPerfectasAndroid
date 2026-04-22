package com.spa.appointments.core.utils

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import com.spa.appointments.ui.admin.catalogos.ICONOS_DISPONIBLES

fun mapIcon(nombre: String?): ImageVector {
    return ICONOS_DISPONIBLES
        .firstOrNull { it.clave == nombre?.lowercase() }
        ?.icono
        ?: Icons.Default.Circle
}