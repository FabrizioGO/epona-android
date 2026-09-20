package com.fabriziogo.epona.feature.home

import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.Species

/**
 * Sample data for `@Preview`s. Mirrors only the fields the home feed's network mapper
 * (`NearbyAlertDto.toDomain()`) actually populates — no `ownerAvatar`, no pet `description`.
 */
internal fun previewAlertWithDetails(
    id: String = "preview-1",
    name: String = "Milo",
    species: Species = Species.DOG,
    type: AlertType = AlertType.LOST,
    breed: String? = "Beagle",
    color: String? = "Brown & white",
    address: String? = "Maple Street, Riverside",
    reward: Double? = 50.0,
    sightingCount: Int = 3,
    distanceMeters: Double? = 850.0,
    photoUrls: List<String> = emptyList()
): AlertWithDetails = AlertWithDetails(
    alert = Alert(
        id = id,
        type = type,
        lastSeenLocation = Location(40.7128, -74.0060),
        lastSeenAddress = address,
        lastSeenAt = System.currentTimeMillis(),
        reward = reward,
        sightingCount = sightingCount,
        createdAt = System.currentTimeMillis()
    ),
    pet = Pet(
        name = name,
        species = species,
        breed = breed,
        color = color,
        photoUrls = photoUrls
    ),
    ownerName = "Alex",
    ownerAvatar = null,
    distanceMeters = distanceMeters
)

internal val sampleAlerts: List<AlertWithDetails> = listOf(
    previewAlertWithDetails(
        id = "preview-1",
        name = "Milo",
        species = Species.DOG,
        type = AlertType.LOST,
        address = "Maple Street, Riverside",
        reward = 50.0,
        sightingCount = 3,
        distanceMeters = 850.0
    ),
    previewAlertWithDetails(
        id = "preview-2",
        name = "Luna",
        species = Species.CAT,
        type = AlertType.FOUND,
        breed = "Tabby",
        color = "Grey",
        address = "Oak Avenue",
        reward = null,
        sightingCount = 1,
        distanceMeters = 1200.0
    ),
    previewAlertWithDetails(
        id = "preview-3",
        name = "Rocky",
        species = Species.DOG,
        type = AlertType.LOST,
        breed = "Labrador",
        color = "Golden",
        address = "Central Park West",
        reward = 100.0,

        sightingCount = 7,
        distanceMeters = 320.0
    ),
    previewAlertWithDetails(
        id = "preview-4",
        name = "Coco",
        species = Species.DOG,
        type = AlertType.FOUND,
        breed = null,
        color = "White",
        address = "Elm Street",
        reward = null,
        sightingCount = 0,
        distanceMeters = 2100.0
    )
)
