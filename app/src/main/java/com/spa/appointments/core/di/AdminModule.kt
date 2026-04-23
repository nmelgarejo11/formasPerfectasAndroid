package com.spa.appointments.core.di

import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.data.repository.CatalogosAdminRepository
import com.spa.appointments.data.repository.ProfesionalesAdminRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdminModule {

    @Provides
    @Singleton
    fun provideCatalogosAdminRepository(api: ApiService): CatalogosAdminRepository =
        CatalogosAdminRepository(api)

    @Provides
    @Singleton
    fun provideProfesionalesAdminRepository(api: ApiService): ProfesionalesAdminRepository =
        ProfesionalesAdminRepository(api)
}