package com.spa.appointments.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spa.appointments.core.utils.Constants.Routes
import com.spa.appointments.ui.auth.LoginScreen
import com.spa.appointments.ui.auth.LoginViewModel
import com.spa.appointments.ui.citas.HistorialScreen
import com.spa.appointments.ui.citas.MisCitasScreen
import com.spa.appointments.ui.disponibilidad.DisponibilidadScreen
import com.spa.appointments.ui.disponibilidad.DisponibilidadViewModel
import com.spa.appointments.ui.financiero.FinancieroScreen
import com.spa.appointments.ui.home.HomeScreen
import com.spa.appointments.ui.home.HomeViewModel
import com.spa.appointments.ui.perfil.PerfilScreen
import com.spa.appointments.ui.profesionales.ProfesionalesScreen
import com.spa.appointments.ui.reserva.ReservaSharedViewModel
import com.spa.appointments.ui.servicios.ServiciosScreen
import com.spa.appointments.ui.splash.SplashScreen

@Composable
fun AppNav() {
    val nav = rememberNavController()

    // Rutas conocidas — se navega solo si están aquí
    val rutasConocidas = setOf(
        Routes.SERVICIOS,
        Routes.PROFESIONALES,
        Routes.DISPONIBILIDAD,
        Routes.MIS_CITAS,
        Routes.HISTORIAL,
        Routes.FINANCIERO,
        Routes.PERFIL,
        "logout"
    )

    NavHost(navController = nav, startDestination = Routes.SPLASH) {

        // ── Splash ───────────────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onGoLogin = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onGoHome = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ────────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            val vm = hiltViewModel<LoginViewModel>()
            LoginScreen(vm) {
                nav.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }

        // ── Home ─────────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            val homeVm = hiltViewModel<HomeViewModel>()
            HomeScreen(
                onLogout = {
                    homeVm.logout()
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigate = { ruta ->
                    when {
                        // Logout es una acción especial, no una pantalla
                        ruta == "logout" -> {
                            homeVm.logout()
                            nav.navigate(Routes.LOGIN) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                        ruta in rutasConocidas -> nav.navigate(ruta)
                        // Ruta no implementada — no hace nada por ahora
                    }
                },
                vm = homeVm
            )
        }

        // ── Flujo de reserva ─────────────────────────────────────────────────

        composable(Routes.SERVICIOS) {
            val backEntry = remember(it) { nav.getBackStackEntry(Routes.SERVICIOS) }
            val sharedVm  = hiltViewModel<ReservaSharedViewModel>(backEntry)

            ServiciosScreen(
                onBack = { nav.popBackStack() },
                onSeleccionarServicio = { servicio ->
                    sharedVm.servicioSeleccionado = servicio
                    nav.navigate(Routes.PROFESIONALES)
                }
            )
        }

        composable(Routes.PROFESIONALES) {
            val backEntry = remember(it) { nav.getBackStackEntry(Routes.SERVICIOS) }
            val sharedVm  = hiltViewModel<ReservaSharedViewModel>(backEntry)

            ProfesionalesScreen(
                onBack = { nav.popBackStack() },
                onSeleccionarProfesional = { profesional ->
                    sharedVm.profesionalSeleccionado = profesional
                    nav.navigate(Routes.DISPONIBILIDAD)
                }
            )
        }

        composable(Routes.DISPONIBILIDAD) {
            val backEntry = remember(it) { nav.getBackStackEntry(Routes.SERVICIOS) }
            val sharedVm  = hiltViewModel<ReservaSharedViewModel>(backEntry)
            val dispVm    = hiltViewModel<DisponibilidadViewModel>()

            val servicio    = sharedVm.servicioSeleccionado
            val profesional = sharedVm.profesionalSeleccionado

            if (servicio != null && profesional != null) {
                DisponibilidadScreen(
                    servicio     = servicio,
                    profesional  = profesional,
                    onBack       = { nav.popBackStack() },
                    onCitaCreada = {
                        nav.navigate(Routes.MIS_CITAS) {
                            popUpTo(Routes.SERVICIOS) { inclusive = true }
                        }
                    },
                    vm = dispVm
                )
            }
        }

        // ── Citas ────────────────────────────────────────────────────────────

        composable(Routes.MIS_CITAS) {
            MisCitasScreen(
                onBack         = { nav.popBackStack() },
                onVerHistorial = { nav.navigate(Routes.HISTORIAL) }
            )
        }

        composable(Routes.HISTORIAL) {
            HistorialScreen(onBack = { nav.popBackStack() })
        }

        // ── Financiero ───────────────────────────────────────────────────────

        composable(Routes.FINANCIERO) {
            FinancieroScreen(onBack = { nav.popBackStack() })
        }

        // ── Perfil ───────────────────────────────────────────────────────────

        composable(Routes.PERFIL) {
            PerfilScreen(
                onBack   = { nav.popBackStack() },
                onLogout = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}