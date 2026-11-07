package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BrandEntity
import com.example.data.local.CategoryEntity
import com.example.ui.MainTab
import com.example.voice.BasheerVoiceIntent
import com.example.voice.BasheerVoiceParser
import com.example.voice.TtsHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BasheerVoiceAssistantTest {

    private lateinit var categories: List<CategoryEntity>
    private lateinit var brands: List<BrandEntity>
    private lateinit var context: Context
    private lateinit var ttsHelper: TtsHelper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        ttsHelper = TtsHelper(context)

        categories = listOf(
            CategoryEntity(id = 1, name = "شاشات"),
            CategoryEntity(id = 2, name = "ثلاجات"),
            CategoryEntity(id = 3, name = "غسالات"),
            CategoryEntity(id = 4, name = "بوتاجازات"),
            CategoryEntity(id = 5, name = "تكييفات")
        )

        brands = listOf(
            BrandEntity(id = 1, name = "شارب", categoryId = 2),
            BrandEntity(id = 2, name = "توشيبا", categoryId = 3),
            BrandEntity(id = 3, name = "إل جي", categoryId = 3),
            BrandEntity(id = 4, name = "سامسونج", categoryId = 1),
            BrandEntity(id = 5, name = "تورنيدو", categoryId = 1)
        )
    }

    // --- 1. Egyptian Number to Speech Engine Tests ---

    @Test
    fun testEgyptianCurrencySpeechFormatting() {
        assertEquals("خمسمية جنيه", ttsHelper.formatEgyptianCurrencyForSpeech(500.0))
        assertEquals("ألف جنيه", ttsHelper.formatEgyptianCurrencyForSpeech(1000.0))
        assertEquals("ألف وخمسمية جنيه", ttsHelper.formatEgyptianCurrencyForSpeech(1500.0))
        assertEquals("ألفين جنيه", ttsHelper.formatEgyptianCurrencyForSpeech(2000.0))
        assertEquals("اتناشر ألف جنيه", ttsHelper.formatEgyptianCurrencyForSpeech(12000.0))
        assertEquals("خمستاشر ألف جنيه", ttsHelper.formatEgyptianCurrencyForSpeech(15000.0))
        assertEquals("اتنين وتلاتين ألف وخمسمية جنيه", ttsHelper.formatEgyptianCurrencyForSpeech(32500.0))
        assertEquals("صفر جنيه", ttsHelper.formatEgyptianCurrencyForSpeech(0.0))
    }

    // --- 2. Egyptian Navigation Commands Tests ---

    @Test
    fun testNavigationIntents() {
        // Accounts & Cash Treasury
        val accIntent1 = BasheerVoiceParser.parse("افتح الخزنة", categories, brands)
        assertTrue(accIntent1 is BasheerVoiceIntent.Navigate)
        assertEquals(MainTab.ACCOUNTS, (accIntent1 as BasheerVoiceIntent.Navigate).targetTab)

        val accIntent2 = BasheerVoiceParser.parse("روح الحسابات والمصروفات", categories, brands)
        assertTrue(accIntent2 is BasheerVoiceIntent.Navigate)
        assertEquals(MainTab.ACCOUNTS, (accIntent2 as BasheerVoiceIntent.Navigate).targetTab)

        // Catalog & Warehouse
        val catIntent = BasheerVoiceParser.parse("وريني بضاعة المخزن", categories, brands)
        assertTrue(catIntent is BasheerVoiceIntent.Navigate)
        assertEquals(MainTab.CATALOG, (catIntent as BasheerVoiceIntent.Navigate).targetTab)

        // Sales & Invoices
        val salesIntent = BasheerVoiceParser.parse("افتح شاشة المبيعات", categories, brands)
        assertTrue(salesIntent is BasheerVoiceIntent.Navigate)
        assertEquals(MainTab.SALES_LOG, (salesIntent as BasheerVoiceIntent.Navigate).targetTab)

        // Returns
        val retIntent = BasheerVoiceParser.parse("هات شاشة المرتجعات", categories, brands)
        assertTrue(retIntent is BasheerVoiceIntent.Navigate)
        assertEquals(MainTab.RETURNS, (retIntent as BasheerVoiceIntent.Navigate).targetTab)

        // Damage & Repair
        val damIntent = BasheerVoiceParser.parse("افتح قسم الهالك والصيانة", categories, brands)
        assertTrue(damIntent is BasheerVoiceIntent.Navigate)
        assertEquals(MainTab.DAMAGE_REPAIR, (damIntent as BasheerVoiceIntent.Navigate).targetTab)

        // Inventory Audit & Movements
        val invIntent = BasheerVoiceParser.parse("افتح شاشة الجرد", categories, brands)
        assertTrue(invIntent is BasheerVoiceIntent.Navigate)
        assertEquals(MainTab.INVENTORY, (invIntent as BasheerVoiceIntent.Navigate).targetTab)

        // Settings
        val setIntent = BasheerVoiceParser.parse("افتح الإعدادات", categories, brands)
        assertTrue(setIntent is BasheerVoiceIntent.Navigate)
        assertEquals(MainTab.SETTINGS, (setIntent as BasheerVoiceIntent.Navigate).targetTab)

        // Recycle Bin
        val binIntent = BasheerVoiceParser.parse("افتح سلة المحذوفات", categories, brands)
        assertTrue(binIntent is BasheerVoiceIntent.Navigate)
        assertEquals(MainTab.RECYCLE_BIN, (binIntent as BasheerVoiceIntent.Navigate).targetTab)
    }

    // --- 3. Price & Stock Inquiry Tests ---

    @Test
    fun testPriceAndStockInquiry() {
        val q1 = BasheerVoiceParser.parse("سعر غسالة إل جي 9 كيلو كام؟", categories, brands)
        assertTrue(q1 is BasheerVoiceIntent.SearchPrice)
        assertTrue((q1 as BasheerVoiceIntent.SearchPrice).query.contains("غسال"))

        val q2 = BasheerVoiceParser.parse("بكام ثلاجة شارب 18 قدم؟", categories, brands)
        assertTrue(q2 is BasheerVoiceIntent.SearchPrice)
        assertTrue((q2 as BasheerVoiceIntent.SearchPrice).query.contains("ثلاج"))

        val q3 = BasheerVoiceParser.parse("فيه عندنا شاشات سامسونج 55؟", categories, brands)
        assertTrue(q3 is BasheerVoiceIntent.SearchPrice)
        assertTrue((q3 as BasheerVoiceIntent.SearchPrice).query.contains("شاش"))
    }

    // --- 4. Starting a Sale / Invoice ---

    @Test
    fun testStartSaleIntent() {
        val sale1 = BasheerVoiceParser.parse("عايز ابيع ثلاجة شارب 18 قدم", categories, brands)
        assertTrue(sale1 is BasheerVoiceIntent.StartSale)
        assertTrue((sale1 as BasheerVoiceIntent.StartSale).query.contains("شارب"))

        val sale2 = BasheerVoiceParser.parse("طلع فاتورة لـ الحاج أحمد", categories, brands)
        assertTrue(sale2 is BasheerVoiceIntent.StartSale)
    }

    // --- 5. Add Product & Parsing Fields ---

    @Test
    fun testAddProductIntent() {
        val addCmd = "ضيف ثلاجة شارب 18 قدم نوفروست، قسم الثلاجات، شركة شارب، السعر 32500 والحد الأدنى 31000 والكمية 4"
        val parsed = BasheerVoiceParser.parse(addCmd, categories, brands, isAdmin = true)

        assertTrue(parsed is BasheerVoiceIntent.AddProduct)
        val data = (parsed as BasheerVoiceIntent.AddProduct).data
        assertEquals("ثلاجات", data.categoryName)
        assertEquals("شارب", data.brandName)
        assertEquals(32500.0, data.finalPrice, 0.01)
        assertEquals(31000.0, data.minAllowedPrice, 0.01)
        assertEquals(4, data.stockQuantity)
    }

    // --- 6. Role Permissions Enforcement (Admin vs Seller) ---

    @Test
    fun testSellerCannotExecuteAdminActions() {
        // Seller cannot add product
        val addBySeller = BasheerVoiceParser.parse(
            "ضيف غسالة جديدة قسم الغسالات شركة إل جي سعرها 25000",
            categories,
            brands,
            isAdmin = false
        )
        assertTrue(addBySeller is BasheerVoiceIntent.PermissionDenied)
        assertTrue((addBySeller as BasheerVoiceIntent.PermissionDenied).spokenFeedback.contains("المدير"))

        // Seller cannot delete model
        val deleteBySeller = BasheerVoiceParser.parse(
            "احذف موديل AU7000",
            categories,
            brands,
            isAdmin = false
        )
        assertTrue(deleteBySeller is BasheerVoiceIntent.PermissionDenied)

        // Seller cannot update prices
        val updatePriceBySeller = BasheerVoiceParser.parse(
            "عدل سعر ثلاجة شارب لـ 33 ألف",
            categories,
            brands,
            isAdmin = false
        )
        assertTrue(updatePriceBySeller is BasheerVoiceIntent.PermissionDenied)

        // Seller cannot record expenses
        val expenseBySeller = BasheerVoiceParser.parse(
            "سجل مصروف كهربا 500 جنيه",
            categories,
            brands,
            isAdmin = false
        )
        assertTrue(expenseBySeller is BasheerVoiceIntent.PermissionDenied)

        // Seller cannot pay suppliers
        val paySupBySeller = BasheerVoiceParser.parse(
            "ادفع للمورد العربي 10000 جنيه",
            categories,
            brands,
            isAdmin = false
        )
        assertTrue(paySupBySeller is BasheerVoiceIntent.PermissionDenied)
    }

    // --- 7. Admin Sensitive Operations Parse as Confirmation Candidates ---

    @Test
    fun testAdminOperationsParsedAccurately() {
        // Delete model by admin
        val deleteByAdmin = BasheerVoiceParser.parse(
            "احذف موديل AU7000",
            categories,
            brands,
            isAdmin = true
        )
        assertTrue(deleteByAdmin is BasheerVoiceIntent.DeleteModel)
        assertEquals("AU7000", (deleteByAdmin as BasheerVoiceIntent.DeleteModel).modelQuery.uppercase())

        // Update price by admin
        val updatePriceByAdmin = BasheerVoiceParser.parse(
            "عدل سعر ثلاجة شارب لـ 34000",
            categories,
            brands,
            isAdmin = true
        )
        assertTrue(updatePriceByAdmin is BasheerVoiceIntent.UpdatePrice)
        assertEquals(34000.0, (updatePriceByAdmin as BasheerVoiceIntent.UpdatePrice).newPrice, 0.01)

        // Record expense by admin
        val expenseByAdmin = BasheerVoiceParser.parse(
            "سجل مصروف كهربا 750 جنيه",
            categories,
            brands,
            isAdmin = true
        )
        assertTrue(expenseByAdmin is BasheerVoiceIntent.RecordExpense)
        assertEquals(750.0, (expenseByAdmin as BasheerVoiceIntent.RecordExpense).amount, 0.01)
        assertEquals("كهرباء ومرافق", (expenseByAdmin as BasheerVoiceIntent.RecordExpense).categoryName)

        // Supplier payment by admin
        val paySupByAdmin = BasheerVoiceParser.parse(
            "ادفع للمورد العربي 15000 جنيه",
            categories,
            brands,
            isAdmin = true
        )
        assertTrue(paySupByAdmin is BasheerVoiceIntent.SupplierPayment)
        assertEquals(15000.0, (paySupByAdmin as BasheerVoiceIntent.SupplierPayment).amount, 0.01)
    }

    // --- 8. Treasury Cash Balance Inquiry ---

    @Test
    fun testTreasuryCashInquiry() {
        val t1 = BasheerVoiceParser.parse("كام رصيد الخزنة دلوقتي؟", categories, brands)
        assertTrue(t1 is BasheerVoiceIntent.CheckTreasuryBalance)

        val t2 = BasheerVoiceParser.parse("الخزنة فيها كام فلوس؟", categories, brands)
        assertTrue(t2 is BasheerVoiceIntent.CheckTreasuryBalance)

        val t3 = BasheerVoiceParser.parse("معانا كام كاش في الدرج؟", categories, brands)
        assertTrue(t3 is BasheerVoiceIntent.CheckTreasuryBalance)
    }

    // --- 9. Handling Unrecognized Commands (Polite Egyptian Clarification) ---

    @Test
    fun testUnrecognizedCommandRequestsClarificationWithoutBogusData() {
        val gibberish = BasheerVoiceParser.parse("صباح الخير يا سيدي الفاضل هل الجو جميل اليوم؟", categories, brands)
        assertTrue(gibberish is BasheerVoiceIntent.NeedClarification)
        assertTrue((gibberish as BasheerVoiceIntent.NeedClarification).spokenFeedback.contains("مافهمتش قصدك"))
    }
}
