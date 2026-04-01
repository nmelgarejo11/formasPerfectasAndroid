package com.spa.appointments.di

import android.content.Context
import com.spa.appointments.core.security.TokenStorage
import com.spa.appointments.data.remote.ApiService
import com.spa.appointments.data.remote.AuthApiService
import com.spa.appointments.data.remote.AuthInterceptor
import com.spa.appointments.data.remote.TokenAuthenticator
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://192.168.1.6:5005/api/"

    // ---------- Moshi ----------
    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

    // ---------- Token Storage ----------
    @Provides
    @Singleton
    fun provideTokenStorage(
        @ApplicationContext context: Context
    ): TokenStorage = TokenStorage(context)

    // ---------- OkHttp SIN Auth (Login / Refresh) ----------
    @Provides
    @Singleton
    @Named("noAuth")
    fun provideNoAuthOkHttp(): OkHttpClient =
        OkHttpClient.Builder().build()

    // ---------- Auth API ----------
    @Provides
    @Singleton
    fun provideAuthApi(
        @Named("noAuth") okHttp: OkHttpClient,
        moshi: Moshi
    ): AuthApiService =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AuthApiService::class.java)

    // ---------- OkHttp CON Auth ----------
    @Provides
    @Singleton
    fun provideAuthOkHttp(
        tokenStorage: TokenStorage,
        authApi: AuthApiService
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStorage))
            .authenticator(TokenAuthenticator(authApi, tokenStorage))
            .build()

    // ---------- API protegida ----------
    @Provides
    @Singleton
    fun provideApi(
        okHttp: OkHttpClient,
        moshi: Moshi
    ): ApiService =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
}
