package com.spa.appointments.ui.admin.catalogos

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class IconoOpcion(val clave: String, val icono: ImageVector, val etiqueta: String)

val ICONOS_DISPONIBLES = listOf(

    // -------------------- CATEGORIAS --------------------
    IconoOpcion("calendar", Icons.Default.CalendarMonth, "Calendario"),
    IconoOpcion("star", Icons.Default.Star, "Favorito"),
    IconoOpcion("attach_money", Icons.Default.AttachMoney, "Dinero"),
    IconoOpcion("person", Icons.Default.Person, "Usuario"),
    IconoOpcion("add", Icons.Default.Add, "Agregar"),
    IconoOpcion("history", Icons.Default.History, "Historial"),
    IconoOpcion("group", Icons.Default.Group, "Grupo"),
    IconoOpcion("people", Icons.Default.People, "Personas"),
    IconoOpcion("settings", Icons.Default.Settings, "Configuración"),
    IconoOpcion("admin_panel_settings", Icons.Default.AdminPanelSettings, "Administración"),
    IconoOpcion("category", Icons.Default.Category, "Categoría"),

    // -------------------- BELLEZA Y SPA --------------------
    IconoOpcion("spa", Icons.Default.Spa, "Spa"),
    IconoOpcion("self_improvement", Icons.Default.SelfImprovement, "Meditación"),
    IconoOpcion("face", Icons.Default.Face, "Facial"),
    IconoOpcion("colorize", Icons.Default.Colorize, "Color"),
    IconoOpcion("brush", Icons.Default.Brush, "Brocha"),
    IconoOpcion("auto_fix_high", Icons.Default.AutoFixHigh, "Magia"),

    // -------------------- SALUD --------------------
    IconoOpcion("favorite", Icons.Default.Favorite, "Salud"),
    IconoOpcion("healing", Icons.Default.Healing, "Curación"),
    IconoOpcion("health_and_safety", Icons.Default.HealthAndSafety, "Seguridad"),
    IconoOpcion("medical_services", Icons.Default.MedicalServices, "Médico"),
    IconoOpcion("monitor_heart", Icons.Default.MonitorHeart, "Corazón"),
    IconoOpcion("psychology", Icons.Default.Psychology, "Psicología"),
    IconoOpcion("local_hospital", Icons.Default.LocalHospital, "Hospital"),
    IconoOpcion("vaccines", Icons.Default.Vaccines, "Vacunas"),

    // -------------------- FITNESS --------------------
    IconoOpcion("fitness_center", Icons.Default.FitnessCenter, "Gym"),
    IconoOpcion("sports", Icons.Default.Sports, "Deporte"),
    IconoOpcion("pool", Icons.Default.Pool, "Piscina"),
    IconoOpcion("hiking", Icons.Default.Hiking, "Senderismo"),
    IconoOpcion("sports_martial_arts", Icons.Default.SportsMartialArts, "Artes marciales"),

    // -------------------- RESTAURANTES / COMIDA --------------------
    IconoOpcion("restaurant", Icons.Default.Restaurant, "Restaurante"),
    IconoOpcion("fastfood", Icons.Default.Fastfood, "Comida rápida"),
    IconoOpcion("local_cafe", Icons.Default.LocalCafe, "Café"),
    IconoOpcion("local_bar", Icons.Default.LocalBar, "Bar"),
    IconoOpcion("cake", Icons.Default.Cake, "Pastelería"),
    IconoOpcion("icecream", Icons.Default.Icecream, "Heladería"),
    IconoOpcion("lunch_dining", Icons.Default.LunchDining, "Almuerzos"),

    // -------------------- COMERCIO / TIENDA --------------------
    IconoOpcion("store", Icons.Default.Store, "Tienda"),
    IconoOpcion("shopping_cart", Icons.Default.ShoppingCart, "Carrito"),
    IconoOpcion("shopping_bag", Icons.Default.ShoppingBag, "Compras"),
    IconoOpcion("point_of_sale", Icons.Default.PointOfSale, "Caja"),
    IconoOpcion("receipt", Icons.Default.Receipt, "Factura"),
    IconoOpcion("inventory", Icons.Default.Inventory, "Inventario"),

    // -------------------- SERVICIOS --------------------
    IconoOpcion("build", Icons.Default.Build, "Servicio técnico"),
    IconoOpcion("cleaning_services", Icons.Default.CleaningServices, "Limpieza"),
    IconoOpcion("plumbing", Icons.Default.Plumbing, "Plomería"),
    IconoOpcion("electrical_services", Icons.Default.ElectricalServices, "Electricidad"),
    IconoOpcion("car_repair", Icons.Default.CarRepair, "Reparación"),
    IconoOpcion("support_agent", Icons.Default.SupportAgent, "Soporte"),

    // -------------------- TECNOLOGÍA --------------------
    IconoOpcion("computer", Icons.Default.Computer, "Computadores"),
    IconoOpcion("smartphone", Icons.Default.Smartphone, "Celulares"),
    IconoOpcion("devices", Icons.Default.Devices, "Dispositivos"),
    IconoOpcion("cloud", Icons.Default.Cloud, "Nube"),
    IconoOpcion("security", Icons.Default.Security, "Seguridad IT"),
    IconoOpcion("code", Icons.Default.Code, "Desarrollo"),

    // -------------------- EDUCACIÓN --------------------
    IconoOpcion("school", Icons.Default.School, "Educación"),
    IconoOpcion("quiz", Icons.Default.Quiz, "Exámenes"),
    IconoOpcion("workspace_premium", Icons.Default.WorkspacePremium, "Certificación"),

    // -------------------- TRANSPORTE --------------------
    IconoOpcion("directions_car", Icons.Default.DirectionsCar, "Carro"),
    IconoOpcion("two_wheeler", Icons.Default.TwoWheeler, "Moto"),
    IconoOpcion("local_taxi", Icons.Default.LocalTaxi, "Taxi"),
    IconoOpcion("flight", Icons.Default.Flight, "Vuelo"),
    IconoOpcion("delivery_dining", Icons.Default.DeliveryDining, "Domicilios"),

    // -------------------- HOGAR --------------------
    IconoOpcion("home", Icons.Default.Home, "Hogar"),
    IconoOpcion("chair", Icons.Default.Chair, "Muebles"),
    IconoOpcion("kitchen", Icons.Default.Kitchen, "Cocina"),
    IconoOpcion("bed", Icons.Default.Bed, "Dormitorio"),

    // -------------------- FINANZAS --------------------
    IconoOpcion("attach_money", Icons.Default.AttachMoney, "Dinero"),
    IconoOpcion("credit_card", Icons.Default.CreditCard, "Tarjeta"),
    IconoOpcion("account_balance", Icons.Default.AccountBalance, "Banco"),

    // -------------------- GENERALES --------------------
    IconoOpcion("category", Icons.Default.Category, "Categoría"),
    IconoOpcion("star", Icons.Default.Star, "Destacado"),
    IconoOpcion("local_offer", Icons.Default.LocalOffer, "Oferta"),
    IconoOpcion("emoji_events", Icons.Default.EmojiEvents, "Premio"),

    // -------------------- TIEMPO --------------------
    IconoOpcion("schedule", Icons.Default.Schedule, "Horario"),
    IconoOpcion("event", Icons.Default.Event, "Evento"),
    IconoOpcion("timer", Icons.Default.Timer, "Tiempo")
)

@Composable
fun IconoPickerDialog(
    iconoActual: String?,
    onSeleccionar: (IconoOpcion) -> Unit,
    onDismiss: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }

    val filtrados = remember(busqueda) {
        if (busqueda.isBlank()) ICONOS_DISPONIBLES
        else ICONOS_DISPONIBLES.filter {
            it.etiqueta.contains(busqueda, ignoreCase = true) ||
                    it.clave.contains(busqueda, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar ícono") },
        text = {
            Column {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    label = { Text("Buscar") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtrados) { opcion ->
                        val seleccionado = opcion.clave == iconoActual
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (seleccionado) 2.dp else 0.dp,
                                    color = if (seleccionado)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSeleccionar(opcion) }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = opcion.icono,
                                contentDescription = opcion.etiqueta,
                                modifier = Modifier.size(32.dp),
                                tint = if (seleccionado)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = opcion.etiqueta,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}