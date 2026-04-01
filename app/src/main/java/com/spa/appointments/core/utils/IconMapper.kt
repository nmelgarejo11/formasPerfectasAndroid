package com.spa.appointments.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// Convierte el nombre de ícono guardado en BD al ImageVector de Material Icons
// Si no reconoce el nombre, usa un ícono genérico
fun mapIcon(nombre: String?): ImageVector {
    return when (nombre?.lowercase()) {
        "calendar", "citas"       -> Icons.Default.CalendarMonth
        "person", "perfil"        -> Icons.Default.Person
        "attach_money", "finance",
        "financiero"              -> Icons.Default.AttachMoney
        "settings", "config"      -> Icons.Default.Settings
        "history", "historial"    -> Icons.Default.History
        "add", "nuevo"            -> Icons.Default.Add
        "list", "lista"           -> Icons.Default.List
        "star", "servicios"       -> Icons.Default.Star
        "group", "profesionales"  -> Icons.Default.Group
        else                      -> Icons.Default.Circle
    }
}