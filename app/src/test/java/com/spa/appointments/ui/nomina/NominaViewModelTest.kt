package com.spa.appointments.ui.nomina

import com.spa.appointments.domain.model.*
import com.spa.appointments.domain.repository.NominaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class NominaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeNominaRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeNominaRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel(): NominaViewModel {
        return NominaViewModel(fakeRepository, testDispatcher)
    }

    @Test
    fun testCargaInicial_ConPeriodos_SeleccionaPrimerPeriodoYCargaContratos() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.periodos.size)
        assertEquals(1, state.periodoSeleccionado?.id)
        assertEquals(1, state.contratos.size)
        assertFalse(state.cargandoPeriodos)
        assertFalse(state.cargandoContratos)
    }

    @Test
    fun testCargaInicial_SinPeriodos_MuestraEstadoVacio() = runTest(testDispatcher) {
        fakeRepository.periodosResult = Result.success(emptyList())

        val vm = crearViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.periodos.isEmpty())
        assertNull(state.periodoSeleccionado)
        assertTrue(state.contratos.isEmpty())
    }

    @Test
    fun testDebounce_VariasTeclas_SolaUnaConsultaAAlFin() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        fakeRepository.consultasContratosCount = 0

        vm.actualizarBusqueda("J")
        advanceTimeBy(100)
        vm.actualizarBusqueda("Ju")
        advanceTimeBy(100)
        vm.actualizarBusqueda("Juan")
        advanceTimeBy(400) // Se cumple el debounce

        advanceUntilIdle()

        assertEquals(1, fakeRepository.consultasContratosCount)
        assertEquals("Juan", fakeRepository.lastBusquedaRealizada)
    }

    @Test
    fun testPreliquidar_OK() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        val contrato = fakeRepository.contratoSinPreliquidar
        vm.preliquidar(contrato)
        advanceUntilIdle()

        assertEquals(1, fakeRepository.llamadasPreliquidar)
    }

    @Test
    fun testPreliquidar_RechazadoSiLiquidada() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        val contrato = fakeRepository.contratoLiquidado
        vm.preliquidar(contrato)
        advanceUntilIdle()

        assertEquals(0, fakeRepository.llamadasPreliquidar)
    }

    @Test
    fun testLiquidar_OK() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        val contrato = fakeRepository.contratoPreliquidadaLista
        vm.liquidar(contrato)
        advanceUntilIdle()

        assertEquals(1, fakeRepository.llamadasCrearDesdePreliquidacion)
    }

    @Test
    fun testLiquidar_ConErrorMinus410_EmiteMostrarRecalcularYRecarga() = runTest(testDispatcher) {
        fakeRepository.crearDesdePreliquidacionResult = Result.failure(
            NominaApiException(422, -410, "La preliquidación está desactualizada")
        )
        val vm = crearViewModel()
        advanceUntilIdle()

        fakeRepository.consultasContratosCount = 0
        val contrato = fakeRepository.contratoPreliquidadaLista

        vm.liquidar(contrato)
        advanceUntilIdle()

        val evento = vm.eventos.first()
        assertTrue(evento is NominaEvent.MostrarRecalcular)
        assertEquals(1, fakeRepository.consultasContratosCount) // Confirmar recarga
    }

    @Test
    fun testError409_EmiteMensajeConflictoYRecarga() = runTest(testDispatcher) {
        fakeRepository.crearDesdePreliquidacionResult = Result.failure(
            NominaApiException(409, null, "Ya existe liquidacion")
        )
        val vm = crearViewModel()
        advanceUntilIdle()

        fakeRepository.consultasContratosCount = 0
        val contrato = fakeRepository.contratoPreliquidadaLista

        vm.liquidar(contrato)
        advanceUntilIdle()

        val evento = vm.eventos.first()
        assertTrue(evento is NominaEvent.MostrarMensaje)
        assertEquals("Ya existe liquidacion", (evento as NominaEvent.MostrarMensaje).mensaje)
        assertEquals(1, fakeRepository.consultasContratosCount)
    }

    @Test
    fun testError404_EmiteMensajeYRecarga() = runTest(testDispatcher) {
        fakeRepository.crearDesdePreliquidacionResult = Result.failure(
            NominaApiException(404, null, "Recurso no encontrado")
        )
        val vm = crearViewModel()
        advanceUntilIdle()

        fakeRepository.consultasContratosCount = 0
        val contrato = fakeRepository.contratoPreliquidadaLista

        vm.liquidar(contrato)
        advanceUntilIdle()

        val evento = vm.eventos.first()
        assertTrue(evento is NominaEvent.MostrarMensaje)
        assertEquals(1, fakeRepository.consultasContratosCount)
    }

    @Test
    fun testReintento503_Exitoso() = runTest(testDispatcher) {
        fakeRepository.fallar503Count = 1
        val vm = crearViewModel()
        advanceUntilIdle()

        val contrato = fakeRepository.contratoSinPreliquidar
        vm.preliquidar(contrato)
        advanceUntilIdle()

        assertEquals(2, fakeRepository.llamadasPreliquidar) // Intentado + 1 reintento exitoso
    }

    @Test
    fun testReintento503_DobleFalla_MuestraServidorOcupado() = runTest(testDispatcher) {
        fakeRepository.fallar503Count = 2
        val vm = crearViewModel()
        advanceUntilIdle()

        val contrato = fakeRepository.contratoSinPreliquidar
        vm.preliquidar(contrato)
        advanceUntilIdle()

        assertEquals(2, fakeRepository.llamadasPreliquidar)
        val evento = vm.eventos.first()
        assertTrue(evento is NominaEvent.MostrarMensaje)
        assertTrue((evento as NominaEvent.MostrarMensaje).mensaje.contains("servidor está ocupado"))
    }

    @Test
    fun testAnular_ConMotivoVacio_RechazaYNoLlamaRepositorio() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.anularLiquidacion(1, "   ")
        advanceUntilIdle()

        assertEquals(0, fakeRepository.llamadasAnular)
    }

    @Test
    fun testAnular_OK_Y_DobleToque() = runTest(testDispatcher) {
        fakeRepository.retardoAnularMs = 1000L
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.anularLiquidacion(5, "Motivo válido de anulación")
        vm.anularLiquidacion(5, "Segundo toque")

        advanceUntilIdle()

        assertEquals(1, fakeRepository.llamadasAnular)
    }

    @Test
    fun testDobleToque_DescargarDesprendible_IgnoraSegundaLlamada() = runTest(testDispatcher) {
        fakeRepository.retardoDescargarDesprendibleMs = 1000L
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.descargarDesprendible(10)
        vm.descargarDesprendible(10) // Segundo toque debe ignorarse

        advanceUntilIdle()

        assertEquals(1, fakeRepository.llamadasDescargarDesprendible)
    }

    @Test
    fun testRespuestaObsoletaAlCambiarDePeriodo_DescartaRespuestaAnterior() = runTest(testDispatcher) {
        fakeRepository.retardoContratosPeriodo1Ms = 1000L
        fakeRepository.retardoContratosPeriodo2Ms = 100L

        val vm = crearViewModel()
        advanceTimeBy(50) // En proceso de cargar periodo 1

        val periodo2 = PeriodoNominaItem(2, "Quincena 2", "2026-10-16", "2026-10-31", 1, "Abierto")
        vm.seleccionarPeriodo(periodo2)

        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.periodoSeleccionado?.id)
        assertEquals(2, fakeRepository.lastPeriodoNominaIdBuscado)
    }

    @Test
    fun testEntregaEventosEmitidosSinColector_ChannelConservaEvento() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        // Emite evento cuando NO hay ningun collect activo
        vm.descargarDesprendible(10)
        advanceUntilIdle()

        // Luego se suscribe y el evento se recibe intacto
        val evento = vm.eventos.first()
        assertTrue(evento is NominaEvent.AbrirPdfDesprendible)
    }

    @Test
    fun testCargaCanceladaPorSiguiente_NoEmiteEventosNiDejaCargandoFalso() = runTest(testDispatcher) {
        fakeRepository.retardoContratosPeriodo1Ms = 500L
        fakeRepository.retardoContratosPeriodo2Ms = 100L

        val vm = crearViewModel()
        advanceTimeBy(100) // Carga inicial en progreso

        val periodo2 = PeriodoNominaItem(2, "Quincena 2", "2026-10-16", "2026-10-31", 1, "Abierto")
        vm.seleccionarPeriodo(periodo2) // Cancela la primera carga

        advanceTimeBy(50) // La segunda carga sigue en curso
        assertTrue(vm.uiState.value.cargandoContratos) // No debe quedar en false prematuramente

        advanceUntilIdle() // Completa la segunda carga
        assertFalse(vm.uiState.value.cargandoContratos)
    }

    @Test
    fun testCargarConceptos_Exito() = runTest(testDispatcher) {
        val vm = crearViewModel()
        vm.cargarConceptosSiEsNecesario()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.conceptos.size)
        assertEquals("Horas Extra", vm.uiState.value.conceptos[0].nombre)
    }

    @Test
    fun testCargarNovedades_FiltraPorFechasDePeriodo() = runTest(testDispatcher) {
        fakeRepository.novedadesLista = listOf(
            NovedadNominaResponse(1, 1, 1, 1, "Devengado", "2026-10-05", 50000.0, null, null, true, null),
            NovedadNominaResponse(2, 1, 1, 1, "Devengado", "2026-10-20", 80000.0, null, null, true, null)
        )

        val vm = crearViewModel()
        advanceUntilIdle()

        val contrato = fakeRepository.contratoSinPreliquidar
        vm.abrirNovedadesContrato(contrato)
        advanceUntilIdle()

        val novedades = vm.uiState.value.novedadesContrato
        assertEquals(1, novedades.size)
        assertEquals(1, novedades[0].id)
    }

    @Test
    fun testGuardarNovedad_ValidacionDiasObligatorios() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.abrirNovedadesContrato(fakeRepository.contratoSinPreliquidar)
        advanceUntilIdle()

        vm.guardarNovedad(
            idConcepto = 2,
            fechaNovedad = "2026-10-05",
            valorStr = "50000",
            diasStr = "0",
            observacionesStr = ""
        )
        advanceUntilIdle()

        val evento = vm.eventos.first()
        assertTrue(evento is NominaEvent.MostrarMensaje)
        assertTrue((evento as NominaEvent.MostrarMensaje).mensaje.contains("días"))
        assertEquals(0, fakeRepository.llamadasCrearNovedad)
    }

    @Test
    fun testGuardarNovedad_BloqueadoSiPeriodoNoAbierto() = runTest(testDispatcher) {
        fakeRepository.periodosResult = Result.success(
            listOf(PeriodoNominaItem(1, "Periodo Cerrado", "2026-10-01", "2026-10-15", 2, "Cerrado"))
        )
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.abrirNovedadesContrato(fakeRepository.contratoSinPreliquidar)
        advanceUntilIdle()

        vm.guardarNovedad(
            idConcepto = 1,
            fechaNovedad = "2026-10-05",
            valorStr = "50000",
            diasStr = "",
            observacionesStr = ""
        )
        advanceUntilIdle()

        val evento = vm.eventos.first()
        assertTrue(evento is NominaEvent.MostrarMensaje)
        assertEquals(NominaMensajes.ERROR_PERIODO_NO_ABIERTO, (evento as NominaEvent.MostrarMensaje).mensaje)
        assertEquals(0, fakeRepository.llamadasCrearNovedad)
    }

    @Test
    fun testGuardarNovedad_Exito_Creacion_AutoRecalculaPreliquidacion() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        val preliquidarCountBefore = fakeRepository.llamadasPreliquidar

        vm.abrirNovedadesContrato(fakeRepository.contratoSinPreliquidar)
        advanceUntilIdle()

        vm.guardarNovedad(
            idConcepto = 1,
            fechaNovedad = "2026-10-05",
            valorStr = "150000",
            diasStr = "",
            observacionesStr = "Bono"
        )
        advanceUntilIdle()

        assertEquals(1, fakeRepository.llamadasCrearNovedad)
        assertEquals(preliquidarCountBefore + 1, fakeRepository.llamadasPreliquidar)
    }

    @Test
    fun testAnularNovedad_Exito_AutoRecalculaPreliquidacion() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.abrirNovedadesContrato(fakeRepository.contratoSinPreliquidar)
        advanceUntilIdle()

        val preliquidarCountBefore = fakeRepository.llamadasPreliquidar
        val novedad = NovedadNominaResponse(10, 1, 1, 1, "Horas Extra", "2026-10-05", 50000.0, null, null, true, null)

        vm.anularNovedad(novedad)
        advanceUntilIdle()

        assertEquals(1, fakeRepository.llamadasCambiarEstadoNovedad)
        assertEquals(preliquidarCountBefore + 1, fakeRepository.llamadasPreliquidar)
    }

    @Test
    fun testCompartirDesprendible_Exito_EmiteEventoCompartirPdf() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.compartirDesprendible(5)
        advanceUntilIdle()

        val evento = vm.eventos.first()
        assertTrue(evento is NominaEvent.CompartirPdfDesprendible)
    }

    @Test
    fun testCrearPeriodoNomina_Exito_RefrescaYSeleccionaNuevoPeriodo() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        fakeRepository.periodosResult = Result.success(
            listOf(
                PeriodoNominaItem(1, "Quincena 1", "2026-10-01", "2026-10-15", 1, "Abierto"),
                PeriodoNominaItem(99, "Quincena Noviembre", "2026-11-01", "2026-11-15", 1, "Abierto")
            )
        )

        vm.crearPeriodoNomina("Quincena Noviembre", "2026-11-01", "2026-11-15")
        advanceUntilIdle()

        assertEquals(1, fakeRepository.llamadasCrearPeriodo)
        assertEquals(99, vm.uiState.value.periodoSeleccionado?.id)
    }
}

