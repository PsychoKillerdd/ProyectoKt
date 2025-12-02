# 📚 API de Predicción de Salud - Documentación

## 🌐 Información General

| Campo | Valor |
|-------|-------|
| **URL Base** | `https://health-api-409458732489.us-central1.run.app` |
| **Swagger UI** | `https://health-api-409458732489.us-central1.run.app/docs` |
| **ReDoc** | `https://health-api-409458732489.us-central1.run.app/redoc` |
| **Formato** | JSON |
| **Autenticación** | No requerida (público) |

---

## 📍 Endpoints Disponibles

### 1. Health Check

Verifica que el servidor esté funcionando.

```
GET /
```

#### Response
```json
{
  "status": "ok",
  "message": "Servidor en ejecución correctamente"
}
```

---

### 2. Procesar Usuario

Analiza los datos de salud del usuario y genera un mensaje personalizado con IA.

```
POST /procesar_usuario
```

#### Headers
| Header | Valor | Requerido |
|--------|-------|-----------|
| `Content-Type` | `application/json` | ✅ Sí |

#### Request Body

```json
{
  "user_id": "string"
}
```

| Campo | Tipo | Descripción | Requerido |
|-------|------|-------------|-----------|
| `user_id` | `string` | UID del usuario de Firebase Auth | ✅ Sí |

#### Ejemplo de Request
```json
{
  "user_id": "6VsRDCu3wldPe7o0Hm9iMAdnnUa2"
}
```

---

#### Response (Éxito - 200)

```json
{
  "estado_general": "string",
  "detalles": {
    "sueño": "string",
    "ritmo_cardiaco": "string",
    "estres": "string",
    "pasos": "string"
  },
  "mensaje": "string"
}
```

| Campo | Tipo | Descripción | Valores Posibles |
|-------|------|-------------|------------------|
| `estado_general` | `string` | Evaluación general de salud | `"Saludable"`, `"No saludable"` |
| `detalles.sueño` | `string` | Estado del sueño | `"adecuado"`, `"insuficiente"` |
| `detalles.ritmo_cardiaco` | `string` | Estado cardíaco | `"normal"`, `"irregular"` |
| `detalles.estres` | `string` | Nivel de estrés | `"controlado"`, `"alto"` |
| `detalles.pasos` | `string` | Actividad física | `"suficiente"`, `"bajo"` |
| `mensaje` | `string` | Mensaje personalizado generado por IA (Gemini) | Texto motivacional |

#### Ejemplo de Response Exitosa
```json
{
  "estado_general": "Saludable",
  "detalles": {
    "sueño": "insuficiente",
    "ritmo_cardiaco": "normal",
    "estres": "controlado",
    "pasos": "suficiente"
  },
  "mensaje": "¡Hola, Vicente! 👋\n\n**¡Día Activo y Enfocado!**\nTu jornada muestra un gran compromiso: ¡13,274 pasos es una actividad excelente! 💪 Tu ritmo cardíaco y estrés están controlados.\n\n**El Descanso es Clave**\nNotamos que tus horas de sueño fueron insuficientes. 😴 Prioriza un buen descanso esta noche.\n\n📅 Análisis del 2025-11-24 a las 23:16"
}
```

---

#### Response (Error - Usuario sin registros)

```json
{
  "error": "No se encontró ningún registro para este usuario"
}
```

#### Response (Error - Campos faltantes)

```json
{
  "error": "Faltan campos requeridos en el registro: frecuenciaCardiaca, horasDeSueño",
  "registro": { ... }
}
```

#### Response (Error - Modelos no cargados)

```json
{
  "error": "Modelos no cargados. Ejecuta primero el pipeline de entrenamiento.",
  "instrucciones": "python clean_dataset.py && python augment_data.py && python training/train_models.py"
}
```

---

## 📱 Integración con Android (Kotlin + Retrofit)

### 1. Agregar dependencias en `build.gradle`

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

### 2. Crear modelos de datos

```kotlin
// Request
data class UserRequest(
    val user_id: String
)

// Response
data class HealthResponse(
    val estado_general: String,
    val detalles: Detalles,
    val mensaje: String
)

data class Detalles(
    val sueño: String,
    val ritmo_cardiaco: String,
    val estres: String,
    val pasos: String
)

// Error Response
data class ErrorResponse(
    val error: String
)
```

### 3. Crear interfaz del API

```kotlin
interface HealthApiService {
    
    @GET("/")
    suspend fun healthCheck(): Map<String, String>
    
    @POST("/procesar_usuario")
    suspend fun procesarUsuario(
        @Body request: UserRequest
    ): HealthResponse
}
```

### 4. Configurar Retrofit

```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://health-api-409458732489.us-central1.run.app/"
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val api: HealthApiService by lazy {
        retrofit.create(HealthApiService::class.java)
    }
}
```

### 5. Llamar al endpoint desde ViewModel

