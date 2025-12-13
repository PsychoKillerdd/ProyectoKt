package com.example.proyectotitulo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Modelo de datos para las alertas de salud
 */
data class AlertaSalud(
    val id: String = "",
    val atendida: Boolean = false,
    val leida: Boolean = false,
    val mensaje: String = "",
    val parametrosAfectados: List<String> = emptyList(),
    val recomendacion: String = "",
    val registroOrigen: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val tipo: String = "", // "critica", "advertencia", "informativa"
    val titulo: String = "",
    val umbralesUsados: String = "",
    val valores: Map<String, Any> = emptyMap()
)

/**
 * Servicio para escuchar alertas de salud desde Firebase
 * y mostrar notificaciones push en tiempo real
 */
object AlertasService {
    
    private const val TAG = "AlertasService"
    private const val CHANNEL_ID_CRITICA = "alertas_criticas"
    private const val CHANNEL_ID_ADVERTENCIA = "alertas_advertencia"
    private const val CHANNEL_ID_INFO = "alertas_info"
    
    private var alertasListener: ListenerRegistration? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var context: Context
    
    /**
     * Inicializa el servicio de alertas
     */
    fun init(appContext: Context) {
        context = appContext.applicationContext
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        
        createNotificationChannels()
        Log.d(TAG, "AlertasService inicializado")
    }
    
