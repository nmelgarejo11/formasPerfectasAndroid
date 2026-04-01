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

    // startDestination ahora es el splash, no el login
    // El splash decide a dónde ir después
    NavHost(
        navController = nav,
        startDestination = Routes.SPLASH
    ) {

        // Splash — pantalla inicial
        composable(Routes.SPLASH) {
            SplashScreen(
                onGoLogin = {
                    nav.navigate(Routes.LOGIN) {
                        // Eliminamos el splash del back stack para que el usuario
                        // no pueda volver al splash presionando "atrás"
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

        // Login
        composable(Routes.LOGIN) {
            val vm = hiltViewModel<LoginViewModel>()
            LoginScreen(vm) {
                nav.navigate(Routes.HOME) {
                    // Eliminamos el login del back stack — el usuario NO debe
                    // poder volver al login presionando "atrás" desde el Home
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }

        // Home — aquí irán los demás módulos en fases siguientes
        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}