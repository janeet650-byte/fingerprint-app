package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "biometric_full_profiles")
data class BiometricProfileEntity(
    @PrimaryKey
    val id: String,
    val personName: String,
    val timestamp: Long,

    // قياسات بصمة الإصبع
    val fingerprintPattern: String,
    val fingerprintPatternArabic: String,
    val fingerprintRidgeCountTRC: Int,
    val fingerprintHorizontalRidges: Int,
    val fingerprintVerticalRidges: Int,
    val fingerprintMinutiaeCount: Int,
    val fingerprintRidgeDensity: Float,

    // قياسات العين
    val eyeShape: String,
    val eyeShapeArabic: String,
    val eyeWidthMm: Float,
    val eyeHeightMm: Float,
    val eyeCanthalTiltDeg: Float,
    val eyeSizeCategory: String,

    // قياسات الأنف
    val noseShape: String,
    val noseShapeArabic: String,
    val noseLengthMm: Float,
    val noseWidthMm: Float,
    val noseTipAngleDeg: Float,
    val noseVolumeCategory: String,

    // قياسات الأذن
    val earMorphology: String,
    val earMorphologyArabic: String,
    val earLengthMm: Float,
    val earWidthMm: Float,
    val earLobeAttachment: String,
    val earCurvatureCategory: String,

    // تحليل الشخصية المدمج
    val personalityArchetype: String,
    val personalitySummary: String,
    val primaryTraitsJoined: String,
    val leadershipScore: Int,
    val creativityScore: Int,
    val empathyScore: Int,
    val analyticalScore: Int,
    val communicationStyle: String,
    val decisionMakingStyle: String,
    val recommendedCareersJoined: String,
    val identityCode: String
)

@Dao
interface BiometricProfileDao {

    @Query("SELECT * FROM biometric_full_profiles ORDER BY timestamp DESC")
    fun getAllProfiles(): Flow<List<BiometricProfileEntity>>

    @Query("SELECT * FROM biometric_full_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): BiometricProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BiometricProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: BiometricProfileEntity)

    @Query("DELETE FROM biometric_full_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String)

    @Query("SELECT COUNT(*) FROM biometric_full_profiles")
    suspend fun getProfileCount(): Int
}