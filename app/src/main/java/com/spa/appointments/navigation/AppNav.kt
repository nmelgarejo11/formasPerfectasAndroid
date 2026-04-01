package com.spa.appointments.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spa.appointments.core.utils.Constants.Routes
import com.spa.appointments.ui.auth.LoginScreen
import com.spa.appointments.ui.auth.LoginViewModel
import com.spa.appointments.ui.home.HomeScreen
import com.spa.appointments.ui.splash.SplashScreen

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
                    // Por ahora solo navegamos si la ruta está registrada
                    // En fases siguientes agregas aquí cada pantalla nueva
                    try { nav.navigate(ruta) } catch (e: Exception) { }
                }
            )
        }
    }
}