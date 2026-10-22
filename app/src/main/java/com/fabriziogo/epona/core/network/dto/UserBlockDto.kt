package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A plain table insert on `user_blocks` -- see
 * supabase/migrations/20261022000000_user_blocks.sql. blocker_id and created_at
 * are filled by column DEFAULTs; `authenticated` only holds INSERT on blocked_id.
 */
@Serializable
data class BlockUserInsertParams(
    @SerialName("blocked_id")
    val blockedId: String
)

// A row of the `blocked_users_list` view.
@Serializable
data class BlockedUserDto(
    @SerialName("blocked_id")
    val blockedId: String,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)
