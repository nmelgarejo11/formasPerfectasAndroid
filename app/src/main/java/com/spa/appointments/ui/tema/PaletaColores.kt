package com.spa.appointments.ui.tema

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

val PALETA_COLORES = listOf(

    // ======================
    // ROJOS
    // ======================
    "B71C1C" to "Rojo Vino",
    "C62828" to "Rojo Carmesí",
    "D32F2F" to "Rojo Oscuro",
    "E53935" to "Rojo Intenso",
    "F44336" to "Rojo",
    "EF5350" to "Rojo Claro",
    "E57373" to "Rojo Pastel",
    "EF9A9A" to "Rosa Rojizo",
    "FFCDD2" to "Rosa Claro",

    // ======================
    // ROSAS
    // ======================
    "880E4F" to "Fucsia Oscuro",
    "AD1457" to "Rosa Frambuesa",
    "C2185B" to "Rosa Oscuro",
    "D81B60" to "Rosa Intenso",
    "E91E63" to "Rosa",
    "EC407A" to "Rosa Medio",
    "F06292" to "Rosa Claro",
    "F48FB1" to "Rosa Suave",
    "F8BBD0" to "Rosa Pastel",
    "FF80AB" to "Rosa Chicle",

    // ======================
    // MORADOS
    // ======================
    "4A148C" to "Morado Profundo",
    "6A1B9A" to "Púrpura Oscuro",
    "7B1FA2" to "Morado",
    "8E24AA" to "Púrpura",
    "9C27B0" to "Violeta",
    "AB47BC" to "Violeta Claro",
    "BA68C8" to "Lila",
    "CE93D8" to "Lavanda",
    "E1BEE7" to "Lavanda Pastel",

    // ======================
    // AZUL OSCURO / ÍNDIGO
    // ======================
    "1A237E" to "Azul Índigo Profundo",
    "283593" to "Índigo Oscuro",
    "303F9F" to "Índigo",
    "3949AB" to "Azul Índigo",
    "3F51B5" to "Índigo Medio",
    "5C6BC0" to "Índigo Claro",
    "7986CB" to "Índigo Pastel",
    "9FA8DA" to "Lavanda Azul",

    // ======================
    // AZULES
    // ======================
    "0D47A1" to "Azul Marino",
    "1565C0" to "Azul Rey",
    "1976D2" to "Azul Oscuro",
    "1E88E5" to "Azul Intenso",
    "2196F3" to "Azul",
    "42A5F5" to "Azul Medio",
    "64B5F6" to "Azul Claro",
    "90CAF9" to "Celeste",
    "BBDEFB" to "Azul Pastel",
    "82B1FF" to "Azul Eléctrico",

    // ======================
    // CYAN / TURQUESA
    // ======================
    "006064" to "Turquesa Oscuro",
    "00838F" to "Cyan Oscuro",
    "0097A7" to "Turquesa",
    "00ACC1" to "Cyan",
    "00BCD4" to "Aqua",
    "26C6DA" to "Aqua Claro",
    "4DD0E1" to "Turquesa Claro",
    "80DEEA" to "Menta Azul",
    "B2EBF2" to "Cyan Pastel",

    // ======================
    // TEAL / VERDE AGUA
    // ======================
    "004D40" to "Verde Petróleo",
    "00695C" to "Teal Oscuro",
    "00796B" to "Verde Agua",
    "00897B" to "Teal Intenso",
    "009688" to "Teal",
    "26A69A" to "Menta Oscura",
    "4DB6AC" to "Menta",
    "80CBC4" to "Menta Claro",
    "B2DFDB" to "Menta Pastel",

    // ======================
    // VERDES
    // ======================
    "1B5E20" to "Verde Bosque",
    "2E7D32" to "Verde Oscuro",
    "388E3C" to "Verde Esmeralda",
    "43A047" to "Verde Intenso",
    "4CAF50" to "Verde",
    "66BB6A" to "Verde Medio",
    "81C784" to "Verde Claro",
    "A5D6A7" to "Verde Pastel",
    "C8E6C9" to "Verde Menta",

    // ======================
    // LIMA
    // ======================
    "827717" to "Oliva Oscuro",
    "9E9D24" to "Lima Oscuro",
    "AFB42B" to "Lima",
    "C0CA33" to "Verde Lima",
    "CDDC39" to "Lima Claro",
    "DCE775" to "Lima Pastel",

    // ======================
    // AMARILLOS
    // ======================
    "F57F17" to "Mostaza Oscuro",
    "F9A825" to "Oro",
    "FBC02D" to "Ámbar Oscuro",
    "FDD835" to "Amarillo Oro",
    "FFEB3B" to "Amarillo",
    "FFEE58" to "Amarillo Claro",
    "FFF176" to "Amarillo Pastel",
    "FFF59D" to "Crema Amarilla",

    // ======================
    // NARANJAS
    // ======================
    "E65100" to "Naranja Quemado",
    "EF6C00" to "Naranja Oscuro",
    "F57C00" to "Naranja Fuerte",
    "FB8C00" to "Naranja",
    "FF9800" to "Ámbar",
    "FFA726" to "Naranja Claro",
    "FFB74D" to "Durazno",
    "FFCC80" to "Melocotón",

    // ======================
    // NARANJA ROJO
    // ======================
    "BF360C" to "Terracota Oscuro",
    "D84315" to "Coral Oscuro",
    "E64A19" to "Coral",
    "F4511E" to "Naranja Rojizo",
    "FF5722" to "Coral Intenso",
    "FF8A65" to "Salmón",

    // ======================
    // CAFÉS
    // ======================
    "3E2723" to "Chocolate Oscuro",
    "4E342E" to "Café Espresso",
    "5D4037" to "Chocolate",
    "6D4C41" to "Café Oscuro",
    "795548" to "Café",
    "8D6E63" to "Café Claro",
    "A1887F" to "Capuchino",
    "BCAAA4" to "Arena",

    // ======================
    // GRISES
    // ======================
    "212121" to "Negro",
    "424242" to "Grafito",
    "616161" to "Gris Carbón",
    "757575" to "Gris Oscuro",
    "9E9E9E" to "Gris",
    "BDBDBD" to "Gris Claro",
    "E0E0E0" to "Plata",
    "EEEEEE" to "Gris Perla",
    "F5F5F5" to "Blanco Humo",
    "FAFAFA" to "Blanco Nieve",
    "FFFFFF" to "Blanco",

    // ======================
    // TONOS PREMIUM / MODERNOS
    // ======================
    "0F172A" to "Azul Noche",
    "1E293B" to "Pizarra Oscura",
    "334155" to "Slate",
    "475569" to "Slate Claro",
    "64748B" to "Acero",

    "7F1D1D" to "Burdeos",
    "881337" to "Magenta Oscuro",
    "BE185D" to "Fucsia Premium",

    "312E81" to "Índigo Nocturno",
    "4C1D95" to "Púrpura Real",

    "164E63" to "Azul Petróleo",
    "155E75" to "Turquesa Profundo",

    "14532D" to "Verde Militar",
    "365314" to "Verde Oliva",

    "713F12" to "Bronce",
    "78350F" to "Marrón Oscuro",

    "FECACA" to "Rosa Bebé",
    "FDE68A" to "Vainilla",
    "BFDBFE" to "Azul Bebé",
    "C7D2FE" to "Lavanda Azul Claro",
    "A7F3D0" to "Menta Pastel",
    "DDD6FE" to "Lila Pastel",
    "FBCFE8" to "Rosa Pastel Premium",
    "FED7AA" to "Durazno Claro"
)

