package com.example.proyectotitulo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.proyectotitulo.ui.screens.AlertasScreen
import com.example.proyectotitulo.ui.theme.HealthTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AlertasComposeActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "AlertasCompose"
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
                var alertas by remember { mutableStateOf<List<AlertaSalud>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }
                
                LaunchedEffect(Unit) {
                    loadAlertas { loadedAlertas ->
                        alertas = loadedAlertas
                        isLoading = false
                    }
                }
                
                AlertasScreen(
                    alertas = alertas,
                    isLoading = isLoading,
                    onBackClick = { finish() },
                    onMarcarAtendida = { alertaId ->
                        AlertasService.marcarAlertaComoAtendida(alertaId)
                        // Recargar alertas después de marcar como atendida
                        loadAlertas { loadedAlertas ->
                            alertas = loadedAlertas
                        }
                    }
                )
            }
        }
    }
    
    private fun loadAlertas(onResult: (List<AlertaSalud>) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "Usuario no autenticado")
            onResult(emptyList())
            return
        }
        
        firestore.collection("users")
            .document(userId)
            .collection("alertas")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50) // Últimas 50 alertas
            .get()
            .addOnSuccessListener { documents ->
                val alertas = documents.mapNotNull { doc ->
                    try {
                        AlertaSalud(
                            id = doc.id,
                            atendida = doc.getBoolean("atendida") ?: false,
                            leida = doc.getBoolean("leida") ?: false,
                            mensaje = doc.getString("mensaje") ?: "",
                            parametrosAfectados = doc.get("parametros_afectados") as? List<String> ?: emptyList(),
                            recomendacion = doc.getString("recomendacion") ?: "",
                            registroOrigen = doc.getString("registro_origen") ?: "",
                            timestamp = doc.getTimestamp("timestamp"),
                            tipo = doc.getString("tipo") ?: "informativa",
                            titulo = doc.getString("titulo") ?: "Alerta de Salud",
                            umbralesUsados = doc.getString("umbrales_usados") ?: "",
                            valores = doc.get("valores") as? Map<String, Any> ?: emptyMap()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando alerta: ${e.message}", e)
                        null
                    }
                }
                Log.d(TAG, "Alertas cargadas: ${alertas.size}")
                onResult(alertas)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error cargando alertas: ${e.message}", e)
                onResult(emptyList())
            }
    }
}
