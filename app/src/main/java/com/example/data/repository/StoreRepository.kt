package com.example.data.repository

import com.example.data.local.AccountsDao
import com.example.data.local.BrandEntity
import com.example.data.local.CatalogDao
import com.example.data.local.CategoryEntity
import com.example.data.local.CustomerAccountEntity
import com.example.data.local.CustomerTransactionEntity
import com.example.data.local.MerchantAccountEntity
import com.example.data.local.MerchantTransactionEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ProductModelEntity
import com.example.data.local.RecycleBinDao
import com.example.data.local.RecycleBinItemEntity
import com.example.data.local.SaleRecordEntity
import com.example.data.local.SalesDao
import com.example.data.local.SupplierAccountEntity
import com.example.data.local.SupplierTransactionEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.UserDao
import com.example.utils.RecycleBinSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoreRepository(
    private val userDao: UserDao,
    private val catalogDao: CatalogDao,
    private val salesDao: SalesDao,
    private val accountsDao: AccountsDao,
    private val recycleBinDao: RecycleBinDao
) {
    // Recycle Bin Flows
    val allRecycleBinItems: Flow<List<RecycleBinItemEntity>> = recycleBinDao.getAllRecycleBinItems()
    val recycleBinCount: Flow<Int> = recycleBinDao.getRecycleBinCount()
    // Users
    val allUsers: Flow<List<UserAccountEntity>> = userDao.getAllUsers()

    suspend fun getUserById(id: String): UserAccountEntity? = userDao.getUserById(id)

    suspend fun updatePin(id: String, newPin: String) {
        userDao.updatePin(id, newPin)
    }

    suspend fun updateCustomGreeting(id: String, greeting: String) {
        userDao.updateCustomGreeting(id, greeting)
    }

    // Catalog
    val allCategories: Flow<List<CategoryEntity>> = catalogDao.getAllCategories()
    val allBrands: Flow<List<BrandEntity>> = catalogDao.getAllBrands()
    val allProducts: Flow<List<ProductEntity>> = catalogDao.getAllProducts()
    val allModels: Flow<List<ProductModelEntity>> = catalogDao.getAllModels()

    fun getBrandsForCategory(catId: Long): Flow<List<BrandEntity>> =
        catalogDao.getBrandsForCategory(catId)

    fun getProductsForCategoryAndBrand(catId: Long, brandId: Long): Flow<List<ProductEntity>> =
        catalogDao.getProductsForCategoryAndBrand(catId, brandId)

    fun getModelsForProduct(productId: Long): Flow<List<ProductModelEntity>> =
        catalogDao.getModelsForProduct(productId)

    suspend fun getModelById(id: Long): ProductModelEntity? = catalogDao.getModelById(id)

    // Admin Catalog Mutators
    suspend fun addCategory(name: String, iconType: String = "APPLIANCE"): Long =
        catalogDao.insertCategory(CategoryEntity(name = name, iconType = iconType))

    suspend fun deleteCategory(id: Long, deletedBy: String = "المدير") {
        val cat = catalogDao.getCategoryById(id) ?: return
        val brands = catalogDao.getBrandsForCategorySync(id)
        val prods = catalogDao.getProductsForCategorySync(id)
        val models = mutableListOf<ProductModelEntity>()
        prods.forEach { p ->
            models.addAll(catalogDao.getModelsForProductSync(p.id))
        }
        val json = RecycleBinSerializer.serializeCategory(cat, brands, prods, models)
        recycleBinDao.insertRecycleBinItem(
            RecycleBinItemEntity(
                itemType = "CATEGORY",
                originalId = cat.id,
                title = "قسم: ${cat.name}",
                subtitle = "يشمل ${brands.size} شركات و ${prods.size} منتجات و ${models.size} موديل",
                deletedBy = deletedBy,
                payloadJson = json
            )
        )
        models.forEach { catalogDao.deleteModel(it.id) }
        prods.forEach { catalogDao.deleteProduct(it.id) }
        brands.forEach { catalogDao.deleteBrand(it.id) }
        catalogDao.deleteCategory(id)
    }

    suspend fun addBrand(name: String, categoryId: Long): Long =
        catalogDao.insertBrand(BrandEntity(name = name, categoryId = categoryId))

    suspend fun deleteBrand(id: Long, deletedBy: String = "المدير") {
        val brand = catalogDao.getBrandById(id) ?: return
        val prods = catalogDao.getProductsForBrandSync(id)
        val models = mutableListOf<ProductModelEntity>()
        prods.forEach { p ->
            models.addAll(catalogDao.getModelsForProductSync(p.id))
        }
        val json = RecycleBinSerializer.serializeBrand(brand, prods, models)
        recycleBinDao.insertRecycleBinItem(
            RecycleBinItemEntity(
                itemType = "BRAND",
                originalId = brand.id,
                title = "شركة: ${brand.name}",
                subtitle = "يشمل ${prods.size} منتجات و ${models.size} موديل",
                deletedBy = deletedBy,
                payloadJson = json
            )
        )
        models.forEach { catalogDao.deleteModel(it.id) }
        prods.forEach { catalogDao.deleteProduct(it.id) }
        catalogDao.deleteBrand(id)
    }

    suspend fun addProduct(
        name: String,
        categoryId: Long,
        categoryName: String,
        brandId: Long,
        brandName: String,
        description: String = ""
    ): Long = catalogDao.insertProduct(
        ProductEntity(
            name = name,
            categoryId = categoryId,
            categoryName = categoryName,
            brandId = brandId,
            brandName = brandName,
            description = description
        )
    )

    suspend fun deleteProduct(id: Long, deletedBy: String = "المدير") {
        val prod = catalogDao.getProductById(id) ?: return
        val models = catalogDao.getModelsForProductSync(id)
        val json = RecycleBinSerializer.serializeProduct(prod, models)
        recycleBinDao.insertRecycleBinItem(
            RecycleBinItemEntity(
                itemType = "PRODUCT",
                originalId = prod.id,
                title = "منتج: ${prod.name}",
                subtitle = "قسم ${prod.categoryName} | ماركة ${prod.brandName} (${models.size} موديل)",
                deletedBy = deletedBy,
                payloadJson = json
            )
        )
        catalogDao.deleteModelsByProduct(id)
        catalogDao.deleteProduct(id)
    }

    suspend fun addModel(
        productId: Long,
        productName: String,
        brandName: String,
        categoryName: String,
        modelName: String,
        finalPrice: Double,
        minAllowedPrice: Double,
        stockQuantity: Int,
        code: String = ""
    ): Long = catalogDao.insertModel(
        ProductModelEntity(
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
    )

    suspend fun updateModel(model: ProductModelEntity) = catalogDao.updateModel(model)

    suspend fun deleteModel(id: Long, deletedBy: String = "المدير") {
        val model = catalogDao.getModelById(id) ?: return
        val json = RecycleBinSerializer.serializeModel(model)
        recycleBinDao.insertRecycleBinItem(
            RecycleBinItemEntity(
                itemType = "MODEL",
                originalId = model.id,
                title = "${model.productName} - ${model.modelName}",
                subtitle = "كود: ${model.code} | السعر: ${model.finalPrice} ج.م | المخزن: ${model.stockQuantity} قطع",
                deletedBy = deletedBy,
                payloadJson = json
            )
        )
        catalogDao.deleteModel(id)
    }

    suspend fun updateStock(modelId: Long, newStock: Int) =
        catalogDao.updateStock(modelId, newStock)

    // Sales
    val allSales: Flow<List<SaleRecordEntity>> = salesDao.getAllSales()

    suspend fun recordSale(
        modelId: Long,
        productName: String,
        modelName: String,
        categoryName: String,
        brandName: String,
        soldPrice: Double,
        minAllowedPrice: Double,
        originalFinalPrice: Double,
        quantity: Int,
        sellerId: String,
        sellerName: String,
        notes: String
    ): Result<SaleRecordEntity> {
        // Enforce validation: cannot sell below minimum allowed price
        if (soldPrice < minAllowedPrice) {
            return Result.failure(
                IllegalArgumentException("سعر البيع لا يمكن أن يقل عن الحد الأدنى المسموح به ($minAllowedPrice ج.م)")
            )
        }

        // Deduct stock
        catalogDao.deductStock(modelId, quantity)

        // Record transaction
        val entity = SaleRecordEntity(
            modelId = modelId,
            productName = productName,
            modelName = modelName,
            categoryName = categoryName,
            brandName = brandName,
            soldPrice = soldPrice,
            minAllowedPrice = minAllowedPrice,
            originalFinalPrice = originalFinalPrice,
            quantity = quantity,
            totalPrice = soldPrice * quantity,
            sellerId = sellerId,
            sellerName = sellerName,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        val saleId = salesDao.insertSale(entity)
        return Result.success(entity.copy(id = saleId))
    }

    // Accounts - Suppliers
    val allSuppliers: Flow<List<SupplierAccountEntity>> = accountsDao.getAllSuppliers()

    fun getSupplierTransactions(supplierId: Long): Flow<List<SupplierTransactionEntity>> =
        accountsDao.getTransactionsForSupplier(supplierId)

    suspend fun addSupplier(name: String, phone: String, initialPurchases: Double, notes: String): Long {
        return accountsDao.insertSupplier(
            SupplierAccountEntity(
                supplierName = name,
                phone = phone,
                totalPurchases = initialPurchases,
                totalPaid = 0.0,
                remainingBalance = initialPurchases,
                notes = notes
            )
        )
    }

    suspend fun addSupplierTransaction(supplierId: Long, type: String, amount: Double, notes: String) {
        val current = accountsDao.getSupplierById(supplierId) ?: return
        val newPurchases = if (type == "INVOICE") current.totalPurchases + amount else current.totalPurchases
        val newPaid = if (type == "PAYMENT") current.totalPaid + amount else current.totalPaid
        val newRemaining = newPurchases - newPaid

        accountsDao.updateSupplier(
            current.copy(
                totalPurchases = newPurchases,
                totalPaid = newPaid,
                remainingBalance = newRemaining
            )
        )
        accountsDao.insertSupplierTransaction(
            SupplierTransactionEntity(
                supplierId = supplierId,
                type = type,
                amount = amount,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateSupplierTransaction(
        transactionId: Long,
        newType: String,
        newAmount: Double,
        newNotes: String
    ) {
        val tx = accountsDao.getSupplierTransactionById(transactionId) ?: return
        val current = accountsDao.getSupplierById(tx.supplierId) ?: return

        var purchases = current.totalPurchases
        var paid = current.totalPaid

        // Revert old transaction effect
        if (tx.type == "INVOICE") {
            purchases -= tx.amount
        } else if (tx.type == "PAYMENT") {
            paid -= tx.amount
        }

        // Apply new transaction effect
        if (newType == "INVOICE") {
            purchases += newAmount
        } else if (newType == "PAYMENT") {
            paid += newAmount
        }

        val remaining = purchases - paid

        accountsDao.updateSupplier(
            current.copy(
                totalPurchases = maxOf(0.0, purchases),
                totalPaid = maxOf(0.0, paid),
                remainingBalance = remaining
            )
        )
        accountsDao.updateSupplierTransaction(
            tx.copy(
                type = newType,
                amount = newAmount,
                notes = newNotes
            )
        )
    }

    suspend fun deleteSupplierTransaction(transactionId: Long, deletedBy: String = "المدير") {
        val tx = accountsDao.getSupplierTransactionById(transactionId) ?: return
        val current = accountsDao.getSupplierById(tx.supplierId) ?: return

        var purchases = current.totalPurchases
        var paid = current.totalPaid

        if (tx.type == "INVOICE") {
            purchases -= tx.amount
        } else if (tx.type == "PAYMENT") {
            paid -= tx.amount
        }

        val remaining = purchases - paid

        val json = RecycleBinSerializer.serializeSupplierTx(tx)
        recycleBinDao.insertRecycleBinItem(
            RecycleBinItemEntity(
                itemType = "SUPPLIER_TX",
                originalId = tx.id,
                title = "حركة مورد: ${tx.amount} ج.م",
                subtitle = "مورد: ${current.supplierName} | نوع: ${if (tx.type == "INVOICE") "فاتورة توريد" else "سداد دفعة"} | ${tx.notes}",
                deletedBy = deletedBy,
                payloadJson = json
            )
        )

        accountsDao.updateSupplier(
            current.copy(
                totalPurchases = maxOf(0.0, purchases),
                totalPaid = maxOf(0.0, paid),
                remainingBalance = remaining
            )
        )
        accountsDao.deleteSupplierTransaction(transactionId)
    }

    suspend fun deleteSupplier(supplierId: Long, deletedBy: String = "المدير") {
        val supplier = accountsDao.getSupplierById(supplierId) ?: return
        val txs = accountsDao.getTransactionsForSupplierSync(supplierId)
        val json = RecycleBinSerializer.serializeSupplier(supplier, txs)
        recycleBinDao.insertRecycleBinItem(
            RecycleBinItemEntity(
                itemType = "SUPPLIER",
                originalId = supplier.id,
                title = "مورد: ${supplier.supplierName}",
                subtitle = "هاتف: ${supplier.phone} | متبقي له: ${supplier.remainingBalance} ج.م (${txs.size} حركة مسجلة)",
                deletedBy = deletedBy,
                payloadJson = json
            )
        )
        accountsDao.deleteSupplierTransactionsBySupplier(supplierId)
        accountsDao.deleteSupplier(supplierId)
    }

    // Accounts - Merchants (تجار بالأجل)
    val allMerchants: Flow<List<MerchantAccountEntity>> = accountsDao.getAllMerchants()

    fun getMerchantTransactions(merchantId: Long): Flow<List<MerchantTransactionEntity>> =
        accountsDao.getTransactionsForMerchant(merchantId)

    suspend fun addMerchant(name: String, phone: String, initialDelivered: Double, notes: String): Long {
        return accountsDao.insertMerchant(
            MerchantAccountEntity(
                merchantName = name,
                phone = phone,
                totalGoodsDelivered = initialDelivered,
                totalCollected = 0.0,
                remainingDueToMe = initialDelivered,
                notes = notes
            )
        )
    }

    suspend fun addMerchantTransaction(merchantId: Long, type: String, amount: Double, notes: String) {
        val current = accountsDao.getMerchantById(merchantId) ?: return
        val newDelivered = if (type == "GOODS_DELIVERED") current.totalGoodsDelivered + amount else current.totalGoodsDelivered
        val newCollected = if (type == "COLLECTION") current.totalCollected + amount else current.totalCollected
        val newRemaining = newDelivered - newCollected

        accountsDao.updateMerchant(
            current.copy(
                totalGoodsDelivered = newDelivered,
                totalCollected = newCollected,
                remainingDueToMe = newRemaining
            )
        )
        accountsDao.insertMerchantTransaction(
            MerchantTransactionEntity(
                merchantId = merchantId,
                type = type,
                amount = amount,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateMerchantTransaction(
        transactionId: Long,
        newType: String,
        newAmount: Double,
        newNotes: String
    ) {
        val tx = accountsDao.getMerchantTransactionById(transactionId) ?: return
        val current = accountsDao.getMerchantById(tx.merchantId) ?: return

        var delivered = current.totalGoodsDelivered
        var collected = current.totalCollected

        // Revert old transaction effect
        if (tx.type == "GOODS_DELIVERED") {
            delivered -= tx.amount
        } else if (tx.type == "COLLECTION") {
            collected -= tx.amount
        }

        // Apply new transaction effect
        if (newType == "GOODS_DELIVERED") {
            delivered += newAmount
        } else if (newType == "COLLECTION") {
            collected += newAmount
        }

        val remaining = delivered - collected

        accountsDao.updateMerchant(
            current.copy(
                totalGoodsDelivered = maxOf(0.0, delivered),
                totalCollected = maxOf(0.0, collected),
                remainingDueToMe = remaining
            )
        )
        accountsDao.updateMerchantTransaction(
            tx.copy(
                type = newType,
                amount = newAmount,
                notes = newNotes
            )
        )
    }

    suspend fun deleteMerchantTransaction(transactionId: Long, deletedBy: String = "المدير") {
        val tx = accountsDao.getMerchantTransactionById(transactionId) ?: return
        val current = accountsDao.getMerchantById(tx.merchantId) ?: return

        var delivered = current.totalGoodsDelivered
        var collected = current.totalCollected

        if (tx.type == "GOODS_DELIVERED") {
            delivered -= tx.amount
        } else if (tx.type == "COLLECTION") {
            collected -= tx.amount
        }

        val remaining = delivered - collected

        val json = RecycleBinSerializer.serializeMerchantTx(tx)
        recycleBinDao.insertRecycleBinItem(
            RecycleBinItemEntity(
                itemType = "MERCHANT_TX",
                originalId = tx.id,
                title = "حركة تاجر: ${tx.amount} ج.م",
                subtitle = "تاجر: ${current.merchantName} | نوع: ${if (tx.type == "GOODS_DELIVERED") "تسليم بضاعة" else "تحصيل دفعة"} | ${tx.notes}",
                deletedBy = deletedBy,
                payloadJson = json
            )
        )

        accountsDao.updateMerchant(
            current.copy(
                totalGoodsDelivered = maxOf(0.0, delivered),
                totalCollected = maxOf(0.0, collected),
                remainingDueToMe = remaining
            )
        )
        accountsDao.deleteMerchantTransaction(transactionId)
    }

    suspend fun deleteMerchant(merchantId: Long, deletedBy: String = "المدير") {
        val merchant = accountsDao.getMerchantById(merchantId) ?: return
        val txs = accountsDao.getTransactionsForMerchantSync(merchantId)
        val json = RecycleBinSerializer.serializeMerchant(merchant, txs)
        recycleBinDao.insertRecycleBinItem(
            RecycleBinItemEntity(
                itemType = "MERCHANT",
                originalId = merchant.id,
                title = "تاجر: ${merchant.merchantName}",
                subtitle = "هاتف: ${merchant.phone} | مستحق لي: ${merchant.remainingDueToMe} ج.م (${txs.size} حركة مسجلة)",
                deletedBy = deletedBy,
                payloadJson = json
            )
        )
        accountsDao.deleteMerchantTransactionsByMerchant(merchantId)
        accountsDao.deleteMerchant(merchantId)
    }

    // Accounts - Retail Customers (عملاء التجزئة بالأجل)
    val allCustomers: Flow<List<CustomerAccountEntity>> = accountsDao.getAllCustomers()

    fun getCustomerTransactions(customerId: Long): Flow<List<CustomerTransactionEntity>> =
        accountsDao.getTransactionsForCustomer(customerId)

    suspend fun addCustomer(name: String, phone: String, initialCreditSales: Double, notes: String): Long {
        return accountsDao.insertCustomer(
            CustomerAccountEntity(
                customerName = name,
                phone = phone,
                totalCreditSales = initialCreditSales,
                totalCollected = 0.0,
                remainingDueToMe = initialCreditSales,
                notes = notes
            )
        )
    }

    suspend fun addCustomerTransaction(
        customerId: Long,
        type: String,
        amount: Double,
        productDescription: String,
        notes: String
    ) {
        val current = accountsDao.getCustomerById(customerId) ?: return
        val newCreditSales = if (type == "CREDIT_SALE") current.totalCreditSales + amount else current.totalCreditSales
        val newCollected = if (type == "COLLECTION") current.totalCollected + amount else current.totalCollected
        val newRemaining = newCreditSales - newCollected

        accountsDao.updateCustomer(
            current.copy(
                totalCreditSales = newCreditSales,
                totalCollected = newCollected,
                remainingDueToMe = newRemaining
            )
        )
        accountsDao.insertCustomerTransaction(
            CustomerTransactionEntity(
                customerId = customerId,
                type = type,
                amount = amount,
                productDescription = productDescription,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateCustomerTransaction(
        transactionId: Long,
        newType: String,
        newAmount: Double,
        newProductDescription: String,
        newNotes: String
    ) {
        val tx = accountsDao.getCustomerTransactionById(transactionId) ?: return
        val current = accountsDao.getCustomerById(tx.customerId) ?: return

        var creditSales = current.totalCreditSales
        var collected = current.totalCollected

        // Revert old transaction
        if (tx.type == "CREDIT_SALE") {
            creditSales -= tx.amount
        } else if (tx.type == "COLLECTION") {
            collected -= tx.amount
        }

        // Apply new transaction
        if (newType == "CREDIT_SALE") {
            creditSales += newAmount
        } else if (newType == "COLLECTION") {
            collected += newAmount
        }

        val remaining = creditSales - collected

        accountsDao.updateCustomer(
            current.copy(
                totalCreditSales = maxOf(0.0, creditSales),
                totalCollected = maxOf(0.0, collected),
                remainingDueToMe = remaining
            )
        )
        accountsDao.updateCustomerTransaction(
            tx.copy(
                type = newType,
                amount = newAmount,
                productDescription = newProductDescription,
                notes = newNotes
            )
        )
    }

    suspend fun deleteCustomerTransaction(transactionId: Long, deletedBy: String = "المدير") {
        val tx = accountsDao.getCustomerTransactionById(transactionId) ?: return
        val current = accountsDao.getCustomerById(tx.customerId) ?: return

        var creditSales = current.totalCreditSales
        var collected = current.totalCollected

        if (tx.type == "CREDIT_SALE") {
            creditSales -= tx.amount
        } else if (tx.type == "COLLECTION") {
            collected -= tx.amount
        }

        val remaining = creditSales - collected

        val json = RecycleBinSerializer.serializeCustomerTx(tx)
        recycleBinDao.insertRecycleBinItem(
            RecycleBinItemEntity(
                itemType = "CUSTOMER_TX",
                originalId = tx.id,
                title = "حركة آجل بالخارج: ${tx.amount} ج.م",
                subtitle = "عميل: ${current.customerName} | نوع: ${if (tx.type == "CREDIT_SALE") "بيع بالأجل" else "تحصيل دفعة"} | ${tx.productDescription.ifBlank { tx.notes }}",
                deletedBy = deletedBy,
                payloadJson = json
            )
        )

        accountsDao.updateCustomer(
            current.copy(
                totalCreditSales = maxOf(0.0, creditSales),
                totalCollected = maxOf(0.0, collected),
                remainingDueToMe = remaining
            )
        )
        accountsDao.deleteCustomerTransaction(transactionId)
    }

    suspend fun deleteCustomer(customerId: Long, deletedBy: String = "المدير") {
        val customer = accountsDao.getCustomerById(customerId) ?: return
        val txs = accountsDao.getTransactionsForCustomerSync(customerId)
        val json = RecycleBinSerializer.serializeCustomer(customer, txs)
        recycleBinDao.insertRecycleBinItem(
            RecycleBinItemEntity(
                itemType = "CUSTOMER",
                originalId = customer.id,
                title = "عميل آجل بالخارج: ${customer.customerName}",
                subtitle = "هاتف: ${customer.phone} | متبقي عليه: ${customer.remainingDueToMe} ج.م (${txs.size} حركة مسجلة)",
                deletedBy = deletedBy,
                payloadJson = json
            )
        )
        accountsDao.deleteCustomerTransactionsByCustomer(customerId)
        accountsDao.deleteCustomer(customerId)
    }

    // Backup & Export (CSV format)
    fun buildCsvExport(
        models: List<ProductModelEntity>,
        sales: List<SaleRecordEntity>,
        suppliers: List<SupplierAccountEntity>,
        merchants: List<MerchantAccountEntity>,
        customers: List<CustomerAccountEntity> = emptyList()
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()

        sb.append("=== البشيري للأجهزة الكهربائية - تقرير النسخة الاحتياطية الشاملة ===\n")
        sb.append("تاريخ التصدير: ").append(dateFormat.format(Date())).append("\n\n")

        // 1. Inventory & Products
        sb.append("--- 1. كشف المنتجات والموديلات والمخزون ---\n")
        sb.append("القسم,الشركة,اسم المنتج,الموديل / المواصفة,كود الموديل,الكمية بالمخزن,سعر البيع النهائي,الحد الأدنى للبيع\n")
        models.forEach { m ->
            sb.append("\"${m.categoryName}\",\"${m.brandName}\",\"${m.productName}\",\"${m.modelName}\",\"${m.code}\",${m.stockQuantity},${m.finalPrice},${m.minAllowedPrice}\n")
        }
        sb.append("\n")

        // 2. Sales Log
        sb.append("--- 2. سجل عمليات البيع ---\n")
        sb.append("التاريخ والوقت,اسم البائع,المنتج,الموديل,الكمية,سعر القطعة الفعلي,إجمالي العملية,ملاحظات\n")
        sales.forEach { s ->
            val dateStr = dateFormat.format(Date(s.timestamp))
            sb.append("\"$dateStr\",\"${s.sellerName}\",\"${s.productName}\",\"${s.modelName}\",${s.quantity},${s.soldPrice},${s.totalPrice},\"${s.notes}\"\n")
        }
        sb.append("\n")

        // 3. Supplier Accounts
        sb.append("--- 3. كشوف حسابات الموردين ---\n")
        sb.append("اسم الشركة الموردة,رقم الهاتف,إجمالي المشتريات,إجمالي المدفوع,الباقي المستحق للمورد,ملاحظات\n")
        suppliers.forEach { sup ->
            sb.append("\"${sup.supplierName}\",\"${sup.phone}\",${sup.totalPurchases},${sup.totalPaid},${sup.remainingBalance},\"${sup.notes}\"\n")
        }
        sb.append("\n")

        // 4. Merchant Credit Accounts
        sb.append("--- 4. كشوف حسابات تجار الأجل ---\n")
        sb.append("اسم التاجر,رقم الهاتف,إجمالي البضاعة المسلمة,إجمالي المحصل,الباقي المستحق لي,ملاحظات\n")
        merchants.forEach { m ->
            sb.append("\"${m.merchantName}\",\"${m.phone}\",${m.totalGoodsDelivered},${m.totalCollected},${m.remainingDueToMe},\"${m.notes}\"\n")
        }
        sb.append("\n")

        // 5. Credit Accounts (آجل بالخارج)
        sb.append("--- 5. كشوف حسابات آجل بالخارج ---\n")
        sb.append("اسم العميل,رقم الهاتف,إجمالي المبيعات بالأجل,إجمالي المحصل,الباقي المستحق لي,ملاحظات\n")
        customers.forEach { c ->
            sb.append("\"${c.customerName}\",\"${c.phone}\",${c.totalCreditSales},${c.totalCollected},${c.remainingDueToMe},\"${c.notes}\"\n")
        }

        return sb.toString()
    }

    // --- Recycle Bin Operations ---
    suspend fun restoreRecycleBinItem(item: RecycleBinItemEntity): Result<Unit> {
        return try {
            when (item.itemType) {
                "MODEL" -> {
                    val model = RecycleBinSerializer.deserializeModel(item.payloadJson)
                    catalogDao.insertModel(model)
                }
                "PRODUCT" -> {
                    val bundle = RecycleBinSerializer.deserializeProduct(item.payloadJson)
                    catalogDao.insertProduct(bundle.product)
                    bundle.models.forEach { catalogDao.insertModel(it) }
                }
                "BRAND" -> {
                    val bundle = RecycleBinSerializer.deserializeBrand(item.payloadJson)
                    catalogDao.insertBrand(bundle.brand)
                    bundle.products.forEach { catalogDao.insertProduct(it) }
                    bundle.models.forEach { catalogDao.insertModel(it) }
                }
                "CATEGORY" -> {
                    val bundle = RecycleBinSerializer.deserializeCategory(item.payloadJson)
                    catalogDao.insertCategory(bundle.category)
                    bundle.brands.forEach { catalogDao.insertBrand(it) }
                    bundle.products.forEach { catalogDao.insertProduct(it) }
                    bundle.models.forEach { catalogDao.insertModel(it) }
                }
                "SUPPLIER" -> {
                    val bundle = RecycleBinSerializer.deserializeSupplier(item.payloadJson)
                    accountsDao.insertSupplier(bundle.supplier)
                    bundle.transactions.forEach { accountsDao.insertSupplierTransaction(it) }
                }
                "MERCHANT" -> {
                    val bundle = RecycleBinSerializer.deserializeMerchant(item.payloadJson)
                    accountsDao.insertMerchant(bundle.merchant)
                    bundle.transactions.forEach { accountsDao.insertMerchantTransaction(it) }
                }
                "CUSTOMER" -> {
                    val bundle = RecycleBinSerializer.deserializeCustomer(item.payloadJson)
                    accountsDao.insertCustomer(bundle.customer)
                    bundle.transactions.forEach { accountsDao.insertCustomerTransaction(it) }
                }
                "SUPPLIER_TX" -> {
                    val tx = RecycleBinSerializer.deserializeSupplierTx(item.payloadJson)
                    val supplier = accountsDao.getSupplierById(tx.supplierId)
                    if (supplier != null) {
                        val newPurchases = if (tx.type == "INVOICE") supplier.totalPurchases + tx.amount else supplier.totalPurchases
                        val newPaid = if (tx.type == "PAYMENT") supplier.totalPaid + tx.amount else supplier.totalPaid
                        accountsDao.updateSupplier(
                            supplier.copy(
                                totalPurchases = newPurchases,
                                totalPaid = newPaid,
                                remainingBalance = newPurchases - newPaid
                            )
                        )
                    }
                    accountsDao.insertSupplierTransaction(tx)
                }
                "MERCHANT_TX" -> {
                    val tx = RecycleBinSerializer.deserializeMerchantTx(item.payloadJson)
                    val merchant = accountsDao.getMerchantById(tx.merchantId)
                    if (merchant != null) {
                        val newDelivered = if (tx.type == "GOODS_DELIVERED") merchant.totalGoodsDelivered + tx.amount else merchant.totalGoodsDelivered
                        val newCollected = if (tx.type == "COLLECTION") merchant.totalCollected + tx.amount else merchant.totalCollected
                        accountsDao.updateMerchant(
                            merchant.copy(
                                totalGoodsDelivered = newDelivered,
                                totalCollected = newCollected,
                                remainingDueToMe = newDelivered - newCollected
                            )
                        )
                    }
                    accountsDao.insertMerchantTransaction(tx)
                }
                "CUSTOMER_TX" -> {
                    val tx = RecycleBinSerializer.deserializeCustomerTx(item.payloadJson)
                    val customer = accountsDao.getCustomerById(tx.customerId)
                    if (customer != null) {
                        val newCredit = if (tx.type == "CREDIT_SALE") customer.totalCreditSales + tx.amount else customer.totalCreditSales
                        val newCollected = if (tx.type == "COLLECTION") customer.totalCollected + tx.amount else customer.totalCollected
                        accountsDao.updateCustomer(
                            customer.copy(
                                totalCreditSales = newCredit,
                                totalCollected = newCollected,
                                remainingDueToMe = newCredit - newCollected
                            )
                        )
                    }
                    accountsDao.insertCustomerTransaction(tx)
                }
            }
            recycleBinDao.deleteRecycleBinItem(item.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreRecycleBinItem(id: Long): Result<Unit> {
        val item = recycleBinDao.getRecycleBinItemById(id) ?: return Result.failure(IllegalArgumentException("العنصر غير موجود بالسلة"))
        return restoreRecycleBinItem(item)
    }

    suspend fun restoreAllRecycleBinItems(): Result<Int> {
        return try {
            val all = recycleBinDao.getAllRecycleBinItems().first()
            var count = 0
            all.forEach { item ->
                if (restoreRecycleBinItem(item).isSuccess) {
                    count++
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun permanentDeleteRecycleBinItem(id: Long, userRole: String): Result<Unit> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("الحذف النهائي مقتصر على المدير فقط"))
        }
        recycleBinDao.deleteRecycleBinItem(id)
        return Result.success(Unit)
    }

    suspend fun emptyRecycleBin(userRole: String): Result<Unit> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("إفراغ سلة المحذوفات مقتصر على المدير فقط"))
        }
        recycleBinDao.deleteAllRecycleBinItems()
        return Result.success(Unit)
    }
}