@Composable
fun ColorPickerDialog(
    label: String,
    colorActual: String,
    onColorSeleccionado: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var hexManual by remember { mutableStateOf(colorActual.trimStart('#').uppercase()) }
    var hexError by remember { mutableStateOf(false) }

    val colorPreview = runCatching {
        Color(android.graphics.Color.parseColor("#${hexManual.trimStart('#')}"))
    }.getOrNull()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // — Grilla de colores predefinidos —
                val columnas = 5
                PALETA_COLORES.chunked(columnas).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        fila.forEach { (hex, _) ->
                            val color = runCatching {
                                Color(android.graphics.Color.parseColor("#$hex"))
                            }.getOrElse { Color.LightGray }

                            val seleccionado = hex.equals(hexManual.trimStart('#'), ignoreCase = true)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (seleccionado) 3.dp else 1.dp,
                                        color = if (seleccionado)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        hexManual = hex.uppercase()
                                        hexError = false
                                    }
                            ) {
                                if (seleccionado) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = if (hex == "FFFFFF") Color.Black else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // — Campo hex manual —
                Text(
                    text = "O ingresa un código HEX:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = hexManual,
                        onValueChange = { nuevo ->
                            val limpio = nuevo.trimStart('#').uppercase()
                            if (limpio.length <= 6 && limpio.all { it.isDigit() || it in "ABCDEF" }) {
                                hexManual = limpio
                                hexError = limpio.length == 6 && runCatching {
                                    android.graphics.Color.parseColor("#$limpio")
                                }.isFailure
                            }
                        },
                        prefix = { Text("#") },
                        isError = hexError,
                        supportingText = if (hexError) {{ Text("Código inválido") }} else null,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("6650A4") }
                    )

                    // Preview del color actual
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorPreview ?: Color.LightGray)
                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                    )
                }

                // — Botones —
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            if (hexManual.length == 6 && colorPreview != null) {
                                onColorSeleccionado(hexManual)
                            } else {
                                hexError = true
                            }
                        }
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }
}