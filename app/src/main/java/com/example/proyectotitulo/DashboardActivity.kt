package com.example.proyectotitulo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.proyectotitulo.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Date
import java.util.concurrent.TimeUnit
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Mass
import java.time.Duration
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.XAxis
import android.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.firestore.Query

class DashboardActivity : AppCompatActivity() {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
    }

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val APP_TAG = "HealthConnectApp"

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
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class)
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
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        
        // Long press en History para insertar datos de prueba
        binding.buttonHistory.setOnLongClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Datos de Prueba")
                .setMessage("¿Deseas insertar 20 registros de datos falsos pero realistas de los últimos 14 días?\n\nEsto es solo para pruebas.")
                .setPositiveButton("Sí, insertar") { _, _ ->
                    insertFakeHealthData()
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }

        binding.buttonLogout.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Cargar mensajes de ejemplo
        loadSampleMessages()
        
        // Inicializar UI Samsung
        initSamsungUI()
        
        // Solicitar permisos de notificaciones (Android 13+)
        requestNotificationPermission()
        
        // Programar notificaciones cada 2 horas (8 AM - 12 AM)
        scheduleHealthReminders()
        
        // Cargar gráfico de sueño
        loadSleepChart()
        
        // Cargar gráfico de frecuencia cardíaca
        loadHeartRateChart()
        
        // Cargar último mensaje de IA
        loadIaMessage()
    }
    
    private fun initSamsungUI() {
        // Saludo inicial según hora del día
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Buenos días ☀️"
            hour < 19 -> "Buenas tardes 🌤️"
            else -> "Buenas noches 🌙"
        }
        binding.textViewGreeting.text = greeting
        
        // Fecha actual
        val dateFormat = java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale("es", "ES"))
        binding.textViewDate.text = dateFormat.format(java.util.Date())
        
        // Estado inicial del reloj
        binding.textViewWatchStatus.text = "Presiona actualizar para sincronizar"
        binding.viewWatchIndicator.setBackgroundResource(R.drawable.circle_indicator_gray)
        
        Log.d(APP_TAG, "Samsung UI initialized")
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun scheduleHealthReminders() {
        val reminderRequest = PeriodicWorkRequestBuilder<HealthReminderWorker>(
            2, TimeUnit.HOURS  // Cada 2 horas
        )
        .setInitialDelay(1, TimeUnit.MINUTES)  // Esperar 1 minuto antes de la primera ejecución
        .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "health_reminders",
            ExistingPeriodicWorkPolicy.REPLACE,  // Reemplazar si ya existe para asegurar configuración actualizada
            reminderRequest
        )
        
        Log.d(APP_TAG, "Health reminders scheduled: Every 2 hours (8 AM - 12 AM)")
        Log.d(APP_TAG, "WorkManager periodic work enqueued with ID: health_reminders")
    }

    private fun loadSampleMessages() {
        // Mensaje de bienvenida para el diseño Samsung
        binding.textViewNotifications.text = "Toca el botón de sincronización para actualizar tus datos de salud. Recibirás recordatorios cada 2 horas."
    }

    private fun addMessage(title: String, message: String, type: String = "info") {
        // Actualizar el texto de notificaciones de forma simple
        val currentText = binding.textViewNotifications.text.toString()
        val newMessage = "$title\n$message\n\n"
        
        if (currentText.contains("Bienvenido") || currentText.contains("No hay")) {
            binding.textViewNotifications.text = newMessage
        } else {
            binding.textViewNotifications.text = "$newMessage$currentText"
        }
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
            val oxygenSaturation = readOxygenSaturation(startTime, endTime)
            
            // Extraer datos de frecuencia cardíaca
            val currentHeartRate = heartRateData.getOrNull(0) ?: 0
            val avgHeartRate = heartRateData.getOrNull(1) ?: 0
            val maxHeartRate = heartRateData.getOrNull(2) ?: 0
            val minHeartRate = heartRateData.getOrNull(3) ?: 0
            
            // Determinar si el reloj está colocado basándose en la disponibilidad de datos de frecuencia cardíaca
            val watchWorn = heartRateData.isNotEmpty() && currentHeartRate > 0
            
            // Calcular nivel de estrés basado en la frecuencia cardíaca actual
            val stressLevel = calculateStressLevel(listOf(currentHeartRate, avgHeartRate, maxHeartRate, minHeartRate))
            
            // Obtener hora y fecha actual
            val currentDateTime = ZonedDateTime.now()
            val timeOfDay = "${currentDateTime.hour.toString().padStart(2, '0')}:${currentDateTime.minute.toString().padStart(2, '0')}"
            val dateString = "${currentDateTime.year}-${currentDateTime.monthValue.toString().padStart(2, '0')}-${currentDateTime.dayOfMonth.toString().padStart(2, '0')}"
            
            // Crear objeto HealthData
            val healthData = HealthData(
                pasosDiarios = steps,
                horasDeSueño = sleepHours,
                saturacionOxigeno = oxygenSaturation,
                frecuenciaCardiaca = currentHeartRate, // Última medición (actual)
                frecuenciaCardiacaMax = maxHeartRate, // Max del día
                frecuenciaCardiacaMin = minHeartRate, // Min del día
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
                appendLine("═══════════════════════════════")
                appendLine("RESUMEN DE SALUD")
                appendLine("═══════════════════════════════")
                appendLine()
                appendLine("Fecha: ${healthData.fecha}")
                appendLine("Hora: ${healthData.horaRegistro}")
                appendLine()
                appendLine("ACTIVIDAD FÍSICA")
                appendLine("───────────────────────────────")
                appendLine("• Pasos: ${healthData.pasosDiarios}")
                appendLine()
                appendLine("SALUD VITAL")
                appendLine("───────────────────────────────")
                
                // Mostrar estado del reloj y frecuencia cardíaca
                if (healthData.frecuenciaCardiaca > 0) {
                    appendLine("• Frecuencia Cardíaca: ${healthData.frecuenciaCardiaca} BPM (actual)")
                    if (avgHeartRate > 0) {
                        appendLine("  - Promedio día: $avgHeartRate BPM")
                    }
                    if (healthData.frecuenciaCardiacaMax > 0) {
                        appendLine("  - Máxima: ${healthData.frecuenciaCardiacaMax} BPM")
                        appendLine("  - Mínima: ${healthData.frecuenciaCardiacaMin} BPM")
                    }
                } else {
                    appendLine("• Frecuencia Cardíaca: ⌚ Reloj no colocado")
                    appendLine("  - Coloca tu smartwatch para medir")
                }
                
                // Mostrar nivel de estrés
                if (healthData.nivelDeEstres > 0) {
                    appendLine("• Nivel de Estrés: ${healthData.nivelDeEstres}/100")
                } else {
                    appendLine("• Nivel de Estrés: -- (sin datos de RC)")
                }
                
                // Mostrar saturación de oxígeno
                if (healthData.saturacionOxigeno > 0) {
                    appendLine("• Saturación de Oxígeno: ${"%.1f".format(healthData.saturacionOxigeno)}%")
                } else {
                    appendLine("• Saturación de Oxígeno: -- (sin medición)")
                }
                
                appendLine()
                appendLine("DESCANSO")
                appendLine("───────────────────────────────")
                appendLine("• Sueño: ${"%.1f".format(healthData.horasDeSueño)} horas")
                appendLine()
                appendLine("═══════════════════════════════")
            }
            
            // Actualizar UI del nuevo diseño Samsung
            updateSamsungUI(healthData, avgHeartRate)
            
            // Guardar en Firebase (siempre guarda, incluso con valores 0)
            saveHealthDataToFirebase(healthData)
        }
    }
    
    private fun updateSamsungUI(healthData: HealthData, avgHeartRate: Long) {
        // Actualizar saludo según hora del día
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Buenos días ☀️"
            hour < 19 -> "Buenas tardes 🌤️"
            else -> "Buenas noches 🌙"
        }
        binding.textViewGreeting.text = greeting
        
        // Actualizar fecha
        val dateFormat = java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale("es", "ES"))
        binding.textViewDate.text = dateFormat.format(java.util.Date())
        
        // Actualizar tarjetas de métricas
        // Pasos
        binding.textViewSteps.text = if (healthData.pasosDiarios > 0) {
            java.text.NumberFormat.getNumberInstance().format(healthData.pasosDiarios)
        } else "--"
        
        // Frecuencia cardíaca
        binding.textViewHeartRate.text = if (healthData.frecuenciaCardiaca > 0) {
            healthData.frecuenciaCardiaca.toString()
        } else "--"
        
        // Estado del ritmo cardíaco
        binding.textViewHeartRateStatus.text = when {
            healthData.frecuenciaCardiaca <= 0 -> "Sin datos"
            healthData.frecuenciaCardiaca < 60 -> "Bajo"
            healthData.frecuenciaCardiaca <= 100 -> "Normal"
            else -> "Elevado"
        }
        
        // Sueño
        binding.textViewSleep.text = if (healthData.horasDeSueño > 0) {
            "%.1f".format(healthData.horasDeSueño)
        } else "--"
        
        binding.textViewSleepStatus.text = when {
            healthData.horasDeSueño <= 0 -> "Sin datos"
            healthData.horasDeSueño < 6 -> "Poco sueño"
            healthData.horasDeSueño <= 8 -> "Buen descanso"
            else -> "Excelente"
        }
        
        // SpO2
        binding.textViewSpO2.text = if (healthData.saturacionOxigeno > 0) {
            "%.0f".format(healthData.saturacionOxigeno)
        } else "--"
        
        binding.textViewSpO2Status.text = when {
            healthData.saturacionOxigeno <= 0 -> "Sin datos"
            healthData.saturacionOxigeno < 95 -> "Bajo ⚠️"
            else -> "Normal"
        }
        
        // Estrés
        binding.textViewStress.text = if (healthData.nivelDeEstres > 0) {
            healthData.nivelDeEstres.toString()
        } else "--"
        
        binding.textViewStressLabel.text = when {
            healthData.nivelDeEstres <= 0 -> "Sin datos"
            healthData.nivelDeEstres <= 30 -> "Relajado 😌"
            healthData.nivelDeEstres <= 60 -> "Normal 🙂"
            healthData.nivelDeEstres <= 80 -> "Elevado 😐"
            else -> "Alto 😰"
        }
        
        // Estado del reloj
        if (healthData.relojColocado) {
            binding.textViewWatchStatus.text = "Reloj conectado y sincronizado"
            binding.viewWatchIndicator.setBackgroundResource(R.drawable.circle_indicator_green)
        } else {
            binding.textViewWatchStatus.text = "Coloca tu reloj para obtener datos"
            binding.viewWatchIndicator.setBackgroundResource(R.drawable.circle_indicator_red)
        }
        
        // Última actualización
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        binding.textViewLastUpdate.text = "Última actualización: ${timeFormat.format(java.util.Date())}"
        
        Log.d(APP_TAG, "Samsung UI updated successfully")
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
                return listOf(0, 0, 0, 0) // current, avg, max, min
            }
            
            val allBpm = response.records.flatMap { record ->
                record.samples.map { it.beatsPerMinute }
            }
            
            if (allBpm.isEmpty()) {
                return listOf(0, 0, 0, 0)
            }
            
            // Última medición (más reciente)
            val current = allBpm.lastOrNull() ?: 0
            val avg = allBpm.average().toLong()
            val max = allBpm.maxOrNull() ?: 0
            val min = allBpm.minOrNull() ?: 0
            
            Log.d(APP_TAG, "Heart rate - Current: $current, Avg: $avg, Max: $max, Min: $min (${allBpm.size} samples)")
            listOf(current, avg, max, min)
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error reading heart rate: ", e)
            listOf(0, 0, 0, 0)
        }
    }

    private suspend fun readOxygenSaturation(startTime: Instant, endTime: Instant): Double {
        val request = ReadRecordsRequest(
            recordType = OxygenSaturationRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return try {
            val response = healthConnectClient.readRecords(request)
            if (response.records.isNotEmpty()) {
                // Obtener la última lectura
                val lastReading = response.records.last()
                val spo2 = lastReading.percentage.value
                Log.d(APP_TAG, "Oxygen saturation: $spo2% (${response.records.size} samples)")
                spo2
            } else {
                Log.d(APP_TAG, "No oxygen saturation data available")
                0.0
            }
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error reading oxygen saturation: ", e)
            0.0
        }
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
        
        // Validar que al menos un dato sea diferente de 0
        val hasValidData = healthData.pasosDiarios > 0 ||
                          healthData.horasDeSueño > 0.0 ||
                          healthData.saturacionOxigeno > 0.0 ||
                          healthData.frecuenciaCardiaca > 0 ||
                          healthData.peso > 0.0 ||
                          healthData.altura > 0.0
        
        if (!hasValidData) {
            Toast.makeText(
                this, 
                "⚠️ No se guardaron los datos: Todos los valores son 0", 
                Toast.LENGTH_LONG
            ).show()
            Log.d(APP_TAG, "Data not saved: All values are 0")
            
            addMessage(
                "Datos No Guardados",
                "No se registraron datos porque todos los valores son 0. Verifica que el reloj esté conectado y con datos disponibles.",
                "warning"
            )
            return
        }
        
        val dataMap = mapOf(
            "pasosDiarios" to healthData.pasosDiarios,
            "horasDeSueño" to healthData.horasDeSueño,
            "saturacionOxigeno" to healthData.saturacionOxigeno,
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

        // Usar fecha + timestamp para ID único (permite múltiples registros por día)
        val documentId = "${healthData.fecha}_${System.currentTimeMillis()}"
        
        firestore.collection("users").document(userId)
            .collection("health_records").document(documentId)
            .set(dataMap)
            .addOnSuccessListener {
                // Mensaje diferente según si el reloj estaba colocado
                val message = if (healthData.relojColocado) {
                    "Datos guardados: RC ${healthData.frecuenciaCardiaca} BPM ✓"
                } else {
                    "Datos guardados (⌚ Reloj no colocado - RC: 0)"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                Log.d(APP_TAG, "Health data saved successfully to Firebase with ID: $documentId")
                
                // Agregar mensaje de éxito con detalle
                val detailMessage = if (healthData.relojColocado) {
                    "Datos guardados correctamente en ${healthData.fecha} a las ${healthData.horaRegistro}"
                } else {
                    "Datos guardados en ${healthData.fecha} a las ${healthData.horaRegistro}. Nota: Reloj no colocado (FC: 0 BPM)"
                }
                
                addMessage(
                    "Datos Actualizados",
                    detailMessage,
                    "success"
                )
                
                // Recargar gráficos
                loadSleepChart()
                loadHeartRateChart()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(APP_TAG, "Error saving to Firestore", e)
                
                addMessage(
                    "Error al Guardar",
                    "No se pudieron guardar los datos: ${e.message}",
                    "error"
                )
            }
    }
    
    // --- Sleep Chart Functions ---
    
    private fun loadSleepChart() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        // Calcular las fechas de los últimos 5 días
        val today = LocalDate.now()
        val dates = mutableListOf<String>()
        val dateLabels = mutableListOf<String>()
        
        for (i in 4 downTo 0) {
            val date = today.minusDays(i.toLong())
            dates.add(date.toString())
            
            // Formatear etiqueta (Ej: "Lun 10")
            val dayOfWeek = when(date.dayOfWeek.value) {
                1 -> "Lun"
                2 -> "Mar"
                3 -> "Mié"
                4 -> "Jue"
                5 -> "Vie"
                6 -> "Sáb"
                7 -> "Dom"
                else -> ""
            }
            dateLabels.add("$dayOfWeek ${date.dayOfMonth}")
        }
        
        Log.d(APP_TAG, "Loading sleep chart for dates: $dates")
        
        // Consultar Firebase para obtener datos de sueño de los últimos 5 días
        firestore.collection("users").document(userId)
            .collection("health_records")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(100) // Obtener últimos 100 registros para asegurar que cubrimos 5 días
            .get()
            .addOnSuccessListener { documents ->
                Log.d(APP_TAG, "Fetched ${documents.size()} documents from Firebase")
                
                // Agrupar por fecha y calcular promedio de horas de sueño
                val sleepByDate = mutableMapOf<String, MutableList<Double>>()
                
                for (document in documents) {
                    val fecha = document.getString("fecha") ?: continue
                    val horasSueno = document.getDouble("horasDeSueño") ?: 0.0
                    
                    Log.d(APP_TAG, "Document: fecha=$fecha, horasDeSueño=$horasSueno")
                    
                    if (dates.contains(fecha) && horasSueno > 0.0) {
                        if (!sleepByDate.containsKey(fecha)) {
                            sleepByDate[fecha] = mutableListOf()
                        }
                        sleepByDate[fecha]?.add(horasSueno)
                    }
                }
                
                Log.d(APP_TAG, "Grouped sleep data: $sleepByDate")
                
                // Crear entradas para el gráfico
                val entries = mutableListOf<BarEntry>()
                
                for ((index, date) in dates.withIndex()) {
                    val sleepValues = sleepByDate[date]
                    val sleepHours = if (sleepValues != null && sleepValues.isNotEmpty()) {
                        sleepValues.average().toFloat()
                    } else {
                        0f
                    }
                    entries.add(BarEntry(index.toFloat(), sleepHours))
                    Log.d(APP_TAG, "Chart entry - Date: $date (${dateLabels[index]}), Sleep: $sleepHours hours")
                }
                
                // Configurar gráfico
                setupBarChart(entries, dateLabels)
            }
            .addOnFailureListener { e ->
                Log.e(APP_TAG, "Error loading sleep data for chart", e)
                // Mostrar gráfico vacío
                setupBarChart(listOf(
                    BarEntry(0f, 0f),
                    BarEntry(1f, 0f),
                    BarEntry(2f, 0f),
                    BarEntry(3f, 0f),
                    BarEntry(4f, 0f)
                ), dateLabels)
            }
    }
    
    private fun setupBarChart(entries: List<BarEntry>, labels: List<String>) {
        val barChart = binding.sleepBarChart
        
        // Crear dataset
        val dataSet = BarDataSet(entries, "Horas de Sueño")
        dataSet.color = Color.parseColor("#1877F2") // Azul de Facebook
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f
        
        // ValueFormatter personalizado para mostrar "No registrado" cuando el valor sea 0
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value == 0f) {
                    "Sin datos"
                } else {
                    String.format("%.1fh", value)
                }
            }
        }
        
        // Crear BarData
        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        
        // Configurar el gráfico
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
        barChart.legend.textSize = 12f
        
        // Configurar eje X (fechas)
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 11f
        
        // Configurar eje Y izquierdo (horas)
        val leftAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 12f // Máximo 12 horas
        leftAxis.granularity = 2f
        leftAxis.textSize = 11f
        leftAxis.setDrawGridLines(true)
        
        // Deshabilitar eje Y derecho
        barChart.axisRight.isEnabled = false
        
        // Configuraciones adicionales
        barChart.setFitBars(true)
        barChart.animateY(1000)
        barChart.invalidate() // Refrescar el gráfico
        
        Log.d(APP_TAG, "Sleep chart configured with ${entries.size} entries")
    }
    
    // --- Heart Rate Chart Functions ---
    
    private fun loadHeartRateChart() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        // Obtener fecha de hoy
        val today = LocalDate.now().toString()
        
        Log.d(APP_TAG, "Loading heart rate chart for today: $today")
        
        // Consultar Firebase para obtener todos los registros de hoy
        firestore.collection("users").document(userId)
            .collection("health_records")
            .whereEqualTo("fecha", today)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(APP_TAG, "Fetched ${documents.size()} documents for today's heart rate chart")
                
                // Crear lista de mediciones con hora y frecuencia cardíaca
                val heartRateData = mutableListOf<Pair<String, Double>>()
                
                for (document in documents) {
                    val horaRegistro = document.getString("horaRegistro") ?: continue
                    val heartRate = document.getDouble("frecuenciaCardiaca") ?: 0.0
                    
                    if (heartRate > 0.0) {
                        heartRateData.add(Pair(horaRegistro, heartRate))
                        Log.d(APP_TAG, "Heart rate at $horaRegistro: $heartRate bpm")
                    }
                }
                
                // Ordenar por hora
                heartRateData.sortBy { it.first }
                
                Log.d(APP_TAG, "Sorted heart rate data: ${heartRateData.size} entries")
                
                if (heartRateData.isEmpty()) {
                    // Si no hay datos hoy, mostrar gráfico vacío con mensaje
                    setupLineChart(listOf(Entry(0f, 0f)), listOf("Sin datos hoy"))
                    return@addOnSuccessListener
                }
                
                // Crear entradas para el gráfico
                val entries = mutableListOf<Entry>()
                val timeLabels = mutableListOf<String>()
                
                for ((index, data) in heartRateData.withIndex()) {
                    entries.add(Entry(index.toFloat(), data.second.toFloat()))
                    timeLabels.add(data.first)  // Hora como etiqueta (ej: "08:30")
                }
                
                Log.d(APP_TAG, "Chart entries created: ${entries.size}")
                
                // Configurar gráfico
                setupLineChart(entries, timeLabels)
            }
            .addOnFailureListener { e ->
                Log.e(APP_TAG, "Error loading heart rate data for chart", e)
                // Mostrar gráfico vacío
                setupLineChart(listOf(Entry(0f, 0f)), listOf("Error"))
            }
    }
    
    private fun setupLineChart(entries: List<Entry>, labels: List<String>) {
        val lineChart = binding.heartRateChart
        
        // Crear dataset
        val dataSet = LineDataSet(entries, "Frecuencia Cardíaca (bpm)")
        dataSet.color = Color.parseColor("#E53935") // Rojo
        dataSet.setCircleColor(Color.parseColor("#E53935"))
        dataSet.lineWidth = 2.5f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(true)
        dataSet.circleHoleRadius = 2f
        dataSet.setDrawValues(false) // No mostrar valores encima de la línea
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Línea suave
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#FFCDD2") // Rojo claro para relleno
        dataSet.fillAlpha = 100
        
        // Crear LineData
        val lineData = LineData(dataSet)
        
        // Configurar el gráfico
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.legend.textSize = 12f
        
        // Configurar eje X (fechas)
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 11f
        
        // Configurar eje Y izquierdo (bpm)
        val leftAxis = lineChart.axisLeft
        leftAxis.axisMinimum = 40f // Mínimo 40 bpm
        leftAxis.axisMaximum = 150f // Máximo 150 bpm
        leftAxis.granularity = 20f
        leftAxis.textSize = 11f
        leftAxis.setDrawGridLines(true)
        
        // Deshabilitar eje Y derecho
        lineChart.axisRight.isEnabled = false
        
        // Configuraciones adicionales
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(false)
        lineChart.animateX(1000)
        lineChart.invalidate() // Refrescar el gráfico
        
        Log.d(APP_TAG, "Heart rate chart configured with ${entries.size} entries")
    }
    
    // --- IA Message Functions ---
    
    private fun loadIaMessage() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.e(APP_TAG, "User not authenticated, cannot load IA message")
            binding.textViewIaMessage.text = "Inicia sesión para ver el análisis de IA"
            return
        }
        
        Log.d(APP_TAG, "Loading last IA message for user: $userId")
        
        // OPCIÓN 1: Leer desde users/{userId}/messagesIa (subcolección del usuario)
        firestore.collection("users").document(userId)
            .collection("messagesIa")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(APP_TAG, "No IA messages found in users/$userId/messagesIa")
                    
                    // OPCIÓN 2: Si no hay en subcolección, intentar desde raíz messagesIa
                    tryLoadFromRootCollection(userId)
                } else {
                    displayIaMessage(documents.documents[0])
                }
            }
            .addOnFailureListener { e ->
                Log.e(APP_TAG, "Error loading from subcollection: ${e.message}")
                // Si falla la subcolección, intentar desde raíz
                tryLoadFromRootCollection(userId)
            }
    }
    
    private fun tryLoadFromRootCollection(userId: String) {
        Log.d(APP_TAG, "Trying to load from root messagesIa collection")
        
        firestore.collection("messagesIa")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(APP_TAG, "No IA messages found for userId: $userId")
                    binding.textViewIaMessage.text = "📊 Aún no hay análisis disponible. Los mensajes de IA aparecerán aquí cuando estén listos."
                } else {
                    // Ordenar por timestamp en el cliente
                    val sortedDocs = documents.sortedByDescending { 
                        it.getTimestamp("timestamp")?.toDate()?.time ?: 0 
                    }
                    
                    Log.d(APP_TAG, "Found ${documents.size()} messages, displaying most recent")
                    sortedDocs.firstOrNull()?.let { displayIaMessage(it) }
                }
            }
            .addOnFailureListener { e ->
                Log.e(APP_TAG, "Error loading from root collection: ${e.message}", e)
                binding.textViewIaMessage.text = "⚠️ Error al cargar el análisis de IA: ${e.message}"
            }
    }
    
    private fun displayIaMessage(document: com.google.firebase.firestore.DocumentSnapshot) {
        val message = document.getString("mensaje") ?: document.getString("message") ?: "Sin mensaje"
        val timestamp = document.getTimestamp("timestamp")
        
        Log.d(APP_TAG, "IA message loaded successfully")
        Log.d(APP_TAG, "Document ID: ${document.id}")
        Log.d(APP_TAG, "Message length: ${message.length} characters")
        Log.d(APP_TAG, "Message preview: ${message.take(100)}...")
        
        // Formatear el mensaje con timestamp si existe
        val displayMessage = if (timestamp != null) {
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            "📅 ${dateFormat.format(timestamp.toDate())}\n\n$message"
        } else {
            message
        }
        
        binding.textViewIaMessage.text = displayMessage
    }
    
    /**
     * Inserta 20 registros por día durante los últimos 14 días (280 registros totales)
     * Simula los datos de una persona de 1.81m con variaciones naturales a diferentes horas
     */
    private fun insertFakeHealthData() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        Log.d(APP_TAG, "Starting to insert fake health data for user: $userId")
        Toast.makeText(this, "⏳ Insertando 280 registros (20/día x 14 días)...", Toast.LENGTH_LONG).show()
        
        val calendar = java.util.Calendar.getInstance()
        val random = java.util.Random()
        
        // Generar 20 registros por día durante 14 días = 280 registros
        val recordsToInsert = mutableListOf<Map<String, Any>>()
        val totalDays = 14
        val recordsPerDay = 20
        val totalRecords = totalDays * recordsPerDay
        
        // Datos base: persona de 1.81m
        val altura = 1.81
        val pesoBase = 75.0  // Peso base que variará ligeramente
        
        for (dayOffset in 0 until totalDays) {
            // Configurar fecha base para este día
            val baseCalendar = java.util.Calendar.getInstance()
            baseCalendar.add(java.util.Calendar.DAY_OF_YEAR, -dayOffset)
            
            val isWeekend = baseCalendar.get(java.util.Calendar.DAY_OF_WEEK) in listOf(
                java.util.Calendar.SATURDAY, 
                java.util.Calendar.SUNDAY
            )
            
            // Generar horas distribuidas a lo largo del día (6:00 - 23:00)
            val hoursOfDay = mutableListOf<Int>()
            for (h in 6..23) {
                hoursOfDay.add(h)
            }
            hoursOfDay.shuffle(random)
            val selectedHours = hoursOfDay.take(recordsPerDay).sorted()
            
            // Pasos acumulados durante el día
            var accumulatedSteps = 0
            val dailyStepGoal = if (isWeekend) random.nextInt(4000) + 5000 else random.nextInt(5000) + 7000
            
            for ((recordIndex, hour) in selectedHours.withIndex()) {
                calendar.time = baseCalendar.time
                calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                calendar.set(java.util.Calendar.MINUTE, random.nextInt(60))
                calendar.set(java.util.Calendar.SECOND, random.nextInt(60))
                
                // Pasos: acumulados durante el día con más actividad en ciertas horas
                val stepIncrement = when {
                    hour in 7..9 -> random.nextInt(1500) + 500   // Mañana activa
                    hour in 12..14 -> random.nextInt(1000) + 300  // Almuerzo
                    hour in 17..20 -> random.nextInt(2000) + 800  // Tarde/ejercicio
                    else -> random.nextInt(500) + 100             // Resto del día
                }
                accumulatedSteps += stepIncrement
                val steps = minOf(accumulatedSteps, dailyStepGoal + random.nextInt(2000))
                
                // Frecuencia cardíaca: varía según hora y actividad
                val baseHR = when {
                    hour in 6..7 -> 58 + random.nextInt(8)     // Recién despierto
                    hour in 8..11 -> 68 + random.nextInt(15)   // Mañana activa
                    hour in 12..14 -> 72 + random.nextInt(12)  // Post-almuerzo
                    hour in 15..17 -> 70 + random.nextInt(10)  // Tarde tranquila
                    hour in 18..20 -> 75 + random.nextInt(25)  // Ejercicio/actividad
                    hour in 21..23 -> 62 + random.nextInt(10)  // Relajación nocturna
                    else -> 65 + random.nextInt(10)
                }
                val heartRate = baseHR
                val heartRateMax = heartRate + random.nextInt(25) + 15
                val heartRateMin = (heartRate - random.nextInt(12) - 8).coerceAtLeast(52)
                
                // Sueño: solo tiene valor significativo en registros de la mañana
                val sleepHours = if (hour in 6..10 && recordIndex < 3) {
                    5.5 + random.nextDouble() * 3.0  // 5.5 - 8.5 horas
                } else {
                    0.0  // Sin dato de sueño en otros momentos
                }
                
                // SpO2: normalmente entre 95-99%, ligeramente menor si muy activo
                val baseSpo2 = if (hour in 18..20 && random.nextBoolean()) 95.0 else 96.0
                val spo2 = baseSpo2 + random.nextDouble() * 3.0
                
                // Estrés: basado en hora del día y si es fin de semana
                val baseStress = when {
                    isWeekend && hour in 10..20 -> 20 + random.nextInt(25)  // Relax fin de semana
                    hour in 9..12 -> 35 + random.nextInt(30)                // Mañana laboral
                    hour in 14..17 -> 40 + random.nextInt(35)               // Tarde laboral
                    hour in 18..20 -> 30 + random.nextInt(25)               // Post-trabajo
                    else -> 20 + random.nextInt(20)                          // Noche/mañana temprano
                }
                val stress = baseStress.coerceIn(10, 85)
                
                // Peso: varía ligeramente durante el día (±0.5kg)
                val peso = pesoBase + (random.nextDouble() - 0.5)
                
                // Formato de fecha y hora
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                
                val dataMap = mapOf(
                    "pasosDiarios" to steps.toLong(),
                    "horasDeSueño" to if (sleepHours > 0) String.format("%.1f", sleepHours).replace(",", ".").toDouble() else 0.0,
                    "saturacionOxigeno" to String.format("%.1f", spo2).replace(",", ".").toDouble(),
                    "frecuenciaCardiaca" to heartRate.toLong(),
                    "frecuenciaCardiacaMax" to heartRateMax.toLong(),
                    "frecuenciaCardiacaMin" to heartRateMin.toLong(),
                    "relojColocado" to true,
                    "nivelDeEstres" to stress,
                    "horaRegistro" to timeFormat.format(calendar.time),
                    "fecha" to dateFormat.format(calendar.time),
                    "peso" to String.format("%.1f", peso).replace(",", ".").toDouble(),
                    "altura" to altura,
                    "lastUpdated" to calendar.time
                )
                
                recordsToInsert.add(dataMap)
            }
        }
        
        Log.d(APP_TAG, "Generated ${recordsToInsert.size} fake records to insert")
        
        // Insertar todos los registros en Firebase
        var successCount = 0
        var errorCount = 0
        
        for ((index, dataMap) in recordsToInsert.withIndex()) {
            val fecha = dataMap["fecha"] as String
            val horaRegistro = dataMap["horaRegistro"] as String
            val timestamp = (dataMap["lastUpdated"] as java.util.Date).time
            val documentId = "${fecha}_${horaRegistro.replace(":", "")}_${timestamp}_fake"
            
            firestore.collection("users").document(userId)
                .collection("health_records").document(documentId)
                .set(dataMap)
                .addOnSuccessListener {
                    successCount++
                    if (successCount % 50 == 0) {
                        Log.d(APP_TAG, "Progress: $successCount/$totalRecords records inserted")
                    }
                    
                    if (successCount + errorCount == totalRecords) {
                        Toast.makeText(
                            this,
                            "✅ $successCount registros insertados (14 días x 20/día)",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Recargar gráficos y UI
                        loadSleepChart()
                        loadHeartRateChart()
                    }
                }
                .addOnFailureListener { e ->
                    errorCount++
                    Log.e(APP_TAG, "Error inserting record ${index + 1}: ${e.message}")
                    
                    if (successCount + errorCount == totalRecords) {
                        Toast.makeText(
                            this,
                            "⚠️ Insertados: $successCount exitosos, $errorCount errores",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        loadSleepChart()
                        loadHeartRateChart()
                    }
                }
        }
        
        Log.d(APP_TAG, "Started inserting $totalRecords fake health records")
    }
}

