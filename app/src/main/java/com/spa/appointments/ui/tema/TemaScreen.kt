package com.spa.appointments.ui.tema

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemaScreen(
    onBack: () -> Unit,
    viewModel: TemaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Snackbar
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
                title = { Text("Tema de la Empresa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // — Colores —
            SeccionTitulo("Colores")
            ColorField(
                label = "Color Primario",
                value = state.colorPrimario,
                onValueChange = viewModel::onColorPrimarioChange
            )
            ColorField(
                label = "Color Secundario",
                value = state.colorSecundario,
                onValueChange = viewModel::onColorSecundarioChange
            )
            ColorField(
                label = "Color Terciario",
                value = state.colorTerciario,
                onValueChange = viewModel::onColorTerciarioChange
            )

            HorizontalDivider()

            // — Textos —
            SeccionTitulo("Información")
            OutlinedTextField(
                value = state.nombreApp,
                onValueChange = viewModel::onNombreAppChange,
                label = { Text("Nombre de la App") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = state.slogan,
                onValueChange = viewModel::onSloganChange,
                label = { Text("Slogan") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            HorizontalDivider()

            // — Mensaje WhatsApp —
            SeccionTitulo("Mensaje WhatsApp")
            MensajeWhatsAppField(
                value = state.mensajeWhatsApp,
                onValueChange = viewModel::onMensajeWhatsAppChange,
                variables = viewModel.variablesDisponibles,
                onInsertarVariable = viewModel::insertarVariable
            )

            // — Botón guardar —
            Button(
                onClick = viewModel::guardar,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.guardando
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.guardando) "Guardando..." else "Guardar Cambios")
            }
        }
    }
}

// — Componente: campo de color con preview —
@Composable
private fun ColorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var mostrarDialog by remember { mutableStateOf(false) }

    val colorParseado = runCatching {
        Color(android.graphics.Color.parseColor("#${value.trimStart('#')}"))
    }.getOrNull()

    // Calcula si el color es claro para ajustar el texto
    val esColorClaro = colorParseado?.let {
        (it.red * 0.299 + it.green * 0.587 + it.blue * 0.114) > 0.7f
    } ?: false

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Caja clickeable que abre el picker
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(colorParseado ?: Color.LightGray)
                .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                .clickable { mostrarDialog = true }
        ) {
            Text(
                text = "✎",
                color = if (esColorClaro) Color.Black else Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "#${value.uppercase()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        TextButton(onClick = { mostrarDialog = true }) {
            Text("Cambiar")
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

// — Componente: mensaje WhatsApp con chips y preview —
@Composable
private fun MensajeWhatsAppField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    variables: List<String>,
    onInsertarVariable: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Mensaje") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Chips de variables
        Text(
            text = "Toca para insertar en el cursor:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            variables.forEach { variable ->
                SuggestionChip(
                    onClick = { onInsertarVariable(variable) },
                    label = { Text(variable, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        // Preview con datos de ejemplo
        val preview = value.text
            .replace("{NombreCliente}", "María García")
            .replace("{Fecha}", "15/05/2026")
            .replace("{Hora}", "10:30 AM")

        if (preview.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Vista previa",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// — Título de sección —
@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}