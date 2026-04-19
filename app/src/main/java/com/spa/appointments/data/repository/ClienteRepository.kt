package com.spa.appointments.data.repository

import com.spa.appointments.data.remote.ApiService
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
}