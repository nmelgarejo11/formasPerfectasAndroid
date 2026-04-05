package com.spa.appointments.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SlotDisponible(
    @Json(name = "horaInicio") val horaInicio: String,
    @Json(name = "horaFin")    val horaFin: String,
    @Json(name = "disponible") val disponible: Boolean
)

@JsonClass(generateAdapter = true)
data class ServicioDetalle(
    @Json(name = "idServicio") val idServicio: Int,
    @Json(name = "precio")     val precio: Double,
    @Json(name = "duracion")   val duracion: Int
)

@JsonClass(generateAdapter = true)
data class CrearCitaRequest(
    @Json(name = "idSede")        val idSede: Int,
    @Json(name = "idCliente")     val idCliente: Int,
    @Json(name = "idProfesional") val idProfesional: Int,
    @Json(name = "fechaInicio")   val fechaInicio: String,
    @Json(name = "fechaFin")      val fechaFin: String,
    @Json(name = "notas")         val notas: String?,
    @Json(name = "servicios")     val servicios: List<ServicioDetalle>
)

@JsonClass(generateAdapter = true)
data class CrearCitaResponse(
    @Json(name = "idCita")  val idCita: Int,
    @Json(name = "mensaje") val mensaje: String
)

@JsonClass(generateAdapter = true)
data class Cita(
    @Json(name = "id")               val id: Int,
    @Json(name = "fechaHoraInicio")  val fechaHoraInicio: String,
    @Json(name = "fechaHoraFin")     val fechaHoraFin: String,
    @Json(name = "notas")            val notas: String?,
    @Json(name = "idEstado")         val idEstado: Int,
    @Json(name = "estado")           val estado: String,
    @Json(name = "colorEstado")      val colorEstado: String?,
    @Json(name = "idProfesional")    val idProfesional: Int,
    @Json(name = "profesional")      val profesional: String,
    @Json(name = "fotoProfesional")  val fotoProfesional: String?,
    @Json(name = "cargoProfesional") val cargoProfesional: String?,
    @Json(name = "sede")             val sede: String,
    @Json(name = "servicios")        val servicios: String?,
    @Json(name = "total")            val total: Double
)

@JsonClass(generateAdapter = true)
data class AccionCitaCancelarRequest(
    @Json(name = "idCita")  val idCita: Int
)

@JsonClass(generateAdapter = true)
data class AccionCitaReagendarRequest(
    @Json(name = "idCita")  val idCita: Int,
    @Json(name = "motivo")  val motivo: String?
)

@JsonClass(generateAdapter = true)
data class AccionCitaResponse(
    @Json(name = "ok")      val ok: Boolean,
    @Json(name = "mensaje") val mensaje: String
)