    /**
     * Crea los canales de notificación según la prioridad
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para alertas críticas (máxima prioridad)
            val criticalChannel = NotificationChannel(
                CHANNEL_ID_CRITICA,
                "Alertas Críticas de Salud",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de salud que requieren atención inmediata"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            }
            
            // Canal para advertencias (prioridad media)
            val warningChannel = NotificationChannel(
                CHANNEL_ID_ADVERTENCIA,
                "Advertencias de Salud",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertas de salud importantes pero no urgentes"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Canal para información (prioridad baja)
            val infoChannel = NotificationChannel(
                CHANNEL_ID_INFO,
                "Información de Salud",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Alertas informativas sobre tu salud"
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(criticalChannel)
            notificationManager.createNotificationChannel(warningChannel)
            notificationManager.createNotificationChannel(infoChannel)
            
            Log.d(TAG, "Canales de notificación creados")
        }
    }
    
    /**
     * Inicia la escucha de alertas en tiempo real
     */
    fun startListening() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "Usuario no autenticado. No se puede escuchar alertas.")
            return
        }
        
        // Cancelar listener anterior si existe
        stopListening()
        
        Log.d(TAG, "Iniciando escucha de alertas para usuario: $userId")
        
        // Escuchar alertas en tiempo real
        alertasListener = firestore.collection("users")
            .document(userId)
            .collection("alertas")
            .whereEqualTo("leida", false) // Solo alertas no leídas
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Error al escuchar alertas: ${error.message}", error)
                    return@addSnapshotListener
                }
                
                if (snapshots != null) {
                    for (docChange in snapshots.documentChanges) {
                        when (docChange.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                                val doc = docChange.document
                                val alerta = AlertaSalud(
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
                                
                                Log.d(TAG, "Nueva alerta detectada: ${alerta.tipo} - ${alerta.titulo}")
                                mostrarNotificacionAlerta(alerta)
                                
                                // Marcar como leída después de mostrar la notificación
                                marcarAlertaComoLeida(userId, alerta.id)
                            }
                            else -> {
                                // Ignorar modificaciones y eliminaciones
                            }
                        }
                    }
                }
            }
        
        Log.d(TAG, "Listener de alertas activo")
    }
    
    /**
     * Detiene la escucha de alertas
     */
    fun stopListening() {
        alertasListener?.remove()
        alertasListener = null
        Log.d(TAG, "Listener de alertas detenido")
    }
    
    /**
     * Muestra una notificación push para la alerta
     */
    private fun mostrarNotificacionAlerta(alerta: AlertaSalud) {
        val channelId = when (alerta.tipo.lowercase()) {
            "critica" -> CHANNEL_ID_CRITICA
            "advertencia" -> CHANNEL_ID_ADVERTENCIA
            else -> CHANNEL_ID_INFO
        }
        
        val intent = Intent(context, DashboardComposeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("alerta_id", alerta.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            alerta.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Icono según el tipo de alerta
        val icon = when (alerta.tipo.lowercase()) {
            "critica" -> android.R.drawable.ic_dialog_alert
            "advertencia" -> android.R.drawable.ic_dialog_info
            else -> android.R.drawable.ic_dialog_email
        }
        
        // Color según el tipo
        val color = when (alerta.tipo.lowercase()) {
            "critica" -> 0xFFD32F2F.toInt() // Rojo
            "advertencia" -> 0xFFFFA000.toInt() // Naranja
            else -> 0xFF1976D2.toInt() // Azul
        }
        
        // Construir texto detallado con valores
        val detallesTexto = buildString {
            append(alerta.mensaje)
            
            if (alerta.valores.isNotEmpty()) {
                append("\n\n📊 Valores registrados:")
                alerta.valores.forEach { (key, value) ->
                    val label = when (key) {
                        "frecuenciaCardiaca" -> "Frecuencia Cardíaca"
                        "horasDeSueno" -> "Horas de Sueño"
                        "nivelDeEstres" -> "Nivel de Estrés"
                        "saturacionOxigeno" -> "Saturación de Oxígeno"
                        "pasosDiarios" -> "Pasos"
                        else -> key
                    }
                    val unit = when (key) {
                        "frecuenciaCardiaca" -> " bpm"
                        "horasDeSueno" -> " hrs"
                        "nivelDeEstres" -> "/100"
                        "saturacionOxigeno" -> "%"
                        "pasosDiarios" -> " pasos"
                        else -> ""
                    }
                    append("\n• $label: $value$unit")
                }
            }
            
            if (alerta.recomendacion.isNotEmpty()) {
                append("\n\n💡 Recomendación: ${alerta.recomendacion}")
            }
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(alerta.titulo)
            .setContentText(alerta.mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detallesTexto))
            .setPriority(
                when (alerta.tipo.lowercase()) {
                    "critica" -> NotificationCompat.PRIORITY_MAX
                    "advertencia" -> NotificationCompat.PRIORITY_HIGH
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setColor(color)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(alerta.id.hashCode(), notification)
        
        Log.d(TAG, "Notificación mostrada para alerta: ${alerta.id}")
    }
    
    /**
     * Marca una alerta como leída en Firebase
     */
    private fun marcarAlertaComoLeida(userId: String, alertaId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("alertas")
            .document(alertaId)
            .update("leida", true)
            .addOnSuccessListener {
                Log.d(TAG, "Alerta marcada como leída: $alertaId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al marcar alerta como leída: ${e.message}", e)
            }
    }
    
    /**
     * Marca una alerta como atendida
     */
    fun marcarAlertaComoAtendida(alertaId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        firestore.collection("users")
            .document(userId)
            .collection("alertas")
            .document(alertaId)
            .update(
                mapOf(
                    "atendida" to true,
                    "leida" to true
                )
            )
            .addOnSuccessListener {
                Log.d(TAG, "Alerta marcada como atendida: $alertaId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al marcar alerta como atendida: ${e.message}", e)
            }
    }
    
    /**
     * Obtiene el conteo de alertas no atendidas
     */
    fun obtenerAlertasNoAtendidas(callback: (Int) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            callback(0)
            return
        }
        
        firestore.collection("users")
            .document(userId)
            .collection("alertas")
            .whereEqualTo("atendida", false)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.size())
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener alertas no atendidas: ${e.message}", e)
                callback(0)
            }
    }
    
    /**
     * Función pública para mostrar notificación (usada por el servicio)
     */
    fun mostrarNotificacionPublica(alerta: AlertaSalud) {
        mostrarNotificacionAlerta(alerta)
    }
    
    /**
     * Función pública para marcar alerta como leída
     */
    fun marcarAlertaComoLeidaPublica(userId: String, alertaId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("alertas")
            .document(alertaId)
            .update("leida", true)
            .addOnSuccessListener {
                Log.d(TAG, "Alerta marcada como leída: $alertaId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al marcar alerta como leída: ${e.message}", e)
            }
    }
}
