package com.fabriziogo.epona.core.domain.usecase.alert

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The score behind the wizard's match step: exact species is the gate (applied
 * by the use case, not here), breed and color only order what is already the
 * right animal.
 */
class PetMatchScoreTest {

    @Test
    fun `exact breed and color beats breed alone`() {
        val full = petMatchScore("Labrador", "Golden", "Labrador", "Golden")
        val breedOnly = petMatchScore("Labrador", "Golden", "Labrador", "Black")

        assertTrue(full > breedOnly)
        assertTrue(breedOnly > 0)
    }

    @Test
    fun `a blank wanted breed or color tolerates anything`() {
        assertEquals(0, petMatchScore(null, null, "Labrador", "Golden"))
        assertEquals(0, petMatchScore("", "  ", "Labrador", "Golden"))
        assertEquals(0, petMatchScore("Labrador", "Golden", null, null))
    }

    @Test
    fun `matching is case-insensitive in either direction`() {
        assertTrue(petMatchScore("lab", null, "Labrador Mix", null) > 0)
        assertTrue(petMatchScore("Labrador Mix", null, "lab", null) > 0)
        assertTrue(petMatchScore("GOLDEN", null, "golden retriever", null) > 0)
    }

    @Test
    fun `unrelated breed and color score zero`() {
        assertEquals(0, petMatchScore("Poodle", "White", "Labrador", "Golden"))
    }

    @Test
    fun `color alone still scores`() {
        assertEquals(1, petMatchScore("Poodle", "Golden", "Labrador", "Golden"))
    }
}
