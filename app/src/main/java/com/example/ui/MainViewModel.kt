package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BrandEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.CustomerAccountEntity
import com.example.data.local.CustomerTransactionEntity
import com.example.data.local.DatabaseInitializer
import com.example.data.local.DamageRepairRecordEntity
import com.example.data.local.ExpenseCategoryEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.FinancialAdjustmentEntity
import com.example.data.local.InventoryAuditEntity
import com.example.data.local.InventoryAuditItemEntity
import com.example.data.local.InventoryAuditWithItems
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceItemEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.MerchantAccountEntity
import com.example.data.local.MerchantTransactionEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ProductModelEntity
import com.example.data.local.RecycleBinItemEntity
import com.example.data.local.ReturnWithItems
import com.example.data.local.SaleRecordEntity
import com.example.data.local.StockMovementEntity
import com.example.data.local.SupplierAccountEntity
import com.example.data.local.SupplierTransactionEntity
import com.example.data.local.TreasuryTransactionEntity
import com.example.data.local.UserAccountEntity
import com.example.data.repository.FinancialSummaryReport
import com.example.data.repository.InvoiceDraftItem
import com.example.data.repository.ReturnDraftItem
import com.example.data.repository.StoreRepository
import android.net.Uri
import com.example.utils.BluetoothPrinterHelper
import com.example.utils.StoreProfile
import com.example.utils.StoreProfileManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    CATALOG,
    INVENTORY,
    SALES_LOG,
    RETURNS,
    DAMAGE_REPAIR,
    ACCOUNTS,
    SETTINGS,
    RECYCLE_BIN
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = StoreRepository(
        userDao = db.userDao(),
        catalogDao = db.catalogDao(),
        salesDao = db.salesDao(),
        accountsDao = db.accountsDao(),
        recycleBinDao = db.recycleBinDao(),
        invoiceDao = db.invoiceDao(),
        returnsDao = db.returnsDao(),
        damageRepairDao = db.damageRepairDao(),
        stockMovementDao = db.stockMovementDao(),
        inventoryAuditDao = db.inventoryAuditDao(),
        financialDao = db.financialDao()
    )

    // Current User Session
    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    // Navigation Tab
    private val _currentTab = MutableStateFlow(MainTab.CATALOG)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // SharedPreferences for local settings
    private val prefs = application.getSharedPreferences("albashiri_prefs", Context.MODE_PRIVATE)
    private val _isTtsEnabled = MutableStateFlow(prefs.getBoolean("tts_welcome_enabled", true))
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    fun setTtsEnabled(enabled: Boolean) {
        _isTtsEnabled.value = enabled
        prefs.edit().putBoolean("tts_welcome_enabled", enabled).apply()
    }

    private val _isTtsOperationsEnabled = MutableStateFlow(prefs.getBoolean("tts_operations_enabled", true))
    val isTtsOperationsEnabled: StateFlow<Boolean> = _isTtsOperationsEnabled.asStateFlow()

    fun setTtsOperationsEnabled(enabled: Boolean) {
        _isTtsOperationsEnabled.value = enabled
        prefs.edit().putBoolean("tts_operations_enabled", enabled).apply()
    }

    // UI Message Notifications
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Hierarchical Catalog State
    private val _selectedCategory = MutableStateFlow<CategoryEntity?>(null)
    val selectedCategory: StateFlow<CategoryEntity?> = _selectedCategory.asStateFlow()

    private val _selectedBrand = MutableStateFlow<BrandEntity?>(null)
    val selectedBrand: StateFlow<BrandEntity?> = _selectedBrand.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    private val _catalogSearchQuery = MutableStateFlow("")
    val catalogSearchQuery: StateFlow<String> = _catalogSearchQuery.asStateFlow()

    // Inventory Screen Search & Filters
    private val _inventorySearchQuery = MutableStateFlow("")
    val inventorySearchQuery: StateFlow<String> = _inventorySearchQuery.asStateFlow()

    private val _inventoryCategoryFilter = MutableStateFlow<String?>(null)
    val inventoryCategoryFilter: StateFlow<String?> = _inventoryCategoryFilter.asStateFlow()

    private val _inventorySortOption = MutableStateFlow("ALL") // "ALL", "LOW_STOCK", "HIGH_STOCK", "NAME"
    val inventorySortOption: StateFlow<String> = _inventorySortOption.asStateFlow()

    // Sale Dialog State
    private val _saleTargetModel = MutableStateFlow<ProductModelEntity?>(null)
    val saleTargetModel: StateFlow<ProductModelEntity?> = _saleTargetModel.asStateFlow()

    // Selected Supplier, Merchant & Customer for detailed transactions view
    private val _selectedSupplier = MutableStateFlow<SupplierAccountEntity?>(null)
    val selectedSupplier: StateFlow<SupplierAccountEntity?> = _selectedSupplier.asStateFlow()

    private val _selectedMerchant = MutableStateFlow<MerchantAccountEntity?>(null)
    val selectedMerchant: StateFlow<MerchantAccountEntity?> = _selectedMerchant.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerAccountEntity?>(null)
    val selectedCustomer: StateFlow<CustomerAccountEntity?> = _selectedCustomer.asStateFlow()

    // Bluetooth Thermal Printer State
    private val _savedPrinter = MutableStateFlow<BluetoothPrinterHelper.PrinterDevice?>(null)
    val savedPrinter: StateFlow<BluetoothPrinterHelper.PrinterDevice?> = _savedPrinter.asStateFlow()

    private val _storeProfile = MutableStateFlow(StoreProfileManager.getStoreProfile(application))
    val storeProfile: StateFlow<StoreProfile> = _storeProfile.asStateFlow()

    private val _storeName = MutableStateFlow(_storeProfile.value.storeName)
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting: StateFlow<Boolean> = _isPrinting.asStateFlow()

    // Last completed sale to offer receipt printing
    private val _lastCompletedSale = MutableStateFlow<SaleRecordEntity?>(null)
    val lastCompletedSale: StateFlow<SaleRecordEntity?> = _lastCompletedSale.asStateFlow()

    // Selected invoice for full-screen preview and reprinting
    private val _selectedInvoiceForPreview = MutableStateFlow<SaleRecordEntity?>(null)
    val selectedInvoiceForPreview: StateFlow<SaleRecordEntity?> = _selectedInvoiceForPreview.asStateFlow()

    // Multi-Item Invoice flows & states
    val allInvoicesWithItems: StateFlow<List<InvoiceWithItems>> = repository.allInvoicesWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showCreateInvoiceScreen = MutableStateFlow(false)
    val showCreateInvoiceScreen: StateFlow<Boolean> = _showCreateInvoiceScreen.asStateFlow()

    private val _lastCompletedInvoice = MutableStateFlow<InvoiceWithItems?>(null)
    val lastCompletedInvoice: StateFlow<InvoiceWithItems?> = _lastCompletedInvoice.asStateFlow()

    private val _selectedInvoiceWithItemsForPreview = MutableStateFlow<InvoiceWithItems?>(null)
    val selectedInvoiceWithItemsForPreview: StateFlow<InvoiceWithItems?> = _selectedInvoiceWithItemsForPreview.asStateFlow()

    // Returns Management flows & states
    val allReturnsWithItems: StateFlow<List<ReturnWithItems>> = repository.allReturnsWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedReturnForPreview = MutableStateFlow<ReturnWithItems?>(null)
    val selectedReturnForPreview: StateFlow<ReturnWithItems?> = _selectedReturnForPreview.asStateFlow()

    private val _returnsSearchQuery = MutableStateFlow("")
    val returnsSearchQuery: StateFlow<String> = _returnsSearchQuery.asStateFlow()

    private val _returnsTypeFilter = MutableStateFlow<String?>("ALL")
    val returnsTypeFilter: StateFlow<String?> = _returnsTypeFilter.asStateFlow()

    // Damage & Scrap & Repair flows & states
    val allDamageRepairRecords: StateFlow<List<DamageRepairRecordEntity>> = repository.allDamageRepairRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _damageSearchQuery = MutableStateFlow("")
    val damageSearchQuery: StateFlow<String> = _damageSearchQuery.asStateFlow()

    private val _damageStatusFilter = MutableStateFlow<String?>("ALL")
    val damageStatusFilter: StateFlow<String?> = _damageStatusFilter.asStateFlow()

    fun setDamageSearchQuery(query: String) {
        _damageSearchQuery.value = query
    }

    fun setDamageStatusFilter(filter: String?) {
        _damageStatusFilter.value = filter
    }

    // Stock Movements & Inventory Audits Flows & States
    val allStockMovements: StateFlow<List<StockMovementEntity>> = repository.allStockMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInventoryAudits: StateFlow<List<InventoryAuditWithItems>> = repository.allInventoryAudits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _movementSearchQuery = MutableStateFlow("")
    val movementSearchQuery: StateFlow<String> = _movementSearchQuery.asStateFlow()

    private val _movementTypeFilter = MutableStateFlow<String?>("ALL")
    val movementTypeFilter: StateFlow<String?> = _movementTypeFilter.asStateFlow()

    private val _selectedAudit = MutableStateFlow<InventoryAuditWithItems?>(null)
    val selectedAudit: StateFlow<InventoryAuditWithItems?> = _selectedAudit.asStateFlow()

    fun setMovementSearchQuery(query: String) {
        _movementSearchQuery.value = query
    }

    fun setMovementTypeFilter(filter: String?) {
        _movementTypeFilter.value = filter
    }

    fun selectAudit(audit: InventoryAuditWithItems?) {
        _selectedAudit.value = audit
    }

    // Data Flows from Repository
    val allUsers: StateFlow<List<UserAccountEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBrands: StateFlow<List<BrandEntity>> = repository.allBrands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allModels: StateFlow<List<ProductModelEntity>> = repository.allModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<SaleRecordEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSuppliers: StateFlow<List<SupplierAccountEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMerchants: StateFlow<List<MerchantAccountEntity>> = repository.allMerchants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<CustomerAccountEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecycleBinItems: StateFlow<List<RecycleBinItemEntity>> = repository.allRecycleBinItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recycleBinCount: StateFlow<Int> = repository.recycleBinCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Financial Flows & States
    val allTreasuryTransactions: StateFlow<List<TreasuryTransactionEntity>> = repository.allTreasuryTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenseCategories: StateFlow<List<ExpenseCategoryEntity>> = repository.allExpenseCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFinancialAdjustments: StateFlow<List<FinancialAdjustmentEntity>> = repository.allFinancialAdjustments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _financialSummary = MutableStateFlow(FinancialSummaryReport())
    val financialSummary: StateFlow<FinancialSummaryReport> = _financialSummary.asStateFlow()

    init {
        refreshPrinterState()
        viewModelScope.launch {
            DatabaseInitializer.seedDatabaseIfEmpty(db)
            refreshFinancialSummary()
        }
    }

    // --- Authentication ---
    fun login(userId: String, pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserById(userId)
            if (user == null) {
                onError("الحساب المحدد غير موجود")
                return@launch
            }
            if (user.pin == pin) {
                _currentUser.value = user
                _currentTab.value = MainTab.CATALOG
                onSuccess()
            } else {
                onError("كلمة المرور غير صحيحة")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _selectedCategory.value = null
        _selectedBrand.value = null
        _selectedProduct.value = null
        _saleTargetModel.value = null
        _selectedSupplier.value = null
        _selectedMerchant.value = null
        _selectedCustomer.value = null
    }

    fun changePassword(
        currentPin: String,
        newPin: String,
        confirmPin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: return
        if (user.pin != currentPin) {
            onError("كلمة المرور الحالية غير صحيحة")
            return
        }
        if (newPin.length < 4) {
            onError("كلمة المرور يجب ألا تقل عن 4 أرقام/حروف")
            return
        }
        if (newPin != confirmPin) {
            onError("كلمة المرور الجديدة غير متطابقة مع التأكيد")
            return
        }
        viewModelScope.launch {
            repository.updatePin(user.id, newPin)
            _currentUser.value = user.copy(pin = newPin)
            onSuccess()
        }
    }

    fun updateCustomGreeting(userId: String, greeting: String, onSuccess: () -> Unit = {}) {
        val trimmedGreeting = greeting.trim()
        viewModelScope.launch {
            repository.updateCustomGreeting(userId, trimmedGreeting)
            if (_currentUser.value?.id == userId) {
                _currentUser.value = _currentUser.value?.copy(customGreeting = trimmedGreeting)
            }
            onSuccess()
        }
    }

    // --- Navigation ---
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: CategoryEntity) {
        _selectedCategory.value = category
        _selectedBrand.value = null
        _selectedProduct.value = null
    }

    fun selectBrand(brand: BrandEntity) {
        _selectedBrand.value = brand
        _selectedProduct.value = null
    }

    fun selectProduct(product: ProductEntity) {
        _selectedProduct.value = product
    }

    fun navigateCatalogBack(): Boolean {
        if (_selectedProduct.value != null) {
            _selectedProduct.value = null
            return true
        }
        if (_selectedBrand.value != null) {
            _selectedBrand.value = null
            return true
        }
        if (_selectedCategory.value != null) {
            _selectedCategory.value = null
            return true
        }
        return false
    }

    fun setCatalogSearchQuery(query: String) {
        _catalogSearchQuery.value = query
    }

    // --- Inventory Filters ---
    fun setInventorySearchQuery(query: String) {
        _inventorySearchQuery.value = query
    }

    fun setInventoryCategoryFilter(category: String?) {
        _inventoryCategoryFilter.value = category
    }

    fun setInventorySortOption(option: String) {
        _inventorySortOption.value = option
    }

    // --- Sales Operation ---
    fun openSaleDialog(model: ProductModelEntity) {
        _saleTargetModel.value = model
    }

    fun closeSaleDialog() {
        _saleTargetModel.value = null
    }

    fun submitSale(
        model: ProductModelEntity,
        soldPrice: Double,
        quantity: Int,
        notes: String = "",
        customerName: String = "",
        customerPhone: String = "",
        onSuccess: (SaleRecordEntity) -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (soldPrice < model.minAllowedPrice) {
            onError("عذرًا! لا يمكن البيع بسعر أقل من الحد الأدنى (${model.minAllowedPrice} ج.م)")
            return
        }
        if (quantity <= 0) {
            onError("الكمية يجب أن تكون 1 على الأقل")
            return
        }
        if (model.stockQuantity < quantity) {
            onError("الكمية المطلوبة ($quantity) أكبر من المتوفر في المخزن (${model.stockQuantity})")
            return
        }

        viewModelScope.launch {
            val result = repository.recordSale(
                modelId = model.id,
                productName = model.productName,
                modelName = model.modelName,
                categoryName = model.categoryName,
                brandName = model.brandName,
                soldPrice = soldPrice,
                minAllowedPrice = model.minAllowedPrice,
                originalFinalPrice = model.finalPrice,
                quantity = quantity,
                sellerId = user.id,
                sellerName = user.displayName,
                notes = notes,
                customerName = customerName,
                customerPhone = customerPhone
            )
            if (result.isSuccess) {
                val record = result.getOrNull()!!
                closeSaleDialog()
                _lastCompletedSale.value = record
                _toastMessage.emit("تم تسجيل عملية البيع بنجاح وخصم المخزون")
                onSuccess(record)
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء تسجيل البيع")
            }
        }
    }

    // --- Invoice Preview Dialog Control ---
    fun openInvoicePreview(sale: SaleRecordEntity) {
        _selectedInvoiceForPreview.value = sale
    }

    fun closeInvoicePreview() {
        _selectedInvoiceForPreview.value = null
    }

    // --- Multi-Item Invoice Creation & Management ---
    fun openCreateInvoiceScreen() {
        _showCreateInvoiceScreen.value = true
    }

    fun closeCreateInvoiceScreen() {
        _showCreateInvoiceScreen.value = false
    }

    fun openInvoiceWithItemsPreview(invoice: InvoiceWithItems) {
        _selectedInvoiceWithItemsForPreview.value = invoice
    }

    fun closeInvoiceWithItemsPreview() {
        _selectedInvoiceWithItemsForPreview.value = null
    }

    fun closePostInvoiceDialog() {
        _lastCompletedInvoice.value = null
    }

    fun submitMultiItemInvoice(
        customerName: String,
        customerPhone: String,
        items: List<InvoiceDraftItem>,
        discount: Double = 0.0,
        paidAmount: Double? = null,
        notes: String = "",
        onSuccess: (InvoiceWithItems) -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (items.isEmpty()) {
            onError("يرجى إضافة صنف واحد على الأقل داخل الفاتورة")
            return
        }

        viewModelScope.launch {
            val result = repository.createInvoice(
                customerName = customerName,
                customerPhone = customerPhone,
                sellerId = user.id,
                sellerName = user.displayName,
                items = items,
                discount = discount,
                paidAmount = paidAmount,
                notes = notes,
                storeProfile = _storeProfile.value
            )
            if (result.isSuccess) {
                val invoiceWithItems = result.getOrNull()!!
                _showCreateInvoiceScreen.value = false
                _lastCompletedInvoice.value = invoiceWithItems
                _toastMessage.emit("تم حفظ واعتماد الفاتورة #${invoiceWithItems.invoice.invoiceNumber} بنجاح وتحديث المخزون")
                onSuccess(invoiceWithItems)
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء اعتماد الفاتورة")
            }
        }
    }

    fun printInvoiceWithItems(
        invoice: InvoiceWithItems,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isPrinting.value = true
            val result = BluetoothPrinterHelper.printInvoiceWithItems(getApplication(), invoice)
            _isPrinting.value = false
            onResult(result.isSuccess, result.message)
        }
    }

    // --- Returns Operations ---
    fun setReturnsSearchQuery(q: String) {
        _returnsSearchQuery.value = q
    }

    fun setReturnsTypeFilter(f: String?) {
        _returnsTypeFilter.value = f
    }

    fun openReturnPreview(returnWithItems: ReturnWithItems) {
        _selectedReturnForPreview.value = returnWithItems
    }

    fun closeReturnPreview() {
        _selectedReturnForPreview.value = null
    }

    suspend fun getReturnableInvoiceItems(invoiceId: Long): Result<List<Pair<com.example.data.local.InvoiceItemEntity, Int>>> {
        return repository.getReturnableInvoiceItems(invoiceId)
    }

    fun createReturn(
        type: String,
        partyName: String,
        partyPhone: String = "",
        partyId: Long? = null,
        originalInvoiceNumber: String = "",
        originalInvoiceId: Long? = null,
        items: List<ReturnDraftItem>,
        reason: String = "",
        settlementType: String,
        settlementAmount: Double? = null,
        settlementNotes: String = "",
        notes: String = "",
        onSuccess: (ReturnWithItems) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("تسجيل المرتجعات مقتصر على المدير فقط")
            return
        }
        if (items.isEmpty()) {
            onError("يرجى إضافة صنف واحد على الأقل داخل المرتجع")
            return
        }

        viewModelScope.launch {
            val result = repository.createReturn(
                userRole = user.role,
                userId = user.id,
                userName = user.displayName,
                type = type,
                partyName = partyName,
                partyPhone = partyPhone,
                partyId = partyId,
                originalInvoiceNumber = originalInvoiceNumber,
                originalInvoiceId = originalInvoiceId,
                items = items,
                reason = reason,
                settlementType = settlementType,
                settlementAmount = settlementAmount,
                settlementNotes = settlementNotes,
                notes = notes
            )
            if (result.isSuccess) {
                val ret = result.getOrNull()!!
                _toastMessage.emit("تم تسجيل واعتماد المرتجع #${ret.returnRecord.returnNumber} بنجاح")
                onSuccess(ret)
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء حفظ المرتجع")
            }
        }
    }

    fun cancelReturn(
        returnId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null || user.role != "ADMIN") {
            onError("إلغاء المرتجعات مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            val result = repository.cancelReturn(returnId, user.role)
            if (result.isSuccess) {
                _toastMessage.emit("تم إلغاء المرتجع وإرجاع حالة المخزون والحسابات")
                if (_selectedReturnForPreview.value?.returnRecord?.id == returnId) {
                    _selectedReturnForPreview.value = null
                }
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء إلغاء المرتجع")
            }
        }
    }

    fun printReturnReceipt(
        returnWithItems: ReturnWithItems,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isPrinting.value = true
            val result = BluetoothPrinterHelper.printReturnReceipt(getApplication(), returnWithItems)
            _isPrinting.value = false
            onResult(result.isSuccess, result.message)
        }
    }

    fun exportReturnsCsv(): String {
        return repository.exportReturnsToCsv(allReturnsWithItems.value)
    }

    // --- Damage, Scrap & Repair Operations ---
    fun registerDamageOrScrap(
        modelId: Long,
        quantity: Int,
        reason: String,
        damageDetails: String,
        initialStatus: String,
        sourceType: String = "INVENTORY",
        returnRecordId: Long? = null,
        returnNumber: String = "",
        unitCost: Double? = null,
        totalLossAmount: Double? = null,
        imageUri: String = "",
        notes: String = "",
        onSuccess: (DamageRepairRecordEntity) -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("عذرًا! تسجيل واعتماد الهالك والتالف مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            val result = repository.registerDamageOrScrap(
                modelId = modelId,
                quantity = quantity,
                reason = reason,
                damageDetails = damageDetails,
                initialStatus = initialStatus,
                sourceType = sourceType,
                returnRecordId = returnRecordId,
                returnNumber = returnNumber,
                unitCost = unitCost,
                totalLossAmount = totalLossAmount,
                imageUri = imageUri,
                notes = notes,
                userRole = user.role,
                userId = user.id,
                userName = user.displayName
            )
            if (result.isSuccess) {
                val record = result.getOrNull()!!
                _toastMessage.emit("تم تسجيل العملية بنجاح #${record.recordNumber}")
                onSuccess(record)
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء تسجيل العملية")
            }
        }
    }

    fun sendToRepair(
        recordId: Long,
        repairReason: String,
        technicianNotes: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("تحويل المنتج للإصلاح مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            val result = repository.sendToRepair(
                recordId = recordId,
                repairReason = repairReason,
                technicianNotes = technicianNotes,
                userRole = user.role,
                userId = user.id,
                userName = user.displayName
            )
            if (result.isSuccess) {
                _toastMessage.emit("تم تحويل المنتج إلى قسم الإصلاح بنجاح")
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء التحويل للإصلاح")
            }
        }
    }

    fun completeRepair(
        recordId: Long,
        outcome: String,
        sparePartsCost: Double,
        laborCost: Double,
        repairNotes: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("تسجيل نتيجة الإصلاح مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            val result = repository.completeRepair(
                recordId = recordId,
                outcome = outcome,
                sparePartsCost = sparePartsCost,
                laborCost = laborCost,
                repairNotes = repairNotes,
                userRole = user.role,
                userId = user.id,
                userName = user.displayName
            )
            if (result.isSuccess) {
                val msg = if (outcome == "CANNOT_BE_FIXED") {
                    "تم تصنيف المنتج كتالف نهائيًا لتعذر الإصلاح"
                } else {
                    "تم تسجيل اكتمال الإصلاح وبانتظار اعتماد إعادته للمخزون"
                }
                _toastMessage.emit(msg)
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء تسجيل الإصلاح")
            }
        }
    }

    fun approveReturnToStock(
        recordId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("اعتماد عودة المنتج للمخزون مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            val result = repository.approveReturnToStock(
                recordId = recordId,
                userRole = user.role,
                userId = user.id,
                userName = user.displayName
            )
            if (result.isSuccess) {
                _toastMessage.emit("تم اعتماد عودة المنتج للمخزون المتاح للبيع بنجاح")
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء اعتماد عودة المنتج")
            }
        }
    }

    fun exportDamageRepairCsv(): String {
        return repository.exportDamageRepairCsv(allDamageRepairRecords.value)
    }

    // --- Inbound Stock Receiving & Movements Operations ---
    fun receiveStock(
        modelId: Long,
        quantity: Int,
        unitCostPrice: Double,
        supplierId: Long? = null,
        merchantId: Long? = null,
        supplierOrMerchantName: String = "",
        purchaseInvoiceNumber: String = "",
        notes: String = "",
        onSuccess: (ProductModelEntity) -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("عذرًا! تسجيل البضاعة الواردة مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            val result = repository.receiveStock(
                modelId = modelId,
                quantity = quantity,
                unitCostPrice = unitCostPrice,
                supplierId = supplierId,
                merchantId = merchantId,
                supplierOrMerchantName = supplierOrMerchantName,
                purchaseInvoiceNumber = purchaseInvoiceNumber,
                notes = notes,
                userRole = user.role,
                userId = user.id,
                userName = user.displayName
            )
            if (result.isSuccess) {
                val updatedModel = result.getOrNull()!!
                _toastMessage.emit("تم تسجيل توريد ${quantity} قطع بنجاح وتحديث الرصيد بالمخزن")
                onSuccess(updatedModel)
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء تسجيل التوريد")
            }
        }
    }

    fun manualAdjustStock(
        modelId: Long,
        newStock: Int,
        reason: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("تعديل رصيد المخزون مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            try {
                repository.updateStock(
                    modelId = modelId,
                    newStock = newStock,
                    userId = user.id,
                    userName = user.displayName,
                    reason = reason
                )
                _toastMessage.emit("تم تعديل الرصيد وتوثيق الحركة بالسجل")
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "حدث خطأ أثناء تعديل المخزون")
            }
        }
    }

    // --- Physical Inventory Audits Operations ---
    fun createInventoryAudit(
        title: String,
        scopeType: String = "ALL",
        scopeFilter: String? = null,
        notes: String = "",
        onSuccess: (InventoryAuditWithItems) -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("إنشاء الجرد مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            val result = repository.createInventoryAudit(
                title = title,
                scopeType = scopeType,
                scopeFilter = scopeFilter ?: "",
                customModelIds = emptyList(),
                notes = notes,
                userRole = user.role,
                userId = user.id,
                userName = user.displayName
            )
            if (result.isSuccess) {
                val auditWithItems = result.getOrNull()!!
                _selectedAudit.value = auditWithItems
                _toastMessage.emit("تم إنشاء جلسة جرد جديدة #${auditWithItems.audit.auditNumber}")
                onSuccess(auditWithItems)
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء بدء الجرد")
            }
        }
    }

    fun updateAuditItemActualQuantity(
        itemId: Long,
        actualQuantity: Int,
        notes: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("تعديل كميات الجرد مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            val result = repository.updateAuditItemActualQuantity(
                itemId = itemId,
                actualQuantity = actualQuantity,
                notes = notes,
                userRole = user.role
            )
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء تسجيل الكمية الفعلية")
            }
        }
    }

    fun settleInventoryAudit(
        auditId: Long,
        settlementReason: String = "",
        onSuccess: (InventoryAuditWithItems) -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("اعتماد وتسوية الجرد مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            val result = repository.settleInventoryAudit(
                auditId = auditId,
                settlementReason = settlementReason,
                userRole = user.role,
                userId = user.id,
                userName = user.displayName
            )
            if (result.isSuccess) {
                val settled = result.getOrNull()!!
                _selectedAudit.value = settled
                _toastMessage.emit("تم اعتماد تسوية فروق الجرد #${settled.audit.auditNumber} وتحديث أرصدة المخزون بنجاح")
                onSuccess(settled)
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء اعتماد تسوية الجرد")
            }
        }
    }

    fun cancelInventoryAudit(
        auditId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onError("يجب تسجيل الدخول أولاً")
            return
        }
        if (user.role != "ADMIN") {
            onError("إلغاء الجرد مقتصر على المدير فقط")
            return
        }
        viewModelScope.launch {
            val result = repository.cancelInventoryAudit(auditId, user.role)
            if (result.isSuccess) {
                _toastMessage.emit("تم إلغاء جلسة الجرد")
                if (_selectedAudit.value?.audit?.id == auditId) {
                    _selectedAudit.value = null
                }
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "حدث خطأ أثناء إلغاء الجرد")
            }
        }
    }

    fun exportStockMovementsCsv(): String {
        return repository.exportStockMovementsCsv(allStockMovements.value)
    }

    fun exportAuditVarianceCsv(audit: InventoryAuditWithItems): String {
        return repository.exportAuditVarianceCsv(audit)
    }

    fun exportCurrentStockValuationCsv(): String {
        return repository.exportCurrentStockValuationCsv(allModels.value)
    }

    // --- Bluetooth Thermal Printer & Store Profile Operations ---
    fun refreshPrinterState() {
        _savedPrinter.value = BluetoothPrinterHelper.getSavedPrinter(getApplication())
        _storeProfile.value = StoreProfileManager.getStoreProfile(getApplication())
        _storeName.value = _storeProfile.value.storeName
    }

    fun setSavedPrinter(printer: BluetoothPrinterHelper.PrinterDevice) {
        BluetoothPrinterHelper.savePrinter(getApplication(), printer)
        _savedPrinter.value = printer
    }

    fun clearSavedPrinter() {
        BluetoothPrinterHelper.clearSavedPrinter(getApplication())
        _savedPrinter.value = null
    }

    fun updateStoreName(name: String) {
        val updated = _storeProfile.value.copy(storeName = name.trim().ifBlank { StoreProfile.DEFAULT_STORE_NAME })
        updateStoreProfile(updated)
    }

    fun updateStoreProfile(
        profile: StoreProfile,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = _currentUser.value
        if (user?.role != "ADMIN") {
            val msg = "عذرًا! تعديل بيانات المعرض والبيانات القانونية والضريبية مقتصر على المدير فقط"
            onError(msg)
            viewModelScope.launch { _toastMessage.emit(msg) }
            return
        }
        StoreProfileManager.saveStoreProfile(getApplication(), profile)
        _storeProfile.value = profile
        _storeName.value = profile.storeName
        viewModelScope.launch {
            _toastMessage.emit("تم حفظ بيانات المعرض والبيانات القانونية والضريبية بنجاح")
        }
        onSuccess()
    }

    fun saveStoreLogo(
        uri: Uri,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = _currentUser.value
        if (user?.role != "ADMIN") {
            val msg = "تغيير شعار المعرض مقتصر على المدير فقط"
            onError(msg)
            viewModelScope.launch { _toastMessage.emit(msg) }
            return
        }
        val savedPath = StoreProfileManager.saveCustomLogoFromUri(getApplication(), uri)
        if (savedPath != null) {
            val updated = _storeProfile.value.copy(logoPath = savedPath)
            StoreProfileManager.saveStoreProfile(getApplication(), updated)
            _storeProfile.value = updated
            viewModelScope.launch {
                _toastMessage.emit("تم تحديث شعار المعرض بنجاح")
            }
            onSuccess(savedPath)
        } else {
            val err = "فشل حفظ الشعار، يرجى المحاولة بصورة أخرى"
            onError(err)
            viewModelScope.launch { _toastMessage.emit(err) }
        }
    }

    fun resetStoreLogo(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = _currentUser.value
        if (user?.role != "ADMIN") {
            val msg = "استعادة الشعار الافتراضي مقتصر على المدير فقط"
            onError(msg)
            viewModelScope.launch { _toastMessage.emit(msg) }
            return
        }
        StoreProfileManager.resetCustomLogo(getApplication())
        val updated = _storeProfile.value.copy(logoPath = "")
        _storeProfile.value = updated
        viewModelScope.launch {
            _toastMessage.emit("تمت استعادة الشعار الافتراضي لمؤسسة البشيري")
        }
        onSuccess()
    }

    fun closePostSaleDialog() {
        _lastCompletedSale.value = null
    }

    fun printReceipt(
        sale: SaleRecordEntity,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isPrinting.value = true
            val result = BluetoothPrinterHelper.printSaleReceipt(getApplication(), sale)
            _isPrinting.value = false
            onResult(result.isSuccess, result.message)
        }
    }

    fun printTestReceipt(
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isPrinting.value = true
            val result = BluetoothPrinterHelper.printTestReceipt(getApplication())
            _isPrinting.value = false
            onResult(result.isSuccess, result.message)
        }
    }

    // --- Admin Operations ---
    fun updateStock(modelId: Long, newStock: Int) {
        val user = _currentUser.value
        if (user?.role != "ADMIN") return
        viewModelScope.launch {
            repository.updateStock(modelId, newStock)
            _toastMessage.emit("تم تحديث رصيد المخزن")
        }
    }

    fun addCategory(name: String) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.addCategory(name)
            _toastMessage.emit("تمت إضافة القسم بنجاح")
        }
    }

    fun deleteCategory(id: Long) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.deleteCategory(id, user.displayName)
            if (_selectedCategory.value?.id == id) {
                _selectedCategory.value = null
            }
            _toastMessage.emit("تم نقل القسم إلى سلة المحذوفات")
        }
    }

    fun addBrand(name: String, categoryId: Long) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.addBrand(name, categoryId)
            _toastMessage.emit("تمت إضافة الشركة بنجاح")
        }
    }

    fun deleteBrand(id: Long) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.deleteBrand(id, user.displayName)
            if (_selectedBrand.value?.id == id) {
                _selectedBrand.value = null
            }
            _toastMessage.emit("تم نقل الشركة إلى سلة المحذوفات")
        }
    }

    fun addProduct(name: String, categoryId: Long, categoryName: String, brandId: Long, brandName: String) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.addProduct(
                name = name,
                categoryId = categoryId,
                categoryName = categoryName,
                brandId = brandId,
                brandName = brandName
            )
            _toastMessage.emit("تمت إضافة المنتج بنجاح")
        }
    }

    fun deleteProduct(id: Long) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.deleteProduct(id, user.displayName)
            if (_selectedProduct.value?.id == id) {
                _selectedProduct.value = null
            }
            _toastMessage.emit("تم نقل المنتج إلى سلة المحذوفات")
        }
    }

    fun addModel(
        productId: Long,
        productName: String,
        brandName: String,
        categoryName: String,
        modelName: String,
        finalPrice: Double,
        minAllowedPrice: Double,
        stockQuantity: Int,
        code: String
    ) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.addModel(
                productId = productId,
                productName = productName,
                brandName = brandName,
                categoryName = categoryName,
                modelName = modelName,
                finalPrice = finalPrice,
                minAllowedPrice = minAllowedPrice,
                stockQuantity = stockQuantity,
                code = code
            )
            _toastMessage.emit("تمت إضافة الموديل بنجاح")
        }
    }

    fun updateModel(model: ProductModelEntity) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.updateModel(model)
            _toastMessage.emit("تم تعديل بيانات الموديل بنجاح")
        }
    }

    fun deleteModel(id: Long) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.deleteModel(id, user.displayName)
            _toastMessage.emit("تم نقل الموديل إلى سلة المحذوفات")
        }
    }

    // --- Accounts & Transactions (Suppliers & Merchants) ---
    fun selectSupplier(supplier: SupplierAccountEntity?) {
        _selectedSupplier.value = supplier
    }

    fun getSupplierTransactions(supplierId: Long): StateFlow<List<SupplierTransactionEntity>> {
        return repository.getSupplierTransactions(supplierId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addSupplier(name: String, phone: String, initialPurchases: Double, notes: String) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.addSupplier(name, phone, initialPurchases, notes)
            _toastMessage.emit("تمت إضافة حساب المورد بنجاح")
        }
    }

    fun addSupplierTransaction(supplierId: Long, type: String, amount: Double, notes: String) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.addSupplierTransaction(supplierId, type, amount, notes)
            _toastMessage.emit("تم تسجيل العملية وتحديث كشف حساب المورد")
        }
    }

    fun updateSupplierTransaction(transactionId: Long, type: String, amount: Double, notes: String) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.updateSupplierTransaction(transactionId, type, amount, notes)
            _toastMessage.emit("تم تعديل العملية وتحديث رصيد المورد بنجاح")
        }
    }

    fun deleteSupplierTransaction(transactionId: Long) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.deleteSupplierTransaction(transactionId, user.displayName)
            _toastMessage.emit("تم نقل الحركة إلى سلة المحذوفات")
        }
    }

    fun deleteSupplier(supplierId: Long) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.deleteSupplier(supplierId, user.displayName)
            if (_selectedSupplier.value?.id == supplierId) {
                _selectedSupplier.value = null
            }
            _toastMessage.emit("تم نقل حساب المورد وعملياته إلى سلة المحذوفات")
        }
    }

    fun selectMerchant(merchant: MerchantAccountEntity?) {
        _selectedMerchant.value = merchant
    }

    fun getMerchantTransactions(merchantId: Long): StateFlow<List<MerchantTransactionEntity>> {
        return repository.getMerchantTransactions(merchantId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addMerchant(name: String, phone: String, initialDelivered: Double, notes: String) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.addMerchant(name, phone, initialDelivered, notes)
            _toastMessage.emit("تمت إضافة حساب التاجر بنجاح")
        }
    }

    fun addMerchantTransaction(merchantId: Long, type: String, amount: Double, notes: String) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.addMerchantTransaction(merchantId, type, amount, notes)
            _toastMessage.emit("تم تسجيل العملية وتحديث كشف حساب التاجر")
        }
    }

    fun updateMerchantTransaction(transactionId: Long, type: String, amount: Double, notes: String) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.updateMerchantTransaction(transactionId, type, amount, notes)
            _toastMessage.emit("تم تعديل العملية وتحديث رصيد التاجر بنجاح")
        }
    }

    fun deleteMerchantTransaction(transactionId: Long) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.deleteMerchantTransaction(transactionId, user.displayName)
            _toastMessage.emit("تم نقل الحركة إلى سلة المحذوفات")
        }
    }

    fun deleteMerchant(merchantId: Long) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.deleteMerchant(merchantId, user.displayName)
            if (_selectedMerchant.value?.id == merchantId) {
                _selectedMerchant.value = null
            }
            _toastMessage.emit("تم نقل حساب التاجر وعملياته إلى سلة المحذوفات")
        }
    }

    // --- Accounts & Transactions (آجل بالخارج) ---
    fun selectCustomer(customer: CustomerAccountEntity?) {
        _selectedCustomer.value = customer
    }

    fun getCustomerTransactions(customerId: Long): StateFlow<List<CustomerTransactionEntity>> {
        return repository.getCustomerTransactions(customerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addCustomer(name: String, phone: String, initialCreditSales: Double, notes: String) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.addCustomer(name, phone, initialCreditSales, notes)
            _toastMessage.emit("تمت إضافة حساب عميل آجل بالخارج بنجاح")
        }
    }

    fun addCustomerTransaction(
        customerId: Long,
        type: String,
        amount: Double,
        productDescription: String,
        notes: String
    ) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.addCustomerTransaction(customerId, type, amount, productDescription, notes)
            _toastMessage.emit("تم تسجيل العملية وتحديث كشف حساب العميل")
        }
    }

    fun updateCustomerTransaction(
        transactionId: Long,
        type: String,
        amount: Double,
        productDescription: String,
        notes: String
    ) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            repository.updateCustomerTransaction(transactionId, type, amount, productDescription, notes)
            _toastMessage.emit("تم تعديل العملية وتحديث رصيد العميل بنجاح")
        }
    }

    fun deleteCustomerTransaction(transactionId: Long) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.deleteCustomerTransaction(transactionId, user.displayName)
            _toastMessage.emit("تم نقل الحركة إلى سلة المحذوفات")
        }
    }

    fun deleteCustomer(customerId: Long) {
        val user = _currentUser.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            repository.deleteCustomer(customerId, user.displayName)
            if (_selectedCustomer.value?.id == customerId) {
                _selectedCustomer.value = null
            }
            _toastMessage.emit("تم نقل حساب العميل وعملياته إلى سلة المحذوفات")
        }
    }

    // --- Recycle Bin UI Actions ---
    fun restoreRecycleBinItem(item: RecycleBinItemEntity) {
        viewModelScope.launch {
            val res = repository.restoreRecycleBinItem(item)
            if (res.isSuccess) {
                _toastMessage.emit("تمت استعادة «${item.title}» بنجاح")
            } else {
                _toastMessage.emit("تعذر الاستعادة: ${res.exceptionOrNull()?.message ?: "خطأ غير متوقع"}")
            }
        }
    }

    fun restoreAllRecycleBinItems() {
        viewModelScope.launch {
            val res = repository.restoreAllRecycleBinItems()
            if (res.isSuccess) {
                val count = res.getOrNull() ?: 0
                _toastMessage.emit("تمت استعادة $count عناصر بنجاح")
            } else {
                _toastMessage.emit("تعذر استعادة الكل: ${res.exceptionOrNull()?.message ?: "خطأ"}")
            }
        }
    }

    fun permanentDeleteRecycleBinItem(item: RecycleBinItemEntity) {
        val user = _currentUser.value
        if (user?.role != "ADMIN") {
            viewModelScope.launch {
                _toastMessage.emit("عذرًا! الحذف النهائي مقتصر على المدير فقط")
            }
            return
        }
        viewModelScope.launch {
            val res = repository.permanentDeleteRecycleBinItem(item.id, user.role)
            if (res.isSuccess) {
                _toastMessage.emit("تم الحذف النهائي لـ «${item.title}»")
            } else {
                _toastMessage.emit(res.exceptionOrNull()?.message ?: "تعذر الحذف النهائي")
            }
        }
    }

    fun emptyRecycleBin() {
        val user = _currentUser.value
        if (user?.role != "ADMIN") {
            viewModelScope.launch {
                _toastMessage.emit("عذرًا! إفراغ سلة المحذوفات مقتصر على المدير فقط")
            }
            return
        }
        viewModelScope.launch {
            val res = repository.emptyRecycleBin(user.role)
            if (res.isSuccess) {
                _toastMessage.emit("تم إفراغ سلة المحذوفات بالكامل")
            } else {
                _toastMessage.emit(res.exceptionOrNull()?.message ?: "تعذر إفراغ السلة")
            }
        }
    }

    // --- Voice Assistant "Basheer" Product Ingestion ---
    fun addVoiceProduct(
        categoryName: String,
        brandName: String,
        productName: String,
        modelName: String,
        finalPrice: Double,
        minAllowedPrice: Double,
        stockQuantity: Int
    ) {
        if (_currentUser.value?.role != "ADMIN") return
        viewModelScope.launch {
            val existingCat = allCategories.value.firstOrNull { it.name.trim() == categoryName.trim() }
            val catId = existingCat?.id ?: repository.addCategory(categoryName.trim())

            val existingBrand = allBrands.value.firstOrNull { it.name.trim() == brandName.trim() && it.categoryId == catId }
            val brandId = existingBrand?.id ?: repository.addBrand(brandName.trim(), catId)

            val prodId = repository.addProduct(
                name = productName.trim(),
                categoryId = catId,
                categoryName = categoryName.trim(),
                brandId = brandId,
                brandName = brandName.trim()
            )

            repository.addModel(
                productId = prodId,
                productName = productName.trim(),
                brandName = brandName.trim(),
                categoryName = categoryName.trim(),
                modelName = modelName.trim(),
                finalPrice = finalPrice,
                minAllowedPrice = minAllowedPrice,
                stockQuantity = stockQuantity,
                code = "V-" + (1000..9999).random()
            )

            _toastMessage.emit("تمت إضافة المنتج والموديل صوتياً بنجاح بواسطة بشير!")
        }
    }

    // --- Backup & Export ---
    fun generateBackupCsv(): String {
        return repository.buildCsvExport(
            models = allModels.value,
            sales = allSales.value,
            suppliers = allSuppliers.value,
            merchants = allMerchants.value,
            customers = allCustomers.value
        )
    }

    // --- Financial System Operations ---
    fun refreshFinancialSummary(startDate: Long? = null, endDate: Long? = null) {
        viewModelScope.launch {
            _financialSummary.value = repository.getFinancialSummaryReport(startDate, endDate)
        }
    }

    fun recordTreasuryTransaction(
        type: String,
        category: String,
        amount: Double,
        referenceType: String = "MANUAL",
        partyName: String = "",
        paymentMethod: String = "CASH",
        description: String = "",
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val res = repository.recordTreasuryTransaction(
                type = type,
                category = category,
                amount = amount,
                referenceType = referenceType,
                partyType = "INTERNAL",
                partyName = partyName,
                paymentMethod = paymentMethod,
                description = description,
                userId = user?.id ?: "admin",
                userName = user?.name ?: "المدير"
            )
            if (res.isSuccess) {
                _toastMessage.emit("تم تسجيل حركة الخزينة بنجاح")
                refreshFinancialSummary()
                onComplete(true, "تم تسجيل حركة الخزينة بنجاح")
            } else {
                val err = res.exceptionOrNull()?.message ?: "فشل تسجيل حركة الخزينة"
                _toastMessage.emit(err)
                onComplete(false, err)
            }
        }
    }

    fun reverseTreasuryTransaction(transactionId: Long, reason: String, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val user = _currentUser.value
            val res = repository.reverseTreasuryTransaction(
                transactionId = transactionId,
                reversalReason = reason,
                userId = user?.id ?: "admin",
                userName = user?.name ?: "المدير"
            )
            if (res.isSuccess) {
                _toastMessage.emit("تمت الحركة العكسية وإلغاء المعاملة بنجاح")
                refreshFinancialSummary()
                onComplete(true, "تمت الحركة العكسية بنجاح")
            } else {
                val err = res.exceptionOrNull()?.message ?: "فشل إلغاء الحركة"
                _toastMessage.emit(err)
                onComplete(false, err)
            }
        }
    }

    fun recordExpense(
        categoryName: String,
        amount: Double,
        description: String,
        beneficiary: String = "",
        paymentMethod: String = "CASH_TREASURY",
        referenceNumber: String = "",
        notes: String = "",
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val res = repository.recordExpense(
                categoryName = categoryName,
                amount = amount,
                description = description,
                beneficiary = beneficiary,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                userId = user?.id ?: "admin",
                userName = user?.name ?: "المدير",
                notes = notes
            )
            if (res.isSuccess) {
                _toastMessage.emit("تم تسجيل المصروف بنجاح")
                refreshFinancialSummary()
                onComplete(true, "تم تسجيل المصروف بنجاح")
            } else {
                val err = res.exceptionOrNull()?.message ?: "فشل تسجيل المصروف"
                _toastMessage.emit(err)
                onComplete(false, err)
            }
        }
    }

    fun reverseExpense(expenseId: Long, reason: String, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val user = _currentUser.value
            val res = repository.reverseExpense(
                expenseId = expenseId,
                reversalReason = reason,
                userId = user?.id ?: "admin",
                userName = user?.name ?: "المدير"
            )
            if (res.isSuccess) {
                _toastMessage.emit("تم إلغاء المصروف بحركة عكسية بنجاح")
                refreshFinancialSummary()
                onComplete(true, "تم إلغاء المصروف بنجاح")
            } else {
                val err = res.exceptionOrNull()?.message ?: "فشل إلغاء المصروف"
                _toastMessage.emit(err)
                onComplete(false, err)
            }
        }
    }

    fun addExpenseCategory(name: String, description: String = "", onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val res = repository.addExpenseCategory(name, description)
            if (res.isSuccess) {
                _toastMessage.emit("تم إضافة بند المصروف بنجاح")
                onComplete(true, "تمت الإضافة")
            } else {
                val err = res.exceptionOrNull()?.message ?: "فشل إضافة البند"
                _toastMessage.emit(err)
                onComplete(false, err)
            }
        }
    }

    fun deleteExpenseCategory(id: Long, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val res = repository.deleteExpenseCategory(id)
            if (res.isSuccess) {
                _toastMessage.emit("تم حذف البند بنجاح")
                onComplete(true, "تم الحذف")
            } else {
                val err = res.exceptionOrNull()?.message ?: "فشل حذف البند"
                _toastMessage.emit(err)
                onComplete(false, err)
            }
        }
    }

    fun recordFinancialAdjustment(
        targetAccountType: String,
        targetAccountId: Long?,
        targetAccountName: String,
        adjustmentType: String,
        amount: Double,
        reason: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val res = repository.recordFinancialAdjustment(
                targetAccountType = targetAccountType,
                targetAccountId = targetAccountId,
                targetAccountName = targetAccountName,
                adjustmentType = adjustmentType,
                amount = amount,
                reason = reason,
                userId = user?.id ?: "admin",
                userName = user?.name ?: "المدير"
            )
            if (res.isSuccess) {
                _toastMessage.emit("تم اعتماد التسوية المالية وتحديث الأرصدة بنجاح")
                refreshFinancialSummary()
                onComplete(true, "تمت التسوية بنجاح")
            } else {
                val err = res.exceptionOrNull()?.message ?: "فشل إجراء التسوية المالية"
                _toastMessage.emit(err)
                onComplete(false, err)
            }
        }
    }

    fun exportTreasuryLedgerCsv(): String =
        repository.exportTreasuryLedgerCsv(allTreasuryTransactions.value)

    fun exportExpensesCsv(): String =
        repository.exportExpensesCsv(allExpenses.value)

    fun exportFinancialReportCsv(): String =
        repository.exportFinancialReportCsv(financialSummary.value)
}
