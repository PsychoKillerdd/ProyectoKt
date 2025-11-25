package com.example.proyectotitulo

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.proyectotitulo.ui.screens.RegisterData
import com.example.proyectotitulo.ui.screens.RegisterScreen
import com.example.proyectotitulo.ui.theme.HealthTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class RegisterComposeActivity : ComponentActivity() {
    
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        setContent {
            HealthTrackTheme {
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                
                RegisterScreen(
                    onRegisterClick = { data ->
                        isLoading = true
                        errorMessage = null
                        registerUser(data) { success, error ->
                            isLoading = false
                            if (success) {
                                navigateToDashboard()
                            } else {
                                errorMessage = error
                            }
                        }
                    },
                    onBackClick = { finish() },
                    onDatePickerClick = { onDateSelected ->
                        showDatePicker(onDateSelected)
                    },
                    isLoading = isLoading,
                    errorMessage = errorMessage
                )
            }
        }
    }
    
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                onDateSelected("$day/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun registerUser(data: RegisterData, callback: (Boolean, String?) -> Unit) {
        // Validaciones
        if (data.name.isBlank() || data.email.isBlank() || data.password.isBlank()) {
            callback(false, "Por favor completa todos los campos obligatorios")
            return
        }
        
        if (data.password.length < 6) {
            callback(false, "La contraseña debe tener al menos 6 caracteres")
            return
        }
        
        if (data.password != data.confirmPassword) {
            callback(false, "Las contraseñas no coinciden")
            return
        }
        
        // Crear usuario en Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(data.email, data.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId == null) {
                        callback(false, "Error al obtener ID de usuario")
                        return@addOnCompleteListener
                    }
                    
                    // Guardar datos en Firestore
                    val user = User(
                        userId = userId,
                        name = data.name,
                        dob = data.dob,
                        email = data.email,
                        height = data.height.toDoubleOrNull() ?: 0.0,
                        weight = data.weight.toDoubleOrNull() ?: 0.0,
                        goal = data.goal,
                        sex = data.sex,
                        emergencyContact = data.emergencyContact
                    )
                    
                    firestore.collection("users").document(userId).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                            callback(true, null)
                        }
                        .addOnFailureListener { e ->
                            callback(false, "Error al guardar datos: ${e.message}")
                        }
                } else {
                    val errorMsg = when {
                        task.exception?.message?.contains("email address is already") == true ->
                            "Este correo ya está registrado"
                        task.exception?.message?.contains("email address is badly") == true ->
                            "Correo electrónico inválido"
                        else ->
                            "Error en el registro: ${task.exception?.message}"
                    }
                    callback(false, errorMsg)
                }
            }
    }
    
    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardComposeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
