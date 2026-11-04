package com.spa.appointments.domain.repository

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.domain.model.AsignarSubModuloRequest
import com.spa.appointments.domain.model.CambiarPerfilRequest
import com.spa.appointments.domain.model.Cargo
import com.spa.appointments.domain.model.ConsultaPerfil
import com.spa.appointments.domain.model.CrearCargoRequest
import com.spa.appointments.domain.model.CrearPerfilRequest
import com.spa.appointments.domain.model.CrearUsuarioRequest
import com.spa.appointments.domain.model.SubModuloAdmin
import com.spa.appointments.domain.model.SubModuloAsignadoDto
import com.spa.appointments.domain.model.Usuario
import javax.inject.Inject

interface AdministracionRepository {
    suspend fun crearUsuario(req: CrearUsuarioRequest): Result<Unit>
    suspend fun crearPerfil(req: CrearPerfilRequest): Result<Unit>
    suspend fun obtenerSubModulos(): Result<List<SubModuloAdmin>>
    suspend fun obtenerCargos(): Result<List<Cargo>>
    suspend fun crearCargo(req: CrearCargoRequest): Result<Unit>
    suspend fun cambiarEstadoCargo(id: Int, estado: Boolean): Result<Unit>
    suspend fun obtenerPerfiles(): Result<List<ConsultaPerfil>>
    suspend fun obtenerUsuarios(): Result<List<Usuario>>
    suspend fun cambiarPerfilUsuario(idUsuario: Int, idPerfil: Int): Result<Unit>
    suspend fun getSubModulos(idPerfil: Int): Result<List<SubModuloAsignadoDto>>
    suspend fun asignar(idPerfil: Int, idSubModulo: Int): Result<Unit>
    suspend fun quitar(idPerfil: Int, idSubModulo: Int): Result<Unit>

}

class AdministracionRepositoryImpl @Inject constructor(
    private val api: ApiService
) : AdministracionRepository {

    override suspend fun crearUsuario(req: CrearUsuarioRequest) = runCatching {
        val r = api.crearUsuario(req)
        if (!r.isSuccessful) throw Exception(r.errorBody()?.string() ?: "Error al crear usuario")
    }

    override suspend fun crearPerfil(req: CrearPerfilRequest) = runCatching {
        val r = api.crearPerfil(req)
        if (!r.isSuccessful) throw Exception(r.errorBody()?.string() ?: "Error al crear perfil")
    }

    override suspend fun obtenerSubModulos() = runCatching {
        val response = api.obtenerSubModulos()
        val body = response.body()
        body ?: emptyList()
    }

    override suspend fun obtenerCargos() = runCatching {
        api.obtenerCargos().body() ?: emptyList()
    }

    override suspend fun crearCargo(req: CrearCargoRequest) = runCatching {
        val r = api.crearCargo(req)
        if (!r.isSuccessful) throw Exception(r.errorBody()?.string() ?: "Error al crear cargo")
    }

    override suspend fun cambiarEstadoCargo(id: Int, estado: Boolean) = runCatching {
        val r = api.cambiarEstadoCargo(id, estado)
        if (!r.isSuccessful) throw Exception("Error al cambiar estado")
    }

    override suspend fun obtenerPerfiles() = runCatching {
        api.obtenerPerfiles().body() ?: emptyList()
    }

    override suspend fun obtenerUsuarios() = runCatching {
        api.obtenerUsuarios().body() ?: emptyList()
    }

    override suspend fun cambiarPerfilUsuario(idUsuario: Int, idPerfil: Int) = runCatching {
        val r = api.cambiarPerfilUsuario(CambiarPerfilRequest(idUsuario, idPerfil))
        if (!r.isSuccessful) throw Exception(r.errorBody()?.string() ?: "Error al cambiar perfil")
    }

    override suspend fun getSubModulos(idPerfil: Int) = runCatching {
        val response = api.getSubModulosPorPerfil(idPerfil)
        if (!response.isSuccessful) throw Exception(response.errorBody()?.string() ?: "Error al obtener submódulos")
        response.body() ?: emptyList()
    }

    override suspend fun asignar(idPerfil: Int, idSubModulo: Int) = runCatching {
        val response = api.asignarSubModulo(AsignarSubModuloRequest(idPerfil, idSubModulo))
        if (!response.isSuccessful) throw Exception(response.errorBody()?.string() ?: "Error al asignar submódulo")
    }

    override suspend fun quitar(idPerfil: Int, idSubModulo: Int) = runCatching {
        val response = api.quitarSubModulo(AsignarSubModuloRequest(idPerfil, idSubModulo))
        if (!response.isSuccessful) throw Exception(response.errorBody()?.string() ?: "Error al quitar submódulo")
    }

}