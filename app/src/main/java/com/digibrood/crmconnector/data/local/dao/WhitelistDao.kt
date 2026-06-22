package com.digibrood.crmconnector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.digibrood.crmconnector.data.local.entity.WhitelistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: WhitelistEntity): Long

    @Query("SELECT * FROM whitelist ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WhitelistEntity>>

    @Query("SELECT * FROM whitelist ORDER BY createdAt DESC")
    suspend fun getAll(): List<WhitelistEntity>

    @Query("SELECT * FROM whitelist WHERE number = :number LIMIT 1")
    suspend fun getByNumber(number: String): WhitelistEntity?

    @Query("SELECT * FROM whitelist WHERE proposedToCrm = 0")
    suspend fun getUnproposed(): List<WhitelistEntity>

    @Query("SELECT number FROM whitelist WHERE status = 'APPROVED'")
    suspend fun approvedNumbers(): List<String>

    @Query("UPDATE whitelist SET status = :status, updatedAt = :now WHERE number = :number")
    suspend fun updateStatus(number: String, status: String, now: Long)

    @Query("UPDATE whitelist SET proposedToCrm = :proposed, status = :status, updatedAt = :now WHERE number = :number")
    suspend fun markProposed(number: String, proposed: Boolean, status: String, now: Long)

    @Query("DELETE FROM whitelist WHERE number = :number")
    suspend fun deleteByNumber(number: String)
}
