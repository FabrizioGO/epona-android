package com.fabriziogo.epona.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fabriziogo.epona.core.database.entity.SightingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SightingDao {

    @Query("""
        SELECT * FROM sightings
        WHERE alert_id = :alertId
        ORDER BY spotted_at ASC
    """)
    fun observeSightingTrail(alertId: String): Flow<List<SightingEntity>>

    @Query("""
        SELECT * FROM sightings
        WHERE alert_id = :alertId
        ORDER BY spotted_at ASC
    """)
    suspend fun getSightingTrail(alertId: String): List<SightingEntity>

    @Query("SELECT * FROM sightings WHERE id = :sightingId LIMIT 1")
    suspend fun getSighting(sightingId: String): SightingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSighting(sighting: SightingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSightings(sightings: List<SightingEntity>)

    @Query("DELETE FROM sightings WHERE id = :sightingId")
    suspend fun deleteSighting(sightingId: String)

    @Query("DELETE FROM sightings WHERE alert_id = :alertId")
    suspend fun deleteAllForAlert(alertId: String)

    @Query("SELECT COUNT(*) FROM sightings WHERE alert_id = :alertId")
    suspend fun getSightingCount(alertId: String): Int

    @Query("SELECT COUNT(*) FROM sightings WHERE reporter_id = :reporterId")
    suspend fun getReporterSightingCount(reporterId: String): Int
}