package com.spa.appointments.data.repository

import android.content.Context
import android.net.Uri
import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.ActualizarPerfilRequest
import com.spa.appointments.domain.model.Perfil
import com.spa.appointments.domain.repository.PerfilRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject

class PerfilRepositoryImpl @Inject constructor(
    private val api: ApiService
) : PerfilRepository {

    override suspend fun obtenerPerfil(): Result<Perfil> = runCatching {
        val response = api.obtenerPerfil()
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        response.body() ?: error("Perfil no encontrado")
    }

    override suspend fun actualizarPerfil(request: ActualizarPerfilRequest): Result<Unit> =
        runCatching {
            val response = api.actualizarPerfil(request)
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
        }

    override suspend fun subirFoto(uri: Uri, context: Context): Result<String> = runCatching {
        val stream = context.contentResolver.openInputStream(uri)
            ?: error("No se pudo abrir la imagen")
        val bytes    = stream.readBytes()
        stream.close()

        val mimeType    = context.contentResolver.getType(uri) ?: "image/jpeg"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part        = MultipartBody.Part.createFormData("foto", "foto.jpg", requestBody)

        val response = api.subirFoto(part)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        response.body()?.fotoUrl ?: error("No se recibió URL de foto")
    }
}