```kotlin
class HealthViewModel : ViewModel() {
    
    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje
    
    private val _estadoGeneral = MutableLiveData<String>()
    val estadoGeneral: LiveData<String> = _estadoGeneral
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun procesarUsuario(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val response = RetrofitClient.api.procesarUsuario(
                    UserRequest(user_id = userId)
                )
                
                _mensaje.value = response.mensaje
                _estadoGeneral.value = response.estado_general
                
            } catch (e: HttpException) {
                _error.value = "Error del servidor: ${e.code()}"
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

### 6. Usar en Jetpack Compose

```kotlin
@Composable
fun HealthScreen(
    viewModel: HealthViewModel = viewModel()
) {
    val mensaje by viewModel.mensaje.observeAsState("")
    val estadoGeneral by viewModel.estadoGeneral.observeAsState("")
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    
    // Obtener UID del usuario actual de Firebase
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { viewModel.procesarUsuario(userId) },
            enabled = !isLoading && userId.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Obtener Análisis de Salud")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        error?.let {
            Text(
                text = it,
                color = Color.Red
            )
        }
        
        if (estadoGeneral.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Estado: $estadoGeneral",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = mensaje)
                }
            }
        }
    }
}
```

---

## 🔄 Flujo de la API

```
┌─────────────────────────────────────────────────────────────┐
│                      FLUJO COMPLETO                         │
└─────────────────────────────────────────────────────────────┘

[App Android]
     │
     │ POST /procesar_usuario
     │ { "user_id": "abc123" }
     │
     ▼
[Cloud Run - FastAPI]
     │
     ├──► [1] Obtener perfil del usuario (Firestore)
     │         users/{user_id} → name, goal, height, weight, sex
     │
     ├──► [2] Obtener último registro válido (Firestore)
     │         users/{user_id}/health_records
     │         Filtros: relojColocado=true, frecuenciaCardiaca>0
     │
     ├──► [3] Ejecutar 4 modelos ML (scikit-learn)
     │         - Modelo sueño → adecuado/insuficiente
     │         - Modelo ritmo → normal/irregular
     │         - Modelo estrés → controlado/alto
     │         - Modelo pasos → suficiente/bajo
     │
     ├──► [4] Generar mensaje con IA (Gemini)
     │         Prompt personalizado con datos + predicciones
     │
     ├──► [5] Guardar mensaje en Firestore
     │         users/{user_id}/messagesIa/{doc_id}
     │
     └──► [6] Retornar respuesta JSON
                │
                ▼
          [App Android]
          Muestra mensaje al usuario
```

---

## 📊 Datos Requeridos en Firestore

### Perfil del Usuario
**Colección:** `users/{user_id}`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `name` | string | Nombre del usuario |
| `goal` | string | Objetivo de salud |
| `height` | number | Altura en cm |
| `weight` | number | Peso en kg |
| `sex` | number | 0=Femenino, 1=Masculino |

### Registros de Salud
**Colección:** `users/{user_id}/health_records/{doc_id}`

| Campo | Tipo | Descripción | Requerido |
|-------|------|-------------|-----------|
| `frecuenciaCardiaca` | number | BPM promedio | ✅ |
| `frecuenciaCardiacaMin` | number | BPM mínimo | ❌ |
| `frecuenciaCardiacaMax` | number | BPM máximo | ❌ |
| `horasDeSueño` | number | Horas dormidas | ✅ |
| `nivelDeEstres` | number | 0-100 | ✅ |
| `pasosDiarios` | number | Cantidad de pasos | ✅ |
| `saturacionOxigeno` | number | SpO2 % | ❌ |
| `relojColocado` | boolean | Si el reloj estaba puesto | ✅ (debe ser true) |
| `fecha` | string | "2025-11-24" | ✅ |
| `horaRegistro` | string | "14:30" | ❌ |
| `lastUpdated` | Timestamp | Firestore timestamp | ❌ |

---

## ⚠️ Códigos de Error HTTP

| Código | Significado | Causa común |
|--------|-------------|-------------|
| 200 | OK | Éxito |
| 400 | Bad Request | JSON inválido o user_id faltante |
| 404 | Not Found | Usuario sin registros |
| 500 | Internal Server Error | Error en servidor o modelos |

---

## 🧪 Probar con cURL

### Health Check
```bash
curl https://health-api-409458732489.us-central1.run.app/
```

### Procesar Usuario
```bash
curl -X POST \
  https://health-api-409458732489.us-central1.run.app/procesar_usuario \
  -H "Content-Type: application/json" \
  -d '{"user_id": "6VsRDCu3wldPe7o0Hm9iMAdnnUa2"}'
```

---

## 🧪 Probar con PowerShell

```powershell
$body = @{user_id = "6VsRDCu3wldPe7o0Hm9iMAdnnUa2"} | ConvertTo-Json
Invoke-RestMethod -Uri "https://health-api-409458732489.us-central1.run.app/procesar_usuario" -Method POST -Body $body -ContentType "application/json"
```

---

## 📞 Contacto

- **Proyecto:** Proyecto de Título - Universidad
- **Desarrollador Backend:** Iván Vargas
- **Email:** ivanoooh.vargas@gmail.com
