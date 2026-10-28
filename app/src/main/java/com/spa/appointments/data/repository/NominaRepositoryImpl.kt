package com.spa.appointments.data.repository

import android.content.Context
import com.spa.appointments.core.utils.runCatchingSuspend
import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.data.remote.NominaErrorMapper
import com.spa.appointments.domain.model.*
import com.spa.appointments.domain.repository.NominaRepository
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NominaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val moshi: Moshi
) : NominaRepository {

    override suspend fun obtenerLiquidacionPorId(id: Int): Result<LiquidacionNominaResponse> = runCatchingSuspend {
        val response = apiService.obtenerLiquidacionNominaPorId(id)
        procesarRespuesta(response)
    }

    override suspend fun preliquidar(request: PreliquidarContratoRequest): Result<PreliquidacionResponse> = runCatchingSuspend {
        val response = apiService.preliquidarNomina(request)
        procesarRespuesta(response)
    }

    override suspend fun crearDesdePreliquidacion(
        request: CrearLiquidacionDesdePreliquidacionRequest
    ): Result<RespuestaSimpleNomina> = runCatchingSuspend {
        val response = apiService.crearLiquidacionDesdePreliquidacion(request)
        procesarRespuesta(response)
    }

    override suspend fun anularLiquidacion(
        id: Int,
        request: AnularLiquidacionRequest
    ): Result<RespuestaSimpleNomina> = runCatchingSuspend {
        val response = apiService.anularLiquidacionNomina(id, request)
        procesarRespuesta(response)
    }

    override suspend fun descargarDesprendiblePdf(id: Int): Result<File> = withContext(Dispatchers.IO) {
        runCatchingSuspend {
            val response = apiService.descargarDesprendiblePdf(id)
            if (!response.isSuccessful) {
                throw NominaErrorMapper.parsearError(response, moshi)
            }
            val body = response.body() ?: throw NominaApiException(response.code(), null, "Respuesta vacía del servidor")

            val pdfDir = File(context.cacheDir, "pdf").also { if (!it.exists()) it.mkdirs() }
            val cdHeader = response.headers()["Content-Disposition"]
            val rawExtracted = extraerNombreArchivo(cdHeader)
            val safeName = sanitizarNombre(rawExtracted, id)

            val tempFile = File(pdfDir, "$safeName.tmp")
            val finalFile = File(pdfDir, safeName)

            try {
                body.byteStream().use { inputStream ->
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                if (finalFile.exists()) {
                    finalFile.delete()
                }

                val renamed = tempFile.renameTo(finalFile)
                if (!renamed) {
                    if (tempFile.exists()) tempFile.delete()
                    throw NominaApiException(response.code(), null, "Error al guardar el archivo PDF")
                }

                // Borrar los demás archivos en pdfDir excepto finalFile
                pdfDir.listFiles()?.forEach { file ->
                    if (file.absolutePath != finalFile.absolutePath) {
                        file.delete()
                    }
                }

                finalFile
            } catch (e: Exception) {
                if (tempFile.exists()) tempFile.delete()
                throw e
            }
        }
    }

    private fun <T> procesarRespuesta(response: Response<T>): T {
        if (response.isSuccessful) {
            return response.body() ?: throw NominaApiException(response.code(), null, "Cuerpo de respuesta nulo")
        }
        throw NominaErrorMapper.parsearError(response, moshi)
    }

    private fun sanitizarNombre(extractedName: String?, id: Int): String {
        if (extractedName.isNullOrBlank()) return "Desprendible_Nomina_$id.pdf"
        val baseName = File(extractedName).name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        if (baseName.isBlank()) return "Desprendible_Nomina_$id.pdf"
        return if (baseName.endsWith(".pdf", ignoreCase = true)) baseName else "$baseName.pdf"
    }

    private fun extraerNombreArchivo(contentDisposition: String?): String? {
        if (contentDisposition.isNullOrBlank()) return null
        val match = Regex("""filename\*?=['"]?(?:UTF-8'')?([^;'"\r\n]+)['"]?""", RegexOption.IGNORE_CASE)
            .find(contentDisposition)
        return match?.groupValues?.get(1)
    }
}
