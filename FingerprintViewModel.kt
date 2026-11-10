package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BiometricProfileEntity
import com.example.data.local.FingerprintRecordEntity
import com.example.data.repository.FingerprintRepository
import com.example.engine.ArchetypeGenerator
import com.example.engine.DermatoglyphicsEngine
import com.example.engine.FingerprintProcessor
import com.example.engine.MorphoBiometricsEngine
import com.example.model.FingerprintAnalysisResult
import com.example.model.FingerprintPatternType
import com.example.model.LayerToggleState
import com.example.model.MinutiaePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class FingerprintViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FingerprintRepository

    val savedFingerprints: StateFlow<List<FingerprintRecordEntity>>
    val savedFullProfiles: StateFlow<List<BiometricProfileEntity>>

    private val _analysisResult = MutableStateFlow<FingerprintAnalysisResult?>(null)
    val analysisResult: StateFlow<FingerprintAnalysisResult?> = _analysisResult.asStateFlow()

    private val _processedBitmaps = MutableStateFlow<FingerprintProcessor.ProcessedBitmaps?>(null)
    val processedBitmaps: StateFlow<FingerprintProcessor.ProcessedBitmaps?> = _processedBitmaps.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _layerState = MutableStateFlow(LayerToggleState())
    val layerState: StateFlow<LayerToggleState> = _layerState.asStateFlow()

    private val _selectedMinutiae = MutableStateFlow<MinutiaePoint?>(null)
    val selectedMinutiae: StateFlow<MinutiaePoint?> = _selectedMinutiae.asStateFlow()

    private val _personalityProfile = MutableStateFlow<DermatoglyphicsEngine.PersonalityProfile?>(null)
    val personalityProfile: StateFlow<DermatoglyphicsEngine.PersonalityProfile?> = _personalityProfile.asStateFlow()

    private val _selectedEye = MutableStateFlow(MorphoBiometricsEngine.EyeShape.ALMOND)
    val selectedEye: StateFlow<MorphoBiometricsEngine.EyeShape> = _selectedEye.asStateFlow()

    private val _selectedNose = MutableStateFlow(MorphoBiometricsEngine.NoseShape.ROMAN_AQUILINE)
    val selectedNose: StateFlow<MorphoBiometricsEngine.NoseShape> = _selectedNose.asStateFlow()

    private val _selectedEar = MutableStateFlow(MorphoBiometricsEngine.EarMorphology.FREE_LOBE)
    val selectedEar: StateFlow<MorphoBiometricsEngine.EarMorphology> = _selectedEar.asStateFlow()

    private val _completeDossier = MutableStateFlow<MorphoBiometricsEngine.CompleteIdentityDossier?>(null)
    val completeDossier: StateFlow<MorphoBiometricsEngine.CompleteIdentityDossier?> = _completeDossier.asStateFlow()

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    private val _comparisonResult = MutableStateFlow<DermatoglyphicsEngine.ComparisonResult?>(null)
    val comparisonResult: StateFlow<DermatoglyphicsEngine.ComparisonResult?> = _comparisonResult.asStateFlow()

    private val _selectedForComparison = MutableStateFlow<List<FingerprintRecordEntity>>(emptyList())
    val selectedForComparison: StateFlow<List<FingerprintRecordEntity>> = _selectedForComparison.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FingerprintRepository(database.fingerprintDao(), database.biometricProfileDao())
        savedFingerprints = repository.allFingerprints
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        savedFullProfiles = repository.allFullProfiles
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            loadArchetype(FingerprintPatternType.WHORL)
        }
    }

    fun loadArchetype(pattern: FingerprintPatternType) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            withContext(Dispatchers.Default) {
                val bitmap = ArchetypeGenerator.generateArchetypeBitmap(pattern)
                processBitmapInternal(bitmap, pattern.arabicName, "نموذج معياري")
            }
            _isAnalyzing.value = false
        }
    }

    fun analyzeScannedBitmap(bitmap: Bitmap, sourceName: String = "ماسح حي") {
        viewModelScope.launch {
            _isAnalyzing.value = true
            withContext(Dispatchers.Default) {
                processBitmapInternal(bitmap, "بصمة مستشعرة حديثاً", sourceName)
            }
            _isAnalyzing.value = false
        }
    }

    private fun processBitmapInternal(bitmap: Bitmap, title: String, sourceName: String) {
        val (result, bitmaps) = FingerprintProcessor.analyzeFingerprint(bitmap, title, sourceName)
        val profile = DermatoglyphicsEngine.analyzePersonality(result)
        val dossier = MorphoBiometricsEngine.generateFullIdentity(
            subjectName = "المستخدم",
            fingerprintResult = result,
            eye = _selectedEye.value,
            nose = _selectedNose.value,
            ear = _selectedEar.value
        )

        _analysisResult.value = result
        _processedBitmaps.value = bitmaps
        _personalityProfile.value = profile
        _completeDossier.value = dossier
    }

    fun updateTransect(x1Norm: Float, y1Norm: Float, x2Norm: Float, y2Norm: Float) {
        val current = _analysisResult.value ?: return
        viewModelScope.launch(Dispatchers.Default) {
            val width = 300
            val height = 360
            val x1 = x1Norm * width
            val y1 = y1Norm * height
            val x2 = x2Norm * width
            val y2 = y2Norm * height

            val bmp = _processedBitmaps.value?.enhancedGrayscale ?: return@launch
            val enhanced = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val p = bmp.getPixel(x, y)
                    enhanced[y * width + x] = android.graphics.Color.red(p)
                }
            }

            val newTransect = FingerprintProcessor.computeTransectProfile(
                enhanced, x1, y1, x2, y2, width, height
            )

            _analysisResult.value = current.copy(transect = newTransect)
        }
    }

    fun selectMinutiae(minutiae: MinutiaePoint?) {
        _selectedMinutiae.value = minutiae
    }

    fun toggleEnhancedLayer() {
        val s = _layerState.value
        _layerState.value = s.copy(showEnhancedOriginal = !s.showEnhancedOriginal)
    }

    fun toggleSkeletonLayer() {
        val s = _layerState.value
        _layerState.value = s.copy(showSkeletonRidges = !s.showSkeletonRidges)
    }

    fun toggleMinutiaeLayer() {
        val s = _layerState.value
        _layerState.value = s.copy(showMinutiaeMarkers = !s.showMinutiaeMarkers)
    }

    fun toggleFlowVectorsLayer() {
        val s = _layerState.value
        _layerState.value = s.copy(showFlowVectors = !s.showFlowVectors)
    }

    fun toggleTransectRuler() {
        val s = _layerState.value
        _layerState.value = s.copy(showTransectRuler = !s.showTransectRuler)
    }

    fun togglePoresLayer() {
        val s = _layerState.value
        _layerState.value = s.copy(showSweatPores = !s.showSweatPores)
    }

    fun setEyeShape(eye: MorphoBiometricsEngine.EyeShape) {
        _selectedEye.value = eye
        regenerateIdentity()
    }

    fun setNoseShape(nose: MorphoBiometricsEngine.NoseShape) {
        _selectedNose.value = nose
        regenerateIdentity()
    }

    fun setEarMorphology(ear: MorphoBiometricsEngine.EarMorphology) {
        _selectedEar.value = ear
        regenerateIdentity()
    }

    fun regenerateIdentity(name: String = "المستخدم") {
        val result = _analysisResult.value ?: return
        _completeDossier.value = MorphoBiometricsEngine.generateFullIdentity(
            subjectName = name,
            fingerprintResult = result,
            eye = _selectedEye.value,
            nose = _selectedNose.value,
            ear = _selectedEar.value
        )
    }

    fun openSaveDialog() {
        _showSaveDialog.value = true
    }

    fun closeSaveDialog() {
        _showSaveDialog.value = false
    }

    fun saveCurrentFingerprint(personName: String, fingerName: String, handSide: String) {
        val result = _analysisResult.value ?: return
        val profile = _personalityProfile.value ?: return

        viewModelScope.launch {
            repository.saveAnalysis(
                result = result,
                personName = personName,
                fingerName = fingerName,
                handSide = handSide,
                personalitySummary = profile.detailedSummary,
                personalityArchetype = profile.archetypeTitle,
                traits = profile.primaryTraits
            )
            _showSaveDialog.value = false
        }
    }

    fun saveFullIntakeProfile(
        personName: String,
        eye: MorphoBiometricsEngine.EyeShape,
        eyeWidth: Float,
        eyeHeight: Float,
        eyeTilt: Float,
        nose: MorphoBiometricsEngine.NoseShape,
        noseLength: Float,
        noseWidth: Float,
        noseAngle: Float,
        ear: MorphoBiometricsEngine.EarMorphology,
        earLength: Float,
        earWidth: Float
    ) {
        val result = _analysisResult.value ?: return
        val profile = _personalityProfile.value ?: DermatoglyphicsEngine.analyzePersonality(result)
        val dossier = MorphoBiometricsEngine.generateFullIdentity(
            subjectName = personName,
            fingerprintResult = result,
            eye = eye,
            nose = nose,
            ear = ear
        )

        _selectedEye.value = eye
        _selectedNose.value = nose
        _selectedEar.value = ear
        _completeDossier.value = dossier

        viewModelScope.launch {
            val entity = BiometricProfileEntity(
                id = UUID.randomUUID().toString(),
                personName = personName.ifBlank { "مستخدم المنظومة" },
                timestamp = System.currentTimeMillis(),
                fingerprintPattern = result.patternType.name,
                fingerprintPatternArabic = result.patternType.arabicName,
                fingerprintRidgeCountTRC = result.level2.totalRidgeCountTRC,
                fingerprintHorizontalRidges = result.level2.horizontalRidgeCount,
                fingerprintVerticalRidges = result.level2.verticalRidgeCount,
                fingerprintMinutiaeCount = result.level2.totalMinutiaeCount,
                fingerprintRidgeDensity = result.level2.ridgeDensityPerCm,
                eyeShape = eye.name,
                eyeShapeArabic = eye.arabicName,
                eyeWidthMm = eyeWidth,
                eyeHeightMm = eyeHeight,
                eyeCanthalTiltDeg = eyeTilt,
                eyeSizeCategory = "متوسطة متناسقة",
                noseShape = nose.name,
                noseShapeArabic = nose.arabicName,
                noseLengthMm = noseLength,
                noseWidthMm = noseWidth,
                noseTipAngleDeg = noseAngle,
                noseVolumeCategory = "بارز قيادي",
                earMorphology = ear.name,
                earMorphologyArabic = ear.arabicName,
                earLengthMm = earLength,
                earWidthMm = earWidth,
                earLobeAttachment = if (ear == MorphoBiometricsEngine.EarMorphology.FREE_LOBE) "منفصلة حرة" else "متصلة بالخد",
                earCurvatureCategory = "منحنية متناسقة",
                personalityArchetype = dossier.titleArchetype,
                personalitySummary = dossier.integratedPersonalityVerdict,
                primaryTraitsJoined = profile.primaryTraits.joinToString(", "),
                leadershipScore = profile.leadershipPotential,
                creativityScore = profile.creativityIndex,
                empathyScore = profile.empathyScore,
                analyticalScore = profile.analyticalScore,
                communicationStyle = dossier.communicationProtocol,
                decisionMakingStyle = dossier.decisionMakingMatrix,
                recommendedCareersJoined = dossier.optimalCareerPaths.joinToString(", "),
                identityCode = dossier.identityCode
            )
            repository.saveFullBiometricProfile(entity)
        }
    }

    fun deleteFingerprintRecord(id: String) {
        viewModelScope.launch {
            repository.deleteFingerprint(id)
            _selectedForComparison.value = _selectedForComparison.value.filter { it.id != id }
        }
    }

    fun deleteFullProfileRecord(id: String) {
        viewModelScope.launch {
            repository.deleteProfile(id)
        }
    }

    fun loadDossierFromEntity(entity: BiometricProfileEntity) {
        val eye = try { MorphoBiometricsEngine.EyeShape.valueOf(entity.eyeShape) } catch (_: Exception) { MorphoBiometricsEngine.EyeShape.ALMOND }
        val nose = try { MorphoBiometricsEngine.NoseShape.valueOf(entity.noseShape) } catch (_: Exception) { MorphoBiometricsEngine.NoseShape.ROMAN_AQUILINE }
        val ear = try { MorphoBiometricsEngine.EarMorphology.valueOf(entity.earMorphology) } catch (_: Exception) { MorphoBiometricsEngine.EarMorphology.FREE_LOBE }
        val careers = entity.recommendedCareersJoined.split(", ")

        _selectedEye.value = eye
        _selectedNose.value = nose
        _selectedEar.value = ear

        _completeDossier.value = MorphoBiometricsEngine.CompleteIdentityDossier(
            identityCode = entity.identityCode,
            subjectName = entity.personName,
            titleArchetype = entity.personalityArchetype,
            fingerprintSummary = "نمط [${entity.fingerprintPatternArabic}] | TRC: ${entity.fingerprintRidgeCountTRC} خطاً | ${entity.fingerprintMinutiaeCount} علامة غالتونية",
            eyeProfile = eye,
            noseProfile = nose,
            earProfile = ear,
            integratedPersonalityVerdict = entity.personalitySummary,
            communicationProtocol = entity.communicationStyle,
            decisionMakingMatrix = entity.decisionMakingStyle,
            stressResponseStyle = "إدارة الضغوط: ${if (ear == MorphoBiometricsEngine.EarMorphology.FREE_LOBE) "يحتاج لمساحة استقلالية وتأمل لاستعادة الطاقة" else "يعتمد على الثوابت والروتين المنظم"}",
            optimalCareerPaths = careers,
            corePhilosophyQuote = "«قوة الشخصية تنبع من الانسجام بين وضوح البصيرة وثبات الإرادة وحكمة الإصغاء»"
        )
    }

    fun toggleSelectionForComparison(record: FingerprintRecordEntity) {
        val currentList = _selectedForComparison.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            currentList.removeAt(index)
        } else {
            if (currentList.size >= 2) {
                currentList.removeAt(0)
            }
            currentList.add(record)
        }
        _selectedForComparison.value = currentList

        if (currentList.size == 2) {
            compareRecords(currentList[0], currentList[1])
        }
    }

    private fun compareRecords(a: FingerprintRecordEntity, b: FingerprintRecordEntity) {
        viewModelScope.launch(Dispatchers.Default) {
            val patternA = FingerprintPatternType.valueOf(a.patternType)
            val patternB = FingerprintPatternType.valueOf(b.patternType)

            val bmpA = ArchetypeGenerator.generateArchetypeBitmap(patternA)
            val bmpB = ArchetypeGenerator.generateArchetypeBitmap(patternB)

            val (resA, _) = FingerprintProcessor.analyzeFingerprint(bmpA, "${a.personName} (${a.fingerName})", a.personName)
            val (resB, _) = FingerprintProcessor.analyzeFingerprint(bmpB, "${b.personName} (${b.fingerName})", b.personName)

            val comparison = DermatoglyphicsEngine.compareProfiles(resA, resB)
            _comparisonResult.value = comparison
        }
    }

    fun dismissComparisonDialog() {
        _comparisonResult.value = null
    }
}