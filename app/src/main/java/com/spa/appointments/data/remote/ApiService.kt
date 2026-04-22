package com.spa.appointments.data.remote

import com.spa.appointments.domain.model.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.MultipartBody

interface ApiService {

    // ── Auth ──────────────────────────────────────
    @POST("Auth/Login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // ── Menú ──────────────────────────────────────
    @GET("menu/menu")
    suspend fun obtenerMenu(): MenuResponse

    // ── Servicios ─────────────────────────────────
    @GET("Servicios")
    suspend fun getServicios(): List<Servicio>

    // ── Profesionales ─────────────────────────────
    @GET("Profesionales")
    suspend fun getProfesionales(
        @Query("idSede") idSede: Int? = null
    ): List<Profesional>

    @GET("Profesionales/{idProfesional}/servicios")
    suspend fun getServiciosPorProfesional(
        @Path("idProfesional") idProfesional: Int
    ): List<Servicio>

    // ── Disponibilidad ────────────────────────────
    @GET("Agenda/disponibilidad")
    suspend fun getDisponibilidad(
        @Query("idProfesional") idProfesional: Int,
        @Query("idSede")        idSede: Int,
        @Query("fecha")         fecha: String,
        @Query("duracion")      duracion: Int
    ): List<SlotDisponible>

    // ── Citas ─────────────────────────────────────
    @POST("Citas")
    suspend fun crearCita(@Body request: CrearCitaRequest): CrearCitaResponse

    @GET("Citas/activas")
    suspend fun getCitasActivas(@Query("idCliente") idCliente: Int): List<Cita>

    @GET("Citas/historial")
    suspend fun getCitasHistorial(@Query("idCliente") idCliente: Int): List<Cita>

    @PUT("Citas/cancelar")
    suspend fun cancelarCita(@Body request: AccionCitaCancelarRequest): AccionCitaResponse

    @PUT("Citas/reagendar")
    suspend fun reagendarCita(@Body request: AccionCitaReagendarRequest): AccionCitaResponse

    // ── Financiero ────────────────────────────────
    @GET("Financiero/resumen")
    suspend fun getResumenFinanciero(
        @Query("fechaDesde") fechaDesde: String,
        @Query("fechaHasta") fechaHasta: String
    ): ResumenFinanciero

    @GET("Financiero/ingresos-dia")
    suspend fun getIngresosPorDia(
        @Query("fechaDesde") fechaDesde: String,
        @Query("fechaHasta") fechaHasta: String
    ): List<IngresoDia>

    @GET("Financiero/ingresos-mes")
    suspend fun getIngresosPorMes(
        @Query("anio") anio: Int
    ): List<IngresoMes>

    @GET("Financiero/servicios-vendidos")
    suspend fun getServiciosVendidos(
        @Query("fechaDesde") fechaDesde: String,
        @Query("fechaHasta") fechaHasta: String,
        @Query("top")        top: Int = 5
    ): List<ServicioVendido>

    @GET("Financiero/profesionales-ranking")
    suspend fun getProfesionalesRanking(
        @Query("fechaDesde") fechaDesde: String,
        @Query("fechaHasta") fechaHasta: String,
        @Query("top")        top: Int = 5
    ): List<ProfesionalRanking>

    // ── Licencia ──────────────────────────────────
    @GET("Licencia/validar")
    suspend fun validarLicencia(): LicenciaResponse

    // Sin token — endpoint público
    @GET("Contacto")
    suspend fun getContactoSoporte(): ContactoSoporte

    // ── Tema ──────────────────────────────────────
    @GET("Tema")
    suspend fun getTemaEmpresa(): TemaEmpresa

    // ── Notificaciones ────────────────────────────
    @POST("Notificaciones/registrar-token")
    suspend fun registrarFcmToken(@Body request: FcmTokenRequest)

    // ── Clientes ──────────────────────────────────────
    @GET("Clientes/buscar")
    suspend fun buscarClientes(
        @Query("busqueda") busqueda: String
    ): List<Cliente>

    @POST("Clientes")
    suspend fun crearCliente(@Body request: CrearClienteRequest): Cliente

    @GET("Clientes/{id}")
    suspend fun obtenerCliente(@Path("id") id: Int): Cliente

    @PUT("Clientes/{id}")
    suspend fun actualizarCliente(
        @Path("id") id: Int,
        @Body request: ActualizarClienteRequest
    )

    @DELETE("Clientes/{id}")
    suspend fun desactivarCliente(@Path("id") id: Int)

    // ── Perfil ──────────────────────────────────────
    @GET("Perfil")
    suspend fun obtenerPerfil(): Response<Perfil>

    @PUT("Perfil")
    suspend fun actualizarPerfil(@Body request: ActualizarPerfilRequest): Response<Unit>

    @Multipart
    @POST("Perfil/foto")
    suspend fun subirFoto(
        @Part foto: MultipartBody.Part
    ): Response<FotoResponse>

    // Reagendamiento
    @GET("Reagendamiento/pendientes")
    suspend fun getPendientesReagendamiento(): List<CitaPendiente>

    @PUT("Reagendamiento/{id}/aprobar")
    suspend fun aprobarReagendamiento(
        @Path("id") id: Int,
        @Body request: AprobarReagendamientoRequest
    )

    @PUT("Reagendamiento/{id}/rechazar")
    suspend fun rechazarReagendamiento(
        @Path("id") id: Int,
        @Body request: RechazarReagendamientoRequest
    )
    // ── Admin Catálogos ───────────────────────────────
    @GET("categorias")
    suspend fun getCategorias(): List<CategoriaAdmin>

    @POST("categorias")
    suspend fun crearCategoria(@Body request: CategoriaRequest): IdResponse

    @PUT("categorias/{id}")
    suspend fun editarCategoria(
        @Path("id") id: Int,
        @Body request: CategoriaRequest
    )

    @PATCH("categorias/{id}/estado")
    suspend fun toggleEstadoCategoria(@Path("id") id: Int): EstadoResponse

    @GET("admin/servicios")
    suspend fun getServiciosAdmin(): List<ServicioAdmin>

    @POST("admin/servicios")
    suspend fun crearServicio(@Body request: ServicioRequest): IdResponse

    @PUT("admin/servicios/{id}")
    suspend fun editarServicio(
        @Path("id") id: Int,
        @Body request: ServicioRequest
    )

    @PATCH("admin/servicios/{id}/estado")
    suspend fun toggleEstadoServicio(@Path("id") id: Int): EstadoResponse
}