# Guía de Implementación - Sistema de Alertas en Android

## 📋 RESUMEN PARA TU IA

**Copia esto y pégalo a tu IA para que entienda el contexto:**

> "Necesito implementar un sistema de alertas de salud en mi app Android (Kotlin). El backend ya guarda alertas en Firebase Firestore en la colección `users/{userId}/alertas/`. Necesito:
> 1. Escuchar esa colección en tiempo real
> 2. Cuando llegue una alerta nueva (leida=false), mostrar una notificación
> 3. Si la alerta es de tipo "critica", enviar SMS a los contactos de emergencia
> Los contactos están en el documento del usuario en los campos `emergencyContact` y `emergencyContact2`"

---

## 🎯 OBJETIVO

Tu app debe:
1. **Escuchar** la colección `alertas/` en Firebase en tiempo real
2. **Mostrar notificación** cuando llegue una alerta nueva
3. **Enviar SMS** a los contactos de emergencia si es alerta crítica

---

## 📁 Estructura en Firebase

Las alertas se guardan en:
```
users/{userId}/alertas/{alertaId}
```

Cada alerta tiene esta estructura:
```json
{
  "tipo": "critica",
  "titulo": "🚨 ALERTA CRÍTICA DE SALUD",
  "mensaje": "🚨 Pulso muy alto: 192 bpm",
  "valores": {
    "frecuenciaCardiaca": 192,
    "saturacionOxigeno": 94,
    "nivelDeEstres": 65,
    "horasDeSueno": 6
  },
  "leida": false,
  "atendida": false,
  "timestamp": "2025-12-08T..."
}
```

**Campos importantes:**
- `tipo`: `"critica"` o `"advertencia"`
- `mensaje`: El texto a mostrar en la notificación
- `leida`: `false` = nueva, `true` = ya procesada

---

## 📱 Contactos de Emergencia

Los contactos están en el documento del usuario:
```
users/{userId}/
  ├── emergencyContact: "+56937250240"
  └── emergencyContact2: "+56974626542"
```

**⚠️ IMPORTANTE:** Los campos se llaman `emergencyContact` y `emergencyContact2` (en inglés, sin acento).

---

## ✅ PASOS A SEGUIR

