package com.example.proyectotitulo

import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.lifecycleScope
import com.example.proyectotitulo.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Date
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Mass
import java.time.Duration

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val APP_TAG = "HealthConnectApp"
    
    // Handler para la recolección automática cada 30 minutos
    private val autoCollectHandler = Handler(Looper.getMainLooper())
    private val autoCollectInterval = 30 * 60 * 1000L // 30 minutos en milisegundos

    private val providerPackageName = "com.google.android.apps.healthdata"
    private val healthConnectClient: HealthConnectClient by lazy { HealthConnectClient.getOrCreate(this) }

    private val requestPermissions = registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        Log.d(APP_TAG, "--- PERMISSION RESULT ---")
        Log.d(APP_TAG, "Permissions returned by dialog: $granted")
        if (granted.containsAll(PERMISSIONS)) {
            Log.d(APP_TAG, "SUCCESS: All permissions were granted in this request.")
            readHealthData()
        } else {
            Log.d(APP_TAG, "FAILURE: Some or all permissions were denied by the user or system.")
            Toast.makeText(this, "Permissions denied. Cannot read health data.", Toast.LENGTH_LONG).show()
        }
    }

    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(HeightRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar edge-to-edge para evitar que los botones queden ocultos
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        checkAvailability()

        binding.buttonUpdate.setOnClickListener {
            Log.d(APP_TAG, "Update button clicked. Starting permission check...")
            checkPermissionsAndRun()
        }

        binding.buttonHistory.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de historial no implementada", Toast.LENGTH_SHORT).show()
        }

        binding.buttonLogout.setOnClickListener {
            autoCollectHandler.removeCallbacks(autoCollectRunnable)
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Cargar mensajes de ejemplo
        loadSampleMessages()
        
        // Iniciar recolección automática cada 30 minutos
        startAutoCollection()
    }

    private fun loadSampleMessages() {
        // Ejemplo de cómo agregar mensajes
        addMessage("📢 Bienvenido", "¡Bienvenido a tu dashboard de salud! Tus datos se actualizarán automáticamente cada 30 minutos. También puedes presionar 'Actualizar' para ver tus datos al instante.", "info")
    }

    private fun addMessage(title: String, message: String, type: String = "info") {
        val messagesContainer = binding.messagesContainer
        
        // Ocultar estado vacío
        binding.root.findViewById<LinearLayout>(R.id.emptyStateLayout)?.visibility = android.view.View.GONE

        // Crear card para el mensaje
        val messageCard = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(when(type) {
                "warning" -> android.graphics.Color.parseColor("#FFF3E0")
                "error" -> android.graphics.Color.parseColor("#FFEBEE")
                "success" -> android.graphics.Color.parseColor("#E8F5E9")
                else -> android.graphics.Color.parseColor("#F5F7FA")
            })
        }

        val messageLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(when(type) {
                "warning" -> android.graphics.Color.parseColor("#E65100")
                "error" -> android.graphics.Color.parseColor("#C62828")
                "success" -> android.graphics.Color.parseColor("#2E7D32")
                else -> android.graphics.Color.parseColor("#263238")
            })
        }

        val messageView = TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#546E7A"))
            setPadding(0, 8, 0, 0)
            setLineSpacing(4f, 1f)
        }

        val timeView = TextView(this).apply {
            text = "Ahora"
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#90A4AE"))
            setPadding(0, 8, 0, 0)
            gravity = Gravity.END
        }

        messageLayout.addView(titleView)
        messageLayout.addView(messageView)
        messageLayout.addView(timeView)
        messageCard.addView(messageLayout)
        messagesContainer.addView(messageCard, 0) // Agregar al principio
    }

    private fun checkAvailability() {
        val availabilityStatus = HealthConnectClient.getSdkStatus(this, providerPackageName)
        Log.d(APP_TAG, "Health Connect SDK Status: $availabilityStatus")
        
        when (availabilityStatus) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                Log.e(APP_TAG, "Health Connect is not installed or not available.")
                Toast.makeText(
                    this,
                    "Health Connect no está instalado. Por favor, instálalo desde Play Store.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                Log.w(APP_TAG, "Health Connect requires an update.")
                Toast.makeText(
                    this,
                    "Health Connect necesita una actualización.",
                    Toast.LENGTH_LONG
                ).show()
                val uriString = "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriString)))
                } catch (e: Exception) {
                    Log.e(APP_TAG, "Failed to open Play Store", e)
                }
                return
            }
            else -> {
                Log.d(APP_TAG, "Health Connect is available and ready.")
            }
        }
    }

    private fun checkPermissionsAndRun() {
        lifecycleScope.launch {
            try {
                Log.d(APP_TAG, "--- CHECKING PERMISSIONS ---")
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                Log.d(APP_TAG, "Current granted permissions: $granted")
                Log.d(APP_TAG, "Required permissions: $PERMISSIONS")

                val missingPermissions = PERMISSIONS - granted

                if (missingPermissions.isEmpty()) {
                    Log.d(APP_TAG, "All permissions already granted. Proceeding to read data.")
                    readHealthData()
                } else {
                    Log.w(APP_TAG, "Missing permissions found: $missingPermissions. Launching permission request...")
                    Log.d(APP_TAG, "Attempting to request ${missingPermissions.size} permissions")
                    
                    // Try to launch the permission request
                    try {
                        requestPermissions.launch(missingPermissions)
                        Log.d(APP_TAG, "Permission request launched successfully")
                    } catch (e: Exception) {
                        Log.e(APP_TAG, "Failed to launch permission request", e)
                        Toast.makeText(
                            this@DashboardActivity,
                            "Error al solicitar permisos: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(APP_TAG, "EXCEPTION while checking permissions: ", e)
                Toast.makeText(
                    this@DashboardActivity,
                    "Error verificando permisos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun readHealthData() {
        lifecycleScope.launch {
            val startTime = ZonedDateTime.now().toLocalDate().atStartOfDay(ZonedDateTime.now().zone).toInstant()
            val endTime = Instant.now()
            
            Log.d(APP_TAG, "Starting to read health data from $startTime to $endTime")
            
            // Leer todos los datos
            val steps = readSteps(startTime, endTime)
            val weight = readLatestWeight()
            val height = readLatestHeight()
            val sleepHours = readSleepHours(startTime, endTime)
            val heartRateData = readHeartRate(startTime, endTime)
            val screenTime = readScreenTime() // Nota: Esto requiere permisos adicionales del sistema
            
            // Determinar si el reloj está colocado basándose en la disponibilidad de datos de frecuencia cardíaca
            val watchWorn = heartRateData.isNotEmpty()
            
            // Calcular nivel de estrés basado en variabilidad de frecuencia cardíaca
            val stressLevel = calculateStressLevel(heartRateData)
            
            // Obtener hora y fecha actual
            val currentDateTime = ZonedDateTime.now()
            val timeOfDay = "${currentDateTime.hour.toString().padStart(2, '0')}:${currentDateTime.minute.toString().padStart(2, '0')}"
            val dateString = "${currentDateTime.year}-${currentDateTime.monthValue.toString().padStart(2, '0')}-${currentDateTime.dayOfMonth.toString().padStart(2, '0')}"
            
            // Crear objeto HealthData
            val healthData = HealthData(
                pasosDiarios = steps,
                horasDeSueño = sleepHours,
                tiempoPantalla = screenTime,
                frecuenciaCardiaca = heartRateData.getOrNull(0) ?: 0, // Promedio
                frecuenciaCardiacaMax = heartRateData.getOrNull(1) ?: 0,
                frecuenciaCardiacaMin = heartRateData.getOrNull(2) ?: 0,
                relojColocado = watchWorn,
                nivelDeEstres = stressLevel,
                horaRegistro = timeOfDay,
                fecha = dateString,
                peso = weight?.inKilograms ?: 0.0,
                altura = height?.inMeters ?: 0.0
            )
            
            Log.d(APP_TAG, "Health data collected: $healthData")
            
            // Actualizar UI
            val uiText = buildString {
                appendLine("📅 Fecha: ${healthData.fecha}")
                appendLine("🕐 Hora: ${healthData.horaRegistro}")
                appendLine("👟 Pasos: ${healthData.pasosDiarios}")
                appendLine("😴 Sueño: ${"%.1f".format(healthData.horasDeSueño)} horas")
                appendLine("📱 Tiempo Pantalla: ${"%.1f".format(healthData.tiempoPantalla)} horas")
                appendLine("❤️ Frecuencia Cardíaca: ${healthData.frecuenciaCardiaca} BPM")
                if (healthData.frecuenciaCardiacaMax > 0) {
                    appendLine("   Max: ${healthData.frecuenciaCardiacaMax} BPM")
                    appendLine("   Min: ${healthData.frecuenciaCardiacaMin} BPM")
                }
                appendLine("⌚ Reloj Colocado: ${if (healthData.relojColocado) "Sí" else "No"}")
                appendLine("😰 Nivel de Estrés: ${healthData.nivelDeEstres}/100")
                appendLine("⚖️ Peso: ${"%.1f".format(healthData.peso)} kg")
                appendLine("📏 Altura: ${"%.2f".format(healthData.altura)} m")
            }
            
            binding.textViewContentBody.text = uiText
            
            // Guardar en Firebase
            saveHealthDataToFirebase(healthData)
        }
    }

    private suspend fun readSteps(startTime: Instant, endTime: Instant): Long {
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return try {
            val response = healthConnectClient.readRecords(request)
            response.records.sumOf { it.count }
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error reading steps: ", e)
            0L
        }
    }

    private suspend fun readLatestWeight(): Mass? {
        val request = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.before(Instant.now()),
            ascendingOrder = false,
            pageSize = 1
        )
        return try {
            val response = healthConnectClient.readRecords(request)
            response.records.firstOrNull()?.weight
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error reading weight: ", e)
            null
        }
    }

    private suspend fun readLatestHeight(): Length? {
        val request = ReadRecordsRequest(
            recordType = HeightRecord::class,
            timeRangeFilter = TimeRangeFilter.before(Instant.now()),
            ascendingOrder = false,
            pageSize = 1
        )
        return try {
            val response = healthConnectClient.readRecords(request)
            response.records.firstOrNull()?.height
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error reading height: ", e)
            null
        }
    }

    private suspend fun readSleepHours(startTime: Instant, endTime: Instant): Double {
        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return try {
            val response = healthConnectClient.readRecords(request)
            val totalMinutes = response.records.sumOf { session ->
                Duration.between(session.startTime, session.endTime).toMinutes()
            }
            totalMinutes / 60.0
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error reading sleep: ", e)
            0.0
        }
    }

    private suspend fun readHeartRate(startTime: Instant, endTime: Instant): List<Long> {
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return try {
            val response = healthConnectClient.readRecords(request)
            if (response.records.isEmpty()) {
                return listOf(0, 0, 0) // avg, max, min
            }
            
            val allBpm = response.records.flatMap { record ->
                record.samples.map { it.beatsPerMinute }
            }
            
            if (allBpm.isEmpty()) {
                return listOf(0, 0, 0)
            }
            
            val avg = allBpm.average().toLong()
            val max = allBpm.maxOrNull() ?: 0
            val min = allBpm.minOrNull() ?: 0
            
            Log.d(APP_TAG, "Heart rate - Avg: $avg, Max: $max, Min: $min (${allBpm.size} samples)")
            listOf(avg, max, min)
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error reading heart rate: ", e)
            listOf(0, 0, 0)
        }
    }

    private fun readScreenTime(): Double {
        // Nota: El tiempo de pantalla no está disponible directamente en Health Connect
        // Se necesitaría acceso a UsageStatsManager del sistema Android
        // Por ahora, retornamos 0.0
        // Para implementar esto correctamente, necesitarías permisos de PACKAGE_USAGE_STATS
        Log.d(APP_TAG, "Screen time data not available from Health Connect")
        return 0.0
    }

    private fun calculateStressLevel(heartRateData: List<Long>): Int {
        if (heartRateData.isEmpty() || heartRateData[0] == 0L) {
            return 0
        }
        
        val avg = heartRateData[0]
        val max = heartRateData[1]
        val min = heartRateData[2]
        
        // Cálculo simple de nivel de estrés basado en variabilidad de frecuencia cardíaca
        // Una mayor variabilidad (diferencia entre max y min) puede indicar mejor salud cardiovascular
        // Una menor variabilidad o frecuencia cardíaca elevada puede indicar estrés
        
        val variability = if (max > min) max - min else 0
        
        // Normalizar el nivel de estrés (0-100)
        // Frecuencia cardíaca en reposo normal: 60-100 BPM
        // Mayor que 100 = más estrés
        val stressFromAvg = when {
            avg < 60 -> 10 // Muy bajo, posiblemente atleta
            avg in 60..80 -> 20 // Normal-bajo
            avg in 81..100 -> 40 // Normal
            avg in 101..120 -> 70 // Elevado
            else -> 90 // Muy elevado
        }
        
        // Ajustar según variabilidad
        // Mayor variabilidad = mejor (reduce estrés)
        val adjustedStress = when {
            variability > 40 -> (stressFromAvg * 0.7).toInt()
            variability > 20 -> (stressFromAvg * 0.85).toInt()
            else -> stressFromAvg
        }
        
        return adjustedStress.coerceIn(0, 100)
    }

    private fun saveHealthDataToFirebase(healthData: HealthData) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
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
            "lastUpdated" to Date()
        )
        
        Log.d(APP_TAG, "Saving health data to Firebase: $dataMap")

        firestore.collection("users").document(userId)
            .collection("daily_health_data").document(healthData.fecha)
            .set(dataMap)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Datos guardados exitosamente en Firebase", Toast.LENGTH_SHORT).show()
                Log.d(APP_TAG, "Health data saved successfully to Firebase")
                
                // Agregar mensaje de éxito
                addMessage(
                    "✅ Datos Actualizados",
                    "Tus datos de salud se han guardado correctamente en ${healthData.fecha} a las ${healthData.horaRegistro}",
                    "success"
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(APP_TAG, "Error saving to Firestore", e)
                
                addMessage(
                    "❌ Error al Guardar",
                    "No se pudieron guardar los datos: ${e.message}",
                    "error"
                )
            }
    }
    
    // --- Auto-Collection Functions ---
    
    private val autoCollectRunnable = object : Runnable {
        override fun run() {
            Log.d(APP_TAG, "Auto-collection: Starting automatic data collection...")
            checkPermissionsAndRun()
            autoCollectHandler.postDelayed(this, autoCollectInterval)
        }
    }
    
    private fun startAutoCollection() {
        Log.d(APP_TAG, "Starting auto-collection every ${autoCollectInterval / 1000 / 60} minutes")
        autoCollectHandler.postDelayed(autoCollectRunnable, autoCollectInterval)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        autoCollectHandler.removeCallbacks(autoCollectRunnable)
        Log.d(APP_TAG, "Auto-collection stopped")
    }
}
