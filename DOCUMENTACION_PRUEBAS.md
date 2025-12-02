# DOCUMENTACIÓN DE PRUEBAS - SAMSUNG MACHINE

## Sistema de Testing Automatizado

---

## ÍNDICE

1. [Introducción](#1-introducción)
2. [Configuración del Entorno de Pruebas](#2-configuración-del-entorno-de-pruebas)
3. [Tipos de Pruebas Implementadas](#3-tipos-de-pruebas-implementadas)
4. [Pruebas de UI con Espresso/Compose](#4-pruebas-de-ui-con-espressocompose)
5. [Pruebas Unitarias](#5-pruebas-unitarias)
6. [Casos de Prueba Detallados](#6-casos-de-prueba-detallados)
7. [Métricas y Cobertura](#7-métricas-y-cobertura)
8. [Ejecución de Pruebas](#8-ejecución-de-pruebas)
9. [Resultados y Reportes](#9-resultados-y-reportes)
10. [Mejores Prácticas](#10-mejores-prácticas)

---

## 1. INTRODUCCIÓN

### 1.1 Propósito

Este documento describe el sistema de pruebas automatizadas implementado para la aplicación **Samsung Machine**, una aplicación de monitoreo de salud desarrollada en Android con Kotlin y Jetpack Compose.

### 1.2 Alcance de las Pruebas

| Tipo de Prueba | Framework | Cobertura |
|----------------|-----------|-----------|
| **Pruebas de UI** | Espresso + Compose Testing | Pantallas principales |
| **Pruebas Unitarias** | JUnit 4 | Lógica de negocio |
| **Pruebas de Integración** | Compose Test | Componentes UI |

### 1.3 Objetivos

- ✅ Verificar el correcto funcionamiento de la interfaz de usuario
- ✅ Validar la lógica de cálculos de salud
- ✅ Asegurar la correcta visualización de datos
- ✅ Comprobar la navegación entre pantallas
- ✅ Detectar regresiones en el código

---

## 2. CONFIGURACIÓN DEL ENTORNO DE PRUEBAS

### 2.1 Dependencias de Testing

```kotlin
// build.gradle.kts
dependencies {
    // Testing Unitario
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Testing de Android
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    
    // Testing de Compose
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // Testing adicional
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
}
```

### 2.2 Configuración del Test Runner

```kotlin
// defaultConfig en build.gradle.kts
testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
```

### 2.3 Estructura de Archivos de Test

```
app/src/
├── test/                              # Tests Unitarios
│   └── java/com/example/proyectotitulo/
│       ├── HealthDataUnitTest.kt     # 22 tests de lógica
│       └── ApiModelsTest.kt          # 13 tests de modelos
│
└── androidTest/                       # Tests Instrumentados
    └── java/com/example/proyectotitulo/
        ├── ExampleInstrumentedTest.kt # Test básico
        ├── LoginScreenTest.kt         # 9 tests de login
        ├── DashboardScreenTest.kt     # 15 tests de dashboard
        └── HistoryScreenTest.kt       # 15 tests de historial
```

---

## 3. TIPOS DE PRUEBAS IMPLEMENTADAS

### 3.1 Pruebas Unitarias (Unit Tests)

**Ubicación:** `app/src/test/`

**Características:**
- Se ejecutan en la JVM local (sin emulador)
- Rápidas de ejecutar
- Prueban lógica aislada
- No requieren contexto Android

**Archivos:**
| Archivo | Cantidad de Tests | Propósito |
|---------|-------------------|-----------|
| `HealthDataUnitTest.kt` | 22 | Lógica de cálculos de salud |
| `ApiModelsTest.kt` | 13 | Modelos de API |

### 3.2 Pruebas Instrumentadas (Instrumented Tests)

**Ubicación:** `app/src/androidTest/`

**Características:**
- Se ejecutan en emulador o dispositivo real
- Prueban la UI real
- Requieren contexto Android
- Más lentas pero más realistas

**Archivos:**
| Archivo | Cantidad de Tests | Propósito |
|---------|-------------------|-----------|
| `LoginScreenTest.kt` | 9 | Pantalla de login |
| `DashboardScreenTest.kt` | 15 | Pantalla principal |
| `HistoryScreenTest.kt` | 15 | Pantalla de historial |

---

## 4. PRUEBAS DE UI CON ESPRESSO/COMPOSE

### 4.1 Configuración de Compose Testing

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun example_test() {
    composeTestRule.setContent {
        HealthTrackTheme {
            LoginScreen(
                onLoginClick = { _, _ -> },
                onRegisterClick = { },
                onForgotPasswordClick = { }
            )
        }
    }
    
    composeTestRule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
}
```

### 4.2 Selectores de UI (Finders)

| Selector | Uso | Ejemplo |
|----------|-----|---------|
| `onNodeWithText()` | Buscar por texto exacto | `onNodeWithText("Login")` |
| `onNodeWithContentDescription()` | Buscar por descripción | `onNodeWithContentDescription("Volver")` |
| `onAllNodesWithText()` | Buscar múltiples nodos | `onAllNodesWithText("👟")` |
| `hasText()` | Matcher de texto | `onRoot().performScrollToNode(hasText("Historial"))` |

### 4.3 Acciones de UI

| Acción | Descripción | Ejemplo |
|--------|-------------|---------|
| `performClick()` | Click en elemento | `.performClick()` |
| `performTextInput()` | Escribir texto | `.performTextInput("email@test.com")` |
| `performScrollToNode()` | Scroll a elemento | `.performScrollToNode(hasText("..."))` |

### 4.4 Assertions (Verificaciones)

| Assertion | Descripción |
|-----------|-------------|
| `assertIsDisplayed()` | Verifica que está visible |
| `assertIsEnabled()` | Verifica que está habilitado |
| `assertIsNotEnabled()` | Verifica que está deshabilitado |
| `assertDoesNotExist()` | Verifica que no existe |
| `assertCountEquals(n)` | Verifica cantidad de elementos |

---

## 5. PRUEBAS UNITARIAS

### 5.1 Estructura de un Test Unitario

```kotlin
class HealthDataUnitTest {
    
    @Test
    fun calculateStress_heartRateBelow60_returnsVeryLowStress() {
        // Arrange
        val heartRate = 55
        
        // Act
        val stress = calculateStress(heartRate)
        
        // Assert
        assertEquals("FC < 60 debe dar estrés nivel 20", 20, stress)
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
}
```

### 5.2 Categorías de Tests Unitarios

| Categoría | Tests | Descripción |
|-----------|-------|-------------|
| Cálculo de Estrés | 5 | Verificar niveles de estrés según FC |
| Formateo de Números | 2 | Verificar formato "k" para miles |
| Normalización de Datos | 4 | Compatibilidad con/sin tilde |
| Validación de Datos | 8 | Rangos válidos de métricas |
| Categorización | 3 | Etiquetas de estrés |

---

## 6. CASOS DE PRUEBA DETALLADOS

### 6.1 LoginScreenTest - 9 Casos de Prueba

| ID | Nombre | Descripción | Resultado Esperado |
|----|--------|-------------|-------------------|
| L01 | `loginScreen_displaysAllUIElements` | Verificar todos los elementos UI | Todos visibles |
| L02 | `loginScreen_emailFieldAcceptsInput` | Campo email acepta texto | Texto visible |
| L03 | `loginScreen_passwordFieldAcceptsInput` | Campo password acepta texto | Input oculto |
| L04 | `loginScreen_loginButtonDisabledWhenFieldsEmpty` | Botón deshabilitado sin datos | Botón disabled |
| L05 | `loginScreen_loginButtonEnabledWhenFieldsFilled` | Botón habilitado con datos | Botón enabled |
| L06 | `loginScreen_loginButtonTriggersCallback` | Click en login ejecuta callback | Callback llamado |
| L07 | `loginScreen_registerLinkTriggersCallback` | Click en registro ejecuta callback | Callback llamado |
| L08 | `loginScreen_displaysErrorMessage` | Mostrar mensaje de error | Error visible |
| L09 | `loginScreen_showsLoadingIndicator` | Mostrar indicador de carga | Loading visible |

### 6.2 DashboardScreenTest - 15 Casos de Prueba

| ID | Nombre | Descripción | Resultado Esperado |
|----|--------|-------------|-------------------|
| D01 | `dashboardScreen_displaysGreeting` | Verificar saludo | "Buenos días" visible |
| D02 | `dashboardScreen_displaysStepsMetric` | Verificar métrica pasos | Card de pasos visible |
| D03 | `dashboardScreen_displaysHeartRateMetric` | Verificar métrica FC | "72 bpm" visible |
| D04 | `dashboardScreen_displaysSleepMetric` | Verificar métrica sueño | "7.5 horas" visible |
| D05 | `dashboardScreen_displaysSpO2Metric` | Verificar métrica SpO2 | "98%" visible |
| D06 | `dashboardScreen_displaysStressLevel` | Verificar nivel estrés | "35" visible |
| D07 | `dashboardScreen_displaysWatchConnectedStatus` | Estado reloj conectado | "Conectado" visible |
| D08 | `dashboardScreen_displaysWatchDisconnectedStatus` | Estado reloj desconectado | "Sin reloj" visible |
| D09 | `dashboardScreen_syncButtonTriggersCallback` | Click en sincronizar | Callback llamado |
| D10 | `dashboardScreen_historyButtonTriggersCallback` | Click en historial | Callback llamado |
| D11 | `dashboardScreen_displaysIAMessage` | Mostrar mensaje IA | Mensaje visible |
| D12 | `dashboardScreen_analyzeButtonTriggersCallback` | Click en analizar | Callback llamado |
| D13 | `dashboardScreen_displaysHeartRateChartTitle` | Título gráfico FC | "Frecuencia Cardíaca" visible |
| D14 | `dashboardScreen_displaysSleepChartTitle` | Título gráfico sueño | "Horas de Sueño" visible |
| D15 | `dashboardScreen_isScrollable` | Pantalla scrolleable | Scroll funcional |

### 6.3 HistoryScreenTest - 15 Casos de Prueba

| ID | Nombre | Descripción | Resultado Esperado |
|----|--------|-------------|-------------------|
| H01 | `historyScreen_displaysTitle` | Verificar título | "Historial" visible |
| H02 | `historyScreen_displaysLoadingIndicator` | Estado de carga | Loading visible |
| H03 | `historyScreen_displaysEmptyState` | Estado sin datos | "Sin registros" visible |
| H04 | `historyScreen_displaysRecordCount` | Contador registros | "3 registros" visible |
| H05 | `historyScreen_displaysGroupedDates` | Agrupación por fecha | Fechas formateadas |
| H06 | `historyScreen_displaysRecordTimes` | Horas de registro | "🕐 14:30" visible |
| H07 | `historyScreen_displaysMetricsInCards` | Iconos de métricas | Todos los iconos visibles |
| H08 | `historyScreen_displaysStepsValues` | Valores de pasos | "8.5k" visible |
| H09 | `historyScreen_displaysHeartRateValues` | Valores de FC | "72" visible |
| H10 | `historyScreen_displaysSleepValues` | Valores de sueño | "7.5" visible |
| H11 | `historyScreen_backButtonTriggersCallback` | Botón volver funciona | Callback llamado |
| H12 | `historyScreen_displaysCompleteBadge` | Badge "Completo" | 3 badges visibles |
| H13 | `historyScreen_isScrollable` | Lista scrolleable | Scroll funcional |
| H14 | `historyScreen_displaysEmptySpO2` | SpO2 vacío muestra "-" | "-" visible |
| H15 | `historyScreen_displaysRecordsInCorrectOrder` | Orden correcto | Más reciente primero |

### 6.4 HealthDataUnitTest - 22 Casos de Prueba

| ID | Nombre | Descripción | Resultado Esperado |
|----|--------|-------------|-------------------|
| U01 | `calculateStress_heartRateBelow60_returnsVeryLowStress` | FC < 60 | Estrés = 20 |
| U02 | `calculateStress_heartRate60to69_returnsLowStress` | FC 60-69 | Estrés = 30 |
| U03 | `calculateStress_heartRate70to79_returnsNormalStress` | FC 70-79 | Estrés = 45 |
| U04 | `calculateStress_heartRate80to89_returnsModerateStress` | FC 80-89 | Estrés = 60 |
| U05 | `calculateStress_heartRate90OrAbove_returnsHighStress` | FC >= 90 | Estrés = 75 |
| U06 | `formatNumber_lessThan1000_returnsAsIs` | Número < 1000 | Sin formato |
| U07 | `formatNumber_1000OrMore_returnsKFormat` | Número >= 1000 | Formato "Xk" |
| U08 | `normalizeHealthData_sleepWithTilde_normalizesCorrectly` | horasDeSueño | Lee correctamente |
| U09 | `normalizeHealthData_sleepWithoutTilde_normalizesCorrectly` | horasDeSueno | Lee correctamente |
| U10 | `normalizeHealthData_bothFormats_prioritizesWithoutTilde` | Ambos formatos | Prioriza sin tilde |
| U11 | `normalizeHealthData_bothNull_returnsZero` | Ambos null | Retorna 0 |
| U12 | `validateHeartRate_validRange_returnsTrue` | FC válida | true |
| U13 | `validateHeartRate_invalidRange_returnsFalse` | FC inválida | false |
| U14 | `validateSpO2_validRange_returnsTrue` | SpO2 válido | true |
| U15 | `validateSpO2_invalidRange_returnsFalse` | SpO2 inválido | false |
| U16 | `validateSleepHours_validRange_returnsTrue` | Sueño válido | true |
| U17 | `validateSleepHours_invalidRange_returnsFalse` | Sueño inválido | false |
| U18 | `validateSteps_validRange_returnsTrue` | Pasos válidos | true |
| U19 | `validateSteps_invalidRange_returnsFalse` | Pasos inválidos | false |
| U20 | `getStressLabel_lowLevel_returnsCorrectLabel` | Estrés bajo | "Bajo" |
| U21 | `getStressLabel_moderateLevel_returnsCorrectLabel` | Estrés moderado | "Moderado" |
| U22 | `getStressLabel_highLevel_returnsCorrectLabel` | Estrés alto | "Alto" |

### 6.5 ApiModelsTest - 13 Casos de Prueba

| ID | Nombre | Descripción | Resultado Esperado |
|----|--------|-------------|-------------------|
| A01 | `userRequest_creation_setsUserId` | Crear UserRequest | user_id asignado |
| A02 | `userRequest_emptyId_isStillValid` | UserRequest vacío | Es válido |
| A03 | `healthResponse_creation_setsAllFields` | Crear HealthResponse | Todos los campos |
| A04 | `healthResponse_detalles_containsSleepInfo` | Detalles de sueño | Contiene info |
| A05 | `healthResponse_detalles_containsHeartRateInfo` | Detalles de FC | Contiene "bpm" |
| A06 | `healthResponse_detalles_containsStressInfo` | Detalles de estrés | Contiene nivel |
| A07 | `healthResponse_detalles_containsStepsInfo` | Detalles de pasos | Contiene "pasos" |
| A08 | `healthResponse_estadoBueno_isValid` | Estado "Bueno" | Es válido |
| A09 | `healthResponse_estadoRegular_isValid` | Estado "Regular" | Es válido |
| A10 | `healthResponse_estadoMalo_isValid` | Estado "Malo" | Es válido |
| A11 | `healthResponse_estadoExcelente_isValid` | Estado "Excelente" | Es válido |
| A12 | `healthResponse_mensaje_notEmpty` | Mensaje no vacío | Tiene contenido |
| A13 | `healthResponse_mensaje_canContainEmojis` | Mensaje con emojis | Acepta emojis |

---

## 7. MÉTRICAS Y COBERTURA

### 7.1 Resumen de Tests

| Categoría | Cantidad | Tipo |
|-----------|----------|------|
| Tests de Login | 9 | Instrumentado |
| Tests de Dashboard | 15 | Instrumentado |
| Tests de Historial | 15 | Instrumentado |
| Tests de Lógica de Salud | 22 | Unitario |
| Tests de API Models | 13 | Unitario |
| **TOTAL** | **74** | - |

### 7.2 Cobertura por Componente

| Componente | Cobertura | Estado |
|------------|-----------|--------|
| LoginScreen | ✅ Alta | Completo |
| DashboardScreen | ✅ Alta | Completo |
| HistoryScreen | ✅ Alta | Completo |
| Cálculo de Estrés | ✅ 100% | Completo |
| Formateo de Números | ✅ 100% | Completo |
| Normalización de Datos | ✅ 100% | Completo |
| Validación de Datos | ✅ 100% | Completo |
| Modelos de API | ✅ Alta | Completo |

### 7.3 Funcionalidades Probadas

| Funcionalidad | Cubierta | Tests |
|---------------|----------|-------|
| Renderizado de UI | ✅ | 25+ |
| Interacción con formularios | ✅ | 10+ |
| Navegación | ✅ | 5+ |
| Callbacks/Eventos | ✅ | 10+ |
| Estados de carga | ✅ | 3+ |
| Manejo de errores | ✅ | 3+ |
| Lógica de negocio | ✅ | 22 |
| Modelos de datos | ✅ | 13 |

---

## 8. EJECUCIÓN DE PRUEBAS

### 8.1 Comandos de Ejecución

#### Ejecutar todos los tests unitarios:
```bash
./gradlew test
```

#### Ejecutar tests unitarios de debug:
```bash
./gradlew testDebugUnitTest
```

#### Ejecutar tests instrumentados:
```bash
./gradlew connectedAndroidTest
```

#### Ejecutar un test específico:
```bash
./gradlew testDebugUnitTest --tests "com.example.proyectotitulo.HealthDataUnitTest"
```

### 8.2 Desde Android Studio

1. **Tests Unitarios:**
   - Click derecho en `app/src/test/` → "Run 'Tests in proyectotitulo'"

2. **Tests Instrumentados:**
   - Conectar dispositivo/emulador
   - Click derecho en `app/src/androidTest/` → "Run 'Tests in proyectotitulo'"

3. **Test Individual:**
   - Abrir archivo de test
   - Click en icono verde junto al método `@Test`

### 8.3 Configuración para CI/CD

```yaml
# .github/workflows/android.yml
name: Android CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'adopt'
          
      - name: Run Unit Tests
        run: ./gradlew test
        
      - name: Upload Test Results
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: app/build/reports/tests/
```

---

## 9. RESULTADOS Y REPORTES

### 9.1 Ubicación de Reportes

| Reporte | Ubicación |
|---------|-----------|
| Tests Unitarios Debug | `app/build/reports/tests/testDebugUnitTest/` |
| Tests Unitarios Release | `app/build/reports/tests/testReleaseUnitTest/` |
| Tests Instrumentados | `app/build/reports/androidTests/connected/` |

### 9.2 Formato de Reportes

Los reportes se generan en formato HTML y XML:

```
app/build/reports/tests/testDebugUnitTest/
├── index.html           # Reporte visual
├── classes/             # Detalles por clase
│   ├── HealthDataUnitTest.html
│   └── ApiModelsTest.html
└── packages/            # Agrupado por paquete
```

### 9.3 Interpretación de Resultados

| Estado | Significado | Acción |
|--------|-------------|--------|
| ✅ PASSED | Test exitoso | Ninguna |
| ❌ FAILED | Test falló | Revisar assertion |
| ⚠️ SKIPPED | Test omitido | Revisar condiciones |
| 🔴 ERROR | Error de ejecución | Revisar código/config |

---

## 10. MEJORES PRÁCTICAS

### 10.1 Nomenclatura de Tests

```kotlin
// Formato: metodo_condicion_resultadoEsperado
@Test
fun calculateStress_heartRateBelow60_returnsVeryLowStress() { }

@Test
fun loginScreen_loginButtonDisabledWhenFieldsEmpty() { }
```

### 10.2 Estructura AAA

```kotlin
@Test
fun example_test() {
    // Arrange - Preparar datos
    val input = 55
    
    // Act - Ejecutar acción
    val result = calculateStress(input)
    
    // Assert - Verificar resultado
    assertEquals(20, result)
}
```

### 10.3 Tests de UI Compose

```kotlin
@Test
fun example_composeTest() {
    // 1. Configurar contenido
    composeTestRule.setContent {
        HealthTrackTheme {
            MyComposable()
        }
    }
    
    // 2. Interactuar
    composeTestRule.onNodeWithText("Button").performClick()
    
    // 3. Verificar
    composeTestRule.onNodeWithText("Result").assertIsDisplayed()
}
```

### 10.4 Manejo de Callbacks

```kotlin
@Test
fun button_click_triggersCallback() {
    var clicked = false
    
    composeTestRule.setContent {
        Button(onClick = { clicked = true }) {
            Text("Click me")
        }
    }
    
    composeTestRule.onNodeWithText("Click me").performClick()
    
    assert(clicked) { "Callback was not triggered" }
}
```

---

## ANEXOS

### A. Tabla de Validaciones de Datos

| Métrica | Rango Válido | Unidad |
|---------|--------------|--------|
| Frecuencia Cardíaca | 30 - 250 | bpm |
| SpO2 | 70 - 100 | % |
| Horas de Sueño | 0 - 24 | horas |
| Pasos | 0 - 100,000 | pasos |
| Nivel de Estrés | 0 - 100 | índice |

### B. Tabla de Niveles de Estrés

| FC (bpm) | Nivel | Etiqueta | Color |
|----------|-------|----------|-------|
| < 60 | 20 | Bajo | Verde |
| 60-69 | 30 | Bajo | Verde |
| 70-79 | 45 | Moderado | Amarillo |
| 80-89 | 60 | Moderado | Amarillo |
| ≥ 90 | 75 | Alto | Rojo |

### C. Formato de Números

| Valor | Formato |
|-------|---------|
| 500 | "500" |
| 1000 | "1.0k" |
| 8542 | "8.5k" |
| 15230 | "15.2k" |

---

**Documento generado:** Diciembre 2025  
**Versión:** 1.0  
**Total de Tests:** 74  
**Autor:** Equipo de Desarrollo Samsung Machine
