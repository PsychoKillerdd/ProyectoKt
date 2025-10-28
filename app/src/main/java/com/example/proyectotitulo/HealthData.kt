package com.example.proyectotitulo

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data class to represent comprehensive health data collected from Health Connect.
 *
 * @property pasosDiarios Daily step count
 * @property horasDeSueño Sleep duration in hours
 * @property saturacionOxigeno Blood oxygen saturation (SpO2) percentage
 * @property frecuenciaCardiaca Heart rate in BPM (beats per minute)
 * @property relojColocado Whether the watch/device is being worn (from heart rate data availability)
 * @property nivelDeEstres Stress level (0-100, derived from heart rate variability if available)
 * @property horaRegistro Time when the data was recorded
 * @property fecha Date of the health data
 * @property peso Weight in kg
 * @property altura Height in meters
 * @property lastUpdated Server timestamp of when this record was saved to Firebase
 */
data class HealthData(
    val pasosDiarios: Long = 0,
    val horasDeSueño: Double = 0.0,
    val saturacionOxigeno: Double = 0.0, // SpO2 en porcentaje
    val frecuenciaCardiaca: Long = 0, // Promedio del día en BPM
    val frecuenciaCardiacaMax: Long = 0,
    val frecuenciaCardiacaMin: Long = 0,
    val relojColocado: Boolean = false,
    val nivelDeEstres: Int = 0, // 0-100
    val horaRegistro: String = "",
    val fecha: String = "",
    val peso: Double = 0.0,
    val altura: Double = 0.0,
    @ServerTimestamp
    val lastUpdated: Date? = null
)
