package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor(
    private val supabase: SupabaseProvider
) {
    companion object {
        const val BUCKET_AVATARS = "avatars"
        const val BUCKET_PETS = "pets"
        const val BUCKET_SIGHTINGS = "sightings"
    }

    /**
     * Upload a file to a bucket and return the public URL.
     *
     * @param bucket Bucket name (avatars, pets, sightings)
     * @param path File path within the bucket (e.g., "{userId}/profile.jpg")
     * @param data File bytes
     * @param contentType MIME type (e.g., "image/jpeg")
     * @param upsert Whether to overwrite existing file
     * @return Public URL of the uploaded file
     */
    suspend fun upload(
        bucket: String,
        path: String,
        data: ByteArray,
        contentType: String = "image/jpeg",
        upsert: Boolean = true
    ): String {
        val storage = supabase.client.storage[bucket]
        storage.upload(path, data) {
            this.upsert = upsert
            this.contentType = ContentType.parse(contentType)
        }
        return storage.publicUrl(path)
    }

    /**
     * Delete a file from a bucket.
     */
    suspend fun delete(bucket: String, path: String) {
        supabase.client.storage[bucket].delete(path)
    }

    /**
     * Get public URL for a file.
     */
    fun getPublicUrl(bucket: String, path: String): String =
        supabase.client.storage[bucket].publicUrl(path)

    // ----- Convenience methods -----

    suspend fun uploadAvatar(
        userId: String,
        data: ByteArray,
        fileName: String = "profile.jpg"
    ): String = upload(
        bucket = BUCKET_AVATARS,
        path = "$userId/$fileName",
        data = data
    )

    suspend fun uploadPetPhoto(
        petId: String,
        data: ByteArray,
        fileName: String
    ): String = upload(
        bucket = BUCKET_PETS,
        path = "$petId/$fileName",
        data = data
    )

    suspend fun uploadSightingPhoto(
        sightingId: String,
        data: ByteArray,
        fileName: String
    ): String = upload(
        bucket = BUCKET_SIGHTINGS,
        path = "$sightingId/$fileName",
        data = data
    )
}