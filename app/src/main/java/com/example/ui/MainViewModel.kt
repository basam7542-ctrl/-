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
import com.example.data.local.MerchantAccountEntity
import com.example.data.local.MerchantTransactionEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ProductModelEntity
import com.example.data.local.SaleRecordEntity
import com.example.data.local.SupplierAccountEntity
import com.example.data.local.SupplierTransactionEntity
import com.example.data.local.UserAccountEntity
import com.example.data.repository.StoreRepository
import com.example.utils.BluetoothPrinterHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.data.local.RecycleBinItemEntity
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    CATALOG,
    INVENTORY,
    SALES_LOG,
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
        recycleBinDao = db.recycleBinDao()
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

    private val _storeName = MutableStateFlow(BluetoothPrinterHelper.DEFAULT_STORE_NAME)
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting: StateFlow<Boolean> = _isPrinting.asStateFlow()

    // Last completed sale to offer receipt printing
    private val _lastCompletedSale = MutableStateFlow<SaleRecordEntity?>(null)
    val lastCompletedSale: StateFlow<SaleRecordEntity?> = _lastCompletedSale.asStateFlow()

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

    init {
        refreshPrinterState()
        viewModelScope.launch {
            DatabaseInitializer.seedDatabaseIfEmpty(db)
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
        notes: String,
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
                notes = notes
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

    // --- Bluetooth Thermal Printer Operations ---
    fun refreshPrinterState() {
        _savedPrinter.value = BluetoothPrinterHelper.getSavedPrinter(getApplication())
        _storeName.value = BluetoothPrinterHelper.getStoreName(getApplication())
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
        BluetoothPrinterHelper.saveStoreName(getApplication(), name)
        _storeName.value = name.trim().ifBlank { BluetoothPrinterHelper.DEFAULT_STORE_NAME }
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
}
