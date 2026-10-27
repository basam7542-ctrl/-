package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ProductModelEntity
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
            recycleBinDao = db.recycleBinDao()
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
            notes = "بيع ناجح"
        )
        assertTrue(successResult.isSuccess)

        // Stock should be deducted from 5 to 3
        val modelAfterSuccess = repository.getModelById(modelId)
        assertEquals(3, modelAfterSuccess?.stockQuantity)

        // Sales records should contain 1 transaction
        val sales = repository.allSales.first()
        assertEquals(1, sales.size)
        assertEquals(28000.0, sales[0].totalPrice, 0.01)
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
}
