package com.spa.appointments.core.utils

object Constants {

    // Tiempo que se muestra el splash antes de navegar (en milisegundos)
    const val SPLASH_DELAY_MS = 1500L

    // Nombre de la app (se puede leer desde strings.xml también)
    const val APP_NAME = "SPA Appointments"

    // Rutas de navegación — todas las pantallas de la app
    // Usamos un objeto anidado para agruparlas y evitar errores de tipeo
    object Routes {
        const val SPLASH = "splash"
        const val LOGIN  = "login"
        const val HOME   = "home"
        // Aquí irás agregando más rutas: "appointments", "services", etc.
    }
}
