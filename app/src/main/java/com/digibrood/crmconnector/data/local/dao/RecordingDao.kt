package com.digibrood.crmconnector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.digibrood.crmconnector.data.local.entity.RecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recording: RecordingEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM recordings WHERE filePath = :filePath)")
    suspend fun existsByPath(filePath: String): Boolean

    @Query("SELECT * FROM recordings WHERE uploadState = :state ORDER BY recordedAt ASC LIMIT :limit")
    suspend fun getByState(state: String, limit: Int): List<RecordingEntity>

    @Query("SELECT * FROM recordings WHERE clientCallId = :clientCallId LIMIT 1")
    suspend fun getByCall(clientCallId: String): RecordingEntity?

    @Query("UPDATE recordings SET uploadState = :state, attemptCount = attemptCount + 1, lastAttemptAt = :now WHERE id = :id")
    suspend fun markState(id: Long, state: String, now: Long)

    @Query("UPDATE recordings SET uploadState = 'UPLOADED', recordingId = :recordingId, objectKey = :objectKey WHERE id = :id")
    suspend fun markUploaded(id: Long, recordingId: String?, objectKey: String?)

    @Query("SELECT COUNT(*) FROM recordings WHERE uploadState = 'UPLOADED' AND lastAttemptAt >= :since")
    suspend fun uploadedSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM recordings WHERE uploadState IN ('PENDING', 'FAILED')")
    fun pendingCount(): Flow<Int>

    @Query("DELETE FROM recordings WHERE uploadState = 'UPLOADED' AND createdAt < :olderThan")
    suspend fun purgeUploadedOlderThan(olderThan: Long)
}
