package com.example.proyectotitulo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectotitulo.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // You can add logic for your dashboard buttons here, for example:
        binding.buttonUpdate.setOnClickListener {
            // Add logic to refresh data
        }

        binding.buttonHistory.setOnClickListener {
            // Add logic to navigate to a history screen
        }

        // --- Logout Button Click Listener ---
        binding.buttonLogout.setOnClickListener {
            // Sign out from Firebase
            firebaseAuth.signOut()

            // Navigate back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            // Clear the activity stack to prevent the user from going back to the dashboard with the back button
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Close the DashboardActivity
        }
    }
}
