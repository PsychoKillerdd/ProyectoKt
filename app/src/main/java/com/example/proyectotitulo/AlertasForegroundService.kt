package com.example.proyectotitulo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Servicio de Android que mantiene el listener de alertas activo en background
 */
class AlertasForegroundService : Service() {
    
    companion object {
        private const val TAG = "AlertasFgService"
        const val ACTION_START = "START_ALERTAS_SERVICE"
        const val ACTION_STOP = "STOP_ALERTAS_SERVICE"
    }
    
    private var alertasListener: ListenerRegistration? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    
    override fun onCreate() {
        super.onCreate()
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        Log.d(TAG, "Servicio de alertas creado")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                Log.d(TAG, "Iniciando servicio de alertas")
                startListeningAlerts()
            }
            ACTION_STOP -> {
                Log.d(TAG, "Deteniendo servicio de alertas")
                stopSelf()
            }
        }
        
        // START_STICKY asegura que el servicio se reinicie si es detenido por el sistema
        return START_STICKY
    }
    
    private fun startListeningAlerts() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "Usuario no autenticado")
            stopSelf()
            return
        }
        
        // Detener listener anterior si existe
        alertasListener?.remove()
        
        Log.d(TAG, "Iniciando listener de alertas para usuario: $userId")
        
        // Escuchar alertas en tiempo real
        alertasListener = firestore.collection("users")
            .document(userId)
            .collection("alertas")
            .whereEqualTo("leida", false)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Error en listener: ${error.message}", error)
                    return@addSnapshotListener
                }
                
                if (snapshots != null) {
                    for (docChange in snapshots.documentChanges) {
                        if (docChange.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val doc = docChange.document
                            
                            // Verificar que la alerta sea realmente nueva (timestamp reciente)
                            val timestamp = doc.getTimestamp("timestamp")
                            val now = System.currentTimeMillis()
                            val alertTime = timestamp?.toDate()?.time ?: 0
                            val diffMinutes = (now - alertTime) / (1000 * 60)
                            
                            // Solo procesar alertas de los últimos 5 minutos
                            if (diffMinutes <= 5) {
                                val alerta = AlertaSalud(
                                    id = doc.id,
                                    atendida = doc.getBoolean("atendida") ?: false,
                                    leida = doc.getBoolean("leida") ?: false,
                                    mensaje = doc.getString("mensaje") ?: "",
                                    parametrosAfectados = doc.get("parametros_afectados") as? List<String> ?: emptyList(),
                                    recomendacion = doc.getString("recomendacion") ?: "",
                                    registroOrigen = doc.getString("registro_origen") ?: "",
                                    timestamp = timestamp,
                                    tipo = doc.getString("tipo") ?: "informativa",
                                    titulo = doc.getString("titulo") ?: "Alerta de Salud",
                                    umbralesUsados = doc.getString("umbrales_usados") ?: "",
                                    valores = doc.get("valores") as? Map<String, Any> ?: emptyMap()
                                )
                                
                                Log.d(TAG, "🆕 Nueva alerta detectada: ${alerta.tipo} - ${alerta.titulo}")
                                
                                // Usar AlertasService para mostrar la notificación
                                AlertasService.mostrarNotificacionPublica(alerta)
                                
                                // Marcar como leída
                                AlertasService.marcarAlertaComoLeidaPublica(userId, alerta.id)
                            } else {
                                Log.d(TAG, "Alerta antigua ignorada: $diffMinutes minutos")
                            }
                        }
                    }
                }
            }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        alertasListener?.remove()
        Log.d(TAG, "Servicio de alertas destruido")
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
