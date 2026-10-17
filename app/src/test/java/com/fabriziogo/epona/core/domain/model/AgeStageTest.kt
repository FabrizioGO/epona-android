package com.fabriziogo.epona.core.domain.model

import com.fabriziogo.epona.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AgeStageTest {

    @Test
    fun `baby reads as puppy for dogs and kitten for cats`() {
        assertEquals(R.string.age_stage_puppy, AgeStage.BABY.label(Species.DOG))
        assertEquals(R.string.age_stage_kitten, AgeStage.BABY.label(Species.CAT))
        assertEquals(R.string.age_stage_baby, AgeStage.BABY.label(Species.OTHER))
    }

    @Test
    fun `other stages read the same for every species`() {
        Species.entries.forEach {
            assertEquals(R.string.age_stage_adult, AgeStage.ADULT.label(it))
        }
    }

    @Test
    fun `unknown value is null rather than a default`() {
        assertNull(AgeStage.fromValue("ancient"))
        assertNull(AgeStage.fromValue(null))
        assertEquals(AgeStage.SENIOR, AgeStage.fromValue("senior"))
    }
}