class FakeNominaRepository : NominaRepository {
    var llamadasPreliquidar = 0
    var llamadasCrearDesdePreliquidacion = 0
    var llamadasAnular = 0
    var llamadasDescargarDesprendible = 0
    var consultasContratosCount = 0
    var lastPeriodoNominaIdBuscado: Int? = null
    var lastBusquedaRealizada: String? = null

    var retardoPreliquidarMs = 0L
    var retardoAnularMs = 0L
    var retardoDescargarDesprendibleMs = 0L
    var retardoContratosPeriodo1Ms = 0L
    var retardoContratosPeriodo2Ms = 0L
    var fallar503Count = 0

    val contratoSinPreliquidar = ContratoElegibleNomina(
        idContratoLaboral = 1,
        idProfesional = 2,
        profesionalNombre = "Juan",
        profesionalDocumento = "123",
        cargo = "Cargo",
        idSede = 1,
        tipoContrato = "Indefinido",
        salarioBase = 1000000.0,
        fechaInicio = "2026-01-01",
        fechaFin = null,
        idPreliquidacionVigente = null,
        netoPreliquidado = null,
        motivoPreliquidacion = null,
        idLiquidacionVigente = null,
        netoLiquidado = null
    )

    val contratoPreliquidadaLista = ContratoElegibleNomina(
        idContratoLaboral = 1,
        idProfesional = 2,
        profesionalNombre = "Juan",
        profesionalDocumento = "123",
        cargo = "Cargo",
        idSede = 1,
        tipoContrato = "Indefinido",
        salarioBase = 1000000.0,
        fechaInicio = "2026-01-01",
        fechaFin = null,
        idPreliquidacionVigente = 10,
        netoPreliquidado = 900000.0,
        motivoPreliquidacion = null,
        idLiquidacionVigente = null,
        netoLiquidado = null
    )

