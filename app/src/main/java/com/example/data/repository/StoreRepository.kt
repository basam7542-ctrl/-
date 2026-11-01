package com.example.data.repository

import com.example.data.local.AccountsDao
import com.example.data.local.BrandEntity
import com.example.data.local.CatalogDao
import com.example.data.local.CategoryEntity
import com.example.data.local.CustomerAccountEntity
import com.example.data.local.CustomerTransactionEntity
import com.example.data.local.DamageRepairDao
import com.example.data.local.DamageRepairRecordEntity
import com.example.data.local.ExpenseCategoryEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.FinancialAdjustmentEntity
import com.example.data.local.FinancialDao
import com.example.data.local.InventoryAuditDao
import com.example.data.local.InventoryAuditEntity
import com.example.data.local.InventoryAuditItemEntity
import com.example.data.local.InventoryAuditWithItems
import com.example.data.local.InvoiceDao
import com.example.data.local.TreasuryTransactionEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceItemEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.MerchantAccountEntity
import com.example.data.local.MerchantTransactionEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ProductModelEntity
import com.example.data.local.RecycleBinDao
import com.example.data.local.RecycleBinItemEntity
import com.example.data.local.ReturnsDao
import com.example.data.local.ReturnRecordEntity
import com.example.data.local.ReturnItemEntity
import com.example.data.local.ReturnWithItems
import com.example.data.local.SaleRecordEntity
import com.example.data.local.SalesDao
import com.example.data.local.StockMovementDao
import com.example.data.local.StockMovementEntity
import com.example.data.local.SupplierAccountEntity
import com.example.data.local.SupplierTransactionEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.UserDao
import com.example.utils.RecycleBinSerializer
import com.example.utils.StoreProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InvoiceDraftItem(
    val model: ProductModelEntity,
    val quantity: Int,
    val negotiatedUnitPrice: Double
) {
    val unitPrice: Double get() = negotiatedUnitPrice
    val totalPrice: Double get() = quantity * negotiatedUnitPrice

    fun withPrice(newPrice: Double): InvoiceDraftItem = copy(negotiatedUnitPrice = newPrice)
    fun withQuantity(newQuantity: Int): InvoiceDraftItem = copy(quantity = newQuantity)
}

data class ReturnDraftItem(
    val modelId: Long,
    val productName: String,
    val modelName: String,
    val categoryName: String = "",
    val brandName: String = "",
    val quantity: Int,
    val unitPrice: Double,
    val stockCondition: String = "RESELLABLE", // "RESELLABLE", "DAMAGED", "INSPECTION", "REPAIRABLE"
    val itemNotes: String = ""
) {
    val totalPrice: Double get() = quantity * unitPrice
}

