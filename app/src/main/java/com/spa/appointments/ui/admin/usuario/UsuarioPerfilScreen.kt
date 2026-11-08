// Ruta: app/src/main/java/com/spa/appointments/ui/admin/usuario/UsuarioPerfilScreen.kt
package com.spa.appointments.ui.admin.usuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
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
            tab = 0
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // 🎨 Canvas: Acento decorativo debajo de la TopAppBar
                val primaryColor   = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                ) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.4f),
                                secondaryColor.copy(alpha = 0.2f),
                                primaryColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Pestañas integradas M3
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(
                    selected = tab == 0,
                    onClick  = { tab = 0 },
                    text     = { Text("Nuevo Usuario", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = tab == 1,
                    onClick  = { tab = 1 },
                    text     = { Text("Nuevo Perfil", fontWeight = FontWeight.Bold) }
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
                        leadingIcon  = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier     = Modifier.fillMaxWidth(),
                        shape        = RoundedCornerShape(14.dp),
                        colors       = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        singleLine   = true,
                        enabled      = !state.cargando
                    )

                    OutlinedTextField(
                        value        = clave,
                        onValueChange = { clave = it },
                        label        = { Text("Contraseña") },
                        leadingIcon  = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
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
                        shape                = RoundedCornerShape(14.dp),
                        colors               = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        singleLine           = true,
                        enabled              = !state.cargando
                    )

                    Text(
                        text       = "Asignar Perfil de Acceso",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground,
                        modifier   = Modifier.padding(top = 4.dp)
                    )

                    // Lista de perfiles encapsulada en Card M3
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(18.dp),
                        colors   = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column {
                            state.perfiles.forEachIndexed { index, perfil ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !state.cargando) { perfilSel = perfil.id }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    RadioButton(
                                        selected = perfilSel == perfil.id,
                                        onClick  = { perfilSel = perfil.id },
                                        enabled  = !state.cargando
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text       = perfil.nombre,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (index < state.perfiles.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick  = { viewModel.crearUsuario(usuario.trim(), clave, perfilSel) },
                        enabled  = usuario.isNotBlank() && clave.isNotBlank() && perfilSel != -1 && !state.cargando,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(14.dp)
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
                            Text("Registrar Usuario", fontWeight = FontWeight.Bold)
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
                        leadingIcon  = { Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier     = Modifier.fillMaxWidth(),
                        shape        = RoundedCornerShape(14.dp),
                        colors       = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        singleLine   = true,
                        enabled      = !state.cargando
                    )

                    OutlinedTextField(
                        value        = descPerfil,
                        onValueChange = { descPerfil = it },
                        label        = { Text("Descripción (opcional)") },
                        leadingIcon  = { Icon(Icons.Default.Notes, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier     = Modifier.fillMaxWidth(),
                        shape        = RoundedCornerShape(14.dp),
                        colors       = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        maxLines     = 2,
                        enabled      = !state.cargando
                    )

                    Text(
                        text       = "Permisos de Módulos (Accesos)",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground,
                        modifier   = Modifier.padding(top = 4.dp)
                    )

                    // Lista de submódulos encapsulada en Card M3
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(18.dp),
                        colors   = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column {
                            state.subModulos.forEachIndexed { index, sub ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !state.cargando) { viewModel.toggleSubModulo(sub.id) }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Checkbox(
                                        checked         = sub.seleccionado,
                                        onCheckedChange = { viewModel.toggleSubModulo(sub.id) },
                                        enabled         = !state.cargando
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text       = sub.nombre,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (index < state.subModulos.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.crearPerfil(
                                nombrePerfil.trim(),
                                descPerfil.ifBlank { null }
                            )
                        },
                        enabled  = nombrePerfil.isNotBlank() && !state.cargando,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(14.dp),
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
                            Text("Guardar Perfil", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
