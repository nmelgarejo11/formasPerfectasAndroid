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
        @Query("idSede")      idSede: Int? = null,
        @Query("idServicio")  idServicio: Int? = null
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
    suspend fun getCitasActivas(
        @Query("nombreCliente")  nombreCliente:  String? = null,
        @Query("fechaDesde")     fechaDesde:     String? = null,
        @Query("fechaHasta")     fechaHasta:     String? = null,
        @Query("idProfesional")  idProfesional:  Int?    = null,
        @Query("idEstado")       idEstado:       Int?    = null
    ): List<Cita>

    @GET("Citas/historial")
    suspend fun getCitasHistorial(
        @Query("nombreCliente")  nombreCliente:  String? = null,
        @Query("fechaDesde")     fechaDesde:     String? = null,
        @Query("fechaHasta")     fechaHasta:     String? = null,
        @Query("idProfesional")  idProfesional:  Int?    = null,
        @Query("idEstado")       idEstado:       Int?    = null
    ): List<Cita>

    @PUT("Citas/cancelar")
    suspend fun cancelarCita(@Body request: AccionCitaCancelarRequest): AccionCitaResponse

    @PUT("Citas/reagendar")
    suspend fun reagendarCita(@Body request: AccionCitaReagendarRequest): AccionCitaResponse

    @GET("Citas/estados")
    suspend fun getEstadosCita(
        @Query("grupo") grupo: String? = null
    ): List<EstadoCita>

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

    // ── Admin Profesionales ───────────────────────────────────
    @GET("admin/profesionales")
    suspend fun getProfesionalesAdmin(): List<ProfesionalAdmin>

    @POST("admin/profesionales")
    suspend fun crearProfesional(@Body request: ProfesionalRequest): IdResponse

    @PUT("admin/profesionales/{id}")
    suspend fun editarProfesional(
        @Path("id") id: Int,
        @Body request: ProfesionalRequest
    )

    @PATCH("admin/profesionales/{id}/estado")
    suspend fun toggleEstadoProfesional(@Path("id") id: Int): EstadoResponse

    @Multipart
    @POST("admin/profesionales/{id}/foto")
    suspend fun subirFotoProfesional(
        @Path("id") id: Int,
        @Part foto: MultipartBody.Part
    ): FotoResponse

    @GET("admin/profesionales/cargos")
    suspend fun getCargos(): List<CargoAdmin>

    @GET("admin/profesionales/sedes")
    suspend fun getSedesEmpresa(): List<SedeAdmin>

    @GET("admin/profesionales/{id}/sedes")
    suspend fun getSedesProfesional(@Path("id") id: Int): List<Int>

    @PUT("admin/profesionales/{id}/sedes")
    suspend fun guardarSedesProfesional(
        @Path("id") id: Int,
        @Body request: GuardarSedesRequest
    )

    @GET("admin/profesionales/{id}/servicios")
    suspend fun getServiciosProfesional(@Path("id") id: Int): List<ServicioProfesionalResponse>

    @PUT("admin/profesionales/{id}/servicios")
    suspend fun guardarServiciosProfesional(
        @Path("id") id: Int,
        @Body request: GuardarServiciosRequest
    )

    // ── Admin Horarios ────────────────────────────────────────
    @GET("admin/horarios/{idProfesional}")
    suspend fun getHorario(@Path("idProfesional") idProfesional: Int): List<HorarioItem>

    @PUT("admin/horarios/{idProfesional}")
    suspend fun guardarHorario(
        @Path("idProfesional") idProfesional: Int,
        @Body request: GuardarHorarioRequest
    )

    @POST("admin/horarios/{idProfesional}/copiar")
    suspend fun copiarHorario(
        @Path("idProfesional") idProfesional: Int,
        @Body request: CopiarHorarioRequest
    )

    @GET("admin/horarios/{idProfesional}/bloqueos")
    suspend fun getBloqueos(@Path("idProfesional") idProfesional: Int): List<BloqueoResponse>

    @POST("admin/horarios/{idProfesional}/bloqueos")
    suspend fun crearBloqueo(
        @Path("idProfesional") idProfesional: Int,
        @Body request: BloqueoRequest
    )

    @DELETE("admin/horarios/{idProfesional}/bloqueos/{id}")
    suspend fun eliminarBloqueo(
        @Path("idProfesional") idProfesional: Int,
        @Path("id") id: Int
    )

    @GET("Citas/metodos-pago")
    suspend fun getMetodosPago(): List<MetodoPago>

    @PUT("Citas/finalizar")
    suspend fun finalizarCita(@Body request: FinalizarCitaRequest): FinalizarCitaResponse

    @GET("Citas/{idCita}/whatsapp")
    suspend fun getWhatsAppCita(
        @Path("idCita") idCita: Int
    ): Response<WhatsAppCitaResponse>


    // Gastos
    @POST("financiero/gastos")
    suspend fun registrarGasto(@Body request: GastoRequest): Response<GastoResultado>

    @GET("financiero/gastos")
    suspend fun listarGastos(
        @Query("idSede") idSede: Int?,
        @Query("fechaDesde") fechaDesde: String?,
        @Query("fechaHasta") fechaHasta: String?
    ): Response<List<GastoResponse>>

    @DELETE("financiero/gastos/{id}")
    suspend fun eliminarGasto(@Path("id") id: Int): Response<GastoResultado>

    @GET("admin/profesionales/sedes")
    suspend fun getSedes(): Response<List<Sede>>

    @GET("financiero/cierre-caja")
    suspend fun getCierreCaja(
        @Query("idSede") idSede: Int,
        @Query("fecha")  fecha : String? = null
    ): CierreCajaResponse

    @GET("financiero/ingresos-vs-gastos")
    suspend fun getIngresosVsGastos(
        @Query("idSede")      idSede      : Int,
        @Query("fechaInicio") fechaInicio : String,
        @Query("fechaFin")    fechaFin    : String
    ): IngresosVsGastosResponse

    @GET("api/tema")
    suspend fun getTema(): Response<TemaEmpresa>

    @PUT("api/tema")
    suspend fun actualizarTema(@Body request: TemaEmpresaRequest): Response<Unit>

}