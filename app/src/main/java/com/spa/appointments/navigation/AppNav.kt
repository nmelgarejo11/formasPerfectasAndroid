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
import com.spa.appointments.ui.licencia.DemoExpiradoScreen
import com.spa.appointments.ui.perfil.PerfilScreen
import com.spa.appointments.ui.profesionales.ProfesionalesScreen
import com.spa.appointments.ui.reserva.ReservaSharedViewModel
import com.spa.appointments.ui.servicios.ServiciosScreen
import com.spa.appointments.ui.splash.SplashEmpresaScreen
import com.spa.appointments.ui.splash.SplashScreen
import com.spa.appointments.ui.clientes.SeleccionarClienteScreen
import com.spa.appointments.ui.clientes.ClientesScreen
import com.spa.appointments.ui.clientes.ClienteDetalleScreen
import com.spa.appointments.ui.citas.ReagendamientosScreen
import com.spa.appointments.ui.admin.catalogos.CategoriasScreen
import com.spa.appointments.ui.admin.catalogos.ServiciosAdminScreen

@Composable
fun AppNav(pendingDestination: androidx.compose.runtime.MutableState<String?>) {
    val nav = rememberNavController()

    val rutasConocidas = setOf(
        Routes.CLIENTES,
        Routes.SELECCIONAR_CLIENTE,
        Routes.SERVICIOS,
        Routes.PROFESIONALES,
        Routes.DISPONIBILIDAD,
        Routes.MIS_CITAS,
        Routes.HISTORIAL,
        Routes.FINANCIERO,
        Routes.PERFIL,
        Routes.REAGENDAMIENTOS,
        Routes.ADMIN_CATEGORIAS,
        Routes.ADMIN_SERVICIOS,
        "logout"
    )

    NavHost(navController = nav, startDestination = Routes.SPLASH) {

        // ── Splash ───────────────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onGoLogin = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { this.inclusive = true }
                    }
                },
                onGoHome = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { this.inclusive = true }
                    }
                },
                onGoExpired = {
                    nav.navigate(Routes.DEMO_EXPIRADO) {
                        popUpTo(Routes.SPLASH) { this.inclusive = true }
                    }
                }
            )
        }

        // ── Login ────────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            val vm = hiltViewModel<LoginViewModel>()
            LoginScreen(
                vm             = vm,
                onLoginSuccess = {
                    nav.navigate(Routes.SPLASH_EMPRESA) {
                        popUpTo(Routes.LOGIN) { this.inclusive = true }
                    }
                },
                onLoginExpired = {
                    nav.navigate(Routes.DEMO_EXPIRADO) {
                        popUpTo(Routes.LOGIN) { this.inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SPLASH_EMPRESA) {
            SplashEmpresaScreen(
                onContinuar = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH_EMPRESA) { this.inclusive = true }
                    }
                }
            )
        }

        // ── Home ─────────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            val homeVm = hiltViewModel<HomeViewModel>()
            HomeScreen(
                onLogout = {
                    homeVm.logout()
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { this.inclusive = true }
                    }
                },
                onNavigate = { ruta ->
                    when (ruta) {
                        "logout" -> {
                            homeVm.logout()
                            nav.navigate(Routes.LOGIN) {
                                popUpTo(Routes.HOME) { this.inclusive = true }
                            }
                        }
                        in rutasConocidas -> nav.navigate(ruta)
                        else -> Unit
                    }
                },
                pendingDestination = pendingDestination,
                vm = homeVm
            )
        }

        // ── Flujo de reserva ─────────────────────────────────────────────────

        composable(Routes.SELECCIONAR_CLIENTE) {
            val backEntry = remember(it) {
                try { nav.getBackStackEntry(Routes.SELECCIONAR_CLIENTE) }
                catch (e: Exception) { it }
            }
            val sharedVm = hiltViewModel<ReservaSharedViewModel>(backEntry)

            SeleccionarClienteScreen(
                onBack = { nav.popBackStack() },
                onClienteSeleccionado = { cliente ->
                    sharedVm.clienteSeleccionado = cliente
                    nav.navigate(Routes.SERVICIOS)
                }
            )
        }

        composable(Routes.SERVICIOS) {
            val backEntry = remember(it) {
                try { nav.getBackStackEntry(Routes.SELECCIONAR_CLIENTE) }
                catch (e: Exception) { it }
            }
            val sharedVm = hiltViewModel<ReservaSharedViewModel>(backEntry)

            ServiciosScreen(
                onBack = { nav.popBackStack() },
                onSeleccionarServicio = { servicio ->
                    sharedVm.servicioSeleccionado = servicio
                    nav.navigate(Routes.PROFESIONALES)
                }
            )
        }

        composable(Routes.PROFESIONALES) {
            val backEntry = remember(it) {
                try { nav.getBackStackEntry(Routes.SELECCIONAR_CLIENTE) }
                catch (e: Exception) { it }
            }
            val sharedVm = hiltViewModel<ReservaSharedViewModel>(backEntry)

            ProfesionalesScreen(
                onBack = { nav.popBackStack() },
                onSeleccionarProfesional = { profesional ->
                    sharedVm.profesionalSeleccionado = profesional
                    if (sharedVm.servicioSeleccionado != null) {
                        nav.navigate(Routes.DISPONIBILIDAD)
                    } else {
                        nav.navigate(Routes.SERVICIOS)
                    }
                }
            )
        }

        composable(Routes.DISPONIBILIDAD) {
            val backEntry = remember(it) {
                try { nav.getBackStackEntry(Routes.SELECCIONAR_CLIENTE) }
                catch (e: Exception) { it }
            }
            val sharedVm = hiltViewModel<ReservaSharedViewModel>(backEntry)
            val dispVm   = hiltViewModel<DisponibilidadViewModel>()
            dispVm.clienteSeleccionado = sharedVm.clienteSeleccionado

            val servicio    = sharedVm.servicioSeleccionado
            val profesional = sharedVm.profesionalSeleccionado

            if (servicio != null && profesional != null) {
                DisponibilidadScreen(
                    servicio     = servicio,
                    profesional  = profesional,
                    onBack       = { nav.popBackStack() },
                    onCitaCreada = {
                        nav.navigate(Routes.MIS_CITAS) {
                            popUpTo(Routes.SERVICIOS) { this.inclusive = true }
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
                onVerHistorial = { nav.navigate(Routes.HISTORIAL) },
                onVerReagendamientos  = { nav.navigate(Routes.REAGENDAMIENTOS) }
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

        // ── Demo Expirado ────────────────────────────────────────────────────

        composable(Routes.DEMO_EXPIRADO) {
            DemoExpiradoScreen(
                onCerrarSesion = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DEMO_EXPIRADO) { this.inclusive = true }
                    }
                }
            )
        }

        // ── Clientes ─────────────────────────────────────────────────────────
        composable(Routes.CLIENTES) {
            ClientesScreen(
                onBack         = { nav.popBackStack() },
                onVerCliente   = { id -> nav.navigate("${Routes.CLIENTE_DETALLE}/$id") },
                onCrearCliente = { nav.navigate(Routes.CLIENTE_NUEVO) }
            )
        }

        composable("${Routes.CLIENTE_DETALLE}/{id}") { backEntry ->
            val id = backEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
            ClienteDetalleScreen(
                idCliente = id,
                esNuevo   = false,
                onBack    = { nav.popBackStack() }
            )
        }

        composable(Routes.CLIENTE_NUEVO) {
            ClienteDetalleScreen(
                idCliente = 0,
                esNuevo   = true,
                onBack    = { nav.popBackStack() }
            )
        }

        // ── Reagendamientos ──────────────────────────────────────────────────
        composable(Routes.REAGENDAMIENTOS) {
            ReagendamientosScreen(onBack = { nav.popBackStack() })
        }

        // ── Admin Catálogos ──────────────────────────────────────────────────
        composable(Routes.ADMIN_CATEGORIAS) {
            CategoriasScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.ADMIN_SERVICIOS) {
            ServiciosAdminScreen(onBack = { nav.popBackStack() })
        }
    }
}