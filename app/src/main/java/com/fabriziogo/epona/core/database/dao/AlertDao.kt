package com.fabriziogo.epona.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fabriziogo.epona.core.database.entity.AlertEntity
import com.fabriziogo.epona.core.database.entity.AlertWithPetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    // ---- Alerts with Pet relationship ----

    @Transaction
    @Query("""
        SELECT * FROM alerts
        WHERE status = 'active'
        ORDER BY created_at DESC
    """)
    fun observeActiveAlerts(): Flow<List<AlertWithPetEntity>>

    @Transaction
    @Query("""
        SELECT * FROM alerts
        WHERE status = 'active'
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getActiveAlerts(
        limit: Int = 50,
        offset: Int = 0
    ): List<AlertWithPetEntity>

    @Transaction
    @Query("""
        SELECT * FROM alerts
        WHERE status = 'active' AND type = :type
        ORDER BY created_at DESC
    """)
    fun observeActiveAlertsByType(type: String): Flow<List<AlertWithPetEntity>>

    @Transaction
    @Query("SELECT * FROM alerts WHERE id = :alertId LIMIT 1")
    suspend fun getAlertWithPet(alertId: String): AlertWithPetEntity?

    @Transaction
    @Query("SELECT * FROM alerts WHERE id = :alertId LIMIT 1")
    fun observeAlertWithPet(alertId: String): Flow<AlertWithPetEntity?>

    @Transaction
    @Query("""
        SELECT * FROM alerts
        WHERE user_id = :userId
        ORDER BY created_at DESC
    """)
    fun observeMyAlerts(userId: String): Flow<List<AlertWithPetEntity>>

    @Transaction
    @Query("""
        SELECT * FROM alerts
        WHERE user_id = :userId
        ORDER BY created_at DESC
    """)
    suspend fun getMyAlerts(userId: String): List<AlertWithPetEntity>

    // ---- Single alert operations ----

    @Query("SELECT * FROM alerts WHERE id = :alertId LIMIT 1")
    suspend fun getAlert(alertId: String): AlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertEntity>)

    @Update
    suspend fun updateAlert(alert: AlertEntity)

    @Query("""
        UPDATE alerts
        SET status = 'resolved',
            resolved_at = :resolvedAt,
            updated_at = :updatedAt
        WHERE id = :alertId
    """)
    suspend fun resolveAlert(
        alertId: String,
        resolvedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE alerts
        SET sighting_count = sighting_count + 1,
            updated_at = :updatedAt
        WHERE id = :alertId
    """)
    suspend fun incrementSightingCount(
        alertId: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM alerts WHERE id = :alertId")
    suspend fun deleteAlert(alertId: String)

    @Query("DELETE FROM alerts WHERE status = 'expired'")
    suspend fun deleteExpiredAlerts()

    @Query("DELETE FROM alerts")
    suspend fun deleteAll()

    // ---- Counts ----

    @Query("SELECT COUNT(*) FROM alerts WHERE user_id = :userId AND status = 'active'")
    suspend fun getActiveAlertCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM alerts WHERE status = 'active'")
    fun observeActiveAlertCount(): Flow<Int>
}