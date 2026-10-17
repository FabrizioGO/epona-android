package com.fabriziogo.epona.feature.adoption.create

import com.fabriziogo.epona.core.ui.text.UiText

import com.fabriziogo.epona.core.domain.model.AgeStage
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.domain.usecase.adoption.ADOPTION_TITLE_MAX_LENGTH
import com.fabriziogo.epona.core.domain.usecase.adoption.ADOPTION_TITLE_MIN_LENGTH
import com.fabriziogo.epona.core.media.PhotoItem

data class CreateAdoptionUiState(
    val title: String = "",
    val species: Species = Species.DOG,
    val breed: String = "",
    val petName: String = "",
    val ageStage: AgeStage? = null,
    val size: PetSize? = null,
    val gender: PetGender? = null,
    val description: String = "",
    /** Raw field text; parsed by [priceValue]. Blank means free. */
    val price: String = "",
    val phone: String = "",
    val photos: List<PhotoItem> = emptyList(),
    val isProcessingPhotos: Boolean = false,
    val isSubmitting: Boolean = false,
    /** Set after the first publish attempt so required-field errors do not show while typing. */
    val showErrors: Boolean = false,
    val error: UiText? = null
) {
    val trimmedTitle: String get() = title.trim()

    val isTitleValid: Boolean
        get() = trimmedTitle.length in ADOPTION_TITLE_MIN_LENGTH..ADOPTION_TITLE_MAX_LENGTH

    val isPhoneValid: Boolean get() = phone.isNotBlank()

    /** Null for a blank field; also null for text that is not a number — see [isPriceValid]. */
    val priceValue: Double? get() = parsePrice(price)

    val isPriceValid: Boolean
        get() = price.isBlank() || (priceValue?.let { it >= 0.0 && it.isFinite() } == true)

    val canPublish: Boolean
        get() = isTitleValid && isPhoneValid && isPriceValid && !isSubmitting && !isProcessingPhotos

    /** True once the user has typed or picked anything, so leaving should confirm. */
    val isDirty: Boolean
        get() = title.isNotBlank() || breed.isNotBlank() || petName.isNotBlank() ||
            ageStage != null || size != null || gender != null ||
            description.isNotBlank() || price.isNotBlank() || photos.isNotEmpty()
}

/** Accepts "25", "25.5" and "25,5"; blank or unparseable text gives null. */
fun parsePrice(text: String): Double? =
    text.trim().replace(',', '.').takeIf { it.isNotEmpty() }?.toDoubleOrNull()