    val contratoLiquidado = ContratoElegibleNomina(
        idContratoLaboral = 1,
        idProfesional = 2,
        profesionalNombre = "Juan",
        profesionalDocumento = "123",
        cargo = "Cargo",
        idSede = 1,
        tipoContrato = "Indefinido",
        salarioBase = 1000000.0,
        fechaInicio = "2026-01-01",
        fechaFin = null,
        idPreliquidacionVigente = 10,
        netoPreliquidado = 900000.0,
        motivoPreliquidacion = null,
        idLiquidacionVigente = 5,
        netoLiquidado = 900000.0
    )

    var periodosResult: Result<List<PeriodoNominaItem>> = Result.success(
        listOf(
            PeriodoNominaItem(1, "Quincena 1", "2026-10-01", "2026-10-15", 1, "Abierto"),
            PeriodoNominaItem(2, "Quincena 2", "2026-10-16", "2026-10-31", 1, "Abierto")
        )
    )

    var crearDesdePreliquidacionResult: Result<RespuestaSimpleNomina> = Result.success(
        RespuestaSimpleNomina(id = 100, mensaje = "OK")
    )

    override suspend fun obtenerLiquidacionPorId(id: Int): Result<LiquidacionNominaResponse> {
        return Result.failure(NotImplementedError())
    }

