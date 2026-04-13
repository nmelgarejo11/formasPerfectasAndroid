package com.spa.appointments.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    vm:             LoginViewModel,
    onLoginSuccess: () -> Unit,
    onLoginExpired: () -> Unit
) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val st = vm.state

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text  = "Iniciar sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value         = user,
                    onValueChange = { user = it },
                    label         = { Text("Usuario") },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedTextColor     = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor   = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor    = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor          = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value                = pass,
                    onValueChange        = { pass = it },
                    label                = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier             = Modifier.fillMaxWidth(),
                    colors               = OutlinedTextFieldDefaults.colors(
                        focusedTextColor     = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor   = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor    = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor          = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick  = { vm.login(user, pass, onLoginSuccess, onLoginExpired) },
                    enabled  = !st.loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (st.loading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Acceder")
                    }
                }

                st.error?.let {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text  = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}