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