    override suspend fun descargarDesprendiblePdf(id: Int): Result<File> {
        llamadasDescargarDesprendible++
        if (retardoDescargarDesprendibleMs > 0) delay(retardoDescargarDesprendibleMs)
        return Result.success(File.createTempFile("test_", ".pdf"))
    }

    override suspend fun preliquidar(request: PreliquidarContratoRequest): Result<PreliquidacionResponse> {
        llamadasPreliquidar++
        if (fallar503Count > 0) {
            fallar503Count--
            return Result.failure(NominaApiException(503, -503, "Servidor ocupado (AppLock)", retryAfterSegundos = 1))
        }
        if (retardoPreliquidarMs > 0) delay(retardoPreliquidarMs)
        return Result.success(
            PreliquidacionResponse(
                idPreliquidacion = 10,
                idContratoLaboral = request.idContratoLaboral,
                idPeriodoNomina = request.idPeriodoNomina,
                totalDevengado = 1000000.0,
                totalDeducciones = 100000.0,
                netoPagar = 900000.0,
                salarioBaseSnapshot = 1000000.0,
                smmlvSnapshot = 1423500.0,
                auxTransporteSnapshot = 200000.0,
                ibcCalculado = 1000000.0,
                mensaje = "OK"
            )
        )
    }

    override suspend fun crearDesdePreliquidacion(request: CrearLiquidacionDesdePreliquidacionRequest): Result<RespuestaSimpleNomina> {
        llamadasCrearDesdePreliquidacion++
        return crearDesdePreliquidacionResult
    }

