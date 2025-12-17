package com.spa.appointments.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spa.appointments.ui.auth.LoginScreen
import com.spa.appointments.ui.home.HomeScreen

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "login") {

        composable("login") {
            val vm = hiltViewModel<com.spa.appointments.ui.auth.LoginViewModel>()
            LoginScreen(vm) {
                nav.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        composable("home") { HomeScreen() }
    }
}
