package com.spa.appointments.ui.tema

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.res.painterResource
import com.spa.appointments.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemaScreen(
    onBack: () -> Unit,
    viewModel: TemaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.exito) {
        if (state.exito) {
            snackbarHostState.showSnackbar("Tema actualizado correctamente")
            viewModel.limpiarExito()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tema de la empresa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    // Botón atrás con borde circular
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(50))
                            .border(
                                0.5.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(50)
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onBack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (state.cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── SECCIÓN COLORES ──────────────────────────────────────────────
            SeccionContainer(titulo = "Paleta de colores") {

                // Lista de colores
                val colores = listOf(
                    Triple("Color primario", state.colorPrimario, viewModel::onColorPrimarioChange),
                    Triple("Color secundario", state.colorSecundario, viewModel::onColorSecundarioChange),
                    Triple("Color terciario", state.colorTerciario, viewModel::onColorTerciarioChange)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp, MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    colores.forEachIndexed { index, (label, valor, onChange) ->
                        ColorRow(
                            label = label,
                            value = valor,
                            onValueChange = onChange
                        )
                        if (index < colores.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }

                // Preview de paleta
                PaletaPreview(
                    primario = state.colorPrimario,
                    secundario = state.colorSecundario,
                    terciario = state.colorTerciario
                )
            }

            // ── SECCIÓN INFORMACIÓN ──────────────────────────────────────────
            SeccionContainer(titulo = "Información") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp, MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column {
                        CampoTextoPlano(
                            label = "Nombre de la app",
                            value = state.nombreApp,
                            onValueChange = viewModel::onNombreAppChange,
                            capitalization = KeyboardCapitalization.Words,
                            singleLine = true
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        CampoTextoPlano(
                            label = "Slogan",
                            value = state.slogan,
                            onValueChange = viewModel::onSloganChange,
                            capitalization = KeyboardCapitalization.Sentences,
                            minLines = 2
                        )
                    }
                }
            }

            // ── SECCIÓN WHATSAPP ─────────────────────────────────────────────
            SeccionContainer(titulo = "Mensaje WhatsApp") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp, MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    MensajeWhatsAppField(
                        value = state.mensajeWhatsApp,
                        onValueChange = viewModel::onMensajeWhatsAppChange,
                        variables = viewModel.variablesDisponibles,
                        onInsertarVariable = viewModel::insertarVariable
                    )
                }
            }

            // ── BOTÓN GUARDAR PRINCIPAL ──────────────────────────────────────
            Button(
                onClick = viewModel::guardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.guardando,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.surface
                    )
                    Spacer(Modifier.width(10.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = if (state.guardando) "Guardando..." else "Guardar cambios",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ── Contenedor de sección con header minimalista ─────────────────────────────
@Composable
private fun SeccionContainer(
    titulo: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header: punto + label en uppercase
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.onSurface)
            )
            Text(
                text = titulo.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.08.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

// ── Fila de color dentro del card ────────────────────────────────────────────
@Composable
private fun ColorRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var mostrarDialog by remember { mutableStateOf(false) }

    val colorParseado = runCatching {
        Color(android.graphics.Color.parseColor("#${value.trimStart('#')}"))
    }.getOrNull()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { mostrarDialog = true }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Swatch rectangular redondeado
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorParseado ?: Color.LightGray)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "#${value.uppercase().trimStart('#')}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Botón editar pequeño
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    0.5.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(8.dp)
                )
        ) {
            Icon(
                // Reemplaza con el ícono de lápiz que uses en tu proyecto
                imageVector = Icons.Default.Save, // <- cambiar por Edit/Pencil
                contentDescription = "Editar color",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (mostrarDialog) {
        ColorPickerDialog(
            label = label,
            colorActual = value,
            onColorSeleccionado = { hex ->
                onValueChange(hex)
                mostrarDialog = false
            },
            onDismiss = { mostrarDialog = false }
        )
    }
}

// ── Preview de la paleta de colores ──────────────────────────────────────────
@Composable
private fun PaletaPreview(
    primario: String,
    secundario: String,
    terciario: String
) {
    val colorP = runCatching {
        Color(android.graphics.Color.parseColor("#${primario.trimStart('#')}"))
    }.getOrElse { Color(0xFF6C4FD4) }

    val colorS = runCatching {
        Color(android.graphics.Color.parseColor("#${secundario.trimStart('#')}"))
    }.getOrElse { Color(0xFF1DB88B) }

    val colorT = runCatching {
        Color(android.graphics.Color.parseColor("#${terciario.trimStart('#')}"))
    }.getOrElse { Color(0xFFF4975A) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Vista previa",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            // Barra de proporción de colores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(Modifier.weight(1f).fillMaxHeight().background(colorP))
                Box(Modifier.weight(1f).fillMaxHeight().background(colorS))
                Box(Modifier.weight(1f).fillMaxHeight().background(colorT))
            }
            // Botones de muestra
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Primario" to colorP,
                    "Secundario" to colorS,
                    "Terciario" to colorT
                ).forEach { (nombre, color) ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                    ) {
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ── Campo de texto plano dentro de card (sin OutlinedTextField) ───────────────
@Composable
private fun CampoTextoPlano(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.07.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(capitalization = capitalization),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = "Escribe aquí...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                inner()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Mensaje WhatsApp con chips y burbuja de preview ───────────────────────────
@Composable
private fun MensajeWhatsAppField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    variables: List<String>,
    onInsertarVariable: (String) -> Unit
) {
    val whatsappVerde = Color(0xFF25D366)
    val bubbleBackground = Color(0xFFE7F8EE)
    val bubbleText = Color(0xFF1A3323)
    val bubbleMeta = Color(0xFF5A9A75)
    val bubbleLabel = Color(0xFF1A7A45)

    Column {
        // Header con ícono WhatsApp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_whatsapp),
                contentDescription = "WhatsApp",
                tint = Color.Unspecified,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Texto de confirmación automática",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Campo de texto
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Chips de variables
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            variables.forEach { variable ->
                val etiqueta = when {
                    variable.contains("Nombre") -> "+ Nombre"
                    variable.contains("Fecha") -> "+ Fecha"
                    variable.contains("Hora") -> "+ Hora"
                    else -> "+ $variable"
                }
                SuggestionChip(
                    onClick = { onInsertarVariable(variable) },
                    label = {
                        Text(
                            etiqueta,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = RoundedCornerShape(50)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Burbuja de preview estilo WhatsApp
        val preview = value.text
            .replace("{NombreCliente}", "María García")
            .replace("{Fecha}", "15/05/2026")
            .replace("{Hora}", "10:30 AM")

        if (preview.isNotBlank()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(bubbleBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = "VISTA PREVIA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.07.sp,
                            fontSize = 10.sp
                        ),
                        fontWeight = FontWeight.Medium,
                        color = bubbleLabel
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = bubbleText,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "10:30 ✓✓",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = bubbleMeta,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        }
    }
}