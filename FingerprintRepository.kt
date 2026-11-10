package com.example.data.repository

import com.example.data.local.BiometricProfileDao
import com.example.data.local.BiometricProfileEntity
import com.example.data.local.FingerprintDao
import com.example.data.local.FingerprintRecordEntity
import com.example.model.FingerprintAnalysisResult
import com.example.model.FingerprintPatternType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FingerprintRepository(
    private val fingerprintDao: FingerprintDao,
    private val profileDao: BiometricProfileDao
) {

    val allFingerprints: Flow<List<FingerprintRecordEntity>> = fingerprintDao.getAllFingerprints()
    val allFullProfiles: Flow<List<BiometricProfileEntity>> = profileDao.getAllProfiles()

    suspend fun saveAnalysis(
        result: FingerprintAnalysisResult,
        personName: String,
        fingerName: String,
        handSide: String,
        personalitySummary: String,
        personalityArchetype: String,
        traits: List<String>
    ) {
        val record = FingerprintRecordEntity(
            id = result.id,
            personName = personName.ifBlank { "مستخدم" },
            fingerName = fingerName.ifBlank { "الإبهام" },
            handSide = handSide.ifBlank { "اليد اليمنى" },
            patternType = result.patternType.name,
            patternArabicName = result.patternType.arabicName,
            totalRidgeCountTRC = result.level2.totalRidgeCountTRC,
            horizontalRidgeCount = result.level2.horizontalRidgeCount,
            verticalRidgeCount = result.level2.verticalRidgeCount,
            minutiaeCount = result.level2.totalMinutiaeCount,
            ridgeDensity = result.level2.ridgeDensityPerCm,
            uniquenessRatio = result.uniquenessRatio,
            personalityArchetype = personalityArchetype,
            personalitySummary = personalitySummary,
            primaryTraitsJoined = traits.joinToString(", "),
            timestamp = System.currentTimeMillis()
        )
        fingerprintDao.insertFingerprint(record)
    }

    suspend fun saveFullBiometricProfile(profile: BiometricProfileEntity) {
        profileDao.insertProfile(profile)
    }

    suspend fun deleteProfile(id: String) {
        profileDao.deleteProfileById(id)
    }

    suspend fun deleteFingerprint(id: String) {
        fingerprintDao.deleteFingerprintById(id)
    }

    suspend fun seedInitialDataIfEmpty() {
        if (fingerprintDao.getCount() == 0) {
            val sample1 = FingerprintRecordEntity(
                id = UUID.randomUUID().toString(),
                personName = "أحمد",
                fingerName = "الإبهام",
                handSide = "اليد اليمنى",
                patternType = FingerprintPatternType.WHORL.name,
                patternArabicName = FingerprintPatternType.WHORL.arabicName,
                totalRidgeCountTRC = 22,
                horizontalRidgeCount = 28,
                verticalRidgeCount = 31,
                minutiaeCount = 54,
                ridgeDensity = 21.5f,
                uniquenessRatio = "1 في 130 مليار",
                personalityArchetype = "القائد الاستراتيجي والمستقل",
                personalitySummary = "شخصية قيادية ذات إرادة حديدية ورؤية بعيدة المدى، بارع في حل المعضلات المعقدة والاعتماد على الذات.",
                primaryTraitsJoined = "إرادة حديدية, تفكير استراتيجي, طموح, دقة ومثالية",
                timestamp = System.currentTimeMillis() - 86400000L
            )

            val sample2 = FingerprintRecordEntity(
                id = UUID.randomUUID().toString(),
                personName = "سارة",
                fingerName = "السبابة",
                handSide = "اليد اليسرى",
                patternType = FingerprintPatternType.ULNAR_LOOP.name,
                patternArabicName = FingerprintPatternType.ULNAR_LOOP.arabicName,
                totalRidgeCountTRC = 17,
                horizontalRidgeCount = 24,
                verticalRidgeCount = 26,
                minutiaeCount = 46,
                ridgeDensity = 19.8f,
                uniquenessRatio = "1 في 110 مليار",
                personalityArchetype = "الدبلوماسي الاجتماعي والمتعاطف",
                personalitySummary = "ذكاء عاطفي فائق وقدرة استثنائية على بناء جسور الود والتفاهم وخلق بيئة يسودها الانسجام والتعاون.",
                primaryTraitsJoined = "مرونة اجتماعية, ذكاء عاطفي, روح التعاون, سلام داخلي",
                timestamp = System.currentTimeMillis() - 43200000L
            )

            fingerprintDao.insertFingerprint(sample1)
            fingerprintDao.insertFingerprint(sample2)
        }

        if (profileDao.getProfileCount() == 0) {
            val sampleFullProfile = BiometricProfileEntity(
                id = UUID.randomUUID().toString(),
                personName = "مستخدم معتمد",
                timestamp = System.currentTimeMillis(),
                fingerprintPattern = "WHORL",
                fingerprintPatternArabic = "دوامة مركزية (Whorl)",
                fingerprintRidgeCountTRC = 23,
                fingerprintHorizontalRidges = 28,
                fingerprintVerticalRidges = 32,
                fingerprintMinutiaeCount = 52,
                fingerprintRidgeDensity = 21.2f,
                eyeShape = "ALMOND",
                eyeShapeArabic = "عين لوزية (Almond)",
                eyeWidthMm = 32.4f,
                eyeHeightMm = 12.8f,
                eyeCanthalTiltDeg = 4.2f,
                eyeSizeCategory = "متوسطة متناسقة",
                noseShape = "ROMAN_AQUILINE",
                noseShapeArabic = "أنف روماني / نسر (Roman)",
                noseLengthMm = 52.0f,
                noseWidthMm = 34.5f,
                noseTipAngleDeg = 96.5f,
                noseVolumeCategory = "بارز قيادي",
                earMorphology = "FREE_LOBE",
                earMorphologyArabic = "شحمة أذن منفصلة (Free Lobe)",
                earLengthMm = 62.5f,
                earWidthMm = 35.0f,
                earLobeAttachment = "منفصلة حرة",
                earCurvatureCategory = "منحنية متناسقة",
                personalityArchetype = "القائد الحاسم والاستراتيجي الميداني",
                personalitySummary = "تكامل فريد يجمع بين إرادة بصمة الدوامة الفولاذية، واستبصار العين اللوزية المتزنة، وجرأة الأنف الروماني، واستقلالية الأذن الحرة.",
                primaryTraitsJoined = "إرادة حديدية, حكمة واستبصار, جرأة قيادية, استقلالية فكرية, دقة تنفيذية",
                leadershipScore = 96,
                creativityScore = 86,
                empathyScore = 80,
                analyticalScore = 94,
                communicationStyle = "دبلوماسي رفيع؛ ينصت بعمق ويحسم الحوار بكلمات سديدة وواثقة.",
                decisionMakingStyle = "قرارات شجاعة ومحسوبة؛ يثق بحدسه القيادي ويتحمل المسؤولية الكاملة.",
                recommendedCareersJoined = "إدارة الأعمال والقيادة التنفيذية, التخطيط الاستراتيجي, ريادة الأعمال والاستثمار",
                identityCode = "BIO-ID-F4A1-23"
            )
            profileDao.insertProfile(sampleFullProfile)
        }
    }
}