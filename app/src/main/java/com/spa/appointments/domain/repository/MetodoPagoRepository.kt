package com.spa.appointments.domain.repository

import com.spa.appointments.domain.model.MetodoPagoAdmin
import com.spa.appointments.domain.model.MetodoPagoDetalleAdmin

interface MetodoPagoRepository {
    suspend fun listar(): Result<List<MetodoPagoAdmin>>
    suspend fun obtener(id: Int): Result<MetodoPagoAdmin>
    suspend fun crear(nombre: String): Result<Int>
    suspend fun actualizar(id: Int, nombre: String, activo: Boolean): Result<Unit>
    suspend fun listarDetalles(idMetodoPago: Int): Result<List<MetodoPagoDetalleAdmin>>
    suspend fun crearDetalle(idMetodoPago: Int, nombre: String): Result<Int>
    suspend fun actualizarDetalle(id: Int, nombre: String, activo: Boolean): Result<Unit>
}