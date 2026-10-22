package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import com.fabriziogo.epona.core.network.dto.BlockUserInsertParams
import com.fabriziogo.epona.core.network.dto.BlockedUserDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockService @Inject constructor(
    private val supabase: SupabaseProvider
) {
    private val table get() = supabase.client.postgrest["user_blocks"]

    suspend fun block(params: BlockUserInsertParams) {
        table.insert(params)
    }

    // RLS limits the delete to the caller's own rows, so filtering by blocked_id is enough.
    suspend fun unblock(blockedId: String) {
        table.delete { filter { eq("blocked_id", blockedId) } }
    }

    suspend fun getBlockedUsers(): List<BlockedUserDto> =
        supabase.client.postgrest["blocked_users_list"].select {
            order("created_at", Order.DESCENDING)
        }.decodeList()
}
