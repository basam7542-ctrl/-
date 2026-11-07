package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

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
    val customerName: String = "",
    val customerPhone: String = "",
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

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String = "",
    val customerPhone: String = "",
    val sellerId: String,
    val sellerName: String,
    val subtotal: Double,
    val discount: Double = 0.0,
    val finalTotal: Double,
    val paidAmount: Double,
    val remainingAmount: Double = 0.0,
    val paymentStatus: String = "PAID", // "PAID", "PARTIAL", "CREDIT"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val storeName: String = "",
    val storeAddress: String = "",
    val storePhone: String = "",
    val commercialRegister: String = "",
    val taxCard: String = "",
    val vatNumber: String = "",
    val storeLogoUri: String = ""
)

@Entity(
    tableName = "invoice_items",
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val modelId: Long,
    val productName: String,
    val modelName: String,
    val categoryName: String = "",
    val brandName: String = "",
    val quantity: Int,
    val unitPrice: Double,
    val minAllowedPrice: Double,
    val originalPrice: Double,
    val totalPrice: Double
)

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>
)

@Entity(tableName = "return_records")
data class ReturnRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val returnNumber: String,
    val type: String, // "CUSTOMER_TO_STORE", "STORE_TO_SUPPLIER", "MERCHANT_TO_STORE", "STORE_TO_MERCHANT"
    val timestamp: Long = System.currentTimeMillis(),
    val partyName: String,
    val partyPhone: String = "",
    val partyId: Long? = null,
    val originalInvoiceNumber: String = "",
    val originalInvoiceId: Long? = null,
    val totalAmount: Double,
    val reason: String = "",
    val settlementType: String, // "CASH_REFUND", "CUSTOMER_CREDIT", "EXCHANGE", "DECREASE_DEBT", "DECREASE_MERCHANT_DUE", "CASH_REFUND_FROM_SUPPLIER"
    val settlementAmount: Double,
    val settlementNotes: String = "",
    val createdByUserId: String,
    val createdByName: String,
    val status: String = "APPROVED", // "APPROVED", "CANCELLED"
    val notes: String = ""
)

@Entity(
    tableName = "return_items",
    indices = [Index(value = ["returnId"])]
)
data class ReturnItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val returnId: Long,
    val modelId: Long,
    val productName: String,
    val modelName: String,
    val categoryName: String = "",
    val brandName: String = "",
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val stockCondition: String = "RESELLABLE", // "RESELLABLE", "DAMAGED", "INSPECTION", "REPAIRABLE"
    val itemNotes: String = ""
)

data class ReturnWithItems(
    @Embedded val returnRecord: ReturnRecordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "returnId"
    )
    val items: List<ReturnItemEntity>
)

@Entity(tableName = "damage_repair_records")
data class DamageRepairRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordNumber: String,
    val modelId: Long,
    val productName: String,
    val modelName: String,
    val categoryName: String = "",
    val brandName: String = "",
    val quantity: Int,
    val reason: String, // "كسر", "حرق كهربائي", "عيب تصنيع", "فقد", "تلف أثناء النقل", "أخرى"
    val damageDetails: String = "",
    val status: String, // "TOTAL_SCRAP", "UNDER_INSPECTION", "REPAIRABLE", "UNDER_REPAIR", "REPAIRED_PENDING_APPROVAL", "RESTORED_TO_STOCK"
    val sourceType: String = "INVENTORY", // "INVENTORY", "CUSTOMER_RETURN"
    val returnRecordId: Long? = null,
    val returnNumber: String = "",
    val unitCost: Double,
    val totalLossAmount: Double,
    val sparePartsCost: Double = 0.0,
    val laborCost: Double = 0.0,
    val totalRepairCost: Double = 0.0,
    val repairNotes: String = "",
    val repairOutcome: String = "", // "FIXED", "CANNOT_BE_FIXED", "PARTIALLY_FIXED"
    val sentToRepairDate: Long? = null,
    val repairCompletedDate: Long? = null,
    val restoredToStockDate: Long? = null,
    val imageUri: String = "",
    val notes: String = "",
    val registeredByUserId: String,
    val registeredByName: String,
    val registeredTimestamp: Long = System.currentTimeMillis(),
    val approvedByUserId: String = "",
    val approvedByName: String = "",
    val approvedTimestamp: Long? = null,
    val isStockDeducted: Boolean = false,
    val isStockRestored: Boolean = false
)

