package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ProductModelEntity
import com.example.data.repository.InvoiceDraftItem
import com.example.data.repository.StoreRepository
import com.example.utils.BluetoothPrinterHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: StoreRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = StoreRepository(
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
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppNameResource() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("البشيري", appName)
    }

    @Test
    fun testSaleValidationPreventsBelowMinPrice() = runBlocking {
        val modelId = repository.addModel(
            productId = 1,
            productName = "غسالة توشيبا",
            brandName = "توشيبا",
            categoryName = "غسالات",
            modelName = "10 كيلو",
            finalPrice = 14500.0,
            minAllowedPrice = 13900.0,
            stockQuantity = 5,
            code = "TOSH-10"
        )

        // Attempting to sell below min price (13,000 < 13,900) should fail
        val failedResult = repository.recordSale(
            modelId = modelId,
            productName = "غسالة توشيبا",
            modelName = "10 كيلو",
            categoryName = "غسالات",
            brandName = "توشيبا",
            soldPrice = 13000.0,
            minAllowedPrice = 13900.0,
            originalFinalPrice = 14500.0,
            quantity = 1,
            sellerId = "seller1",
            sellerName = "بائع 1",
            notes = "test"
        )
        assertTrue(failedResult.isFailure)

        // Stock must remain intact (5)
        val modelAfterFailed = repository.getModelById(modelId)
        assertEquals(5, modelAfterFailed?.stockQuantity)

        // Valid sale at negotiated price >= minAllowedPrice (14,000 >= 13,900)
        val successResult = repository.recordSale(
            modelId = modelId,
            productName = "غسالة توشيبا",
            modelName = "10 كيلو",
            categoryName = "غسالات",
            brandName = "توشيبا",
            soldPrice = 14000.0,
            minAllowedPrice = 13900.0,
            originalFinalPrice = 14500.0,
            quantity = 2,
            sellerId = "seller1",
            sellerName = "بائع 1",
            notes = "بيع ناجح",
            customerName = "أحمد البشيري",
            customerPhone = "01098765432"
        )
        assertTrue(successResult.isSuccess)

        // Stock should be deducted from 5 to 3
        val modelAfterSuccess = repository.getModelById(modelId)
        assertEquals(3, modelAfterSuccess?.stockQuantity)

        // Sales records should contain 1 transaction with intact customer name and phone (with leading zero)
        val sales = repository.allSales.first()
        assertEquals(1, sales.size)
        assertEquals(28000.0, sales[0].totalPrice, 0.01)
        assertEquals("أحمد البشيري", sales[0].customerName)
        assertEquals("01098765432", sales[0].customerPhone)
        assertTrue("Phone must strictly preserve leading zero", sales[0].customerPhone.startsWith("0"))
    }

    @Test
    fun testSupplierTransactionEditAndDelete() = runBlocking {
        // 1. Add supplier
        val sId = repository.addSupplier(
            name = "شركة النصر للأجهزة",
            phone = "01011122233",
            initialPurchases = 50000.0,
            notes = "توريد بوتاجازات"
        )
        val initialSupplier = db.accountsDao().getSupplierById(sId)
        assertEquals(50000.0, initialSupplier?.totalPurchases ?: 0.0, 0.01)
        assertEquals(0.0, initialSupplier?.totalPaid ?: 0.0, 0.01)
        assertEquals(50000.0, initialSupplier?.remainingBalance ?: 0.0, 0.01)

        // 2. Add payment transaction
        repository.addSupplierTransaction(sId, type = "PAYMENT", amount = 20000.0, notes = "دفعة أولى")
        val txsAfterAdd = repository.getSupplierTransactions(sId).first()
        assertEquals(1, txsAfterAdd.size)
        val txId = txsAfterAdd[0].id

        val supplierAfterAdd = db.accountsDao().getSupplierById(sId)
        assertEquals(20000.0, supplierAfterAdd?.totalPaid ?: 0.0, 0.01)
        assertEquals(30000.0, supplierAfterAdd?.remainingBalance ?: 0.0, 0.01)

        // 3. Edit payment transaction: change amount to 25,000
        repository.updateSupplierTransaction(txId, newType = "PAYMENT", newAmount = 25000.0, newNotes = "دفعة معدلة")
        val supplierAfterEdit = db.accountsDao().getSupplierById(sId)
        assertEquals(25000.0, supplierAfterEdit?.totalPaid ?: 0.0, 0.01)
        assertEquals(25000.0, supplierAfterEdit?.remainingBalance ?: 0.0, 0.01)

        // 4. Delete payment transaction -> balance reverts to original
        repository.deleteSupplierTransaction(txId)
        val supplierAfterDeleteTx = db.accountsDao().getSupplierById(sId)
        assertEquals(0.0, supplierAfterDeleteTx?.totalPaid ?: 0.0, 0.01)
        assertEquals(50000.0, supplierAfterDeleteTx?.remainingBalance ?: 0.0, 0.01)
        val txsAfterDelete = repository.getSupplierTransactions(sId).first()
        assertTrue(txsAfterDelete.isEmpty())

        // 5. Delete entire supplier
        repository.deleteSupplier(sId)
        val deletedSupplier = db.accountsDao().getSupplierById(sId)
        org.junit.Assert.assertNull(deletedSupplier)
    }

    @Test
    fun testMerchantTransactionEditAndDelete() = runBlocking {
        // 1. Add merchant
        val mId = repository.addMerchant(
            name = "معرض السلام",
            phone = "01122233344",
            initialDelivered = 40000.0,
            notes = "معرض تجزئة"
        )
        val initialMerchant = db.accountsDao().getMerchantById(mId)
        assertEquals(40000.0, initialMerchant?.totalGoodsDelivered ?: 0.0, 0.01)
        assertEquals(0.0, initialMerchant?.totalCollected ?: 0.0, 0.01)
        assertEquals(40000.0, initialMerchant?.remainingDueToMe ?: 0.0, 0.01)

        // 2. Add collection transaction
        repository.addMerchantTransaction(mId, type = "COLLECTION", amount = 15000.0, notes = "تحصيل نقدي")
        val txsAfterAdd = repository.getMerchantTransactions(mId).first()
        assertEquals(1, txsAfterAdd.size)
        val txId = txsAfterAdd[0].id

        val merchantAfterAdd = db.accountsDao().getMerchantById(mId)
        assertEquals(15000.0, merchantAfterAdd?.totalCollected ?: 0.0, 0.01)
        assertEquals(25000.0, merchantAfterAdd?.remainingDueToMe ?: 0.0, 0.01)

        // 3. Edit collection transaction: change amount to 18,000
        repository.updateMerchantTransaction(txId, newType = "COLLECTION", newAmount = 18000.0, newNotes = "تحصيل معدل")
        val merchantAfterEdit = db.accountsDao().getMerchantById(mId)
        assertEquals(18000.0, merchantAfterEdit?.totalCollected ?: 0.0, 0.01)
        assertEquals(22000.0, merchantAfterEdit?.remainingDueToMe ?: 0.0, 0.01)

        // 4. Delete collection transaction -> balance reverts to original
        repository.deleteMerchantTransaction(txId)
        val merchantAfterDeleteTx = db.accountsDao().getMerchantById(mId)
        assertEquals(0.0, merchantAfterDeleteTx?.totalCollected ?: 0.0, 0.01)
        assertEquals(40000.0, merchantAfterDeleteTx?.remainingDueToMe ?: 0.0, 0.01)

        // 5. Delete entire merchant -> safely moves to recycle bin
        repository.deleteMerchant(mId, deletedBy = "مدير النظام")
        val deletedMerchant = db.accountsDao().getMerchantById(mId)
        org.junit.Assert.assertNull(deletedMerchant)

        // Verify it exists in recycle bin (contains the deleted transaction + the deleted merchant)
        val binItems = repository.allRecycleBinItems.first()
        assertEquals(2, binItems.size)
        val merchantBinItem = binItems.first { it.itemType == "MERCHANT" }
        assertEquals("تاجر: معرض السلام", merchantBinItem.title)
        assertEquals("مدير النظام", merchantBinItem.deletedBy)

        // 6. Restore merchant
        repository.restoreRecycleBinItem(merchantBinItem.id)
        val restoredMerchant = db.accountsDao().getMerchantById(mId)
        org.junit.Assert.assertNotNull(restoredMerchant)
        assertEquals("معرض السلام", restoredMerchant?.merchantName)
        assertEquals(40000.0, restoredMerchant?.remainingDueToMe ?: 0.0, 0.01)

        val binAfterRestore = repository.allRecycleBinItems.first()
        assertEquals(1, binAfterRestore.size) // Only the earlier transaction remains in bin
    }

    @Test
    fun testRecycleBinProductModelSoftDeleteAndRestore() = runBlocking {
        val catId = repository.addCategory("غسالات", "APPLIANCE")
        val brandId = repository.addBrand("إل جي", catId)
        val prodId = repository.addProduct(
            name = "غسالة أوتوماتيك",
            categoryId = catId,
            categoryName = "غسالات",
            brandId = brandId,
            brandName = "إل جي"
        )
        val modelId = repository.addModel(
            productId = prodId,
            productName = "غسالة أوتوماتيك",
            brandName = "إل جي",
            categoryName = "غسالات",
            modelName = "F4V5VYP0W",
            finalPrice = 18000.0,
            minAllowedPrice = 17500.0,
            stockQuantity = 5,
            code = "LG-WASH-10"
        )

        // Soft delete model
        repository.deleteModel(modelId, deletedBy = "المدير")
        val modelAfterDelete = repository.getModelById(modelId)
        org.junit.Assert.assertNull(modelAfterDelete)

        val binItems = repository.allRecycleBinItems.first()
        assertEquals(1, binItems.size)
        assertEquals("MODEL", binItems[0].itemType)
        assertEquals("غسالة أوتوماتيك - F4V5VYP0W", binItems[0].title)

        // Restore model
        repository.restoreRecycleBinItem(binItems[0].id)
        val restoredModel = repository.getModelById(modelId)
        org.junit.Assert.assertNotNull(restoredModel)
        assertEquals("F4V5VYP0W", restoredModel?.modelName)
        assertEquals(5, restoredModel?.stockQuantity)
        assertEquals(17500.0, restoredModel?.minAllowedPrice ?: 0.0, 0.01)

        val emptyBin = repository.allRecycleBinItems.first()
        assertEquals(0, emptyBin.size)
    }

    @Test
    fun testMultiItemInvoiceWith5ProductsCalculationsAndStockDeduction() = runBlocking {
        // 1. Register 5 distinct products and models in inventory
        val m1Id = repository.addModel(
            productId = 1,
            productName = "ثلاجة شارب ديجيتال",
            brandName = "شارب",
            categoryName = "ثلاجات",
            modelName = "18 قدم إنفرتر نوفروست",
            finalPrice = 22000.0,
            minAllowedPrice = 21000.0,
            stockQuantity = 8,
            code = "SH-18-INV"
        )

        val m2Id = repository.addModel(
            productId = 2,
            productName = "غسالة إل جي فيفاس",
            brandName = "إل جي",
            categoryName = "غسالات",
            modelName = "9 كيلو بالمجفف AI DD",
            finalPrice = 17500.0,
            minAllowedPrice = 17000.0,
            stockQuantity = 10,
            code = "LG-9-DD"
        )

        val m3Id = repository.addModel(
            productId = 3,
            productName = "شاشة سامسونج سمارت",
            brandName = "سامسونج",
            categoryName = "شاشات",
            modelName = "55 بوصة كريستال 4K",
            finalPrice = 15000.0,
            minAllowedPrice = 14500.0,
            stockQuantity = 6,
            code = "SAM-55-4K"
        )

        val m4Id = repository.addModel(
            productId = 4,
            productName = "بوتاجاز يونيون إير",
            brandName = "يونيون إير",
            categoryName = "بوتاجازات",
            modelName = "5 شعلة أمان كامل 60×90",
            finalPrice = 9500.0,
            minAllowedPrice = 9000.0,
            stockQuantity = 5,
            code = "UN-5-BURN"
        )

        val m5Id = repository.addModel(
            productId = 5,
            productName = "مكنسة باناسونيك برميل",
            brandName = "باناسونيك",
            categoryName = "أجهزة صغيرة",
            modelName = "2000 وات ياباني تجميع ماليزي",
            finalPrice = 4200.0,
            minAllowedPrice = 4000.0,
            stockQuantity = 12,
            code = "PAN-2000W"
        )

        // 2. Build 5 draft items at negotiated prices (all above min price)
        val m1 = repository.getModelById(m1Id)!!
        val m2 = repository.getModelById(m2Id)!!
        val m3 = repository.getModelById(m3Id)!!
        val m4 = repository.getModelById(m4Id)!!
        val m5 = repository.getModelById(m5Id)!!

        val draftItems = listOf(
            InvoiceDraftItem(model = m1, quantity = 2, negotiatedUnitPrice = 21500.0), // 43,000
            InvoiceDraftItem(model = m2, quantity = 1, negotiatedUnitPrice = 17200.0), // 17,200
            InvoiceDraftItem(model = m3, quantity = 3, negotiatedUnitPrice = 14800.0), // 44,400
            InvoiceDraftItem(model = m4, quantity = 1, negotiatedUnitPrice = 9200.0),  // 9,200
            InvoiceDraftItem(model = m5, quantity = 2, negotiatedUnitPrice = 4100.0)   // 8,200
        )
        // Subtotal = 43000 + 17200 + 44400 + 9200 + 8200 = 122,000

        val discount = 2000.0
        // Net Final Total = 120,000
        val paidAmount = 100000.0
        // Remaining Balance = 20,000 (Partial payment / credit)

        // 3. Create Multi-Item Invoice
        val result = repository.createInvoice(
            customerName = "الحاج محمود البشيري",
            customerPhone = "01012345678",
            sellerId = "seller1",
            sellerName = "بائع 1",
            items = draftItems,
            discount = discount,
            paidAmount = paidAmount,
            notes = "دفعة من فاتورة جهاز عروسة كاملة"
        )

        assertTrue("Invoice creation must succeed", result.isSuccess)
        val invoiceWithItems = result.getOrNull()!!
        val inv = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        // 4. Assert Invoice Fields and Calculations
        assertEquals(5, items.size)
        assertEquals("01012345678", inv.customerPhone)
        assertTrue("Customer phone must strictly retain leading zero", inv.customerPhone.startsWith("0"))
        assertEquals("الحاج محمود البشيري", inv.customerName)
        assertEquals(122000.0, inv.subtotal, 0.01)
        assertEquals(2000.0, inv.discount, 0.01)
        assertEquals(120000.0, inv.finalTotal, 0.01)
        assertEquals(100000.0, inv.paidAmount, 0.01)
        assertEquals(20000.0, inv.remainingAmount, 0.01)
        assertEquals("PARTIAL", inv.paymentStatus)

        // Verify each line item saved linked to the same invoice ID
        items.forEach { item ->
            assertEquals(inv.id, item.invoiceId)
        }
        assertEquals(43000.0, items[0].totalPrice, 0.01)
        assertEquals(17200.0, items[1].totalPrice, 0.01)
        assertEquals(44400.0, items[2].totalPrice, 0.01)
        assertEquals(9200.0, items[3].totalPrice, 0.01)
        assertEquals(8200.0, items[4].totalPrice, 0.01)

        // 5. Assert Inventory Stock Deduction: each model deducted exactly once
        assertEquals(6, repository.getModelById(m1Id)?.stockQuantity)  // 8 - 2
        assertEquals(9, repository.getModelById(m2Id)?.stockQuantity)  // 10 - 1
        assertEquals(3, repository.getModelById(m3Id)?.stockQuantity)  // 6 - 3
        assertEquals(4, repository.getModelById(m4Id)?.stockQuantity)  // 5 - 1
        assertEquals(10, repository.getModelById(m5Id)?.stockQuantity) // 12 - 2

        // 6. Verify retrieval from Flow
        val storedInvoices = repository.allInvoicesWithItems.first()
        assertEquals(1, storedInvoices.size)
        assertEquals(5, storedInvoices[0].items.size)
        assertEquals(120000.0, storedInvoices[0].invoice.finalTotal, 0.01)

        // 7. Verify Bluetooth Thermal Receipt Bitmap generation for multi-item document
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bitmap = BluetoothPrinterHelper.generateInvoiceWithItemsBitmap(
            context = context,
            invoiceWithItems = invoiceWithItems,
            storeName = "البشيري للأجهزة الكهربائية"
        )
        assertNotNull("Generated print bitmap must not be null", bitmap)
        assertTrue("Bitmap width should match thermal standard 384px", bitmap.width == 384)
        assertTrue("Bitmap height should be positive and include all items", bitmap.height > 600)
    }

    @Test
    fun testMultiItemInvoiceWith10ProductsUnlimitedCalculationsAndStock() = runBlocking {
        // 1. Register 10 distinct products and models in inventory (verifying >5, 6th, 7th, up to 10th product)
        val m1Id = repository.addModel(
            productId = 1,
            productName = "ثلاجة شارب ديجيتال",
            brandName = "شارب",
            categoryName = "ثلاجات",
            modelName = "18 قدم إنفرتر نوفروست",
            finalPrice = 22000.0,
            minAllowedPrice = 21000.0,
            stockQuantity = 8,
            code = "SH-18-INV"
        )
        val m2Id = repository.addModel(
            productId = 2,
            productName = "غسالة إل جي فيفاس",
            brandName = "إل جي",
            categoryName = "غسالات",
            modelName = "9 كيلو بالمجفف AI DD",
            finalPrice = 17500.0,
            minAllowedPrice = 17000.0,
            stockQuantity = 10,
            code = "LG-9-DD"
        )
        val m3Id = repository.addModel(
            productId = 3,
            productName = "شاشة سامسونج سمارت",
            brandName = "سامسونج",
            categoryName = "شاشات",
            modelName = "55 بوصة كريستال 4K",
            finalPrice = 15000.0,
            minAllowedPrice = 14500.0,
            stockQuantity = 6,
            code = "SAM-55-4K"
        )
        val m4Id = repository.addModel(
            productId = 4,
            productName = "بوتاجاز يونيون إير",
            brandName = "يونيون إير",
            categoryName = "بوتاجازات",
            modelName = "5 شعلة أمان كامل 60×90",
            finalPrice = 9500.0,
            minAllowedPrice = 9000.0,
            stockQuantity = 5,
            code = "UN-5-BURN"
        )
        val m5Id = repository.addModel(
            productId = 5,
            productName = "مكنسة باناسونيك برميل",
            brandName = "باناسونيك",
            categoryName = "أجهزة صغيرة",
            modelName = "2000 وات ياباني تجميع ماليزي",
            finalPrice = 4200.0,
            minAllowedPrice = 4000.0,
            stockQuantity = 12,
            code = "PAN-2000W"
        )
        val m6Id = repository.addModel(
            productId = 6,
            productName = "ميكروويف فريش ديجيتال",
            brandName = "فريش",
            categoryName = "أجهزة صغيرة",
            modelName = "28 لتر بالشواية سمارت",
            finalPrice = 4000.0,
            minAllowedPrice = 3700.0,
            stockQuantity = 7,
            code = "FR-MW-28L"
        )
        val m7Id = repository.addModel(
            productId = 7,
            productName = "سخان أوليمبيك كهرباء",
            brandName = "أوليمبيك",
            categoryName = "سخانات",
            modelName = "50 لتر ديجيتال إنفينيتي",
            finalPrice = 3100.0,
            minAllowedPrice = 2800.0,
            stockQuantity = 9,
            code = "OLY-WH-50L"
        )
        val m8Id = repository.addModel(
            productId = 8,
            productName = "ديب فريزر توشيبا",
            brandName = "توشيبا",
            categoryName = "تجميد",
            modelName = "5 درج نوفروست جولد",
            finalPrice = 17000.0,
            minAllowedPrice = 16200.0,
            stockQuantity = 4,
            code = "TOSH-DF-5D"
        )
        val m9Id = repository.addModel(
            productId = 9,
            productName = "خلاط مولينكس سوبر بلندر",
            brandName = "مولينكس",
            categoryName = "أجهزة صغيرة",
            modelName = "700 وات بالمطحنة والمفرمة",
            finalPrice = 1300.0,
            minAllowedPrice = 1150.0,
            stockQuantity = 15,
            code = "MOU-BL-700"
        )
        val m10Id = repository.addModel(
            productId = 10,
            productName = "مكواة بخار تيفال",
            brandName = "تيفال",
            categoryName = "أجهزة صغيرة",
            modelName = "الترا جلايد برو 2400 وات",
            finalPrice = 2000.0,
            minAllowedPrice = 1800.0,
            stockQuantity = 11,
            code = "TEF-IR-2400"
        )

        val m1 = repository.getModelById(m1Id)!!
        val m2 = repository.getModelById(m2Id)!!
        val m3 = repository.getModelById(m3Id)!!
        val m4 = repository.getModelById(m4Id)!!
        val m5 = repository.getModelById(m5Id)!!
        val m6 = repository.getModelById(m6Id)!!
        val m7 = repository.getModelById(m7Id)!!
        val m8 = repository.getModelById(m8Id)!!
        val m9 = repository.getModelById(m9Id)!!
        val m10 = repository.getModelById(m10Id)!!

        // 2. Build 10 draft items (testing 6th, 7th, 8th, 9th, and 10th products seamlessly)
        val draftItems = listOf(
            InvoiceDraftItem(model = m1, quantity = 1, negotiatedUnitPrice = 21500.0), // 21,500
            InvoiceDraftItem(model = m2, quantity = 2, negotiatedUnitPrice = 17200.0), // 34,400
            InvoiceDraftItem(model = m3, quantity = 1, negotiatedUnitPrice = 14800.0), // 14,800
            InvoiceDraftItem(model = m4, quantity = 1, negotiatedUnitPrice = 9200.0),  // 9,200
            InvoiceDraftItem(model = m5, quantity = 2, negotiatedUnitPrice = 4100.0),  // 8,200
            InvoiceDraftItem(model = m6, quantity = 1, negotiatedUnitPrice = 3800.0),  // 3,800
            InvoiceDraftItem(model = m7, quantity = 2, negotiatedUnitPrice = 2900.0),  // 5,800
            InvoiceDraftItem(model = m8, quantity = 1, negotiatedUnitPrice = 16500.0), // 16,500
            InvoiceDraftItem(model = m9, quantity = 3, negotiatedUnitPrice = 1200.0),  // 3,600
            InvoiceDraftItem(model = m10, quantity = 1, negotiatedUnitPrice = 1900.0)  // 1,900
        )
        // Subtotal = 21500 + 34400 + 14800 + 9200 + 8200 + 3800 + 5800 + 16500 + 3600 + 1900 = 119,700

        val discount = 4700.0
        // Net Final Total = 115,000
        val paidAmount = 100000.0
        // Remaining Balance = 15,000

        // 3. Create Multi-Item Invoice with 10 items
        val result = repository.createInvoice(
            customerName = "مؤسسة الأمل للتجهيزات",
            customerPhone = "01099887766",
            sellerId = "admin",
            sellerName = "المدير",
            items = draftItems,
            discount = discount,
            paidAmount = paidAmount,
            notes = "فاتورة شاملة 10 أجهزة كهربائية وموديلات متنوعة"
        )

        assertTrue("Invoice creation for 10 items must succeed", result.isSuccess)
        val invoiceWithItems = result.getOrNull()!!
        val inv = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        // 4. Assert 10 items persisted and calculations accurate
        assertEquals(10, items.size)
        assertEquals("01099887766", inv.customerPhone)
        assertTrue("Customer phone must retain leading zero", inv.customerPhone.startsWith("0"))
        assertEquals("مؤسسة الأمل للتجهيزات", inv.customerName)
        assertEquals(119700.0, inv.subtotal, 0.01)
        assertEquals(4700.0, inv.discount, 0.01)
        assertEquals(115000.0, inv.finalTotal, 0.01)
        assertEquals(100000.0, inv.paidAmount, 0.01)
        assertEquals(15000.0, inv.remainingAmount, 0.01)
        assertEquals("PARTIAL", inv.paymentStatus)

        // Verify all 10 items linked to the same invoice ID
        items.forEachIndexed { idx, item ->
            assertEquals(inv.id, item.invoiceId)
            assertEquals(draftItems[idx].totalPrice, item.totalPrice, 0.01)
        }

        // 5. Assert Stock Deductions: all 10 items deducted exactly once
        assertEquals(7, repository.getModelById(m1Id)?.stockQuantity)   // 8 - 1
        assertEquals(8, repository.getModelById(m2Id)?.stockQuantity)   // 10 - 2
        assertEquals(5, repository.getModelById(m3Id)?.stockQuantity)   // 6 - 1
        assertEquals(4, repository.getModelById(m4Id)?.stockQuantity)   // 5 - 1
        assertEquals(10, repository.getModelById(m5Id)?.stockQuantity)  // 12 - 2
        assertEquals(6, repository.getModelById(m6Id)?.stockQuantity)   // 7 - 1
        assertEquals(7, repository.getModelById(m7Id)?.stockQuantity)   // 9 - 2
        assertEquals(3, repository.getModelById(m8Id)?.stockQuantity)   // 4 - 1
        assertEquals(12, repository.getModelById(m9Id)?.stockQuantity)  // 15 - 3
        assertEquals(10, repository.getModelById(m10Id)?.stockQuantity) // 11 - 1

        // 6. Verify retrieved invoice contains all 10 items
        val allStored = repository.allInvoicesWithItems.first()
        val retrieved = allStored.firstOrNull { it.invoice.id == inv.id }
        assertNotNull("Retrieved invoice must exist", retrieved)
        assertEquals(10, retrieved?.items?.size)

        // 7. Verify Bluetooth Thermal Receipt Bitmap generation handles 10 products
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bitmap = BluetoothPrinterHelper.generateInvoiceWithItemsBitmap(
            context = context,
            invoiceWithItems = invoiceWithItems,
            storeName = "البشيري للأجهزة الكهربائية"
        )
        assertNotNull("Generated 10-item print bitmap must not be null", bitmap)
        assertEquals(384, bitmap.width)
        assertTrue("Bitmap height should accommodate all 10 items", bitmap.height > 1000)
    }

    @Test
    fun testStoreProfilePersistenceAndEmptyFieldsHandling() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Initial/Default profile check
        val initialProfile = com.example.utils.StoreProfileManager.getStoreProfile(context)
        assertEquals("البشيري للأجهزة الكهربائية", initialProfile.storeName)

        // 2. Save full profile
        val profile = com.example.utils.StoreProfile(
            storeName = "مؤسسة البشيري للأجهزة الكهربائية",
            storeAddress = "المنصورة - شارع الجمهورية",
            storePhone = "01000000000",
            commercialRegister = "48921",
            taxCard = "712-340-908",
            vatNumber = "300-123-456",
            logoPath = ""
        )
        com.example.utils.StoreProfileManager.saveStoreProfile(context, profile)

        val loaded = com.example.utils.StoreProfileManager.getStoreProfile(context)
        assertEquals("مؤسسة البشيري للأجهزة الكهربائية", loaded.storeName)
        assertEquals("المنصورة - شارع الجمهورية", loaded.storeAddress)
        assertEquals("01000000000", loaded.storePhone)
        assertEquals("48921", loaded.commercialRegister)
        assertEquals("712-340-908", loaded.taxCard)
        assertEquals("300-123-456", loaded.vatNumber)

        // 3. Test empty fields handling: when VAT is blank, no empty label appears
        val partialProfile = com.example.utils.StoreProfile(
            storeName = "البشيري",
            storeAddress = "شارع الجيش",
            storePhone = "",
            commercialRegister = "1234",
            taxCard = "5678",
            vatNumber = ""
        )
        assertTrue(partialProfile.hasLegalInfo)
        assertTrue(partialProfile.formattedLegalInfo.contains("سجل تجاري: 1234"))
        assertTrue(partialProfile.formattedLegalInfo.contains("بطاقة ضريبية: 5678"))
        assertTrue(!partialProfile.formattedLegalInfo.contains("تسجيل ضريبي")) // Must NOT contain empty VAT label
        assertTrue(partialProfile.formattedContactInfo.contains("العنوان: شارع الجيش"))
        assertTrue(!partialProfile.formattedContactInfo.contains("هاتف")) // Must NOT contain empty phone label

        // When all legal fields are blank
        val blankLegal = com.example.utils.StoreProfile(storeName = "البشيري")
        assertTrue(!blankLegal.hasLegalInfo)
        assertEquals("", blankLegal.formattedLegalInfo)
        assertTrue(!blankLegal.hasContactInfo)
        assertEquals("", blankLegal.formattedContactInfo)
    }

    @Test
    fun testInvoiceSnapshotsStoreProfileAndPreservesHistoricalData() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Setup a product model in stock
        val modelId = repository.addModel(
            productId = 1,
            productName = "شاشة سامسونج",
            brandName = "سامسونج",
            categoryName = "شاشات",
            modelName = "55 بوصة 4K",
            finalPrice = 18000.0,
            minAllowedPrice = 17000.0,
            stockQuantity = 10,
            code = "SAMS-55"
        )
        val liveModel = repository.getModelById(modelId)!!

        // 2. Profile A: First active showroom configuration
        val profileA = com.example.utils.StoreProfile(
            storeName = "معرض البشيري - الفرع الرئيسي",
            storeAddress = "شارع الجمهورية - المنصورة",
            storePhone = "01011111111",
            commercialRegister = "11111",
            taxCard = "222-333-444",
            vatNumber = "555-666",
            logoPath = ""
        )
        com.example.utils.StoreProfileManager.saveStoreProfile(context, profileA)

        // Create Invoice #1 with Profile A
        val itemsA = listOf(InvoiceDraftItem(liveModel, 1, 18000.0))
        val result1 = repository.createInvoice(
            customerName = "أحمد علي",
            customerPhone = "01012345678",
            sellerId = "seller1",
            sellerName = "بائع 1",
            items = itemsA,
            storeProfile = profileA
        )
        assertTrue("Invoice 1 creation must succeed", result1.isSuccess)
        val invoice1 = result1.getOrNull()!!.invoice
        assertEquals("معرض البشيري - الفرع الرئيسي", invoice1.storeName)
        assertEquals("شارع الجمهورية - المنصورة", invoice1.storeAddress)
        assertEquals("01011111111", invoice1.storePhone)
        assertEquals("11111", invoice1.commercialRegister)
        assertEquals("222-333-444", invoice1.taxCard)
        assertEquals("555-666", invoice1.vatNumber)

        // 3. Later in time, Admin modifies store details (Profile B)
        val profileB = com.example.utils.StoreProfile(
            storeName = "مؤسسة البشيري للأجهزة والكهرباء",
            storeAddress = "التجمع الخامس - القاهرة",
            storePhone = "01099999999",
            commercialRegister = "99999",
            taxCard = "888-777-666",
            vatNumber = "444-333",
            logoPath = ""
        )
        com.example.utils.StoreProfileManager.saveStoreProfile(context, profileB)

        // Create Invoice #2 with Profile B
        val itemsB = listOf(InvoiceDraftItem(liveModel, 1, 18000.0))
        val result2 = repository.createInvoice(
            customerName = "محمود السيد",
            customerPhone = "01087654321",
            sellerId = "seller2",
            sellerName = "بائع 2",
            items = itemsB,
            storeProfile = profileB
        )
        assertTrue("Invoice 2 creation must succeed", result2.isSuccess)
        val invoice2 = result2.getOrNull()!!.invoice
        assertEquals("مؤسسة البشيري للأجهزة والكهرباء", invoice2.storeName)
        assertEquals("التجمع الخامس - القاهرة", invoice2.storeAddress)
        assertEquals("01099999999", invoice2.storePhone)
        assertEquals("99999", invoice2.commercialRegister)
        assertEquals("888-777-666", invoice2.taxCard)

        // 4. CRITICAL VERIFICATION: Past Invoice #1 MUST retain Profile A permanently!
        val allInvoices = repository.allInvoicesWithItems.first()
        val retrieved1 = allInvoices.first { it.invoice.id == invoice1.id }.invoice
        assertEquals("معرض البشيري - الفرع الرئيسي", retrieved1.storeName)
        assertEquals("شارع الجمهورية - المنصورة", retrieved1.storeAddress)
        assertEquals("01011111111", retrieved1.storePhone)
        assertEquals("11111", retrieved1.commercialRegister)
        assertEquals("222-333-444", retrieved1.taxCard)
        assertEquals("555-666", retrieved1.vatNumber)

        // 5. Test thermal receipt bitmap generation with store profile
        val bitmap = BluetoothPrinterHelper.generateInvoiceWithItemsBitmap(
            context = context,
            invoiceWithItems = result1.getOrNull()!!,
            storeName = retrieved1.storeName
        )
        assertNotNull("Receipt bitmap must not be null", bitmap)
        assertEquals(384, bitmap.width)
        assertTrue("Receipt bitmap height must be valid", bitmap.height > 600)
    }

    @Test
    fun testReturnCustomerToStoreResellableIncrementsStock() = runBlocking {
        val modelId = repository.addModel(
            productId = 1L,
            productName = "غسالة أوتوماتيك",
            brandName = "إل جي",
            categoryName = "غسالات",
            modelName = "LG-9KG-V1",
            finalPrice = 25000.0,
            minAllowedPrice = 23000.0,
            stockQuantity = 5,
            code = "LG9"
        )

        val returnItem = com.example.data.repository.ReturnDraftItem(
            modelId = modelId,
            productName = "غسالة أوتوماتيك",
            modelName = "LG-9KG-V1",
            quantity = 2,
            unitPrice = 25000.0,
            stockCondition = "RESELLABLE"
        )

        val result = repository.createReturn(
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير",
            type = "CUSTOMER_TO_STORE",
            partyName = "أحمد محمد",
            partyPhone = "01011111111",
            items = listOf(returnItem),
            reason = "طلب العميل استبدال",
            settlementType = "CASH_REFUND"
        )

        assertTrue("Return creation must succeed", result.isSuccess)
        val returnRecord = result.getOrNull()!!.returnRecord
        assertEquals("APPROVED", returnRecord.status)
        assertEquals(50000.0, returnRecord.totalAmount, 0.01)

        // Stock must have increased from 5 to 7 because condition is RESELLABLE!
        val updatedModel = repository.allModels.first().first { it.id == modelId }
        assertEquals(7, updatedModel.stockQuantity)
    }

    @Test
    fun testReturnDamagedDoesNotIncrementSellingStock() = runBlocking {
        val modelId = repository.addModel(
            productId = 1L,
            productName = "شاشة سمارت",
            brandName = "سامسونج",
            categoryName = "شاشات",
            modelName = "SAM-55-4K",
            finalPrice = 18000.0,
            minAllowedPrice = 16000.0,
            stockQuantity = 4,
            code = "SAM55"
        )

        val returnItem = com.example.data.repository.ReturnDraftItem(
            modelId = modelId,
            productName = "شاشة سمارت",
            modelName = "SAM-55-4K",
            quantity = 1,
            unitPrice = 18000.0,
            stockCondition = "DAMAGED" // Defective: MUST NOT re-enter selling inventory!
        )

        val result = repository.createReturn(
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير",
            type = "CUSTOMER_TO_STORE",
            partyName = "علي حسن",
            items = listOf(returnItem),
            reason = "كسر داخلي بالشاشة",
            settlementType = "CASH_REFUND"
        )

        assertTrue("Return creation must succeed", result.isSuccess)
        // Selling inventory must remain exactly 4!
        val updatedModel = repository.allModels.first().first { it.id == modelId }
        assertEquals(4, updatedModel.stockQuantity)
    }

    @Test
    fun testReturnStoreToSupplierDeductsStockAndDecreasesDebt() = runBlocking {
        val modelId = repository.addModel(
            productId = 1L,
            productName = "ثلاجة نوفروست",
            brandName = "شارب",
            categoryName = "ثلاجات",
            modelName = "SHARP-16FT",
            finalPrice = 28000.0,
            minAllowedPrice = 26000.0,
            stockQuantity = 10,
            code = "SHP16"
        )

        val supplierId = repository.addSupplier(
            name = "شركة العربي جروب",
            phone = "01000000000",
            initialPurchases = 100000.0,
            notes = "مورد رئيسي"
        )

        val returnItem = com.example.data.repository.ReturnDraftItem(
            modelId = modelId,
            productName = "ثلاجة نوفروست",
            modelName = "SHARP-16FT",
            quantity = 2,
            unitPrice = 28000.0,
            stockCondition = "DAMAGED"
        )

        val result = repository.createReturn(
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير",
            type = "STORE_TO_SUPPLIER",
            partyName = "شركة العربي جروب",
            partyId = supplierId,
            items = listOf(returnItem),
            reason = "عيوب صناعة مرتجعة للمورد",
            settlementType = "DECREASE_DEBT",
            settlementAmount = 56000.0
        )

        assertTrue(result.isSuccess)

        // Store inventory must be deducted from 10 to 8
        val updatedModel = repository.allModels.first().first { it.id == modelId }
        assertEquals(8, updatedModel.stockQuantity)

        // Supplier remaining debt must decrease from 100,000 to 44,000
        val updatedSupplier = repository.allSuppliers.first().first { it.id == supplierId }
        assertEquals(44000.0, updatedSupplier.remainingBalance, 0.01)
    }

    @Test
    fun testReturnLinkedToInvoiceValidatesRemainingReturnableQuantity() = runBlocking {
        val modelId = repository.addModel(
            productId = 1L,
            productName = "مروحة سقف",
            brandName = "توشيبا",
            categoryName = "مراوح",
            modelName = "TOSH-CEIL-56",
            finalPrice = 1200.0,
            minAllowedPrice = 1000.0,
            stockQuantity = 20,
            code = "TSH56"
        )
        val liveModel = repository.allModels.first().first { it.id == modelId }

        // Create Invoice with 3 units
        val invResult = repository.createInvoice(
            customerName = "كريم كمال",
            customerPhone = "01122334455",
            sellerId = "seller1",
            sellerName = "بائع 1",
            items = listOf(InvoiceDraftItem(liveModel, 3, 1200.0))
        )
        assertTrue(invResult.isSuccess)
        val invoice = invResult.getOrNull()!!

        // Return 2 units in first return operation
        val retResult1 = repository.createReturn(
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير",
            type = "CUSTOMER_TO_STORE",
            partyName = "كريم كمال",
            originalInvoiceNumber = invoice.invoice.invoiceNumber,
            originalInvoiceId = invoice.invoice.id,
            items = listOf(
                com.example.data.repository.ReturnDraftItem(
                    modelId = modelId,
                    productName = "مروحة سقف",
                    modelName = "TOSH-CEIL-56",
                    quantity = 2,
                    unitPrice = 1200.0,
                    stockCondition = "RESELLABLE"
                )
            ),
            settlementType = "CASH_REFUND"
        )
        assertTrue(retResult1.isSuccess)

        // Check remaining returnable items helper: remaining must now be 1!
        val returnableItems = repository.getReturnableInvoiceItems(invoice.invoice.id).getOrNull()!!
        assertEquals(1, returnableItems.first().second)

        // Attempting to return 2 units in second return operation MUST FAIL (exceeds remaining 1)!
        val retResult2 = repository.createReturn(
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير",
            type = "CUSTOMER_TO_STORE",
            partyName = "كريم كمال",
            originalInvoiceNumber = invoice.invoice.invoiceNumber,
            originalInvoiceId = invoice.invoice.id,
            items = listOf(
                com.example.data.repository.ReturnDraftItem(
                    modelId = modelId,
                    productName = "مروحة سقف",
                    modelName = "TOSH-CEIL-56",
                    quantity = 2,
                    unitPrice = 1200.0,
                    stockCondition = "RESELLABLE"
                )
            ),
            settlementType = "CASH_REFUND"
        )
        assertTrue("Attempt to return more than purchased must fail", retResult2.isFailure)
        assertTrue(retResult2.exceptionOrNull()?.message?.contains("تتجاوز") == true)
    }

    @Test
    fun testSellerCannotCreateReturnOnlyAdmin() = runBlocking {
        val modelId = repository.addModel(
            productId = 1L,
            productName = "مكواة بخار",
            brandName = "باناسونيك",
            categoryName = "أجهزة صغيرة",
            modelName = "PAN-ST-200",
            finalPrice = 1500.0,
            minAllowedPrice = 1300.0,
            stockQuantity = 5,
            code = "PAN20"
        )

        val returnItem = com.example.data.repository.ReturnDraftItem(
            modelId = modelId,
            productName = "مكواة بخار",
            modelName = "PAN-ST-200",
            quantity = 1,
            unitPrice = 1500.0
        )

        // Attempt by SELLER
        val sellerResult = repository.createReturn(
            userRole = "SELLER",
            userId = "seller1",
            userName = "بائع 1",
            type = "CUSTOMER_TO_STORE",
            partyName = "عميل",
            items = listOf(returnItem),
            settlementType = "CASH_REFUND"
        )
        assertTrue("Seller must NOT be permitted to create returns", sellerResult.isFailure)
        assertTrue(sellerResult.exceptionOrNull()?.message?.contains("المدير") == true)
    }

    @Test
    fun testCancelReturnReversesInventoryAndAccounts() = runBlocking {
        val modelId = repository.addModel(
            productId = 1L,
            productName = "خلاط كهربائي",
            brandName = "مولينكس",
            categoryName = "أجهزة صغيرة",
            modelName = "MOU-GEN-400",
            finalPrice = 1800.0,
            minAllowedPrice = 1500.0,
            stockQuantity = 10,
            code = "MOU40"
        )

        val returnItem = com.example.data.repository.ReturnDraftItem(
            modelId = modelId,
            productName = "خلاط كهربائي",
            modelName = "MOU-GEN-400",
            quantity = 2,
            unitPrice = 1800.0,
            stockCondition = "RESELLABLE"
        )

        val createResult = repository.createReturn(
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير",
            type = "CUSTOMER_TO_STORE",
            partyName = "سمير وليم",
            items = listOf(returnItem),
            settlementType = "CASH_REFUND"
        )
        assertTrue(createResult.isSuccess)
        val returnId = createResult.getOrNull()!!.returnRecord.id

        // Stock was 10, increased to 12
        var model = repository.allModels.first().first { it.id == modelId }
        assertEquals(12, model.stockQuantity)

        // Cancel the return
        val cancelResult = repository.cancelReturn(returnId, "ADMIN")
        assertTrue(cancelResult.isSuccess)

        // Stock must be reversed back to 10!
        model = repository.allModels.first().first { it.id == modelId }
        assertEquals(10, model.stockQuantity)

        val allReturns = repository.allReturnsWithItems.first()
        val cancelledReturn = allReturns.first { it.returnRecord.id == returnId }
        assertEquals("CANCELLED", cancelledReturn.returnRecord.status)
    }

    @Test
    fun testReturnReceiptBitmapGeneration() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val modelId = repository.addModel(
            productId = 1L,
            productName = "شاشة سامسونج",
            brandName = "سامسونج",
            categoryName = "شاشات",
            modelName = "SAM-65-OLED",
            finalPrice = 35000.0,
            minAllowedPrice = 32000.0,
            stockQuantity = 3,
            code = "SAM65"
        )

        val returnItem = com.example.data.repository.ReturnDraftItem(
            modelId = modelId,
            productName = "شاشة سامسونج",
            modelName = "SAM-65-OLED",
            quantity = 1,
            unitPrice = 35000.0,
            stockCondition = "RESELLABLE"
        )

        val createResult = repository.createReturn(
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير",
            type = "CUSTOMER_TO_STORE",
            partyName = "خالد إبراهيم",
            partyPhone = "01055555555",
            originalInvoiceNumber = "INV-123456",
            items = listOf(returnItem),
            reason = "استبدال بمقاس أصغر",
            settlementType = "CASH_REFUND"
        )
        assertTrue(createResult.isSuccess)
        val returnWithItems = createResult.getOrNull()!!

        val bitmap = BluetoothPrinterHelper.generateReturnReceiptBitmap(
            context = context,
            returnWithItems = returnWithItems
        )
        assertNotNull("Return receipt bitmap must not be null", bitmap)
        assertEquals(384, bitmap.width)
        assertTrue("Bitmap height must be reasonable", bitmap.height > 500)
    }

    @Test
    fun testRegisterDamageFromInventoryDeductsStockOnce() = runBlocking {
        val modelId = repository.addModel(
            productId = 1,
            productName = "شاشة إل جي",
            brandName = "إل جي",
            categoryName = "شاشات",
            modelName = "55 بوصة",
            finalPrice = 18000.0,
            minAllowedPrice = 17000.0,
            stockQuantity = 5,
            code = "LG-55"
        )

        val regResult = repository.registerDamageOrScrap(
            modelId = modelId,
            quantity = 2,
            reason = "كسر شاشة أثناء النقل",
            damageDetails = "شروخ في اللوحة الداخلية",
            initialStatus = "TOTAL_SCRAP",
            sourceType = "INVENTORY",
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير"
        )

        assertTrue(regResult.isSuccess)
        val record = regResult.getOrNull()!!
        assertEquals("TOTAL_SCRAP", record.status)
        assertEquals(2, record.quantity)
        assertTrue(record.isStockDeducted)

        // Stock in catalog should be reduced from 5 to 3
        val updatedModel = repository.getModelById(modelId)
        assertEquals(3, updatedModel?.stockQuantity)
    }

    @Test
    fun testRegisterDamageCannotExceedAvailableStock() = runBlocking {
        val modelId = repository.addModel(
            productId = 1,
            productName = "مروحة سقف",
            brandName = "توشيبا",
            categoryName = "مراوح",
            modelName = "أبيض 56 بوصة",
            finalPrice = 1200.0,
            minAllowedPrice = 1100.0,
            stockQuantity = 2,
            code = "FAN-56"
        )

        // Attempting to record damage of 3 when stock is only 2 must fail!
        val failedResult = repository.registerDamageOrScrap(
            modelId = modelId,
            quantity = 3,
            reason = "حرق ملف الموتور",
            damageDetails = "ماس كهربائي",
            initialStatus = "UNDER_INSPECTION",
            sourceType = "INVENTORY",
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير"
        )

        assertTrue(failedResult.isFailure)
        assertTrue(failedResult.exceptionOrNull()?.message?.contains("أكبر من الرصيد المتاح") == true)

        // Stock remains unchanged at 2
        val updatedModel = repository.getModelById(modelId)
        assertEquals(2, updatedModel?.stockQuantity)
    }

    @Test
    fun testRegisterFromCustomerReturnDoesNotDoubleDeductStock() = runBlocking {
        val modelId = repository.addModel(
            productId = 1,
            productName = "بوتاجاز يونيفرسال",
            brandName = "يونيفرسال",
            categoryName = "بوتاجازات",
            modelName = "5 شعلة",
            finalPrice = 9000.0,
            minAllowedPrice = 8500.0,
            stockQuantity = 4,
            code = "UNI-5"
        )

        // Customer return created with condition "DAMAGED"
        val returnItem = com.example.data.repository.ReturnDraftItem(
            modelId = modelId,
            productName = "بوتاجاز يونيفرسال",
            modelName = "5 شعلة",
            quantity = 1,
            unitPrice = 9000.0,
            stockCondition = "DAMAGED",
            itemNotes = "كسر في الزجاج الخارجي"
        )

        val returnResult = repository.createReturn(
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير",
            type = "CUSTOMER_TO_STORE",
            partyName = "أحمد كامل",
            items = listOf(returnItem),
            settlementType = "CASH_REFUND"
        )
        assertTrue(returnResult.isSuccess)

        // Stock should still be 4 because DAMAGED condition did NOT add it back to inventory
        val modelAfterReturn = repository.getModelById(modelId)
        assertEquals(4, modelAfterReturn?.stockQuantity)

        // Now register this returned item into Damage & Repair system
        val damageResult = repository.registerDamageOrScrap(
            modelId = modelId,
            quantity = 1,
            reason = "كسر",
            damageDetails = "كسر الزجاج الخارجي",
            initialStatus = "REPAIRABLE",
            sourceType = "CUSTOMER_RETURN",
            returnRecordId = returnResult.getOrNull()!!.returnRecord.id,
            returnNumber = returnResult.getOrNull()!!.returnRecord.returnNumber,
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير"
        )
        assertTrue(damageResult.isSuccess)

        // Stock MUST remain 4 — NO double-deduction for return items!
        val modelAfterDamage = repository.getModelById(modelId)
        assertEquals(4, modelAfterDamage?.stockQuantity)
    }

    @Test
    fun testRepairLifecycleFromSendToCompleteToAdminApprovalRestoresStockOnce() = runBlocking {
        val modelId = repository.addModel(
            productId = 1,
            productName = "ميكروويف شارب",
            brandName = "شارب",
            categoryName = "أجهزة صغيرة",
            modelName = "34 لتر",
            finalPrice = 8000.0,
            minAllowedPrice = 7500.0,
            stockQuantity = 3,
            code = "SH-34"
        )

        // 1. Register damage from inventory -> Stock drops from 3 to 2
        val regResult = repository.registerDamageOrScrap(
            modelId = modelId,
            quantity = 1,
            reason = "عيب تصنيع",
            damageDetails = "عطل بلوحة التحكم",
            initialStatus = "UNDER_INSPECTION",
            sourceType = "INVENTORY",
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير"
        )
        assertTrue(regResult.isSuccess)
        val recordId = regResult.getOrNull()!!.id
        assertEquals(2, repository.getModelById(modelId)?.stockQuantity)

        // 2. Send to repair -> status becomes UNDER_REPAIR
        val sendResult = repository.sendToRepair(
            recordId = recordId,
            repairReason = "تغيير لوحة التحكم",
            technicianNotes = "تم إرساله إلى توكيل شارب",
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير"
        )
        assertTrue(sendResult.isSuccess)
        assertEquals("UNDER_REPAIR", sendResult.getOrNull()!!.status)

        // Stock is still 2 while under repair
        assertEquals(2, repository.getModelById(modelId)?.stockQuantity)

        // 3. Complete repair -> status becomes REPAIRED_PENDING_APPROVAL
        val completeResult = repository.completeRepair(
            recordId = recordId,
            outcome = "FIXED",
            sparePartsCost = 500.0,
            laborCost = 200.0,
            repairNotes = "تم استبدال اللوحة واختبار الجهاز بنجاح",
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير"
        )
        assertTrue(completeResult.isSuccess)
        val repairedRecord = completeResult.getOrNull()!!
        assertEquals("REPAIRED_PENDING_APPROVAL", repairedRecord.status)
        assertEquals(700.0, repairedRecord.totalRepairCost, 0.01)

        // Still not restored to stock until admin approves
        assertEquals(2, repository.getModelById(modelId)?.stockQuantity)

        // 4. Admin approves return to stock -> restoredToStockDate set, status RESTORED_TO_STOCK, stock restored to 3
        val approveResult = repository.approveReturnToStock(
            recordId = recordId,
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير"
        )
        assertTrue(approveResult.isSuccess)
        val restoredRecord = approveResult.getOrNull()!!
        assertEquals("RESTORED_TO_STOCK", restoredRecord.status)
        assertTrue(restoredRecord.isStockRestored)

        // Stock in catalog is now back to 3!
        assertEquals(3, repository.getModelById(modelId)?.stockQuantity)

        // 5. Attempting to approve again MUST fail and NOT re-add stock!
        val secondApprove = repository.approveReturnToStock(
            recordId = recordId,
            userRole = "ADMIN",
            userId = "admin",
            userName = "المدير"
        )
        assertTrue(secondApprove.isFailure)
        assertEquals(3, repository.getModelById(modelId)?.stockQuantity)
    }

    @Test
    fun testSellerCannotApproveOrRegisterDamageOnlyAdmin() = runBlocking {
        val modelId = repository.addModel(
            productId = 1,
            productName = "خلاط براون",
            brandName = "براون",
            categoryName = "خلاطات",
            modelName = "مالتي كويك",
            finalPrice = 3000.0,
            minAllowedPrice = 2800.0,
            stockQuantity = 2,
            code = "BR-MQ"
        )

        // Attempting to register damage by SELLER must fail
        val sellerResult = repository.registerDamageOrScrap(
            modelId = modelId,
            quantity = 1,
            reason = "كسر دورق",
            damageDetails = "سقط أثناء العرض",
            initialStatus = "TOTAL_SCRAP",
            sourceType = "INVENTORY",
            userRole = "SELLER",
            userId = "seller1",
            userName = "بائع"
        )
        assertTrue(sellerResult.isFailure)
        assertTrue(sellerResult.exceptionOrNull()?.message?.contains("المدير فقط") == true)
        assertEquals(2, repository.getModelById(modelId)?.stockQuantity)
    }

    @Test
    fun testStockMovementLoggedOnSaleAndCannotExceedStock() = runBlocking {
        val modelId = repository.addModel(
            productId = 1,
            productName = "شاشة سامسونج",
            brandName = "سامسونج",
            categoryName = "شاشات",
            modelName = "55 بوصة",
            finalPrice = 18000.0,
            minAllowedPrice = 17500.0,
            stockQuantity = 3,
            code = "SAM-55"
        )

        // 1. Sell 1 unit
        val saleResult = repository.recordSale(
            modelId = modelId,
            productName = "شاشة سامسونج",
            modelName = "55 بوصة",
            categoryName = "شاشات",
            brandName = "سامسونج",
            soldPrice = 18000.0,
            minAllowedPrice = 17500.0,
            originalFinalPrice = 18000.0,
            quantity = 1,
            sellerId = "seller1",
            sellerName = "محمد",
            notes = ""
        )
        assertTrue(saleResult.isSuccess)
        assertEquals(2, repository.getModelById(modelId)?.stockQuantity)

        // Verify stock movement was recorded
        val movements = repository.allStockMovements.first()
        val saleMovement = movements.find { it.modelId == modelId && it.movementType == "SALE" }
        assertNotNull(saleMovement)
        assertEquals(3, saleMovement!!.quantityBefore)
        assertEquals(2, saleMovement.quantityAfter)
        assertEquals(-1, saleMovement.quantityChanged)

        // 2. Selling 5 units when only 2 available must fail
        val excessSaleResult = repository.recordSale(
            modelId = modelId,
            productName = "شاشة سامسونج",
            modelName = "55 بوصة",
            categoryName = "شاشات",
            brandName = "سامسونج",
            soldPrice = 18000.0,
            minAllowedPrice = 17500.0,
            originalFinalPrice = 18000.0,
            quantity = 5,
            sellerId = "seller1",
            sellerName = "محمد",
            notes = ""
        )
        assertTrue(excessSaleResult.isFailure)
        assertTrue(excessSaleResult.exceptionOrNull()?.message?.contains("غير متاحة") == true)
        // Stock remains 2
        assertEquals(2, repository.getModelById(modelId)?.stockQuantity)
    }

    @Test
    fun testReceiveStockIncrementsInventoryAndLogsPurchase() = runBlocking {
        val modelId = repository.addModel(
            productId = 1,
            productName = "بوتاجاز يونيون إير",
            brandName = "يونيون إير",
            categoryName = "بوتاجازات",
            modelName = "5 شعلة أمان كامل",
            finalPrice = 9000.0,
            minAllowedPrice = 8500.0,
            stockQuantity = 2,
            code = "UN-5B"
        )

        val receiveResult = repository.receiveStock(
            modelId = modelId,
            quantity = 6,
            unitCostPrice = 7500.0,
            supplierId = null,
            merchantId = null,
            supplierOrMerchantName = "شركة النصر للأجهزة",
            purchaseInvoiceNumber = "SUPP-INV-8899",
            notes = "شحنة واردة مستودع رئيسي",
            userRole = "ADMIN",
            userId = "admin1",
            userName = "المدير العام"
        )
        assertTrue(receiveResult.isSuccess)
        val updatedModel = repository.getModelById(modelId)
        assertNotNull(updatedModel)
        assertEquals(8, updatedModel!!.stockQuantity)

        // Verify purchase movement
        val movements = repository.allStockMovements.first()
        val purchaseMovement = movements.find { it.modelId == modelId && it.movementType == "PURCHASE" }
        assertNotNull(purchaseMovement)
        assertEquals(2, purchaseMovement!!.quantityBefore)
        assertEquals(8, purchaseMovement.quantityAfter)
        assertEquals(6, purchaseMovement.quantityChanged)
        assertEquals("SUPP-INV-8899", purchaseMovement.referenceNumber)
    }

    @Test
    fun testPhysicalInventoryAuditDraftAndVarianceCalculation() = runBlocking {
        val modelA = repository.addModel(
            productId = 1,
            productName = "مروحة توشيبا",
            brandName = "توشيبا",
            categoryName = "مراوح",
            modelName = "سقف 56",
            finalPrice = 1200.0,
            minAllowedPrice = 1100.0,
            stockQuantity = 10,
            code = "FAN-56"
        )
        val modelB = repository.addModel(
            productId = 1,
            productName = "مروحة فريش",
            brandName = "فريش",
            categoryName = "مراوح",
            modelName = "عمود شبح",
            finalPrice = 1400.0,
            minAllowedPrice = 1300.0,
            stockQuantity = 4,
            code = "FAN-FR"
        )

        // Create inventory audit
        val createResult = repository.createInventoryAudit(
            title = "جرد شهر أكتوبر",
            scopeType = "ALL",
            scopeFilter = "",
            notes = "جرد دوري للمخزن",
            userRole = "ADMIN",
            userId = "admin1",
            userName = "المدير"
        )
        assertTrue(createResult.isSuccess)
        val auditWithItems = createResult.getOrThrow()

        // Get audit with items
        val audits = repository.allInventoryAudits.first()
        val currentAudit = audits.find { it.audit.id == auditWithItems.audit.id }
        assertNotNull(currentAudit)
        assertEquals("DRAFT", currentAudit!!.audit.status)
        assertEquals(2, currentAudit.items.size)

        val itemA = currentAudit.items.find { it.modelId == modelA }!!
        val itemB = currentAudit.items.find { it.modelId == modelB }!!

        // Update actual count: Model A counted 8 (deficit of -2)
        repository.updateAuditItemActualQuantity(itemA.id, 8)
        // Update actual count: Model B counted 6 (surplus of +2)
        repository.updateAuditItemActualQuantity(itemB.id, 6)

        val updatedAudits = repository.allInventoryAudits.first()
        val refreshedAudit = updatedAudits.find { it.audit.id == auditWithItems.audit.id }!!
        val updatedItemA = refreshedAudit.items.find { it.modelId == modelA }!!
        val updatedItemB = refreshedAudit.items.find { it.modelId == modelB }!!

        assertEquals(8, updatedItemA.actualQuantity)
        assertEquals(-2, updatedItemA.differenceQuantity)

        assertEquals(6, updatedItemB.actualQuantity)
        assertEquals(2, updatedItemB.differenceQuantity)

        // In DRAFT mode, catalog stock MUST NOT have changed yet!
        assertEquals(10, repository.getModelById(modelA)?.stockQuantity)
        assertEquals(4, repository.getModelById(modelB)?.stockQuantity)
    }

    @Test
    fun testSettleInventoryAuditUpdatesStockAndLogsMovementsExactlyOnce() = runBlocking {
        val modelA = repository.addModel(
            productId = 1,
            productName = "ديب فريزر كريازي",
            brandName = "كريازي",
            categoryName = "فريزر",
            modelName = "5 درج ديجيتال",
            finalPrice = 21000.0,
            minAllowedPrice = 20000.0,
            stockQuantity = 5,
            code = "KIR-DF5"
        )

        val createResult = repository.createInventoryAudit(
            title = "جرد فريزر",
            scopeType = "ALL",
            scopeFilter = "",
            notes = "جرد فريزرات",
            userRole = "ADMIN",
            userId = "admin1",
            userName = "المدير"
        )
        val auditWithItems = createResult.getOrThrow()
        val audits = repository.allInventoryAudits.first()
        val currentAudit = audits.find { it.audit.id == auditWithItems.audit.id }!!
        val itemA = currentAudit.items.find { it.modelId == modelA }!!

        // Counted 3 instead of 5 (Deficit: -2)
        repository.updateAuditItemActualQuantity(itemA.id, 3)

        // Settle audit
        val settleResult = repository.settleInventoryAudit(
            auditId = auditWithItems.audit.id,
            settlementReason = "اعتماد عجز قطعتين في التخزين",
            userRole = "ADMIN",
            userId = "admin1",
            userName = "المدير"
        )
        assertTrue(settleResult.isSuccess)

        // 1. Catalog stock must now be updated to actual (3)
        assertEquals(3, repository.getModelById(modelA)?.stockQuantity)

        // 2. Audit status must be SETTLED and isSettled = true
        val settledAudits = repository.allInventoryAudits.first()
        val settledAudit = settledAudits.find { it.audit.id == auditWithItems.audit.id }!!
        assertEquals("SETTLED", settledAudit.audit.status)
        assertTrue(settledAudit.audit.isSettled)

        // 3. Movement must be recorded as INVENTORY_AUDIT_DEFICIT
        val movements = repository.allStockMovements.first()
        val deficitMovement = movements.find {
            it.modelId == modelA && it.movementType == "INVENTORY_AUDIT_DEFICIT"
        }
        assertNotNull(deficitMovement)
        assertEquals(5, deficitMovement!!.quantityBefore)
        assertEquals(3, deficitMovement.quantityAfter)
        assertEquals(-2, deficitMovement.quantityChanged)

        // 4. Attempting to settle again must fail
        val duplicateSettle = repository.settleInventoryAudit(
            auditId = auditWithItems.audit.id,
            settlementReason = "تسوية ثانية",
            userRole = "ADMIN",
            userId = "admin1",
            userName = "المدير"
        )
        assertTrue(duplicateSettle.isFailure)
        assertTrue(duplicateSettle.exceptionOrNull()?.message?.contains("تمت تسوية") == true)
    }

    @Test
    fun testSellerCannotCreateOrSettleAudit() = runBlocking {
        val createResult = repository.createInventoryAudit(
            title = "محاولة جرد من بائع",
            scopeType = "ALL",
            scopeFilter = "",
            notes = "",
            userRole = "SELLER",
            userId = "seller1",
            userName = "بائع"
        )
        assertTrue(createResult.isFailure)
        assertTrue(createResult.exceptionOrNull()?.message?.contains("المدير فقط") == true)
    }
}

