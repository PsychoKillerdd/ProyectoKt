package com.example.proyectotitulo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectotitulo.AlertaSalud
import com.example.proyectotitulo.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    alertas: List<AlertaSalud>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onMarcarAtendida: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Alertas de Salud",
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
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                alertas.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Success
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "¡Todo en orden!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No tienes alertas de salud pendientes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(alertas) { alerta ->
                            AlertaCard(
                                alerta = alerta,
                                onMarcarAtendida = { onMarcarAtendida(alerta.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertaCard(
    alerta: AlertaSalud,
    onMarcarAtendida: () -> Unit
) {
    val backgroundColor = when (alerta.tipo.lowercase()) {
        "critica" -> Error.copy(alpha = 0.1f)
        "advertencia" -> Color(0xFFFFA000).copy(alpha = 0.1f)
        else -> Primary.copy(alpha = 0.05f)
    }
    
    val borderColor = when (alerta.tipo.lowercase()) {
        "critica" -> Error
        "advertencia" -> Color(0xFFFFA000)
        else -> Primary
    }
    
    val icon = when (alerta.tipo.lowercase()) {
        "critica" -> Icons.Default.Warning
        "advertencia" -> Icons.Default.Info
        else -> Icons.Default.Notifications
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header con icono y tipo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = alerta.tipo.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = borderColor
                    )
                }
                
                // Timestamp
                alerta.timestamp?.let { timestamp ->
                    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                    Text(
                        text = sdf.format(timestamp.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Título
            Text(
                text = alerta.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mensaje
            Text(
                text = alerta.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            // Valores registrados
            if (alerta.valores.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "📊 Valores Registrados",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        alerta.valores.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val label = when (key) {
                                    "frecuenciaCardiaca" -> "Frecuencia Cardíaca"
                                    "horasDeSueno" -> "Horas de Sueño"
                                    "nivelDeEstres" -> "Nivel de Estrés"
                                    "saturacionOxigeno" -> "Saturación O₂"
                                    "pasosDiarios" -> "Pasos"
                                    else -> key
                                }
                                val unit = when (key) {
                                    "frecuenciaCardiaca" -> " bpm"
                                    "horasDeSueno" -> " hrs"
                                    "nivelDeEstres" -> "/100"
                                    "saturacionOxigeno" -> "%"
                                    "pasosDiarios" -> ""
                                    else -> ""
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "$value$unit",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
            
            // Recomendación
            if (alerta.recomendacion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "💡", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = alerta.recomendacion,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            // Botón de marcar como atendida
            if (!alerta.atendida) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onMarcarAtendida,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = borderColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Marcar como Atendida")
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Atendida",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
