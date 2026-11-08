package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.local.ProductModelEntity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ui.screens.cosmic.CosmicPlanetDef
import com.example.ui.screens.cosmic.CosmicSolarSystemScreen
import com.example.ui.screens.cosmic.CosmicProductsPlanetScreen
import com.example.ui.screens.cosmic.CosmicPlanetaryTransitionOverlay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.InvoiceWithItems
import com.example.data.local.SaleRecordEntity
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.components.BasheerFloatingButton
import com.example.ui.components.BasheerPulsingEnergyOrb
import com.example.ui.components.BasheerVoiceAssistantDialog
import com.example.ui.components.FutureGateAuraBackground
import com.example.ui.components.InvoicePreviewDialog
import com.example.ui.components.NoPrinterAlertDialog
import com.example.ui.components.PostSaleReceiptDialog
import com.example.ui.components.SaleDialog
import com.example.ui.components.StoreTopAppBar
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.CreateInvoiceScreen
import com.example.ui.screens.DamageRepairScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RecycleBinScreen
import com.example.ui.screens.ReturnsScreen
import com.example.ui.screens.ReturnReceiptPreviewDialog
import com.example.ui.screens.SalesLogScreen
import com.example.ui.screens.SettingsScreen
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Warning
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDarkBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.NavySurfaceVariant
import com.example.voice.SpeechRecognizerHelper
import com.example.voice.TtsHelper
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AlBashiriApp()
                }
            }
        }
    }
}

