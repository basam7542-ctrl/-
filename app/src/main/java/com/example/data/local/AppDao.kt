package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_accounts")
    fun getAllUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserAccountEntity?

    @Query("UPDATE user_accounts SET pin = :newPin WHERE id = :id")
    suspend fun updatePin(id: String, newPin: String)

    @Query("UPDATE user_accounts SET customGreeting = :greeting WHERE id = :id")
    suspend fun updateCustomGreeting(id: String, greeting: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserAccountEntity>)

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserCount(): Int
}

@Dao
interface CatalogDao {
    // Categories
    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    // Brands
    @Query("SELECT * FROM brands WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getBrandsForCategory(categoryId: Long): Flow<List<BrandEntity>>

    @Query("SELECT * FROM brands ORDER BY name ASC")
    fun getAllBrands(): Flow<List<BrandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(brand: BrandEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrands(brands: List<BrandEntity>)

    @Query("DELETE FROM brands WHERE id = :id")
    suspend fun deleteBrand(id: Long)

    // Products
    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND brandId = :brandId ORDER BY name ASC")
    fun getProductsForCategoryAndBrand(categoryId: Long, brandId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    // Product Models
    @Query("SELECT * FROM product_models WHERE productId = :productId ORDER BY id ASC")
    fun getModelsForProduct(productId: Long): Flow<List<ProductModelEntity>>

    @Query("SELECT * FROM product_models ORDER BY categoryName ASC, brandName ASC, productName ASC")
    fun getAllModels(): Flow<List<ProductModelEntity>>

    @Query("SELECT * FROM product_models WHERE id = :id LIMIT 1")
    suspend fun getModelById(id: Long): ProductModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: ProductModelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModels(models: List<ProductModelEntity>)

    @Update
    suspend fun updateModel(model: ProductModelEntity)

    @Query("DELETE FROM product_models WHERE id = :id")
    suspend fun deleteModel(id: Long)

    @Query("UPDATE product_models SET stockQuantity = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int)

    @Query("UPDATE product_models SET stockQuantity = MAX(0, stockQuantity - :qty) WHERE id = :id")
    suspend fun deductStock(id: Long, qty: Int)

    @Query("UPDATE product_models SET stockQuantity = stockQuantity + :qty WHERE id = :id")
    suspend fun addStock(id: Long, qty: Int)

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM brands WHERE id = :id LIMIT 1")
    suspend fun getBrandById(id: Long): BrandEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM brands WHERE categoryId = :categoryId")
    suspend fun getBrandsForCategorySync(categoryId: Long): List<BrandEntity>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    suspend fun getProductsForCategorySync(categoryId: Long): List<ProductEntity>

    @Query("SELECT * FROM products WHERE brandId = :brandId")
    suspend fun getProductsForBrandSync(brandId: Long): List<ProductEntity>

    @Query("SELECT * FROM product_models WHERE productId = :productId")
    suspend fun getModelsForProductSync(productId: Long): List<ProductModelEntity>

    @Query("DELETE FROM product_models WHERE productId = :productId")
    suspend fun deleteModelsByProduct(productId: Long)

    @Query("DELETE FROM products WHERE brandId = :brandId")
    suspend fun deleteProductsByBrand(brandId: Long)

    @Query("DELETE FROM products WHERE categoryId = :categoryId")
    suspend fun deleteProductsByCategory(categoryId: Long)

    @Query("DELETE FROM brands WHERE categoryId = :categoryId")
    suspend fun deleteBrandsByCategory(categoryId: Long)
}

@Dao
interface SalesDao {
    @Query("SELECT * FROM sales_records ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleRecordEntity>>

    @Query("SELECT * FROM sales_records WHERE sellerId = :sellerId ORDER BY timestamp DESC")
    fun getSalesBySeller(sellerId: String): Flow<List<SaleRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleRecordEntity): Long

    @Query("DELETE FROM sales_records WHERE id = :id")
    suspend fun deleteSale(id: Long)
}

@Dao
interface AccountsDao {
    // Suppliers
    @Query("SELECT * FROM supplier_accounts ORDER BY supplierName ASC")
    fun getAllSuppliers(): Flow<List<SupplierAccountEntity>>

    @Query("SELECT * FROM supplier_accounts WHERE id = :id LIMIT 1")
    suspend fun getSupplierById(id: Long): SupplierAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierAccountEntity>)

    @Update
    suspend fun updateSupplier(supplier: SupplierAccountEntity)

    @Query("DELETE FROM supplier_accounts WHERE id = :id")
    suspend fun deleteSupplier(id: Long)

    @Query("SELECT * FROM supplier_transactions WHERE supplierId = :supplierId ORDER BY timestamp DESC")
    fun getTransactionsForSupplier(supplierId: Long): Flow<List<SupplierTransactionEntity>>

    @Query("SELECT * FROM supplier_transactions WHERE id = :id LIMIT 1")
    suspend fun getSupplierTransactionById(id: Long): SupplierTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierTransaction(tx: SupplierTransactionEntity): Long

    @Update
    suspend fun updateSupplierTransaction(tx: SupplierTransactionEntity)

    @Query("DELETE FROM supplier_transactions WHERE id = :id")
    suspend fun deleteSupplierTransaction(id: Long)

    @Query("DELETE FROM supplier_transactions WHERE supplierId = :supplierId")
    suspend fun deleteSupplierTransactionsBySupplier(supplierId: Long)

    @Query("SELECT * FROM supplier_transactions ORDER BY timestamp DESC")
    fun getAllSupplierTransactions(): Flow<List<SupplierTransactionEntity>>

    // Merchants
    @Query("SELECT * FROM merchant_accounts ORDER BY merchantName ASC")
    fun getAllMerchants(): Flow<List<MerchantAccountEntity>>

    @Query("SELECT * FROM merchant_accounts WHERE id = :id LIMIT 1")
    suspend fun getMerchantById(id: Long): MerchantAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchant(merchant: MerchantAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchants(merchants: List<MerchantAccountEntity>)

    @Update
    suspend fun updateMerchant(merchant: MerchantAccountEntity)

    @Query("DELETE FROM merchant_accounts WHERE id = :id")
    suspend fun deleteMerchant(id: Long)

    @Query("SELECT * FROM merchant_transactions WHERE merchantId = :merchantId ORDER BY timestamp DESC")
    fun getTransactionsForMerchant(merchantId: Long): Flow<List<MerchantTransactionEntity>>

    @Query("SELECT * FROM merchant_transactions WHERE id = :id LIMIT 1")
    suspend fun getMerchantTransactionById(id: Long): MerchantTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchantTransaction(tx: MerchantTransactionEntity): Long

    @Update
    suspend fun updateMerchantTransaction(tx: MerchantTransactionEntity)

    @Query("DELETE FROM merchant_transactions WHERE id = :id")
    suspend fun deleteMerchantTransaction(id: Long)

    @Query("DELETE FROM merchant_transactions WHERE merchantId = :merchantId")
    suspend fun deleteMerchantTransactionsByMerchant(merchantId: Long)

    @Query("SELECT * FROM merchant_transactions ORDER BY timestamp DESC")
    fun getAllMerchantTransactions(): Flow<List<MerchantTransactionEntity>>

    // Retail Customers (عملاء التجزئة بالأجل)
    @Query("SELECT * FROM customer_accounts ORDER BY customerName ASC")
    fun getAllCustomers(): Flow<List<CustomerAccountEntity>>

    @Query("SELECT * FROM customer_accounts WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): CustomerAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerAccountEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerAccountEntity)

    @Query("DELETE FROM customer_accounts WHERE id = :id")
    suspend fun deleteCustomer(id: Long)

    @Query("SELECT * FROM customer_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<CustomerTransactionEntity>>

    @Query("SELECT * FROM customer_transactions WHERE id = :id LIMIT 1")
    suspend fun getCustomerTransactionById(id: Long): CustomerTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerTransaction(tx: CustomerTransactionEntity): Long

    @Update
    suspend fun updateCustomerTransaction(tx: CustomerTransactionEntity)

    @Query("DELETE FROM customer_transactions WHERE id = :id")
    suspend fun deleteCustomerTransaction(id: Long)

    @Query("DELETE FROM customer_transactions WHERE customerId = :customerId")
    suspend fun deleteCustomerTransactionsByCustomer(customerId: Long)

    @Query("SELECT * FROM customer_transactions ORDER BY timestamp DESC")
    fun getAllCustomerTransactions(): Flow<List<CustomerTransactionEntity>>

    @Query("SELECT * FROM supplier_transactions WHERE supplierId = :supplierId")
    suspend fun getTransactionsForSupplierSync(supplierId: Long): List<SupplierTransactionEntity>

    @Query("SELECT * FROM merchant_transactions WHERE merchantId = :merchantId")
    suspend fun getTransactionsForMerchantSync(merchantId: Long): List<MerchantTransactionEntity>

    @Query("SELECT * FROM customer_transactions WHERE customerId = :customerId")
    suspend fun getTransactionsForCustomerSync(customerId: Long): List<CustomerTransactionEntity>
}

@Dao
interface RecycleBinDao {
    @Query("SELECT * FROM recycle_bin ORDER BY deletedAt DESC")
    fun getAllRecycleBinItems(): Flow<List<RecycleBinItemEntity>>

    @Query("SELECT * FROM recycle_bin WHERE id = :id LIMIT 1")
    suspend fun getRecycleBinItemById(id: Long): RecycleBinItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecycleBinItem(item: RecycleBinItemEntity): Long

    @Query("DELETE FROM recycle_bin WHERE id = :id")
    suspend fun deleteRecycleBinItem(id: Long)

    @Query("DELETE FROM recycle_bin")
    suspend fun deleteAllRecycleBinItems()

    @Query("SELECT COUNT(*) FROM recycle_bin")
    fun getRecycleBinCount(): Flow<Int>
}

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems?

    @Transaction
    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getInvoiceWithItemsByNumber(invoiceNumber: String): InvoiceWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoice(id: Long)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceItems(invoiceId: Long)
}

@Dao
interface ReturnsDao {
    @Transaction
    @Query("SELECT * FROM return_records ORDER BY timestamp DESC")
    fun getAllReturnsWithItems(): Flow<List<ReturnWithItems>>

    @Transaction
    @Query("SELECT * FROM return_records WHERE id = :id LIMIT 1")
    suspend fun getReturnById(id: Long): ReturnWithItems?

    @Transaction
    @Query("SELECT * FROM return_records WHERE returnNumber = :returnNumber LIMIT 1")
    suspend fun getReturnByNumber(returnNumber: String): ReturnWithItems?

    @Transaction
    @Query("SELECT * FROM return_records WHERE originalInvoiceId = :invoiceId AND status != 'CANCELLED'")
    suspend fun getActiveReturnsForInvoice(invoiceId: Long): List<ReturnWithItems>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnRecord(record: ReturnRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnItems(items: List<ReturnItemEntity>)

    @Query("UPDATE return_records SET status = :status WHERE id = :id")
    suspend fun updateReturnStatus(id: Long, status: String)

    @Query("DELETE FROM return_records WHERE id = :id")
    suspend fun deleteReturnRecord(id: Long)

    @Query("DELETE FROM return_items WHERE returnId = :returnId")
    suspend fun deleteReturnItems(returnId: Long)
}

@Dao
interface DamageRepairDao {
    @Query("SELECT * FROM damage_repair_records ORDER BY registeredTimestamp DESC")
    fun getAllRecords(): Flow<List<DamageRepairRecordEntity>>

    @Query("SELECT * FROM damage_repair_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): DamageRepairRecordEntity?

    @Query("SELECT * FROM damage_repair_records WHERE status = :status ORDER BY registeredTimestamp DESC")
    fun getRecordsByStatus(status: String): Flow<List<DamageRepairRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DamageRepairRecordEntity): Long

    @Update
    suspend fun updateRecord(record: DamageRepairRecordEntity)

    @Query("DELETE FROM damage_repair_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE modelId = :modelId ORDER BY timestamp DESC")
    fun getMovementsForModel(modelId: Long): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE movementType = :type ORDER BY timestamp DESC")
    fun getMovementsByType(type: String): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getMovementsSince(sinceTimestamp: Long): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovementEntity>)
}

@Dao
interface InventoryAuditDao {
    @Transaction
    @Query("SELECT * FROM inventory_audits ORDER BY startTimestamp DESC")
    fun getAllAudits(): Flow<List<InventoryAuditWithItems>>

    @Transaction
    @Query("SELECT * FROM inventory_audits WHERE id = :id LIMIT 1")
    suspend fun getAuditById(id: Long): InventoryAuditWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: InventoryAuditEntity): Long

    @Update
    suspend fun updateAudit(audit: InventoryAuditEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditItems(items: List<InventoryAuditItemEntity>)

    @Update
    suspend fun updateAuditItem(item: InventoryAuditItemEntity)

    @Update
    suspend fun updateAuditItems(items: List<InventoryAuditItemEntity>)

    @Query("SELECT * FROM inventory_audit_items WHERE id = :id LIMIT 1")
    suspend fun getAuditItemById(id: Long): InventoryAuditItemEntity?

    @Query("SELECT * FROM inventory_audit_items WHERE auditId = :auditId")
    suspend fun getAuditItems(auditId: Long): List<InventoryAuditItemEntity>

    @Query("DELETE FROM inventory_audit_items WHERE auditId = :auditId")
    suspend fun deleteAuditItems(auditId: Long)

    @Query("DELETE FROM inventory_audits WHERE id = :id")
    suspend fun deleteAudit(id: Long)
}

@Dao
interface FinancialDao {
    // Treasury Transactions
    @Query("SELECT * FROM treasury_transactions ORDER BY timestamp DESC")
    fun getAllTreasuryTransactions(): Flow<List<TreasuryTransactionEntity>>

    @Query("SELECT * FROM treasury_transactions WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getTreasuryTransactionsSince(since: Long): Flow<List<TreasuryTransactionEntity>>

    @Query("SELECT * FROM treasury_transactions WHERE referenceType = :refType AND referenceId = :refId AND isReversed = 0 LIMIT 1")
    suspend fun getActiveTransactionByReference(refType: String, refId: Long): TreasuryTransactionEntity?

    @Query("SELECT * FROM treasury_transactions ORDER BY id DESC LIMIT 1")
    suspend fun getLatestTreasuryTransaction(): TreasuryTransactionEntity?

    @Query("SELECT * FROM treasury_transactions WHERE id = :id LIMIT 1")
    suspend fun getTreasuryTransactionById(id: Long): TreasuryTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreasuryTransaction(tx: TreasuryTransactionEntity): Long

    @Update
    suspend fun updateTreasuryTransaction(tx: TreasuryTransactionEntity)

    // Expense Categories
    @Query("SELECT * FROM expense_categories ORDER BY name ASC")
    fun getAllExpenseCategories(): Flow<List<ExpenseCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExpenseCategory(cat: ExpenseCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExpenseCategories(cats: List<ExpenseCategoryEntity>)

    @Update
    suspend fun updateExpenseCategory(cat: ExpenseCategoryEntity)

    @Query("DELETE FROM expense_categories WHERE id = :id AND isDefault = 0")
    suspend fun deleteExpenseCategory(id: Long)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getExpensesBetween(start: Long, end: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryName = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    // Financial Adjustments
    @Query("SELECT * FROM financial_adjustments ORDER BY timestamp DESC")
    fun getAllFinancialAdjustments(): Flow<List<FinancialAdjustmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialAdjustment(adj: FinancialAdjustmentEntity): Long
}

