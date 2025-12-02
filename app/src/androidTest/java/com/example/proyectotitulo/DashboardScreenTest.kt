package com.example.proyectotitulo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.proyectotitulo.ui.screens.DashboardScreen
import com.example.proyectotitulo.ui.screens.HealthData
import com.example.proyectotitulo.ui.theme.HealthTrackTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de UI para la pantalla de Dashboard
 * 
 * Estas pruebas verifican:
 * - Renderizado de métricas de salud
 * - Visualización de gráficos
 * - Interacción con botones
 * - Estado de conexión del reloj
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Datos de prueba
    private val testHealthData = HealthData(
        steps = 8542,
        heartRate = 72,
        heartRateMin = 58,
        heartRateMax = 95,
        sleepHours = 7.5,
        spO2 = 98.0,
        stressLevel = 35,
        isWatchConnected = true
    )

    private val testHeartRateData = listOf(
        "08:00" to 65f,
        "10:00" to 72f,
        "12:00" to 78f,
        "14:00" to 70f,
        "16:00" to 85f
    )

    private val testSleepData = listOf(
        "Lun" to 7.5f,
        "Mar" to 6.8f,
        "Mié" to 8.0f,
        "Jue" to 7.2f,
        "Vie" to 6.5f
    )

    /**
     * TEST 1: Verificar que el saludo se muestra correctamente
     */
    @Test
    fun dashboardScreen_displaysGreeting() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días ☀️",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Buenos días ☀️").assertIsDisplayed()
        composeTestRule.onNodeWithText("26 de Noviembre").assertIsDisplayed()
    }

    /**
     * TEST 2: Verificar que las métricas de pasos se muestran
     */
    @Test
    fun dashboardScreen_displaysStepsMetric() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        // Verificar título de pasos
        composeTestRule.onNodeWithText("Pasos").assertIsDisplayed()
        
        // Verificar icono de pasos
        composeTestRule.onNodeWithText("👟").assertIsDisplayed()
    }

    /**
     * TEST 3: Verificar que las métricas de ritmo cardíaco se muestran
     */
    @Test
    fun dashboardScreen_displaysHeartRateMetric() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        // Verificar título
        composeTestRule.onNodeWithText("Ritmo Cardíaco").assertIsDisplayed()
        
        // Verificar valor (72 bpm)
        composeTestRule.onNodeWithText("72").assertIsDisplayed()
        composeTestRule.onNodeWithText("bpm").assertIsDisplayed()
        
        // Verificar icono
        composeTestRule.onNodeWithText("❤️").assertIsDisplayed()
    }

    /**
     * TEST 4: Verificar que las métricas de sueño se muestran
     */
    @Test
    fun dashboardScreen_displaysSleepMetric() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        // Verificar título
        composeTestRule.onNodeWithText("Sueño").assertIsDisplayed()
        
        // Verificar valor (7.5 horas)
        composeTestRule.onNodeWithText("7.5").assertIsDisplayed()
        composeTestRule.onNodeWithText("horas").assertIsDisplayed()
        
        // Verificar icono
        composeTestRule.onNodeWithText("😴").assertIsDisplayed()
    }

    /**
     * TEST 5: Verificar que las métricas de SpO2 se muestran
     */
    @Test
    fun dashboardScreen_displaysSpO2Metric() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        // Verificar título
        composeTestRule.onNodeWithText("SpO2").assertIsDisplayed()
        
        // Verificar valor (98%)
        composeTestRule.onNodeWithText("98").assertIsDisplayed()
        
        // Verificar icono
        composeTestRule.onNodeWithText("💨").assertIsDisplayed()
    }

    /**
     * TEST 6: Verificar que el nivel de estrés se muestra
     */
    @Test
    fun dashboardScreen_displaysStressLevel() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        // Verificar título
        composeTestRule.onNodeWithText("Nivel de Estrés").assertIsDisplayed()
        
        // Verificar valor (35)
        composeTestRule.onNodeWithText("35").assertIsDisplayed()
        
        // Verificar icono
        composeTestRule.onNodeWithText("🧘").assertIsDisplayed()
    }

    /**
     * TEST 7: Verificar indicador de conexión del reloj
     */
    @Test
    fun dashboardScreen_displaysWatchConnectedStatus() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData.copy(isWatchConnected = true),
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Conectado").assertIsDisplayed()
    }

    /**
     * TEST 8: Verificar indicador de reloj desconectado
     */
    @Test
    fun dashboardScreen_displaysWatchDisconnectedStatus() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData.copy(isWatchConnected = false),
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Sin reloj").assertIsDisplayed()
    }

    /**
     * TEST 9: Verificar que el botón Sincronizar funciona
     */
    @Test
    fun dashboardScreen_syncButtonTriggersCallback() {
        var syncClicked = false

        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { syncClicked = true },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Sincronizar").performClick()
        
        assert(syncClicked) { "El callback de sincronizar no fue llamado" }
    }

    /**
     * TEST 10: Verificar que el botón Historial funciona
     */
    @Test
    fun dashboardScreen_historyButtonTriggersCallback() {
        var historyClicked = false

        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { historyClicked = true },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Historial", substring = true).performClick()
        
        assert(historyClicked) { "El callback de historial no fue llamado" }
    }

    /**
     * TEST 11: Verificar que el mensaje de IA se muestra
     */
    @Test
    fun dashboardScreen_displaysIAMessage() {
        val testMessage = "Tu ritmo cardíaco está estable. ¡Buen trabajo manteniéndote activo!"

        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = testMessage,
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Análisis de IA").assertIsDisplayed()
        composeTestRule.onNodeWithText(testMessage).assertIsDisplayed()
    }

    /**
     * TEST 12: Verificar que el botón de analizar funciona
     */
    @Test
    fun dashboardScreen_analyzeButtonTriggersCallback() {
        var analyzeClicked = false

        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { analyzeClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Analizar", substring = true).performClick()
        
        assert(analyzeClicked) { "El callback de analizar no fue llamado" }
    }

    /**
     * TEST 13: Verificar título del gráfico de frecuencia cardíaca
     */
    @Test
    fun dashboardScreen_displaysHeartRateChartTitle() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Frecuencia Cardíaca").assertIsDisplayed()
    }

    /**
     * TEST 14: Verificar título del gráfico de sueño
     */
    @Test
    fun dashboardScreen_displaysSleepChartTitle() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Horas de Sueño").assertIsDisplayed()
    }

    /**
     * TEST 15: Verificar que la pantalla es scrolleable
     */
    @Test
    fun dashboardScreen_isScrollable() {
        composeTestRule.setContent {
            HealthTrackTheme {
                DashboardScreen(
                    greeting = "Buenos días",
                    dateText = "26 de Noviembre",
                    healthData = testHealthData,
                    heartRateChartData = testHeartRateData,
                    sleepChartData = testSleepData,
                    iaMessage = "Tu salud se ve bien",
                    onSyncClick = { },
                    onHistoryClick = { },
                    onLogoutClick = { },
                    onAnalyzeClick = { }
                )
            }
        }

        // Hacer scroll hacia abajo
        composeTestRule.onRoot().performScrollToNode(hasText("Historial", substring = true))
        
        // Verificar que el botón de historial es visible después del scroll
        composeTestRule.onNodeWithText("Historial", substring = true).assertIsDisplayed()
    }
}
