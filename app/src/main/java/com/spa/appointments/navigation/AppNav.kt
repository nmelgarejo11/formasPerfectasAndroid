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
import com.spa.appointments.ui.disponibilidad.DisponibilidadScreen
import com.spa.appointments.ui.disponibilidad.DisponibilidadViewModel
import com.spa.appointments.ui.home.HomeScreen
import com.spa.appointments.ui.profesionales.ProfesionalesScreen
import com.spa.appointments.ui.reserva.ReservaSharedViewModel
import com.spa.appointments.ui.servicios.ServiciosScreen
import com.spa.appointments.ui.splash.SplashScreen
import com.spa.appointments.ui.citas.MisCitasScreen
import com.spa.appointments.ui.citas.HistorialScreen

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.SPLASH) {

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

        composable(Routes.LOGIN) {
            val vm = hiltViewModel<LoginViewModel>()
            LoginScreen(vm) {
                nav.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }

        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigate = { ruta ->
                    try { nav.navigate(ruta) } catch (e: Exception) { }
                }
            )
        }

        // ── Flujo de reserva ──────────────────────────────────────────────

        composable(Routes.SERVICIOS) {
            // remember envuelve el getBackStackEntry para que sea seguro en composición
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

        composable(Routes.MIS_CITAS) {
            MisCitasScreen(
                onBack = { nav.popBackStack() },
                onVerHistorial = { nav.navigate(Routes.HISTORIAL) }
            )
        }

        composable(Routes.HISTORIAL) {
            HistorialScreen(
                onBack = { nav.popBackStack() }
            )
        }
    }
}