package com.spa.appointments.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun mapIcon(nombre: String?): ImageVector {
    return when (nombre?.lowercase()) {
        // Módulos
        "calendar"               -> Icons.Default.CalendarMonth
        "star"                   -> Icons.Default.Star
        "attach_money"           -> Icons.Default.AttachMoney
        "person"                 -> Icons.Default.Person

        // Submódulos de Citas
        "add"                    -> Icons.Default.Add
        "history"                -> Icons.Default.History

        // Submódulos de Catálogo
        "group"                  -> Icons.Default.Group
        "people"                 -> Icons.Default.People

        // Submódulos de Mi cuenta
        "settings"               -> Icons.Default.Settings

        // Submódulos de Administración
        "admin_panel_settings"   -> Icons.Default.AdminPanelSettings
        "category"               -> Icons.Default.Category
        "spa"                    -> Icons.Default.Spa

        // Fallback genérico
        else                     -> Icons.Default.Circle
    }
}