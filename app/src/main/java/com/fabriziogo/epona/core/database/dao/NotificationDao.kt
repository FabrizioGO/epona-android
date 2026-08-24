package com.fabriziogo.epona.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fabriziogo.epona.core.database.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("""
        SELECT * FROM notifications
        WHERE user_id = :userId
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getNotifications(
        userId: String,
        limit: Int = 50,
        offset: Int = 0
    ): List<NotificationEntity>

    @Query("""
        SELECT * FROM notifications
        WHERE user_id = :userId
        ORDER BY created_at DESC
    """)
    fun observeNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("""
        SELECT COUNT(*) FROM notifications
        WHERE user_id = :userId AND is_read = 0
    """)
    fun observeUnreadCount(userId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM notifications
        WHERE user_id = :userId AND is_read = 0
    """)
    suspend fun getUnreadCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("""
        UPDATE notifications
        SET is_read = 1
        WHERE user_id = :userId AND is_read = 0
    """)
    suspend fun markAllAsRead(userId: String)

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: String)

    @Query("DELETE FROM notifications WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}