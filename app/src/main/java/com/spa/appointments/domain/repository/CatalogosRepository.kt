package com.spa.appointments.domain.repository

import com.spa.appointments.domain.model.BancoItem
import com.spa.appointments.domain.model.TipoCuentaItem
import com.spa.appointments.domain.model.TipoDocumentoItem

interface CatalogosRepository {
    suspend fun getBancos(): Result<List<BancoItem>>
    suspend fun getTiposDocumento(): Result<List<TipoDocumentoItem>>
    suspend fun getTiposCuenta(): Result<List<TipoCuentaItem>>
}
