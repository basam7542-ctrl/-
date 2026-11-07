package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun seedDatabaseIfEmpty(db: AppDatabase) = withContext(Dispatchers.IO) {
        val userDao = db.userDao()
        val count = userDao.getUserCount()
        if (count > 0) return@withContext

        // 1. Initial 4 Users
        val users = listOf(
            UserAccountEntity(id = "admin", displayName = "المدير", role = "ADMIN", pin = "1234"),
            UserAccountEntity(id = "seller1", displayName = "بائع 1", role = "SELLER", pin = "1111"),
            UserAccountEntity(id = "seller2", displayName = "بائع 2", role = "SELLER", pin = "2222"),
            UserAccountEntity(id = "seller3", displayName = "بائع 3", role = "SELLER", pin = "3333")
        )
        userDao.insertUsers(users)

        // 2. Categories
        val catalogDao = db.catalogDao()
        val catWashingId = catalogDao.insertCategory(CategoryEntity(name = "غسالات", iconType = "WASHING"))
        val catFridgeId = catalogDao.insertCategory(CategoryEntity(name = "ثلاجات وديب فريزر", iconType = "FRIDGE"))
        val catAcId = catalogDao.insertCategory(CategoryEntity(name = "تكييفات ومراوح", iconType = "AC"))
        val catTvId = catalogDao.insertCategory(CategoryEntity(name = "شاشات وتلفزيونات", iconType = "TV"))
        val catCookerId = catalogDao.insertCategory(CategoryEntity(name = "بوتاجازات وأفران", iconType = "COOKER"))
        val catHeaterId = catalogDao.insertCategory(CategoryEntity(name = "سخانات ومبردات مياه", iconType = "HEATER"))
        val catSmallId = catalogDao.insertCategory(CategoryEntity(name = "أجهزة منزلية صغيرة", iconType = "SMALL_APPLIANCE"))

        // 3. Brands for each category
        // Washing brands
        val bToshibaWash = catalogDao.insertBrand(BrandEntity(name = "توشيبا", categoryId = catWashingId))
        val bLgWash = catalogDao.insertBrand(BrandEntity(name = "إل جي", categoryId = catWashingId))
        val bZanussiWash = catalogDao.insertBrand(BrandEntity(name = "زانوسي", categoryId = catWashingId))

        // Fridge brands
        val bSharpFridge = catalogDao.insertBrand(BrandEntity(name = "شارب", categoryId = catFridgeId))
        val bBekoFridge = catalogDao.insertBrand(BrandEntity(name = "بيكو", categoryId = catFridgeId))
        val bToshibaFridge = catalogDao.insertBrand(BrandEntity(name = "توشيبا", categoryId = catFridgeId))

        // AC brands
        val bSharpAc = catalogDao.insertBrand(BrandEntity(name = "شارب", categoryId = catAcId))
        val bFreshAc = catalogDao.insertBrand(BrandEntity(name = "فريش", categoryId = catAcId))

        // TV brands
        val bSamsungTv = catalogDao.insertBrand(BrandEntity(name = "سامسونج", categoryId = catTvId))
        val bLgTv = catalogDao.insertBrand(BrandEntity(name = "إل جي", categoryId = catTvId))
        val bTornadoTv = catalogDao.insertBrand(BrandEntity(name = "تورنيدو", categoryId = catTvId))

        // Cooker brands
        val bFreshCooker = catalogDao.insertBrand(BrandEntity(name = "فريش", categoryId = catCookerId))
        val bKiriaziCooker = catalogDao.insertBrand(BrandEntity(name = "كريازي", categoryId = catCookerId))

        // Small appliance brands
        val bTornadoSmall = catalogDao.insertBrand(BrandEntity(name = "تورنيدو", categoryId = catSmallId))
        val bSharpSmall = catalogDao.insertBrand(BrandEntity(name = "شارب", categoryId = catSmallId))

        // 4. Products & Models
        // Washing: Toshiba
        val p1 = catalogDao.insertProduct(ProductEntity(name = "غسالة توشيبا فوق أوتوماتيك", categoryId = catWashingId, categoryName = "غسالات", brandId = bToshibaWash, brandName = "توشيبا"))
        catalogDao.insertModel(ProductModelEntity(productId = p1, productName = "غسالة توشيبا فوق أوتوماتيك", brandName = "توشيبا", categoryName = "غسالات", modelName = "10 كيلو فضي ديجيتال طلمبة", finalPrice = 14500.0, minAllowedPrice = 13900.0, stockQuantity = 6, code = "AEW-E1050SUP"))
        catalogDao.insertModel(ProductModelEntity(productId = p1, productName = "غسالة توشيبا فوق أوتوماتيك", brandName = "توشيبا", categoryName = "غسالات", modelName = "12 كيلو سيلفر إنفرتر", finalPrice = 17800.0, minAllowedPrice = 16900.0, stockQuantity = 4, code = "AEW-DC1200SUP"))

        // Washing: LG
        val p2 = catalogDao.insertProduct(ProductEntity(name = "غسالة إل جي تحميل أمامي AI DD", categoryId = catWashingId, categoryName = "غسالات", brandId = bLgWash, brandName = "إل جي"))
        catalogDao.insertModel(ProductModelEntity(productId = p2, productName = "غسالة إل جي تحميل أمامي AI DD", brandName = "إل جي", categoryName = "غسالات", modelName = "8 كيلو محرك دفع مباشر بالبخار", finalPrice = 25900.0, minAllowedPrice = 24800.0, stockQuantity = 5, code = "F4J3TN3W"))
        catalogDao.insertModel(ProductModelEntity(productId = p2, productName = "غسالة إل جي تحميل أمامي AI DD", brandName = "إل جي", categoryName = "غسالات", modelName = "9 كيلو سمارت إنفرتر بالبخار مجفف", finalPrice = 32500.0, minAllowedPrice = 31200.0, stockQuantity = 3, code = "FH4G6VDGG6"))

        // Washing: Zanussi
        val p3 = catalogDao.insertProduct(ProductEntity(name = "غسالة زانوسي بيرفكت كير", categoryId = catWashingId, categoryName = "غسالات", brandId = bZanussiWash, brandName = "زانوسي"))
        catalogDao.insertModel(ProductModelEntity(productId = p3, productName = "غسالة زانوسي بيرفكت كير", brandName = "زانوسي", categoryName = "غسالات", modelName = "7 كيلو 1200 لفة أوتوماتيك فضي", finalPrice = 18200.0, minAllowedPrice = 17400.0, stockQuantity = 7, code = "ZWF71243SE"))

        // Fridge: Sharp
        val p4 = catalogDao.insertProduct(ProductEntity(name = "ثلاجة شارب نوفروست إنفرتر", categoryId = catFridgeId, categoryName = "ثلاجات وديب فريزر", brandId = bSharpFridge, brandName = "شارب"))
        catalogDao.insertModel(ProductModelEntity(productId = p4, productName = "ثلاجة شارب نوفروست إنفرتر", brandName = "شارب", categoryName = "ثلاجات وديب فريزر", modelName = "18 قدم بلازما كلاستر زجاج أسود", finalPrice = 32800.0, minAllowedPrice = 31500.0, stockQuantity = 4, code = "SJ-GV58G-BK"))
        catalogDao.insertModel(ProductModelEntity(productId = p4, productName = "ثلاجة شارب نوفروست إنفرتر", brandName = "شارب", categoryName = "ثلاجات وديب فريزر", modelName = "16 قدم بلازما كلاستر سيلفر", finalPrice = 27500.0, minAllowedPrice = 26400.0, stockQuantity = 5, code = "SJ-PC48A-SL"))

        // Fridge: Beko
        val p5 = catalogDao.insertProduct(ProductEntity(name = "ثلاجة بيكو هارفست فريش", categoryId = catFridgeId, categoryName = "ثلاجات وديب فريزر", brandId = bBekoFridge, brandName = "بيكو"))
        catalogDao.insertModel(ProductModelEntity(productId = p5, productName = "ثلاجة بيكو هارفست فريش", brandName = "بيكو", categoryName = "ثلاجات وديب فريزر", modelName = "450 لتر نوفروست إنفرتر أنثراسيت", finalPrice = 29500.0, minAllowedPrice = 28200.0, stockQuantity = 3, code = "RCNE560E50ZXPN"))

        // AC: Sharp
        val p6 = catalogDao.insertProduct(ProductEntity(name = "تكييف شارب سبليت بلازما كلاستر", categoryId = catAcId, categoryName = "تكييفات ومراوح", brandId = bSharpAc, brandName = "شارب"))
        catalogDao.insertModel(ProductModelEntity(productId = p6, productName = "تكييف شارب سبليت بلازما كلاستر", brandName = "شارب", categoryName = "تكييفات ومراوح", modelName = "1.5 حصان بارد فقط ديجيتال", finalPrice = 22900.0, minAllowedPrice = 21900.0, stockQuantity = 8, code = "AH-AP12YHE"))
        catalogDao.insertModel(ProductModelEntity(productId = p6, productName = "تكييف شارب سبليت بلازما كلاستر", brandName = "شارب", categoryName = "تكييفات ومراوح", modelName = "2.25 حصان بارد ساخن إنفرتر", finalPrice = 34500.0, minAllowedPrice = 33000.0, stockQuantity = 4, code = "AY-XP18WHE"))

        // TV: Samsung
        val p7 = catalogDao.insertProduct(ProductEntity(name = "شاشة سامسونج سمارت 4K", categoryId = catTvId, categoryName = "شاشات وتلفزيونات", brandId = bSamsungTv, brandName = "سامسونج"))
        catalogDao.insertModel(ProductModelEntity(productId = p7, productName = "شاشة سامسونج سمارت 4K", brandName = "سامسونج", categoryName = "شاشات وتلفزيونات", modelName = "55 بوصة كريستال UHD ريموت ذكي", finalPrice = 19500.0, minAllowedPrice = 18700.0, stockQuantity = 6, code = "UA55CU7000"))
        catalogDao.insertModel(ProductModelEntity(productId = p7, productName = "شاشة سامسونج سمارت 4K", brandName = "سامسونج", categoryName = "شاشات وتلفزيونات", modelName = "65 بوصة كيو ليد QLED سمارت", finalPrice = 33800.0, minAllowedPrice = 32200.0, stockQuantity = 3, code = "QA65Q60C"))

        // Cooker: Fresh
        val p8 = catalogDao.insertProduct(ProductEntity(name = "بوتاجاز فريش بروفيشنال هامر", categoryId = catCookerId, categoryName = "بوتاجازات وأفران", brandId = bFreshCooker, brandName = "فريش"))
        catalogDao.insertModel(ProductModelEntity(productId = p8, productName = "بوتاجاز فريش بروفيشنال هامر", brandName = "فريش", categoryName = "بوتاجازات وأفران", modelName = "90×60 سم 5 شعلة أمان كامل تاتش مروحتين", finalPrice = 16800.0, minAllowedPrice = 15900.0, stockQuantity = 5, code = "500015523"))

        // Small Appliances: Tornado & Sharp
        val p9 = catalogDao.insertProduct(ProductEntity(name = "قلاية هوائية تورنيدو ديجيتال", categoryId = catSmallId, categoryName = "أجهزة منزلية صغيرة", brandId = bTornadoSmall, brandName = "تورنيدو"))
        catalogDao.insertModel(ProductModelEntity(productId = p9, productName = "قلاية هوائية تورنيدو ديجيتال", brandName = "تورنيدو", categoryName = "أجهزة منزلية صغيرة", modelName = "4.2 لتر شاشة لمس 1550 وات", finalPrice = 4800.0, minAllowedPrice = 4400.0, stockQuantity = 12, code = "THF-1554D"))

        val p10 = catalogDao.insertProduct(ProductEntity(name = "ميكروويف شارب جريل", categoryId = catSmallId, categoryName = "أجهزة منزلية صغيرة", brandId = bSharpSmall, brandName = "شارب"))
        catalogDao.insertModel(ProductModelEntity(productId = p10, productName = "ميكروويف شارب جريل", brandName = "شارب", categoryName = "أجهزة منزلية صغيرة", modelName = "25 لتر 900 وات فضي ديجيتال", finalPrice = 7600.0, minAllowedPrice = 7100.0, stockQuantity = 7, code = "R-750MT(S)"))

        // 5. Initial Supplier Accounts & sample transactions
        val accountsDao = db.accountsDao()
        val s1Id = accountsDao.insertSupplier(SupplierAccountEntity(
            supplierName = "مجموعة العربي (توشيبا - تورنيدو - شارب)",
            phone = "19319",
            totalPurchases = 420000.0,
            totalPaid = 310000.0,
            remainingBalance = 110000.0,
            notes = "المورد الرئيسي للأجهزة اليابانية وتوشيبا"
        ))
        accountsDao.insertSupplierTransaction(SupplierTransactionEntity(supplierId = s1Id, type = "INVOICE", amount = 220000.0, notes = "فاتورة توريد شاشات وغسالات رقم 8412"))
        accountsDao.insertSupplierTransaction(SupplierTransactionEntity(supplierId = s1Id, type = "PAYMENT", amount = 150000.0, notes = "شيك بنكي مسدد رقم 7741"))
        accountsDao.insertSupplierTransaction(SupplierTransactionEntity(supplierId = s1Id, type = "INVOICE", amount = 200000.0, notes = "فاتورة توريد تكييفات شارب صيف 2026"))
        accountsDao.insertSupplierTransaction(SupplierTransactionEntity(supplierId = s1Id, type = "PAYMENT", amount = 160000.0, notes = "دفعة نقدية خزينة"))

        val s2Id = accountsDao.insertSupplier(SupplierAccountEntity(
            supplierName = "شركة إل جي مصر للإلكترونيات",
            phone = "19960",
            totalPurchases = 260000.0,
            totalPaid = 200000.0,
            remainingBalance = 60000.0,
            notes = "توريد غسالات وشاشات إل جي"
        ))
        accountsDao.insertSupplierTransaction(SupplierTransactionEntity(supplierId = s2Id, type = "INVOICE", amount = 260000.0, notes = "فاتورة استلام بضاعة مجمعة"))
        accountsDao.insertSupplierTransaction(SupplierTransactionEntity(supplierId = s2Id, type = "PAYMENT", amount = 200000.0, notes = "تحويل بنكي حساب الشركة"))

        val s3Id = accountsDao.insertSupplier(SupplierAccountEntity(
            supplierName = "شركة فريش للأجهزة المنزلية",
            phone = "19059",
            totalPurchases = 180000.0,
            totalPaid = 145000.0,
            remainingBalance = 35000.0,
            notes = "بوتاجازات وتكييفات فريش"
        ))
        accountsDao.insertSupplierTransaction(SupplierTransactionEntity(supplierId = s3Id, type = "INVOICE", amount = 180000.0, notes = "فاتورة توريد بوتاجازات هامر"))
        accountsDao.insertSupplierTransaction(SupplierTransactionEntity(supplierId = s3Id, type = "PAYMENT", amount = 145000.0, notes = "سداد نقدًا للمندوب"))

        // 6. Initial Merchant Accounts (تجار بالأجل) & sample transactions
        val m1Id = accountsDao.insertMerchant(MerchantAccountEntity(
            merchantName = "معرض النور للأجهزة (الحاج عاطف)",
            phone = "01002345678",
            totalGoodsDelivered = 95000.0,
            totalCollected = 65000.0,
            remainingDueToMe = 30000.0,
            notes = "سحب بضاعة بالأجل أسبوعي"
        ))
        accountsDao.insertMerchantTransaction(MerchantTransactionEntity(merchantId = m1Id, type = "GOODS_DELIVERED", amount = 95000.0, notes = "تسليم 3 ثلاجات و 2 غسالة شارب"))
        accountsDao.insertMerchantTransaction(MerchantTransactionEntity(merchantId = m1Id, type = "COLLECTION", amount = 65000.0, notes = "دفعة محصلة نقدًا من المعرض"))

        val m2Id = accountsDao.insertMerchant(MerchantAccountEntity(
            merchantName = "تاجر الأمل - إبراهيم حسني",
            phone = "01119876543",
            totalGoodsDelivered = 110000.0,
            totalCollected = 85000.0,
            remainingDueToMe = 25000.0,
            notes = "سحب أجهزة صغيرة وشاشات"
        ))
        accountsDao.insertMerchantTransaction(MerchantTransactionEntity(merchantId = m2Id, type = "GOODS_DELIVERED", amount = 110000.0, notes = "تسليم 5 شاشات و 6 قلايات"))
        accountsDao.insertMerchantTransaction(MerchantTransactionEntity(merchantId = m2Id, type = "COLLECTION", amount = 85000.0, notes = "تحصيل إيصال أمانة رقم 12"))

        // 7. Initial Accounts (آجل بالخارج)
        val c1Id = accountsDao.insertCustomer(CustomerAccountEntity(
            customerName = "أحمد محمود عبد العال",
            phone = "01091234567",
            totalCreditSales = 18500.0,
            totalCollected = 10000.0,
            remainingDueToMe = 8500.0,
            notes = "جهاز عروسة - قسط شهري"
        ))
        accountsDao.insertCustomerTransaction(CustomerTransactionEntity(
            customerId = c1Id,
            type = "CREDIT_SALE",
            amount = 18500.0,
            productDescription = "غسالة زانوسي 7 كيلو + قلاية تورنيدو",
            notes = "فاتورة بيع آجل رقم 301"
        ))
        accountsDao.insertCustomerTransaction(CustomerTransactionEntity(
            customerId = c1Id,
            type = "COLLECTION",
            amount = 10000.0,
            productDescription = "",
            notes = "دفعة أولى مقدمة نقدًا"
        ))

        val c2Id = accountsDao.insertCustomer(CustomerAccountEntity(
            customerName = "محمد سامي النجار",
            phone = "01287654321",
            totalCreditSales = 22900.0,
            totalCollected = 15000.0,
            remainingDueToMe = 7900.0,
            notes = "تكييف شارب 1.5 حصان"
        ))
        accountsDao.insertCustomerTransaction(CustomerTransactionEntity(
            customerId = c2Id,
            type = "CREDIT_SALE",
            amount = 22900.0,
            productDescription = "تكييف شارب 1.5 حصان بارد",
            notes = "شراء بالتقسيط"
        ))
        accountsDao.insertCustomerTransaction(CustomerTransactionEntity(
            customerId = c2Id,
            type = "COLLECTION",
            amount = 15000.0,
            productDescription = "",
            notes = "سداد قسط نقدًا"
        ))

        // 8. Initial Sample Sales Log for demo
        val salesDao = db.salesDao()
        salesDao.insertSale(SaleRecordEntity(
            modelId = 1,
            productName = "غسالة توشيبا فوق أوتوماتيك",
            modelName = "10 كيلو فضي ديجيتال طلمبة",
            categoryName = "غسالات",
            brandName = "توشيبا",
            soldPrice = 14200.0,
            minAllowedPrice = 13900.0,
            originalFinalPrice = 14500.0,
            quantity = 1,
            totalPrice = 14200.0,
            sellerId = "seller1",
            sellerName = "بائع 1",
            timestamp = System.currentTimeMillis() - 86400000L,
            notes = "عميل نقدي"
        ))
        salesDao.insertSale(SaleRecordEntity(
            modelId = 7,
            productName = "شاشة سامسونج سمارت 4K",
            modelName = "55 بوصة كريستال UHD ريموت ذكي",
            categoryName = "شاشات وتلفزيونات",
            brandName = "سامسونج",
            soldPrice = 19000.0,
            minAllowedPrice = 18700.0,
            originalFinalPrice = 19500.0,
            quantity = 1,
            totalPrice = 19000.0,
            sellerId = "seller2",
            sellerName = "بائع 2",
            timestamp = System.currentTimeMillis() - 43200000L,
            notes = "شامل التوصيل"
        ))
    }
}
