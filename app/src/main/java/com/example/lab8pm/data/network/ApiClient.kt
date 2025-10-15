package com.example.lab8pm.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente singleton para acceder a la API de Pexels
 * Configura Retrofit con Gson y logging para desarrollo
 */
object ApiClient {

    private const val BASE_URL = "https://api.pexels.com/"

    /**
     * Cliente HTTP con logging interceptor para debug
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Instancia de Retrofit configurada
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Instancia del servicio de Pexels API
     */
    val pexelsApi: PexelsApi by lazy {
        retrofit.create(PexelsApi::class.java)
    }
}