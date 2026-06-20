package com.digibrood.crmconnector.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.digibrood.crmconnector.data.local.dao.CallDao
import com.digibrood.crmconnector.data.local.dao.RecordingDao
import com.digibrood.crmconnector.data.local.dao.RemarkDao
import com.digibrood.crmconnector.data.local.entity.CallEntity
import com.digibrood.crmconnector.data.local.entity.RecordingEntity
import com.digibrood.crmconnector.data.local.entity.RemarkEntity

/**
 * Room database that holds the offline sync queues for calls, recordings and
 * remarks. Everything captured offline lives here until the CRM acknowledges it.
 */
@Database(
    entities = [
        CallEntity::class,
        RecordingEntity::class,
        RemarkEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callDao(): CallDao
    abstract fun recordingDao(): RecordingDao
    abstract fun remarkDao(): RemarkDao

    companion object {
        const val NAME = "crm_connector.db"
    }
}