@Entity(
    tableName = "stock_movements",
    indices = [
        Index(value = ["modelId"]),
        Index(value = ["timestamp"]),
        Index(value = ["movementType"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val movementType: String, // "PURCHASE", "SALE", "RETURN_CUSTOMER", "RETURN_SUPPLIER", "DAMAGE_SCRAP", "REPAIR_RESTORE", "INVENTORY_AUDIT_SURPLUS", "INVENTORY_AUDIT_DEFICIT", "MANUAL_ADJUSTMENT"
    val modelId: Long,
    val productName: String,
    val modelName: String,
    val code: String = "",
    val categoryName: String = "",
    val brandName: String = "",
    val quantityChanged: Int, // e.g. +5 or -2
    val quantityBefore: Int,
    val quantityAfter: Int,
    val unitCostPrice: Double = 0.0,
    val referenceType: String = "", // "INVOICE", "RETURN", "DAMAGE", "AUDIT", "SUPPLIER_PURCHASE", "MANUAL"
    val referenceNumber: String = "",
    val referenceId: Long? = null,
    val userId: String,
    val userName: String,
    val notes: String = ""
)

@Entity(
    tableName = "inventory_audits",
    indices = [
        Index(value = ["auditNumber"], unique = true),
        Index(value = ["startTimestamp"])
    ]
)
data class InventoryAuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val auditNumber: String,
    val title: String,
    val scopeType: String = "ALL", // "ALL", "CATEGORY", "BRAND", "CUSTOM"
    val scopeFilter: String = "",
    val startTimestamp: Long = System.currentTimeMillis(),
    val completedTimestamp: Long? = null,
    val status: String = "DRAFT", // "DRAFT", "PENDING_APPROVAL", "SETTLED", "CANCELLED"
    val totalItemsCounted: Int = 0,
    val totalMatchedItems: Int = 0,
    val totalDeficitItems: Int = 0,
    val totalSurplusItems: Int = 0,
    val netQuantityVariance: Int = 0,
    val netValueVariance: Double = 0.0,
    val notes: String = "",
    val createdByUserId: String,
    val createdByName: String,
    val approvedByUserId: String = "",
    val approvedByName: String = "",
    val settledTimestamp: Long? = null,
    val isSettled: Boolean = false
)

@Entity(
    tableName = "inventory_audit_items",
    indices = [
        Index(value = ["auditId"]),
        Index(value = ["modelId"])
    ]
)
data class InventoryAuditItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val auditId: Long,
    val modelId: Long,
    val productName: String,
    val modelName: String,
    val code: String = "",
    val categoryName: String = "",
    val brandName: String = "",
    val costPrice: Double = 0.0,
    val systemQuantity: Int,
    val actualQuantity: Int,
    val differenceQuantity: Int, // actualQuantity - systemQuantity
    val varianceValue: Double = 0.0, // differenceQuantity * costPrice
    val status: String = "MATCH", // "MATCH", "DEFICIT", "SURPLUS"
    val notes: String = "",
    val isSettled: Boolean = false
)

data class InventoryAuditWithItems(
    @Embedded val audit: InventoryAuditEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "auditId"
    )
    val items: List<InventoryAuditItemEntity>
)

@Entity(
    tableName = "treasury_transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["type"]),
        Index(value = ["category"]),
        Index(value = ["referenceType"]),
        Index(value = ["referenceNumber"])
    ]
)
data class TreasuryTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "CASH_IN" (مقبوضات), "CASH_OUT" (مدفوعات)
    val category: String, // "SALE_CASH", "CUSTOMER_COLLECTION", "MERCHANT_COLLECTION", "SUPPLIER_PAYMENT", "EXPENSE", "CAPITAL_INJECTION", "TRANSFER", "ADJUSTMENT", "RETURN_REFUND"
    val amount: Double,
    val balanceAfter: Double,
    val referenceType: String = "", // "INVOICE", "SUPPLIER_TX", "MERCHANT_TX", "CUSTOMER_TX", "EXPENSE", "RETURN", "MANUAL"
    val referenceId: Long? = null,
    val referenceNumber: String = "",
    val partyType: String = "", // "CUSTOMER", "SUPPLIER", "MERCHANT", "EXPENSE", "INTERNAL"
    val partyName: String = "",
    val paymentMethod: String = "CASH", // "CASH", "BANK_TRANSFER", "WALLET"
    val description: String = "",
    val userId: String,
    val userName: String,
    val isReversed: Boolean = false,
    val reversalNotes: String = "",
    val reversalTransactionId: Long? = null
)

@Entity(
    tableName = "expense_categories",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class ExpenseCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["expenseNumber"], unique = true),
        Index(value = ["date"]),
        Index(value = ["categoryName"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expenseNumber: String,
    val categoryName: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val description: String,
    val beneficiary: String = "", // الجهة المستفيدة
    val paymentMethod: String = "CASH_TREASURY", // "CASH_TREASURY", "BANK", "WALLET"
    val referenceNumber: String = "",
    val createdByUserId: String,
    val createdByUserName: String,
    val isApproved: Boolean = true,
    val notes: String = "",
    val isReversed: Boolean = false
)

@Entity(
    tableName = "financial_adjustments",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["targetAccountType"])
    ]
)
data class FinancialAdjustmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val targetAccountType: String, // "SUPPLIER", "MERCHANT", "CUSTOMER", "TREASURY"
    val targetAccountId: Long? = null,
    val targetAccountName: String,
    val adjustmentType: String, // "CREDIT" (إضافة رصيد / دائن), "DEBIT" (خصم رصيد / مدين)
    val amount: Double,
    val reason: String,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val approvedByUserId: String,
    val approvedByUserName: String
)

