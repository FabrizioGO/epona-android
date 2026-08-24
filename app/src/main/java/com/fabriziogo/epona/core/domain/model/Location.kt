package com.fabriziogo.epona.core.domain.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
) {
    companion object {
        val EMPTY = Location(0.0, 0.0)
    }
}