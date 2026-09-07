package com.fabriziogo.epona.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The substitution these cover is the difference between "edit a pet and its photos
 * keep the order you left them in" and "every new photo jumps to the end".
 */
class PhotoLimitsTest {

    @Test
    fun `local uri is anything that is not http`() {
        assertTrue("file:///data/cache/a.jpg".isLocalPhotoUri())
        assertTrue("content://media/external/images/1".isLocalPhotoUri())
        assertTrue("https://x.supabase.co/storage/a.jpg".isRemotePhotoUrl())
        assertFalse("https://x.supabase.co/storage/a.jpg".isLocalPhotoUri())
        assertFalse("HTTP://x.supabase.co/a.jpg".isLocalPhotoUri())
        assertFalse("".isLocalPhotoUri())
    }

    @Test
    fun `uploaded urls replace local uris in place`() {
        val before = listOf(
            "https://cdn/old1.jpg",
            "file:///cache/new1.jpg",
            "https://cdn/old2.jpg",
            "file:///cache/new2.jpg"
        )

        val after = before.withUploadedPhotos(
            listOf("https://cdn/up1.jpg", "https://cdn/up2.jpg")
        )

        assertEquals(
            listOf(
                "https://cdn/old1.jpg",
                "https://cdn/up1.jpg",
                "https://cdn/old2.jpg",
                "https://cdn/up2.jpg"
            ),
            after
        )
    }

    @Test
    fun `a list with nothing local is returned unchanged`() {
        val remote = listOf("https://cdn/a.jpg", "https://cdn/b.jpg")
        assertEquals(remote, remote.withUploadedPhotos(emptyList()))
    }

    @Test
    fun `a newly added photo keeps the position it was given`() {
        // The photo the user inserted first stays first after the save.
        val before = listOf("file:///cache/new.jpg", "https://cdn/old.jpg")
        assertEquals(
            listOf("https://cdn/uploaded.jpg", "https://cdn/old.jpg"),
            before.withUploadedPhotos(listOf("https://cdn/uploaded.jpg"))
        )
    }

    @Test
    fun `the cap is the one the pickers enforce`() {
        assertEquals(3, MAX_PHOTOS_PER_ENTITY)
    }
}
