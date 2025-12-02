# INFORME TÉCNICO - SAMSUNG MACHINE

## Aplicación de Monitoreo de Salud con Inteligencia Artificial

---

## ÍNDICE

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Arquitectura del Sistema](#2-arquitectura-del-sistema)
3. [Stack Tecnológico](#3-stack-tecnológico)
4. [Estructura del Proyecto](#4-estructura-del-proyecto)
5. [Módulos y Componentes](#5-módulos-y-componentes)
6. [Sistema de Diseño UI/UX](#6-sistema-de-diseño-uiux)
7. [Integración con Health Connect](#7-integración-con-health-connect)
8. [Backend y Servicios en la Nube](#8-backend-y-servicios-en-la-nube)
9. [API de Inteligencia Artificial](#9-api-de-inteligencia-artificial)
10. [Modelo de Datos](#10-modelo-de-datos)
11. [Sistema de Notificaciones](#11-sistema-de-notificaciones)
12. [Seguridad y Autenticación](#12-seguridad-y-autenticación)
13. [Rendimiento y Optimización](#13-rendimiento-y-optimización)
14. [Configuración y Despliegue](#14-configuración-y-despliegue)
15. [Mantenimiento y Escalabilidad](#15-mantenimiento-y-escalabilidad)

---

## 1. RESUMEN EJECUTIVO

### 1.1 Descripción General

**Samsung Machine** es una aplicación móvil Android de monitoreo de salud integral que permite a los usuarios registrar, visualizar y analizar sus métricas de salud en tiempo real. La aplicación se integra con dispositivos wearables a través de la API oficial de Google Health Connect, almacena datos en Firebase Cloud Firestore, y utiliza un servicio de inteligencia artificial desplegado en Google Cloud Run para proporcionar análisis personalizados y recomendaciones de salud.

### 1.2 Objetivos del Sistema

| Objetivo | Descripción |
|----------|-------------|
| **Monitoreo Continuo** | Recolección automática de métricas vitales desde dispositivos wearables |
| **Análisis Inteligente** | Procesamiento de datos mediante algoritmos de IA para generar insights personalizados |
| **Historial Completo** | Almacenamiento persistente y consulta de registros históricos de salud |
| **Experiencia de Usuario** | Interfaz moderna, accesible y responsiva basada en Material Design 3 |
| **Notificaciones Proactivas** | Sistema de recordatorios para mantener el seguimiento de la salud |

### 1.3 Métricas de Salud Monitoreadas

- **Pasos diarios** - Conteo de actividad física
- **Frecuencia cardíaca** - Promedio, mínimo y máximo (BPM)
- **Saturación de oxígeno (SpO2)** - Porcentaje de oxígeno en sangre
- **Horas de sueño** - Duración y calidad del descanso
- **Nivel de estrés** - Índice calculado basado en variabilidad cardíaca

---

## 2. ARQUITECTURA DEL SISTEMA

### 2.1 Diagrama de Arquitectura de Alto Nivel

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CAPA DE PRESENTACIÓN                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │  LoginScreen    │  │ DashboardScreen │  │  HistoryScreen  │             │
│  │  (Compose)      │  │    (Compose)    │  │    (Compose)    │             │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘             │
│           │                    │                    │                       │
│           └────────────────────┼────────────────────┘                       │
│                                ▼                                            │
│              ┌─────────────────────────────────┐                            │
│              │     UI Components (Compose)     │                            │
│              │  HealthMetricCard, ChartCard,   │                            │
│              │  AIMessageCard, ActionButton    │                            │
│              └─────────────────────────────────┘                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            CAPA DE LÓGICA                                   │
│  ┌──────────────────────────┐  ┌──────────────────────────┐                │
│  │ DashboardComposeActivity │  │  HistoryComposeActivity  │                │
│  │  - syncHealthData()      │  │  - loadHistory()         │                │
│  │  - analyzeWithIA()       │  │  - filterRecords()       │                │
│  │  - saveToFirebase()      │  └──────────────────────────┘                │
│  └──────────────────────────┘                                               │
│                                                                              │
│  ┌──────────────────────────┐  ┌──────────────────────────┐                │
│  │   HealthReminderWorker   │  │   HealthApiService       │                │
│  │   (WorkManager)          │  │   (Retrofit Client)      │                │
│  └──────────────────────────┘  └──────────────────────────┘                │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌─────────────────────┐ ┌─────────────────┐ ┌─────────────────────┐
│   HEALTH CONNECT    │ │    FIREBASE     │ │   GOOGLE CLOUD      │
│   (Google API)      │ │                 │ │   RUN                │
│                     │ │  ┌───────────┐  │ │                     │
│  - StepsRecord      │ │  │   Auth    │  │ │  ┌───────────────┐  │
│  - HeartRateRecord  │ │  └───────────┘  │ │  │  Health AI    │  │
│  - SleepRecord      │ │                 │ │  │  API          │  │
│  - OxygenSaturation │ │  ┌───────────┐  │ │  │               │  │
│                     │ │  │ Firestore │  │ │  │ /procesar_    │  │
│                     │ │  │  (NoSQL)  │  │ │  │  usuario      │  │
│                     │ │  └───────────┘  │ │  └───────────────┘  │
└─────────────────────┘ └─────────────────┘ └─────────────────────┘
```

### 2.2 Patrón Arquitectónico

La aplicación implementa una arquitectura basada en **Clean Architecture** adaptada para Android, con las siguientes capas:

| Capa | Responsabilidad | Componentes |
|------|-----------------|-------------|
| **Presentación** | UI y manejo de eventos | Composables, Activities |
| **Dominio** | Lógica de negocio | Data Classes, Use Cases implícitos |
| **Datos** | Acceso a datos externos | Firebase, Health Connect, Retrofit |

### 2.3 Flujo de Datos

```
Usuario → UI (Compose) → Activity → Firebase/HealthConnect → Callback → State Update → UI Recomposition
                                  ↓
                            API Cloud Run
                                  ↓
                         Análisis IA → Respuesta
```

---

## 3. STACK TECNOLÓGICO

### 3.1 Lenguaje y Plataforma

| Componente | Tecnología | Versión |
|------------|------------|---------|
| **Lenguaje** | Kotlin | 2.0.21 |
| **Plataforma** | Android | API 29+ (Android 10+) |
| **Target SDK** | Android | API 36 |
| **Build System** | Gradle (Kotlin DSL) | 8.13.0 |
| **JVM Target** | Java Virtual Machine | 11 |

### 3.2 Frameworks de UI

| Framework | Propósito | Versión |
|-----------|-----------|---------|
| **Jetpack Compose** | UI declarativa moderna | BOM 2024.06.00 |
| **Material 3** | Sistema de diseño | 1.2.1 |
| **Material Icons Extended** | Iconografía | Bundled |
| **Navigation Compose** | Navegación entre pantallas | 2.7.7 |
| **Activity Compose** | Integración con Activities | 1.9.0 |

### 3.3 Backend y Cloud Services

| Servicio | Propósito | Versión/Detalle |
|----------|-----------|-----------------|
| **Firebase Auth** | Autenticación de usuarios | BOM 33.1.1 |
| **Firebase Firestore** | Base de datos NoSQL | BOM 33.1.1 |
| **Firebase App Check** | Seguridad de API | Play Integrity |
| **Google Cloud Run** | API de IA | us-central1 |

### 3.4 APIs de Salud

| API | Propósito | Versión |
|-----|-----------|---------|
| **Health Connect** | Lectura de datos de wearables | 1.1.0-alpha10 |
| **Samsung Health Data** | Compatibilidad Samsung (AAR) | 1.0.0 |

### 3.5 Networking

| Librería | Propósito | Versión |
|----------|-----------|---------|
| **Retrofit 2** | Cliente HTTP REST | 2.9.0 |
| **Gson Converter** | Serialización JSON | 2.9.0 |
| **OkHttp Logging** | Debugging de requests | 4.12.0 |

### 3.6 Otras Dependencias

| Librería | Propósito | Versión |
|----------|-----------|---------|
| **WorkManager** | Tareas en background | 2.9.0 |
| **Lifecycle Runtime KTX** | Coroutines lifecycle-aware | 2.8.0 |
| **Lifecycle ViewModel Compose** | ViewModels en Compose | 2.8.0 |
| **MPAndroidChart** | Gráficos (legacy) | 3.1.0 |
| **Vico Charts** | Gráficos para Compose | 1.13.1 |

---

## 4. ESTRUCTURA DEL PROYECTO

### 4.1 Árbol de Directorios

```
ProyectoTitulo/
├── app/
│   ├── build.gradle.kts              # Configuración de build del módulo
│   ├── google-services.json          # Configuración Firebase
│   ├── proguard-rules.pro            # Reglas de ofuscación
│   ├── samsung-health-data-api-1.0.0.aar  # SDK Samsung Health
│   │
│   └── src/main/
│       ├── AndroidManifest.xml       # Manifiesto de la aplicación
│       │
│       ├── java/com/example/proyectotitulo/
│       │   │
│       │   ├── api/                  # Capa de servicios de red
│       │   │   └── HealthApiService.kt
│       │   │
│       │   ├── ui/                   # Capa de presentación
│       │   │   ├── components/       # Componentes reutilizables
│       │   │   │   ├── HealthComponents.kt
│       │   │   │   └── HealthCharts.kt
│       │   │   │
│       │   │   ├── screens/          # Pantallas principales
│       │   │   │   ├── LoginScreen.kt
│       │   │   │   ├── RegisterScreen.kt
│       │   │   │   ├── DashboardScreen.kt
│       │   │   │   └── HistoryScreen.kt
│       │   │   │
│       │   │   └── theme/            # Sistema de diseño
│       │   │       ├── Color.kt
│       │   │       ├── Theme.kt
│       │   │       └── Type.kt
│       │   │
│       │   ├── DashboardComposeActivity.kt
│       │   ├── HistoryComposeActivity.kt
│       │   ├── LoginComposeActivity.kt
│       │   ├── RegisterComposeActivity.kt
│       │   ├── HealthData.kt
│       │   ├── HealthReminderWorker.kt
│       │   ├── User.kt
│       │   └── ...
│       │
│       └── res/                      # Recursos
│           ├── drawable/
│           ├── mipmap/
│           ├── values/
│           └── xml/
│
├── gradle/
│   ├── libs.versions.toml            # Catálogo de versiones
│   └── wrapper/
│
├── build.gradle.kts                  # Build raíz
├── settings.gradle.kts               # Configuración del proyecto
├── gradle.properties                 # Propiedades de Gradle
└── local.properties                  # Configuración local (SDK path)
```

### 4.2 Organización por Capas

```
┌────────────────────────────────────────────────────────────────┐
│                         PRESENTATION                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   screens/   │  │ components/  │  │    theme/    │         │
│  │              │  │              │  │              │         │
│  │ LoginScreen  │  │HealthMetric │  │   Color.kt   │         │
│  │ Dashboard    │  │ ChartCard   │  │   Theme.kt   │         │
│  │ History      │  │ AIMessage   │  │   Type.kt    │         │
│  │ Register     │  │ ActionBtn   │  │              │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                          ACTIVITIES                            │
│  ┌────────────────────┐  ┌────────────────────┐               │
│  │ DashboardCompose   │  │  HistoryCompose    │               │
│  │ Activity.kt        │  │  Activity.kt       │               │
│  │                    │  │                    │               │
│  │ - State management │  │ - Data loading     │               │
│  │ - Business logic   │  │ - Filtering        │               │
│  │ - API calls        │  │                    │               │
│  └────────────────────┘  └────────────────────┘               │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                        DATA / SERVICES                         │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐   │
│  │    api/        │  │  HealthData.kt │  │HealthReminder  │   │
│  │                │  │                │  │ Worker.kt      │   │
│  │ HealthApi      │  │  Data class    │  │                │   │
│  │ Service.kt     │  │  definitions   │  │  WorkManager   │   │
│  │                │  │                │  │  background    │   │
│  │ Retrofit       │  │                │  │  tasks         │   │
│  │ Client         │  │                │  │                │   │
│  └────────────────┘  └────────────────┘  └────────────────┘   │
└────────────────────────────────────────────────────────────────┘
```

---

## 5. MÓDULOS Y COMPONENTES

### 5.1 Módulo de Autenticación

#### 5.1.1 LoginComposeActivity

**Archivo:** `LoginComposeActivity.kt`

**Responsabilidades:**
- Gestión del estado de login
- Validación de credenciales
- Integración con Firebase Auth
- Navegación post-autenticación

**Flujo de Autenticación:**
```
1. Usuario ingresa credenciales
2. Validación local (campos no vacíos)
3. Firebase Auth signInWithEmailAndPassword()
4. Success → Navigate to Dashboard
5. Failure → Mostrar error message
```

#### 5.1.2 RegisterComposeActivity

**Archivo:** `RegisterComposeActivity.kt`

**Datos recolectados:**
| Campo | Tipo | Validación |
|-------|------|------------|
| name | String | No vacío |
| email | String | Formato email válido |
| password | String | Mínimo 6 caracteres |
| confirmPassword | String | Coincide con password |
| dob | String | Fecha válida |
| height | Double | Rango válido |
| weight | Double | Rango válido |
| sex | Int | 0 o 1 |
| emergencyContact | String | Formato teléfono |
| goal | String | Opcional |

### 5.2 Módulo de Dashboard

#### 5.2.1 DashboardComposeActivity

**Archivo:** `DashboardComposeActivity.kt`

**Líneas de código:** ~530

**Funciones Principales:**

| Función | Propósito | Parámetros |
|---------|-----------|------------|
| `onCreate()` | Inicialización de la actividad | Bundle |
| `getGreeting()` | Saludo contextual por hora | - |
| `getCurrentDate()` | Fecha formateada en español | - |
| `loadHealthDataFromFirebase()` | Carga datos desde Firestore | Callback |
| `loadChartData()` | Carga datos para gráficos | userId, date, Callback |
| `loadSleepChartData()` | Datos de sueño últimos 5 días | userId, Callback |
| `loadIaMessage()` | Último mensaje de IA | userId, Callback |
| `syncHealthData()` | Sincronización con Health Connect | Callback |
| `calculateStress()` | Cálculo de nivel de estrés | heartRate |
| `saveHealthDataToFirebase()` | Persistencia de datos | HealthData |
| `analyzeWithIA()` | Llamada a API de análisis | Callback |

**Estado Compose Manejado:**
```kotlin
var healthData by remember { mutableStateOf(HealthData()) }
var heartRateChartData by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
var sleepChartData by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
var iaMessage by remember { mutableStateOf("...") }
var isLoading by remember { mutableStateOf(false) }
var isAnalyzing by remember { mutableStateOf(false) }
```

#### 5.2.2 DashboardScreen

**Archivo:** `ui/screens/DashboardScreen.kt`

**Estructura de UI:**
```
Scaffold
├── TopAppBar
│   ├── WatchStatusIndicator
│   └── LogoutButton
│
└── Content (Scrollable Column)
    ├── DashboardHeader (Greeting + Date)
    │
    ├── Metrics Grid (2x2)
    │   ├── Steps Card
    │   ├── Heart Rate Card
    │   ├── Sleep Card
    │   └── SpO2 Card
    │
    ├── Stress Card (Full width)
    │
    ├── Heart Rate Chart Card
    │
    ├── Sleep Chart Card
    │
    ├── AI Message Card (with Analyze button)
    │
    └── Action Buttons Row
        ├── Sync Button
        └── History Button
```

### 5.3 Módulo de Historial

#### 5.3.1 HistoryComposeActivity

**Archivo:** `HistoryComposeActivity.kt`

**Query Firestore:**
```kotlin
firestore.collection("users").document(userId)
    .collection("health_records")
    .orderBy("lastUpdated", Query.Direction.DESCENDING)
    .limit(20)
    .get()
```

**Filtrado de Registros:**
- Excluye registros donde `relojColocado = false` Y todos los valores son 0
- Ordena por fecha descendente y luego por hora descendente

#### 5.3.2 HistoryScreen

**Archivo:** `ui/screens/HistoryScreen.kt`

**Componentes:**
| Componente | Propósito |
|------------|-----------|
| `HistoryScreen` | Pantalla principal con LazyColumn |
| `HistoryRecordCard` | Card individual de registro |
| `CompactMetricItem` | Métrica individual compacta |
| `EmptyHistoryPlaceholder` | Estado vacío |

### 5.4 Módulo de Componentes UI

#### 5.4.1 HealthComponents.kt

**Componentes Reutilizables:**

| Componente | Props | Uso |
|------------|-------|-----|
| `HealthMetricCard` | title, value, unit, icon, accentColor | Dashboard metrics |
| `ChartCard` | title, subtitle, icon, content | Chart containers |
| `AIMessageCard` | message, onAnalyzeClick, isAnalyzing | AI insights |
| `ActionButton` | text, icon, backgroundColor, onClick | Primary actions |
| `DashboardHeader` | greeting, dateText | Screen header |
| `WatchStatusIndicator` | isConnected | Connection status |
| `NavButton` | icon, label, isSelected, onClick | Navigation |
| `HistoryItemCard` | title, value, unit, icon, timestamp | Legacy |

#### 5.4.2 HealthCharts.kt

**Gráficos Personalizados:**

| Componente | Tipo | Datos |
|------------|------|-------|
| `HeartRateLineChart` | Barras verticales con valores | `List<Pair<String, Float>>` |
| `SleepBarChart` | Barras horizontales con días | `List<Pair<String, Float>>` |
| `EmptyChartPlaceholder` | Estado vacío | message: String |

---

## 6. SISTEMA DE DISEÑO UI/UX

### 6.1 Filosofía de Diseño

La aplicación implementa el sistema de diseño **shadcn/ui** adaptado para Android, caracterizado por:

- **Minimalismo**: Interfaces limpias sin elementos superfluos
- **Bordes sutiles**: Uso de bordes en lugar de sombras
- **Paleta neutra**: Colores zinc como base
- **Tipografía clara**: Jerarquía visual definida
- **Espaciado consistente**: Sistema de 4dp

### 6.2 Paleta de Colores

#### 6.2.1 Colores Primarios

```kotlin
// Primary - Negro/zinc (shadcn default)
val Primary = Color(0xFF18181B)          // zinc-900
val PrimaryLight = Color(0xFF27272A)     // zinc-800
val PrimaryDark = Color(0xFF09090B)      // zinc-950
val PrimaryContainer = Color(0xFFF4F4F5) // zinc-100
```

#### 6.2.2 Colores de Fondo

```kotlin
val Background = Color(0xFFFFFFFF)       // Blanco puro
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFFAFAFA)   // zinc-50
val CardBackground = Color(0xFFFFFFFF)
```

#### 6.2.3 Colores de Texto

```kotlin
val TextPrimary = Color(0xFF09090B)      // zinc-950
val TextSecondary = Color(0xFF71717A)    // zinc-500
val TextTertiary = Color(0xFFA1A1AA)     // zinc-400
```

#### 6.2.4 Colores de Métricas

| Métrica | Color | Código Hex |
|---------|-------|------------|
| Pasos | zinc-900 | `#18181B` |
| Ritmo Cardíaco | red-600 | `#DC2626` |
| Sueño | violet-600 | `#7C3AED` |
| SpO2 | blue-600 | `#2563EB` |
| Estrés | orange-600 | `#EA580C` |

#### 6.2.5 Colores de Estado

```kotlin
val Success = Color(0xFF16A34A)          // green-600
val Warning = Color(0xFFCA8A04)          // yellow-600
val Error = Color(0xFFDC2626)            // red-600
val Info = Color(0xFF2563EB)             // blue-600
```

#### 6.2.6 Bordes y Separadores

```kotlin
val Border = Color(0xFFE4E4E7)           // zinc-200
val BorderFocus = Color(0xFF18181B)      // zinc-900
val Divider = Color(0xFFE4E4E7)
```

### 6.3 Tema Material 3

```kotlin
@Composable
fun HealthTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### 6.4 Componentes Estilizados

#### 6.4.1 Cards (estilo shadcn)

```kotlin
Card(
    modifier = modifier
        .border(
            width = 1.dp,
            color = Border,
            shape = RoundedCornerShape(12.dp)
        ),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = CardBackground),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Sin sombra
)
```

#### 6.4.2 Botones

```kotlin
Button(
    onClick = onClick,
    modifier = modifier.height(44.dp),
    shape = RoundedCornerShape(8.dp),
    colors = ButtonDefaults.buttonColors(containerColor = Primary),
    elevation = ButtonDefaults.buttonElevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp
    )
)
```

#### 6.4.3 TextField

```kotlin
OutlinedTextField(
    colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = Primary,
        focusedBorderColor = Primary,
        unfocusedBorderColor = Border,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )
)
```

### 6.5 Espaciado y Layout

| Elemento | Valor |
|----------|-------|
| Padding horizontal de pantalla | 16.dp |
| Padding interno de Cards | 16-20.dp |
| Espaciado entre Cards | 12.dp |
| Espaciado entre secciones | 24.dp |
| Radio de bordes (Cards) | 12.dp |
| Radio de bordes (Buttons) | 8.dp |
| Altura de botones | 44.dp |

---

## 7. INTEGRACIÓN CON HEALTH CONNECT

### 7.1 Descripción de Health Connect

Health Connect es la API oficial de Google para acceder a datos de salud y fitness de manera unificada. Actúa como un repositorio central donde múltiples aplicaciones pueden leer y escribir datos de salud.

### 7.2 Permisos Requeridos

**AndroidManifest.xml:**
```xml
<!-- Health Connect Permissions -->
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE" />
<uses-permission android:name="android.permission.health.READ_SLEEP" />
<uses-permission android:name="android.permission.health.READ_WEIGHT" />
<uses-permission android:name="android.permission.health.READ_HEIGHT" />
<uses-permission android:name="android.permission.health.READ_EXERCISE" />
<uses-permission android:name="android.permission.health.READ_OXYGEN_SATURATION" />

<!-- Query para Health Connect -->
<queries>
    <package android:name="com.google.android.apps.healthdata" />
    <intent>
        <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
    </intent>
</queries>
```

### 7.3 Configuración de Cliente

```kotlin
private var healthConnectClient: HealthConnectClient? = null

private val healthPermissions = setOf(
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getReadPermission(HeartRateRecord::class),
    HealthPermission.getReadPermission(SleepSessionRecord::class),
    HealthPermission.getReadPermission(OxygenSaturationRecord::class)
)

// Verificar disponibilidad
val availabilityStatus = HealthConnectClient.getSdkStatus(this)
if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
    healthConnectClient = HealthConnectClient.getOrCreate(this)
}
```

### 7.4 Lectura de Datos

#### 7.4.1 Pasos

```kotlin
val stepsResponse = client.readRecords(
    ReadRecordsRequest(StepsRecord::class, timeRange)
)
val totalSteps = stepsResponse.records.sumOf { it.count }
```

#### 7.4.2 Frecuencia Cardíaca

```kotlin
val hrResponse = client.readRecords(
    ReadRecordsRequest(HeartRateRecord::class, timeRange)
)
val hrSamples = hrResponse.records.flatMap { it.samples }
val avgHr = hrSamples.map { it.beatsPerMinute }.average().toLong()
val minHr = hrSamples.minOfOrNull { it.beatsPerMinute } ?: 0
val maxHr = hrSamples.maxOfOrNull { it.beatsPerMinute } ?: 0
```

#### 7.4.3 Saturación de Oxígeno

```kotlin
val spo2Response = client.readRecords(
    ReadRecordsRequest(OxygenSaturationRecord::class, timeRange)
)
val avgSpo2 = spo2Response.records.map { it.percentage.value }.average()
```

#### 7.4.4 Sueño

```kotlin
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
```

### 7.5 Flujo de Sincronización

```
┌──────────────────┐
│ Usuario presiona │
│ "Sincronizar"    │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐     NO     ┌──────────────────┐
│ ¿Health Connect  │───────────►│ Mostrar mensaje  │
│ disponible?      │            │ de error         │
└────────┬─────────┘            └──────────────────┘
         │ SÍ
         ▼
┌──────────────────┐     NO     ┌──────────────────┐
│ ¿Permisos        │───────────►│ Solicitar        │
│ concedidos?      │            │ permisos         │
└────────┬─────────┘            └──────────────────┘
         │ SÍ
         ▼
┌──────────────────┐
│ Leer datos de    │
│ Health Connect   │
│ - Steps          │
│ - HeartRate      │
│ - SpO2           │
│ - Sleep          │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Calcular nivel   │
│ de estrés        │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Guardar en       │
│ Firebase         │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Actualizar UI    │
│ con nuevos datos │
└──────────────────┘
```

---

## 8. BACKEND Y SERVICIOS EN LA NUBE

### 8.1 Firebase Authentication

#### 8.1.1 Configuración

- **Método de autenticación:** Email/Password
- **Archivo de configuración:** `google-services.json`

#### 8.1.2 Operaciones

| Operación | Método |
|-----------|--------|
| Login | `signInWithEmailAndPassword(email, password)` |
| Registro | `createUserWithEmailAndPassword(email, password)` |
| Logout | `signOut()` |
| Usuario actual | `currentUser?.uid` |

### 8.2 Firebase Firestore

#### 8.2.1 Estructura de Base de Datos

```
firestore/
├── users/
│   └── {userId}/
│       ├── profile/
│       │   ├── name: String
│       │   ├── email: String
│       │   ├── dob: String
│       │   ├── height: Double
│       │   ├── weight: Double
│       │   ├── sex: Int
│       │   ├── emergencyContact: String
│       │   └── goal: String
│       │
│       ├── health_records/
│       │   └── {recordId}/
│       │       ├── pasosDiarios: Long
│       │       ├── frecuenciaCardiaca: Long
│       │       ├── frecuenciaCardiacaMin: Long
│       │       ├── frecuenciaCardiacaMax: Long
│       │       ├── horasDeSueño: Double
│       │       ├── saturacionOxigeno: Double
│       │       ├── nivelDeEstres: Int
│       │       ├── relojColocado: Boolean
│       │       ├── fecha: String
│       │       ├── horaRegistro: String
│       │       └── lastUpdated: Timestamp
│       │
│       └── messagesIa/
│           └── {messageId}/
│               ├── message: String
│               └── timestamp: Timestamp
```

#### 8.2.2 Queries Utilizadas

**Cargar último registro:**
```kotlin
firestore.collection("users").document(userId)
    .collection("health_records")
    .orderBy("lastUpdated", Query.Direction.DESCENDING)
    .limit(1)
    .get()
```

**Cargar historial:**
```kotlin
firestore.collection("users").document(userId)
    .collection("health_records")
    .orderBy("lastUpdated", Query.Direction.DESCENDING)
    .limit(20)
    .get()
```

**Cargar registros por fecha:**
```kotlin
firestore.collection("users").document(userId)
    .collection("health_records")
    .whereEqualTo("fecha", today)
    .get()
```

### 8.3 Firebase App Check

Configuración de seguridad para proteger las APIs:

```kotlin
// Play Integrity para producción
implementation("com.google.firebase:firebase-appcheck-playintegrity")

// Debug provider para desarrollo
implementation("com.google.firebase:firebase-appcheck-debug")
```

---

## 9. API DE INTELIGENCIA ARTIFICIAL

### 9.1 Descripción del Servicio

El análisis de salud con IA se realiza a través de un servicio REST desplegado en **Google Cloud Run**. Este servicio procesa los datos de salud del usuario almacenados en Firestore y genera recomendaciones personalizadas.

### 9.2 Endpoint

| Atributo | Valor |
|----------|-------|
| **URL Base** | `https://health-api-409458732489.us-central1.run.app/` |
| **Endpoint** | `POST /procesar_usuario` |
| **Región** | us-central1 |
| **Timeout** | 30 segundos |

### 9.3 Configuración del Cliente Retrofit

```kotlin
object HealthApiClient {
    private const val BASE_URL = "https://health-api-409458732489.us-central1.run.app/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val api: HealthApiService by lazy {
        retrofit.create(HealthApiService::class.java)
    }
}
```

### 9.4 Interfaz del Servicio

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

### 9.5 Modelos de Datos

#### Request

```kotlin
data class UserRequest(
    val user_id: String
)
```

#### Response

```kotlin
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
```

### 9.6 Manejo de Errores

```kotlin
try {
    val response = HealthApiClient.api.procesarUsuario(UserRequest(userId))
    onResult(response.mensaje)
    
} catch (e: retrofit2.HttpException) {
    val errorMessage = when (e.code()) {
        404 -> "❌ No hay registros de salud para analizar."
        500 -> "❌ Error en el servidor. Intenta más tarde."
        else -> "❌ Error: ${e.message()}"
    }
    onResult(errorMessage)
    
} catch (e: java.net.UnknownHostException) {
    onResult("❌ Sin conexión a internet.")
    
} catch (e: java.net.SocketTimeoutException) {
    onResult("❌ Tiempo de espera agotado.")
    
} catch (e: Exception) {
    onResult("❌ Error: ${e.message}")
}
```

### 9.7 Flujo de Análisis con IA

```
┌──────────────────────┐
│  Usuario presiona    │
│  "Analizar"          │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  isAnalyzing = true  │
│  Mostrar spinner     │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  POST /procesar_     │
│  usuario             │
│  { user_id: "..." }  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐     ┌──────────────────────┐
│  Cloud Run procesa   │────►│  IA analiza datos    │
│  la solicitud        │     │  de Firestore        │
└──────────┬───────────┘     └──────────────────────┘
           │
           ▼
┌──────────────────────┐
│  Respuesta:          │
│  {                   │
│    estado_general,   │
│    detalles,         │
│    mensaje           │
│  }                   │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Actualizar          │
│  iaMessage en UI     │
│  isAnalyzing = false │
└──────────────────────┘
```

---

## 10. MODELO DE DATOS

### 10.1 HealthData (Data Class Principal)

```kotlin
data class HealthData(
    val pasosDiarios: Long = 0,
    val horasDeSueño: Double = 0.0,
    val saturacionOxigeno: Double = 0.0,
    val frecuenciaCardiaca: Long = 0,
    val frecuenciaCardiacaMax: Long = 0,
    val frecuenciaCardiacaMin: Long = 0,
    val relojColocado: Boolean = false,
    val nivelDeEstres: Int = 0,
    val horaRegistro: String = "",
    val fecha: String = "",
    val peso: Double = 0.0,
    val altura: Double = 0.0,
    @ServerTimestamp
    val lastUpdated: Date? = null
)
```

### 10.2 HealthData para UI (Compose)

```kotlin
data class HealthData(
    val steps: Long = 0,
    val heartRate: Long = 0,
    val heartRateMin: Long = 0,
    val heartRateMax: Long = 0,
    val sleepHours: Double = 0.0,
    val spO2: Double = 0.0,
    val stressLevel: Int = 0,
    val isWatchConnected: Boolean = false
)
```

### 10.3 HistoryRecord

```kotlin
data class HistoryRecord(
    val id: String = "",
    val fecha: String = "",
    val horaRegistro: String = "",
    val pasos: Long = 0,
    val frecuenciaCardiaca: Long = 0,
    val horasSueno: Double = 0.0,
    val spo2: Double = 0.0,
    val estres: Int = 0
)
```

### 10.4 RegisterData

```kotlin
data class RegisterData(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val dob: String = "",
    val height: String = "",
    val weight: String = "",
    val goal: String = "",
    val sex: Int = -1,
    val emergencyContact: String = ""
)
```

### 10.5 Cálculo de Nivel de Estrés

```kotlin
private fun calculateStress(heartRate: Int): Int {
    return when {
        heartRate < 60 -> 20   // Muy bajo
        heartRate < 70 -> 30   // Bajo
        heartRate < 80 -> 45   // Normal
        heartRate < 90 -> 60   // Moderado
        else -> 75             // Alto
    }
}
```

**Tabla de Niveles de Estrés:**

| BPM | Nivel | Categoría |
|-----|-------|-----------|
| < 60 | 20 | Muy bajo |
| 60-69 | 30 | Bajo |
| 70-79 | 45 | Normal |
| 80-89 | 60 | Moderado |
| ≥ 90 | 75 | Alto |

---

## 11. SISTEMA DE NOTIFICACIONES

### 11.1 HealthReminderWorker

**Archivo:** `HealthReminderWorker.kt`

**Propósito:** Enviar recordatorios periódicos para que el usuario registre sus datos de salud.

### 11.2 Configuración de WorkManager

```kotlin
class HealthReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "health_reminders"
        const val NOTIFICATION_ID = 1001
    }
}
```

### 11.3 Horario de Notificaciones

- **Rango activo:** 8:00 AM - 12:00 AM (medianoche)
- **Mensaje especial a medianoche:** Recordatorio de colocar el reloj antes de dormir

### 11.4 Mensajes Motivacionales

La aplicación cuenta con 15 mensajes motivacionales diferentes que se seleccionan aleatoriamente:

```kotlin
private val motivationalMessages = listOf(
    "💪 ¡Es hora de medir tu salud! Toma tu oxígeno en sangre y frecuencia cardíaca.",
    "❤️ Registra tus datos de salud ahora. Tu bienestar es importante.",
    "🩺 ¡Momento de chequeo! Mide tu SpO2 y ritmo cardíaco.",
    // ... 12 mensajes más
)
```

### 11.5 Canal de Notificaciones

```kotlin
private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Samsung Machine - Recordatorios",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones para recordarte registrar tus datos de salud"
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
```

### 11.6 Permisos de Notificaciones

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

---

## 12. SEGURIDAD Y AUTENTICACIÓN

### 12.1 Firebase Authentication

#### Flujo de Autenticación

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Usuario   │────►│   App       │────►│  Firebase   │
│   Login     │     │   Client    │     │  Auth       │
└─────────────┘     └─────────────┘     └─────────────┘
                           │                    │
                           │  signInWith        │
                           │  EmailAndPassword  │
                           │───────────────────►│
                           │                    │
                           │     AuthResult     │
                           │◄───────────────────│
                           │                    │
                           │     uid, token     │
                           │◄───────────────────│
```

### 12.2 Protección de Datos

| Medida | Implementación |
|--------|----------------|
| **Autenticación** | Firebase Auth con email/password |
| **Autorización** | Firestore Security Rules por userId |
| **Datos en tránsito** | HTTPS/TLS |
| **API Protection** | Firebase App Check |

### 12.3 Reglas de Seguridad Firestore (Recomendadas)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      match /health_records/{recordId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /messagesIa/{messageId} {
        allow read: if request.auth != null && request.auth.uid == userId;
        allow write: if false; // Solo backend puede escribir
      }
    }
  }
}
```

### 12.4 Activity Alias para Permisos de Salud

```xml
<activity-alias
    android:name="ViewPermissionUsageActivity"
    android:exported="true"
    android:targetActivity=".DashboardActivity"
    android:permission="android.permission.START_VIEW_PERMISSION_USAGE">
    <intent-filter>
        <action android:name="android.intent.action.VIEW_PERMISSION_USAGE" />
        <category android:name="android.intent.category.HEALTH_PERMISSIONS" />
    </intent-filter>
</activity-alias>
```

---

## 13. RENDIMIENTO Y OPTIMIZACIÓN

### 13.1 Estrategias de Optimización

| Área | Estrategia | Implementación |
|------|------------|----------------|
| **UI** | Composición diferida | `remember`, `LaunchedEffect` |
| **Red** | Timeout configurado | 30 segundos |
| **Firestore** | Queries limitadas | `.limit(20)` |
| **Imágenes** | Recursos optimizados | WebP, drawable-xxhdpi |

### 13.2 Gestión de Estado en Compose

```kotlin
// Estado reactivo con recomposición eficiente
var healthData by remember { mutableStateOf(HealthData()) }
var isLoading by remember { mutableStateOf(false) }

// Carga inicial una sola vez
LaunchedEffect(Unit) {
    loadHealthDataFromFirebase { data, hrChart, sleepChart, ia ->
        healthData = data
        heartRateChartData = hrChart
        sleepChartData = sleepChart
        iaMessage = ia
    }
}
```

### 13.3 Coroutines y Lifecycle

```kotlin
lifecycleScope.launch {
    try {
        // Operación asíncrona
        val response = HealthApiClient.api.procesarUsuario(request)
        onResult(response.mensaje)
    } catch (e: Exception) {
        // Manejo de errores
    }
}
```

### 13.4 Lazy Loading en Historial

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(dayRecords.sortedByDescending { it.horaRegistro }) { record ->
        HistoryRecordCard(record = record)
    }
}
```

---

## 14. CONFIGURACIÓN Y DESPLIEGUE

### 14.1 Requisitos del Sistema

| Componente | Requisito Mínimo |
|------------|------------------|
| **Android** | API 29 (Android 10) |
| **Target SDK** | API 36 |
| **Kotlin** | 2.0.21 |
| **Gradle** | 8.13.0 |
| **Java** | JDK 11 |

### 14.2 Variables de Entorno

**local.properties:**
```properties
sdk.dir=C\:\\Users\\[USER]\\AppData\\Local\\Android\\Sdk
```

### 14.3 Configuración de Firebase

1. Crear proyecto en Firebase Console
2. Registrar aplicación Android con `applicationId`
3. Descargar `google-services.json`
4. Colocar en `app/google-services.json`
5. Habilitar Authentication (Email/Password)
6. Crear base de datos Firestore

### 14.4 Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

### 14.5 Firma de la Aplicación

Para producción, configurar en `build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = "..."
            keyAlias = "..."
            keyPassword = "..."
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(...)
        }
    }
}
```

---

## 15. MANTENIMIENTO Y ESCALABILIDAD

### 15.1 Versionado

- **Código:** Git con branch `feature/health-connect`
- **Versión actual:** 1.0 (versionCode: 1)
- **Tag más reciente:** v1.4.0-shadcn-ui

### 15.2 Áreas de Mejora Futura

| Área | Mejora Propuesta |
|------|------------------|
| **Arquitectura** | Implementar MVVM completo con ViewModels |
| **Testing** | Agregar unit tests y UI tests |
| **Offline** | Implementar Room para caché local |
| **Analytics** | Integrar Firebase Analytics |
| **Reportes** | Exportación de datos a PDF |
| **Wearables** | Soporte para más dispositivos |

### 15.3 Escalabilidad

| Componente | Estrategia de Escalabilidad |
|------------|---------------------------|
| **Firebase** | Auto-scaling incluido |
| **Cloud Run** | Escalado automático horizontal |
| **Firestore** | Indexación para queries complejas |

### 15.4 Monitoreo

- **Firebase Crashlytics:** Reportes de crashes
- **Firebase Performance:** Métricas de rendimiento
- **Cloud Logging:** Logs de API en Cloud Run

---

## ANEXOS

### A. Dependencias Completas

```kotlin
dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)

    // Health Connect
    implementation("androidx.health.connect:connect-client:1.1.0-alpha10")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-appcheck-debug")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

### B. Catálogo de Versiones

```toml
[versions]
agp = "8.13.0"
kotlin = "2.0.21"
coreKtx = "1.17.0"
composeBom = "2024.06.00"
lifecycleRuntime = "2.8.0"
activityCompose = "1.9.0"
navigationCompose = "2.7.7"
```

### C. Permisos del Manifiesto

```xml
<!-- Básicos -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Notificaciones -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Health Connect -->
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE" />
<uses-permission android:name="android.permission.health.READ_SLEEP" />
<uses-permission android:name="android.permission.health.READ_WEIGHT" />
<uses-permission android:name="android.permission.health.READ_HEIGHT" />
<uses-permission android:name="android.permission.health.READ_EXERCISE" />
<uses-permission android:name="android.permission.health.READ_OXYGEN_SATURATION" />
```

---

## GLOSARIO

| Término | Definición |
|---------|------------|
| **BPM** | Beats Per Minute - Latidos por minuto |
| **SpO2** | Saturación de oxígeno en sangre |
| **Health Connect** | API de Google para datos de salud unificados |
| **Firestore** | Base de datos NoSQL de Firebase |
| **Cloud Run** | Servicio serverless de Google Cloud |
| **Compose** | Framework de UI declarativa para Android |
| **Material 3** | Sistema de diseño de Google |
| **shadcn/ui** | Sistema de diseño minimalista |
| **Retrofit** | Cliente HTTP para Android |
| **WorkManager** | API para tareas en background |

---

**Documento generado:** 26 de Noviembre de 2025  
**Versión del documento:** 1.0  
**Autor:** Equipo de Desarrollo Samsung Machine
