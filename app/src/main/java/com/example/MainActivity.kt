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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.BasheerVoiceAssistantDialog
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
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDarkBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
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

        Scaffold(
            topBar = {
                StoreTopAppBar(
                    currentUser = user,
                    recycleBinCount = recycleBinCount,
                    onRecycleBinClick = {
                        previousTab = currentTab
                        viewModel.selectTab(MainTab.RECYCLE_BIN)
                    },
                    onLogoutClick = { viewModel.logout() }
                )
            },
            bottomBar = {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        GoldBorderGlow,
                                        ElectricBlue.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    NavigationBar(
                        containerColor = NavySurface,
                        contentColor = GoldPrimary,
                        tonalElevation = 6.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == MainTab.CATALOG,
                            onClick = { viewModel.selectTab(MainTab.CATALOG) },
                            icon = { Icon(Icons.Default.GridView, contentDescription = "الكتالوج الهرمي") },
                            label = { Text("الكتالوج", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextDark,
                                selectedTextColor = GoldPrimary,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_catalog")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.INVENTORY,
                            onClick = { viewModel.selectTab(MainTab.INVENTORY) },
                            icon = { Icon(Icons.Default.Inventory2, contentDescription = "المخزون الشامل") },
                            label = { Text("المخزون", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextDark,
                                selectedTextColor = GoldPrimary,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_inventory")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.SALES_LOG,
                            onClick = { viewModel.selectTab(MainTab.SALES_LOG) },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "قسم الفواتير والمبيعات") },
                            label = { Text("الفواتير", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextDark,
                                selectedTextColor = GoldPrimary,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_sales")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.RETURNS,
                            onClick = { viewModel.selectTab(MainTab.RETURNS) },
                            icon = { Icon(Icons.AutoMirrored.Filled.AssignmentReturn, contentDescription = "قسم المرتجعات") },
                            label = { Text("المرتجعات", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextDark,
                                selectedTextColor = GoldPrimary,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_returns")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.DAMAGE_REPAIR,
                            onClick = { viewModel.selectTab(MainTab.DAMAGE_REPAIR) },
                            icon = { Icon(Icons.Default.Build, contentDescription = "قسم الهالك والإصلاح") },
                            label = { Text("الهالك والإصلاح", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextDark,
                                selectedTextColor = GoldPrimary,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_damage_repair")
                        )

                        if (isAdmin) {
                            NavigationBarItem(
                                selected = currentTab == MainTab.ACCOUNTS,
                                onClick = { viewModel.selectTab(MainTab.ACCOUNTS) },
                                icon = { Icon(Icons.Default.AccountBalance, contentDescription = "كشوف الحسابات") },
                                label = { Text("الحسابات", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TextDark,
                                    selectedTextColor = GoldPrimary,
                                    indicatorColor = GoldPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_tab_accounts")
                            )
                        }

                        NavigationBarItem(
                            selected = currentTab == MainTab.SETTINGS,
                            onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "الإعدادات") },
                            label = { Text("الإعدادات", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextDark,
                                selectedTextColor = GoldPrimary,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_settings")
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = CharcoalBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    MainTab.CATALOG -> {
                        CatalogScreen(
                            currentUser = user,
                            categories = categories,
                            brands = brands,
                            products = products,
                            models = models,
                            selectedCategory = selectedCategory,
                            selectedBrand = selectedBrand,
                            selectedProduct = selectedProduct,
                            searchQuery = catalogSearchQuery,
                            onSearchChange = { viewModel.setCatalogSearchQuery(it) },
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onSelectBrand = { viewModel.selectBrand(it) },
                            onSelectProduct = { viewModel.selectProduct(it) },
                            onNavigateBack = { viewModel.navigateCatalogBack() },
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
                            onDeleteModel = { viewModel.deleteModel(it) }
                        )
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

                // FIXED PROMINENT CIRCULAR VOICE ASSISTANT BUTTON "بشير"
                if (currentTab != MainTab.RECYCLE_BIN) {
                    BasheerFloatingButton(
                        onClick = {
                            ttsHelper.stop()
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
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
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
                        onOpenSale = { viewModel.openSaleDialog(it) }
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
            }
        }
    }
}
