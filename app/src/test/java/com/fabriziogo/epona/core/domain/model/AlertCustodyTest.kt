package com.fabriziogo.epona.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the rule that decides whether a stranger may post about an animal that is
 * sitting in somebody's flat. create_sighting enforces the same thing server-side.
 */
class AlertCustodyTest {

    private fun alert(type: AlertType, custody: FoundCustody?) = Alert(
        type = type,
        foundCustody = custody,
        lastSeenLocation = Location(40.4, -3.7)
    )

    @Test
    fun `a found pet the finder took with them accepts no sightings`() {
        assertFalse(alert(AlertType.FOUND, FoundCustody.WITH_FINDER).acceptsSightings)
    }

    @Test
    fun `a found pet left where it was seen accepts sightings`() {
        assertTrue(alert(AlertType.FOUND, FoundCustody.AT_LOCATION).acceptsSightings)
    }

    @Test
    fun `a lost alert has no custody and accepts sightings`() {
        val lost = alert(AlertType.LOST, custody = null)
        assertNull(lost.foundCustody)
        assertTrue(lost.acceptsSightings)
    }

    @Test
    fun `an alert defaults to no custody`() {
        assertNull(Alert(type = AlertType.LOST, lastSeenLocation = Location.EMPTY).foundCustody)
    }

    @Test
    fun `fromValue maps the two stored values`() {
        assertEquals(FoundCustody.WITH_FINDER, FoundCustody.fromValue("with_finder"))
        assertEquals(FoundCustody.AT_LOCATION, FoundCustody.fromValue("at_location"))
    }

    @Test
    fun `fromValue returns null rather than defaulting`() {
        // Defaulting an unreadable value to AT_LOCATION would re-open sightings on a
        // pet that is not out there, which is the one mistake this type prevents.
        assertNull(FoundCustody.fromValue(null))
        assertNull(FoundCustody.fromValue(""))
        assertNull(FoundCustody.fromValue("WITH_FINDER"))
        assertNull(FoundCustody.fromValue("adopted"))
    }
}
