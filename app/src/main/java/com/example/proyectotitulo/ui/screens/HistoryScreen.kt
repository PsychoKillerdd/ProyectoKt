package com.example.proyectotitulo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectotitulo.ui.theme.*

/**
 * Modelo de datos para un registro de historial
 */
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    records: List<HistoryRecord>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Agrupar registros por fecha
    val groupedRecords = records.groupBy { it.fecha }
        .toSortedMap(reverseOrder())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Historial",
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Primary,
                            strokeWidth = 2.dp
                        )
                    }
                }
                records.isEmpty() -> {
                    EmptyHistoryPlaceholder()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${records.size} registros encontrados",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        groupedRecords.forEach { (fecha, dayRecords) ->
                            // Header del día
                            item {
                                Text(
                                    text = formatDate(fecha),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                                )
                            }
                            
                            // Registros del día
                            items(dayRecords.sortedByDescending { it.horaRegistro }) { record ->
                                HistoryRecordCard(record = record)
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRecordCard(
    record: HistoryRecord,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Hora del registro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🕐 ${record.horaRegistro}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                // Badge de estado
                if (record.frecuenciaCardiaca > 0 || record.spo2 > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Success.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "✓ Completo",
                            style = MaterialTheme.typography.labelSmall,
                            color = Success,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grid de métricas - 5 columnas compactas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CompactMetricItem(
                    icon = "👟",
                    value = formatNumber(record.pasos),
                    label = "pasos"
                )
                CompactMetricItem(
                    icon = "❤️",
                    value = "${record.frecuenciaCardiaca}",
                    label = "bpm"
                )
                CompactMetricItem(
                    icon = "😴",
                    value = String.format("%.1f", record.horasSueno),
                    label = "hrs"
                )
                CompactMetricItem(
                    icon = "💨",
                    value = if (record.spo2 > 0) String.format("%.0f", record.spo2) else "-",
                    label = "SpO2"
                )
                CompactMetricItem(
                    icon = "🧘",
                    value = if (record.estres > 0) "${record.estres}" else "-",
                    label = "estrés"
                )
            }
        }
    }
}

@Composable
fun CompactMetricItem(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontSize = 10.sp
        )
    }
}

@Composable
fun MetricItem(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
    }
}

@Composable
fun EmptyHistoryPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📭", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin registros",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sincroniza tus datos para ver el historial",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val day = parts[2].toInt()
            val month = when (parts[1].toInt()) {
                1 -> "Enero"
                2 -> "Febrero"
                3 -> "Marzo"
                4 -> "Abril"
                5 -> "Mayo"
                6 -> "Junio"
                7 -> "Julio"
                8 -> "Agosto"
                9 -> "Septiembre"
                10 -> "Octubre"
                11 -> "Noviembre"
                12 -> "Diciembre"
                else -> ""
            }
            val year = parts[0]
            "$day de $month, $year"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

private fun formatNumber(number: Long): String {
    return when {
        number >= 1000 -> String.format("%.1fk", number / 1000.0)
        else -> number.toString()
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    HealthTrackTheme {
        HistoryScreen(
            records = listOf(
                HistoryRecord(
                    fecha = "2025-11-24",
                    horaRegistro = "14:30",
                    pasos = 8542,
                    frecuenciaCardiaca = 72,
                    horasSueno = 7.5,
                    spo2 = 98.0,
                    estres = 35
                ),
                HistoryRecord(
                    fecha = "2025-11-24",
                    horaRegistro = "10:15",
                    pasos = 3200,
                    frecuenciaCardiaca = 68,
                    horasSueno = 7.5,
                    spo2 = 97.0,
                    estres = 28
                ),
                HistoryRecord(
                    fecha = "2025-11-23",
                    horaRegistro = "22:00",
                    pasos = 10234,
                    frecuenciaCardiaca = 70,
                    horasSueno = 8.0,
                    spo2 = 98.0,
                    estres = 25
                )
            ),
            isLoading = false,
            onBackClick = { }
        )
    }
}