    override suspend fun anularLiquidacion(id: Int, request: AnularLiquidacionRequest): Result<RespuestaSimpleNomina> {
        llamadasAnular++
        if (retardoAnularMs > 0) delay(retardoAnularMs)
        return Result.success(RespuestaSimpleNomina(id = id, mensaje = "OK"))
    }

    override suspend fun listarPeriodosNomina(soloAbiertos: Boolean): Result<List<PeriodoNominaItem>> {
        return periodosResult
    }

    override suspend fun listarContratosElegiblesNomina(
        idPeriodoNomina: Int,
        idSede: Int?,
        busqueda: String?
    ): Result<List<ContratoElegibleNomina>> {
        consultasContratosCount++
        lastPeriodoNominaIdBuscado = idPeriodoNomina
        lastBusquedaRealizada = busqueda

        if (idPeriodoNomina == 1 && retardoContratosPeriodo1Ms > 0) delay(retardoContratosPeriodo1Ms)
        if (idPeriodoNomina == 2 && retardoContratosPeriodo2Ms > 0) delay(retardoContratosPeriodo2Ms)

        return Result.success(listOf(contratoSinPreliquidar))
    }

    override suspend fun listarPreliquidacionesVigentesNomina(idPeriodoNomina: Int): Result<List<PreliquidacionVigenteItem>> {
        return Result.success(emptyList())
    }

