// Ruta: app/src/main/java/com/spa/appointments/ui/tema/TemaScreen.kt
package com.spa.appointments.ui.tema

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spa.appointments.R

// ─── Screen Principal ─────────────────────────────────────────────────────────

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Tema de la empresa",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text  = "Personalización de colores y marca",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
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

        if (state.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color       = MaterialTheme.colorScheme.primary,
                        trackColor  = MaterialTheme.colorScheme.primaryContainer,
                        strokeWidth = 3.dp,
                        modifier    = Modifier.size(36.dp)
                    )
                    Text(
                        "Cargando configuración de tema…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── SECCIÓN COLORES ──────────────────────────────────────────────
            SeccionContainer(
                titulo = "Paleta de Colores",
                icono  = Icons.Outlined.Palette
            ) {
                val colores = listOf(
                    Triple("Color primario", state.colorPrimario, viewModel::onColorPrimarioChange),
                    Triple("Color secundario", state.colorSecundario, viewModel::onColorSecundarioChange),
                    Triple("Color terciario", state.colorTerciario, viewModel::onColorTerciarioChange)
                )

                OutlinedCard(
                    shape  = RoundedCornerShape(18.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        colores.forEachIndexed { index, (label, valor, onChange) ->
                            ColorRow(
                                label         = label,
                                value         = valor,
                                onValueChange = onChange
                            )
                            if (index < colores.lastIndex) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Preview de paleta con Canvas Bar
                PaletaPreview(
                    primario   = state.colorPrimario,
                    secundario = state.colorSecundario,
                    terciario  = state.colorTerciario
                )
            }

            // ── SECCIÓN INFORMACIÓN ──────────────────────────────────────────
            SeccionContainer(
                titulo = "Información de la Empresa",
                icono  = Icons.Outlined.Business
            ) {
                OutlinedCard(
                    shape  = RoundedCornerShape(18.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value         = state.nombreApp,
                            onValueChange = viewModel::onNombreAppChange,
                            label         = { Text("Nombre de la aplicación") },
                            leadingIcon   = { Icon(Icons.Outlined.Store, null, tint = MaterialTheme.colorScheme.primary) },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            shape         = RoundedCornerShape(14.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )

                        OutlinedTextField(
                            value         = state.slogan,
                            onValueChange = viewModel::onSloganChange,
                            label         = { Text("Slogan o lema") },
                            leadingIcon   = { Icon(Icons.Outlined.FormatQuote, null, tint = MaterialTheme.colorScheme.primary) },
                            modifier      = Modifier.fillMaxWidth(),
                            minLines      = 2,
                            maxLines      = 3,
                            shape         = RoundedCornerShape(14.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )

                        OutlinedTextField(
                            value         = state.nit,
                            onValueChange = viewModel::onNitChange,
                            label         = { Text("NIT / Identificación Fiscal") },
                            leadingIcon   = { Icon(Icons.Outlined.Badge, null, tint = MaterialTheme.colorScheme.primary) },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            shape         = RoundedCornerShape(14.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )

                        OutlinedTextField(
                            value         = state.razonSocial,
                            onValueChange = viewModel::onRazonSocialChange,
                            label         = { Text("Razón Social") },
                            leadingIcon   = { Icon(Icons.Outlined.Domain, null, tint = MaterialTheme.colorScheme.primary) },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            shape         = RoundedCornerShape(14.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                    }
                }
            }

            // ── SECCIÓN WHATSAPP ─────────────────────────────────────────────
            SeccionContainer(
                titulo = "Mensaje Automático de WhatsApp",
                icono  = Icons.Outlined.Chat
            ) {
                OutlinedCard(
                    shape  = RoundedCornerShape(18.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MensajeWhatsAppField(
                        value              = state.mensajeWhatsApp,
                        onValueChange      = viewModel::onMensajeWhatsAppChange,
                        variables          = viewModel.variablesDisponibles,
                        onInsertarVariable = viewModel::insertarVariable
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── BOTÓN GUARDAR PRINCIPAL ──────────────────────────────────────
            Button(
                onClick  = viewModel::guardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled  = !state.guardando,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                } else {
                    Icon(
                        imageVector        = Icons.Default.Save,
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text       = if (state.guardando) "Guardando tema…" else "Guardar cambios",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Contenedor de Sección con Header M3 ────────────────────────────────────────

@Composable
private fun SeccionContainer(
    titulo:  String,
    icono:   androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(start = 2.dp)
        ) {
            Icon(
                imageVector        = icono,
                contentDescription = null,
                modifier           = Modifier.size(18.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = titulo.uppercase(),
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.08.sp,
                color      = MaterialTheme.colorScheme.primary
            )
        }
        content()
    }
}

// ── Fila de Color ─────────────────────────────────────────────────────────────

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
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier              = Modifier
            .fillMaxWidth()
            .clickable { mostrarDialog = true }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape    = RoundedCornerShape(12.dp),
            color    = colorParseado ?: Color.LightGray,
            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {}

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text       = "#${value.uppercase().trimStart('#')}",
                style      = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector        = Icons.Default.ChevronRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (mostrarDialog) {
        ColorPickerDialog(
            label               = label,
            colorActual         = value,
            onColorSeleccionado = { hex ->
                onValueChange(hex)
                mostrarDialog = false
            },
            onDismiss = { mostrarDialog = false }
        )
    }
}

// ── Preview de la Paleta de Colores con Canvas ────────────────────────────────

@Composable
private fun PaletaPreview(
    primario:   String,
    secundario: String,
    terciario:  String
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

    OutlinedCard(
        shape  = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text       = "Vista Previa de la Paleta",
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )

            // 🎨 Canvas: Barra de proporción continua trazada con Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                val w = size.width
                val segmentW = w / 3f
                drawRect(color = colorP, size = androidx.compose.ui.geometry.Size(segmentW, size.height))
                drawRect(color = colorS, topLeft = androidx.compose.ui.geometry.Offset(segmentW, 0f), size = androidx.compose.ui.geometry.Size(segmentW, size.height))
                drawRect(color = colorT, topLeft = androidx.compose.ui.geometry.Offset(segmentW * 2, 0f), size = androidx.compose.ui.geometry.Size(segmentW, size.height))
            }

            // Muestras de botones de la paleta
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Primario" to colorP,
                    "Secundario" to colorS,
                    "Terciario" to colorT
                ).forEach { (nombre, color) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape    = RoundedCornerShape(10.dp),
                        color    = color
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text       = nombre,
                                style      = MaterialTheme.typography.labelSmall,
                                color      = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Mensaje WhatsApp con Chips y Burbuja M3 ───────────────────────────────────

@Composable
internal fun MensajeWhatsAppField(
    value:              TextFieldValue,
    onValueChange:      (TextFieldValue) -> Unit,
    variables:          List<String>,
    onInsertarVariable: (String) -> Unit
) {
    val bubbleBackground = Color(0xFFE7F8EE)
    val bubbleText       = Color(0xFF1A3323)
    val bubbleMeta       = Color(0xFF5A9A75)
    val bubbleLabel      = Color(0xFF1A7A45)

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                painter            = painterResource(id = R.drawable.ic_whatsapp),
                contentDescription = "WhatsApp",
                tint               = Color.Unspecified,
                modifier           = Modifier.size(18.dp)
            )
            Text(
                text       = "Plantilla de mensaje automático",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            minLines      = 3,
            maxLines      = 5,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(14.dp),
            colors   = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        Spacer(Modifier.height(10.dp))

        // Chips de variables (Scrollable horizontalmente)
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            variables.forEach { variable ->
                val etiqueta = when {
                    variable.contains("Nombre") -> "+ Nombre"
                    variable.contains("Fecha") -> "+ Fecha"
                    variable.contains("Hora") -> "+ Hora"
                    variable.contains("Duracion") -> "+ Duración"
                    variable.contains("Asistentes") -> "+ Asistentes"
                    else -> "+ $variable"
                }
                SuggestionChip(
                    onClick = { onInsertarVariable(variable) },
                    label   = {
                        Text(
                            etiqueta,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    shape = RoundedCornerShape(50)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(12.dp))

        // Burbuja de preview estilo WhatsApp
        val preview = value.text
            .replace("{NombreCliente}", "María García")
            .replace("{Fecha}", "15/05/2026")
            .replace("{Hora}", "10:30 AM")
            .replace("{Duracion}", "1 Hora 30 Minutos")
            .replace("{Asistentes}", "4 personas")

        if (preview.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                    .background(bubbleBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text       = "VISTA PREVIA WHATSAPP",
                        style      = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.07.sp,
                            fontSize      = 10.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color      = bubbleLabel
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = preview,
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = bubbleText,
                        lineHeight = 20.sp
                    )
                    Text(
                        text      = "10:30 ✓✓",
                        style     = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color     = bubbleMeta,
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
