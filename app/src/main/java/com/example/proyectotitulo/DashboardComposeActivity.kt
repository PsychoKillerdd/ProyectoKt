package com.example.proyectotitulo
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.proyectotitulo.api.HealthApiClient
import com.example.proyectotitulo.api.UserRequest
import com.example.proyectotitulo.ui.screens.DashboardScreen
import com.example.proyectotitulo.ui.screens.HealthData
import com.example.proyectotitulo.ui.theme.HealthTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.*
import java.util.*
class DashboardComposeActivity : ComponentActivity() {
    companion object {
        private const val TAG = "DashboardCompose"
    }
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var healthConnectClient: HealthConnectClient? = null
    
    private val healthPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class)
    )
    
    // Launcher para solicitar permisos de Health Connect
    private val requestPermissionsLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(healthPermissions)) {
            Log.d(TAG, "✅ Todos los permisos de Health Connect concedidos")
            Toast.makeText(this, "✅ Permisos concedidos", Toast.LENGTH_SHORT).show()
        } else {
            Log.w(TAG, "⚠️ Algunos permisos no fueron concedidos: ${healthPermissions - grantedPermissions}")
            Toast.makeText(this, "⚠️ Algunos permisos no fueron concedidos", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        // Verificar si Health Connect está disponible
        val availabilityStatus = HealthConnectClient.getSdkStatus(this)
        if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
            healthConnectClient = HealthConnectClient.getOrCreate(this)
            // Verificar y solicitar permisos al inicio
            checkAndRequestPermissions()
        } else {
            Log.w(TAG, "Health Connect no disponible. Status: $availabilityStatus")
        }
        
        setContent {
            HealthTrackTheme {
                var healthData by remember { mutableStateOf(HealthData()) }
                var heartRateChartData by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
                var sleepChartData by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
                var iaMessage by remember { mutableStateOf("📊 Presiona 'Analizar' para obtener recomendaciones personalizadas de IA.") }
                var isLoading by remember { mutableStateOf(false) }
                var isAnalyzing by remember { mutableStateOf(false) }
                
                // Cargar datos al iniciar
                LaunchedEffect(Unit) {
                    loadHealthDataFromFirebase { data, hrChart, sleepChart, ia ->
                        healthData = data
                        heartRateChartData = hrChart
                        sleepChartData = sleepChart
                        iaMessage = ia
                    }
                }
                
                DashboardScreen(
                    greeting = getGreeting(),
                    dateText = getCurrentDate(),
                    healthData = healthData,
                    heartRateChartData = heartRateChartData,
                    sleepChartData = sleepChartData,
                    iaMessage = iaMessage,
                    onSyncClick = {
                        isLoading = true
                        syncHealthData { data, hrChart, sleepChart ->
                            healthData = data
                            heartRateChartData = hrChart
                            sleepChartData = sleepChart
                            isLoading = false
                        }
                    },
                    onHistoryClick = {
                        startActivity(Intent(this, HistoryComposeActivity::class.java))
                    },
                    onLogoutClick = {
                        firebaseAuth.signOut()
                        startActivity(Intent(this, LoginComposeActivity::class.java))
                        finish()
                    },
                    onAnalyzeClick = {
                        isAnalyzing = true
                        analyzeWithIA { message ->
                            iaMessage = message
                            isAnalyzing = false
                        }
                    },
                    isLoading = isLoading,
                    isAnalyzing = isAnalyzing
                )
            }
        }
    }
    
    /**
     * Verifica y solicita permisos de Health Connect si es necesario
     */
    private fun checkAndRequestPermissions() {
        lifecycleScope.launch {
            try {
                val client = healthConnectClient ?: return@launch
                val granted = client.permissionController.getGrantedPermissions()
                
                if (!granted.containsAll(healthPermissions)) {
                    Log.d(TAG, "Solicitando permisos de Health Connect...")
                    requestPermissionsLauncher.launch(healthPermissions)
                } else {
                    Log.d(TAG, "Todos los permisos ya están concedidos")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error verificando permisos", e)
            }
        }
    }
    
    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Buenos días ☀️"
            hour < 19 -> "Buenas tardes 🌤️"
            else -> "Buenas noches 🌙"
        }
    }
    
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
        return dateFormat.format(Date())
    }
    
    private fun loadHealthDataFromFirebase(
        onResult: (HealthData, List<Pair<String, Float>>, List<Pair<String, Float>>, String) -> Unit
    ) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val today = LocalDate.now().toString()
        
        // Cargar último registro de salud
        firestore.collection("users").document(userId)
            .collection("health_records")
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                var healthData = HealthData()
                
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    healthData = HealthData(
                        steps = doc.getLong("pasosDiarios") ?: 0,
                        heartRate = doc.getLong("frecuenciaCardiaca") ?: 0,
                        heartRateMin = doc.getLong("frecuenciaCardiacaMin") ?: 0,
                        heartRateMax = doc.getLong("frecuenciaCardiacaMax") ?: 0,
                        sleepHours = doc.getDouble("horasDeSueno") ?: doc.getDouble("horasDeSueño") ?: 0.0,
                        spO2 = doc.getDouble("saturacionOxigeno") ?: 0.0,
                        stressLevel = (doc.getLong("nivelDeEstres") ?: 0).toInt(),
                        isWatchConnected = doc.getBoolean("relojColocado") ?: false
                    )
                }
                
                // Cargar datos para gráficos
                loadChartData(userId, today) { hrChart, sleepChart ->
                    // Cargar mensaje de IA
                    loadIaMessage(userId) { message ->
                        onResult(healthData, hrChart, sleepChart, message)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading health data", e)
                onResult(HealthData(), emptyList(), emptyList(), "Error al cargar datos")
            }
    }
    
    private fun loadChartData(
        userId: String,
        today: String,
        onResult: (List<Pair<String, Float>>, List<Pair<String, Float>>) -> Unit
    ) {
        // Cargar datos de frecuencia cardíaca de hoy
        firestore.collection("users").document(userId)
            .collection("health_records")
            .whereEqualTo("fecha", today)
            .get()
            .addOnSuccessListener { documents ->
                val hrData = mutableListOf<Pair<String, Float>>()
                
                for (doc in documents) {
                    val hora = doc.getString("horaRegistro") ?: continue
                    val hr = doc.getDouble("frecuenciaCardiaca")?.toFloat() ?: continue
                    if (hr > 0) hrData.add(hora to hr)
                }
                
                hrData.sortBy { it.first }
                
                // Cargar datos de sueño de últimos 5 días
                loadSleepChartData(userId) { sleepData ->
                    onResult(hrData, sleepData)
                }
            }
            .addOnFailureListener {
                onResult(emptyList(), emptyList())
            }
    }
    
    private fun loadSleepChartData(
        userId: String,
        onResult: (List<Pair<String, Float>>) -> Unit
    ) {
        val today = LocalDate.now()
        val dates = (4 downTo 0).map { today.minusDays(it.toLong()).toString() }
        val dayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        
        firestore.collection("users").document(userId)
            .collection("health_records")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { documents ->
                val sleepByDate = mutableMapOf<String, MutableList<Double>>()
                
                for (doc in documents) {
                    val fecha = doc.getString("fecha") ?: continue
                    val sleep = doc.getDouble("horasDeSueno") ?: doc.getDouble("horasDeSueño") ?: 0.0
                    
                    if (dates.contains(fecha) && sleep > 0) {
                        sleepByDate.getOrPut(fecha) { mutableListOf() }.add(sleep)
                    }
                }
                
                val result = dates.map { date ->
                    val dayOfWeek = LocalDate.parse(date).dayOfWeek.value
                    val label = dayNames.getOrElse(dayOfWeek - 1) { "?" }
                    val avg = sleepByDate[date]?.average()?.toFloat() ?: 0f
                    label to avg
                }
                
                onResult(result)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
    
    private fun loadIaMessage(userId: String, onResult: (String) -> Unit) {
        firestore.collection("users").document(userId)
            .collection("messagesIa")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val message = documents.documents[0].getString("message")
                        ?: "📊 Aún no hay análisis disponible."
                    onResult(message)
                } else {
                    onResult("📊 Sincroniza tus datos para recibir un análisis personalizado de tu salud.")
                }
            }
            .addOnFailureListener {
                onResult("📊 Error al cargar análisis de IA.")
            }
    }
    
    private fun syncHealthData(
        onResult: (HealthData, List<Pair<String, Float>>, List<Pair<String, Float>>) -> Unit
    ) {
        lifecycleScope.launch {
            try {
                val client = healthConnectClient
                if (client == null) {
                    Log.w(TAG, "Health Connect no disponible")
                    Toast.makeText(this@DashboardComposeActivity, "⚠️ Health Connect no disponible - Usando datos de prueba", Toast.LENGTH_LONG).show()
                    insertTestData()
                    kotlinx.coroutines.delay(1000)
                    loadHealthDataFromFirebase { data, hr, sleep, _ -> onResult(data, hr, sleep) }
                    return@launch
                }
                
                // Verificar permisos
                val granted = client.permissionController.getGrantedPermissions()
                Log.d(TAG, "Permisos concedidos: $granted")
                Log.d(TAG, "Permisos requeridos: $healthPermissions")
                
                if (!granted.containsAll(healthPermissions)) {
                    Log.w(TAG, "Faltan permisos. Solicitando...")
                    Toast.makeText(this@DashboardComposeActivity, "🔐 Solicitando permisos de Health Connect...", Toast.LENGTH_SHORT).show()
                    
                    // Solicitar permisos
                    requestPermissionsLauncher.launch(healthPermissions)
                    
                    // Mientras tanto, cargar datos existentes de Firebase
                    loadHealthDataFromFirebase { data, hr, sleep, _ -> onResult(data, hr, sleep) }
                    return@launch
                }
                
                // Leer datos de Health Connect
                val now = Instant.now()
                val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
                
                val timeRange = TimeRangeFilter.between(startOfDay, now)
                
                // Steps
                val stepsResponse = client.readRecords(
                    ReadRecordsRequest(StepsRecord::class, timeRange)
                )
                val totalSteps = stepsResponse.records.sumOf { it.count }
                
                // Heart Rate
                val hrResponse = client.readRecords(
                    ReadRecordsRequest(HeartRateRecord::class, timeRange)
                )
                val hrSamples = hrResponse.records.flatMap { it.samples }
                val avgHr = hrSamples.map { it.beatsPerMinute }.average().toLong()
                val minHr = hrSamples.minOfOrNull { it.beatsPerMinute } ?: 0
                val maxHr = hrSamples.maxOfOrNull { it.beatsPerMinute } ?: 0
                
                // SpO2
                val spo2Response = client.readRecords(
                    ReadRecordsRequest(OxygenSaturationRecord::class, timeRange)
                )
                val avgSpo2 = spo2Response.records.map { it.percentage.value }.average()
                
                // Sleep (last night)
                val sleepStart = LocalDate.now().minusDays(1).atTime(20, 0)
                    .atZone(ZoneId.systemDefault()).toInstant()
                val sleepResponse = client.readRecords(
                    ReadRecordsRequest(
                        SleepSessionRecord::class,
                        TimeRangeFilter.between(sleepStart, now)
                    )
                )
                val sleepHours = sleepResponse.records.sumOf {
                    Duration.between(it.startTime, it.endTime).toMinutes()
                } / 60.0
                
                val healthData = HealthData(
                    steps = totalSteps,
                    heartRate = avgHr,
                    heartRateMin = minHr,
                    heartRateMax = maxHr,
                    sleepHours = sleepHours,
                    spO2 = avgSpo2,
                    stressLevel = calculateStress(avgHr.toInt()),
                    isWatchConnected = true
                )
                
                // Guardar en Firebase
                saveHealthDataToFirebase(healthData)
                
                // Recargar datos de gráficos
                val userId = firebaseAuth.currentUser?.uid ?: return@launch
                loadChartData(userId, LocalDate.now().toString()) { hrChart, sleepChart ->
                    onResult(healthData, hrChart, sleepChart)
                }
                
                Toast.makeText(this@DashboardComposeActivity, "✅ Datos sincronizados", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing health data", e)
                Toast.makeText(this@DashboardComposeActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                loadHealthDataFromFirebase { data, hr, sleep, _ -> onResult(data, hr, sleep) }
            }
        }
    }
    
    private fun calculateStress(heartRate: Int): Int {
        return when {
            heartRate < 60 -> 20
            heartRate < 70 -> 30
            heartRate < 80 -> 45
            heartRate < 90 -> 60
            else -> 75
        }
    }
    
    private fun saveHealthDataToFirebase(data: HealthData) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val today = LocalDate.now().toString()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val record = hashMapOf(
            "pasosDiarios" to data.steps,
            "frecuenciaCardiaca" to data.heartRate,
            "frecuenciaCardiacaMin" to data.heartRateMin,
            "frecuenciaCardiacaMax" to data.heartRateMax,
            "horasDeSueno" to data.sleepHours,
            "saturacionOxigeno" to data.spO2,
            "nivelDeEstres" to data.stressLevel,
            "relojColocado" to data.isWatchConnected,
            "fecha" to today,
            "horaRegistro" to timeFormat.format(Date()),
            "lastUpdated" to Date()
        )
        
        val docId = "${today}_${System.currentTimeMillis()}"
        firestore.collection("users").document(userId)
            .collection("health_records").document(docId)
            .set(record)
            .addOnSuccessListener {
                Log.d(TAG, "Health data saved to Firebase")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving health data", e)
            }
    }
    
    /**
     * Llama a la API de análisis de IA
     */
    private fun analyzeWithIA(onResult: (String) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid
        
        if (userId == null) {
            onResult("❌ Error: Usuario no autenticado")
            return
        }
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Llamando API de análisis para usuario: $userId")
                
                val response = HealthApiClient.api.procesarUsuario(
                    UserRequest(user_id = userId)
                )
                
                Log.d(TAG, "Respuesta API - Estado: ${response.estado_general}")
                Log.d(TAG, "Mensaje: ${response.mensaje}")
                
                // Mostrar el mensaje de IA
                onResult(response.mensaje)
                
                Toast.makeText(
                    this@DashboardComposeActivity,
                    "✅ Análisis completado",
                    Toast.LENGTH_SHORT
                ).show()
                
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "Error HTTP ${e.code()}: $errorBody", e)
                
                val errorMessage = when (e.code()) {
                    404 -> "❌ No hay registros de salud para analizar. Sincroniza tus datos primero."
                    500 -> "❌ Error en el servidor. Intenta más tarde."
                    else -> "❌ Error: ${e.message()}"
                }
                onResult(errorMessage)
                
            } catch (e: java.net.UnknownHostException) {
                Log.e(TAG, "Error de conexión", e)
                onResult("❌ Sin conexión a internet. Verifica tu conexión.")
                
            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Timeout", e)
                onResult("❌ Tiempo de espera agotado. El servidor está tardando demasiado.")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado", e)
                onResult("❌ Error: ${e.message ?: "Error desconocido"}")
            }
        }
    }
    
    /**
     * Inserta datos de prueba para el día actual
     * Útil para testing cuando no hay reloj conectado
     */
    fun insertTestData() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val today = LocalDate.now().toString()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        // Datos de prueba realistas
        val testRecords = listOf(
            mapOf(
                "pasosDiarios" to 8542L,
                "frecuenciaCardiaca" to 72L,
                "frecuenciaCardiacaMin" to 58L,
                "frecuenciaCardiacaMax" to 95L,
                "horasDeSueño" to 7.5,
                "saturacionOxigeno" to 98.0,
                "nivelDeEstres" to 35,
                "relojColocado" to true,
                "fecha" to today,
                "horaRegistro" to "08:30",
                "lastUpdated" to Date()
            ),
            mapOf(
                "pasosDiarios" to 12350L,
                "frecuenciaCardiaca" to 78L,
                "frecuenciaCardiacaMin" to 62L,
                "frecuenciaCardiacaMax" to 110L,
                "horasDeSueño" to 7.5,
                "saturacionOxigeno" to 97.0,
                "nivelDeEstres" to 42,
                "relojColocado" to true,
                "fecha" to today,
                "horaRegistro" to "14:00",
                "lastUpdated" to Date()
            ),
            mapOf(
                "pasosDiarios" to 15230L,
                "frecuenciaCardiaca" to 68L,
                "frecuenciaCardiacaMin" to 55L,
                "frecuenciaCardiacaMax" to 105L,
                "horasDeSueño" to 7.5,
                "saturacionOxigeno" to 99.0,
                "nivelDeEstres" to 28,
                "relojColocado" to true,
                "fecha" to today,
                "horaRegistro" to timeFormat.format(Date()),
                "lastUpdated" to Date()
            )
        )
        
        testRecords.forEachIndexed { index, record ->
            val docId = "${today}_test_${System.currentTimeMillis()}_$index"
            firestore.collection("users").document(userId)
                .collection("health_records").document(docId)
                .set(record)
                .addOnSuccessListener {
                    Log.d(TAG, "Test data $index saved")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving test data $index", e)
                }
        }
        
        Toast.makeText(this, "✅ Datos de prueba insertados", Toast.LENGTH_SHORT).show()
    }
}
