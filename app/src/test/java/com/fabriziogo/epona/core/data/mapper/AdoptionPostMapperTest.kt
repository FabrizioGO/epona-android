package com.fabriziogo.epona.core.data.mapper

import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.AdoptionStatus
import com.fabriziogo.epona.core.domain.model.AgeStage
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.network.dto.AdoptionPostDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdoptionPostMapperTest {

    private fun dto(
        ageStage: String? = null,
        size: String? = null,
        petName: String? = null,
        price: Double? = null
    ) = AdoptionPostDto(
        id = "1",
        userId = "u1",
        title = "Puppies",
        species = "dog",
        ageStage = ageStage,
        size = size,
        petName = petName,
        price = price,
        contactPhone = "555"
    )

    @Test
    fun `unknown or missing age stage and size stay null instead of defaulting`() {
        val post = dto(ageStage = "ancient", size = null).toDomain().post

        assertNull(post.ageStage)
        assertNull(post.size)
        assertNull(post.gender)
    }

    @Test
    fun `known values map to their enums`() {
        val post = dto(ageStage = "baby", size = "large").toDomain().post

        assertEquals(AgeStage.BABY, post.ageStage)
        assertEquals(PetSize.LARGE, post.size)
        assertEquals(Species.DOG, post.species)
    }

    @Test
    fun `blank name becomes null and null price stays free`() {
        val post = dto(petName = "   ", price = null).toDomain().post

        assertNull(post.petName)
        assertNull(post.price)
    }

    @Test
    fun `missing owner name becomes empty and unknown status reads as active`() {
        val item = dto().copy(status = "weird", ownerName = null).toDomain()

        assertEquals("", item.ownerName)
        assertEquals(AdoptionStatus.ACTIVE, item.post.status)
    }

    @Test
    fun `insert dto trims text and carries optional fields as null`() {
        val insert = AdoptionPost(
            title = "  Kittens  ",
            species = Species.CAT,
            petName = " ",
            contactPhone = " 555 "
        ).toInsertDto(userId = "u1")

        assertEquals("Kittens", insert.title)
        assertEquals("555", insert.contactPhone)
        assertEquals("cat", insert.species)
        assertNull(insert.petName)
        assertNull(insert.ageStage)
        assertNull(insert.price)
        assertEquals("u1", insert.userId)
    }

    @Test
    fun `entity round trip keeps every field`() {
        val original = dto(ageStage = "senior", size = "small", petName = "Rex", price = 25.5)
            .copy(photoUrls = listOf("https://cdn/a.jpg"), ownerName = "Alex")

        val back = original.toEntity().toDomain()

        assertEquals(original.toDomain(), back)
    }
}
