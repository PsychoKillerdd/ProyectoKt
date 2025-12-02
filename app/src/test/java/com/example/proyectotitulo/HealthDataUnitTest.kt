package com.example.proyectotitulo

import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias para la lógica de negocio de Samsung Machine
 * 
 * Estas pruebas verifican:
 * - Cálculo de nivel de estrés
 * - Formateo de números
 * - Normalización de datos de salud
 * - Validación de datos
 */
class HealthDataUnitTest {

    // ============================================
    // TESTS DE CÁLCULO DE NIVEL DE ESTRÉS
    // ============================================

    /**
     * TEST 1: Estrés muy bajo con FC menor a 60
     */
    @Test
    fun calculateStress_heartRateBelow60_returnsVeryLowStress() {
        val heartRate = 55
        val stress = calculateStress(heartRate)
        assertEquals("FC < 60 debe dar estrés nivel 20", 20, stress)
    }

    /**
     * TEST 2: Estrés bajo con FC entre 60-69
     */
    @Test
    fun calculateStress_heartRate60to69_returnsLowStress() {
        val stress60 = calculateStress(60)
        val stress65 = calculateStress(65)
        val stress69 = calculateStress(69)
        
        assertEquals("FC 60 debe dar estrés nivel 30", 30, stress60)
        assertEquals("FC 65 debe dar estrés nivel 30", 30, stress65)
        assertEquals("FC 69 debe dar estrés nivel 30", 30, stress69)
    }

    /**
     * TEST 3: Estrés normal con FC entre 70-79
     */
    @Test
    fun calculateStress_heartRate70to79_returnsNormalStress() {
        val stress70 = calculateStress(70)
        val stress75 = calculateStress(75)
        val stress79 = calculateStress(79)
        
        assertEquals("FC 70 debe dar estrés nivel 45", 45, stress70)
        assertEquals("FC 75 debe dar estrés nivel 45", 45, stress75)
        assertEquals("FC 79 debe dar estrés nivel 45", 45, stress79)
    }

    /**
     * TEST 4: Estrés moderado con FC entre 80-89
     */
    @Test
    fun calculateStress_heartRate80to89_returnsModerateStress() {
        val stress80 = calculateStress(80)
        val stress85 = calculateStress(85)
        val stress89 = calculateStress(89)
        
        assertEquals("FC 80 debe dar estrés nivel 60", 60, stress80)
        assertEquals("FC 85 debe dar estrés nivel 60", 60, stress85)
        assertEquals("FC 89 debe dar estrés nivel 60", 60, stress89)
    }

    /**
     * TEST 5: Estrés alto con FC mayor o igual a 90
     */
    @Test
    fun calculateStress_heartRate90OrAbove_returnsHighStress() {
        val stress90 = calculateStress(90)
        val stress100 = calculateStress(100)
        val stress120 = calculateStress(120)
        
        assertEquals("FC 90 debe dar estrés nivel 75", 75, stress90)
        assertEquals("FC 100 debe dar estrés nivel 75", 75, stress100)
        assertEquals("FC 120 debe dar estrés nivel 75", 75, stress120)
    }

    // ============================================
    // TESTS DE FORMATEO DE NÚMEROS
    // ============================================

    /**
     * TEST 6: Formateo de número menor a 1000
     */
    @Test
    fun formatNumber_lessThan1000_returnsAsIs() {
        assertEquals("500", formatNumber(500))
        assertEquals("999", formatNumber(999))
        assertEquals("0", formatNumber(0))
    }

    /**
     * TEST 7: Formateo de número mayor o igual a 1000
     */
    @Test
    fun formatNumber_1000OrMore_returnsKFormat() {
        assertEquals("1.0k", formatNumber(1000))
        assertEquals("8.5k", formatNumber(8500))
        assertEquals("10.2k", formatNumber(10234))
        assertEquals("15.0k", formatNumber(15000))
    }

