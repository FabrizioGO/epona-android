package com.fabriziogo.epona.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fabriziogo.epona.core.database.converter.Converters
import com.fabriziogo.epona.core.database.dao.AlertDao
import com.fabriziogo.epona.core.database.dao.NotificationDao
import com.fabriziogo.epona.core.database.dao.PetDao
import com.fabriziogo.epona.core.database.dao.SightingDao
import com.fabriziogo.epona.core.database.dao.UserDao
import com.fabriziogo.epona.core.database.entity.AlertEntity
import com.fabriziogo.epona.core.database.entity.NotificationEntity
import com.fabriziogo.epona.core.database.entity.PetEntity
import com.fabriziogo.epona.core.database.entity.SightingEntity
import com.fabriziogo.epona.core.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PetEntity::class,
        AlertEntity::class,
        SightingEntity::class,
        NotificationEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class EponaDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun alertDao(): AlertDao
    abstract fun sightingDao(): SightingDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        const val DATABASE_NAME = "epona_db"
    }
}