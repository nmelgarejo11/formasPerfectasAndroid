package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.ActualizarClienteRequest
import com.spa.appointments.domain.model.Cliente
import com.spa.appointments.domain.model.CrearClienteRequest
import javax.inject.Inject

class ClienteRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun buscarClientes(busqueda: String): List<Cliente> =
        api.buscarClientes(busqueda)

    suspend fun crearCliente(request: CrearClienteRequest): Cliente =
        api.crearCliente(request)

    suspend fun obtenerCliente(id: Int): Cliente =
        api.obtenerCliente(id)

    suspend fun actualizarCliente(id: Int, request: ActualizarClienteRequest) =
        api.actualizarCliente(id, request)

    suspend fun desactivarCliente(id: Int) =
        api.desactivarCliente(id)
}