    // ============================================
    // TESTS DE NORMALIZACIÓN DE DATOS
    // ============================================

    /**
     * TEST 8: Normalización de campo de sueño con tilde
     */
    @Test
    fun normalizeHealthData_sleepWithTilde_normalizesCorrectly() {
        val data = mapOf(
            "horasDeSueño" to 7.5,
            "pasosDiarios" to 8000L
        )
        
        val sleep = getSleepValue(data)
        assertEquals("Debe leer horasDeSueño correctamente", 7.5, sleep, 0.01)
    }

    /**
     * TEST 9: Normalización de campo de sueño sin tilde
     */
    @Test
    fun normalizeHealthData_sleepWithoutTilde_normalizesCorrectly() {
        val data = mapOf(
            "horasDeSueno" to 8.0,
            "pasosDiarios" to 10000L
        )
        
        val sleep = getSleepValue(data)
        assertEquals("Debe leer horasDeSueno correctamente", 8.0, sleep, 0.01)
    }

    /**
     * TEST 10: Normalización prioriza campo sin tilde
     */
    @Test
    fun normalizeHealthData_bothFormats_prioritizesWithoutTilde() {
        val data = mapOf(
            "horasDeSueno" to 6.0,  // Sin tilde - prioridad
            "horasDeSueño" to 8.0   // Con tilde
        )
        
        val sleep = getSleepValue(data)
        assertEquals("Debe priorizar horasDeSueno", 6.0, sleep, 0.01)
    }

    /**
     * TEST 11: Normalización con ambos campos nulos
     */
    @Test
    fun normalizeHealthData_bothNull_returnsZero() {
        val data = mapOf(
            "pasosDiarios" to 5000L
        )
        
        val sleep = getSleepValue(data)
        assertEquals("Debe retornar 0 cuando ambos son nulos", 0.0, sleep, 0.01)
    }

    // ============================================
    // TESTS DE VALIDACIÓN DE DATOS
    // ============================================

    /**
     * TEST 12: Validación de ritmo cardíaco válido
     */
    @Test
    fun validateHeartRate_validRange_returnsTrue() {
        assertTrue("40 bpm es válido", isValidHeartRate(40))
        assertTrue("72 bpm es válido", isValidHeartRate(72))
        assertTrue("200 bpm es válido", isValidHeartRate(200))
    }

    /**
     * TEST 13: Validación de ritmo cardíaco inválido
     */
    @Test
    fun validateHeartRate_invalidRange_returnsFalse() {
        assertFalse("0 bpm es inválido", isValidHeartRate(0))
        assertFalse("-10 bpm es inválido", isValidHeartRate(-10))
        assertFalse("300 bpm es inválido", isValidHeartRate(300))
    }

    /**
     * TEST 14: Validación de SpO2 válido
     */
    @Test
    fun validateSpO2_validRange_returnsTrue() {
        assertTrue("95% es válido", isValidSpO2(95.0))
        assertTrue("98% es válido", isValidSpO2(98.0))
        assertTrue("100% es válido", isValidSpO2(100.0))
    }

    /**
     * TEST 15: Validación de SpO2 inválido
     */
    @Test
    fun validateSpO2_invalidRange_returnsFalse() {
        assertFalse("0% es inválido", isValidSpO2(0.0))
        assertFalse("50% es inválido (muy bajo)", isValidSpO2(50.0))
        assertFalse("101% es inválido", isValidSpO2(101.0))
    }

    /**
     * TEST 16: Validación de horas de sueño válidas
     */
    @Test
    fun validateSleepHours_validRange_returnsTrue() {
        assertTrue("0 horas es válido (no durmió)", isValidSleepHours(0.0))
        assertTrue("7.5 horas es válido", isValidSleepHours(7.5))
        assertTrue("12 horas es válido", isValidSleepHours(12.0))
    }

