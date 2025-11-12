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
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
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

        binding.buttonLogout.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Cargar mensajes de ejemplo
        loadSampleMessages()
        
        // Solicitar permisos de notificaciones (Android 13+)
        requestNotificationPermission()
        
        // Programar notificaciones cada 2 horas (8 AM - 12 AM)
        scheduleHealthReminders()
        
        // Cargar gráfico de sueño
        loadSleepChart()
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
        // Mensaje de bienvenida
        binding.textViewNotifications.text = "Bienvenido\n\nPresiona 'Actualizar' para recopilar tus datos de salud.\n\n📊 Recibirás recordatorios cada 2 horas (8 AM - 12 AM). La última notificación te recordará colocarte el reloj."
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
            
            binding.textViewHealthData.text = uiText
            
            // Guardar en Firebase (siempre guarda, incluso con valores 0)
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
                
                // Recargar gráfico de sueño
                loadSleepChart()
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
}

