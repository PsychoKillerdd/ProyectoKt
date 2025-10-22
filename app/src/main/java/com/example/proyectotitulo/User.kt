package com.example.proyectotitulo

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data class to represent a user.
 *
 * @property userId Unique identifier for the user (usually from Firebase Auth).
 * @property name User's full name.
 * @property dob Date of birth.
 * @property email User's email address.
 * @property height User's height in centimeters.
 * @property weight User's weight in kilograms.
 * @property goal User's personal fitness or health goal.
 * @property sex User's sex (0 for female, 1 for male).
 * @property creationDate Timestamp of when the user account was created.
 */
data class User(
    val userId: String = "",
    val name: String = "",
    val dob: String = "", // Storing as String for simplicity, can be converted to Date object
    val email: String = "",
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val goal: String = "",
    val sex: Int = -1, // -1 indicates not set
    @ServerTimestamp
    val creationDate: Date? = null
)