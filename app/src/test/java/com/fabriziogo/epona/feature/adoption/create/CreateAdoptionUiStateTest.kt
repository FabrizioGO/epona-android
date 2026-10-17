package com.fabriziogo.epona.feature.adoption.create

import com.fabriziogo.epona.core.domain.model.AgeStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateAdoptionUiStateTest {

    private val valid = CreateAdoptionUiState(title = "Puppies in adoption", phone = "555")

    @Test
    fun `title and phone are all that is required to publish`() {
        assertTrue(valid.canPublish)
        assertFalse(valid.copy(title = "").canPublish)
        assertFalse(valid.copy(title = "ab").canPublish)
        assertFalse(valid.copy(phone = " ").canPublish)
    }

    @Test
    fun `blank price is valid and means free`() {
        assertTrue(valid.copy(price = "").isPriceValid)
        assertNull(valid.copy(price = "").priceValue)
    }

    @Test
    fun `price accepts dot and comma decimals`() {
        assertEquals(25.5, parsePrice("25.5")!!, 0.0)
        assertEquals(25.5, parsePrice("25,5")!!, 0.0)
        assertEquals(0.0, parsePrice("0")!!, 0.0)
    }

    @Test
    fun `unparseable price blocks publishing`() {
        val bad = valid.copy(price = "1.2.3")
        assertFalse(bad.isPriceValid)
        assertFalse(bad.canPublish)
    }

    @Test
    fun `publishing is blocked while submitting or processing photos`() {
        assertFalse(valid.copy(isSubmitting = true).canPublish)
        assertFalse(valid.copy(isProcessingPhotos = true).canPublish)
    }

    @Test
    fun `dirty once anything optional is touched`() {
        assertFalse(CreateAdoptionUiState().isDirty)
        assertTrue(CreateAdoptionUiState(ageStage = AgeStage.BABY).isDirty)
        assertTrue(CreateAdoptionUiState(price = "10").isDirty)
    }
}