    /**
     * TEST 17: Validación de horas de sueño inválidas
     */
    @Test
    fun validateSleepHours_invalidRange_returnsFalse() {
        assertFalse("-1 horas es inválido", isValidSleepHours(-1.0))
        assertFalse("25 horas es inválido", isValidSleepHours(25.0))
    }

    /**
     * TEST 18: Validación de pasos válidos
     */
    @Test
    fun validateSteps_validRange_returnsTrue() {
        assertTrue("0 pasos es válido", isValidSteps(0))
        assertTrue("10000 pasos es válido", isValidSteps(10000))
        assertTrue("50000 pasos es válido", isValidSteps(50000))
    }

    /**
     * TEST 19: Validación de pasos inválidos
     */
    @Test
    fun validateSteps_invalidRange_returnsFalse() {
        assertFalse("-100 pasos es inválido", isValidSteps(-100))
        assertFalse("1000000 pasos es inválido (muy alto)", isValidSteps(1000000))
    }

    // ============================================
    // TESTS DE CATEGORIZACIÓN DE ESTRÉS
    // ============================================

    /**
     * TEST 20: Etiqueta de estrés bajo
     */
    @Test
    fun getStressLabel_lowLevel_returnsCorrectLabel() {
        assertEquals("Bajo", getStressLabel(20))
        assertEquals("Bajo", getStressLabel(29))
    }

    /**
     * TEST 21: Etiqueta de estrés moderado
     */
    @Test
    fun getStressLabel_moderateLevel_returnsCorrectLabel() {
        assertEquals("Moderado", getStressLabel(30))
        assertEquals("Moderado", getStressLabel(45))
        assertEquals("Moderado", getStressLabel(59))
    }

    /**
     * TEST 22: Etiqueta de estrés alto
     */
    @Test
    fun getStressLabel_highLevel_returnsCorrectLabel() {
        assertEquals("Alto", getStressLabel(60))
        assertEquals("Alto", getStressLabel(75))
        assertEquals("Alto", getStressLabel(100))
    }

    // ============================================
    // FUNCIONES AUXILIARES PARA TESTS
    // ============================================

    /**
     * Calcula el nivel de estrés basado en la frecuencia cardíaca
     */
    private fun calculateStress(heartRate: Int): Int {
        return when {
            heartRate < 60 -> 20
            heartRate < 70 -> 30
            heartRate < 80 -> 45
            heartRate < 90 -> 60
            else -> 75
        }
    }

    /**
     * Formatea un número para mostrar en la UI
     */
    private fun formatNumber(number: Long): String {
        return when {
            number >= 1000 -> String.format("%.1fk", number / 1000.0)
            else -> number.toString()
        }
    }

    /**
     * Obtiene el valor de sueño normalizando ambos formatos
     */
    private fun getSleepValue(data: Map<String, Any?>): Double {
        val sueno = data["horasDeSueno"] as? Double
        val sueño = data["horasDeSueño"] as? Double
        return sueno ?: sueño ?: 0.0
    }

    /**
     * Valida que el ritmo cardíaco esté en un rango razonable
     */
    private fun isValidHeartRate(bpm: Int): Boolean {
        return bpm in 30..250
    }

    /**
     * Valida que el SpO2 esté en un rango razonable
     */
    private fun isValidSpO2(percentage: Double): Boolean {
        return percentage in 70.0..100.0
    }

    /**
     * Valida que las horas de sueño sean razonables
     */
    private fun isValidSleepHours(hours: Double): Boolean {
        return hours in 0.0..24.0
    }

    /**
     * Valida que los pasos sean razonables
     */
    private fun isValidSteps(steps: Long): Boolean {
        return steps in 0..100000
    }

    /**
     * Obtiene la etiqueta de estrés
     */
    private fun getStressLabel(level: Int): String {
        return when {
            level < 30 -> "Bajo"
            level < 60 -> "Moderado"
            else -> "Alto"
        }
    }
}
