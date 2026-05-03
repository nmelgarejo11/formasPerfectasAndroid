package com.spa.appointments.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onLoginExpired: () -> Unit
) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    val st = vm.state
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Logo placeholder (reemplaza con tu Image() cuando tengas el asset) ──
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape    = RoundedCornerShape(20.dp),
                    color    = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text  = "PR",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── Títulos ──
                Text(
                    text       = "Bienvenido",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text      = "Inicia sesión para continuar",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // ── Campo usuario ──
                OutlinedTextField(
                    value         = user,
                    onValueChange = { user = it },
                    label         = { Text("Usuario") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // ── Campo contraseña ──
                OutlinedTextField(
                    value                = pass,
                    onValueChange        = { pass = it },
                    label                = { Text("Contraseña") },
                    singleLine           = true,
                    modifier             = Modifier.fillMaxWidth(),
                    shape                = RoundedCornerShape(12.dp),
                    visualTransformation = if (passVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passVisible = !passVisible }) {
                            Icon(
                                imageVector = if (passVisible)
                                    Icons.Outlined.Visibility
                                else
                                    Icons.Outlined.VisibilityOff,
                                contentDescription = if (passVisible)
                                    "Ocultar contraseña"
                                else
                                    "Mostrar contraseña"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (!st.loading) vm.login(user, pass, onLoginSuccess, onLoginExpired)
                        }
                    )
                )

                Spacer(Modifier.height(4.dp))

                // ── Botón principal ──
                Button(
                    onClick  = {
                        focusManager.clearFocus()
                        vm.login(user, pass, onLoginSuccess, onLoginExpired)
                    },
                    enabled  = !st.loading && user.isNotBlank() && pass.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    AnimatedContent(
                        targetState = st.loading,
                        label       = "login_button_content"
                    ) { loading ->
                        if (loading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                color       = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text       = "Acceder",
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 16.sp
                            )
                        }
                    }
                }

                // ── Error con contenedor visual ──
                AnimatedVisibility(
                    visible = st.error != null,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    st.error?.let { errorMsg ->
                        Surface(
                            modifier       = Modifier.fillMaxWidth(),
                            shape          = RoundedCornerShape(10.dp),
                            color          = MaterialTheme.colorScheme.errorContainer,
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text      = errorMsg,
                                color     = MaterialTheme.colorScheme.onErrorContainer,
                                style     = MaterialTheme.typography.bodySmall,
                                modifier  = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}