package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.text.Collator
import java.util.Locale

private val spanishCollator: Collator = Collator.getInstance(Locale("es", "ES")).apply {
    strength = Collator.PRIMARY
}

fun List<Cliente>.ordenarAlfabeticamente(): List<Cliente> {
    return this.sortedWith { c1, c2 ->
        val name1 = c1.nombreCompleto.trim()
        val name2 = c2.nombreCompleto.trim()
        val res = spanishCollator.compare(name1, name2)
        if (res != 0) res else name1.compareTo(name2, ignoreCase = true)
    }
}

@JsonClass(generateAdapter = true)
data class Cliente(
    @Json(name = "id")       val id: Int,
    @Json(name = "nombre")   val nombre: String,
    @Json(name = "apellido") val apellido: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "email")    val email: String?,
    @Json(name = "estado") val estado: Boolean = true
) {
    val nombreCompleto get() = "$nombre $apellido"
}

@JsonClass(generateAdapter = true)
data class CrearClienteRequest(
    @Json(name = "nombre")   val nombre: String,
    @Json(name = "apellido") val apellido: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "email")    val email: String?
)

@JsonClass(generateAdapter = true)
data class ActualizarClienteRequest(
    @Json(name = "nombre") val nombre: String,
    @Json(name = "apellido") val apellido: String,
    @Json(name = "telefono") val telefono: String?,
    @Json(name = "email") val email: String?
)