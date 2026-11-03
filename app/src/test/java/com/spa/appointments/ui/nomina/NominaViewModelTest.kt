package com.spa.appointments.ui.nomina

import com.spa.appointments.domain.model.*
import com.spa.appointments.domain.repository.NominaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class NominaViewModelTest {

    private lateinit var fakeRepository: FakeNominaRepository

    @Before
    fun setup() {
        fakeRepository = FakeNominaRepository()
    }

    private fun crearViewModel(): NominaViewModel {
        return NominaViewModel(fakeRepository, Dispatchers.Unconfined)
    }

    @Test
    fun testCargaInicial_ConPeriodos_SeleccionaPrimerPeriodoYCargaContratos() = runBlocking {
        val vm = crearViewModel()

        val state = vm.uiState.value
        assertEquals(1, state.periodos.size)
        assertEquals(1, state.periodoSeleccionado?.id)
        assertEquals(1, state.contratos.size)
        assertFalse(state.cargandoPeriodos)
        assertFalse(state.cargandoContratos)
    }

    @Test
    fun testCargaInicial_SinPeriodos_MuestraEstadoVacio() = runBlocking {
        fakeRepository.periodosResult = Result.success(emptyList())

        val vm = crearViewModel()

        val state = vm.uiState.value
        assertTrue(state.periodos.isEmpty())
        assertNull(state.periodoSeleccionado)
        assertTrue(state.contratos.isEmpty())
    }

    @Test
    fun testCambioDePeriodo_RecargaContratos() = runBlocking {
        val vm = crearViewModel()

        val periodo2 = PeriodoNominaItem(2, "Quincena 2", "2026-10-16", "2026-10-31", 1, "Abierto")
        vm.seleccionarPeriodo(periodo2)

        assertEquals(2, vm.uiState.value.periodoSeleccionado?.id)
        assertEquals(2, fakeRepository.lastPeriodoNominaIdBuscado)
    }

    @Test
    fun testPreliquidar_OK() = runBlocking {
        val vm = crearViewModel()
        val contrato = fakeRepository.contratoSinPreliquidar

        vm.preliquidar(contrato)

        assertEquals(1, fakeRepository.llamadasPreliquidar)
    }

    @Test
    fun testPreliquidar_RechazadoSiLiquidada() = runBlocking {
        val vm = crearViewModel()
        val contrato = fakeRepository.contratoLiquidado

        vm.preliquidar(contrato)

        assertEquals(0, fakeRepository.llamadasPreliquidar)
    }

    @Test
    fun testLiquidar_OK() = runBlocking {
        val vm = crearViewModel()
        val contrato = fakeRepository.contratoPreliquidadaLista

        vm.liquidar(contrato)

        assertEquals(1, fakeRepository.llamadasCrearDesdePreliquidacion)
    }

    @Test
    fun testLiquidar_ConError410_EmiteMostrarRecalcular() = runBlocking {
        fakeRepository.crearDesdePreliquidacionResult = Result.failure(
            NominaApiException(422, -410, "La preliquidación está desactualizada")
        )
        val vm = crearViewModel()
        val contrato = fakeRepository.contratoPreliquidadaLista

        var eventoCapturado: NominaEvent? = null
        val job = launch(Dispatchers.Unconfined) {
            vm.eventos.collect { eventoCapturado = it }
        }

        vm.liquidar(contrato)

        assertTrue(eventoCapturado is NominaEvent.MostrarRecalcular)
        job.cancel()
    }

    @Test
    fun testAnular_ConMotivoVacio_RechazaYNoLlamaRepositorio() = runBlocking {
        val vm = crearViewModel()

        vm.anularLiquidacion(1, "   ")

        assertEquals(0, fakeRepository.llamadasAnular)
    }

    @Test
    fun testAnular_OK() = runBlocking {
        val vm = crearViewModel()

        vm.anularLiquidacion(1, "Motivo de prueba válido")

        assertEquals(1, fakeRepository.llamadasAnular)
    }

    @Test
    fun testDescargarDesprendible_OK() = runBlocking {
        val vm = crearViewModel()

        var eventoCapturado: NominaEvent? = null
        val job = launch(Dispatchers.Unconfined) {
            vm.eventos.collect { eventoCapturado = it }
        }

        vm.descargarDesprendible(1)

        assertTrue(eventoCapturado is NominaEvent.AbrirPdfDesprendible)
        job.cancel()
    }

    @Test
    fun testDobleToque_IgnoraSegundaAccionEnCurso() = runBlocking {
        fakeRepository.retardoPreliquidarMs = 1000L
        val vm = crearViewModel()
        val contrato = fakeRepository.contratoSinPreliquidar

        vm.preliquidar(contrato)
        vm.preliquidar(contrato) // Segundo toque debe ignorarse

        assertEquals(1, fakeRepository.llamadasPreliquidar)
    }
}

class FakeNominaRepository : NominaRepository {
    var llamadasPreliquidar = 0
    var llamadasCrearDesdePreliquidacion = 0
    var llamadasAnular = 0
    var lastPeriodoNominaIdBuscado: Int? = null
    var retardoPreliquidarMs = 0L

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
        listOf(PeriodoNominaItem(1, "Quincena 1", "2026-10-01", "2026-10-15", 1, "Abierto"))
    )

    var crearDesdePreliquidacionResult: Result<RespuestaSimpleNomina> = Result.success(
        RespuestaSimpleNomina(id = 100, mensaje = "OK")
    )

    override suspend fun obtenerLiquidacionPorId(id: Int): Result<LiquidacionNominaResponse> {
        return Result.failure(NotImplementedError())
    }

    override suspend fun descargarDesprendiblePdf(id: Int): Result<File> {
        return Result.success(File.createTempFile("test_", ".pdf"))
    }

    override suspend fun preliquidar(request: PreliquidarContratoRequest): Result<PreliquidacionResponse> {
        llamadasPreliquidar++
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
        lastPeriodoNominaIdBuscado = idPeriodoNomina
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
}
