package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.ActualizarDetalleRequest
import com.spa.appointments.domain.model.ActualizarMetodoPagoRequest
import com.spa.appointments.domain.model.CrearDetalleRequest
import com.spa.appointments.domain.model.CrearMetodoPagoRequest
import com.spa.appointments.domain.repository.MetodoPagoRepository

class MetodoPagoRepositoryImpl(private val api: ApiService) : MetodoPagoRepository {

    override suspend fun listar() = runCatching {
        api.listarMetodosPago().body()!!
    }

    override suspend fun obtener(id: Int) = runCatching {
        api.obtenerMetodoPago(id).body()!!
    }

    override suspend fun crear(nombre: String) = runCatching {
        api.crearMetodoPago(CrearMetodoPagoRequest(nombre)).body()!!.id
    }

    override suspend fun actualizar(id: Int, nombre: String, activo: Boolean) = runCatching {
        api.actualizarMetodoPago(id, ActualizarMetodoPagoRequest(nombre, activo))
        Unit
    }

    override suspend fun listarDetalles(idMetodoPago: Int) = runCatching {
        api.listarDetalles(idMetodoPago).body()!!
    }

    override suspend fun crearDetalle(idMetodoPago: Int, nombre: String) = runCatching {
        api.crearDetalle(idMetodoPago, CrearDetalleRequest(nombre)).body()!!.id
    }

    override suspend fun actualizarDetalle(id: Int, nombre: String, activo: Boolean) = runCatching {
        api.actualizarDetalle(id, ActualizarDetalleRequest(nombre, activo))
        Unit
    }
}