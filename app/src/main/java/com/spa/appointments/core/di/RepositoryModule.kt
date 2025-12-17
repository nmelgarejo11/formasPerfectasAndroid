package com.spa.appointments.core.di

import com.spa.appointments.data.repository.AppointmentsRepositoryImpl
import com.spa.appointments.domain.repository.AppointmentsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAppointmentsRepository(): AppointmentsRepository =
        AppointmentsRepositoryImpl()
}
