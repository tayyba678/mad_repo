package com.example.unimatch

import org.junit.Test
import org.junit.Assert.*

class ValidationTest {

    @Test
    fun name_isValid() {
        // Validating that name contains only letters and spaces
        assertTrue(ValidationUtils.isValidName("Ahmed Raza"))
        assertFalse(ValidationUtils.isValidName("Ahmed123"))
    }

    @Test
    fun regNo_isValid() {
        // Validating university format: 2023-CS-130
        assertTrue(ValidationUtils.isValidRegNo("2023-CS-130"))
        assertFalse(ValidationUtils.isValidRegNo("23-CS-130"))
        assertFalse(ValidationUtils.isValidRegNo("2023-123"))
    }

    @Test
    fun phone_isValid() {
        // Validating Pakistani phone formats
        assertTrue(ValidationUtils.isValidPhone("03001234567"))
        assertTrue(ValidationUtils.isValidPhone("+923001234567"))
        assertFalse(ValidationUtils.isValidPhone("12345"))
    }

    @Test
    fun matchingAlgorithm_isCorrect() {
        // PROVING the app correctly identifies shared interests
        val user1 = listOf("Android", "Kotlin", "Gaming")
        val user2 = listOf("Kotlin", "Python", "Gaming")
        
        val score = ValidationUtils.calculateMatchScore(user1, user2)
        
        // They share "Kotlin" and "Gaming", so score must be 2
        assertEquals(2, score)
    }
}