    override suspend fun obtenerPreliquidacionPorId(id: Int): Result<PreliquidacionResponse> {
        return Result.success(
            PreliquidacionResponse(
                idPreliquidacion = id,
                idContratoLaboral = 1,
                idPeriodoNomina = 1,
                totalDevengado = 1000000.0,
                totalDeducciones = 100000.0,
                netoPagar = 900000.0,
                salarioBaseSnapshot = 1000000.0,
                smmlvSnapshot = 1423500.0,
                auxTransporteSnapshot = 200000.0,
                ibcCalculado = 1000000.0,
                mensaje = "OK"
            )
        )
    }

    override suspend fun listarLiquidacionesNomina(
        idPeriodoNomina: Int?,
        idContratoLaboral: Int?,
        estado: Int?,
        pagina: Int,
        tamanoPagina: Int
    ): Result<PaginaLiquidaciones> {
        return Result.success(PaginaLiquidaciones(emptyList(), 0, pagina, tamanoPagina))
    }

    var llamadasCrearPeriodo = 0
    override suspend fun crearPeriodoNomina(request: CrearPeriodoNominaRequest): Result<RespuestaSimpleNomina> {
        llamadasCrearPeriodo++
        return Result.success(RespuestaSimpleNomina(id = 99, mensaje = "Período creado"))
    }

    var novedadesLista: List<NovedadNominaResponse> = emptyList()
    var conceptosLista: List<ConceptoResponse> = listOf(
        ConceptoResponse(1, "DEV01", "Horas Extra", 1, aplicaSalarioBase = false, descuentaDiasSueldo = false),
        ConceptoResponse(2, "DED01", "Ausencia", 2, aplicaSalarioBase = false, descuentaDiasSueldo = true)
    )
    var llamadasListarNovedades = 0
    var llamadasCrearNovedad = 0
    var llamadasActualizarNovedad = 0
    var llamadasCambiarEstadoNovedad = 0
    var llamadasListarConceptos = 0

    override suspend fun listarNovedadesNomina(idContratoLaboral: Int): Result<List<NovedadNominaResponse>> {
        llamadasListarNovedades++
        return Result.success(novedadesLista)
    }

    override suspend fun crearNovedadNomina(request: CrearNovedadNominaRequest): Result<RespuestaSimpleNomina> {
        llamadasCrearNovedad++
        return Result.success(RespuestaSimpleNomina(id = 1, mensaje = "Novedad creada"))
    }

    override suspend fun actualizarNovedadNomina(id: Int, request: ActualizarNovedadNominaRequest): Result<RespuestaSimpleNomina> {
        llamadasActualizarNovedad++
        return Result.success(RespuestaSimpleNomina(id = id, mensaje = "Novedad actualizada"))
    }

    override suspend fun cambiarEstadoNovedadNomina(id: Int, nuevoEstado: Boolean): Result<RespuestaSimpleNomina> {
        llamadasCambiarEstadoNovedad++
        return Result.success(RespuestaSimpleNomina(id = id, mensaje = "Estado cambiado"))
    }

    override suspend fun listarConceptosNomina(): Result<List<ConceptoResponse>> {
        llamadasListarConceptos++
        return Result.success(conceptosLista)
    }
}