### Paso 1: Agregar permisos en AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.SEND_SMS"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.INTERNET"/>
```

### Paso 2: Crear la clase AlertasManager

Crea un archivo `AlertasManager.kt` con este código:

```kotlin
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AlertasManager(private val context: Context) {
    
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    
    init {
        crearCanalesNotificacion()
    }
    
    private fun crearCanalesNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            
            // Canal para alertas críticas (sonido y vibración)
            val canalCritico = NotificationChannel(
                "alertas_criticas",
                "Alertas Críticas de Salud",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas urgentes que requieren atención inmediata"
                enableVibration(true)
            }
            
            // Canal para advertencias (normal)
            val canalAdvertencia = NotificationChannel(
                "alertas_advertencia",
                "Advertencias de Salud",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            
            manager.createNotificationChannel(canalCritico)
            manager.createNotificationChannel(canalAdvertencia)
        }
    }
    
    // LLAMAR ESTE MÉTODO PARA INICIAR LA ESCUCHA
    fun escucharAlertas() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("users")
            .document(userId)
            .collection("alertas")
            .whereEqualTo("leida", false)  // Solo alertas no leídas
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                
                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        procesarAlerta(change)
                    }
                }
            }
    }
    
    private fun procesarAlerta(change: DocumentChange) {
        val documento = change.document
        val tipo = documento.getString("tipo") ?: ""
        val titulo = documento.getString("titulo") ?: "Alerta de Salud"
        val mensaje = documento.getString("mensaje") ?: ""
        
        // 1. Mostrar notificación
        mostrarNotificacion(tipo, titulo, mensaje)
        
        // 2. Si es crítica, enviar SMS
        if (tipo == "critica") {
            enviarSMSEmergencia(mensaje)
        }
        
        // 3. Marcar como leída
        documento.reference.update("leida", true)
    }
    
    private fun mostrarNotificacion(tipo: String, titulo: String, mensaje: String) {
        val channelId = if (tipo == "critica") "alertas_criticas" else "alertas_advertencia"
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Cambiar por tu icono
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(
                if (tipo == "critica") NotificationCompat.PRIORITY_HIGH 
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .build()
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
            == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), notification)
        }
    }
    
    private fun enviarSMSEmergencia(mensaje: String) {
        val userId = auth.currentUser?.uid ?: return
        
        // Leer contactos de emergencia del perfil del usuario
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                // ⚠️ IMPORTANTE: Los campos se llaman emergencyContact (en inglés)
                val contacto1 = doc.getString("emergencyContact")
                val contacto2 = doc.getString("emergencyContact2")
                
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
                    == PackageManager.PERMISSION_GRANTED) {
                    
                    val smsManager = SmsManager.getDefault()
                    
                    // Enviar a contacto 1
                    if (!contacto1.isNullOrEmpty()) {
                        try {
                            smsManager.sendTextMessage(contacto1, null, mensaje, null, null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    // Enviar a contacto 2
                    if (!contacto2.isNullOrEmpty()) {
                        try {
                            smsManager.sendTextMessage(contacto2, null, mensaje, null, null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
    }
}
```

### Paso 3: Usar en MainActivity

```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var alertasManager: AlertasManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Solicitar permisos
        solicitarPermisos()
        
        // Iniciar escucha de alertas
        alertasManager = AlertasManager(this)
        alertasManager.escucharAlertas()
    }
    
    private fun solicitarPermisos() {
        val permisos = mutableListOf<String>()
        
        // Permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Permiso de SMS
        if (checkSelfPermission(Manifest.permission.SEND_SMS) 
            != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.SEND_SMS)
        }
        
        if (permisos.isNotEmpty()) {
            requestPermissions(permisos.toTypedArray(), 100)
        }
    }
}
```

### Paso 4: Llamar al endpoint cuando lleguen datos del reloj

Cada vez que guardes datos nuevos del reloj en Firebase, debes llamar al endpoint para que detecte alertas:

```kotlin
// Agregar dependencia en build.gradle:
// implementation 'com.squareup.retrofit2:retrofit:2.9.0'
// implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

interface HealthApi {
    @POST("detectar_alerta")
    fun detectarAlerta(@Body body: Map<String, String>): Call<Any>
}

// Crear instancia de Retrofit
val retrofit = Retrofit.Builder()
    .baseUrl("https://health-api-409458732489.us-central1.run.app/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(HealthApi::class.java)

// LLAMAR DESPUÉS DE GUARDAR DATOS DEL RELOJ:
fun verificarAlertas(userId: String) {
    api.detectarAlerta(mapOf("user_id" to userId))
        .enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // La alerta se guardó en Firebase si había anomalía
                // El listener la detectará automáticamente
            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Manejar error
            }
        })
}
```

---

## 🔄 FLUJO COMPLETO

```
1. Reloj mide datos (pulso=190)
          ↓
2. Tu app guarda en Firebase (health_records)
          ↓
3. Tu app llama a verificarAlertas(userId)
          ↓
4. API analiza → Detecta pulso muy alto → Guarda en /alertas
          ↓
5. AlertasManager detecta nueva alerta (listener)
          ↓
6. Muestra notificación: "🚨 Pulso muy alto: 190 bpm"
          ↓
7. Envía SMS a +56937250240 y +56974626542
```

---

## 🧪 PROBAR

Ya hay alertas de prueba en Firebase para el usuario `6VsRDCu3wldPe7o0Hm9iMAdnnUa2`. 

Para probar:
1. Inicia sesión con ese usuario
2. La app debería detectar las alertas con `leida: false`
3. Debería mostrar notificación

---

## 📝 CHECKLIST

- [ ] Agregar permisos en AndroidManifest.xml
- [ ] Crear clase AlertasManager.kt
- [ ] Inicializar en MainActivity
- [ ] Solicitar permisos al usuario
- [ ] Agregar Retrofit para llamar al endpoint
- [ ] Llamar `verificarAlertas()` después de guardar datos del reloj
- [ ] Probar con alertas existentes

---

## ❓ PARA TU IA

Si tu IA necesita más contexto, dile:

> "El backend está en Cloud Run (Python/FastAPI). Cuando llamo a POST /detectar_alerta con {user_id: "xxx"}, el backend analiza los últimos datos de salud del usuario y si detecta valores anormales (pulso muy alto, oxígeno bajo, etc.), guarda automáticamente un documento en Firestore en users/{userId}/alertas/. Mi app Android necesita escuchar esa colección y mostrar notificaciones cuando lleguen alertas nuevas."

---

## 🔗 URLs

- **API Backend:** `https://health-api-409458732489.us-central1.run.app`
- **Endpoint alertas:** `POST /detectar_alerta` con body `{"user_id": "xxx"}`
