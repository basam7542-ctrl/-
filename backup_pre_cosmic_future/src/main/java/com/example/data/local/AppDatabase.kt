package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserAccountEntity::class,
        CategoryEntity::class,
        BrandEntity::class,
        ProductEntity::class,
        ProductModelEntity::class,
        SaleRecordEntity::class,
        SupplierAccountEntity::class,
        SupplierTransactionEntity::class,
        MerchantAccountEntity::class,
        MerchantTransactionEntity::class,
        CustomerAccountEntity::class,
        CustomerTransactionEntity::class,
        RecycleBinItemEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        ReturnRecordEntity::class,
        ReturnItemEntity::class,
        DamageRepairRecordEntity::class,
        StockMovementEntity::class,
        InventoryAuditEntity::class,
        InventoryAuditItemEntity::class,
        TreasuryTransactionEntity::class,
        ExpenseCategoryEntity::class,
        ExpenseEntity::class,
        FinancialAdjustmentEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun catalogDao(): CatalogDao
    abstract fun salesDao(): SalesDao
    abstract fun accountsDao(): AccountsDao
    abstract fun recycleBinDao(): RecycleBinDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun returnsDao(): ReturnsDao
    abstract fun damageRepairDao(): DamageRepairDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun inventoryAuditDao(): InventoryAuditDao
    abstract fun financialDao(): FinancialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN customGreeting TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS customer_accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerName TEXT NOT NULL,
                        phone TEXT NOT NULL DEFAULT '',
                        totalCreditSales REAL NOT NULL DEFAULT 0.0,
                        totalCollected REAL NOT NULL DEFAULT 0.0,
                        remainingDueToMe REAL NOT NULL DEFAULT 0.0,
                        notes TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS customer_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        productDescription TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS recycle_bin (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        itemType TEXT NOT NULL,
                        originalId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        subtitle TEXT NOT NULL DEFAULT '',
                        deletedBy TEXT NOT NULL DEFAULT '',
                        deletedAt INTEGER NOT NULL,
                        payloadJson TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sales_records ADD COLUMN customerName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sales_records ADD COLUMN customerPhone TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS invoices (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        invoiceNumber TEXT NOT NULL,
                        customerName TEXT NOT NULL DEFAULT '',
                        customerPhone TEXT NOT NULL DEFAULT '',
                        sellerId TEXT NOT NULL,
                        sellerName TEXT NOT NULL,
                        subtotal REAL NOT NULL,
                        discount REAL NOT NULL DEFAULT 0.0,
                        finalTotal REAL NOT NULL,
                        paidAmount REAL NOT NULL,
                        remainingAmount REAL NOT NULL DEFAULT 0.0,
                        paymentStatus TEXT NOT NULL DEFAULT 'PAID',
                        notes TEXT NOT NULL DEFAULT '',
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS invoice_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        invoiceId INTEGER NOT NULL,
                        modelId INTEGER NOT NULL,
                        productName TEXT NOT NULL,
                        modelName TEXT NOT NULL,
                        categoryName TEXT NOT NULL DEFAULT '',
                        brandName TEXT NOT NULL DEFAULT '',
                        quantity INTEGER NOT NULL,
                        unitPrice REAL NOT NULL,
                        minAllowedPrice REAL NOT NULL,
                        originalPrice REAL NOT NULL,
                        totalPrice REAL NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_invoice_items_invoiceId ON invoice_items (invoiceId)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE invoices ADD COLUMN storeName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE invoices ADD COLUMN storeAddress TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE invoices ADD COLUMN storePhone TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE invoices ADD COLUMN commercialRegister TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE invoices ADD COLUMN taxCard TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE invoices ADD COLUMN vatNumber TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE invoices ADD COLUMN storeLogoUri TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS return_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        returnNumber TEXT NOT NULL,
                        type TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        partyName TEXT NOT NULL,
                        partyPhone TEXT NOT NULL DEFAULT '',
                        partyId INTEGER,
                        originalInvoiceNumber TEXT NOT NULL DEFAULT '',
                        originalInvoiceId INTEGER,
                        totalAmount REAL NOT NULL,
                        reason TEXT NOT NULL DEFAULT '',
                        settlementType TEXT NOT NULL,
                        settlementAmount REAL NOT NULL,
                        settlementNotes TEXT NOT NULL DEFAULT '',
                        createdByUserId TEXT NOT NULL,
                        createdByName TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'APPROVED',
                        notes TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS return_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        returnId INTEGER NOT NULL,
                        modelId INTEGER NOT NULL,
                        productName TEXT NOT NULL,
                        modelName TEXT NOT NULL,
                        categoryName TEXT NOT NULL DEFAULT '',
                        brandName TEXT NOT NULL DEFAULT '',
                        quantity INTEGER NOT NULL,
                        unitPrice REAL NOT NULL,
                        totalPrice REAL NOT NULL,
                        stockCondition TEXT NOT NULL DEFAULT 'RESELLABLE',
                        itemNotes TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_return_items_returnId ON return_items (returnId)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS damage_repair_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recordNumber TEXT NOT NULL,
                        modelId INTEGER NOT NULL,
                        productName TEXT NOT NULL,
                        modelName TEXT NOT NULL,
                        categoryName TEXT NOT NULL DEFAULT '',
                        brandName TEXT NOT NULL DEFAULT '',
                        quantity INTEGER NOT NULL,
                        reason TEXT NOT NULL,
                        damageDetails TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL,
                        sourceType TEXT NOT NULL DEFAULT 'INVENTORY',
                        returnRecordId INTEGER,
                        returnNumber TEXT NOT NULL DEFAULT '',
                        unitCost REAL NOT NULL,
                        totalLossAmount REAL NOT NULL,
                        sparePartsCost REAL NOT NULL DEFAULT 0.0,
                        laborCost REAL NOT NULL DEFAULT 0.0,
                        totalRepairCost REAL NOT NULL DEFAULT 0.0,
                        repairNotes TEXT NOT NULL DEFAULT '',
                        repairOutcome TEXT NOT NULL DEFAULT '',
                        sentToRepairDate INTEGER,
                        repairCompletedDate INTEGER,
                        restoredToStockDate INTEGER,
                        imageUri TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        registeredByUserId TEXT NOT NULL,
                        registeredByName TEXT NOT NULL,
                        registeredTimestamp INTEGER NOT NULL,
                        approvedByUserId TEXT NOT NULL DEFAULT '',
                        approvedByName TEXT NOT NULL DEFAULT '',
                        approvedTimestamp INTEGER,
                        isStockDeducted INTEGER NOT NULL DEFAULT 0,
                        isStockRestored INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS stock_movements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        movementType TEXT NOT NULL,
                        modelId INTEGER NOT NULL,
                        productName TEXT NOT NULL,
                        modelName TEXT NOT NULL,
                        code TEXT NOT NULL DEFAULT '',
                        categoryName TEXT NOT NULL DEFAULT '',
                        brandName TEXT NOT NULL DEFAULT '',
                        quantityChanged INTEGER NOT NULL,
                        quantityBefore INTEGER NOT NULL,
                        quantityAfter INTEGER NOT NULL,
                        unitCostPrice REAL NOT NULL DEFAULT 0.0,
                        referenceType TEXT NOT NULL DEFAULT '',
                        referenceNumber TEXT NOT NULL DEFAULT '',
                        referenceId INTEGER,
                        userId TEXT NOT NULL,
                        userName TEXT NOT NULL,
                        notes TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_movements_modelId ON stock_movements(modelId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_movements_timestamp ON stock_movements(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_movements_movementType ON stock_movements(movementType)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS inventory_audits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        auditNumber TEXT NOT NULL,
                        title TEXT NOT NULL,
                        scopeType TEXT NOT NULL DEFAULT 'ALL',
                        scopeFilter TEXT NOT NULL DEFAULT '',
                        startTimestamp INTEGER NOT NULL,
                        completedTimestamp INTEGER,
                        status TEXT NOT NULL DEFAULT 'DRAFT',
                        totalItemsCounted INTEGER NOT NULL DEFAULT 0,
                        totalMatchedItems INTEGER NOT NULL DEFAULT 0,
                        totalDeficitItems INTEGER NOT NULL DEFAULT 0,
                        totalSurplusItems INTEGER NOT NULL DEFAULT 0,
                        netQuantityVariance INTEGER NOT NULL DEFAULT 0,
                        netValueVariance REAL NOT NULL DEFAULT 0.0,
                        notes TEXT NOT NULL DEFAULT '',
                        createdByUserId TEXT NOT NULL,
                        createdByName TEXT NOT NULL,
                        approvedByUserId TEXT NOT NULL DEFAULT '',
                        approvedByName TEXT NOT NULL DEFAULT '',
                        settledTimestamp INTEGER,
                        isSettled INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_inventory_audits_auditNumber ON inventory_audits(auditNumber)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_audits_startTimestamp ON inventory_audits(startTimestamp)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS inventory_audit_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        auditId INTEGER NOT NULL,
                        modelId INTEGER NOT NULL,
                        productName TEXT NOT NULL,
                        modelName TEXT NOT NULL,
                        code TEXT NOT NULL DEFAULT '',
                        categoryName TEXT NOT NULL DEFAULT '',
                        brandName TEXT NOT NULL DEFAULT '',
                        costPrice REAL NOT NULL DEFAULT 0.0,
                        systemQuantity INTEGER NOT NULL,
                        actualQuantity INTEGER NOT NULL,
                        differenceQuantity INTEGER NOT NULL,
                        varianceValue REAL NOT NULL DEFAULT 0.0,
                        status TEXT NOT NULL DEFAULT 'MATCH',
                        notes TEXT NOT NULL DEFAULT '',
                        isSettled INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_audit_items_auditId ON inventory_audit_items(auditId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_audit_items_modelId ON inventory_audit_items(modelId)")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS treasury_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        category TEXT NOT NULL,
                        amount REAL NOT NULL,
                        balanceAfter REAL NOT NULL,
                        referenceType TEXT NOT NULL DEFAULT '',
                        referenceId INTEGER,
                        referenceNumber TEXT NOT NULL DEFAULT '',
                        partyType TEXT NOT NULL DEFAULT '',
                        partyName TEXT NOT NULL DEFAULT '',
                        paymentMethod TEXT NOT NULL DEFAULT 'CASH',
                        description TEXT NOT NULL DEFAULT '',
                        userId TEXT NOT NULL,
                        userName TEXT NOT NULL,
                        isReversed INTEGER NOT NULL DEFAULT 0,
                        reversalNotes TEXT NOT NULL DEFAULT '',
                        reversalTransactionId INTEGER
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_treasury_transactions_timestamp ON treasury_transactions(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_treasury_transactions_type ON treasury_transactions(type)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_treasury_transactions_category ON treasury_transactions(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_treasury_transactions_referenceType ON treasury_transactions(referenceType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_treasury_transactions_referenceNumber ON treasury_transactions(referenceNumber)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS expense_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        isDefault INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_expense_categories_name ON expense_categories(name)")

                // Pre-populate default expense categories
                val now = System.currentTimeMillis()
                val defaultCategories = listOf(
                    "كهرباء ومياه وإنترنت",
                    "إيجار المعرض والمخازن",
                    "رواتب ومكافآت الموظفين",
                    "نقل وشحن وبضائع",
                    "صيانة وضيافة ومستلزمات",
                    "دعاية وتسويق",
                    "مصاريف بنكية ورسوم",
                    "مصاريف إدارية أخرى"
                )
                for (cat in defaultCategories) {
                    db.execSQL("INSERT OR IGNORE INTO expense_categories (name, description, isDefault, createdAt) VALUES ('$cat', 'بند مصروفات أساسي للنظام', 1, $now)")
                }

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        expenseNumber TEXT NOT NULL,
                        categoryName TEXT NOT NULL,
                        amount REAL NOT NULL,
                        date INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        beneficiary TEXT NOT NULL DEFAULT '',
                        paymentMethod TEXT NOT NULL DEFAULT 'CASH_TREASURY',
                        referenceNumber TEXT NOT NULL DEFAULT '',
                        createdByUserId TEXT NOT NULL,
                        createdByUserName TEXT NOT NULL,
                        isApproved INTEGER NOT NULL DEFAULT 1,
                        notes TEXT NOT NULL DEFAULT '',
                        isReversed INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_expenses_expenseNumber ON expenses(expenseNumber)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_date ON expenses(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_categoryName ON expenses(categoryName)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS financial_adjustments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        targetAccountType TEXT NOT NULL,
                        targetAccountId INTEGER,
                        targetAccountName TEXT NOT NULL,
                        adjustmentType TEXT NOT NULL,
                        amount REAL NOT NULL,
                        reason TEXT NOT NULL,
                        balanceBefore REAL NOT NULL,
                        balanceAfter REAL NOT NULL,
                        approvedByUserId TEXT NOT NULL,
                        approvedByUserName TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_financial_adjustments_timestamp ON financial_adjustments(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_financial_adjustments_targetAccountType ON financial_adjustments(targetAccountType)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "albashiri_store.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
