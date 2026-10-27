package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String, // "admin", "seller1", "seller2", "seller3"
    val displayName: String,
    val role: String, // "ADMIN" or "SELLER"
    val pin: String,
    val customGreeting: String = ""
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconType: String = "APPLIANCE"
)

@Entity(tableName = "brands")
data class BrandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryId: Long
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val categoryName: String,
    val brandId: Long,
    val brandName: String,
    val description: String = ""
)

@Entity(tableName = "product_models")
data class ProductModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val brandName: String,
    val categoryName: String,
    val modelName: String, // e.g. "18 قدم نوفروست إنفرتر زجاجي"
    val finalPrice: Double, // سعر البيع النهائي
    val minAllowedPrice: Double, // الحد الأدنى المسموح به للبيع
    val stockQuantity: Int, // الكمية المتوفرة بالمخزن
    val code: String = "" // كود الموديل
)

@Entity(tableName = "sales_records")
data class SaleRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modelId: Long,
    val productName: String,
    val modelName: String,
    val categoryName: String,
    val brandName: String,
    val soldPrice: Double, // السعر الفعلي المتفق عليه للقطعة
    val minAllowedPrice: Double,
    val originalFinalPrice: Double,
    val quantity: Int,
    val totalPrice: Double, // soldPrice * quantity
    val sellerId: String,
    val sellerName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "supplier_accounts")
data class SupplierAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierName: String,
    val phone: String = "",
    val totalPurchases: Double = 0.0, // إجمالي المشتريات
    val totalPaid: Double = 0.0, // إجمالي المدفوع
    val remainingBalance: Double = 0.0, // الباقي المستحق للمورد (totalPurchases - totalPaid)
    val notes: String = ""
)

@Entity(tableName = "supplier_transactions")
data class SupplierTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val type: String, // "INVOICE" (توريد بضاعة) or "PAYMENT" (سداد دفعة)
    val amount: Double,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "merchant_accounts")
data class MerchantAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantName: String,
    val phone: String = "",
    val totalGoodsDelivered: Double = 0.0, // إجمالي البضاعة المسلمة بالأجل
    val totalCollected: Double = 0.0, // إجمالي المحصل من التاجر
    val remainingDueToMe: Double = 0.0, // الباقي المستحق لي (totalGoodsDelivered - totalCollected)
    val notes: String = ""
)

@Entity(tableName = "merchant_transactions")
data class MerchantTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantId: Long,
    val type: String, // "GOODS_DELIVERED" (تسليم بضاعة) or "COLLECTION" (تحصيل دفعة)
    val amount: Double,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "customer_accounts")
data class CustomerAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val phone: String = "",
    val totalCreditSales: Double = 0.0, // إجمالي المبيعات بالأجل
    val totalCollected: Double = 0.0, // إجمالي المحصل من العميل
    val remainingDueToMe: Double = 0.0, // الباقي المستحق لي عنده (totalCreditSales - totalCollected)
    val notes: String = ""
)

@Entity(tableName = "customer_transactions")
data class CustomerTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val type: String, // "CREDIT_SALE" (بيع بالأجل) or "COLLECTION" (تحصيل / استلام دفعة)
    val amount: Double,
    val productDescription: String = "", // وصف المنتج / الجهاز المباع بالأجل
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recycle_bin")
data class RecycleBinItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: String, // "MODEL", "PRODUCT", "CATEGORY", "BRAND", "SUPPLIER", "MERCHANT", "CUSTOMER", "SUPPLIER_TX", "MERCHANT_TX", "CUSTOMER_TX"
    val originalId: Long,
    val title: String,
    val subtitle: String = "",
    val deletedBy: String = "",
    val deletedAt: Long = System.currentTimeMillis(),
    val payloadJson: String = ""
)
