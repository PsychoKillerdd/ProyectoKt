package com.example.proyectotitulo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyectotitulo.ui.theme.*

data class RegisterData(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val dob: String = "",
    val height: String = "",
    val weight: String = "",
    val goal: String = "",
    val sex: Int = -1, // 0 = Femenino, 1 = Masculino
    val emergencyContact: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (RegisterData) -> Unit,
    onBackClick: () -> Unit,
    onDatePickerClick: (onDateSelected: (String) -> Unit) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var registerData by remember { mutableStateOf(RegisterData()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Crear Cuenta",
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card de información personal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Border, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Header de sección
                        Text(
                            text = "Información Personal",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Cuéntanos sobre ti",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Nombre
                        Text(
                            text = "Nombre Completo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = registerData.name,
                            onValueChange = { registerData = registerData.copy(name = it) },
                            placeholder = { Text("Tu nombre", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Border,
                                cursorColor = Primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Email
                        Text(
                            text = "Correo Electrónico",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = registerData.email,
                            onValueChange = { registerData = registerData.copy(email = it) },
                            placeholder = { Text("correo@ejemplo.com", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Border,
                                cursorColor = Primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Fecha de nacimiento
                        Text(
                            text = "Fecha de Nacimiento",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = registerData.dob,
                            onValueChange = { },
                            placeholder = { Text("Seleccionar fecha", color = TextTertiary) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    onDatePickerClick { date ->
                                        registerData = registerData.copy(dob = date)
                                    }
                                }) {
                                    Icon(Icons.Default.DateRange, "Seleccionar fecha", tint = TextSecondary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDatePickerClick { date ->
                                        registerData = registerData.copy(dob = date)
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Border,
                                cursorColor = Primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Sexo
                        Text(
                            text = "Sexo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Femenino" to 0, "Masculino" to 1).forEach { (label, value) ->
                                val isSelected = registerData.sex == value
                                OutlinedButton(
                                    onClick = { registerData = registerData.copy(sex = value) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, 
                                        if (isSelected) Primary else Border
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else Color.Transparent
                                    )
                                ) {
                                    Text(
                                        label,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                        color = if (isSelected) Primary else TextPrimary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Altura y Peso
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Altura (m)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = registerData.height,
                                    onValueChange = { registerData = registerData.copy(height = it) },
                                    placeholder = { Text("1.70", color = TextTertiary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = Border,
                                        cursorColor = Primary
                                    )
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Peso (kg)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = registerData.weight,
                                    onValueChange = { registerData = registerData.copy(weight = it) },
                                    placeholder = { Text("70", color = TextTertiary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = Border,
                                        cursorColor = Primary
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Contacto de emergencia
                        Text(
                            text = "Contacto de Emergencia",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = registerData.emergencyContact,
                            onValueChange = { registerData = registerData.copy(emergencyContact = it) },
                            placeholder = { Text("+56 9 1234 5678", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Border,
                                cursorColor = Primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Meta de salud
                        Text(
                            text = "Meta de Salud",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = registerData.goal,
                            onValueChange = { registerData = registerData.copy(goal = it) },
                            placeholder = { Text("Ej: Perder peso, mejorar sueño...", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Border,
                                cursorColor = Primary
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Card de Seguridad
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Border, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Seguridad",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Protege tu cuenta",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Contraseña
                        Text(
                            text = "Contraseña",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = registerData.password,
                            onValueChange = { registerData = registerData.copy(password = it) },
                            placeholder = { Text("Mínimo 6 caracteres", color = TextTertiary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Border,
                                cursorColor = Primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Confirmar contraseña
                        Text(
                            text = "Confirmar Contraseña",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = registerData.confirmPassword,
                            onValueChange = { registerData = registerData.copy(confirmPassword = it) },
                            placeholder = { Text("Repite tu contraseña", color = TextTertiary) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = registerData.confirmPassword.isNotEmpty() && 
                                     registerData.password != registerData.confirmPassword,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Border,
                                errorBorderColor = Error,
                                cursorColor = Primary
                            )
                        )
                        
                        if (registerData.confirmPassword.isNotEmpty() && 
                            registerData.password != registerData.confirmPassword) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Las contraseñas no coinciden",
                                color = Error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Error.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Botón Registrar
                Button(
                    onClick = { onRegisterClick(registerData) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    ),
                    enabled = !isLoading && 
                             registerData.name.isNotBlank() &&
                             registerData.email.isNotBlank() &&
                             registerData.password.length >= 6 &&
                             registerData.password == registerData.confirmPassword
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Crear Cuenta", 
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    HealthTrackTheme {
        RegisterScreen(
            onRegisterClick = { },
            onBackClick = { },
            onDatePickerClick = { }
        )
    }
}
