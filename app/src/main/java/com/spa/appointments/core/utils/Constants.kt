package com.spa.appointments.core.utils

object Constants {

    const val SPLASH_DELAY_MS = 2500L
    const val APP_NAME        = "SPA Formas Perfectas"

    object Routes {
        const val SPLASH      = "splash"
        const val LOGIN       = "login"
        const val HOME        = "home"
        // Citas
        const val SERVICIOS       = "servicios"
        const val PROFESIONALES   = "profesionales"
        const val DISPONIBILIDAD  = "disponibilidad"
        const val MIS_CITAS       = "mis_citas"
        const val HISTORIAL       = "historial"
        const val FINANCIERO      = "financiero"
        const val PERFIL          = "perfil"
        const val DEMO_EXPIRADO  = "demo_expirado"
        const val SPLASH_EMPRESA = "splash_empresa"
        const val SELECCIONAR_CLIENTE = "seleccionar_cliente"
        const val CLIENTES = "clientes"
        const val CLIENTE_DETALLE = "cliente_detalle"   // editar
        const val CLIENTE_NUEVO = "cliente_nuevo"        // crear
    }

    // ID de sede por defecto (puedes hacerlo dinámico después)
    const val ID_SEDE_DEFAULT = 1
}
