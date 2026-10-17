package com.fabriziogo.epona.core.domain.usecase.adoption

import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.domain.repository.AdoptionRepository
import com.fabriziogo.epona.core.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CreateAdoptionPostUseCaseTest {

    private val adoptionRepo = FakeAdoptionRepository()
    private val petRepo = FakePetRepository()
    private val useCase = CreateAdoptionPostUseCase(adoptionRepo, petRepo)

    private fun post(
        title: String = "Golden Retriever puppies in adoption",
        phone: String = "0414-111-1111",
        price: Double? = null,
        photos: List<String> = emptyList()
    ) = AdoptionPost(
        title = title,
        species = Species.DOG,
        price = price,
        photoUrls = photos,
        contactPhone = phone
    )

    private fun assertRejected(block: suspend () -> Unit) = runBlocking {
        try {
            block()
            fail("Expected the use case to reject the post")
        } catch (_: IllegalArgumentException) {
            // expected
        }
        assertEquals(0, adoptionRepo.created.size)
        assertEquals(0, petRepo.uploadCalls)
    }

    @Test
    fun `a title and a phone are enough - name age and price stay empty`() = runBlocking {
        val result = useCase(post())

        assertTrue(result.isSuccess)
        val saved = adoptionRepo.created.single()
        assertNull(saved.petName)
        assertNull(saved.ageStage)
        assertNull(saved.price)
    }

    @Test
    fun `blank or too short title is rejected before anything is uploaded`() {
        assertRejected { useCase(post(title = "   ")) }
        assertRejected { useCase(post(title = "ab")) }
    }

    @Test
    fun `title over the limit is rejected`() {
        assertRejected { useCase(post(title = "x".repeat(ADOPTION_TITLE_MAX_LENGTH + 1))) }
    }

    @Test
    fun `blank phone is rejected`() {
        assertRejected { useCase(post(phone = "  ")) }
    }

    @Test
    fun `negative price is rejected but zero and positive prices pass`() = runBlocking {
        assertRejected { useCase(post(price = -1.0)) }

        assertTrue(useCase(post(price = 0.0)).isSuccess)
        assertTrue(useCase(post(price = 49.99)).isSuccess)
        assertEquals(listOf(0.0, 49.99), adoptionRepo.created.map { it.price })
    }

    @Test
    fun `more than three photos is rejected`() {
        assertRejected { useCase(post(photos = List(4) { "file:///photo$it.jpg" })) }
    }

    @Test
    fun `trims title and phone before saving`() = runBlocking {
        useCase(post(title = "  Kittens to adopt  ", phone = " 555 "))

        val saved = adoptionRepo.created.single()
        assertEquals("Kittens to adopt", saved.title)
        assertEquals("555", saved.contactPhone)
    }

    @Test
    fun `local photos are uploaded first and replaced in place`() = runBlocking {
        petRepo.uploaded = listOf("https://cdn/a.jpg", "https://cdn/b.jpg")

        useCase(
            post(photos = listOf("file:///a.jpg", "https://cdn/existing.jpg", "file:///b.jpg"))
        )

        assertEquals(listOf("file:///a.jpg", "file:///b.jpg"), petRepo.lastUploadRequest)
        assertEquals(
            listOf("https://cdn/a.jpg", "https://cdn/existing.jpg", "https://cdn/b.jpg"),
            adoptionRepo.created.single().photoUrls
        )
    }

    @Test
    fun `a failed upload fails the publish and creates nothing`() = runBlocking {
        petRepo.uploadError = RuntimeException("offline")

        val result = useCase(post(photos = listOf("file:///a.jpg")))

        assertTrue(result.isFailure)
        assertEquals(0, adoptionRepo.created.size)
    }

    private class FakeAdoptionRepository : AdoptionRepository {
        val created = mutableListOf<AdoptionPost>()

        override suspend fun createAdoptionPost(post: AdoptionPost): Result<AdoptionPost> {
            created += post
            return Result.success(post.copy(id = "new-id"))
        }

        override suspend fun getAdoptionPosts(limit: Int, offset: Int): Result<List<AdoptionPostWithOwner>> =
            Result.success(emptyList())

        override suspend fun getAdoptionPost(postId: String): Result<AdoptionPostWithOwner> =
            Result.failure(NoSuchElementException())

        override suspend fun getMyAdoptionPosts(): Result<List<AdoptionPostWithOwner>> =
            Result.success(emptyList())

        override suspend fun markAdopted(postId: String): Result<Unit> = Result.success(Unit)
        override suspend fun deleteAdoptionPost(postId: String): Result<Unit> = Result.success(Unit)
    }

    private class FakePetRepository : PetRepository {
        var uploadCalls = 0
        var lastUploadRequest: List<String> = emptyList()
        var uploaded: List<String> = emptyList()
        var uploadError: Throwable? = null

        override suspend fun uploadPetPhotos(localUris: List<String>): Result<List<String>> {
            uploadCalls++
            lastUploadRequest = localUris
            return uploadError?.let { Result.failure(it) } ?: Result.success(uploaded)
        }

        override fun observeMyPets(): Flow<List<Pet>> = emptyFlow()
        override suspend fun getPet(petId: String): Result<Pet> = Result.failure(NoSuchElementException())
        override suspend fun registerPet(pet: Pet): Result<Pet> = Result.success(pet)
        override suspend fun updatePet(pet: Pet): Result<Pet> = Result.success(pet)
        override suspend fun deletePet(petId: String): Result<Unit> = Result.success(Unit)
    }
}
