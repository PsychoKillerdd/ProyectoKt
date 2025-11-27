package com.example.proyectotitulo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.proyectotitulo.ui.screens.HistoryRecord
import com.example.proyectotitulo.ui.screens.HistoryScreen
import com.example.proyectotitulo.ui.theme.HealthTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryComposeActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "HistoryCompose"
    }
    
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        setContent {
            HealthTrackTheme {
                var records by remember { mutableStateOf<List<HistoryRecord>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }
                
                LaunchedEffect(Unit) {
                    loadHistory { loadedRecords ->
                        records = loadedRecords
                        isLoading = false
                    }
                }
                
                HistoryScreen(
                    records = records,
                    isLoading = isLoading,
                    onBackClick = { finish() }
                )
            }
        }
    }
    
    private fun loadHistory(onResult: (List<HistoryRecord>) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            onResult(emptyList())
            return
        }
        
        firestore.collection("users").document(userId)
            .collection("health_records")
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .limit(20) // Últimos 20 registros
            .get()
            .addOnSuccessListener { documents ->
                val records = documents.mapNotNull { doc ->
                    try {
                        val pasos = doc.getLong("pasosDiarios") ?: 0
                        val hr = doc.getLong("frecuenciaCardiaca") ?: 0
                        val sleep = doc.getDouble("horasDeSueno") ?: doc.getDouble("horasDeSueño") ?: 0.0
                        val spo2 = doc.getDouble("saturacionOxigeno") ?: 0.0
                        val stress = (doc.getLong("nivelDeEstres") ?: 0).toInt()
                        val relojColocado = doc.getBoolean("relojColocado") ?: false
                        
                        // Solo filtrar registros donde el reloj no estaba colocado Y todo está en 0
                        if (!relojColocado && pasos == 0L && hr == 0L && sleep == 0.0) {
                            return@mapNotNull null
                        }
                        
                        HistoryRecord(
                            id = doc.id,
                            fecha = doc.getString("fecha") ?: "",
                            horaRegistro = doc.getString("horaRegistro") ?: "",
                            pasos = pasos,
                            frecuenciaCardiaca = hr,
                            horasSueno = sleep,
                            spo2 = spo2,
                            estres = stress
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing document ${doc.id}", e)
                        null
                    }
                }
                
                // Mostrar TODOS los registros válidos, ordenados por fecha y hora
                val sortedRecords = records
                    .sortedWith(compareByDescending<HistoryRecord> { it.fecha }
                        .thenByDescending { it.horaRegistro })
                
                Log.d(TAG, "Loaded ${sortedRecords.size} history records")
                onResult(sortedRecords)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading history", e)
                onResult(emptyList())
            }
    }
}
