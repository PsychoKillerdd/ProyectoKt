package com.example.proyectotitulo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.proyectotitulo.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.ZonedDateTime
import java.util.Date

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var samsungHealthManager: SamsungHealthManager

    private val APP_TAG = "SamsungHealthApp"
    
    // Handler para recopilación automática cada 30 minutos
    private val autoCollectHandler = Handler(Looper.getMainLooper())
    private val autoCollectInterval = 30 * 60 * 1000L // 30 minutos en milisegundos
    
    private val autoCollectRunnable = object : Runnable {
        override fun run() {
            Log.d(APP_TAG, "⏰ Recopilación automática de datos cada 30 minutos")
            readHealthData()
            autoCollectHandler.postDelayed(this, autoCollectInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar edge-to-edge para evitar que los botones queden ocultos
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inicializar Samsung Health Manager
        samsungHealthManager = SamsungHealthManager(this)
        
        // Configurar callbacks de conexión
        samsungHealthManager.onConnectionSuccess = {
            Log.d(APP_TAG, "Samsung Health conectado exitosamente")
            runOnUiThread {
                Toast.makeText(this, "Samsung Health conectado", Toast.LENGTH_SHORT).show()
                // Solicitar permisos después de conectar
                requestSamsungHealthPermissions()
            }
        }
        
        samsungHealthManager.onConnectionError = { errorCode ->
            Log.e(APP_TAG, "Error al conectar con Samsung Health: $errorCode")
            runOnUiThread {
                Toast.makeText(this, "Error al conectar con Samsung Health", Toast.LENGTH_LONG).show()
            }
        }
        
        // Conectar a Samsung Health
        samsungHealthManager.connect()

        binding.buttonUpdate.setOnClickListener {
            Log.d(APP_TAG, "Update button clicked. Starting data read...")
            if (samsungHealthManager.isConnected()) {
                readHealthData()
            } else {
                Toast.makeText(this, "Reconectando a Samsung Health...", Toast.LENGTH_SHORT).show()
                samsungHealthManager.connect()
            }
        }

        binding.buttonHistory.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de historial no implementada", Toast.LENGTH_SHORT).show()
        }

        binding.buttonLogout.setOnClickListener {
            samsungHealthManager.stopHeartRateMonitoring()
            samsungHealthManager.disconnect()
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Cargar mensajes de ejemplo
        loadSampleMessages()
        
        // Iniciar recopilación automática cada 30 minutos
        startAutoCollection()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener recopilación automática
        autoCollectHandler.removeCallbacks(autoCollectRunnable)
        samsungHealthManager.stopHeartRateMonitoring()
        samsungHealthManager.disconnect()
    }
    
    private fun startAutoCollection() {
        Log.d(APP_TAG, "🔄 Iniciando recopilación automática cada 30 minutos")
        autoCollectHandler.postDelayed(autoCollectRunnable, autoCollectInterval)
        addMessage(
            "🔄 Recopilación Automática",
            "Los datos de salud se recopilarán automáticamente cada 30 minutos",
            "info"
        )
    }

    private fun requestSamsungHealthPermissions() {
        samsungHealthManager.requestPermissions { granted ->
            runOnUiThread {
                if (granted) {
                    Log.d(APP_TAG, "Permisos de Samsung Health otorgados")
                    Toast.makeText(this, "✅ Permisos otorgados", Toast.LENGTH_SHORT).show()
                    
                    // Iniciar monitoreo en tiempo real de frecuencia cardíaca
                    startRealtimeHeartRateMonitoring()
                    
                    // Leer datos iniciales
                    readHealthData()
                } else {
                    Log.e(APP_TAG, "Permisos de Samsung Health denegados")
                    Toast.makeText(this, "❌ Permisos denegados. No se pueden leer datos de salud.", Toast.LENGTH_LONG).show()
                    addMessage(
                        "⚠️ Permisos Requeridos",
                        "Necesitas otorgar permisos de Samsung Health para ver tus datos de salud.",
                        "warning"
                    )
                }
            }
        }
    }

    private fun startRealtimeHeartRateMonitoring() {
        samsungHealthManager.startHeartRateMonitoring { heartRate ->
            runOnUiThread {
                Log.d(APP_TAG, "💓 Frecuencia cardíaca en tiempo real: $heartRate BPM")
                addMessage(
                    "💓 Frecuencia Cardíaca Actualizada",
                    "Nueva lectura: ${heartRate.toInt()} BPM",
                    "info"
                )
            }
        }
    }


    private fun loadSampleMessages() {
        // Mensaje de bienvenida
        addMessage(
            "📢 Bienvenido", 
            "¡Dashboard de salud activo! Los datos se recopilarán automáticamente cada 30 minutos. Presiona 'Actualizar' para crear un nuevo registro ahora.", 
            "info"
        )
    }

    private fun addMessage(title: String, message: String, type: String = "info") {
        val messagesContainer = binding.messagesContainer
        binding.textViewNoMessages.visibility = android.view.View.GONE

        // Crear card para el mensaje
        val messageCard = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            radius = 8f
            cardElevation = 2f
            setCardBackgroundColor(when(type) {
                "warning" -> ContextCompat.getColor(context, android.R.color.holo_orange_light)
                "error" -> ContextCompat.getColor(context, android.R.color.holo_red_light)
                "success" -> ContextCompat.getColor(context, android.R.color.holo_green_light)
                else -> ContextCompat.getColor(context, android.R.color.white)
            })
        }

        val messageLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }

        val messageView = TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            setPadding(0, 8, 0, 0)
        }

        val timeView = TextView(this).apply {
            text = "Ahora"
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            setPadding(0, 8, 0, 0)
            gravity = Gravity.END
        }

        messageLayout.addView(titleView)
        messageLayout.addView(messageView)
        messageLayout.addView(timeView)
        messageCard.addView(messageLayout)
        messagesContainer.addView(messageCard, 0) // Agregar al principio
    }

    private fun readHealthData() {
        Log.d(APP_TAG, "Leyendo datos de salud de Samsung Health...")
        
        // Obtener fecha y hora actual
        val currentDateTime = ZonedDateTime.now()
        val timeOfDay = "${currentDateTime.hour.toString().padStart(2, '0')}:${currentDateTime.minute.toString().padStart(2, '0')}"
        val dateString = "${currentDateTime.year}-${currentDateTime.monthValue.toString().padStart(2, '0')}-${currentDateTime.dayOfMonth.toString().padStart(2, '0')}"
        
        // Leer todos los datos de Samsung Health
        var steps: Long = 0
        var sleepHours: Double = 0.0
        var heartRateAvg: Float = 0f
        var heartRateMax: Float = 0f
        var heartRateMin: Float = 0f
        var weight: Double = 0.0
        var height: Double = 0.0
        
        // Leer pasos
        samsungHealthManager.readStepsToday { stepsResult ->
            steps = stepsResult
            Log.d(APP_TAG, "Pasos: $steps")
            
            // Leer sueño
            samsungHealthManager.readSleep { sleepResult ->
                sleepHours = sleepResult
                Log.d(APP_TAG, "Horas de sueño: $sleepHours")
                
                // Leer frecuencia cardíaca
                samsungHealthManager.readHeartRate { avg, max, min ->
                    heartRateAvg = avg
                    heartRateMax = max
                    heartRateMin = min
                    Log.d(APP_TAG, "Frecuencia cardíaca - Avg: $avg, Max: $max, Min: $min")
                    
                    // Leer peso
                    samsungHealthManager.readWeight { weightResult ->
                        weight = weightResult
                        Log.d(APP_TAG, "Peso: $weight kg")
                        
                        // Leer altura
                        samsungHealthManager.readHeight { heightResult ->
                            height = heightResult
                            Log.d(APP_TAG, "Altura: $height m")
                            
                            // Calcular nivel de estrés
                            val stressLevel = calculateStressLevel(heartRateAvg, heartRateMax, heartRateMin)
                            
                            // Determinar si el reloj está colocado
                            val watchWorn = heartRateAvg > 0
                            
                            // Crear objeto HealthData
                            val healthData = HealthData(
                                pasosDiarios = steps,
                                horasDeSueño = sleepHours,
                                tiempoPantalla = 0.0, // No disponible en Samsung Health SDK
                                frecuenciaCardiaca = heartRateAvg.toLong(),
                                frecuenciaCardiacaMax = heartRateMax.toLong(),
                                frecuenciaCardiacaMin = heartRateMin.toLong(),
                                relojColocado = watchWorn,
                                nivelDeEstres = stressLevel,
                                horaRegistro = timeOfDay,
                                fecha = dateString,
                                peso = weight,
                                altura = height
                            )
                            
                            Log.d(APP_TAG, "Health data collected: $healthData")
                            
                            // Actualizar UI en el hilo principal
                            runOnUiThread {
                                val uiText = buildString {
                                    appendLine("📅 Fecha: ${healthData.fecha}")
                                    appendLine("🕐 Hora: ${healthData.horaRegistro}")
                                    appendLine("👟 Pasos: ${healthData.pasosDiarios}")
                                    appendLine("😴 Sueño: ${"%.1f".format(healthData.horasDeSueño)} horas")
                                    appendLine("❤️ Frecuencia Cardíaca: ${healthData.frecuenciaCardiaca} BPM")
                                    if (healthData.frecuenciaCardiacaMax > 0) {
                                        appendLine("   Max: ${healthData.frecuenciaCardiacaMax} BPM")
                                        appendLine("   Min: ${healthData.frecuenciaCardiacaMin} BPM")
                                    }
                                    appendLine("⌚ Reloj Colocado: ${if (healthData.relojColocado) "Sí" else "No"}")
                                    appendLine("😰 Nivel de Estrés: ${healthData.nivelDeEstres}/100")
                                    if (healthData.peso > 0) {
                                        appendLine("⚖️ Peso: ${"%.1f".format(healthData.peso)} kg")
                                    }
                                    if (healthData.altura > 0) {
                                        appendLine("📏 Altura: ${"%.2f".format(healthData.altura)} m")
                                    }
                                }
                                
                                binding.textViewContentBody.text = uiText
                                
                                // Guardar en Firebase
                                saveHealthDataToFirebase(healthData)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun calculateStressLevel(avg: Float, max: Float, min: Float): Int {
        if (avg == 0f) {
            return 0
        }
        
        // Cálculo de nivel de estrés basado en frecuencia cardíaca
        val variability = if (max > min) max - min else 0f
        
        // Normalizar el nivel de estrés (0-100)
        val stressFromAvg = when {
            avg < 60 -> 10 // Muy bajo, posiblemente atleta
            avg in 60f..80f -> 20 // Normal-bajo
            avg in 81f..100f -> 40 // Normal
            avg in 101f..120f -> 70 // Elevado
            else -> 90 // Muy elevado
        }
        
        // Ajustar según variabilidad
        val adjustedStress = when {
            variability > 40 -> (stressFromAvg * 0.7).toInt()
            variability > 20 -> (stressFromAvg * 0.85).toInt()
            else -> stressFromAvg
        }
        
        return adjustedStress.coerceIn(0, 100)
    }


    private fun saveHealthDataToFirebase(healthData: HealthData) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        // Crear timestamp único para este registro
        val timestamp = System.currentTimeMillis()
        val documentId = "${healthData.fecha}_$timestamp"
        
        val dataMap = mapOf(
            "pasosDiarios" to healthData.pasosDiarios,
            "horasDeSueño" to healthData.horasDeSueño,
            "tiempoPantalla" to healthData.tiempoPantalla,
            "frecuenciaCardiaca" to healthData.frecuenciaCardiaca,
            "frecuenciaCardiacaMax" to healthData.frecuenciaCardiacaMax,
            "frecuenciaCardiacaMin" to healthData.frecuenciaCardiacaMin,
            "relojColocado" to healthData.relojColocado,
            "nivelDeEstres" to healthData.nivelDeEstres,
            "horaRegistro" to healthData.horaRegistro,
            "fecha" to healthData.fecha,
            "peso" to healthData.peso,
            "altura" to healthData.altura,
            "timestamp" to timestamp,
            "createdAt" to Date()
        )
        
        Log.d(APP_TAG, "💾 Guardando NUEVO registro de salud en Firebase con ID: $documentId")

        // Crear un NUEVO documento cada vez (no actualizar el existente)
        firestore.collection("users").document(userId)
            .collection("health_records").document(documentId)
            .set(dataMap)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Nuevo registro guardado en Firebase", Toast.LENGTH_SHORT).show()
                Log.d(APP_TAG, "✅ Nuevo registro de salud guardado exitosamente: $documentId")
                
                // Agregar mensaje de éxito
                addMessage(
                    "✅ Nuevo Registro Creado",
                    "Registro #$timestamp guardado: ${healthData.fecha} a las ${healthData.horaRegistro}",
                    "success"
                )
                
                // Agregar alertas basadas en los datos
                checkHealthAlerts(healthData)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(APP_TAG, "Error saving to Firestore", e)
                
                addMessage(
                    "❌ Error al Guardar",
                    "No se pudo crear el nuevo registro: ${e.message}",
                    "error"
                )
            }
    }

    private fun checkHealthAlerts(healthData: HealthData) {
        // Alertas basadas en nivel de estrés
        if (healthData.nivelDeEstres > 70) {
            addMessage(
                "⚠️ Nivel de Estrés Alto",
                "Tu nivel de estrés es ${healthData.nivelDeEstres}/100. Te recomendamos tomar un descanso y realizar ejercicios de respiración.",
                "warning"
            )
        }

        // Alerta de sueño insuficiente
        if (healthData.horasDeSueño > 0 && healthData.horasDeSueño < 6) {
            addMessage(
                "😴 Sueño Insuficiente",
                "Dormiste ${String.format("%.1f", healthData.horasDeSueño)} horas. Se recomienda dormir entre 7-9 horas para una óptima salud.",
                "warning"
            )
        }

        // Felicitaciones por meta de pasos
        if (healthData.pasosDiarios >= 10000) {
            addMessage(
                "🎉 ¡Meta Cumplida!",
                "¡Excelente! Has alcanzado ${healthData.pasosDiarios} pasos hoy. ¡Sigue así!",
                "success"
            )
        }

        // Alerta si el reloj no está colocado
        if (!healthData.relojColocado) {
            addMessage(
                "⌚ Reloj No Detectado",
                "No se detectaron datos de frecuencia cardíaca. Asegúrate de tener tu reloj colocado para un seguimiento completo.",
                "info"
            )
        }
    }
}
