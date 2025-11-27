package com.example.proyectotitulo.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Request para procesar usuario
 */
data class UserRequest(
    val user_id: String
)

/**
 * Response del endpoint /procesar_usuario
 */
data class HealthResponse(
    val estado_general: String,
    val detalles: Detalles,
    val mensaje: String
)

data class Detalles(
    val sueño: String,
    val ritmo_cardiaco: String,
    val estres: String,
    val pasos: String
)

/**
 * Response de error
 */
data class ErrorResponse(
    val error: String
)

/**
 * Interface del servicio de API de salud
 */
interface HealthApiService {
    
    @GET("/")
    suspend fun healthCheck(): Map<String, String>
    
    @POST("/procesar_usuario")
    suspend fun procesarUsuario(
        @Body request: UserRequest
    ): HealthResponse
}

/**
 * Cliente Retrofit singleton
 */
object HealthApiClient {
    private const val BASE_URL = "https://health-api-409458732489.us-central1.run.app/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val api: HealthApiService by lazy {
        retrofit.create(HealthApiService::class.java)
    }
}
