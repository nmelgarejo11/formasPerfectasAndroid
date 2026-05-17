package com.spa.appointments.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.spa.appointments.ui.reserva.ReservaSharedViewModel
import com.spa.appointments.ui.splash.SplashEmpresaScreen
import com.spa.appointments.ui.splash.SplashScreen
import com.spa.appointments.ui.clientes.SeleccionarClienteScreen
import com.spa.appointments.ui.clientes.ClientesScreen
import com.spa.appointments.ui.citas.ReagendamientosScreen
import com.spa.appointments.ui.admin.catalogos.CategoriasScreen
import com.spa.appointments.ui.admin.catalogos.ServiciosAdminScreen
import com.spa.appointments.ui.admin.profesionales.ProfesionalesAdminScreen
import com.spa.appointments.ui.admin.profesionales.ProfesionalDetalleScreen
import com.spa.appointments.ui.admin.horarios.HorariosScreen
import androidx.compose.runtime.collectAsState
import com.spa.appointments.ui.admin.horarios.HorariosListaScreen
import com.spa.appointments.ui.admin.perfilsubmodulo.PerfilSubModuloScreen
import com.spa.appointments.ui.admin.usuario.CargosScreen
import com.spa.appointments.ui.admin.usuario.GestionUsuariosScreen
import com.spa.appointments.ui.admin.usuario.UsuarioPerfilScreen
import com.spa.appointments.ui.financiero.FinancieroViewModel
import com.spa.appointments.ui.financiero.IngresosVsGastosScreen
import com.spa.appointments.ui.financiero.IngresosVsGastosViewModel
import com.spa.appointments.ui.profesionales.ProfesionalesScreen
import com.spa.appointments.ui.servicios.ServiciosScreen
import com.spa.appointments.ui.profesionales.ProfesionalesViewModel
import com.spa.appointments.ui.gastos.GastoScreen
import com.spa.appointments.ui.metodopago.MetodoPagoDetalleScreen
import com.spa.appointments.ui.metodopago.MetodoPagoScreen
import com.spa.appointments.ui.tema.TemaScreen
import com.spa.appointments.ui.citas.ResponsableGrupalScreen

