package com.digibrood.crmconnector.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.digibrood.crmconnector.data.local.dao.CallDao
import com.digibrood.crmconnector.data.local.dao.RecordingDao
import com.digibrood.crmconnector.data.local.dao.RemarkDao
import com.digibrood.crmconnector.data.local.dao.WhitelistDao
import com.digibrood.crmconnector.data.local.entity.CallEntity
import com.digibrood.crmconnector.data.local.entity.RecordingEntity
import com.digibrood.crmconnector.data.local.entity.RemarkEntity
import com.digibrood.crmconnector.data.local.entity.WhitelistEntity

/**
 * Room database that holds the offline sync queues for calls, recordings and
 * remarks. Everything captured offline lives here until the CRM acknowledges it.
 */
@Database(
    entities = [
        CallEntity::class,
        RecordingEntity::class,
        RemarkEntity::class,
        WhitelistEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callDao(): CallDao
    abstract fun recordingDao(): RecordingDao
    abstract fun remarkDao(): RemarkDao
    abstract fun whitelistDao(): WhitelistDao

    companion object {
        const val NAME = "crm_connector.db"
    }
}
