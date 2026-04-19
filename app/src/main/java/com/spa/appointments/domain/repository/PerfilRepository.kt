package com.spa.appointments.domain.repository

import android.content.Context
import android.net.Uri
import com.spa.appointments.domain.model.ActualizarPerfilRequest
import com.spa.appointments.domain.model.Perfil

interface PerfilRepository {
    suspend fun obtenerPerfil(): Result<Perfil>
    suspend fun actualizarPerfil(request: ActualizarPerfilRequest): Result<Unit>
    suspend fun subirFoto(uri: Uri, context: Context): Result<String>
}