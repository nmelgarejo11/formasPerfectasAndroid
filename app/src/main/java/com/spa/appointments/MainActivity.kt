package com.spa.appointments

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.spa.appointments.navigation.AppNav
import com.spa.appointments.ui.theme.AppDynamicTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppDynamicTheme {
                AppNav()
            }
        }
    }
}