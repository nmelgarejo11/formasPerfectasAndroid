package com.spa.appointments.data.repository

import android.util.Log
import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.ActualizarDetalleRequest
import com.spa.appointments.domain.model.ActualizarMetodoPagoRequest
import com.spa.appointments.domain.model.CrearDetalleRequest
import com.spa.appointments.domain.model.CrearMetodoPagoRequest
import com.spa.appointments.domain.repository.MetodoPagoRepository

class MetodoPagoRepositoryImpl(
    private val api: ApiService
) : MetodoPagoRepository {

    override suspend fun listar() = runCatching {

        val response = api.listarMetodosPago()

        Log.d("METODO_PAGO", "LISTAR code=${response.code()}")

        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            val error = response.errorBody()?.string()
            Log.e("METODO_PAGO", "LISTAR error=$error")
            throw Exception(error ?: "Error al listar")
        }
    }

    override suspend fun obtener(id: Int) = runCatching {

        val response = api.obtenerMetodoPago(id)

        Log.d("METODO_PAGO", "OBTENER code=${response.code()}")

        if (response.isSuccessful) {
            response.body()
                ?: throw Exception("Respuesta vacía")
        } else {
            throw Exception(
                response.errorBody()?.string()
                    ?: "Error al obtener"
            )
        }
    }

    override suspend fun crear(nombre: String) = runCatching {

        val response = api.crearMetodoPago(
            CrearMetodoPagoRequest(nombre)
        )

        Log.d("METODO_PAGO", "CREAR code=${response.code()}")

        if (response.isSuccessful) {
            response.body()?.id
                ?: throw Exception("Id nulo")
        } else {
            throw Exception(
                response.errorBody()?.string()
                    ?: "Error al crear"
            )
        }
    }

    override suspend fun actualizar(
        id: Int,
        nombre: String,
        activo: Boolean
    ) = runCatching {

        val response = api.actualizarMetodoPago(
            id,
            ActualizarMetodoPagoRequest(nombre, activo)
        )

        Log.d("METODO_PAGO", "UPDATE code=${response.code()}")

        if (!response.isSuccessful) {
            throw Exception(
                response.errorBody()?.string()
                    ?: "Error al actualizar"
            )
        }

        Unit
    }

    override suspend fun listarDetalles(idMetodoPago: Int) = runCatching {

        val response = api.listarDetalles(idMetodoPago)

        Log.d("METODO_PAGO", "DETALLES code=${response.code()}")

        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception(
                response.errorBody()?.string()
                    ?: "Error detalles"
            )
        }
    }

    override suspend fun crearDetalle(
        idMetodoPago: Int,
        nombre: String
    ) = runCatching {

        val response = api.crearDetalle(
            idMetodoPago,
            CrearDetalleRequest(nombre)
        )

        Log.d("METODO_PAGO", "CREAR DETALLE code=${response.code()}")

        if (response.isSuccessful) {
            response.body()?.id
                ?: throw Exception("Id nulo")
        } else {
            throw Exception(
                response.errorBody()?.string()
                    ?: "Error al crear detalle"
            )
        }
    }

    override suspend fun actualizarDetalle(
        id: Int,
        nombre: String,
        activo: Boolean
    ) = runCatching {

        val response = api.actualizarDetalle(
            id,
            ActualizarDetalleRequest(nombre, activo)
        )

        Log.d("METODO_PAGO", "UPDATE DETALLE code=${response.code()}")

        if (!response.isSuccessful) {
            throw Exception(
                response.errorBody()?.string()
                    ?: "Error actualizar detalle"
            )
        }

        Unit
    }
}