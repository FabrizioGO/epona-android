package com.fabriziogo.epona.feature.adoption.create

import android.net.Uri
import com.fabriziogo.epona.core.domain.model.AgeStage
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.media.PhotoItem

sealed interface CreateAdoptionEvent {
    data class TitleChanged(val title: String) : CreateAdoptionEvent
    data class SpeciesChanged(val species: Species) : CreateAdoptionEvent
    data class BreedChanged(val breed: String) : CreateAdoptionEvent
    data class PetNameChanged(val name: String) : CreateAdoptionEvent

    /** Null clears the choice; tapping the selected chip again sends null. */
    data class AgeStageChanged(val stage: AgeStage?) : CreateAdoptionEvent
    data class SizeChanged(val size: PetSize?) : CreateAdoptionEvent
    data class GenderChanged(val gender: PetGender?) : CreateAdoptionEvent
    data class DescriptionChanged(val description: String) : CreateAdoptionEvent
    data class PriceChanged(val price: String) : CreateAdoptionEvent
    data class PhoneChanged(val phone: String) : CreateAdoptionEvent

    data class PhotosPicked(val uris: List<Uri>) : CreateAdoptionEvent
    data class PhotoRemoved(val photo: PhotoItem) : CreateAdoptionEvent

    data object PublishClicked : CreateAdoptionEvent
    data object CloseClicked : CreateAdoptionEvent
    data object ErrorDismissed : CreateAdoptionEvent
}

sealed interface CreateAdoptionNavEvent {
    data object NavigateBack : CreateAdoptionNavEvent
    data class NavigateToDetail(val postId: String) : CreateAdoptionNavEvent
}
