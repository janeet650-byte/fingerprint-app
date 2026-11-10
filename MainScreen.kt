package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BiometricProfileEntity
import com.example.data.local.FingerprintRecordEntity
import com.example.model.FingerprintPatternType
import com.example.ui.components.BiometricDossierCard
import com.example.ui.components.BiometricIntakeWizard
import com.example.ui.components.FingerprintCanvas
import com.example.ui.components.FingerprintCompareDialog
import com.example.ui.components.PersonalityCard
import com.example.ui.components.RidgeWaveformChart
import com.example.ui.components.SaveFingerprintDialog
import com.example.ui.theme.BioAmber
import com.example.ui.theme.BioBgDark
import com.example.ui.theme.BioBorder
import com.example.ui.theme.BioCyan
import com.example.ui.theme.BioEmerald
import com.example.ui.theme.BioPurple
import com.example.ui.theme.BioSurfaceDark
import com.example.ui.theme.BioSurfaceVariant
import com.example.ui.theme.BioTextMuted
import com.example.ui.theme.BioTextPrimary
import com.example.ui.theme.BioTextSecondary
import com.example.viewmodel.FingerprintViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(viewModel: FingerprintViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val analysisResult by viewModel.analysisResult.collectAsState()
    val processedBitmaps by viewModel.processedBitmaps.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val layerState by viewModel.layerState.collectAsState()
    val selectedMinutiae by viewModel.selectedMinutiae.collectAsState()
    val personalityProfile by viewModel.personalityProfile.collectAsState()

    val completeDossier by viewModel.completeDossier.collectAsState()
    val savedFingerprints by viewModel.savedFingerprints.collectAsState()
    val savedFullProfiles by viewModel.savedFullProfiles.collectAsState()
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()
    val comparisonResult by viewModel.comparisonResult.collectAsState()
    val selectedForComparison by viewModel.selectedForComparison.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x2200E5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = BioCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "محلل البصمات والقياسات الحيوية",
                                color = BioTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(BioEmerald)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "قاعدة بيانات بيومترية نشطة • هوية شخصية كاملة",
                                    color = BioTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (analysisResult != null) {
                        IconButton(
                            onClick = { viewModel.openSaveDialog() },
                            modifier = Modifier.testTag("top_bar_save_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "حفظ البصمة",
                                tint = BioCyan
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BioBgDark,
                    titleContentColor = BioTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BioSurfaceDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    label = { Text("الفحص الشامل", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BioCyan,
                        selectedTextColor = BioCyan,
                        unselectedIconColor = BioTextMuted,
                        unselectedTextColor = BioTextMuted,
                        indicatorColor = Color(0x2200E5FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_wizard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                    label = { Text("فاحص الخطوط", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BioCyan,
                        selectedTextColor = BioCyan,
                        unselectedIconColor = BioTextMuted,
                        unselectedTextColor = BioTextMuted,
                        indicatorColor = Color(0x2200E5FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_scanner")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    label = { Text("تقرير الهوية", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BioPurple,
                        selectedTextColor = BioPurple,
                        unselectedIconColor = BioTextMuted,
                        unselectedTextColor = BioTextMuted,
                        indicatorColor = Color(0x22B388FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_dossier")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        BadgedBox(
                            badge = {
                                val totalRecords = savedFingerprints.size + savedFullProfiles.size
                                if (totalRecords > 0) {
                                    Badge(containerColor = BioCyan, contentColor = Color(0xFF090E17)) {
                                        Text("$totalRecords")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null)
                        }
                    },
                    label = { Text("قاعدة البيانات", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BioAmber,
                        selectedTextColor = BioAmber,
                        unselectedIconColor = BioTextMuted,
                        unselectedTextColor = BioTextMuted,
                        indicatorColor = Color(0x22FFD600)
                    ),
                    modifier = Modifier.testTag("nav_tab_database")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("الدليل العلمي", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BioCyan,
                        selectedTextColor = BioCyan,
                        unselectedIconColor = BioTextMuted,
                        unselectedTextColor = BioTextMuted,
                        indicatorColor = Color(0x2200E5FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_guide")
                )
            }
        },
        containerColor = BioBgDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // المرحلة الأولى: معالج الفحص البيومتري الشامل (بصمة + عين + أنف + أذن)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            BiometricIntakeWizard(
                                analysisResult = analysisResult,
                                onScanFingerprint = { bmp, src ->
                                    viewModel.analyzeScannedBitmap(bmp, src)
                                },
                                onSaveToDatabase = { name, eye, ew, eh, et, nose, nl, nw, na, ear, el, ew2 ->
                                    viewModel.saveFullIntakeProfile(
                                        personName = name,
                                        eye = eye, eyeWidth = ew, eyeHeight = eh, eyeTilt = et,
                                        nose = nose, noseLength = nl, noseWidth = nw, noseAngle = na,
                                        ear = ear, earLength = el, earWidth = ew2
                                    )
                                    selectedTab = 2 // الانتقال لتقرير الهوية فور الحفظ
                                }
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
                1 -> {
                    // فاحص الخطوط التفاعلي ومسطرة القياس وتموجات الكثافة
                    ScannerAndRidgesTab(
                        viewModel = viewModel,
                        analysisResult = analysisResult,
                        processedBitmaps = processedBitmaps,
                        isAnalyzing = isAnalyzing,
                        layerState = layerState,
                        selectedMinutiae = selectedMinutiae,
                        personalityProfile = personalityProfile,
                        onNavigateToDossier = { selectedTab = 2 }
                    )
                }
                2 -> {
                    // تقرير الهوية البيومترية الموثقة
                    DossierTab(
                        completeDossier = completeDossier,
                        onNavigateToWizard = { selectedTab = 0 }
                    )
                }
                3 -> {
                    // قاعدة البيانات وسجل المقارنة والتوافق
                    DatabaseHistoryTab(
                        fullProfiles = savedFullProfiles,
                        savedFingerprints = savedFingerprints,
                        selectedForComparison = selectedForComparison,
                        onToggleSelect = { viewModel.toggleSelectionForComparison(it) },
                        onDeleteFingerprint = { viewModel.deleteFingerprintRecord(it) },
                        onDeleteFullProfile = { viewModel.deleteFullProfileRecord(it) },
                        onSelectFullProfile = { profile ->
                            viewModel.loadDossierFromEntity(profile)
                            selectedTab = 2
                        },
                        onNavigateToWizard = { selectedTab = 0 }
                    )
                }
                4 -> {
                    // الدليل العلمي للأشكال والطبقات
                    GuideAndGlossaryTab(
                        onSelectArchetype = { pattern ->
                            viewModel.loadArchetype(pattern)
                            selectedTab = 1
                        }
                    )
                }
            }

            if (showSaveDialog && analysisResult != null) {
                SaveFingerprintDialog(
                    result = analysisResult!!,
                    onDismiss = { viewModel.closeSaveDialog() },
                    onSave = { person, finger, hand ->
                        viewModel.saveCurrentFingerprint(person, finger, hand)
                    }
                )
            }

            if (comparisonResult != null) {
                FingerprintCompareDialog(
                    comparison = comparisonResult!!,
                    onDismiss = { viewModel.dismissComparisonDialog() }
                )
            }
        }
    }
}