package com.example.proyectotitulo

import com.example.proyectotitulo.api.Detalles
import com.example.proyectotitulo.api.HealthResponse
import com.example.proyectotitulo.api.UserRequest
import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias para los modelos de la API
 * 
 * Estas pruebas verifican:
 * - Estructura de UserRequest
 * - Estructura de HealthResponse
 * - Parseo de detalles de IA
 */
class ApiModelsTest {

    // ============================================
    // TESTS DE UserRequest
    // ============================================

    /**
     * TEST 1: Crear UserRequest con ID válido
     */
    @Test
    fun userRequest_creation_setsUserId() {
        val userId = "FbBbEZcdhXRRFg6d5Qnf8uUOcKd2"
        val request = UserRequest(user_id = userId)
        
        assertEquals("El user_id debe coincidir", userId, request.user_id)
    }

    /**
     * TEST 2: UserRequest no acepta ID vacío
     */
    @Test
    fun userRequest_emptyId_isStillValid() {
        val request = UserRequest(user_id = "")
        
        assertEquals("El user_id puede estar vacío", "", request.user_id)
    }

    // ============================================
    // TESTS DE HealthResponse
    // ============================================

    /**
     * TEST 3: Crear HealthResponse completo
     */
    @Test
    fun healthResponse_creation_setsAllFields() {
        val detalles = Detalles(
            sueño = "Dormiste 7.5 horas, excelente descanso.",
            ritmo_cardiaco = "Tu ritmo cardíaco promedio fue 72 bpm, normal.",
            estres = "Nivel de estrés bajo (35/100).",
            pasos = "Caminaste 8,542 pasos hoy, ¡buen trabajo!"
        )
        
        val response = HealthResponse(
            estado_general = "Bueno",
            detalles = detalles,
            mensaje = "Tu salud se ve muy bien hoy. Sigue así."
        )
        
        assertEquals("Estado general debe ser 'Bueno'", "Bueno", response.estado_general)
        assertEquals("Mensaje debe coincidir", "Tu salud se ve muy bien hoy. Sigue así.", response.mensaje)
        assertNotNull("Detalles no debe ser nulo", response.detalles)
    }

    /**
     * TEST 4: Verificar detalles de sueño
     */
    @Test
    fun healthResponse_detalles_containsSleepInfo() {
        val detalles = Detalles(
            sueño = "Dormiste 8 horas",
            ritmo_cardiaco = "",
            estres = "",
            pasos = ""
        )
        
        assertTrue("Detalles de sueño debe contener 'Dormiste'", 
            detalles.sueño.contains("Dormiste"))
    }

    /**
     * TEST 5: Verificar detalles de ritmo cardíaco
     */
    @Test
    fun healthResponse_detalles_containsHeartRateInfo() {
        val detalles = Detalles(
            sueño = "",
            ritmo_cardiaco = "72 bpm promedio",
            estres = "",
            pasos = ""
        )
        
        assertTrue("Detalles de ritmo cardíaco debe contener 'bpm'", 
            detalles.ritmo_cardiaco.contains("bpm"))
    }

    /**
     * TEST 6: Verificar detalles de estrés
     */
    @Test
    fun healthResponse_detalles_containsStressInfo() {
        val detalles = Detalles(
            sueño = "",
            ritmo_cardiaco = "",
            estres = "Nivel bajo 35/100",
            pasos = ""
        )
        
        assertTrue("Detalles de estrés debe contener nivel", 
            detalles.estres.contains("35") || detalles.estres.contains("bajo"))
    }

    /**
     * TEST 7: Verificar detalles de pasos
     */
    @Test
    fun healthResponse_detalles_containsStepsInfo() {
        val detalles = Detalles(
            sueño = "",
            ritmo_cardiaco = "",
            estres = "",
            pasos = "10,000 pasos"
        )
        
        assertTrue("Detalles de pasos debe contener 'pasos'", 
            detalles.pasos.contains("pasos"))
    }

    // ============================================
    // TESTS DE ESTADOS GENERALES
    // ============================================

    /**
     * TEST 8: Estado general "Bueno"
     */
    @Test
    fun healthResponse_estadoBueno_isValid() {
        val response = createTestResponse("Bueno")
        
        assertEquals("Bueno", response.estado_general)
        assertTrue(isValidEstadoGeneral(response.estado_general))
    }

    /**
     * TEST 9: Estado general "Regular"
     */
    @Test
    fun healthResponse_estadoRegular_isValid() {
        val response = createTestResponse("Regular")
        
        assertEquals("Regular", response.estado_general)
        assertTrue(isValidEstadoGeneral(response.estado_general))
    }

    /**
     * TEST 10: Estado general "Malo"
     */
    @Test
    fun healthResponse_estadoMalo_isValid() {
        val response = createTestResponse("Malo")
        
        assertEquals("Malo", response.estado_general)
        assertTrue(isValidEstadoGeneral(response.estado_general))
    }

    /**
     * TEST 11: Estado general "Excelente"
     */
    @Test
    fun healthResponse_estadoExcelente_isValid() {
        val response = createTestResponse("Excelente")
        
        assertEquals("Excelente", response.estado_general)
        assertTrue(isValidEstadoGeneral(response.estado_general))
    }

    // ============================================
    // TESTS DE MENSAJES
    // ============================================

    /**
     * TEST 12: Mensaje no vacío
     */
    @Test
    fun healthResponse_mensaje_notEmpty() {
        val response = createTestResponse("Bueno", "Tu salud está bien")
        
        assertTrue("Mensaje no debe estar vacío", response.mensaje.isNotEmpty())
    }

    /**
     * TEST 13: Mensaje puede contener emojis
     */
    @Test
    fun healthResponse_mensaje_canContainEmojis() {
        val mensaje = "✅ Tu salud está bien 💪"
        val response = createTestResponse("Bueno", mensaje)
        
        assertTrue("Mensaje puede contener emojis", response.mensaje.contains("✅"))
        assertTrue("Mensaje puede contener emojis", response.mensaje.contains("💪"))
    }

    // ============================================
    // FUNCIONES AUXILIARES
    // ============================================

    private fun createTestResponse(estado: String, mensaje: String = "Test"): HealthResponse {
        return HealthResponse(
            estado_general = estado,
            detalles = Detalles("", "", "", ""),
            mensaje = mensaje
        )
    }

    private fun isValidEstadoGeneral(estado: String): Boolean {
        return estado in listOf("Excelente", "Bueno", "Regular", "Malo")
    }
}
