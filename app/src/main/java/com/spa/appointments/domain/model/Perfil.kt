package com.spa.appointments.domain.model
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Perfil(
    @Json(name = "idUsuario") val idUsuario: Int = 0,
    @Json(name = "usuario") val usuario: String = "",
    @Json(name = "nombreUsuario") val nombreUsuario: String = "",
    @Json(name = "fotoUrl") val fotoUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class ActualizarPerfilRequest(
    @Json(name = "nombreUsuario") val nombreUsuario: String,
    @Json(name = "clave") val clave: String? = null
)

@JsonClass(generateAdapter = true)
data class FotoResponse(
    @Json(name = "fotoUrl") val fotoUrl: String
)

@JsonClass(generateAdapter = true)
data class CrearPerfilRequest(
    @Json(name = "Nombre")      val nombre:      String,
    @Json(name = "Descripcion") val descripcion: String?,
    @Json(name = "SubModulos")  val subModulos:  List<Int>
)

@JsonClass(generateAdapter = true)
data class ConsultaPerfil(
    @Json(name = "Id")     val id:     Int,
    @Json(name = "Nombre") val nombre: String,
    @Json(name = "Descripcion") val descripcion: String?
)

@JsonClass(generateAdapter = true)
data class CambiarPerfilRequest(
    @Json(name = "IdUsuario") val idUsuario: Int,
    @Json(name = "IdPerfil")  val idPerfil:  Int
)