@Composable
fun AppNav(pendingDestination: androidx.compose.runtime.MutableState<String?>) {
    val nav = rememberNavController()

    val rutasConocidas = setOf(
        Routes.SERVICIOS,
        Routes.PROFESIONALES,
        Routes.SELECCIONAR_CLIENTE,
        Routes.RESPONSABLE_GRUPAL,
        Routes.DISPONIBILIDAD,
        Routes.MIS_CITAS,
        Routes.HISTORIAL,
        Routes.FINANCIERO,
        Routes.PERFIL,
        Routes.REAGENDAMIENTOS,
        Routes.ADMIN_CATEGORIAS,
        Routes.ADMIN_SERVICIOS,
        Routes.ADMIN_PROFESIONALES,
        Routes.ADMIN_HORARIOS_LISTA,
        Routes.GASTOS,
        Routes.INGRESOS_VS_GASTOS,
        Routes.ADMIN_TEMA,
        Routes.METODOS_PAGO,
        Routes.CLIENTES,
        Routes.ADMIN_CARGO,
        Routes.ADMIN_USUARIO,
        Routes.ADMIN_USUARIO_PERFIL,
        Routes.ADMIN_SUBMODULO,
        "logout"
    )

    NavHost(navController = nav, startDestination = Routes.SPLASH) {

        // ── Splash & Auth ────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onGoLogin = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                onGoHome = { nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                onGoExpired = { nav.navigate(Routes.DEMO_EXPIRADO) { popUpTo(Routes.SPLASH) { inclusive = true } } }
            )
        }

        composable(Routes.LOGIN) {
            val vm = hiltViewModel<LoginViewModel>()
            LoginScreen(
                vm = vm,
                onLoginSuccess = { nav.navigate(Routes.SPLASH_EMPRESA) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                onLoginExpired = { nav.navigate(Routes.DEMO_EXPIRADO) { popUpTo(Routes.LOGIN) { inclusive = true } } }
            )
        }

        composable(Routes.SPLASH_EMPRESA) {
            SplashEmpresaScreen(
                onContinuar = { nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH_EMPRESA) { inclusive = true } } }
            )
        }

        // ── Home ─────────────────────────────────────────────

        composable(Routes.HOME) {
            val homeVm = hiltViewModel<HomeViewModel>()
            HomeScreen(
                onLogout = {
                    homeVm.logout()
                    nav.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                },
                onNavigate = { ruta ->
                    when (ruta) {
                        "logout" -> {
                            homeVm.logout()
                            nav.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                        }
                        in rutasConocidas -> nav.navigate(ruta)
                        else -> Unit
                    }
                },
                pendingDestination = pendingDestination,
                vm = homeVm
            )
        }

        // ── Flujo de Reserva ─────────────────────────────────

        composable(Routes.SERVICIOS) {

            val sharedVm = hiltViewModel<ReservaSharedViewModel>(it)

            ServiciosScreen(
                onBack = { nav.popBackStack() },
                onSeleccionarServicio = { servicio ->
                    sharedVm.servicioSeleccionado = servicio

                    if (servicio.esGrupal) {
                        sharedVm.esGrupal = true
                        nav.navigate(Routes.RESPONSABLE_GRUPAL)
                    } else {
                        sharedVm.esGrupal = false
                        nav.navigate(Routes.PROFESIONALES)
                    }
                }
            )
        }

        composable(Routes.PROFESIONALES) {
            val backEntry = remember(it) {
                try { nav.getBackStackEntry(Routes.SERVICIOS) }
                catch (e: Exception) { it }
            }
            val sharedVm = hiltViewModel<ReservaSharedViewModel>(backEntry)
            val profVm   = hiltViewModel<ProfesionalesViewModel>()

            LaunchedEffect(sharedVm.servicioSeleccionado?.id) {
                profVm.iniciar(sharedVm.servicioSeleccionado?.id)
            }

            ProfesionalesScreen(
                onBack = { nav.popBackStack() },
                onSeleccionarProfesional = { profesional ->
                    sharedVm.profesionalSeleccionado = profesional
                    nav.navigate(Routes.SELECCIONAR_CLIENTE) // Siguiente paso individual
                },
                vm = profVm
            )
        }

        composable(Routes.SELECCIONAR_CLIENTE) {
            val backEntry = remember(it) {
                try { nav.getBackStackEntry(Routes.SERVICIOS) }
                catch (e: Exception) { it }
            }
            val sharedVm = hiltViewModel<ReservaSharedViewModel>(backEntry)

            SeleccionarClienteScreen(
                onBack = { nav.popBackStack() },
                onClienteSeleccionado = { cliente ->
                    sharedVm.clienteSeleccionado = cliente
                    nav.navigate(Routes.DISPONIBILIDAD) // Directo a confirmar la hora
                }
            )
        }

        composable(Routes.RESPONSABLE_GRUPAL) {
            val backEntry = remember(it) {
                try { nav.getBackStackEntry(Routes.SERVICIOS) }
                catch (e: Exception) { it }
            }
            val sharedVm = hiltViewModel<ReservaSharedViewModel>(backEntry)
            // Usamos el mismo ViewModel de disponibilidad para reutilizar la función de backend
            val dispVm   = hiltViewModel<DisponibilidadViewModel>()

            ResponsableGrupalScreen(
                onBack = { nav.popBackStack() },
                onContinuar = { nombre, telefono, correo, personas ->
                    sharedVm.responsableNombre = nombre
                    sharedVm.responsableTelefono = telefono
                    sharedVm.responsableCorreo = correo
                    sharedVm.cantidadPersonas = personas

                    // Pasamos el control al ViewModel para que dispare la cita de una vez
                    dispVm.servicio = sharedVm.servicioSeleccionado
                    dispVm.confirmarCita(sharedVm, notas = "Solicitud en espera")
                },
                // Le pasamos el estado del proceso a la pantalla para mostrar un Loading o Diálogo de éxito
                uiStateFlow = dispVm.uiState,
                onExito = {
                    dispVm.resetear()
                    sharedVm.limpiarReserva()
                    nav.navigate(Routes.MIS_CITAS) {
                        popUpTo(Routes.SERVICIOS) { this.inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DISPONIBILIDAD) {
            val backEntry = remember(it) {
                try { nav.getBackStackEntry(Routes.SERVICIOS) }
                catch (e: Exception) { it }
            }
            val sharedVm = hiltViewModel<ReservaSharedViewModel>(backEntry)
            val dispVm   = hiltViewModel<DisponibilidadViewModel>()

            // Inyectamos el cliente al ViewModel si viene del flujo individual
            dispVm.clienteSeleccionado = sharedVm.clienteSeleccionado

            val servicio    = sharedVm.servicioSeleccionado
            val profesional = sharedVm.profesionalSeleccionado

            if (servicio != null) {
                DisponibilidadScreen(
                    servicio     = servicio,
                    profesional  = profesional,
                    onBack       = { nav.popBackStack() },
                    onCitaCreada = {
                        nav.navigate(Routes.MIS_CITAS) {
                            popUpTo(Routes.SERVICIOS) { this.inclusive = true }
                        }
                    },
                    sharedVm     = sharedVm,
                    vm           = dispVm
                )
            }
        }

        // ── Citas  ────────────────────────────────────────────
        composable(Routes.MIS_CITAS) {
            MisCitasScreen(
                onBack = { nav.popBackStack() },
                onVerHistorial = { nav.navigate(Routes.HISTORIAL) },
                onVerReagendamientos = { nav.navigate(Routes.REAGENDAMIENTOS) }
            )
        }

        composable(Routes.HISTORIAL) { HistorialScreen(onBack = { nav.popBackStack() }) }

        composable(Routes.FINANCIERO) { FinancieroScreen(onBack = { nav.popBackStack() }, vm = hiltViewModel()) }

        composable(Routes.INGRESOS_VS_GASTOS) { IngresosVsGastosScreen(onBack = { nav.popBackStack() }, vm = hiltViewModel()) }

        composable(Routes.PERFIL) { PerfilScreen(onBack = { nav.popBackStack() }, onLogout = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } } }) }

        composable(Routes.DEMO_EXPIRADO) { DemoExpiradoScreen(onCerrarSesion = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.DEMO_EXPIRADO) { inclusive = true } } }) }

        composable(Routes.CLIENTES) { ClientesScreen(onBack = { nav.popBackStack() }) }

        composable(Routes.REAGENDAMIENTOS) { ReagendamientosScreen(onBack = { nav.popBackStack() }) }

        // ── Administracion ────────────────────────────────────────

        composable(Routes.ADMIN_SUBMODULO) { PerfilSubModuloScreen(onVolver = { nav.popBackStack() }) }

        composable(Routes.ADMIN_CARGO) { CargosScreen(onVolver = { nav.popBackStack() })}

        composable(Routes.ADMIN_USUARIO) { UsuarioPerfilScreen(onVolver = { nav.popBackStack() })}

        composable(Routes.ADMIN_USUARIO_PERFIL) { GestionUsuariosScreen(onVolver = { nav.popBackStack() })}

        composable(Routes.ADMIN_CATEGORIAS) { CategoriasScreen(onBack = { nav.popBackStack() }) }

        composable(Routes.ADMIN_SERVICIOS) { ServiciosAdminScreen(onBack = { nav.popBackStack() }) }

        composable(Routes.ADMIN_PROFESIONALES) {
            ProfesionalesAdminScreen(
                onBack = { nav.popBackStack() },
                onVerDetalle = { id -> nav.navigate("${Routes.ADMIN_PROFESIONAL_DETALLE}/$id") },
                onVerHorario = { id -> nav.navigate("${Routes.ADMIN_HORARIOS}/$id") }
            )
        }

        composable("${Routes.ADMIN_PROFESIONAL_DETALLE}/{id}") { backEntry ->
            val id = backEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
            ProfesionalDetalleScreen(idProfesional = id, onBack = { nav.popBackStack() })
        }

        composable(Routes.ADMIN_HORARIOS_LISTA) {
            HorariosListaScreen(onBack = { nav.popBackStack() }, onSeleccionar = { id -> nav.navigate("${Routes.ADMIN_HORARIOS}/$id") })
        }

        composable("${Routes.ADMIN_HORARIOS}/{id}") { backEntry ->
            val id = backEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
            val profVm = hiltViewModel<com.spa.appointments.ui.admin.profesionales.ProfesionalesAdminViewModel>()
            val profesional = profVm.profesionales.collectAsState().value.firstOrNull { it.id == id }
            HorariosScreen(idProfesional = id, profesional = profesional, onBack = { nav.popBackStack() })
        }

        composable(Routes.ADMIN_TEMA) { TemaScreen(onBack = { nav.popBackStack() }) }

        composable(Routes.GASTOS) { GastoScreen(onBack = { nav.popBackStack() }) }

        composable(Routes.METODOS_PAGO) {
            MetodoPagoScreen(
                onBack = { nav.popBackStack() },
                onVerDetalles = { metodo -> nav.navigate("${Routes.METODOS_PAGO_DETALLE}/${metodo.id}/${metodo.nombre}") }
            )
        }

        composable("${Routes.METODOS_PAGO_DETALLE}/{metodoId}/{metodoNombre}") { back ->
            val id = back.arguments?.getString("metodoId")?.toInt() ?: return@composable
            val nombre = back.arguments?.getString("metodoNombre") ?: ""
            MetodoPagoDetalleScreen(metodoId = id, metodoNombre = nombre, onBack = { nav.popBackStack() })
        }
    }
}