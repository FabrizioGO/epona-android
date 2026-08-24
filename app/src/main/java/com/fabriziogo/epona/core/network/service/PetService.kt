package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import com.fabriziogo.epona.core.network.dto.PetDto
import com.fabriziogo.epona.core.network.dto.PetInsertDto
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetService @Inject constructor(
    private val supabase: SupabaseProvider
) {
    private val table get() = supabase.client.postgrest["pets"]

    suspend fun getMyPets(ownerId: String): List<PetDto> =
        table.select {
            filter { eq("owner_id", ownerId) }
            order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
        }.decodeList()

    suspend fun getPet(petId: String): PetDto =
        table.select {
            filter { eq("id", petId) }
        }.decodeSingle()

    suspend fun insertPet(pet: PetInsertDto): PetDto =
        table.insert(pet) {
            select()
        }.decodeSingle()

    suspend fun updatePet(petId: String, pet: PetDto): PetDto =
        table.update(pet) {
            filter { eq("id", petId) }
            select()
        }.decodeSingle()

    suspend fun deletePet(petId: String) {
        table.delete {
            filter { eq("id", petId) }
        }
    }

    suspend fun updatePhotoUrls(petId: String, photoUrls: List<String>) {
        table.update(
            buildMap<String, Any> {
                put("photo_urls", photoUrls)
            }
        ) {
            filter { eq("id", petId) }
        }
    }
}