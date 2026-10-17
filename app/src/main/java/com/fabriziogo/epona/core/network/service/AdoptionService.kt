package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import com.fabriziogo.epona.core.network.dto.AdoptionPostDto
import com.fabriziogo.epona.core.network.dto.AdoptionPostInsertDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads go through the `adoption_posts_with_owner` view (owner name/avatar joined in,
 * visibility rule applied there); writes go to the `adoption_posts` table, where RLS
 * restricts them to the owner.
 */
@Singleton
class AdoptionService @Inject constructor(
    private val supabase: SupabaseProvider
) {
    private val table get() = supabase.client.postgrest["adoption_posts"]
    private val view get() = supabase.client.postgrest["adoption_posts_with_owner"]

    suspend fun getActivePosts(limit: Int, offset: Int): List<AdoptionPostDto> =
        view.select {
            filter { eq("status", "active") }
            order("created_at", Order.DESCENDING)
            range(offset.toLong(), (offset + limit - 1).toLong())
        }.decodeList()

    suspend fun getPost(postId: String): AdoptionPostDto =
        view.select {
            filter { eq("id", postId) }
        }.decodeSingle()

    suspend fun getMyPosts(userId: String): List<AdoptionPostDto> =
        view.select {
            filter { eq("user_id", userId) }
            order("created_at", Order.DESCENDING)
        }.decodeList()

    /** Inserts into the table, then re-reads through the view so the owner fields are present. */
    suspend fun createPost(params: AdoptionPostInsertDto): AdoptionPostDto {
        val inserted = table.insert(params) { select() }.decodeSingle<IdOnly>()
        return getPost(inserted.id)
    }

    suspend fun markAdopted(postId: String) {
        table.update(mapOf("status" to "adopted")) {
            filter { eq("id", postId) }
        }
    }

    suspend fun deletePost(postId: String) {
        table.delete {
            filter { eq("id", postId) }
        }
    }

    @kotlinx.serialization.Serializable
    private data class IdOnly(val id: String)
}
