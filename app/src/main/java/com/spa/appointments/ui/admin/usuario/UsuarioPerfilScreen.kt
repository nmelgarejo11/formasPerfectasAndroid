// ui/admin/usuario/UsuarioPerfilScreen.kt
package com.spa.appointments.ui.admin.usuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioPerfilScreen(
    onVolver: () -> Unit,
    viewModel: AdministracionViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    var tab           by remember { mutableIntStateOf(0) }   // 0 = Usuario, 1 = Crear Perfil
    var usuario       by remember { mutableStateOf("") }
    var clave         by remember { mutableStateOf("") }
    var verClave      by remember { mutableStateOf(false) }
    var perfilSel     by remember { mutableIntStateOf(-1) }
    var nombrePerfil  by remember { mutableStateOf("") }
    var descPerfil    by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    // Escuchadores de eventos de éxito y error orientados a la arquitectura base
    LaunchedEffect(state.exitoUsuario) {
        if (state.exitoUsuario) {
            viewModel.limpiarExitos()
            onVolver()
        }
    }

    LaunchedEffect(state.exitoPerfil) {
        if (state.exitoPerfil) {
            viewModel.limpiarExitos()
            nombrePerfil = ""
            descPerfil = ""
            tab = 0 // Regresa al formulario de usuario para usar el perfil creado
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "Seguridad",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = "Accesos y Permisos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Pestañas integradas con la estética limpia del App
            TabRow(selectedTabIndex = tab) {
                Tab(
                    selected = tab == 0,
                    onClick  = { tab = 0 },
                    text     = { Text("Nuevo Usuario", fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = tab == 1,
                    onClick  = { tab = 1 },
                    text     = { Text("Nuevo Perfil", fontWeight = FontWeight.Medium) }
                )
            }

            when (tab) {
                // ── TAB USUARIO ───────────────────────────────────────────────────
                0 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value        = usuario,
                        onValueChange = { usuario = it },
                        label        = { Text("Nombre de usuario") },
                        leadingIcon  = { Icon(Icons.Default.Person, null) },
                        modifier     = Modifier.fillMaxWidth(),
                        shape        = RoundedCornerShape(12.dp),
                        singleLine   = true,
                        enabled      = !state.cargando
                    )

                    OutlinedTextField(
                        value        = clave,
                        onValueChange = { clave = it },
                        label        = { Text("Contraseña") },
                        leadingIcon  = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { verClave = !verClave }) {
                                Icon(
                                    imageVector = if (verClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier             = Modifier.fillMaxWidth(),
                        shape                = RoundedCornerShape(12.dp),
                        singleLine           = true,
                        enabled              = !state.cargando
                    )

                    Text(
                        text       = "Asignar Perfil de Acceso",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(top = 4.dp)
                    )

                    // Lista de perfiles encapsulada en Card idéntica a Servicios/Cargos
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            state.perfiles.forEachIndexed { index, perfil ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !state.cargando) { perfilSel = perfil.id }
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    RadioButton(
                                        selected = perfilSel == perfil.id,
                                        onClick  = { perfilSel = perfil.id },
                                        enabled  = !state.cargando
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text  = perfil.nombre,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (index < state.perfiles.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick  = { viewModel.crearUsuario(usuario.trim(), clave, perfilSel) },
                        enabled  = usuario.isNotBlank() && clave.isNotBlank() && perfilSel != -1 && !state.cargando,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        if (state.cargando) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.PersonAdd, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Registrar Usuario")
                        }
                    }
                }

                // ── TAB PERFIL ────────────────────────────────────────────────────
                1 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value        = nombrePerfil,
                        onValueChange = { nombrePerfil = it },
                        label        = { Text("Nombre del rol o perfil") },
                        leadingIcon  = { Icon(Icons.Default.Badge, null) },
                        modifier     = Modifier.fillMaxWidth(),
                        shape        = RoundedCornerShape(12.dp),
                        singleLine   = true,
                        enabled      = !state.cargando
                    )

                    OutlinedTextField(
                        value        = descPerfil,
                        onValueChange = { descPerfil = it },
                        label        = { Text("Descripción (opcional)") },
                        leadingIcon  = { Icon(Icons.Default.Notes, null) },
                        modifier     = Modifier.fillMaxWidth(),
                        shape        = RoundedCornerShape(12.dp),
                        maxLines     = 2,
                        enabled      = !state.cargando
                    )

                    Text(
                        text       = "Permisos de Módulos (Accesos)",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(top = 4.dp)
                    )

                    // Lista de submódulos encapsulada en Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            state.subModulos.forEachIndexed { index, sub ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !state.cargando) { viewModel.toggleSubModulo(sub.id) }
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Checkbox(
                                        checked         = sub.seleccionado,
                                        onCheckedChange = { viewModel.toggleSubModulo(sub.id) },
                                        enabled         = !state.cargando
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text  = sub.nombre,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (index < state.subModulos.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            viewModel.crearPerfil(
                                nombrePerfil.trim(),
                                descPerfil.ifBlank { null }
                            )
                        },
                        enabled  = nombrePerfil.isNotBlank() && !state.cargando,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (state.cargando) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onSecondary
                            )
                        } else {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar Perfil")
                        }
                    }
                }
            }
        }
    }
}