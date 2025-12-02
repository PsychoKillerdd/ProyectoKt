package com.example.proyectotitulo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.proyectotitulo.ui.screens.LoginScreen
import com.example.proyectotitulo.ui.theme.HealthTrackTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de UI para la pantalla de Login
 * 
 * Estas pruebas verifican:
 * - Renderizado correcto de componentes
 * - Interacción con campos de texto
 * - Validación de formulario
 * - Navegación a registro
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * TEST 1: Verificar que todos los elementos de la UI se renderizan correctamente
     */
    @Test
    fun loginScreen_displaysAllUIElements() {
        composeTestRule.setContent {
            HealthTrackTheme {
                LoginScreen(
                    onLoginClick = { _, _ -> },
                    onRegisterClick = { },
                    onForgotPasswordClick = { }
                )
            }
        }

        // Verificar título de la app
        composeTestRule.onNodeWithText("Samsung Machine").assertIsDisplayed()
        
        // Verificar subtítulo
        composeTestRule.onNodeWithText("Tu salud, en tus manos").assertIsDisplayed()
        
        // Verificar labels de campos
        composeTestRule.onNodeWithText("Correo Electrónico").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
        
        // Verificar placeholders
        composeTestRule.onNodeWithText("email@ejemplo.com").assertIsDisplayed()
        
        // Verificar botón de login
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
        
        // Verificar link de registro
        composeTestRule.onNodeWithText("¿No tienes una cuenta?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Regístrate").assertIsDisplayed()
        
        // Verificar link de olvidaste contraseña
        composeTestRule.onNodeWithText("¿Olvidaste tu contraseña?").assertIsDisplayed()
    }

    /**
     * TEST 2: Verificar que se puede ingresar texto en el campo de email
     */
    @Test
    fun loginScreen_emailFieldAcceptsInput() {
        composeTestRule.setContent {
            HealthTrackTheme {
                LoginScreen(
                    onLoginClick = { _, _ -> },
                    onRegisterClick = { },
                    onForgotPasswordClick = { }
                )
            }
        }

        // Encontrar el campo de email y escribir
        composeTestRule.onNodeWithText("email@ejemplo.com")
            .performTextInput("test@gmail.com")
        
        // Verificar que el texto se ingresó
        composeTestRule.onNodeWithText("test@gmail.com").assertIsDisplayed()
    }

    /**
     * TEST 3: Verificar que se puede ingresar texto en el campo de contraseña
     */
    @Test
    fun loginScreen_passwordFieldAcceptsInput() {
        composeTestRule.setContent {
            HealthTrackTheme {
                LoginScreen(
                    onLoginClick = { _, _ -> },
                    onRegisterClick = { },
                    onForgotPasswordClick = { }
                )
            }
        }

        // Encontrar el campo de contraseña por su placeholder
        composeTestRule.onNodeWithText("••••••••")
            .performTextInput("password123")
        
        // El texto estará oculto, pero el campo debe existir
        composeTestRule.onNodeWithText("••••••••").assertDoesNotExist()
    }

    /**
     * TEST 4: Verificar que el botón de login está deshabilitado con campos vacíos
     */
    @Test
    fun loginScreen_loginButtonDisabledWhenFieldsEmpty() {
        composeTestRule.setContent {
            HealthTrackTheme {
                LoginScreen(
                    onLoginClick = { _, _ -> },
                    onRegisterClick = { },
                    onForgotPasswordClick = { }
                )
            }
        }

        // El botón debe estar deshabilitado inicialmente
        composeTestRule.onNodeWithText("Iniciar Sesión")
            .assertIsNotEnabled()
    }

    /**
     * TEST 5: Verificar que el botón de login se habilita con campos llenos
     */
    @Test
    fun loginScreen_loginButtonEnabledWhenFieldsFilled() {
        composeTestRule.setContent {
            HealthTrackTheme {
                LoginScreen(
                    onLoginClick = { _, _ -> },
                    onRegisterClick = { },
                    onForgotPasswordClick = { }
                )
            }
        }

        // Llenar email
        composeTestRule.onNodeWithText("email@ejemplo.com")
            .performTextInput("test@gmail.com")
        
        // Llenar contraseña
        composeTestRule.onNodeWithText("••••••••")
            .performTextInput("password123")
        
        // El botón debe estar habilitado
        composeTestRule.onNodeWithText("Iniciar Sesión")
            .assertIsEnabled()
    }

    /**
     * TEST 6: Verificar callback de login cuando se hace click
     */
    @Test
    fun loginScreen_loginButtonTriggersCallback() {
        var loginClicked = false
        var capturedEmail = ""
        var capturedPassword = ""

        composeTestRule.setContent {
            HealthTrackTheme {
                LoginScreen(
                    onLoginClick = { email, password ->
                        loginClicked = true
                        capturedEmail = email
                        capturedPassword = password
                    },
                    onRegisterClick = { },
                    onForgotPasswordClick = { }
                )
            }
        }

        // Llenar campos
        composeTestRule.onNodeWithText("email@ejemplo.com")
            .performTextInput("usuario@test.com")
        
        composeTestRule.onNodeWithText("••••••••")
            .performTextInput("miPassword")
        
        // Click en login
        composeTestRule.onNodeWithText("Iniciar Sesión")
            .performClick()
        
        // Verificar callback
        assert(loginClicked) { "El callback de login no fue llamado" }
        assert(capturedEmail == "usuario@test.com") { "Email incorrecto: $capturedEmail" }
        assert(capturedPassword == "miPassword") { "Password incorrecto: $capturedPassword" }
    }

    /**
     * TEST 7: Verificar callback de registro cuando se hace click
     */
    @Test
    fun loginScreen_registerLinkTriggersCallback() {
        var registerClicked = false

        composeTestRule.setContent {
            HealthTrackTheme {
                LoginScreen(
                    onLoginClick = { _, _ -> },
                    onRegisterClick = { registerClicked = true },
                    onForgotPasswordClick = { }
                )
            }
        }

        // Click en "Regístrate"
        composeTestRule.onNodeWithText("Regístrate")
            .performClick()
        
        // Verificar callback
        assert(registerClicked) { "El callback de registro no fue llamado" }
    }

    /**
     * TEST 8: Verificar que el mensaje de error se muestra
     */
    @Test
    fun loginScreen_displaysErrorMessage() {
        val errorMsg = "Credenciales inválidas"

        composeTestRule.setContent {
            HealthTrackTheme {
                LoginScreen(
                    onLoginClick = { _, _ -> },
                    onRegisterClick = { },
                    onForgotPasswordClick = { },
                    errorMessage = errorMsg
                )
            }
        }

        // Verificar que el error se muestra
        composeTestRule.onNodeWithText(errorMsg).assertIsDisplayed()
    }

    /**
     * TEST 9: Verificar indicador de carga
     */
    @Test
    fun loginScreen_showsLoadingIndicator() {
        composeTestRule.setContent {
            HealthTrackTheme {
                LoginScreen(
                    onLoginClick = { _, _ -> },
                    onRegisterClick = { },
                    onForgotPasswordClick = { },
                    isLoading = true
                )
            }
        }

        // En estado de carga, el botón debe estar deshabilitado
        composeTestRule.onNodeWithText("Iniciar Sesión")
            .assertIsNotEnabled()
    }
}
