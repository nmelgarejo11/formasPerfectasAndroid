package com.spa.appointments.core.di

import com.spa.appointments.data.repository.AuthRepositoryImpl
import com.spa.appointments.data.repository.PerfilRepositoryImpl
import com.spa.appointments.domain.repository.AuthRepository
import com.spa.appointments.domain.repository.PerfilRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPerfilRepository(
        impl: PerfilRepositoryImpl
    ): PerfilRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}