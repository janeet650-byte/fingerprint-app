package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FingerprintDao {

    @Query("SELECT * FROM fingerprint_records ORDER BY timestamp DESC")
    fun getAllFingerprints(): Flow<List<FingerprintRecordEntity>>

    @Query("SELECT * FROM fingerprint_records WHERE id = :id LIMIT 1")
    suspend fun getFingerprintById(id: String): FingerprintRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFingerprint(record: FingerprintRecordEntity)

    @Delete
    suspend fun deleteFingerprint(record: FingerprintRecordEntity)

    @Query("DELETE FROM fingerprint_records WHERE id = :id")
    suspend fun deleteFingerprintById(id: String)

    @Query("SELECT COUNT(*) FROM fingerprint_records")
    suspend fun getCount(): Int
}