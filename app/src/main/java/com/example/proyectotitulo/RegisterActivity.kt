package com.example.proyectotitulo

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectotitulo.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.editTextDob.setOnClickListener {
            showDatePickerDialog()
        }

        binding.buttonRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, {
            _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.editTextDob.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun registerUser() {
        // --- 1. Get data from UI ---
        val name = binding.editTextName.text.toString().trim()
        val dob = binding.editTextDob.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()
        val height = binding.editTextHeight.text.toString().toDoubleOrNull() ?: 0.0
        val weight = binding.editTextWeight.text.toString().toDoubleOrNull() ?: 0.0
        val goal = binding.editTextGoal.text.toString().trim()
        val selectedSexId = binding.radioGroupSex.checkedRadioButtonId

        // --- 2. Validate data ---
        if (name.isEmpty() || dob.isEmpty() || email.isEmpty() || password.isEmpty() || goal.isEmpty() || selectedSexId == -1) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        // --- 3. Create user in Firebase Authentication ---
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Authentication successful, now get the user and save data to Firestore
                    val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
                    val userId = firebaseUser?.uid

                    if (userId == null) {
                        Toast.makeText(this, "Error al obtener el ID de usuario.", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    // --- 4. Create User object ---
                    val sex = if (binding.radioButtonFemale.isChecked) 0 else 1
                    val user = User(
                        userId = userId, // Use the real UID from Firebase Auth
                        name = name,
                        dob = dob,
                        email = email, // Email is already captured
                        height = height,
                        weight = weight,
                        goal = goal,
                        sex = sex
                        // creationDate will be set automatically by Firestore
                    )

                    // --- 5. Save user data to Firestore ---
                    firestore.collection("users").document(userId).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_LONG).show()
                            
                            // Navigate to the dashboard or main activity
                            val intent = Intent(this, DashboardActivity::class.java)
                            // Clear the activity stack to prevent going back to register screen
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("FirestoreError", "Error writing document", e)
                        }
                } else {
                    // Authentication failed
                    Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    Log.e("AuthError", "User creation failed", task.exception)
                }
            }
    }
}
