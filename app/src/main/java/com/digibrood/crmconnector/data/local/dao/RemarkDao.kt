package com.digibrood.crmconnector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.digibrood.crmconnector.data.local.entity.RemarkEntity

@Dao
interface RemarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remark: RemarkEntity): Long

    @Query("SELECT * FROM remarks WHERE syncState = :state ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getByState(state: String, limit: Int): List<RemarkEntity>

    @Query("UPDATE remarks SET syncState = :state, attemptCount = attemptCount + 1 WHERE id = :id")
    suspend fun markState(id: Long, state: String)

    @Query("DELETE FROM remarks WHERE syncState = 'SYNCED' AND createdAt < :olderThan")
    suspend fun purgeSyncedOlderThan(olderThan: Long)
}
