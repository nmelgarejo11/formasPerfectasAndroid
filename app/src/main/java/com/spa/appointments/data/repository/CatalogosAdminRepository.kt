package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.*
import javax.inject.Inject

class CatalogosAdminRepository @Inject constructor(
    private val api: ApiService
) {
    // ─── Categorías ───────────────────────────────────────
    suspend fun getCategorias(): List<CategoriaAdmin> =
        api.getCategorias()

    suspend fun crearCategoria(nombre: String, icono: String?): Int =
        api.crearCategoria(CategoriaRequest(nombre, icono)).id

    suspend fun editarCategoria(id: Int, nombre: String, icono: String?) =
        api.editarCategoria(id, CategoriaRequest(nombre, icono))

    suspend fun toggleEstadoCategoria(id: Int): Boolean =
        api.toggleEstadoCategoria(id).estado

    // ─── Servicios ────────────────────────────────────────
    suspend fun getServicios(): List<ServicioAdmin> =
        api.getServiciosAdmin()

    suspend fun crearServicio(req: ServicioRequest): Int =
        api.crearServicio(req).id

    suspend fun editarServicio(id: Int, req: ServicioRequest) =
        api.editarServicio(id, req)

    suspend fun toggleEstadoServicio(id: Int): Boolean =
        api.toggleEstadoServicio(id).estado
}