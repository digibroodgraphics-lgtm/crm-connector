package com.digibrood.crmconnector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.digibrood.crmconnector.data.local.entity.CallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(call: CallEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(calls: List<CallEntity>): List<Long>

    @Query("SELECT * FROM calls WHERE syncState = :state ORDER BY startTime ASC LIMIT :limit")
    suspend fun getByState(state: String, limit: Int): List<CallEntity>

    @Query("SELECT * FROM calls WHERE clientCallId = :clientCallId LIMIT 1")
    suspend fun getByClientId(clientCallId: String): CallEntity?

    @Query("SELECT * FROM calls WHERE phoneNumber = :num AND startTime = :start LIMIT 1")
    suspend fun findByNumberAndStart(num: String, start: Long): CallEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM calls WHERE clientCallId = :clientCallId)")
    suspend fun exists(clientCallId: String): Boolean

    @Query("UPDATE calls SET syncState = :state, attemptCount = attemptCount + 1, lastAttemptAt = :now WHERE clientCallId IN (:ids)")
    suspend fun markState(ids: List<String>, state: String, now: Long)

    @Query("UPDATE calls SET syncState = 'SYNCED', serverCallId = :serverCallId WHERE clientCallId = :clientCallId")
    suspend fun markSynced(clientCallId: String, serverCallId: String?)

    @Query("SELECT COUNT(*) FROM calls WHERE syncState != 'SYNCED'")
    fun pendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM calls WHERE syncState = 'SYNCED' AND lastAttemptAt >= :since")
    suspend fun syncedSince(since: Long): Int

    @Query("DELETE FROM calls WHERE syncState = 'SYNCED' AND createdAt < :olderThan")
    suspend fun purgeSyncedOlderThan(olderThan: Long)

    @Query("DELETE FROM calls WHERE clientCallId IN (:ids)")
    suspend fun deleteByClientIds(ids: List<String>)

    @Query("SELECT MAX(startTime) FROM calls")
    suspend fun latestCapturedStartTime(): Long?

    @Query("SELECT * FROM calls WHERE syncState = 'SYNCED' ORDER BY lastAttemptAt DESC, startTime DESC LIMIT 1")
    suspend fun lastSyncedCall(): CallEntity?
}
