# 📊 Datos de Salud Recopilados - Health Connect Integration

## Datos Implementados

La aplicación ahora recopila los siguientes datos de salud desde Health Connect (compatible con Samsung Health y otras apps de salud):

### 1. 👟 **Pasos Diarios** (`pasosDiarios`)
- **Tipo:** Long
- **Fuente:** `StepsRecord`
- **Descripción:** Contador de pasos del día actual
- **Unidad:** Cantidad de pasos

### 2. 😴 **Horas de Sueño** (`horasDeSueño`)
- **Tipo:** Double
- **Fuente:** `SleepSessionRecord`
- **Descripción:** Total de horas de sueño registradas en el día
- **Unidad:** Horas (con decimales)
- **Cálculo:** Suma de todas las sesiones de sueño del día

### 3. 📱 **Tiempo de Pantalla** (`tiempoPantalla`)
- **Tipo:** Double
- **Fuente:** No disponible directamente en Health Connect
- **Estado:** ⚠️ Actualmente retorna 0.0
- **Nota:** Para implementar esto se requiere acceso a `UsageStatsManager` del sistema Android con permisos especiales `PACKAGE_USAGE_STATS`

### 4. ❤️ **Frecuencia Cardíaca** (`frecuenciaCardiaca`, `frecuenciaCardiacaMax`, `frecuenciaCardiacaMin`)
- **Tipo:** Long (3 valores)
- **Fuente:** `HeartRateRecord`
- **Descripción:** 
  - `frecuenciaCardiaca`: Promedio del día
  - `frecuenciaCardiacaMax`: Frecuencia máxima registrada
  - `frecuenciaCardiacaMin`: Frecuencia mínima registrada
- **Unidad:** BPM (latidos por minuto)

### 5. ⌚ **Reloj Colocado** (`relojColocado`)
- **Tipo:** Boolean
- **Fuente:** Derivado de la disponibilidad de datos de `HeartRateRecord`
- **Descripción:** Indica si el dispositivo wearable (reloj inteligente) está siendo usado
- **Lógica:** `true` si hay datos de frecuencia cardíaca disponibles, `false` en caso contrario

### 6. 😰 **Nivel de Estrés** (`nivelDeEstres`)
- **Tipo:** Int (0-100)
- **Fuente:** Calculado basado en `HeartRateRecord`
- **Descripción:** Estimación del nivel de estrés basada en:
  - Frecuencia cardíaca promedio
  - Variabilidad de frecuencia cardíaca (HRV)
- **Escala:**
  - 0-20: Muy bajo (relajado)
  - 21-40: Bajo
  - 41-60: Moderado
  - 61-80: Alto
  - 81-100: Muy alto
- **Algoritmo:**
  ```
  - FC < 60 BPM → Estrés: 10
  - FC 60-80 BPM → Estrés: 20
  - FC 81-100 BPM → Estrés: 40
  - FC 101-120 BPM → Estrés: 70
  - FC > 120 BPM → Estrés: 90
  - Ajustado por variabilidad (mayor variabilidad = menor estrés)
  ```

### 7. 🕐 **Hora de Registro** (`horaRegistro`)
- **Tipo:** String
- **Formato:** "HH:mm" (24 horas)
- **Descripción:** Hora exacta en que se recopilaron los datos
- **Ejemplo:** "14:30"

### 8. 📅 **Fecha** (`fecha`)
- **Tipo:** String
- **Formato:** "YYYY-MM-DD"
- **Descripción:** Fecha del registro de datos
- **Ejemplo:** "2025-10-25"

### 9. ⚖️ **Peso** (`peso`)
- **Tipo:** Double
- **Fuente:** `WeightRecord` (último registro disponible)
- **Unidad:** Kilogramos (kg)

### 10. 📏 **Altura** (`altura`)
- **Tipo:** Double
- **Fuente:** `HeightRecord` (último registro disponible)
- **Unidad:** Metros (m)

## Estructura de Datos en Firebase

Los datos se guardan en Firestore con la siguiente estructura:

```
users/
  └── {userId}/
      └── daily_health_data/
          └── {fecha} (documento)
              ├── pasosDiarios: 8542
              ├── horasDeSueño: 7.5
              ├── tiempoPantalla: 0.0
              ├── frecuenciaCardiaca: 72
              ├── frecuenciaCardiacaMax: 110
              ├── frecuenciaCardiacaMin: 58
              ├── relojColocado: true
              ├── nivelDeEstres: 35
              ├── horaRegistro: "14:30"
              ├── fecha: "2025-10-25"
              ├── peso: 70.5
              ├── altura: 1.75
              └── lastUpdated: Timestamp
```

## Permisos Requeridos

### AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.health.READ_STEPS"/>
<uses-permission android:name="android.permission.health.READ_WEIGHT"/>
<uses-permission android:name="android.permission.health.READ_HEIGHT"/>
<uses-permission android:name="android.permission.health.READ_SLEEP"/>
<uses-permission android:name="android.permission.health.READ_HEART_RATE"/>
<uses-permission android:name="android.permission.health.READ_EXERCISE"/>
```

### En el código (DashboardActivity.kt)
```kotlin
private val PERMISSIONS = setOf(
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getReadPermission(WeightRecord::class),
    HealthPermission.getReadPermission(HeightRecord::class),
    HealthPermission.getReadPermission(SleepSessionRecord::class),
    HealthPermission.getReadPermission(HeartRateRecord::class),
    HealthPermission.getReadPermission(ExerciseSessionRecord::class)
)
```

## Compatibilidad con Samsung Health

✅ **Todos los datos son compatibles con Samsung Health** ya que Samsung Health sincroniza sus datos con Google Health Connect. 

Los datos recopilados incluyen:
- Pasos (Samsung Health → Health Connect)
- Sueño (Samsung Health → Health Connect)
- Frecuencia cardíaca (Samsung Health / Galaxy Watch → Health Connect)
- Peso (Samsung Health → Health Connect)
- Altura (Samsung Health → Health Connect)

## Visualización en la App

La interfaz muestra todos los datos con emojis para mejor legibilidad:

```
📅 Fecha: 2025-10-25
🕐 Hora: 14:30
👟 Pasos: 8542
😴 Sueño: 7.5 horas
📱 Tiempo Pantalla: 0.0 horas
❤️ Frecuencia Cardíaca: 72 BPM
   Max: 110 BPM
   Min: 58 BPM
⌚ Reloj Colocado: Sí
😰 Nivel de Estrés: 35/100
⚖️ Peso: 70.5 kg
📏 Altura: 1.75 m
```

## Cómo Usar

1. **Instalar Health Connect** desde Play Store (si no está instalado)
2. **Configurar Samsung Health** o cualquier app compatible
3. **Abrir la aplicación** y hacer login
4. **Presionar "Actualizar"** en el Dashboard
5. **Conceder permisos** cuando se soliciten
6. **Ver los datos** actualizados en tiempo real
7. Los datos se guardan automáticamente en Firebase

## Limitaciones Actuales

### ⚠️ Tiempo de Pantalla
- No disponible directamente desde Health Connect
- Requiere permisos especiales del sistema (`PACKAGE_USAGE_STATS`)
- Para implementarlo:
  1. Solicitar permiso especial al usuario
  2. Usar `UsageStatsManager`
  3. Recopilar datos de uso de apps

### 💡 Mejoras Futuras Posibles

1. **Calorías quemadas** (`CaloriesBurnedRecord`)
2. **Distancia recorrida** (`DistanceRecord`)
3. **Actividades físicas** (`ExerciseSessionRecord` - ya tenemos permiso)
4. **Oxígeno en sangre** (`BloodOxygenRecord`)
5. **Presión arterial** (`BloodPressureRecord`)
6. **Glucosa en sangre** (`BloodGlucoseRecord`)
7. **Hidratación** (`HydrationRecord`)
8. **Nutrición** (`NutritionRecord`)

## Requisitos del Dispositivo

- ✅ Android 10+ (API 29+)
- ✅ Health Connect instalado
- ✅ Samsung Health u otra app compatible (opcional pero recomendado)
- ✅ Galaxy Watch o smartwatch compatible (opcional, para frecuencia cardíaca)

## Debugging

Para ver los logs de recopilación de datos:
```bash
adb logcat | findstr "HealthConnectApp"
```

Logs esperados:
```
D/HealthConnectApp: Starting to read health data from 2025-10-25T00:00:00Z to 2025-10-25T14:30:00Z
D/HealthConnectApp: Heart rate - Avg: 72, Max: 110, Min: 58 (145 samples)
D/HealthConnectApp: Health data collected: HealthData(pasosDiarios=8542, ...)
D/HealthConnectApp: Saving health data to Firebase: {pasosDiarios=8542, ...}
D/HealthConnectApp: Health data saved successfully to Firebase
```

## Notas Importantes

1. **Datos históricos**: La app lee datos del día actual. Para históricos, modificar el rango de tiempo en `readHealthData()`
2. **Actualizaciones**: Los datos se actualizan cada vez que se presiona el botón "Actualizar"
3. **Privacidad**: Todos los datos son privados del usuario y se guardan en su propia colección de Firebase
4. **Sincronización**: Samsung Health sincroniza automáticamente con Health Connect si está configurado correctamente
