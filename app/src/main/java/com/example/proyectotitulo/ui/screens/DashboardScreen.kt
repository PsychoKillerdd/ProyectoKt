package com.example.proyectotitulo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectotitulo.ui.components.*
import com.example.proyectotitulo.ui.theme.*

/**
 * Datos de salud para el Dashboard
 */
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    greeting: String,
    dateText: String,
    healthData: HealthData,
    heartRateChartData: List<Pair<String, Float>>,
    sleepChartData: List<Pair<String, Float>>,
    iaMessage: String,
    onSyncClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    isLoading: Boolean = false,
    isAnalyzing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    WatchStatusIndicator(isConnected = healthData.isWatchConnected)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Header
                DashboardHeader(
                    greeting = greeting,
                    dateText = dateText
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Grid de métricas (2x2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HealthMetricCard(
                        title = "Pasos",
                        value = formatNumber(healthData.steps),
                        unit = "pasos",
                        icon = "👟",
                        backgroundColor = CardBackground,
                        accentColor = StepsAccent,
                        subtitle = "Hoy",
                        modifier = Modifier.weight(1f)
                    )
                    
                    HealthMetricCard(
                        title = "Ritmo Cardíaco",
                        value = healthData.heartRate.toString(),
                        unit = "bpm",
                        icon = "❤️",
                        backgroundColor = CardBackground,
                        accentColor = HeartRateAccent,
                        subtitle = "${healthData.heartRateMin}-${healthData.heartRateMax}",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HealthMetricCard(
                        title = "Sueño",
                        value = String.format("%.1f", healthData.sleepHours),
                        unit = "horas",
                        icon = "😴",
                        backgroundColor = CardBackground,
                        accentColor = SleepAccent,
                        modifier = Modifier.weight(1f)
                    )
                    
                    HealthMetricCard(
                        title = "SpO2",
                        value = String.format("%.0f", healthData.spO2),
                        unit = "%",
                        icon = "💨",
                        backgroundColor = CardBackground,
                        accentColor = SpO2Accent,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Estrés
                HealthMetricCard(
                    title = "Nivel de Estrés",
                    value = healthData.stressLevel.toString(),
                    unit = getStressLabel(healthData.stressLevel),
                    icon = "🧘",
                    backgroundColor = CardBackground,
                    accentColor = getStressColor(healthData.stressLevel),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Gráfico de Frecuencia Cardíaca
                ChartCard(
                    title = "Frecuencia Cardíaca",
                    subtitle = "Hoy",
                    icon = "❤️",
                    backgroundColor = CardBackground,
                    accentColor = HeartRateAccent
                ) {
                    HeartRateLineChart(
                        data = heartRateChartData,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Gráfico de Sueño
                ChartCard(
                    title = "Horas de Sueño",
                    subtitle = "Últimos 5 días",
                    icon = "😴",
                    backgroundColor = CardBackground,
                    accentColor = SleepAccent
                ) {
                    SleepBarChart(
                        data = sleepChartData,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Mensaje de IA con botón de análisis
                AIMessageCard(
                    message = iaMessage,
                    onAnalyzeClick = onAnalyzeClick,
                    isAnalyzing = isAnalyzing,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        text = "Sincronizar",
                        icon = "🔄",
                        backgroundColor = Primary,
                        onClick = onSyncClick,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Botón outline style
                    OutlinedButton(
                        onClick = onHistoryClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                    ) {
                        Text(
                            text = "📋 Historial",
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        modifier = Modifier.border(1.dp, Border, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Sincronizando...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatNumber(number: Long): String {
    return when {
        number >= 1000 -> String.format("%.1fk", number / 1000.0)
        else -> number.toString()
    }
}

private fun getStressLabel(level: Int): String {
    return when {
        level < 30 -> "Bajo"
        level < 60 -> "Moderado"
        else -> "Alto"
    }
}

private fun getStressColor(level: Int): Color {
    return when {
        level < 30 -> Success
        level < 60 -> Warning
        else -> Error
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    HealthTrackTheme {
        DashboardScreen(
            greeting = "Buenos días",
            dateText = "24 de Noviembre",
            healthData = HealthData(
                steps = 8542,
                heartRate = 72,
                heartRateMin = 58,
                heartRateMax = 95,
                sleepHours = 7.5,
                spO2 = 98.0,
                stressLevel = 35,
                isWatchConnected = true
            ),
            heartRateChartData = listOf(
                "08:00" to 65f,
                "10:00" to 72f,
                "12:00" to 78f,
                "14:00" to 70f,
                "16:00" to 85f
            ),
            sleepChartData = listOf(
                "Lun" to 7.5f,
                "Mar" to 6.8f,
                "Mié" to 8.0f,
                "Jue" to 7.2f,
                "Vie" to 6.5f
            ),
            iaMessage = "Tu ritmo cardíaco se ha mantenido estable hoy. Recuerda mantener una buena hidratación durante el día.",
            onSyncClick = { },
            onHistoryClick = { },
            onLogoutClick = { },
            onAnalyzeClick = { }
        )
    }
}
