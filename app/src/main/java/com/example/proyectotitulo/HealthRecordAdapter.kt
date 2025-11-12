package com.example.proyectotitulo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter para mostrar registros de salud en el historial
 */
class HealthRecordAdapter(private var records: List<HealthRecord>) :
    RecyclerView.Adapter<HealthRecordAdapter.HealthRecordViewHolder>() {

    class HealthRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        val textViewSteps: TextView = itemView.findViewById(R.id.textViewSteps)
        val textViewHeartRate: TextView = itemView.findViewById(R.id.textViewHeartRate)
        val textViewSleep: TextView = itemView.findViewById(R.id.textViewSleep)
        val textViewSpO2: TextView = itemView.findViewById(R.id.textViewSpO2)
        val textViewStress: TextView = itemView.findViewById(R.id.textViewStress)
        val textViewWatchStatus: TextView = itemView.findViewById(R.id.textViewWatchStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthRecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_record, parent, false)
        return HealthRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: HealthRecordViewHolder, position: Int) {
        val record = records[position]

        // Formatear fecha (Ej: "10 Nov 2025")
        holder.textViewDate.text = formatDate(record.fecha)
        
        // Hora
        holder.textViewTime.text = record.horaRegistro

        // Pasos
        holder.textViewSteps.text = String.format(Locale.getDefault(), "%,d", record.pasosDiarios)

        // Frecuencia Cardíaca
        holder.textViewHeartRate.text = if (record.frecuenciaCardiaca > 0) {
            "${record.frecuenciaCardiaca} BPM"
        } else {
            "-- BPM"
        }

        // Sueño
        holder.textViewSleep.text = if (record.horasDeSueño > 0) {
            String.format(Locale.getDefault(), "%.1fh", record.horasDeSueño)
        } else {
            "-- h"
        }

        // SpO2
        holder.textViewSpO2.text = if (record.saturacionOxigeno > 0) {
            String.format(Locale.getDefault(), "%.1f%%", record.saturacionOxigeno)
        } else {
            "-- %"
        }

        // Nivel de Estrés
        holder.textViewStress.text = if (record.nivelDeEstres > 0) {
            "${record.nivelDeEstres}/100"
        } else {
            "--/100"
        }

        // Estado del Reloj
        holder.textViewWatchStatus.text = if (record.relojColocado) {
            "Colocado ✓"
        } else {
            "No colocado"
        }
    }

    override fun getItemCount(): Int = records.size

    fun updateRecords(newRecords: List<HealthRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                
                val monthName = when (month) {
                    1 -> "Ene"
                    2 -> "Feb"
                    3 -> "Mar"
                    4 -> "Abr"
                    5 -> "May"
                    6 -> "Jun"
                    7 -> "Jul"
                    8 -> "Ago"
                    9 -> "Sep"
                    10 -> "Oct"
                    11 -> "Nov"
                    12 -> "Dic"
                    else -> ""
                }
                
                "$day $monthName $year"
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
}

/**
 * Data class para representar un registro de salud en el historial
 */
data class HealthRecord(
    val fecha: String = "",
    val horaRegistro: String = "",
    val pasosDiarios: Long = 0,
    val frecuenciaCardiaca: Long = 0,
    val horasDeSueño: Double = 0.0,
    val saturacionOxigeno: Double = 0.0,
    val nivelDeEstres: Int = 0,
    val relojColocado: Boolean = false
)
