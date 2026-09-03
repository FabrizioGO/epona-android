package com.fabriziogo.epona.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.fabriziogo.epona.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUser(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUser(userId: String): Flow<UserEntity?>

    // Upsert rather than INSERT OR REPLACE: `users` is the parent of `pets`, which is in
    // turn the parent of `alerts`, both ON DELETE CASCADE. REPLACE deletes the conflicting
    // row before re-inserting it, so simply refreshing a profile took that user's cached
    // pets and alerts down with it.
    @Upsert
    suspend fun insertUser(user: UserEntity)

    /**
     * Records an alert owner we only know from a denormalised RPC payload, so the foreign
     * key on `alerts.user_id` is satisfiable. IGNORE on conflict: a stub like this must
     * never overwrite a profile that was fetched in full.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserIfAbsent(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("""
        UPDATE users
        SET display_name = :displayName,
            phone = :phone,
            avatar_url = :avatarUrl,
            updated_at = :updatedAt
        WHERE id = :userId
    """)
    suspend fun updateProfile(
        userId: String,
        displayName: String,
        phone: String?,
        avatarUrl: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE users
        SET latitude = :lat, longitude = :lng, updated_at = :updatedAt
        WHERE id = :userId
    """)
    suspend fun updateLocation(
        userId: String,
        lat: Double,
        lng: Double,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE users
        SET alert_radius_km = :radiusKm, updated_at = :updatedAt
        WHERE id = :userId
    """)
    suspend fun updateAlertRadius(
        userId: String,
        radiusKm: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE users
        SET fcm_token = :token, updated_at = :updatedAt
        WHERE id = :userId
    """)
    suspend fun updateFcmToken(
        userId: String,
        token: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}