class StoreRepository(
    private val userDao: UserDao,
    private val catalogDao: CatalogDao,
    private val salesDao: SalesDao,
    private val accountsDao: AccountsDao,
    private val recycleBinDao: RecycleBinDao,
    private val invoiceDao: InvoiceDao? = null,
    private val returnsDao: ReturnsDao? = null,
    private val damageRepairDao: DamageRepairDao? = null,
    private val stockMovementDao: StockMovementDao? = null,
    private val inventoryAuditDao: InventoryAuditDao? = null,
    private val financialDao: FinancialDao? = null
) {
    // Invoices Flow
    val allInvoicesWithItems: Flow<List<InvoiceWithItems>> = invoiceDao?.getAllInvoicesWithItems() ?: flowOf(emptyList())

    // Returns Flow
    val allReturnsWithItems: Flow<List<ReturnWithItems>> = returnsDao?.getAllReturnsWithItems() ?: flowOf(emptyList())

    // Damage & Repair Flow
    val allDamageRepairRecords: Flow<List<DamageRepairRecordEntity>> =
        damageRepairDao?.getAllRecords() ?: flowOf(emptyList())

    // Stock Movements Flow
    val allStockMovements: Flow<List<StockMovementEntity>> =
        stockMovementDao?.getAllMovements() ?: flowOf(emptyList())

    // Inventory Audits Flow
    val allInventoryAudits: Flow<List<InventoryAuditWithItems>> =
        inventoryAuditDao?.getAllAudits() ?: flowOf(emptyList())

    // Financial Flows
    val allTreasuryTransactions: Flow<List<TreasuryTransactionEntity>> =
        financialDao?.getAllTreasuryTransactions() ?: flowOf(emptyList())

    val allExpenseCategories: Flow<List<ExpenseCategoryEntity>> =
        financialDao?.getAllExpenseCategories() ?: flowOf(emptyList())

    val allExpenses: Flow<List<ExpenseEntity>> =
        financialDao?.getAllExpenses() ?: flowOf(emptyList())

    val allFinancialAdjustments: Flow<List<FinancialAdjustmentEntity>> =
        financialDao?.getAllFinancialAdjustments() ?: flowOf(emptyList())

    suspend fun logStockMovement(
        movementType: String,
        modelId: Long,
        productName: String,
        modelName: String,
        code: String = "",
        categoryName: String = "",
        brandName: String = "",
        quantityChanged: Int,
        quantityBefore: Int,
        quantityAfter: Int,
        unitCostPrice: Double = 0.0,
        referenceType: String = "",
        referenceNumber: String = "",
        referenceId: Long? = null,
        userId: String = "admin",
        userName: String = "المدير",
        notes: String = ""
    ) {
        val dao = stockMovementDao ?: return
        try {
            dao.insertMovement(
                StockMovementEntity(
                    movementType = movementType,
                    modelId = modelId,
                    productName = productName,
                    modelName = modelName,
                    code = code,
                    categoryName = categoryName,
                    brandName = brandName,
                    quantityChanged = quantityChanged,
                    quantityBefore = quantityBefore,
                    quantityAfter = quantityAfter,
                    unitCostPrice = unitCostPrice,
                    referenceType = referenceType,
                    referenceNumber = referenceNumber,
                    referenceId = referenceId,
                    userId = userId,
                    userName = userName,
                    notes = notes
                )
            )
        } catch (_: Exception) {
            // Non-blocking log persistence
        }
    }

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

    suspend fun updateStock(
        modelId: Long,
        newStock: Int,
        userId: String = "admin",
        userName: String = "المدير",
        reason: String = "تعديل رصيد يدوي"
    ) {
        val model = catalogDao.getModelById(modelId)
        val before = model?.stockQuantity ?: 0
        catalogDao.updateStock(modelId, newStock)
        if (model != null && newStock != before) {
            logStockMovement(
                movementType = "MANUAL_ADJUSTMENT",
                modelId = modelId,
                productName = model.productName,
                modelName = model.modelName,
                code = model.code,
                categoryName = model.categoryName,
                brandName = model.brandName,
                quantityChanged = newStock - before,
                quantityBefore = before,
                quantityAfter = newStock,
                unitCostPrice = model.minAllowedPrice,
                referenceType = "MANUAL",
                referenceNumber = "",
                userId = userId,
                userName = userName,
                notes = reason
            )
        }
    }

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
        notes: String = "",
        customerName: String = "",
        customerPhone: String = ""
    ): Result<SaleRecordEntity> {
        // Enforce validation: cannot sell below minimum allowed price
        if (soldPrice < minAllowedPrice) {
            return Result.failure(
                IllegalArgumentException("سعر البيع لا يمكن أن يقل عن الحد الأدنى المسموح به ($minAllowedPrice ج.م)")
            )
        }

        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("الكمية المباعة يجب أن تكون أكبر من صفر"))
        }

        val liveModel = catalogDao.getModelById(modelId)
            ?: return Result.failure(IllegalArgumentException("المنتج المحدد غير مسجل بالمخزون"))

        // Do not allow selling unavailable quantity
        if (liveModel.stockQuantity < quantity) {
            return Result.failure(
                IllegalArgumentException("الكمية المطلوبة ($quantity) غير متاحة بالمخزن (الرصيد المتاح: ${liveModel.stockQuantity})")
            )
        }

        val beforeStock = liveModel.stockQuantity
        val afterStock = beforeStock - quantity

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
            customerName = customerName,
            customerPhone = customerPhone,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        val saleId = salesDao.insertSale(entity)

        // Dual-write into invoices for unified multi-item viewing
        if (invoiceDao != null) {
            val invoiceNumber = "INV-$saleId"
            val invId = invoiceDao.insertInvoice(
                InvoiceEntity(
                    id = saleId,
                    invoiceNumber = invoiceNumber,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    sellerId = sellerId,
                    sellerName = sellerName,
                    subtotal = entity.totalPrice,
                    discount = 0.0,
                    finalTotal = entity.totalPrice,
                    paidAmount = entity.totalPrice,
                    remainingAmount = 0.0,
                    paymentStatus = "PAID",
                    notes = notes,
                    timestamp = entity.timestamp
                )
            )
            invoiceDao.insertInvoiceItems(
                listOf(
                    InvoiceItemEntity(
                        id = saleId,
                        invoiceId = invId,
                        modelId = modelId,
                        productName = productName,
                        modelName = modelName,
                        categoryName = categoryName,
                        brandName = brandName,
                        quantity = quantity,
                        unitPrice = soldPrice,
                        minAllowedPrice = minAllowedPrice,
                        originalPrice = originalFinalPrice,
                        totalPrice = entity.totalPrice
                    )
                )
            )
        }

        logStockMovement(
            movementType = "SALE",
            modelId = modelId,
            productName = productName,
            modelName = modelName,
            code = liveModel.code,
            categoryName = categoryName,
            brandName = brandName,
            quantityChanged = -quantity,
            quantityBefore = beforeStock,
            quantityAfter = afterStock,
            unitCostPrice = liveModel.minAllowedPrice,
            referenceType = "INVOICE",
            referenceNumber = "INV-$saleId",
            referenceId = saleId,
            userId = sellerId,
            userName = sellerName,
            notes = if (customerName.isNotBlank()) "بيع نقدي للعميل: $customerName" else "بيع نقدي"
        )

        return Result.success(entity.copy(id = saleId))
    }

    suspend fun createInvoice(
        customerName: String,
        customerPhone: String,
        sellerId: String,
        sellerName: String,
        items: List<InvoiceDraftItem>,
        discount: Double = 0.0,
        paidAmount: Double? = null,
        notes: String = "",
        storeProfile: StoreProfile? = null
    ): Result<InvoiceWithItems> {
        if (items.isEmpty()) {
            return Result.failure(Exception("لا يمكن حفظ فاتورة بدون أصناف"))
        }

        // 1. Validation across all items
        for (item in items) {
            val liveModel = catalogDao.getModelById(item.model.id)
                ?: return Result.failure(Exception("المنتج «${item.model.productName} - ${item.model.modelName}» غير مسجل بالمخزون"))

            if (item.negotiatedUnitPrice < liveModel.minAllowedPrice) {
                return Result.failure(
                    Exception("سعر البيع للصنف «${liveModel.modelName}» (${item.negotiatedUnitPrice} ج.م) أقل من الحد الأدنى المسموح به (${liveModel.minAllowedPrice} ج.م)")
                )
            }

            if (item.quantity <= 0) {
                return Result.failure(Exception("الكمية للصنف «${liveModel.modelName}» يجب أن تكون 1 على الأقل"))
            }

            if (liveModel.stockQuantity < item.quantity) {
                return Result.failure(
                    Exception("الرصيد المتاح من «${liveModel.modelName}» بالمخزن (${liveModel.stockQuantity}) أقل من الكمية المطلوبة بالفاتورة (${item.quantity})")
                )
            }
        }

        // 2. Financial Totals
        val subtotal = items.sumOf { it.quantity * it.negotiatedUnitPrice }
        val finalTotal = (subtotal - discount).coerceAtLeast(0.0)
        val finalPaid = (paidAmount ?: finalTotal).coerceAtLeast(0.0)
        val remaining = (finalTotal - finalPaid).coerceAtLeast(0.0)
        val paymentStatus = when {
            remaining <= 0.0 -> "PAID"
            finalPaid > 0.0 -> "PARTIAL"
            else -> "CREDIT"
        }

        val invoiceNumber = "INV-" + System.currentTimeMillis().toString().takeLast(6)
        val timestamp = System.currentTimeMillis()

        val invoiceEntity = InvoiceEntity(
            invoiceNumber = invoiceNumber,
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            sellerId = sellerId,
            sellerName = sellerName,
            subtotal = subtotal,
            discount = discount,
            finalTotal = finalTotal,
            paidAmount = finalPaid,
            remainingAmount = remaining,
            paymentStatus = paymentStatus,
            notes = notes.trim(),
            timestamp = timestamp,
            storeName = storeProfile?.storeName?.trim() ?: "",
            storeAddress = storeProfile?.storeAddress?.trim() ?: "",
            storePhone = storeProfile?.storePhone?.trim() ?: "",
            commercialRegister = storeProfile?.commercialRegister?.trim() ?: "",
            taxCard = storeProfile?.taxCard?.trim() ?: "",
            vatNumber = storeProfile?.vatNumber?.trim() ?: "",
            storeLogoUri = storeProfile?.logoPath?.trim() ?: ""
        )

        val dao = invoiceDao ?: return Result.failure(Exception("خدمة الفواتير غير مفعلة بقاعدة البيانات"))
        val invoiceId = dao.insertInvoice(invoiceEntity)

        val invoiceItemEntities = items.map { item ->
            val liveModel = catalogDao.getModelById(item.model.id)!!
            InvoiceItemEntity(
                invoiceId = invoiceId,
                modelId = item.model.id,
                productName = item.model.productName,
                modelName = item.model.modelName,
                categoryName = item.model.categoryName,
                brandName = item.model.brandName,
                quantity = item.quantity,
                unitPrice = item.negotiatedUnitPrice,
                minAllowedPrice = liveModel.minAllowedPrice,
                originalPrice = liveModel.finalPrice,
                totalPrice = item.quantity * item.negotiatedUnitPrice
            )
        }
        dao.insertInvoiceItems(invoiceItemEntities)

        // 3. Deduct stock for each item exactly once and log stock movement
        for (item in items) {
            val liveModel = catalogDao.getModelById(item.model.id)
            val beforeStock = liveModel?.stockQuantity ?: item.quantity
            val afterStock = beforeStock - item.quantity
            catalogDao.deductStock(item.model.id, item.quantity)
            logStockMovement(
                movementType = "SALE",
                modelId = item.model.id,
                productName = item.model.productName,
                modelName = item.model.modelName,
                code = liveModel?.code ?: item.model.code,
                categoryName = item.model.categoryName,
                brandName = item.model.brandName,
                quantityChanged = -item.quantity,
                quantityBefore = beforeStock,
                quantityAfter = afterStock,
                unitCostPrice = liveModel?.minAllowedPrice ?: 0.0,
                referenceType = "INVOICE",
                referenceNumber = invoiceNumber,
                referenceId = invoiceId,
                userId = sellerId,
                userName = sellerName,
                notes = if (customerName.isNotBlank()) "فاتورة بيع للعميل: $customerName" else "فاتورة بيع نقدي"
            )
        }

        // 4. Dual-write to salesDao for backward compatibility
        for (item in items) {
            val liveModel = catalogDao.getModelById(item.model.id)
            salesDao.insertSale(
                SaleRecordEntity(
                    modelId = item.model.id,
                    productName = item.model.productName,
                    modelName = item.model.modelName,
                    categoryName = item.model.categoryName,
                    brandName = item.model.brandName,
                    soldPrice = item.negotiatedUnitPrice,
                    minAllowedPrice = liveModel?.minAllowedPrice ?: item.model.minAllowedPrice,
                    originalFinalPrice = liveModel?.finalPrice ?: item.model.finalPrice,
                    quantity = item.quantity,
                    totalPrice = item.quantity * item.negotiatedUnitPrice,
                    sellerId = sellerId,
                    sellerName = sellerName,
                    notes = if (notes.isNotBlank()) "فاتورة $invoiceNumber: $notes" else "فاتورة $invoiceNumber",
                    customerName = customerName.trim(),
                    customerPhone = customerPhone.trim(),
                    timestamp = timestamp
                )
            )
        // 5. Automatic Treasury Cash-In for paid amount
        if (finalPaid > 0) {
            recordTreasuryTransaction(
                type = "CASH_IN",
                category = "SALE_CASH",
                amount = finalPaid,
                referenceType = "INVOICE",
                referenceId = invoiceId,
                referenceNumber = invoiceNumber,
                partyType = "CUSTOMER",
                partyName = if (customerName.isNotBlank()) customerName.trim() else "عميل نقدي",
                paymentMethod = "CASH",
                description = "مقبوضات فاتورة بيع $invoiceNumber",
                userId = sellerId,
                userName = sellerName
            )
        }

        val completedInvoice = InvoiceWithItems(
            invoice = invoiceEntity.copy(id = invoiceId),
            items = invoiceItemEntities
        )

        return Result.success(completedInvoice)
    }

    // --- Returns Management System ---
    suspend fun getReturnableInvoiceItems(invoiceId: Long): Result<List<Pair<InvoiceItemEntity, Int>>> {
        val invDao = invoiceDao ?: return Result.failure(Exception("خدمة الفواتير غير مفعلة"))
        val invoice = invDao.getInvoiceWithItemsById(invoiceId)
            ?: return Result.failure(Exception("الفاتورة غير موجودة"))

        val retDao = returnsDao ?: return Result.failure(Exception("خدمة المرتجعات غير مفعلة"))
        val activeReturns = retDao.getActiveReturnsForInvoice(invoiceId)

        val returnedByModel = mutableMapOf<Long, Int>()
        activeReturns.forEach { ret ->
            ret.items.forEach { item ->
                returnedByModel[item.modelId] = (returnedByModel[item.modelId] ?: 0) + item.quantity
            }
        }

        val resultList = invoice.items.map { item ->
            val alreadyReturned = returnedByModel[item.modelId] ?: 0
            val remaining = (item.quantity - alreadyReturned).coerceAtLeast(0)
            Pair(item, remaining)
        }
        return Result.success(resultList)
    }

    suspend fun createReturn(
        userRole: String,
        userId: String,
        userName: String,
        type: String, // "CUSTOMER_TO_STORE", "STORE_TO_SUPPLIER", "MERCHANT_TO_STORE", "STORE_TO_MERCHANT"
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
        notes: String = ""
    ): Result<ReturnWithItems> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("تسجيل واعتماد المرتجعات مقتصر على المدير فقط"))
        }

        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("لا يمكن تسجيل مرتجع بدون أصناف"))
        }

        val retDao = returnsDao ?: return Result.failure(IllegalStateException("خدمة المرتجعات غير مهيأة"))

        // Validate Items & Invoice Link
        if (originalInvoiceId != null) {
            val returnableRes = getReturnableInvoiceItems(originalInvoiceId)
            if (returnableRes.isFailure) {
                return Result.failure(returnableRes.exceptionOrNull() ?: Exception("فشل التحقق من الفاتورة الأصلية"))
            }
            val returnableMap = returnableRes.getOrNull()!!.associate { it.first.modelId to it.second }

            for (item in items) {
                if (item.quantity <= 0) {
                    return Result.failure(IllegalArgumentException("كمية الصنف «${item.modelName}» يجب أن تكون أكبر من صفر"))
                }
                val maxAllowed = returnableMap[item.modelId]
                if (maxAllowed == null) {
                    return Result.failure(IllegalArgumentException("الصنف «${item.modelName}» غير مسجل بالفاتورة الأصلية المرتبطة"))
                }
                if (item.quantity > maxAllowed) {
                    return Result.failure(
                        IllegalArgumentException("الكمية المرتجعة من «${item.modelName}» (${item.quantity}) تتجاوز الكمية المتبقية بالفاتورة ($maxAllowed)")
                    )
                }
            }
        } else {
            for (item in items) {
                if (item.quantity <= 0) {
                    return Result.failure(IllegalArgumentException("كمية الصنف «${item.modelName}» يجب أن تكون أكبر من صفر"))
                }
            }
        }

        val totalAmount = items.sumOf { it.totalPrice }
        val finalSettlementAmount = settlementAmount ?: totalAmount
        val returnNumber = "RET-" + System.currentTimeMillis().toString().takeLast(6)
        val timestamp = System.currentTimeMillis()

        // Stock Updates based on direction and condition
        for (item in items) {
            val liveModel = catalogDao.getModelById(item.modelId)
            val beforeStock = liveModel?.stockQuantity ?: 0
            when (type) {
                "CUSTOMER_TO_STORE", "MERCHANT_TO_STORE" -> {
                    // Only RESELLABLE items increment sellable inventory
                    if (item.stockCondition == "RESELLABLE") {
                        val afterStock = beforeStock + item.quantity
                        catalogDao.addStock(item.modelId, item.quantity)
                        logStockMovement(
                            movementType = "RETURN_CUSTOMER",
                            modelId = item.modelId,
                            productName = item.productName,
                            modelName = item.modelName,
                            code = liveModel?.code ?: "",
                            categoryName = item.categoryName,
                            brandName = item.brandName,
                            quantityChanged = item.quantity,
                            quantityBefore = beforeStock,
                            quantityAfter = afterStock,
                            unitCostPrice = liveModel?.minAllowedPrice ?: item.unitPrice,
                            referenceType = "RETURN",
                            referenceNumber = returnNumber,
                            userId = userId,
                            userName = userName,
                            notes = "مرتجع وارد صالح للبيع من: $partyName"
                        )
                    }
                }
                "STORE_TO_SUPPLIER", "STORE_TO_MERCHANT" -> {
                    // Items leaving the store inventory are deducted
                    val afterStock = (beforeStock - item.quantity).coerceAtLeast(0)
                    catalogDao.deductStock(item.modelId, item.quantity)
                    logStockMovement(
                        movementType = "RETURN_SUPPLIER",
                        modelId = item.modelId,
                        productName = item.productName,
                        modelName = item.modelName,
                        code = liveModel?.code ?: "",
                        categoryName = item.categoryName,
                        brandName = item.brandName,
                        quantityChanged = -item.quantity,
                        quantityBefore = beforeStock,
                        quantityAfter = afterStock,
                        unitCostPrice = liveModel?.minAllowedPrice ?: item.unitPrice,
                        referenceType = "RETURN",
                        referenceNumber = returnNumber,
                        userId = userId,
                        userName = userName,
                        notes = "مرتجع صادر إلى: $partyName"
                    )
                }
            }
        }

        // Financial Account Adjustments if linked to registered account
        if (partyId != null) {
            when (settlementType) {
                "CUSTOMER_CREDIT" -> {
                    val customer = accountsDao.getCustomerById(partyId)
                    if (customer != null) {
                        val newCredit = (customer.totalCreditSales - finalSettlementAmount).coerceAtLeast(0.0)
                        val newDue = (customer.remainingDueToMe - finalSettlementAmount).coerceAtLeast(0.0)
                        accountsDao.updateCustomer(
                            customer.copy(
                                totalCreditSales = newCredit,
                                remainingDueToMe = newDue
                            )
                        )
                        accountsDao.insertCustomerTransaction(
                            CustomerTransactionEntity(
                                customerId = partyId,
                                type = "COLLECTION",
                                amount = finalSettlementAmount,
                                productDescription = "مرتجع $returnNumber",
                                notes = "تسوية مرتجع أصناف",
                                timestamp = timestamp
                            )
                        )
                    }
                }
                "DECREASE_DEBT" -> {
                    val supplier = accountsDao.getSupplierById(partyId)
                    if (supplier != null) {
                        val newPurchases = (supplier.totalPurchases - finalSettlementAmount).coerceAtLeast(0.0)
                        val newRemaining = (supplier.remainingBalance - finalSettlementAmount).coerceAtLeast(0.0)
                        accountsDao.updateSupplier(
                            supplier.copy(
                                totalPurchases = newPurchases,
                                remainingBalance = newRemaining
                            )
                        )
                        accountsDao.insertSupplierTransaction(
                            SupplierTransactionEntity(
                                supplierId = partyId,
                                type = "PAYMENT",
                                amount = finalSettlementAmount,
                                notes = "مرتجع بضاعة للمورد $returnNumber",
                                timestamp = timestamp
                            )
                        )
                    }
                }
                "DECREASE_MERCHANT_DUE" -> {
                    val merchant = accountsDao.getMerchantById(partyId)
                    if (merchant != null) {
                        val newDelivered = (merchant.totalGoodsDelivered - finalSettlementAmount).coerceAtLeast(0.0)
                        val newDue = (merchant.remainingDueToMe - finalSettlementAmount).coerceAtLeast(0.0)
                        accountsDao.updateMerchant(
                            merchant.copy(
                                totalGoodsDelivered = newDelivered,
                                remainingDueToMe = newDue
                            )
                        )
                        accountsDao.insertMerchantTransaction(
                            MerchantTransactionEntity(
                                merchantId = partyId,
                                type = "COLLECTION",
                                amount = finalSettlementAmount,
                                notes = "تسوية مرتجع $returnNumber",
                                timestamp = timestamp
                            )
                        )
                    }
                }
            }
        }

        val recordEntity = ReturnRecordEntity(
            returnNumber = returnNumber,
            type = type,
            timestamp = timestamp,
            partyName = partyName.trim(),
            partyPhone = partyPhone.trim(),
            partyId = partyId,
            originalInvoiceNumber = originalInvoiceNumber.trim(),
            originalInvoiceId = originalInvoiceId,
            totalAmount = totalAmount,
            reason = reason.trim(),
            settlementType = settlementType,
            settlementAmount = finalSettlementAmount,
            settlementNotes = settlementNotes.trim(),
            createdByUserId = userId,
            createdByName = userName,
            status = "APPROVED",
            notes = notes.trim()
        )

        val returnRecordId = retDao.insertReturnRecord(recordEntity)

        val itemEntities = items.map { item ->
            ReturnItemEntity(
                returnId = returnRecordId,
                modelId = item.modelId,
                productName = item.productName,
                modelName = item.modelName,
                categoryName = item.categoryName,
                brandName = item.brandName,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                totalPrice = item.totalPrice,
                stockCondition = item.stockCondition,
                itemNotes = item.itemNotes
            )
        }
        retDao.insertReturnItems(itemEntities)

        // Automatic Treasury cash tracking for cash refunds / returns
        if (settlementType == "CASH_REFUND" && finalSettlementAmount > 0) {
            when (type) {
                "CUSTOMER_TO_STORE", "MERCHANT_TO_STORE" -> {
                    recordTreasuryTransaction(
                        type = "CASH_OUT",
                        category = "RETURN_REFUND",
                        amount = finalSettlementAmount,
                        referenceType = "RETURN",
                        referenceId = returnRecordId,
                        referenceNumber = returnNumber,
                        partyType = if (type == "CUSTOMER_TO_STORE") "CUSTOMER" else "MERCHANT",
                        partyName = partyName.trim(),
                        paymentMethod = "CASH",
                        description = "صرف مبلغ نقدي لمرتجع $returnNumber",
                        userId = userId,
                        userName = userName
                    )
                }
                "STORE_TO_SUPPLIER", "STORE_TO_MERCHANT" -> {
                    recordTreasuryTransaction(
                        type = "CASH_IN",
                        category = "RETURN_REFUND",
                        amount = finalSettlementAmount,
                        referenceType = "RETURN",
                        referenceId = returnRecordId,
                        referenceNumber = returnNumber,
                        partyType = if (type == "STORE_TO_SUPPLIER") "SUPPLIER" else "MERCHANT",
                        partyName = partyName.trim(),
                        paymentMethod = "CASH",
                        description = "استرداد نقدي من مرتجع $returnNumber",
                        userId = userId,
                        userName = userName
                    )
                }
            }
        }

        return Result.success(
            ReturnWithItems(
                returnRecord = recordEntity.copy(id = returnRecordId),
                items = itemEntities
            )
        )
    }

    suspend fun cancelReturn(returnId: Long, userRole: String): Result<Unit> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("إلغاء المرتجعات مقتصر على المدير فقط"))
        }
        val retDao = returnsDao ?: return Result.failure(IllegalStateException("خدمة المرتجعات غير مهيأة"))
        val returnWithItems = retDao.getReturnById(returnId)
            ?: return Result.failure(IllegalArgumentException("عملية المرتجع غير موجودة"))

        if (returnWithItems.returnRecord.status == "CANCELLED") {
            return Result.failure(IllegalStateException("عملية المرتجع ملغاة بالفعل"))
        }

        val record = returnWithItems.returnRecord
        for (item in returnWithItems.items) {
            when (record.type) {
                "CUSTOMER_TO_STORE", "MERCHANT_TO_STORE" -> {
                    if (item.stockCondition == "RESELLABLE") {
                        catalogDao.deductStock(item.modelId, item.quantity)
                    }
                }
                "STORE_TO_SUPPLIER", "STORE_TO_MERCHANT" -> {
                    catalogDao.addStock(item.modelId, item.quantity)
                }
            }
        }

        if (record.partyId != null) {
            when (record.settlementType) {
                "CUSTOMER_CREDIT" -> {
                    val customer = accountsDao.getCustomerById(record.partyId)
                    if (customer != null) {
                        accountsDao.updateCustomer(
                            customer.copy(
                                totalCreditSales = customer.totalCreditSales + record.settlementAmount,
                                remainingDueToMe = customer.remainingDueToMe + record.settlementAmount
                            )
                        )
                    }
                }
                "DECREASE_DEBT" -> {
                    val supplier = accountsDao.getSupplierById(record.partyId)
                    if (supplier != null) {
                        accountsDao.updateSupplier(
                            supplier.copy(
                                totalPurchases = supplier.totalPurchases + record.settlementAmount,
                                remainingBalance = supplier.remainingBalance + record.settlementAmount
                            )
                        )
                    }
                }
                "DECREASE_MERCHANT_DUE" -> {
                    val merchant = accountsDao.getMerchantById(record.partyId)
                    if (merchant != null) {
                        accountsDao.updateMerchant(
                            merchant.copy(
                                totalGoodsDelivered = merchant.totalGoodsDelivered + record.settlementAmount,
                                remainingDueToMe = merchant.remainingDueToMe + record.settlementAmount
                            )
                        )
                    }
                }
            }
        }

        retDao.updateReturnStatus(returnId, "CANCELLED")
        return Result.success(Unit)
    }

    fun exportReturnsToCsv(returns: List<ReturnWithItems>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar"))
        val sb = StringBuilder()
        sb.append("تقرير سجل المرتجعات - البشيري للأجهزة الكهربائية\n")
        sb.append("تاريخ التصدير: ").append(dateFormat.format(Date())).append("\n\n")
        sb.append("رقم المرتجع,النوع,التاريخ والوقت,الطرف المعني,رقم الهاتف,الفاتورة الأصلية,عدد الأصناف,إجمالي القيمة,نوع التسوية,المبلغ المسوى,الحالة,السبب,المسؤول\n")

        returns.forEach { item ->
            val rec = item.returnRecord
            val dateStr = dateFormat.format(Date(rec.timestamp))
            val typeStr = when (rec.type) {
                "CUSTOMER_TO_STORE" -> "عميل -> معرض"
                "STORE_TO_SUPPLIER" -> "معرض -> مورد"
                "MERCHANT_TO_STORE" -> "تاجر -> معرض"
                "STORE_TO_MERCHANT" -> "معرض -> تاجر"
                else -> rec.type
            }
            val statusStr = if (rec.status == "APPROVED") "معتمد" else "ملغي"
            val settlementStr = when (rec.settlementType) {
                "CASH_REFUND" -> "استرداد نقدي"
                "CUSTOMER_CREDIT" -> "رصيد حساب عميل"
                "EXCHANGE" -> "استبدال بضاعة"
                "DECREASE_DEBT" -> "خصم مديونية مورد"
                "DECREASE_MERCHANT_DUE" -> "خصم مستحقات تاجر"
                "CASH_REFUND_FROM_SUPPLIER" -> "استرداد نقدي من مورد"
                else -> rec.settlementType
            }
            sb.append("\"${rec.returnNumber}\",\"$typeStr\",\"$dateStr\",\"${rec.partyName}\",\"${rec.partyPhone}\",\"${rec.originalInvoiceNumber}\",${item.items.size},${rec.totalAmount},\"$settlementStr\",${rec.settlementAmount},\"$statusStr\",\"${rec.reason}\",\"${rec.createdByName}\"\n")
        }
        return sb.toString()
    }

    // --- Damaged, Scrap & Repair Operations («الهالك والتالف والإصلاح») ---

    suspend fun registerDamageOrScrap(
        userRole: String,
        userId: String,
        userName: String,
        modelId: Long,
        quantity: Int,
        reason: String,
        damageDetails: String = "",
        initialStatus: String, // "TOTAL_SCRAP", "UNDER_INSPECTION", "REPAIRABLE"
        sourceType: String = "INVENTORY", // "INVENTORY", "CUSTOMER_RETURN"
        returnRecordId: Long? = null,
        returnNumber: String = "",
        unitCost: Double? = null,
        totalLossAmount: Double? = null,
        imageUri: String = "",
        notes: String = ""
    ): Result<DamageRepairRecordEntity> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("تسجيل واعتماد الهالك والتالف مقتصر على المدير فقط"))
        }

        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("الكمية يجب أن تكون أكبر من صفر"))
        }

        val dao = damageRepairDao ?: return Result.failure(IllegalStateException("خدمة الهالك والتالف غير مفعلة"))
        val liveModel = catalogDao.getModelById(modelId)
            ?: return Result.failure(IllegalArgumentException("المنتج المحدد غير مسجل بالمخزون"))

        // If from inventory, ensure we don't deduct more than available!
        val shouldDeductStock = sourceType == "INVENTORY"
        if (shouldDeductStock) {
            if (liveModel.stockQuantity < quantity) {
                return Result.failure(
                    IllegalArgumentException(
                        "الكمية المطلوب تسجيلها ($quantity) أكبر من الرصيد المتاح بالمخزن (${liveModel.stockQuantity})"
                    )
                )
            }
        }

        val finalUnitCost = unitCost ?: liveModel.minAllowedPrice
        val finalLossAmount = totalLossAmount ?: (finalUnitCost * quantity)
        val recordNumber = "DMR-" + System.currentTimeMillis().toString().takeLast(6)
        val timestamp = System.currentTimeMillis()

        // Deduct from sellable stock ONCE if from inventory
        if (shouldDeductStock) {
            val beforeStock = liveModel.stockQuantity
            val afterStock = beforeStock - quantity
            catalogDao.deductStock(modelId, quantity)
            logStockMovement(
                movementType = "DAMAGE_SCRAP",
                modelId = modelId,
                productName = liveModel.productName,
                modelName = liveModel.modelName,
                code = liveModel.code,
                categoryName = liveModel.categoryName,
                brandName = liveModel.brandName,
                quantityChanged = -quantity,
                quantityBefore = beforeStock,
                quantityAfter = afterStock,
                unitCostPrice = finalUnitCost,
                referenceType = "DAMAGE",
                referenceNumber = recordNumber,
                userId = userId,
                userName = userName,
                notes = "تسجيل هالك وتالف ($reason): $damageDetails"
            )
        }

        val record = DamageRepairRecordEntity(
            recordNumber = recordNumber,
            modelId = modelId,
            productName = liveModel.productName,
            modelName = liveModel.modelName,
            categoryName = liveModel.categoryName,
            brandName = liveModel.brandName,
            quantity = quantity,
            reason = reason,
            damageDetails = damageDetails.trim(),
            status = initialStatus,
            sourceType = sourceType,
            returnRecordId = returnRecordId,
            returnNumber = returnNumber.trim(),
            unitCost = finalUnitCost,
            totalLossAmount = finalLossAmount,
            imageUri = imageUri.trim(),
            notes = notes.trim(),
            registeredByUserId = userId,
            registeredByName = userName,
            registeredTimestamp = timestamp,
            approvedByUserId = userId,
            approvedByName = userName,
            approvedTimestamp = timestamp,
            isStockDeducted = shouldDeductStock,
            isStockRestored = false
        )

        val id = dao.insertRecord(record)
        return Result.success(record.copy(id = id))
    }

    suspend fun sendToRepair(
        recordId: Long,
        repairReason: String,
        technicianNotes: String,
        userRole: String,
        userId: String,
        userName: String
    ): Result<DamageRepairRecordEntity> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("تحويل المنتج للإصلاح مقتصر على المدير فقط"))
        }
        val dao = damageRepairDao ?: return Result.failure(IllegalStateException("خدمة الهالك والتالف غير مفعلة"))
        val current = dao.getRecordById(recordId)
            ?: return Result.failure(IllegalArgumentException("السجل غير موجود"))

        if (current.status == "RESTORED_TO_STOCK") {
            return Result.failure(IllegalStateException("المنتج تم إعادته للمخزون مسبقًا"))
        }

        val updated = current.copy(
            status = "UNDER_REPAIR",
            repairNotes = if (technicianNotes.isNotBlank()) "${current.repairNotes}\n$technicianNotes".trim() else current.repairNotes,
            sentToRepairDate = System.currentTimeMillis()
        )
        dao.updateRecord(updated)
        return Result.success(updated)
    }

    suspend fun completeRepair(
        recordId: Long,
        outcome: String, // "FIXED", "CANNOT_BE_FIXED", "PARTIALLY_FIXED"
        sparePartsCost: Double,
        laborCost: Double,
        repairNotes: String,
        userRole: String,
        userId: String,
        userName: String
    ): Result<DamageRepairRecordEntity> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("تسجيل نتيجة الإصلاح مقتصر على المدير فقط"))
        }
        val dao = damageRepairDao ?: return Result.failure(IllegalStateException("خدمة الهالك والتالف غير مفعلة"))
        val current = dao.getRecordById(recordId)
            ?: return Result.failure(IllegalArgumentException("السجل غير موجود"))

        val totalRepairCost = (sparePartsCost + laborCost).coerceAtLeast(0.0)
        val newStatus = if (outcome == "CANNOT_BE_FIXED") {
            "TOTAL_SCRAP"
        } else {
            "REPAIRED_PENDING_APPROVAL"
        }

        val updated = current.copy(
            status = newStatus,
            repairOutcome = outcome,
            sparePartsCost = sparePartsCost,
            laborCost = laborCost,
            totalRepairCost = totalRepairCost,
            repairNotes = if (repairNotes.isNotBlank()) "${current.repairNotes}\n$repairNotes".trim() else current.repairNotes,
            repairCompletedDate = System.currentTimeMillis()
        )
        dao.updateRecord(updated)
        return Result.success(updated)
    }

    suspend fun approveReturnToStock(
        recordId: Long,
        userRole: String,
        userId: String,
        userName: String
    ): Result<DamageRepairRecordEntity> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("اعتماد عودة المنتج للمخزون مقتصر على المدير فقط"))
        }
        val dao = damageRepairDao ?: return Result.failure(IllegalStateException("خدمة الهالك والتالف غير مفعلة"))
        val current = dao.getRecordById(recordId)
            ?: return Result.failure(IllegalArgumentException("السجل غير موجود"))

        if (current.status != "REPAIRED_PENDING_APPROVAL") {
            return Result.failure(IllegalStateException("المنتج ليس بحالة «تم إصلاحه وينتظر الاعتماد»"))
        }

        if (current.isStockRestored) {
            return Result.failure(IllegalStateException("تمت إضافة المنتج للمخزون مسبقًا، لا يمكن تكرار الإضافة"))
        }

        // Add back to sellable stock EXACTLY ONCE
        val liveModel = catalogDao.getModelById(current.modelId)
        val beforeStock = liveModel?.stockQuantity ?: 0
        val afterStock = beforeStock + current.quantity
        catalogDao.addStock(current.modelId, current.quantity)
        logStockMovement(
            movementType = "REPAIR_RESTORE",
            modelId = current.modelId,
            productName = current.productName,
            modelName = current.modelName,
            code = liveModel?.code ?: "",
            categoryName = current.categoryName,
            brandName = current.brandName,
            quantityChanged = current.quantity,
            quantityBefore = beforeStock,
            quantityAfter = afterStock,
            unitCostPrice = current.unitCost,
            referenceType = "DAMAGE",
            referenceNumber = current.recordNumber,
            userId = userId,
            userName = userName,
            notes = "إعادة للمخزون الصالح بعد اكتمال الصيانة واعتماد المدير"
        )

        val updated = current.copy(
            status = "RESTORED_TO_STOCK",
            isStockRestored = true,
            restoredToStockDate = System.currentTimeMillis(),
            approvedByUserId = userId,
            approvedByName = userName,
            approvedTimestamp = System.currentTimeMillis()
        )
        dao.updateRecord(updated)
        return Result.success(updated)
    }

    fun exportDamageRepairCsv(records: List<DamageRepairRecordEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar"))
        val sb = StringBuilder()
        sb.append("تقرير سجل الهالك والتالف والإصلاح - البشيري للأجهزة الكهربائية\n")
        sb.append("تاريخ التصدير: ").append(dateFormat.format(Date())).append("\n\n")
        sb.append("رقم السجل,اسم المنتج,الموديل,الكمية,الحالة,السبب,المصدر,تكلفة الوحدة,إجمالي الخسارة,تكلفة قطع الغيار,تكلفة المصنعية,إجمالي تكلفة الإصلاح,تاريخ التسجيل,المسؤول,تاريخ العودة للمخزون,الملاحظات\n")

        records.forEach { r ->
            val dateStr = dateFormat.format(Date(r.registeredTimestamp))
            val restoredStr = if (r.restoredToStockDate != null) dateFormat.format(Date(r.restoredToStockDate)) else "-"
            val statusArabic = when (r.status) {
                "TOTAL_SCRAP" -> "تالف نهائيًا"
                "UNDER_INSPECTION" -> "تحت الفحص"
                "REPAIRABLE" -> "قابل للإصلاح"
                "UNDER_REPAIR" -> "تحت الإصلاح"
                "REPAIRED_PENDING_APPROVAL" -> "تم الإصلاح بانتظار الاعتماد"
                "RESTORED_TO_STOCK" -> "تم اعتماده وأعيد للبيع"
                else -> r.status
            }
            val sourceArabic = if (r.sourceType == "CUSTOMER_RETURN") "مرتجع عميل (#${r.returnNumber})" else "المخزون"
            sb.append("\"${r.recordNumber}\",\"${r.productName}\",\"${r.modelName}\",${r.quantity},\"$statusArabic\",\"${r.reason}\",\"$sourceArabic\",${r.unitCost},${r.totalLossAmount},${r.sparePartsCost},${r.laborCost},${r.totalRepairCost},\"$dateStr\",\"${r.registeredByName}\",\"$restoredStr\",\"${r.notes}\"\n")
        }
        return sb.toString()
    }

    // --- Inbound Stock Receiving from Suppliers & Merchants ---
    suspend fun receiveStock(
        modelId: Long,
        quantity: Int,
        unitCostPrice: Double,
        supplierId: Long? = null,
        merchantId: Long? = null,
        supplierOrMerchantName: String = "",
        purchaseInvoiceNumber: String = "",
        notes: String = "",
        userRole: String,
        userId: String,
        userName: String
    ): Result<ProductModelEntity> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("تسجيل وإضافة بضاعة واردة مقتصر على المدير فقط"))
        }
        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("الكمية الواردة يجب أن تكون أكبر من صفر"))
        }

        val liveModel = catalogDao.getModelById(modelId)
            ?: return Result.failure(IllegalArgumentException("المنتج المحدد غير مسجل بالمخزون"))

        val beforeStock = liveModel.stockQuantity
        val afterStock = beforeStock + quantity
        catalogDao.addStock(modelId, quantity)

        logStockMovement(
            movementType = "PURCHASE",
            modelId = modelId,
            productName = liveModel.productName,
            modelName = liveModel.modelName,
            code = liveModel.code,
            categoryName = liveModel.categoryName,
            brandName = liveModel.brandName,
            quantityChanged = quantity,
            quantityBefore = beforeStock,
            quantityAfter = afterStock,
            unitCostPrice = unitCostPrice,
            referenceType = "SUPPLIER_PURCHASE",
            referenceNumber = purchaseInvoiceNumber.trim(),
            userId = userId,
            userName = userName,
            notes = "توريد وارد: $supplierOrMerchantName | ${notes.trim()}".trim()
        )

        // Optionally record in ledger
        if (supplierId != null) {
            val totalPurchaseAmount = quantity * unitCostPrice
            accountsDao.insertSupplierTransaction(
                SupplierTransactionEntity(
                    supplierId = supplierId,
                    type = "PURCHASE",
                    amount = totalPurchaseAmount,
                    notes = "فاتورة شراء #$purchaseInvoiceNumber ($quantity قطع ${liveModel.modelName})"
                )
            )
            val supplier = accountsDao.getSupplierById(supplierId)
            if (supplier != null) {
                accountsDao.updateSupplier(
                    supplier.copy(
                        totalPurchases = supplier.totalPurchases + totalPurchaseAmount,
                        remainingBalance = supplier.remainingBalance + totalPurchaseAmount
                    )
                )
            }
            val totalMerchantAmount = quantity * unitCostPrice
            accountsDao.insertMerchantTransaction(
                MerchantTransactionEntity(
                    merchantId = merchantId!!,
                    type = "RECEIVED",
                    amount = totalMerchantAmount,
                    notes = "توريد من تاجر #$purchaseInvoiceNumber ($quantity قطع ${liveModel.modelName})"
                )
            )
        }

        val updated = catalogDao.getModelById(modelId) ?: liveModel.copy(stockQuantity = afterStock)
        return Result.success(updated)
    }

    // --- Physical Inventory Audits ---
    suspend fun createInventoryAudit(
        title: String,
        scopeType: String = "ALL", // "ALL", "CATEGORY", "BRAND", "CUSTOM"
        scopeFilter: String = "",
        customModelIds: List<Long> = emptyList(),
        notes: String = "",
        userRole: String,
        userId: String,
        userName: String
    ): Result<InventoryAuditWithItems> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("إنشاء الجرد مقتصر على المدير فقط"))
        }
        val dao = inventoryAuditDao ?: return Result.failure(IllegalStateException("خدمة الجرد غير مفعلة"))

        val allModels = catalogDao.getAllModels().first()
        val targetedModels = when (scopeType) {
            "CATEGORY" -> allModels.filter { it.categoryName.trim() == scopeFilter.trim() }
            "BRAND" -> allModels.filter { it.brandName.trim() == scopeFilter.trim() }
            "CUSTOM" -> allModels.filter { it.id in customModelIds }
            else -> allModels
        }

        if (targetedModels.isEmpty()) {
            return Result.failure(IllegalArgumentException("لا توجد منتجات ضمن نطاق الجرد المحدد"))
        }

        val auditNumber = "AUD-" + System.currentTimeMillis().toString().takeLast(6)
        val auditEntity = InventoryAuditEntity(
            auditNumber = auditNumber,
            title = if (title.isNotBlank()) title.trim() else "جرد $auditNumber",
            scopeType = scopeType,
            scopeFilter = scopeFilter.trim(),
            startTimestamp = System.currentTimeMillis(),
            status = "DRAFT",
            notes = notes.trim(),
            createdByUserId = userId,
            createdByName = userName
        )

        val auditId = dao.insertAudit(auditEntity)
        val items = targetedModels.map { m ->
            InventoryAuditItemEntity(
                auditId = auditId,
                modelId = m.id,
                productName = m.productName,
                modelName = m.modelName,
                code = m.code,
                categoryName = m.categoryName,
                brandName = m.brandName,
                costPrice = m.minAllowedPrice,
                systemQuantity = m.stockQuantity,
                actualQuantity = m.stockQuantity,
                differenceQuantity = 0,
                varianceValue = 0.0,
                status = "MATCH",
                notes = "",
                isSettled = false
            )
        }
        dao.insertAuditItems(items)

        val auditWithItems = dao.getAuditById(auditId)
            ?: return Result.failure(IllegalStateException("تعذر استرجاع جلسة الجرد"))
        return Result.success(auditWithItems)
    }

    suspend fun updateAuditItemActualQuantity(
        itemId: Long,
        actualQuantity: Int,
        notes: String = "",
        userRole: String = "ADMIN"
    ): Result<Unit> {
        val dao = inventoryAuditDao ?: return Result.failure(IllegalStateException("خدمة الجرد غير مفعلة"))
        val item = dao.getAuditItemById(itemId) ?: return Result.failure(IllegalArgumentException("بند الجرد غير موجود"))
        return updateAuditItemActualQuantity(item.auditId, itemId, actualQuantity, notes, userRole)
    }

    suspend fun updateAuditItemActualQuantity(
        auditId: Long,
        itemId: Long,
        actualQuantity: Int,
        notes: String = "",
        userRole: String
    ): Result<Unit> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("تعديل أرقام الجرد مقتصر على المدير فقط"))
        }
        val dao = inventoryAuditDao ?: return Result.failure(IllegalStateException("خدمة الجرد غير مفعلة"))
        val auditWithItems = dao.getAuditById(auditId)
            ?: return Result.failure(IllegalArgumentException("جلسة الجرد غير موجودة"))

        if (auditWithItems.audit.isSettled) {
            return Result.failure(IllegalStateException("لا يمكن تعديل جرد تم اعتماده وتسويته مسبقًا"))
        }

        val currentItem = auditWithItems.items.firstOrNull { it.id == itemId }
            ?: return Result.failure(IllegalArgumentException("بند الجرد غير موجود"))

        val diff = actualQuantity - currentItem.systemQuantity
        val status = when {
            diff > 0 -> "SURPLUS"
            diff < 0 -> "DEFICIT"
            else -> "MATCH"
        }
        val varianceVal = diff * currentItem.costPrice

        val updatedItem = currentItem.copy(
            actualQuantity = actualQuantity,
            differenceQuantity = diff,
            varianceValue = varianceVal,
            status = status,
            notes = if (notes.isNotBlank()) notes.trim() else currentItem.notes
        )
        dao.updateAuditItem(updatedItem)

        // Recalculate audit summary
        val allItems = dao.getAuditItems(auditId)
        val totalCounted = allItems.size
        val matchedCount = allItems.count { it.status == "MATCH" }
        val deficitCount = allItems.count { it.status == "DEFICIT" }
        val surplusCount = allItems.count { it.status == "SURPLUS" }
        val netQty = allItems.sumOf { it.differenceQuantity }
        val netVal = allItems.sumOf { it.varianceValue }

        dao.updateAudit(
            auditWithItems.audit.copy(
                totalItemsCounted = totalCounted,
                totalMatchedItems = matchedCount,
                totalDeficitItems = deficitCount,
                totalSurplusItems = surplusCount,
                netQuantityVariance = netQty,
                netValueVariance = netVal
            )
        )
        return Result.success(Unit)
    }

    suspend fun settleInventoryAudit(
        auditId: Long,
        settlementReason: String = "",
        userRole: String,
        userId: String,
        userName: String
    ): Result<InventoryAuditWithItems> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("اعتماد وتسوية فروق الجرد مقتصر على المدير فقط"))
        }
        val dao = inventoryAuditDao ?: return Result.failure(IllegalStateException("خدمة الجرد غير مفعلة"))
        val auditWithItems = dao.getAuditById(auditId)
            ?: return Result.failure(IllegalArgumentException("جلسة الجرد غير موجودة"))

        if (auditWithItems.audit.isSettled) {
            return Result.failure(IllegalStateException("تمت تسوية هذا الجرد واعتماده مسبقًا، لا يمكن تكرار التسوية"))
        }

        // Apply variances to catalog models
        val settledItems = mutableListOf<InventoryAuditItemEntity>()
        for (item in auditWithItems.items) {
            if (item.differenceQuantity != 0) {
                val liveModel = catalogDao.getModelById(item.modelId)
                val beforeStock = liveModel?.stockQuantity ?: item.systemQuantity
                val afterStock = item.actualQuantity

                // Set model stock to the actual counted quantity
                catalogDao.updateStock(item.modelId, item.actualQuantity)

                // Log movement for this model
                val movementType = if (item.differenceQuantity > 0) "INVENTORY_AUDIT_SURPLUS" else "INVENTORY_AUDIT_DEFICIT"
                logStockMovement(
                    movementType = movementType,
                    modelId = item.modelId,
                    productName = item.productName,
                    modelName = item.modelName,
                    code = item.code,
                    categoryName = item.categoryName,
                    brandName = item.brandName,
                    quantityChanged = item.differenceQuantity,
                    quantityBefore = beforeStock,
                    quantityAfter = afterStock,
                    unitCostPrice = item.costPrice,
                    referenceType = "AUDIT",
                    referenceNumber = auditWithItems.audit.auditNumber,
                    referenceId = auditId,
                    userId = userId,
                    userName = userName,
                    notes = "تسوية جرد #${auditWithItems.audit.auditNumber}: ${if (item.differenceQuantity > 0) "زيادة" else "عجز"} ${item.differenceQuantity} قطع | $settlementReason"
                )
                settledItems.add(item.copy(isSettled = true))
            } else {
                settledItems.add(item.copy(isSettled = true))
            }
        }
        dao.updateAuditItems(settledItems)

        val updatedAudit = auditWithItems.audit.copy(
            status = "SETTLED",
            isSettled = true,
            settledTimestamp = System.currentTimeMillis(),
            completedTimestamp = System.currentTimeMillis(),
            approvedByUserId = userId,
            approvedByName = userName,
            notes = if (settlementReason.isNotBlank()) "${auditWithItems.audit.notes}\nسبب التسوية: $settlementReason".trim() else auditWithItems.audit.notes
        )
        dao.updateAudit(updatedAudit)

        return Result.success(dao.getAuditById(auditId) ?: auditWithItems.copy(audit = updatedAudit, items = settledItems))
    }

    suspend fun cancelInventoryAudit(
        auditId: Long,
        userRole: String
    ): Result<Unit> {
        if (userRole != "ADMIN") {
            return Result.failure(IllegalStateException("إلغاء الجرد مقتصر على المدير فقط"))
        }
        val dao = inventoryAuditDao ?: return Result.failure(IllegalStateException("خدمة الجرد غير مفعلة"))
        val auditWithItems = dao.getAuditById(auditId)
            ?: return Result.failure(IllegalArgumentException("جلسة الجرد غير موجودة"))

        if (auditWithItems.audit.isSettled) {
            return Result.failure(IllegalStateException("لا يمكن إلغاء جرد تم اعتماده وتسويته بالمخزن"))
        }

        dao.updateAudit(auditWithItems.audit.copy(status = "CANCELLED"))
        return Result.success(Unit)
    }

    // --- Reports & CSV Exports ---
    fun exportStockMovementsCsv(movements: List<StockMovementEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar"))
        val sb = StringBuilder()
        sb.append("سجل حركة المخزون الشامل - البشيري للأجهزة الكهربائية\n")
        sb.append("تاريخ التصدير: ").append(dateFormat.format(Date())).append("\n\n")
        sb.append("التاريخ والوقت,نوع الحركة,كود الصنف,اسم المنتج,الموديل,التصنيف,الشركة,الرصيد قبل,حجم الحركة,الرصيد بعد,المستند المرجعي,المسؤول,الملاحظات\n")

        movements.forEach { m ->
            val dateStr = dateFormat.format(Date(m.timestamp))
            val typeArabic = when (m.movementType) {
                "PURCHASE" -> "شراء / توريد وارد"
                "SALE" -> "بيع / فاتورة"
                "RETURN_CUSTOMER" -> "مرتجع من عميل"
                "RETURN_SUPPLIER" -> "مرتجع إلى مورد"
                "DAMAGE_SCRAP" -> "هالك وتالف"
                "REPAIR_RESTORE" -> "إعادة بعد الإصلاح"
                "INVENTORY_AUDIT_SURPLUS" -> "تسوية جرد (زيادة)"
                "INVENTORY_AUDIT_DEFICIT" -> "تسوية جرد (عجز)"
                "MANUAL_ADJUSTMENT" -> "تعديل يدوي"
                else -> m.movementType
            }
            val refStr = if (m.referenceNumber.isNotBlank()) "${m.referenceType}: ${m.referenceNumber}" else "-"
            sb.append("\"$dateStr\",\"$typeArabic\",\"${m.code}\",\"${m.productName}\",\"${m.modelName}\",\"${m.categoryName}\",\"${m.brandName}\",${m.quantityBefore},${m.quantityChanged},${m.quantityAfter},\"$refStr\",\"${m.userName}\",\"${m.notes}\"\n")
        }
        return sb.toString()
    }

    fun exportAuditVarianceCsv(auditWithItems: InventoryAuditWithItems): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar"))
        val audit = auditWithItems.audit
        val sb = StringBuilder()
        sb.append("تقرير نتائج وفروق الجرد الفعلي (#${audit.auditNumber}) - البشيري للأجهزة الكهربائية\n")
        sb.append("عنوان الجرد: ").append(audit.title).append("\n")
        sb.append("النطاق: ").append(audit.scopeType).append(if (audit.scopeFilter.isNotBlank()) " (${audit.scopeFilter})" else "").append("\n")
        sb.append("الحالة: ").append(if (audit.isSettled) "معتمد وتمت التسوية" else "مسودة / قيد المراجعة").append("\n")
        sb.append("تاريخ البدء: ").append(dateFormat.format(Date(audit.startTimestamp))).append("\n")
        if (audit.settledTimestamp != null) {
            sb.append("تاريخ الاعتماد: ").append(dateFormat.format(Date(audit.settledTimestamp))).append(" بواسطة: ").append(audit.approvedByName).append("\n")
        }
        sb.append("\nكود الصنف,اسم المنتج,الموديل,التصنيف,الشركة,الرصيد الدفتري,الرصيد الفعلي,الفرق,الحالة,سعر التكلفة,قيمة الفرق (ج.م),ملاحظات\n")

        auditWithItems.items.forEach { item ->
            val statusArabic = when (item.status) {
                "SURPLUS" -> "زيادة"
                "DEFICIT" -> "عجز"
                else -> "مطابق"
            }
            sb.append("\"${item.code}\",\"${item.productName}\",\"${item.modelName}\",\"${item.categoryName}\",\"${item.brandName}\",${item.systemQuantity},${item.actualQuantity},${item.differenceQuantity},\"$statusArabic\",${item.costPrice},${item.varianceValue},\"${item.notes}\"\n")
        }

        sb.append("\nإجمالي الأصناف: ${audit.totalItemsCounted}, أصناف متطابقة: ${audit.totalMatchedItems}, أصناف بها عجز: ${audit.totalDeficitItems}, أصناف بها زيادة: ${audit.totalSurplusItems}\n")
        sb.append("صافي فرق الكميات: ${audit.netQuantityVariance}, صافي القيمة المالية للفروق: ${audit.netValueVariance} ج.م\n")
        return sb.toString()
    }

    fun exportCurrentStockValuationCsv(models: List<ProductModelEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar"))
        val sb = StringBuilder()
        sb.append("تقرير جرد الرصيد الفعلي الحالي وتقييم المخزون - البشيري للأجهزة الكهربائية\n")
        sb.append("تاريخ التصدير: ").append(dateFormat.format(Date())).append("\n\n")
        sb.append("كود الصنف,اسم المنتج,الموديل,التصنيف,الشركة,الرصيد الحالي,سعر التكلفة (ج.م),سعر البيع الرسمي (ج.م),إجمالي قيمة التكلفة (ج.م),إجمالي قيمة البيع (ج.م)\n")

        var totalQty = 0
        var totalCostVal = 0.0
        var totalSaleVal = 0.0

        models.forEach { m ->
            val costVal = m.stockQuantity * m.minAllowedPrice
            val saleVal = m.stockQuantity * m.finalPrice
            totalQty += m.stockQuantity
            totalCostVal += costVal
            totalSaleVal += saleVal
            sb.append("\"${m.code}\",\"${m.productName}\",\"${m.modelName}\",\"${m.categoryName}\",\"${m.brandName}\",${m.stockQuantity},${m.minAllowedPrice},${m.finalPrice},$costVal,$saleVal\n")
        }
        sb.append("\nالإجمالي العام: عدد القطع بالمخزن = $totalQty, إجمالي رأس مال البضاعة بالتكلفة = $totalCostVal ج.م, إجمالي القيمة بسعر البيع = $totalSaleVal ج.م\n")
        return sb.toString()
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
        val txId = accountsDao.insertSupplierTransaction(
            SupplierTransactionEntity(
                supplierId = supplierId,
                type = type,
                amount = amount,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )

        // Automatic Treasury cash tracking for supplier payments
        if (type == "PAYMENT" && amount > 0) {
            recordTreasuryTransaction(
                type = "CASH_OUT",
                category = "SUPPLIER_PAYMENT",
                amount = amount,
                referenceType = "SUPPLIER_TX",
                referenceId = txId,
                referenceNumber = "SUP-$supplierId",
                partyType = "SUPPLIER",
                partyName = current.supplierName,
                paymentMethod = "CASH",
                description = "سداد دفعة نقدية للمورد: ${current.supplierName}${if (notes.isNotBlank()) " ($notes)" else ""}",
                userId = "admin",
                userName = "المدير"
            )
        }
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
        val txId = accountsDao.insertMerchantTransaction(
            MerchantTransactionEntity(
                merchantId = merchantId,
                type = type,
                amount = amount,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )

        // Automatic Treasury cash tracking for merchant collections
        if (type == "COLLECTION" && amount > 0) {
            recordTreasuryTransaction(
                type = "CASH_IN",
                category = "MERCHANT_COLLECTION",
                amount = amount,
                referenceType = "MERCHANT_TX",
                referenceId = txId,
                referenceNumber = "MERCH-$merchantId",
                partyType = "MERCHANT",
                partyName = current.merchantName,
                paymentMethod = "CASH",
                description = "تحصيل دفعة نقدية من التاجر: ${current.merchantName}${if (notes.isNotBlank()) " ($notes)" else ""}",
                userId = "admin",
                userName = "المدير"
            )
        }
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
        val txId = accountsDao.insertCustomerTransaction(
            CustomerTransactionEntity(
                customerId = customerId,
                type = type,
                amount = amount,
                productDescription = productDescription,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )

        // Automatic Treasury cash tracking for customer collections
        if (type == "COLLECTION" && amount > 0) {
            recordTreasuryTransaction(
                type = "CASH_IN",
                category = "CUSTOMER_COLLECTION",
                amount = amount,
                referenceType = "CUSTOMER_TX",
                referenceId = txId,
                referenceNumber = "CUST-$customerId",
                partyType = "CUSTOMER",
                partyName = current.customerName,
                paymentMethod = "CASH",
                description = "تحصيل دفعة نقدية من العميل: ${current.customerName}${if (notes.isNotBlank()) " ($notes)" else ""}",
                userId = "admin",
                userName = "المدير"
            )
        }
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

    // --- Financial Management & Cash Treasury System ---

    suspend fun recordTreasuryTransaction(
        type: String, // "CASH_IN", "CASH_OUT"
        category: String, // "SALE_CASH", "CUSTOMER_COLLECTION", "MERCHANT_COLLECTION", "SUPPLIER_PAYMENT", "EXPENSE", "CAPITAL_INJECTION", "TRANSFER", "ADJUSTMENT", "RETURN_REFUND"
        amount: Double,
        referenceType: String = "",
        referenceId: Long? = null,
        referenceNumber: String = "",
        partyType: String = "",
        partyName: String = "",
        paymentMethod: String = "CASH",
        description: String = "",
        userId: String = "admin",
        userName: String = "المدير"
    ): Result<TreasuryTransactionEntity> {
        val dao = financialDao ?: return Result.failure(Exception("خدمة الخزينة المالية غير مفعلة"))
        if (amount <= 0.0) {
            return Result.failure(IllegalArgumentException("المبلغ يجب أن يكون أكبر من الصفر"))
        }

        // Idempotency check: don't double record for the same reference
        if (referenceId != null && referenceType.isNotBlank()) {
            val existing = dao.getActiveTransactionByReference(referenceType, referenceId)
            if (existing != null) {
                return Result.success(existing)
            }
        }

        val latest = dao.getLatestTreasuryTransaction()
        val currentBalance = latest?.balanceAfter ?: 0.0
        val newBalance = if (type == "CASH_IN") currentBalance + amount else currentBalance - amount

        val tx = TreasuryTransactionEntity(
            timestamp = System.currentTimeMillis(),
            type = type,
            category = category,
            amount = amount,
            balanceAfter = newBalance,
            referenceType = referenceType,
            referenceId = referenceId,
            referenceNumber = referenceNumber,
            partyType = partyType,
            partyName = partyName.trim(),
            paymentMethod = paymentMethod,
            description = description.trim(),
            userId = userId,
            userName = userName
        )
        val id = dao.insertTreasuryTransaction(tx)
        return Result.success(tx.copy(id = id))
    }

    suspend fun reverseTreasuryTransaction(
        transactionId: Long,
        reversalReason: String,
        userId: String = "admin",
        userName: String = "المدير"
    ): Result<TreasuryTransactionEntity> {
        val dao = financialDao ?: return Result.failure(Exception("خدمة الخزينة المالية غير مفعلة"))
        val original = dao.getTreasuryTransactionById(transactionId)
            ?: return Result.failure(IllegalArgumentException("حركة الخزينة غير موجودة"))

        if (original.isReversed) {
            return Result.failure(IllegalStateException("هذه الحركة ملغاة بالفعل بحركة عكسية سابقة"))
        }

        val latest = dao.getLatestTreasuryTransaction()
        val currentBalance = latest?.balanceAfter ?: 0.0

        // Compensating entry: reverse CASH_IN -> CASH_OUT, reverse CASH_OUT -> CASH_IN
        val compType = if (original.type == "CASH_IN") "CASH_OUT" else "CASH_IN"
        val newBalance = if (compType == "CASH_IN") currentBalance + original.amount else currentBalance - original.amount

        val compensatingTx = TreasuryTransactionEntity(
            timestamp = System.currentTimeMillis(),
            type = compType,
            category = "ADJUSTMENT",
            amount = original.amount,
            balanceAfter = newBalance,
            referenceType = "COMPENSATING",
            referenceId = original.id,
            referenceNumber = "REV-#${original.id}",
            partyType = original.partyType,
            partyName = original.partyName,
            paymentMethod = original.paymentMethod,
            description = "حركة عكسية تصحيحية لإلغاء حركة رقم #${original.id}: $reversalReason",
            userId = userId,
            userName = userName
        )
        val compId = dao.insertTreasuryTransaction(compensatingTx)

        // Mark original as reversed
        dao.updateTreasuryTransaction(
            original.copy(
                isReversed = true,
                reversalNotes = reversalReason.trim(),
                reversalTransactionId = compId
            )
        )

        return Result.success(compensatingTx.copy(id = compId))
    }

    suspend fun recordExpense(
        categoryName: String,
        amount: Double,
        description: String,
        beneficiary: String = "",
        paymentMethod: String = "CASH_TREASURY", // "CASH_TREASURY", "BANK", "WALLET"
        referenceNumber: String = "",
        userId: String = "admin",
        userName: String = "المدير",
        notes: String = ""
    ): Result<ExpenseEntity> {
        val dao = financialDao ?: return Result.failure(Exception("خدمة الحسابات المالية غير مفعلة"))
        if (amount <= 0.0) {
            return Result.failure(IllegalArgumentException("مبلغ المصروف يجب أن يكون أكبر من الصفر"))
        }
        if (categoryName.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى اختيار بند المصروف"))
        }
        if (description.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى كتابة بيان وتفاصيل المصروف"))
        }

        val expenseNumber = "EXP-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(Date()) + "-" + (100..999).random()
        val expense = ExpenseEntity(
            expenseNumber = expenseNumber,
            categoryName = categoryName.trim(),
            amount = amount,
            date = System.currentTimeMillis(),
            description = description.trim(),
            beneficiary = beneficiary.trim(),
            paymentMethod = paymentMethod,
            referenceNumber = referenceNumber.trim(),
            createdByUserId = userId,
            createdByUserName = userName,
            isApproved = true,
            notes = notes.trim()
        )
        val expenseId = dao.insertExpense(expense)
        val savedExpense = expense.copy(id = expenseId)

        // If paid from Treasury cash, automatically deduct from cash register
        if (paymentMethod == "CASH_TREASURY") {
            recordTreasuryTransaction(
                type = "CASH_OUT",
                category = "EXPENSE",
                amount = amount,
                referenceType = "EXPENSE",
                referenceId = expenseId,
                referenceNumber = expenseNumber,
                partyType = "EXPENSE",
                partyName = if (beneficiary.isNotBlank()) beneficiary.trim() else categoryName.trim(),
                paymentMethod = "CASH",
                description = "مصروف: ${description.trim()} ($categoryName)",
                userId = userId,
                userName = userName
            )
        }

        return Result.success(savedExpense)
    }

    suspend fun reverseExpense(
        expenseId: Long,
        reversalReason: String,
        userId: String = "admin",
        userName: String = "المدير"
    ): Result<Unit> {
        val dao = financialDao ?: return Result.failure(Exception("خدمة الحسابات المالية غير مفعلة"))
        val expense = dao.getExpenseById(expenseId) ?: return Result.failure(IllegalArgumentException("المصروف غير موجود"))
        if (expense.isReversed) {
            return Result.failure(IllegalStateException("المصروف ملغى بالفعل بحركة عكسية"))
        }

        dao.updateExpense(
            expense.copy(
                isReversed = true,
                notes = if (expense.notes.isNotBlank()) "${expense.notes} | تم الإلغاء: $reversalReason" else "تم الإلغاء بحركة عكسية: $reversalReason"
            )
        )

        if (expense.paymentMethod == "CASH_TREASURY") {
            val treasuryTx = dao.getActiveTransactionByReference("EXPENSE", expenseId)
            if (treasuryTx != null) {
                reverseTreasuryTransaction(treasuryTx.id, "إلغاء مصروف ${expense.expenseNumber}: $reversalReason", userId, userName)
            }
        }
        return Result.success(Unit)
    }

    suspend fun addExpenseCategory(name: String, description: String = ""): Result<Long> {
        val dao = financialDao ?: return Result.failure(Exception("خدمة الحسابات المالية غير مفعلة"))
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("اسم البند لا يمكن أن يكون فارغاً"))
        }
        val id = dao.insertExpenseCategory(
            ExpenseCategoryEntity(
                name = name.trim(),
                description = description.trim(),
                isDefault = false,
                createdAt = System.currentTimeMillis()
            )
        )
        return Result.success(id)
    }

    suspend fun deleteExpenseCategory(id: Long): Result<Unit> {
        val dao = financialDao ?: return Result.failure(Exception("خدمة الحسابات المالية غير مفعلة"))
        dao.deleteExpenseCategory(id)
        return Result.success(Unit)
    }

    suspend fun recordFinancialAdjustment(
        targetAccountType: String, // "SUPPLIER", "MERCHANT", "CUSTOMER", "TREASURY"
        targetAccountId: Long?,
        targetAccountName: String,
        adjustmentType: String, // "CREDIT", "DEBIT"
        amount: Double,
        reason: String,
        userId: String = "admin",
        userName: String = "المدير"
    ): Result<FinancialAdjustmentEntity> {
        val dao = financialDao ?: return Result.failure(Exception("خدمة الحسابات المالية غير مفعلة"))
        if (amount <= 0.0) {
            return Result.failure(IllegalArgumentException("مبلغ التسوية يجب أن يكون أكبر من الصفر"))
        }
        if (reason.isBlank()) {
            return Result.failure(IllegalArgumentException("يجب كتابة سبب التسوية بالتفصيل"))
        }

        var balanceBefore = 0.0
        var balanceAfter = 0.0

        when (targetAccountType) {
            "SUPPLIER" -> {
                val supplier = accountsDao.getSupplierById(targetAccountId ?: 0)
                    ?: return Result.failure(IllegalArgumentException("المورد غير موجود"))
                balanceBefore = supplier.remainingBalance
                balanceAfter = if (adjustmentType == "CREDIT") balanceBefore + amount else balanceBefore - amount
                accountsDao.updateSupplier(supplier.copy(remainingBalance = balanceAfter))
                accountsDao.insertSupplierTransaction(
                    SupplierTransactionEntity(
                        supplierId = supplier.id,
                        type = if (adjustmentType == "CREDIT") "INVOICE" else "PAYMENT",
                        amount = amount,
                        notes = "تسوية رصيد حساب ($reason)",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            "MERCHANT" -> {
                val merchant = accountsDao.getMerchantById(targetAccountId ?: 0)
                    ?: return Result.failure(IllegalArgumentException("التاجر غير موجود"))
                balanceBefore = merchant.remainingDueToMe
                balanceAfter = if (adjustmentType == "CREDIT") balanceBefore + amount else balanceBefore - amount
                accountsDao.updateMerchant(merchant.copy(remainingDueToMe = balanceAfter))
                accountsDao.insertMerchantTransaction(
                    MerchantTransactionEntity(
                        merchantId = merchant.id,
                        type = if (adjustmentType == "CREDIT") "GOODS_DELIVERED" else "COLLECTION",
                        amount = amount,
                        notes = "تسوية رصيد حساب ($reason)",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            "CUSTOMER" -> {
                val customer = accountsDao.getCustomerById(targetAccountId ?: 0)
                    ?: return Result.failure(IllegalArgumentException("العميل غير موجود"))
                balanceBefore = customer.remainingDueToMe
                balanceAfter = if (adjustmentType == "CREDIT") balanceBefore + amount else balanceBefore - amount
                accountsDao.updateCustomer(customer.copy(remainingDueToMe = balanceAfter))
                accountsDao.insertCustomerTransaction(
                    CustomerTransactionEntity(
                        customerId = customer.id,
                        type = if (adjustmentType == "CREDIT") "CREDIT_SALE" else "COLLECTION",
                        amount = amount,
                        productDescription = "تسوية رصيد",
                        notes = "تسوية رصيد حساب ($reason)",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            "TREASURY" -> {
                val latest = dao.getLatestTreasuryTransaction()
                balanceBefore = latest?.balanceAfter ?: 0.0
                val txType = if (adjustmentType == "CREDIT") "CASH_IN" else "CASH_OUT"
                val res = recordTreasuryTransaction(
                    type = txType,
                    category = "ADJUSTMENT",
                    amount = amount,
                    referenceType = "MANUAL",
                    partyType = "INTERNAL",
                    partyName = "تسوية خزينة",
                    paymentMethod = "CASH",
                    description = "تسوية رصيد الخزينة: $reason",
                    userId = userId,
                    userName = userName
                )
                if (res.isFailure) return Result.failure(res.exceptionOrNull() ?: Exception("فشل تسوية الخزينة"))
                balanceAfter = res.getOrNull()?.balanceAfter ?: balanceBefore
            }
            else -> return Result.failure(IllegalArgumentException("نوع الحساب المستهدف غير معروف"))
        }

        val adjustment = FinancialAdjustmentEntity(
            timestamp = System.currentTimeMillis(),
            targetAccountType = targetAccountType,
            targetAccountId = targetAccountId,
            targetAccountName = targetAccountName,
            adjustmentType = adjustmentType,
            amount = amount,
            reason = reason.trim(),
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            approvedByUserId = userId,
            approvedByUserName = userName
        )
        val id = dao.insertFinancialAdjustment(adjustment)
        return Result.success(adjustment.copy(id = id))
    }

    suspend fun getFinancialSummaryReport(startDate: Long? = null, endDate: Long? = null): FinancialSummaryReport {
        val invoices = allInvoicesWithItems.first()
        val filteredInvoices = invoices.filter { inv ->
            (startDate == null || inv.invoice.timestamp >= startDate) &&
            (endDate == null || inv.invoice.timestamp <= endDate)
        }

        var totalCashSales = 0.0
        var totalCreditSales = 0.0
        for (inv in filteredInvoices) {
            totalCashSales += inv.invoice.paidAmount
            totalCreditSales += inv.invoice.remainingAmount
        }
        val totalSales = totalCashSales + totalCreditSales

        val suppliers = accountsDao.getAllSuppliers().first()
        val totalPurchases = suppliers.sumOf { it.totalPurchases }
        val totalSupplierPayables = suppliers.sumOf { it.remainingBalance }

        val customers = accountsDao.getAllCustomers().first()
        val totalCustomerReceivables = customers.sumOf { it.remainingDueToMe }

        val merchants = accountsDao.getAllMerchants().first()
        val totalMerchantReceivables = merchants.sumOf { it.remainingDueToMe }

        val treasuryTxs = allTreasuryTransactions.first().filter { !it.isReversed }
        val filteredTreasury = treasuryTxs.filter { tx ->
            (startDate == null || tx.timestamp >= startDate) &&
            (endDate == null || tx.timestamp <= endDate)
        }
        val totalCashIn = filteredTreasury.filter { it.type == "CASH_IN" }.sumOf { it.amount }
        val totalCashOut = filteredTreasury.filter { it.type == "CASH_OUT" }.sumOf { it.amount }
        val currentTreasuryBalance = treasuryTxs.firstOrNull()?.balanceAfter ?: (totalCashIn - totalCashOut)

        val expenses = allExpenses.first().filter { !it.isReversed }
        val filteredExpenses = expenses.filter { exp ->
            (startDate == null || exp.date >= startDate) &&
            (endDate == null || exp.date <= endDate)
        }
        val totalExpenses = filteredExpenses.sumOf { it.amount }
        val expensesByCategory = filteredExpenses.groupBy { it.categoryName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        var totalCogs = 0.0
        for (inv in filteredInvoices) {
            for (item in inv.items) {
                totalCogs += item.minAllowedPrice * item.quantity
            }
        }
        val grossProfit = totalSales - totalCogs
        val netProfit = grossProfit - totalExpenses

        return FinancialSummaryReport(
            totalCashSales = totalCashSales,
            totalCreditSales = totalCreditSales,
            totalSales = totalSales,
            totalPurchases = totalPurchases,
            totalCashIn = totalCashIn,
            totalCashOut = totalCashOut,
            treasuryCashBalance = currentTreasuryBalance,
            totalSupplierPayables = totalSupplierPayables,
            totalCustomerReceivables = totalCustomerReceivables,
            totalMerchantReceivables = totalMerchantReceivables,
            totalExpenses = totalExpenses,
            expensesByCategory = expensesByCategory,
            netCashFlow = totalCashIn - totalCashOut,
            estimatedGrossProfit = grossProfit,
            estimatedNetProfit = netProfit
        )
    }

    fun exportTreasuryLedgerCsv(transactions: List<TreasuryTransactionEntity>): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH)
        val sb = StringBuilder()
        sb.append("م,التاريخ,النوع,التصنيف,المبلغ,الرصيد بعد الحركة,الجهة,رقم المرجع,البيان,المسؤول,حالة الإلغاء\n")
        transactions.forEachIndexed { index, tx ->
            val typeStr = if (tx.type == "CASH_IN") "مقبوضات (وارد)" else "مدفوعات (صادر)"
            val status = if (tx.isReversed) "ملغاة بحركة عكسية" else "معتمدة"
            val categoryArabic = when (tx.category) {
                "SALE_CASH" -> "مبيعات نقدية"
                "CUSTOMER_COLLECTION" -> "تحصيل من عميل"
                "MERCHANT_COLLECTION" -> "تحصيل من تاجر"
                "SUPPLIER_PAYMENT" -> "سداد لمورد"
                "EXPENSE" -> "مصروفات"
                "RETURN_REFUND" -> "مرتجعات"
                "ADJUSTMENT" -> "تسوية خزينة"
                else -> tx.category
            }
            sb.append("${index + 1},\"${sdf.format(Date(tx.timestamp))}\",\"$typeStr\",\"$categoryArabic\",${tx.amount},${tx.balanceAfter},\"${tx.partyName}\",\"${tx.referenceNumber}\",\"${tx.description.replace("\"", "\"\"")}\",\"${tx.userName}\",\"$status\"\n")
        }
        return sb.toString()
    }

    fun exportExpensesCsv(expenses: List<ExpenseEntity>): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
        val sb = StringBuilder()
        sb.append("م,رقم المصروف,التاريخ,بند المصروف,المبلغ,طريقة الدفع,الجهة المستفيدة,البيان,المسؤول,الحالة\n")
        expenses.forEachIndexed { index, exp ->
            val payMethodStr = when (exp.paymentMethod) {
                "CASH_TREASURY" -> "نقدًا من الخزينة"
                "BANK" -> "تحويل بنكي"
                "WALLET" -> "محفظة إلكترونية"
                else -> exp.paymentMethod
            }
            val status = if (exp.isReversed) "ملغى" else "معتمد"
            sb.append("${index + 1},\"${exp.expenseNumber}\",\"${sdf.format(Date(exp.date))}\",\"${exp.categoryName}\",${exp.amount},\"$payMethodStr\",\"${exp.beneficiary}\",\"${exp.description.replace("\"", "\"\"")}\",\"${exp.createdByUserName}\",\"$status\"\n")
        }
        return sb.toString()
    }

    fun exportFinancialReportCsv(summary: FinancialSummaryReport): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH)
        val sb = StringBuilder()
        sb.append("تقرير الحسابات المالية والتدفقات النقدية - معرض البشيري للأجهزة الكهربائية\n")
        sb.append("تاريخ استخراج التقرير,\"${sdf.format(Date())}\"\n\n")
        sb.append("البند,القيمة (ج.م)\n")
        sb.append("إجمالي المبيعات النقدية,${summary.totalCashSales}\n")
        sb.append("إجمالي المبيعات الآجلة,${summary.totalCreditSales}\n")
        sb.append("إجمالي المبيعات الكلي,${summary.totalSales}\n")
        sb.append("إجمالي المشتريات,${summary.totalPurchases}\n")
        sb.append("إجمالي مقبوضات الخزينة (وارد),${summary.totalCashIn}\n")
        sb.append("إجمالي مدفوعات الخزينة (صادر),${summary.totalCashOut}\n")
        sb.append("رصيد الخزينة النقدية الحالي,${summary.treasuryCashBalance}\n")
        sb.append("إجمالي مستحقات الموردين (ديون علينا),${summary.totalSupplierPayables}\n")
        sb.append("إجمالي مستحقات العملاء (ديون لنا),${summary.totalCustomerReceivables}\n")
        sb.append("إجمالي مستحقات التجار (ديون لنا),${summary.totalMerchantReceivables}\n")
        sb.append("إجمالي المصروفات الإدارية والتشغيلية,${summary.totalExpenses}\n")
        sb.append("صافي التدفق النقدي للخزينة,${summary.netCashFlow}\n")
        sb.append("مجمل الربح التقديري,${summary.estimatedGrossProfit}\n")
        sb.append("صافي الربح التقديري,${summary.estimatedNetProfit}\n\n")
        sb.append("تفاصيل المصروفات حسب البند:\n")
        sb.append("البند,المبلغ (ج.م)\n")
        summary.expensesByCategory.forEach { (cat, amount) ->
            sb.append("\"$cat\",$amount\n")
        }
        return sb.toString()
    }
}

data class FinancialSummaryReport(
    val totalCashSales: Double = 0.0,
    val totalCreditSales: Double = 0.0,
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val totalCashIn: Double = 0.0,
    val totalCashOut: Double = 0.0,
    val treasuryCashBalance: Double = 0.0,
    val totalSupplierPayables: Double = 0.0,
    val totalCustomerReceivables: Double = 0.0,
    val totalMerchantReceivables: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val netCashFlow: Double = 0.0,
    val estimatedGrossProfit: Double = 0.0,
    val estimatedNetProfit: Double = 0.0
)
