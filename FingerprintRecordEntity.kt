package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fingerprint_records")
data class FingerprintRecordEntity(
    @PrimaryKey
    val id: String,
    val personName: String,
    val fingerName: String, // إبهام، سبابة، وسطى، بنصر، خنصر
    val handSide: String,   // اليد اليمنى، اليد اليسرى
    val patternType: String, // WHORL, ULNAR_LOOP, etc.
    val patternArabicName: String,
    val totalRidgeCountTRC: Int,
    val horizontalRidgeCount: Int,
    val verticalRidgeCount: Int,
    val minutiaeCount: Int,
    val ridgeDensity: Float,
    val uniquenessRatio: String,
    val personalityArchetype: String,
    val personalitySummary: String,
    val primaryTraitsJoined: String, // سمات مفصولة بفواصل
    val timestamp: Long
)