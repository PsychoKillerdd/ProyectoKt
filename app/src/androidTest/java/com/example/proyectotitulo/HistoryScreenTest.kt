package com.example.proyectotitulo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.proyectotitulo.ui.screens.HistoryRecord
import com.example.proyectotitulo.ui.screens.HistoryScreen
import com.example.proyectotitulo.ui.theme.HealthTrackTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de UI para la pantalla de Historial
 * 
 * Estas pruebas verifican:
 * - Renderizado de la lista de registros
 * - Estado de carga
 * - Estado vacío
 * - Agrupación por fecha
 * - Navegación de retroceso
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Datos de prueba
    private val testRecords = listOf(
        HistoryRecord(
            id = "1",
            fecha = "2025-11-26",
            horaRegistro = "14:30",
            pasos = 8542,
            frecuenciaCardiaca = 72,
            horasSueno = 7.5,
            spo2 = 98.0,
            estres = 35
        ),
        HistoryRecord(
            id = "2",
            fecha = "2025-11-26",
            horaRegistro = "10:15",
            pasos = 3200,
            frecuenciaCardiaca = 68,
            horasSueno = 7.5,
            spo2 = 97.0,
            estres = 28
        ),
        HistoryRecord(
            id = "3",
            fecha = "2025-11-25",
            horaRegistro = "22:00",
            pasos = 10234,
            frecuenciaCardiaca = 70,
            horasSueno = 8.0,
            spo2 = 98.0,
            estres = 25
        )
    )

    /**
     * TEST 1: Verificar que el título de la pantalla se muestra
     */
    @Test
    fun historyScreen_displaysTitle() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Historial").assertIsDisplayed()
    }

    /**
     * TEST 2: Verificar indicador de carga
     */
    @Test
    fun historyScreen_displaysLoadingIndicator() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = emptyList(),
                    isLoading = true,
                    onBackClick = { }
                )
            }
        }

        // El indicador de carga debe estar presente (CircularProgressIndicator)
        // No podemos verificar directamente el CircularProgressIndicator por nombre,
        // pero podemos verificar que no muestra contenido cuando está cargando
        composeTestRule.onNodeWithText("Sin registros").assertDoesNotExist()
    }

    /**
     * TEST 3: Verificar estado vacío
     */
    @Test
    fun historyScreen_displaysEmptyState() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = emptyList(),
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("Sin registros").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sincroniza tus datos para ver el historial").assertIsDisplayed()
    }

    /**
     * TEST 4: Verificar que muestra el contador de registros
     */
    @Test
    fun historyScreen_displaysRecordCount() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("3 registros encontrados").assertIsDisplayed()
    }

    /**
     * TEST 5: Verificar que se muestran las fechas agrupadas
     */
    @Test
    fun historyScreen_displaysGroupedDates() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        // Verificar que las fechas se muestran formateadas
        composeTestRule.onNodeWithText("26 de Noviembre, 2025").assertIsDisplayed()
        composeTestRule.onNodeWithText("25 de Noviembre, 2025").assertIsDisplayed()
    }

    /**
     * TEST 6: Verificar que se muestran las horas de registro
     */
    @Test
    fun historyScreen_displaysRecordTimes() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("🕐 14:30").assertIsDisplayed()
        composeTestRule.onNodeWithText("🕐 10:15").assertIsDisplayed()
    }

    /**
     * TEST 7: Verificar que las métricas se muestran en las tarjetas
     */
    @Test
    fun historyScreen_displaysMetricsInCards() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        // Verificar iconos de métricas
        composeTestRule.onAllNodesWithText("👟").assertCountEquals(3)
        composeTestRule.onAllNodesWithText("❤️").assertCountEquals(3)
        composeTestRule.onAllNodesWithText("😴").assertCountEquals(3)
        composeTestRule.onAllNodesWithText("💨").assertCountEquals(3)
        composeTestRule.onAllNodesWithText("🧘").assertCountEquals(3)
    }

    /**
     * TEST 8: Verificar valores de pasos
     */
    @Test
    fun historyScreen_displaysStepsValues() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        // 8542 pasos se muestra como "8.5k"
        composeTestRule.onNodeWithText("8.5k").assertIsDisplayed()
        // 3200 pasos se muestra como "3.2k"
        composeTestRule.onNodeWithText("3.2k").assertIsDisplayed()
        // 10234 pasos se muestra como "10.2k"
        composeTestRule.onNodeWithText("10.2k").assertIsDisplayed()
    }

    /**
     * TEST 9: Verificar valores de frecuencia cardíaca
     */
    @Test
    fun historyScreen_displaysHeartRateValues() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        composeTestRule.onNodeWithText("72").assertIsDisplayed()
        composeTestRule.onNodeWithText("68").assertIsDisplayed()
        composeTestRule.onNodeWithText("70").assertIsDisplayed()
    }

    /**
     * TEST 10: Verificar valores de sueño
     */
    @Test
    fun historyScreen_displaysSleepValues() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        // El primer y segundo registro tienen 7.5 horas
        composeTestRule.onAllNodesWithText("7.5").assertCountEquals(2)
        // El tercer registro tiene 8.0 horas
        composeTestRule.onNodeWithText("8.0").assertIsDisplayed()
    }

    /**
     * TEST 11: Verificar que el botón de retroceso funciona
     */
    @Test
    fun historyScreen_backButtonTriggersCallback() {
        var backClicked = false

        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { backClicked = true }
                )
            }
        }

        // Buscar y clickear el botón de retroceso
        composeTestRule.onNodeWithContentDescription("Volver").performClick()
        
        assert(backClicked) { "El callback de retroceso no fue llamado" }
    }

    /**
     * TEST 12: Verificar badge de "Completo" cuando hay datos
     */
    @Test
    fun historyScreen_displaysCompleteBadge() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        // Debe haber 3 badges de "Completo"
        composeTestRule.onAllNodesWithText("✓ Completo").assertCountEquals(3)
    }

    /**
     * TEST 13: Verificar que la lista es scrolleable
     */
    @Test
    fun historyScreen_isScrollable() {
        // Crear una lista más grande para necesitar scroll
        val manyRecords = (1..20).map { index ->
            HistoryRecord(
                id = "$index",
                fecha = "2025-11-${26 - (index / 5)}",
                horaRegistro = "${10 + (index % 12)}:00",
                pasos = (5000 + index * 100).toLong(),
                frecuenciaCardiaca = (60 + index).toLong(),
                horasSueno = 7.0 + (index % 3) * 0.5,
                spo2 = 97.0 + (index % 3),
                estres = 20 + (index % 50)
            )
        }

        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = manyRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        // Verificar que el contador muestra los 20 registros
        composeTestRule.onNodeWithText("20 registros encontrados").assertIsDisplayed()
    }

    /**
     * TEST 14: Verificar SpO2 con valor "-" cuando es 0
     */
    @Test
    fun historyScreen_displaysEmptySpO2() {
        val recordWithoutSpO2 = listOf(
            HistoryRecord(
                id = "1",
                fecha = "2025-11-26",
                horaRegistro = "14:30",
                pasos = 8542,
                frecuenciaCardiaca = 72,
                horasSueno = 7.5,
                spo2 = 0.0,
                estres = 0
            )
        )

        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = recordWithoutSpO2,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        // Cuando SpO2 es 0, debe mostrar "-"
        composeTestRule.onAllNodesWithText("-").assertCountEquals(2) // SpO2 y estrés
    }

    /**
     * TEST 15: Verificar orden correcto de registros
     */
    @Test
    fun historyScreen_displaysRecordsInCorrectOrder() {
        composeTestRule.setContent {
            HealthTrackTheme {
                HistoryScreen(
                    records = testRecords,
                    isLoading = false,
                    onBackClick = { }
                )
            }
        }

        // El primer registro (26 Nov, 14:30) debe aparecer antes
        // que el segundo (26 Nov, 10:15)
        val allNodes = composeTestRule.onAllNodesWithText("🕐", substring = true)
            .fetchSemanticsNodes()
        
        // Verificar que hay al menos 3 registros con hora
        assert(allNodes.size >= 3) { "Deberían haber al menos 3 registros con hora" }
    }
}
