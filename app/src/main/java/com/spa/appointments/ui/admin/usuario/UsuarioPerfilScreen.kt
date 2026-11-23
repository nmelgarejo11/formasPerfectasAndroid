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
import com.spa.appointments.ui.components.SinPermisoContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioPerfilScreen(
    onVolver: () -> Unit,
    viewModel: AdministracionViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    var tab           by remember { mutableIntStateOf(0) }
    var usuario       by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
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
                                text  = "Crear usuarios y definir perfiles de acceso",
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

        if (state.isForbidden) {
            SinPermisoContent(
                modifier = Modifier.padding(padding),
                onVolver = onVolver
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(
                    selectedTabIndex = tab,
                    containerColor   = MaterialTheme.colorScheme.background,
                    contentColor     = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = tab == 0,
                        onClick  = { tab = 0 },
                        text     = { Text("Crear Usuario", fontWeight = FontWeight.Bold) },
                        icon     = { Icon(Icons.Default.PersonAdd, null) }
                    )
                    Tab(
                        selected = tab == 1,
                        onClick  = { tab = 1 },
                        text     = { Text("Crear Perfil", fontWeight = FontWeight.Bold) },
                        icon     = { Icon(Icons.Default.Shield, null) }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (tab == 0) {
                        OutlinedTextField(
                            value         = nombreUsuario,
                            onValueChange = { nombreUsuario = it },
                            label         = { Text("Nombre completo") },
                            supportingText= { Text("Nombre personalizado que se mostrará en el saludo al ingresar") },
                            leadingIcon   = { Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )

                        OutlinedTextField(
                            value         = usuario,
                            onValueChange = { usuario = it },
                            label         = { Text("Nombre de usuario *") },
                            leadingIcon   = { Icon(Icons.Default.AccountCircle, null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )

                        OutlinedTextField(
                            value                = clave,
                            onValueChange        = { clave = it },
                            label                = { Text("Contraseña *") },
                            leadingIcon          = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon         = {
                                IconButton(onClick = { verClave = !verClave }) {
                                    Icon(
                                        imageVector        = if (verClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine           = true,
                            modifier             = Modifier.fillMaxWidth(),
                            shape                = RoundedCornerShape(12.dp),
                            colors               = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )

                        Spacer(Modifier.height(2.dp))

                        Text(
                            text       = "Perfil de acceso *",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )

                        if (state.perfiles.isEmpty()) {
                            Text(
                                text  = "No hay perfiles. Crea uno en la otra pestaña.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            state.perfiles.forEach { perfil ->
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape    = RoundedCornerShape(12.dp),
                                    colors   = CardDefaults.outlinedCardColors(
                                        containerColor = if (perfilSel == perfil.id)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                        else
                                            MaterialTheme.colorScheme.surfaceContainerLow
                                    ),
                                    border  = BorderStroke(
                                        if (perfilSel == perfil.id) 1.5.dp else 1.dp,
                                        if (perfilSel == perfil.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    onClick = { perfilSel = perfil.id }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier          = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        RadioButton(
                                            selected = perfilSel == perfil.id,
                                            onClick  = { perfilSel = perfil.id }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(perfil.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            perfil.descripcion?.let {
                                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick  = { viewModel.crearUsuario(usuario.trim(), nombreUsuario.trim(), clave.trim(), perfilSel) },
                            enabled  = usuario.isNotBlank() && clave.isNotBlank() && perfilSel != -1 && !state.cargando,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            if (state.cargando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Guardar Usuario", fontWeight = FontWeight.Bold)
                            }
                        }

                    } else {
                        OutlinedTextField(
                            value         = nombrePerfil,
                            onValueChange = { nombrePerfil = it },
                            label         = { Text("Nombre del perfil *") },
                            leadingIcon   = { Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )

                        OutlinedTextField(
                            value         = descPerfil,
                            onValueChange = { descPerfil = it },
                            label         = { Text("Descripción (opcional)") },
                            leadingIcon   = { Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )

                        Text(
                            text       = "Submódulos permitidos *",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )

                        state.subModulos.forEach { sub ->
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(12.dp),
                                colors   = CardDefaults.outlinedCardColors(
                                    containerColor = if (sub.seleccionado)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                border  = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                onClick = { viewModel.toggleSubModulo(sub.id) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier          = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Checkbox(
                                        checked         = sub.seleccionado,
                                        onCheckedChange = { viewModel.toggleSubModulo(sub.id) }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(sub.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Button(
                            onClick  = { viewModel.crearPerfil(nombrePerfil.trim(), descPerfil.ifBlank { null }) },
                            enabled  = nombrePerfil.isNotBlank() && state.subModulos.any { it.seleccionado } && !state.cargando,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            if (state.cargando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Guardar Perfil", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