@Composable
fun AlBashiriApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    val categories by viewModel.allCategories.collectAsState()
    val brands by viewModel.allBrands.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val models by viewModel.allModels.collectAsState()
    val sales by viewModel.allSales.collectAsState()
    val suppliers by viewModel.allSuppliers.collectAsState()
    val merchants by viewModel.allMerchants.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val recycleBinItems by viewModel.allRecycleBinItems.collectAsState()
    val recycleBinCount by viewModel.recycleBinCount.collectAsState()
    var previousTab by remember { mutableStateOf(MainTab.CATALOG) }

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedBrand by viewModel.selectedBrand.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val catalogSearchQuery by viewModel.catalogSearchQuery.collectAsState()

    val inventorySearchQuery by viewModel.inventorySearchQuery.collectAsState()
    val inventoryCategoryFilter by viewModel.inventoryCategoryFilter.collectAsState()
    val inventorySortOption by viewModel.inventorySortOption.collectAsState()

    val saleTargetModel by viewModel.saleTargetModel.collectAsState()

    // Bluetooth Thermal Printer state
    val savedPrinter by viewModel.savedPrinter.collectAsState()
    val storeProfile by viewModel.storeProfile.collectAsState()
    val storeName by viewModel.storeName.collectAsState()
    val isPrinting by viewModel.isPrinting.collectAsState()
    val lastCompletedSale by viewModel.lastCompletedSale.collectAsState()
    val selectedInvoiceForPreview by viewModel.selectedInvoiceForPreview.collectAsState()

    // Multi-Item Invoice states
    val allInvoicesWithItems by viewModel.allInvoicesWithItems.collectAsState()
    val showCreateInvoiceScreen by viewModel.showCreateInvoiceScreen.collectAsState()
    val lastCompletedInvoice by viewModel.lastCompletedInvoice.collectAsState()
    val selectedInvoiceWithItemsForPreview by viewModel.selectedInvoiceWithItemsForPreview.collectAsState()

    var showNoPrinterDialog by remember { mutableStateOf(false) }

    fun triggerPrintReceipt(sale: SaleRecordEntity) {
        if (savedPrinter == null) {
            showNoPrinterDialog = true
        } else {
            viewModel.printReceipt(sale) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                if (success) {
                    viewModel.closePostSaleDialog()
                }
            }
        }
    }

    fun triggerPrintInvoiceWithItems(invoice: InvoiceWithItems) {
        if (savedPrinter == null) {
            showNoPrinterDialog = true
        } else {
            viewModel.printInvoiceWithItems(invoice) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Returns Management states
    val allReturnsWithItems by viewModel.allReturnsWithItems.collectAsState()
    val selectedReturnForPreview by viewModel.selectedReturnForPreview.collectAsState()
    val returnsSearchQuery by viewModel.returnsSearchQuery.collectAsState()
    val returnsTypeFilter by viewModel.returnsTypeFilter.collectAsState()

    fun triggerPrintReturn(returnWithItems: com.example.data.local.ReturnWithItems) {
        if (savedPrinter == null) {
            showNoPrinterDialog = true
        } else {
            viewModel.printReturnReceipt(returnWithItems) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun exportReturnsCsv() {
        val csv = viewModel.exportReturnsCsv()
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "سجل المرتجعات - البشيري للأجهزة الكهربائية")
            putExtra(android.content.Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "مشاركة تقرير المرتجعات"))
    }

    // Damage & Repair states
    val damageRepairRecords by viewModel.allDamageRepairRecords.collectAsState()
    val damageSearchQuery by viewModel.damageSearchQuery.collectAsState()
    val damageStatusFilter by viewModel.damageStatusFilter.collectAsState()

    fun exportDamageRepairCsv() {
        val csv = viewModel.exportDamageRepairCsv()
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "سجل الهالك والتالف والإصلاح - البشيري للأجهزة الكهربائية")
            putExtra(android.content.Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "مشاركة تقرير الهالك والتالف والإصلاح"))
    }

    // Stock Movements & Inventory Audit states
    val allStockMovements by viewModel.allStockMovements.collectAsState()
    val allInventoryAudits by viewModel.allInventoryAudits.collectAsState()
    val movementSearchQuery by viewModel.movementSearchQuery.collectAsState()
    val movementTypeFilter by viewModel.movementTypeFilter.collectAsState()

    fun exportStockMovementsCsv() {
        val csv = viewModel.exportStockMovementsCsv()
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "سجل حركة المخزون - البشيري للأجهزة الكهربائية")
            putExtra(android.content.Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "مشاركة سجل حركة المخزون"))
    }

    fun exportAuditCsv(auditWithItems: com.example.data.local.InventoryAuditWithItems) {
        val csv = viewModel.exportAuditVarianceCsv(auditWithItems)
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "تقرير جرد وتسوية مخزون (${auditWithItems.audit.auditNumber}) - البشيري للأجهزة الكهربائية")
            putExtra(android.content.Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "مشاركة تقرير الجرد والتسوية"))
    }

    fun exportStockValuationCsv() {
        val csv = viewModel.exportCurrentStockValuationCsv()
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "تقرير تقييم المخزون الحالي - البشيري للأجهزة الكهربائية")
            putExtra(android.content.Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "مشاركة تقرير تقييم المخزون"))
    }

    // Financial Management states
    val allTreasuryTransactions by viewModel.allTreasuryTransactions.collectAsState()
    val allExpenseCategories by viewModel.allExpenseCategories.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allFinancialAdjustments by viewModel.allFinancialAdjustments.collectAsState()
    val financialSummary by viewModel.financialSummary.collectAsState()

    fun shareCsv(title: String, csvContent: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, title)
            putExtra(android.content.Intent.EXTRA_TEXT, csvContent)
        }
        context.startActivity(android.content.Intent.createChooser(intent, title))
    }

    // Voice Assistant "Basheer" state
    val speechHelper = remember { SpeechRecognizerHelper(context) }
    var showBasheerDialog by remember { mutableStateOf(false) }
    val isListening by speechHelper.isListening.collectAsState()
    val spokenText by speechHelper.spokenText.collectAsState()
    val rmsLevel by speechHelper.rmsLevel.collectAsState()
    val voiceErrorMessage by speechHelper.errorMessage.collectAsState()

    // Native Text-to-Speech (TTS) for login greeting
    val ttsHelper = remember { TtsHelper(context) }
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }
    val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()
    val isTtsOperationsEnabled by viewModel.isTtsOperationsEnabled.collectAsState()

    // Short audio welcome greeting on successful login
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null && isTtsEnabled) {
            ttsHelper.speakGreeting(user.displayName, user.customGreeting)
        }
    }

    val systemSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenMatches?.firstOrNull()?.trim()
            if (!recognizedText.isNullOrBlank()) {
                speechHelper.setRecognizedText(recognizedText)
                showBasheerDialog = true
            } else {
                Toast.makeText(context, "لم يتم التقاط أي كلمات، يرجى إعادة المحاولة", Toast.LENGTH_SHORT).show()
                showBasheerDialog = true
            }
        } else {
            // User finished or cancelled system dialog, keep Basheer dialog open
            showBasheerDialog = true
        }
    }

    val launchSystemSpeechIntent: () -> Unit = {
        ttsHelper.stop()
        speechHelper.stopListening()
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-EG")
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar-EG", "ar-SA", "ar"))
                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث إلى بشير (اسم الجهاز، السعر، أو ابحث)...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            }
            systemSpeechLauncher.launch(intent)
        } catch (e: Exception) {
            showBasheerDialog = true
            Toast.makeText(context, "تعذر تشغيل خدمة الصوت بالنظام", Toast.LENGTH_SHORT).show()
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            ttsHelper.stop()
            launchSystemSpeechIntent()
        } else {
            showBasheerDialog = true
            Toast.makeText(context, "إذن الميكروفون مطلوب للاستماع الصوتي، يمكنك أيضاً كتابة الأمر مباشرة", Toast.LENGTH_LONG).show()
        }
    }

    // Listen for toast messages
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (currentUser == null) {
        LoginScreen(
            onLoginSubmit = { userId, pin ->
                viewModel.login(
                    userId = userId,
                    pin = pin,
                    onSuccess = { loginErrorMessage = null },
                    onError = { err -> loginErrorMessage = err }
                )
            },
            errorMessage = loginErrorMessage
        )
    } else {
        val user = currentUser!!
        val isAdmin = user.role == "ADMIN"

        val lowStockCount = remember(models) { models.count { it.stockQuantity <= 2 } }
        val lowStockModels = remember(models) { models.filter { it.stockQuantity <= 2 } }
        val totalSalesSum = remember(allInvoicesWithItems, sales) {
            if (allInvoicesWithItems.isNotEmpty()) {
                allInvoicesWithItems.sumOf { it.invoice.finalTotal }
            } else {
                sales.sumOf { it.totalPrice }
            }
        }
        val totalInvoicesCount = remember(allInvoicesWithItems, sales) {
            if (allInvoicesWithItems.isNotEmpty()) allInvoicesWithItems.size else sales.size
        }
        var showNotificationsDialog by remember { mutableStateOf(false) }

        var isInsideProductsPlanet by rememberSaveable { mutableStateOf(false) }
        var activeTransitionPlanet by remember { mutableStateOf<CosmicPlanetDef?>(null) }
        var isTransitioningIn by remember { mutableStateOf(false) }
        var isTransitioningOut by remember { mutableStateOf(false) }

        val isSolarSystemActive = currentTab == MainTab.CATALOG && !isInsideProductsPlanet && !isTransitioningIn && !isTransitioningOut

        if (!isSolarSystemActive) {
            BackHandler {
                isTransitioningOut = true
            }
        }

        Scaffold(
            topBar = {
                val shouldShowTopBar = !isSolarSystemActive && !isTransitioningIn && !isTransitioningOut
                if (shouldShowTopBar) {
                    StoreTopAppBar(
                        currentUser = user,
                        recycleBinCount = recycleBinCount,
                        notificationsCount = lowStockCount,
                        onNotificationsClick = { showNotificationsDialog = true },
                        onRecycleBinClick = {
                            previousTab = currentTab
                            viewModel.selectTab(MainTab.RECYCLE_BIN)
                        },
                        onSettingsClick = {
                            viewModel.selectTab(MainTab.SETTINGS)
                        },
                        onLogoutClick = { viewModel.logout() }
                    )
                }
            },
            bottomBar = {
                val shouldShowBottomBar = !isSolarSystemActive && !isTransitioningIn && !isTransitioningOut
                if (shouldShowBottomBar) {
                    AlBashiriBottomNavBar(
                        currentTab = currentTab,
                        isAdmin = isAdmin,
                        onTabSelected = { targetTab ->
                            if (targetTab == MainTab.CATALOG) {
                                isInsideProductsPlanet = false
                            }
                            viewModel.selectTab(targetTab)
                        },
                        onBasheerClick = {
                            ttsHelper.stop()
                            showBasheerDialog = true
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                launchSystemSpeechIntent()
                            } else {
                                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = CharcoalBackground
        ) { innerPadding ->
            val contentPadding = if (isSolarSystemActive || isTransitioningIn || isTransitioningOut) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                if (isTransitioningIn) {
                    CosmicPlanetaryTransitionOverlay(
                        isZoomingIn = true,
                        targetPlanet = activeTransitionPlanet,
                        onTransitionFinished = {
                            isTransitioningIn = false
                            val planet = activeTransitionPlanet
                            if (planet?.isProductsPlanet == true) {
                                isInsideProductsPlanet = true
                                viewModel.selectTab(MainTab.CATALOG)
                            } else if (planet?.targetTab != null) {
                                isInsideProductsPlanet = false
                                viewModel.selectTab(planet.targetTab)
                            }
                        }
                    )
                } else if (isTransitioningOut) {
                    CosmicPlanetaryTransitionOverlay(
                        isZoomingIn = false,
                        targetPlanet = activeTransitionPlanet,
                        onTransitionFinished = {
                            isTransitioningOut = false
                            isInsideProductsPlanet = false
                            activeTransitionPlanet = null
                            viewModel.selectTab(MainTab.CATALOG)
                        }
                    )
                } else {
                    when (currentTab) {
                        MainTab.CATALOG -> {
                            if (isInsideProductsPlanet) {
                                CosmicProductsPlanetScreen(
                                    currentUser = user,
                                    categories = categories,
                                    brands = brands,
                                    products = products,
                                    models = models,
                                    searchQuery = catalogSearchQuery,
                                    onSearchChange = { viewModel.setCatalogSearchQuery(it) },
                                    onOpenSale = { viewModel.openSaleDialog(it) },
                                    onAddCategory = { viewModel.addCategory(it) },
                                    onDeleteCategory = { viewModel.deleteCategory(it) },
                                    onAddBrand = { name, catId -> viewModel.addBrand(name, catId) },
                                    onDeleteBrand = { viewModel.deleteBrand(it) },
                                    onAddProduct = { name, catId, catName, bId, bName ->
                                        viewModel.addProduct(name, catId, catName, bId, bName)
                                        if (isTtsOperationsEnabled) ttsHelper.speakProductAdded()
                                    },
                                    onDeleteProduct = { viewModel.deleteProduct(it) },
                                    onAddModel = { pId, pName, bName, cName, mName, fPrice, mPrice, stock, code ->
                                        viewModel.addModel(pId, pName, bName, cName, mName, fPrice, mPrice, stock, code)
                                        if (isTtsOperationsEnabled) ttsHelper.speakProductAdded()
                                    },
                                    onUpdateModel = { viewModel.updateModel(it) },
                                    onDeleteModel = { viewModel.deleteModel(it) },
                                    onBackToSolarSystem = {
                                        isTransitioningOut = true
                                    }
                                )
                            } else {
                                CosmicSolarSystemScreen(
                                    onEnterPlanet = { planet ->
                                        activeTransitionPlanet = planet
                                        isTransitioningIn = true
                                    },
                                    onBasheerClick = {
                                        ttsHelper.stop()
                                        showBasheerDialog = true
                                        val hasPerm = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPerm) {
                                            launchSystemSpeechIntent()
                                        } else {
                                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                )
                            }
                        }

                    MainTab.INVENTORY -> {
                        InventoryScreen(
                            currentUser = user,
                            models = models,
                            categories = categories,
                            searchQuery = inventorySearchQuery,
                            categoryFilter = inventoryCategoryFilter,
                            sortOption = inventorySortOption,
                            onSearchChange = { viewModel.setInventorySearchQuery(it) },
                            onCategoryFilterChange = { viewModel.setInventoryCategoryFilter(it) },
                            onSortOptionChange = { viewModel.setInventorySortOption(it) },
                            onOpenSale = { viewModel.openSaleDialog(it) },
                            onUpdateStock = { id, stock -> viewModel.updateStock(id, stock) },
                            movements = allStockMovements,
                            audits = allInventoryAudits,
                            suppliers = suppliers,
                            merchants = merchants,
                            movementSearchQuery = movementSearchQuery,
                            movementTypeFilter = movementTypeFilter,
                            onMovementSearchChange = { viewModel.setMovementSearchQuery(it) },
                            onMovementFilterChange = { viewModel.setMovementTypeFilter(it) },
                            onReceiveStock = { modelId, qty, cost, sId, mId, sName, invNo, notes, onSucc, onErr ->
                                viewModel.receiveStock(modelId, qty, cost, sId, mId, sName, invNo, notes, onSucc, onErr)
                            },
                            onCreateAudit = { title, scope, target, notes, onSucc, onErr ->
                                viewModel.createInventoryAudit(title, scope, target, notes, onSucc, onErr)
                            },
                            onUpdateAuditItemActualQuantity = { itemId, actualQty ->
                                viewModel.updateAuditItemActualQuantity(itemId, actualQty)
                            },
                            onSettleAudit = { auditId, reason, onSucc, onErr ->
                                viewModel.settleInventoryAudit(auditId, reason, onSucc, onErr)
                            },
                            onCancelAudit = { auditId, onSucc, onErr ->
                                viewModel.cancelInventoryAudit(auditId, onSucc, onErr)
                            },
                            onExportMovementsCsv = { exportStockMovementsCsv() },
                            onExportAuditCsv = { exportAuditCsv(it) },
                            onExportValuationCsv = { exportStockValuationCsv() }
                        )
                    }

                    MainTab.SALES_LOG -> {
                        SalesLogScreen(
                            currentUser = user,
                            invoicesWithItems = allInvoicesWithItems,
                            legacySales = sales,
                            onOpenCreateInvoice = { viewModel.openCreateInvoiceScreen() },
                            onOpenInvoiceWithItemsPreview = { viewModel.openInvoiceWithItemsPreview(it) },
                            onPrintInvoiceWithItems = { triggerPrintInvoiceWithItems(it) },
                            onOpenInvoicePreview = { viewModel.openInvoicePreview(it) },
                            onPrintSaleReceipt = { triggerPrintReceipt(it) }
                        )
                    }

                    MainTab.RETURNS -> {
                        ReturnsScreen(
                            currentUser = user,
                            returnsList = allReturnsWithItems,
                            invoicesList = allInvoicesWithItems,
                            catalogModels = models,
                            customers = customers,
                            suppliers = suppliers,
                            merchants = merchants,
                            storeProfile = storeProfile,
                            searchQuery = returnsSearchQuery,
                            typeFilter = returnsTypeFilter,
                            onSearchChange = { viewModel.setReturnsSearchQuery(it) },
                            onFilterChange = { viewModel.setReturnsTypeFilter(it) },
                            onOpenCreateReturn = { },
                            onPreviewReturn = { viewModel.openReturnPreview(it) },
                            onPrintReturn = { triggerPrintReturn(it) },
                            onCancelReturn = { returnId ->
                                viewModel.cancelReturn(
                                    returnId = returnId,
                                    onSuccess = {},
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                                )
                            },
                            onExportCsv = { exportReturnsCsv() },
                            onFetchReturnableInvoiceItems = { invId -> viewModel.getReturnableInvoiceItems(invId) },
                            onSubmitCreateReturn = { type, partyName, partyPhone, partyId, origInvNum, origInvId, items, reason, setType, setAmt, setNotes, notes, onOk, onErr ->
                                viewModel.createReturn(
                                    type = type,
                                    partyName = partyName,
                                    partyPhone = partyPhone,
                                    partyId = partyId,
                                    originalInvoiceNumber = origInvNum,
                                    originalInvoiceId = origInvId,
                                    items = items,
                                    reason = reason,
                                    settlementType = setType,
                                    settlementAmount = setAmt,
                                    settlementNotes = setNotes,
                                    notes = notes,
                                    onSuccess = onOk,
                                    onError = onErr
                                )
                            }
                        )
                    }

                    MainTab.DAMAGE_REPAIR -> {
                        DamageRepairScreen(
                            currentUser = user,
                            records = damageRepairRecords,
                            catalogModels = models,
                            returnsWithItems = allReturnsWithItems,
                            searchQuery = damageSearchQuery,
                            statusFilter = damageStatusFilter,
                            onSearchChange = { viewModel.setDamageSearchQuery(it) },
                            onFilterChange = { viewModel.setDamageStatusFilter(it) },
                            onRegisterDamageOrScrap = { modelId, qty, reason, details, status, srcType, retId, retNum, cost, loss, img, notes, onOk, onErr ->
                                viewModel.registerDamageOrScrap(
                                    modelId = modelId,
                                    quantity = qty,
                                    reason = reason,
                                    damageDetails = details,
                                    initialStatus = status,
                                    sourceType = srcType,
                                    returnRecordId = retId,
                                    returnNumber = retNum,
                                    unitCost = cost,
                                    totalLossAmount = loss,
                                    imageUri = img,
                                    notes = notes,
                                    onSuccess = onOk,
                                    onError = onErr
                                )
                            },
                            onSendToRepair = { recordId, reason, notes, onOk, onErr ->
                                viewModel.sendToRepair(
                                    recordId = recordId,
                                    repairReason = reason,
                                    technicianNotes = notes,
                                    onSuccess = onOk,
                                    onError = onErr
                                )
                            },
                            onCompleteRepair = { recordId, outcome, spareParts, labor, notes, onOk, onErr ->
                                viewModel.completeRepair(
                                    recordId = recordId,
                                    outcome = outcome,
                                    sparePartsCost = spareParts,
                                    laborCost = labor,
                                    repairNotes = notes,
                                    onSuccess = onOk,
                                    onError = onErr
                                )
                            },
                            onApproveReturnToStock = { recordId, onOk, onErr ->
                                viewModel.approveReturnToStock(
                                    recordId = recordId,
                                    onSuccess = onOk,
                                    onError = onErr
                                )
                            },
                            onExportCsv = { exportDamageRepairCsv() }
                        )
                    }

                    MainTab.ACCOUNTS -> {
                        if (isAdmin) {
                            AccountsScreen(
                                currentUser = user,
                                suppliers = suppliers,
                                merchants = merchants,
                                customers = customers,
                                treasuryTransactions = allTreasuryTransactions,
                                expenseCategories = allExpenseCategories,
                                expenses = allExpenses,
                                financialAdjustments = allFinancialAdjustments,
                                financialSummary = financialSummary,
                                onAddSupplier = { name, phone, purchases, notes ->
                                    viewModel.addSupplier(name, phone, purchases, notes)
                                },
                                onAddSupplierTransaction = { sId, type, amount, notes ->
                                    viewModel.addSupplierTransaction(sId, type, amount, notes)
                                },
                                onUpdateSupplierTransaction = { id, type, amount, notes ->
                                    viewModel.updateSupplierTransaction(id, type, amount, notes)
                                },
                                onDeleteSupplierTransaction = { id ->
                                    viewModel.deleteSupplierTransaction(id)
                                },
                                onDeleteSupplier = { id ->
                                    viewModel.deleteSupplier(id)
                                },
                                onAddMerchant = { name, phone, delivered, notes ->
                                    viewModel.addMerchant(name, phone, delivered, notes)
                                },
                                onAddMerchantTransaction = { mId, type, amount, notes ->
                                    viewModel.addMerchantTransaction(mId, type, amount, notes)
                                },
                                onUpdateMerchantTransaction = { id, type, amount, notes ->
                                    viewModel.updateMerchantTransaction(id, type, amount, notes)
                                },
                                onDeleteMerchantTransaction = { id ->
                                    viewModel.deleteMerchantTransaction(id)
                                },
                                onDeleteMerchant = { id ->
                                    viewModel.deleteMerchant(id)
                                },
                                onAddCustomer = { name, phone, creditSales, notes ->
                                    viewModel.addCustomer(name, phone, creditSales, notes)
                                },
                                onAddCustomerTransaction = { cId, type, amount, productDesc, notes ->
                                    viewModel.addCustomerTransaction(cId, type, amount, productDesc, notes)
                                },
                                onUpdateCustomerTransaction = { id, type, amount, productDesc, notes ->
                                    viewModel.updateCustomerTransaction(id, type, amount, productDesc, notes)
                                },
                                onDeleteCustomerTransaction = { id ->
                                    viewModel.deleteCustomerTransaction(id)
                                },
                                onDeleteCustomer = { id ->
                                    viewModel.deleteCustomer(id)
                                },
                                onRecordTreasuryTransaction = { type, category, amount, refType, partyName, paymentMethod, desc ->
                                    viewModel.recordTreasuryTransaction(type, category, amount, refType, partyName, paymentMethod, desc)
                                },
                                onReverseTreasuryTransaction = { txId, reason ->
                                    viewModel.reverseTreasuryTransaction(txId, reason)
                                },
                                onRecordExpense = { catName, amount, desc, beneficiary, payMethod, refNum, notes ->
                                    viewModel.recordExpense(catName, amount, desc, beneficiary, payMethod, refNum, notes)
                                },
                                onReverseExpense = { expId, reason ->
                                    viewModel.reverseExpense(expId, reason)
                                },
                                onAddExpenseCategory = { name, desc ->
                                    viewModel.addExpenseCategory(name, desc)
                                },
                                onDeleteExpenseCategory = { id ->
                                    viewModel.deleteExpenseCategory(id)
                                },
                                onRecordFinancialAdjustment = { targetType, targetId, targetName, adjType, amount, reason ->
                                    viewModel.recordFinancialAdjustment(targetType, targetId, targetName, adjType, amount, reason)
                                },
                                onRefreshFinancialSummary = { start, end ->
                                    viewModel.refreshFinancialSummary(start, end)
                                },
                                onExportTreasuryLedger = { viewModel.exportTreasuryLedgerCsv() },
                                onExportExpenses = { viewModel.exportExpensesCsv() },
                                onExportFinancialReport = { viewModel.exportFinancialReportCsv() },
                                onShareCsv = { title, csv -> shareCsv(title, csv) },
                                getSupplierTransactionsFlow = { sId ->
                                    viewModel.getSupplierTransactions(sId)
                                },
                                getMerchantTransactionsFlow = { mId ->
                                    viewModel.getMerchantTransactions(mId)
                                },
                                getCustomerTransactionsFlow = { cId ->
                                    viewModel.getCustomerTransactions(cId)
                                }
                            )
                        } else {
                            viewModel.selectTab(MainTab.CATALOG)
                        }
                    }

                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            currentUser = user,
                            allUsers = allUsers,
                            isTtsEnabled = isTtsEnabled,
                            onToggleTts = { viewModel.setTtsEnabled(it) },
                            isTtsOperationsEnabled = isTtsOperationsEnabled,
                            onToggleTtsOperations = { viewModel.setTtsOperationsEnabled(it) },
                            onUpdateCustomGreeting = { userId, greeting ->
                                viewModel.updateCustomGreeting(userId, greeting)
                            },
                            onPreviewGreeting = { greeting, displayName ->
                                ttsHelper.previewGreeting(greeting, displayName)
                            },
                            onPasswordChange = { cur, new, conf, onOk, onErr ->
                                viewModel.changePassword(cur, new, conf, onOk, onErr)
                            },
                            onGenerateBackupCsv = { viewModel.generateBackupCsv() },
                            savedPrinter = savedPrinter,
                            storeName = storeName,
                            isPrinting = isPrinting,
                            onSelectPrinter = { viewModel.setSavedPrinter(it) },
                            onClearPrinter = { viewModel.clearSavedPrinter() },
                            onUpdateStoreName = { viewModel.updateStoreName(it) },
                            onPrintTestReceipt = { callback ->
                                viewModel.printTestReceipt(callback)
                            },
                            storeProfile = storeProfile,
                            onUpdateStoreProfile = { viewModel.updateStoreProfile(it) },
                            onUploadLogo = { viewModel.saveStoreLogo(it) },
                            onResetLogo = { viewModel.resetStoreLogo() },
                            recycleBinCount = recycleBinCount,
                            onNavigateToRecycleBin = {
                                previousTab = currentTab
                                viewModel.selectTab(MainTab.RECYCLE_BIN)
                            },
                            onLogout = { viewModel.logout() }
                        )
                    }

                    MainTab.RECYCLE_BIN -> {
                        BackHandler {
                            viewModel.selectTab(previousTab)
                        }
                        RecycleBinScreen(
                            currentUser = user,
                            recycleBinItems = recycleBinItems,
                            onNavigateBack = { viewModel.selectTab(previousTab) },
                            onRestoreItem = { viewModel.restoreRecycleBinItem(it) },
                            onRestoreAll = { viewModel.restoreAllRecycleBinItems() },
                            onPermanentDeleteItem = { viewModel.permanentDeleteRecycleBinItem(it) },
                            onEmptyBin = { viewModel.emptyRecycleBin() }
                        )
                    }
                }
                }


                // Basheer Voice Assistant Modal Dialog
                if (showBasheerDialog) {
                    BasheerVoiceAssistantDialog(
                        isListening = isListening,
                        spokenText = spokenText,
                        rmsLevel = rmsLevel,
                        errorMessage = voiceErrorMessage,
                        categories = categories,
                        brands = brands,
                        models = models,
                        isAdmin = isAdmin,
                        treasuryCashBalance = financialSummary.treasuryCashBalance,
                        onStartListening = launchSystemSpeechIntent,
                        onStopListening = { speechHelper.stopListening() },
                        onLaunchSystemSpeechIntent = launchSystemSpeechIntent,
                        onDismiss = {
                            speechHelper.stopListening()
                            showBasheerDialog = false
                        },
                        onConfirmAddProduct = { cat, brand, prod, model, finalP, minP, stock ->
                            viewModel.addVoiceProduct(cat, brand, prod, model, finalP, minP, stock)
                            if (isTtsOperationsEnabled) ttsHelper.speakProductAdded()
                        },
                        onOpenSale = { viewModel.openSaleDialog(it) },
                        onNavigateToTab = { viewModel.selectTab(it) },
                        onDeleteModel = { modelId -> viewModel.deleteModel(modelId) },
                        onUpdatePrice = { modelId, newPrice -> viewModel.updateModelPrice(modelId, newPrice) },
                        onRecordExpense = { cat, amt, desc ->
                            viewModel.recordExpense(cat, amt, desc, beneficiary = "", paymentMethod = "CASH_TREASURY")
                        },
                        onSupplierPayment = { supplierName, amt ->
                            viewModel.paySupplierVoice(supplierName, amt)
                        },
                        onCustomerCollection = { customerName, amt ->
                            viewModel.collectCustomerVoice(customerName, amt)
                        },
                        onSpeakFeedback = { text ->
                            if (isTtsOperationsEnabled) ttsHelper.speak(text)
                        }
                    )
                }

                // Global Sale Dialog
                if (saleTargetModel != null) {
                    SaleDialog(
                        model = saleTargetModel!!,
                        sellerName = user.displayName,
                        onDismiss = { viewModel.closeSaleDialog() },
                        onConfirmSale = { soldPrice, qty, notes, custName, custPhone ->
                            val currentModel = saleTargetModel!!
                            val remainingStock = currentModel.stockQuantity - qty
                            viewModel.submitSale(
                                model = currentModel,
                                soldPrice = soldPrice,
                                quantity = qty,
                                notes = notes,
                                customerName = custName,
                                customerPhone = custPhone,
                                onSuccess = {
                                    Toast.makeText(context, "تم تسجيل البيع بنجاح", Toast.LENGTH_SHORT).show()
                                    if (isTtsOperationsEnabled) {
                                        if (remainingStock in 0..2) {
                                            ttsHelper.speakLowStockWarning(remainingStock, currentModel.modelName)
                                        } else {
                                            ttsHelper.speakSaleSuccess()
                                        }
                                    }
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    )
                }

                // Dedicated Multi-Item Invoice Creation Screen
                if (showCreateInvoiceScreen) {
                    CreateInvoiceScreen(
                        currentUser = user,
                        availableModels = models,
                        onClose = { viewModel.closeCreateInvoiceScreen() },
                        onSubmitInvoice = { custName, custPhone, items, discount, paid, notes, andPrintPreview ->
                            viewModel.submitMultiItemInvoice(
                                customerName = custName,
                                customerPhone = custPhone,
                                items = items,
                                discount = discount,
                                paidAmount = paid,
                                notes = notes,
                                onSuccess = { invoiceWithItems ->
                                    if (andPrintPreview) {
                                        viewModel.openInvoiceWithItemsPreview(invoiceWithItems)
                                    }
                                    if (isTtsOperationsEnabled) {
                                        ttsHelper.speakSaleSuccess()
                                    }
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    )
                }

                // Multi-Item Invoice Post-Creation Dialog
                if (lastCompletedInvoice != null) {
                    InvoicePreviewDialog(
                        invoiceWithItems = lastCompletedInvoice!!,
                        isPrinting = isPrinting,
                        onPrintReceipt = {
                            val inv = lastCompletedInvoice
                            if (inv != null) {
                                triggerPrintInvoiceWithItems(inv)
                            }
                        },
                        onDismiss = { viewModel.closePostInvoiceDialog() }
                    )
                }

                // Multi-Item Invoice Preview Dialog (from Sales Log or Preview action)
                if (selectedInvoiceWithItemsForPreview != null) {
                    InvoicePreviewDialog(
                        invoiceWithItems = selectedInvoiceWithItemsForPreview!!,
                        isPrinting = isPrinting,
                        onPrintReceipt = {
                            val inv = selectedInvoiceWithItemsForPreview
                            if (inv != null) {
                                triggerPrintInvoiceWithItems(inv)
                            }
                        },
                        onDismiss = { viewModel.closeInvoiceWithItemsPreview() }
                    )
                }

                // Post-Sale Official Invoice Dialog (Immediate live preview & print)
                if (lastCompletedSale != null) {
                    InvoicePreviewDialog(
                        sale = lastCompletedSale!!,
                        isPrinting = isPrinting,
                        onPrintReceipt = {
                            val sale = lastCompletedSale
                            if (sale != null) {
                                triggerPrintReceipt(sale)
                            }
                        },
                        onDismiss = { viewModel.closePostSaleDialog() }
                    )
                }

                // Historical Sales Log Invoice Dialog (Preview & Reprint)
                if (selectedInvoiceForPreview != null) {
                    InvoicePreviewDialog(
                        sale = selectedInvoiceForPreview!!,
                        isPrinting = isPrinting,
                        onPrintReceipt = {
                            val sale = selectedInvoiceForPreview
                            if (sale != null) {
                                triggerPrintReceipt(sale)
                            }
                        },
                        onDismiss = { viewModel.closeInvoicePreview() }
                    )
                }

                // Return Receipt Preview Dialog
                if (selectedReturnForPreview != null) {
                    ReturnReceiptPreviewDialog(
                        returnWithItems = selectedReturnForPreview!!,
                        storeProfile = storeProfile,
                        onDismiss = { viewModel.closeReturnPreview() },
                        onPrint = {
                            val ret = selectedReturnForPreview
                            if (ret != null) {
                                triggerPrintReturn(ret)
                            }
                        }
                    )
                }

                // Alert when user wants to print but no printer is configured yet
                if (showNoPrinterDialog) {
                    NoPrinterAlertDialog(
                        onNavigateToSettings = {
                            viewModel.selectTab(MainTab.SETTINGS)
                        },
                        onDismiss = { showNoPrinterDialog = false }
                    )
                }

                // Notifications Summary Modal Dialog
                if (showNotificationsDialog) {
                    NotificationsSummaryDialog(
                        lowStockCount = lowStockCount,
                        lowStockModels = lowStockModels,
                        recycleBinCount = recycleBinCount,
                        savedPrinterName = savedPrinter?.name,
                        onNavigateToInventory = { viewModel.selectTab(MainTab.INVENTORY) },
                        onNavigateToRecycleBin = { viewModel.selectTab(MainTab.RECYCLE_BIN) },
                        onNavigateToSettings = { viewModel.selectTab(MainTab.SETTINGS) },
                        onDismiss = { showNotificationsDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsSummaryDialog(
    lowStockCount: Int,
    lowStockModels: List<ProductModelEntity>,
    recycleBinCount: Int,
    savedPrinterName: String?,
    onNavigateToInventory: () -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مركز التنبيهات والإشعارات",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Alert 1: Low Stock
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (lowStockCount > 0) WarningAmber.copy(alpha = 0.6f) else GlassBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onNavigateToInventory()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (lowStockCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (lowStockCount > 0) WarningAmber else SuccessGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنبيهات المخزون والنواقص",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (lowStockCount > 0)
                                    "يوجد $lowStockCount جهاز أوشك على النفاد (<= 2 قطعة)"
                                else
                                    "المخزون متوفر بجميع الأصناف",
                                fontFamily = CairoFontFamily,
                                color = if (lowStockCount > 0) WarningAmber else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Alert 2: Recycle Bin
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onNavigateToRecycleBin()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Recycling,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سلة المحذوفات",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (recycleBinCount > 0)
                                    "يوجد $recycleBinCount عناصر في سلة المحذوفات"
                                else
                                    "لا توجد عناصر محذوفة حالياً",
                                fontFamily = CairoFontFamily,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Alert 3: Printer Status
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onNavigateToSettings()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            tint = GoldSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حالة الطابعة الحرارية",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (savedPrinterName != null)
                                    "متصلة: $savedPrinterName"
                                else
                                    "غير متصلة (انقر للربط عبر البلوتوث)",
                                fontFamily = CairoFontFamily,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "إغلاق",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
            }
        },
        containerColor = NavySurfaceElevated
    )
}

@Composable
private fun AlBashiriBottomNavBar(
    currentTab: MainTab,
    isAdmin: Boolean,
    onTabSelected: (MainTab) -> Unit,
    onBasheerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xF2080F1D),
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                GoldBorderGlow,
                                ElectricBlue.copy(alpha = 0.4f),
                                GoldBorderGlow,
                                Color.Transparent
                            )
                        )
                    )
            )

            var showMoreDialog by remember { mutableStateOf(false) }

            // Exact 5-item layout matching the reference mockup with Basheer in the center
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Far Right: المزيد (Grid Icon)
                val isMoreSelected = currentTab == MainTab.CATALOG || currentTab == MainTab.RETURNS || currentTab == MainTab.DAMAGE_REPAIR || currentTab == MainTab.SETTINGS
                BottomNavTabItem(
                    selected = isMoreSelected,
                    onClick = { showMoreDialog = true },
                    icon = Icons.Default.GridView,
                    label = "المزيد",
                    testTag = "nav_tab_more",
                    modifier = Modifier.weight(1f)
                )

                // 2. Right: المخزون (Warehouse Icon)
                BottomNavTabItem(
                    selected = currentTab == MainTab.INVENTORY,
                    onClick = { onTabSelected(MainTab.INVENTORY) },
                    icon = Icons.Default.Inventory2,
                    label = "المخزون",
                    testTag = "nav_tab_inventory",
                    modifier = Modifier.weight(1f)
                )

                // 3. Center: Voice Assistant "Basheer" (نواة طاقة هولوجرامية متوهجة ونابضة)
                BasheerPulsingEnergyOrb(
                    onClick = onBasheerClick,
                    modifier = Modifier.weight(1.25f)
                )

                // 4. Left: الفواتير (Invoices / Sales Log)
                BottomNavTabItem(
                    selected = currentTab == MainTab.SALES_LOG,
                    onClick = { onTabSelected(MainTab.SALES_LOG) },
                    icon = Icons.Default.ReceiptLong,
                    label = "الفواتير",
                    testTag = "nav_tab_sales",
                    modifier = Modifier.weight(1f)
                )

                // 5. Far Left: التقارير (Reports / Accounts)
                BottomNavTabItem(
                    selected = currentTab == MainTab.ACCOUNTS,
                    onClick = { onTabSelected(MainTab.ACCOUNTS) },
                    icon = Icons.Default.Assessment,
                    label = "التقارير",
                    testTag = "nav_tab_reports",
                    modifier = Modifier.weight(1f)
                )
            }

            if (showMoreDialog) {
                AlertDialog(
                    onDismissRequest = { showMoreDialog = false },
                    containerColor = Color(0xF50D172B),
                    shape = RoundedCornerShape(22.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GridView, contentDescription = null, tint = Color(0xFFF4CD68))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("أقسام المعرض الإضافية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTabSelected(MainTab.CATALOG)
                                        showMoreDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = if (currentTab == MainTab.CATALOG) Color(0xFF1E3258) else Color(0xFF121E36)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (currentTab == MainTab.CATALOG) Color(0xFFF4CD68) else Color(0x33FFFFFF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.GridView, contentDescription = null, tint = Color(0xFFF4CD68), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("الرئيسية (لوحة التحكم والمعرض)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTabSelected(MainTab.RETURNS)
                                        showMoreDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = if (currentTab == MainTab.RETURNS) Color(0xFF1E3258) else Color(0xFF121E36)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (currentTab == MainTab.RETURNS) Color(0xFFF4CD68) else Color(0x33FFFFFF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.AssignmentReturn, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("المرتجعات وإرجاع الأجهزة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTabSelected(MainTab.DAMAGE_REPAIR)
                                        showMoreDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = if (currentTab == MainTab.DAMAGE_REPAIR) Color(0xFF1E3258) else Color(0xFF121E36)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (currentTab == MainTab.DAMAGE_REPAIR) Color(0xFFF4CD68) else Color(0x33FFFFFF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("الهالك والصيانة والتوالف", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTabSelected(MainTab.SETTINGS)
                                        showMoreDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = if (currentTab == MainTab.SETTINGS) Color(0xFF1E3258) else Color(0xFF121E36)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (currentTab == MainTab.SETTINGS) Color(0xFFF4CD68) else Color(0x33FFFFFF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF8F9FB8), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("إعدادات المعرض والطباعة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showMoreDialog = false }) {
                            Text("إغلاق", fontFamily = CairoFontFamily, color = Color(0xFFF4CD68), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    secondaryLabel: String? = null,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (selected) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                .border(
                    if (selected) 1.dp else 0.dp,
                    if (selected) GoldPrimary.copy(alpha = 0.6f) else Color.Transparent,
                    RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(19.dp),
                tint = if (selected) GoldPrimary else TextMuted
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = CairoFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 9.5.sp,
            color = if (selected) GoldPrimary else TextMuted,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        if (secondaryLabel != null) {
            Text(
                text = secondaryLabel,
                fontFamily = CairoFontFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 8.5.sp,
                color = if (selected) GoldPrimary else TextMuted,
                maxLines = 1,
                lineHeight = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BasheerBottomNavTabItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
            .testTag("nav_tab_basheer"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF091222))
                .border(2.dp, Color(0xFFFFD54F), CircleShape)
                .padding(2.5.dp)
                .border(1.dp, Color(0x99FFD54F), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "المساعد بشير",
                modifier = Modifier.size(25.dp),
                tint = Color(0xFFFFD54F)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "بشير",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.5.sp,
            color = Color(0xFFFFD54F),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
