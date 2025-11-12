package com.example.proyectotitulo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectotitulo.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: HealthRecordAdapter

    private val TAG = "HistoryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Configurar RecyclerView
        adapter = HealthRecordAdapter(emptyList())
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistory.adapter = adapter

        // Botón volver
        binding.buttonBack.setOnClickListener {
            finish()
        }

        // Cargar datos
        loadHealthRecords()
    }

    private fun loadHealthRecords() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User not authenticated")
            showNoData()
            return
        }

        Log.d(TAG, "Loading health records for user: $userId")

        firestore.collection("users").document(userId)
            .collection("health_records")
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .limit(100) // Obtenemos más registros para poder filtrar
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Successfully fetched ${documents.size()} records")

                if (documents.isEmpty) {
                    showNoData()
                } else {
                    val allRecords = documents.mapNotNull { document ->
                        try {
                            HealthRecord(
                                fecha = document.getString("fecha") ?: "",
                                horaRegistro = document.getString("horaRegistro") ?: "",
                                pasosDiarios = document.getLong("pasosDiarios") ?: 0,
                                frecuenciaCardiaca = document.getLong("frecuenciaCardiaca") ?: 0,
                                horasDeSueño = document.getDouble("horasDeSueño") ?: 0.0,
                                saturacionOxigeno = document.getDouble("saturacionOxigeno") ?: 0.0,
                                nivelDeEstres = document.getLong("nivelDeEstres")?.toInt() ?: 0,
                                relojColocado = document.getBoolean("relojColocado") ?: false
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing document: ${e.message}")
                            null
                        }
                    }

                    // Filtrar registros que tengan al menos un valor > 0
                    val validRecords = allRecords.filter { record ->
                        val hasValidData = record.pasosDiarios > 0 ||
                                          record.frecuenciaCardiaca > 0 ||
                                          record.horasDeSueño > 0.0 ||
                                          record.saturacionOxigeno > 0.0
                        
                        if (!hasValidData) {
                            Log.d(TAG, "Skipping record from ${record.fecha} ${record.horaRegistro}: All values are 0")
                        }
                        hasValidData
                    }

                    // Agrupar por fecha y tomar máximo 5 por día
                    val filteredRecords = validRecords
                        .groupBy { it.fecha }
                        .flatMap { (date, records) -> 
                            records.take(5).also {
                                Log.d(TAG, "Date $date: showing ${it.size} of ${records.size} valid records")
                            }
                        }

                    if (filteredRecords.isEmpty()) {
                        showNoData()
                    } else {
                        showData()
                        adapter.updateRecords(filteredRecords)
                        Log.d(TAG, "Displaying ${filteredRecords.size} records (max 5 per day)")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading records: ${e.message}", e)
                showNoData()
            }
    }

    private fun showNoData() {
        binding.recyclerViewHistory.visibility = View.GONE
        binding.textViewNoData.visibility = View.VISIBLE
    }

    private fun showData() {
        binding.recyclerViewHistory.visibility = View.VISIBLE
        binding.textViewNoData.visibility = View.GONE
    }
}
