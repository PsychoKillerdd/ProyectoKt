package com.example.proyectotitulo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
                var contactosEmergencia by remember { mutableStateOf<Pair<String, String>>("" to "") }
                
                LaunchedEffect(Unit) {
                    loadAlertas { loadedAlertas ->
                        alertas = loadedAlertas
                        isLoading = false
                    }
                    loadContactosEmergencia { contacto1, contacto2 ->
                        contactosEmergencia = contacto1 to contacto2
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
                    },
                    onContactarEmergenciaSMS = { alerta ->
                        enviarSMSEmergencia(alerta, contactosEmergencia)
                    },
                    onContactarEmergenciaWhatsApp = { alerta ->
                        enviarWhatsAppEmergencia(alerta, contactosEmergencia)
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
    
    private fun loadContactosEmergencia(onResult: (String, String) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "Usuario no autenticado")
            onResult("", "")
            return
        }
        
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                // Intentar cargar los nuevos campos (emergencyContact1 y emergencyContact2)
                var contacto1 = document.getString("emergencyContact1") ?: ""
                var contacto2 = document.getString("emergencyContact2") ?: ""
                
                // Fallback: si no existen los nuevos campos, usar el campo antiguo
                if (contacto1.isEmpty() && contacto2.isEmpty()) {
                    val contactoAntiguo = document.getString("emergencyContact") ?: ""
                    if (contactoAntiguo.isNotEmpty()) {
                        contacto1 = contactoAntiguo
                        Log.d(TAG, "Usando formato antiguo de contacto de emergencia")
                    }
                }
                
                Log.d(TAG, "Contactos cargados - C1: ${contacto1.isNotEmpty()}, C2: ${contacto2.isNotEmpty()}")
                onResult(contacto1, contacto2)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error cargando contactos: ${e.message}", e)
                onResult("", "")
            }
    }
    
    private fun construirMensajeAlerta(alerta: AlertaSalud): String {
        val nombre = firebaseAuth.currentUser?.displayName ?: "Tu contacto"
        
        return buildString {
            append("🚨 ALERTA DE SALUD - HealthTrack 🚨\n\n")
            append("$nombre necesita tu atención:\n\n")
            append("${alerta.titulo}\n")
            append("${alerta.mensaje}\n\n")
            
            if (alerta.valores.isNotEmpty()) {
                append("📊 Datos registrados:\n")
                alerta.valores.forEach { (key, value) ->
                    val label = when (key) {
                        "frecuenciaCardiaca" -> "❤️ Frecuencia Cardíaca"
                        "horasDeSueno" -> "😴 Horas de Sueño"
                        "nivelDeEstres" -> "😰 Nivel de Estrés"
                        "saturacionOxigeno" -> "🫁 Saturación O₂"
                        "pasosDiarios" -> "🚶 Pasos"
                        else -> key
                    }
                    val unit = when (key) {
                        "frecuenciaCardiaca" -> " bpm"
                        "horasDeSueno" -> " hrs"
                        "nivelDeEstres" -> "/100"
                        "saturacionOxigeno" -> "%"
                        else -> ""
                    }
                    append("• $label: $value$unit\n")
                }
                append("\n")
            }
            
            if (alerta.recomendacion.isNotEmpty()) {
                append("💡 Recomendación:\n${alerta.recomendacion}\n\n")
            }
            
            append("Por favor, verifica su estado.")
        }
    }
    
    private fun enviarSMSEmergencia(alerta: AlertaSalud, contactos: Pair<String, String>) {
        val mensaje = construirMensajeAlerta(alerta)
        
        // Determinar qué contacto usar (priorizar el primero)
        val numeroDestino = when {
            contactos.first.isNotBlank() -> contactos.first
            contactos.second.isNotBlank() -> contactos.second
            else -> {
                Toast.makeText(this, "No hay contactos de emergencia registrados", Toast.LENGTH_LONG).show()
                return
            }
        }
        
        try {
            val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:$numeroDestino")
                putExtra("sms_body", mensaje)
            }
            startActivity(smsIntent)
            Log.d(TAG, "SMS de emergencia enviado a: $numeroDestino")
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar SMS: ${e.message}", e)
            Toast.makeText(this, "Error al abrir SMS", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun enviarWhatsAppEmergencia(alerta: AlertaSalud, contactos: Pair<String, String>) {
        val mensaje = construirMensajeAlerta(alerta)
        
        // Determinar qué contacto usar (priorizar el primero)
        val numeroDestino = when {
            contactos.first.isNotBlank() -> contactos.first.replace("+", "").replace(" ", "")
            contactos.second.isNotBlank() -> contactos.second.replace("+", "").replace(" ", "")
            else -> {
                Toast.makeText(this, "No hay contactos de emergencia registrados", Toast.LENGTH_LONG).show()
                return
            }
        }
        
        try {
            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$numeroDestino?text=${Uri.encode(mensaje)}")
                setPackage("com.whatsapp")
            }
            startActivity(whatsappIntent)
            Log.d(TAG, "WhatsApp de emergencia abierto a: $numeroDestino")
        } catch (e: Exception) {
            Log.e(TAG, "WhatsApp no instalado, intentando navegador", e)
            try {
                // Fallback: abrir en navegador
                val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$numeroDestino?text=${Uri.encode(mensaje)}")
                }
                startActivity(browserIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Error al abrir WhatsApp: ${e2.message}", e2)
                Toast.makeText(this, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
