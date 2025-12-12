package com.example.proyectotitulo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.proyectotitulo.ui.screens.LoginScreen
import com.example.proyectotitulo.ui.theme.HealthTrackTheme
import com.google.firebase.auth.FirebaseAuth

class LoginComposeActivity : ComponentActivity() {
    
    private lateinit var firebaseAuth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        firebaseAuth = FirebaseAuth.getInstance()
        
        // Si ya está logueado, ir al Dashboard
        if (firebaseAuth.currentUser != null) {
            // Inicializar servicio de alertas
            AlertasService.init(applicationContext)
            navigateToDashboard()
            return
        }
        
        setContent {
            HealthTrackTheme {
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                
                LoginScreen(
                    onLoginClick = { email, password ->
                        isLoading = true
                        errorMessage = null
                        loginUser(email, password) { success, error ->
                            isLoading = false
                            if (success) {
                                navigateToDashboard()
                            } else {
                                errorMessage = error
                            }
                        }
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegisterComposeActivity::class.java))
                    },
                    onForgotPasswordClick = {
                        Toast.makeText(this, "Función en desarrollo", Toast.LENGTH_SHORT).show()
                    },
                    isLoading = isLoading,
                    errorMessage = errorMessage
                )
            }
        }
    }
    
    private fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            callback(false, "Por favor completa todos los campos")
            return
        }
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Inicializar servicio de alertas después del login
                    AlertasService.init(applicationContext)
                    callback(true, null)
                } else {
                    val errorMsg = when {
                        task.exception?.message?.contains("password") == true -> 
                            "Contraseña incorrecta"
                        task.exception?.message?.contains("no user record") == true -> 
                            "Usuario no encontrado"
                        task.exception?.message?.contains("email") == true -> 
                            "Correo electrónico inválido"
                        else -> 
                            "Error al iniciar sesión: ${task.exception?.message}"
                    }
                    callback(false, errorMsg)
                }
            }
    }
    
    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardComposeActivity::class.java))
        finish()
    }
}
