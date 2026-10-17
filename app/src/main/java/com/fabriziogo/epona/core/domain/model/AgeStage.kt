package com.fabriziogo.epona.core.domain.model

import androidx.annotation.StringRes
import com.fabriziogo.epona.R

/**
 * Rough life stage of the pet(s) in an adoption post. A stage rather than a number
 * because a post may cover a whole litter, or an animal nobody knows the exact age of.
 *
 * Optional everywhere: [fromValue] returns null rather than a default, so an unknown
 * server value reads as "not given" instead of inventing an age.
 */
enum class AgeStage(
    val value: String,
    @StringRes private val babyLabel: Int,
    @StringRes private val genericLabel: Int
) {
    BABY("baby", R.string.age_stage_baby, R.string.age_stage_baby),
    YOUNG("young", R.string.age_stage_young, R.string.age_stage_young),
    ADULT("adult", R.string.age_stage_adult, R.string.age_stage_adult),
    SENIOR("senior", R.string.age_stage_senior, R.string.age_stage_senior);

    /** "Puppy" / "Kitten" for dogs and cats; the other stages read the same for every species. */
    @StringRes
    fun label(species: Species): Int = when (this) {
        BABY -> when (species) {
            Species.DOG -> R.string.age_stage_puppy
            Species.CAT -> R.string.age_stage_kitten
            Species.OTHER -> babyLabel
        }
        else -> genericLabel
    }

    companion object {
        fun fromValue(v: String?): AgeStage? = entries.firstOrNull { it.value == v }
    }
}
