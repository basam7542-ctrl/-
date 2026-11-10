package com.example.voice

import com.example.data.local.BrandEntity
import com.example.data.local.CategoryEntity
import com.example.ui.MainTab

data class ParsedProductVoiceData(
    val categoryName: String,
    val brandName: String,
    val productName: String,
    val modelName: String,
    val finalPrice: Double,
    val minAllowedPrice: Double,
    val stockQuantity: Int,
    val originalSpokenText: String
)

sealed class BasheerVoiceIntent {
    // 1. Navigation between app sections
    data class Navigate(
        val targetTab: MainTab,
        val screenNameArabic: String,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 2. Search Price & Stock
    data class SearchPrice(
        val query: String,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 3. Start a Sale / Invoice
    data class StartSale(
        val query: String,
        val customerName: String = "",
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 4. Add Product (Admin Only, pre-fills review card)
    data class AddProduct(
        val data: ParsedProductVoiceData,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 5. Update Product Price (Admin Only, requires confirmation)
    data class UpdatePrice(
        val modelQuery: String,
        val newPrice: Double,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 6. Delete Model (Admin Only, requires confirmation)
    data class DeleteModel(
        val modelQuery: String,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 7. Check Treasury Cash Balance
    data class CheckTreasuryBalance(
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 8. Record Expense (Admin Only, requires confirmation)
    data class RecordExpense(
        val categoryName: String,
        val amount: Double,
        val description: String,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 9. Customer / Merchant Collection
    data class CustomerCollection(
        val customerName: String,
        val amount: Double,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 10. Supplier Payment (Admin Only, requires confirmation)
    data class SupplierPayment(
        val supplierName: String,
        val amount: Double,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 11. Returns Workflow
    data class InitiateReturn(
        val returnTypeArabic: String,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 12. Damage, Scrap & Repair Workflow
    data class InitiateDamageRepair(
        val modeArabic: String,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 13. Inventory Audit Workflow
    data class InitiateAudit(
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 14. Stock Movements Log
    data class StockMovements(
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 15. Help / Capabilities
    data class Help(
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 16. Permission Denied (when seller requests admin action)
    data class PermissionDenied(
        val attemptedAction: String,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()

    // 17. Unknown / Need Clarification (Polite Egyptian guidance, NO dummy data creation)
    data class NeedClarification(
        val rawText: String,
        val spokenFeedback: String
    ) : BasheerVoiceIntent()
}

object BasheerVoiceParser {

    fun parse(
        spokenText: String,
        categories: List<CategoryEntity>,
        brands: List<BrandEntity>,
        isAdmin: Boolean = true
    ): BasheerVoiceIntent {
        val raw = spokenText.trim()
        if (raw.isBlank()) {
            return BasheerVoiceIntent.NeedClarification(
                rawText = "",
                spokenFeedback = "أنا سامعك يا باشا، اتفضل اتكلم وأنا تحت أمرك."
            )
        }

        val norm = normalizeArabic(raw)

        // 1. HELP & CAPABILITIES
        if (norm.contains("بتعرف تعمل ايه") ||
            norm.contains("انت مين") ||
            norm.contains("مين بشير") ||
            norm.contains("قائمه الاوامر") ||
            norm.contains("مساعده") ||
            norm.contains("تعليمات") ||
            norm.contains("اوامر صوتيه")
        ) {
            return BasheerVoiceIntent.Help(
                spokenFeedback = "أنا بشير، مساعدك في معرض البشيري للأجهزة الكهربائية. أقدر أساعدك في الاستفسار عن الأسعار، تسجيل بيع وفواتير، إضافة وتعديل بضاعة، متابعة الخزنة والمصروفات، والتنقل بين أقسام المحل."
            )
        }

        // 2. CHECK TREASURY BALANCE
        val isTreasuryQuery = (
            norm.contains("خزن") || norm.contains("خزين") || norm.contains("كاش") || norm.contains("الدرج")
        ) && (
            norm.contains("رصيد") || norm.contains("كام") || norm.contains("فلوس") || norm.contains("معانا") || norm.contains("فيها")
        )
        if (isTreasuryQuery) {
            return BasheerVoiceIntent.CheckTreasuryBalance(
                spokenFeedback = "حاضر يا فندم، هجيبلك رصيد الخزنة الحالي دلوقتي."
            )
        }

        // 3. RECORD EXPENSES (ADMIN ONLY)
        val isExpenseIntent = norm.contains("سجل مصروف") ||
                norm.contains("حط مصروف") ||
                norm.contains("ادفع مصروف") ||
                norm.contains("اصرف مصروف") ||
                norm.contains("مصروف كهرب") ||
                norm.contains("مصروف ايجار") ||
                norm.contains("مصروف بوفيه") ||
                norm.contains("مصروف نضاف") ||
                norm.contains("مصروف صيان") ||
                norm.contains("مصروف نقل") ||
                norm.contains("سجل مصاريف")
        if (isExpenseIntent) {
            if (!isAdmin) {
                return BasheerVoiceIntent.PermissionDenied(
                    attemptedAction = "تسجيل المصروفات",
                    spokenFeedback = "عفواً يا باشا، تسجيل وصرف المصروفات من الخزنة خاص بصلاحيات المدير فقط."
                )
            }

            val amount = findFirstNumber(norm)
            val effectiveAmount = if (amount > 0) amount else 200.0

            val category = when {
                norm.contains("كهرب") -> "كهرباء ومرافق"
                norm.contains("ايجار") -> "إيجار المعرض"
                norm.contains("بوفيه") || norm.contains("شاي") -> "بوفيه وضيافة"
                norm.contains("نضاف") -> "نظافة وأدوات"
                norm.contains("صيان") -> "صيانة ومعدات"
                norm.contains("نقل") || norm.contains("شحن") -> "نقل ومشالات"
                norm.contains("رواتب") || norm.contains("مرتب") -> "رواتب وعمالة"
                else -> "مصروفات تشغيلية"
            }

            return BasheerVoiceIntent.RecordExpense(
                categoryName = category,
                amount = effectiveAmount,
                description = "مصروف $category مسجل صوتياً عبر بشير",
                spokenFeedback = "يا مدير، للتأكيد: هل توافق على صرف $effectiveAmount جنيه لبند $category من الخزنة؟"
            )
        }

        // 4. SUPPLIER PAYMENT (ADMIN ONLY)
        val isSupplierPaymentIntent = norm.contains("سداد مورد") ||
                norm.contains("ادفع للمورد") ||
                norm.contains("سدد للمورد") ||
                norm.contains("دفعه للمورد") ||
                norm.contains("فلوس المورد")
        if (isSupplierPaymentIntent) {
            if (!isAdmin) {
                return BasheerVoiceIntent.PermissionDenied(
                    attemptedAction = "سداد الموردين",
                    spokenFeedback = "عفواً يا باشا، سداد الموردين والصرف من الخزنة خاص بالمدير فقط."
                )
            }

            val amount = findFirstNumber(norm)
            val effectiveAmount = if (amount > 0) amount else 5000.0
            val supplierName = extractPartyName(norm, listOf("للمورد", "مورد", "لشركه", "شركه"))

            return BasheerVoiceIntent.SupplierPayment(
                supplierName = if (supplierName.isNotBlank()) supplierName else "المورد الرئيسي",
                amount = effectiveAmount,
                spokenFeedback = "يا مدير، للتأكيد: موافق على سداد مبلغ $effectiveAmount جنيه للمورد وخصمه من الخزنة؟"
            )
        }

        // 5. CUSTOMER / MERCHANT COLLECTION
        val isCollectionIntent = norm.contains("حصل من") ||
                norm.contains("تحصيل من") ||
                norm.contains("استلم دفعه من") ||
                norm.contains("قبض من") ||
                norm.contains("دفعه من العميل") ||
                norm.contains("دفعه من التاجر")
        if (isCollectionIntent) {
            val amount = findFirstNumber(norm)
            val effectiveAmount = if (amount > 0) amount else 1000.0
            val customerName = extractPartyName(norm, listOf("من العميل", "من التاجر", "من الزبون", "من"))

            return BasheerVoiceIntent.CustomerCollection(
                customerName = if (customerName.isNotBlank()) customerName else "عميل نقدي",
                amount = effectiveAmount,
                spokenFeedback = "يا باشا، للتأكيد: موافق على تسجيل تحصيل $effectiveAmount جنيه وتوريدها للخزنة؟"
            )
        }

        // 6. DELETE PRODUCT / MODEL (ADMIN ONLY)
        val isDeleteIntent = norm.contains("احذف موديل") ||
                norm.contains("امسح موديل") ||
                norm.contains("احذف الصنف") ||
                norm.contains("امسح الصنف") ||
                norm.contains("شيل الموديل") ||
                norm.contains("احذف جهاز") ||
                norm.contains("امسح جهاز")
        if (isDeleteIntent) {
            if (!isAdmin) {
                return BasheerVoiceIntent.PermissionDenied(
                    attemptedAction = "حذف المنتجات",
                    spokenFeedback = "عفواً يا باشا، حذف المنتجات والموديلات من صلاحيات المدير فقط للحفاظ على الداتا."
                )
            }

            var query = norm
                .replace("احذف موديل", "")
                .replace("امسح موديل", "")
                .replace("احذف الصنف", "")
                .replace("امسح الصنف", "")
                .replace("شيل الموديل", "")
                .replace("احذف جهاز", "")
                .replace("امسح جهاز", "")
                .trim()

            return BasheerVoiceIntent.DeleteModel(
                modelQuery = query,
                spokenFeedback = "يا مدير، للتأكيد: هل أنت متأكد من حذف هذا الموديل ونقله لسلة المحذوفات؟"
            )
        }

        // 7. UPDATE PRODUCT PRICE (ADMIN ONLY)
        val isUpdatePriceIntent = norm.contains("عدل سعر") ||
                norm.contains("غير سعر") ||
                norm.contains("خلي سعر") ||
                norm.contains("تعديل سعر")
        if (isUpdatePriceIntent) {
            if (!isAdmin) {
                return BasheerVoiceIntent.PermissionDenied(
                    attemptedAction = "تعديل الأسعار",
                    spokenFeedback = "عفواً يا باشا، تعديل أسعار البيع والحدود الدنيا من صلاحيات المدير فقط."
                )
            }

            val newPrice = findFirstNumber(norm)
            var modelQuery = norm
                .replace("عدل سعر", "")
                .replace("غير سعر", "")
                .replace("خلي سعر", "")
                .replace("تعديل سعر", "")
                .replace("لـ", "")
                .replace("الي", "")
                .replace(Regex("""\d+"""), "")
                .trim()

            return BasheerVoiceIntent.UpdatePrice(
                modelQuery = modelQuery,
                newPrice = if (newPrice > 0) newPrice else 15000.0,
                spokenFeedback = "يا مدير، للتأكيد: هل تريد تعديل سعر هذا الموديل؟"
            )
        }

        // 8. NAVIGATION TO APPLICATION SCREENS
        val isNavIntent = norm.contains("افتح") ||
                norm.contains("روح") ||
                norm.contains("شاشه") ||
                norm.contains("وريني") ||
                norm.contains("هات") ||
                norm.contains("ادخل علي") ||
                norm.contains("حركه المخزون") ||
                norm.contains("سجل الحركات") ||
                norm.contains("قسم")

        if (isNavIntent) {
            when {
                norm.contains("الخزن") || norm.contains("الخزين") || norm.contains("الحسابات") || norm.contains("المصروفات") || norm.contains("التدفقات") -> {
                    return BasheerVoiceIntent.Navigate(
                        targetTab = MainTab.ACCOUNTS,
                        screenNameArabic = "الحسابات والخزينة النقدية",
                        spokenFeedback = "حاضر من عيني، فتحتلك شاشة الحسابات والخزينة دلوقتي."
                    )
                }
                norm.contains("المخزن") || norm.contains("البضاع") || norm.contains("المنتجات") || norm.contains("الكتالوج") || norm.contains("المخزون") && !norm.contains("حركه") -> {
                    return BasheerVoiceIntent.Navigate(
                        targetTab = MainTab.CATALOG,
                        screenNameArabic = "المخزن وكتالوج الأجهزة",
                        spokenFeedback = "حاضر يا باشا، فتحتلك المخزن وكتالوج الأجهزة."
                    )
                }
                norm.contains("البيع") || norm.contains("المبيعات") || norm.contains("نبيع") -> {
                    return BasheerVoiceIntent.Navigate(
                        targetTab = MainTab.SALES_LOG,
                        screenNameArabic = "شاشة المبيعات",
                        spokenFeedback = "يلا بينا، فتحتلك شاشة المبيعات عشان نسجل بيعة جديدة."
                    )
                }
                norm.contains("الفواتير") || norm.contains("سجل الفواتير") || norm.contains("فاتوره") -> {
                    return BasheerVoiceIntent.Navigate(
                        targetTab = MainTab.SALES_LOG,
                        screenNameArabic = "سجل الفواتير والمبيعات",
                        spokenFeedback = "تمام يا فندم، فتحتلك سجل الفواتير والمبيعات."
                    )
                }
                norm.contains("المرتجع") || norm.contains("المرتجعات") || norm.contains("ترجيع") -> {
                    return BasheerVoiceIntent.Navigate(
                        targetTab = MainTab.RETURNS,
                        screenNameArabic = "المرتجعات",
                        spokenFeedback = "حاضر يا باشا، فتحتلك شاشة المرتجعات."
                    )
                }
                norm.contains("الهالك") || norm.contains("التالف") || norm.contains("الصيان") || norm.contains("الاصلاح") || norm.contains("مكسور") || norm.contains("بايظ") -> {
                    return BasheerVoiceIntent.Navigate(
                        targetTab = MainTab.DAMAGE_REPAIR,
                        screenNameArabic = "الهالك والتالف والإصلاح",
                        spokenFeedback = "حاضر، فتحتلك شاشة الهالك والتالف والصيانة."
                    )
                }
                norm.contains("الجرد") || norm.contains("حركه المخزون") || norm.contains("سجل الحركات") -> {
                    return BasheerVoiceIntent.Navigate(
                        targetTab = MainTab.INVENTORY,
                        screenNameArabic = "الجرد وحركة المخزون",
                        spokenFeedback = "تمام يا فندم، فتحتلك شاشة الجرد وحركة المخزون."
                    )
                }
                norm.contains("الاعدادات") || norm.contains("الضبط") || norm.contains("الطابعه") || norm.contains("البروفايل") -> {
                    return BasheerVoiceIntent.Navigate(
                        targetTab = MainTab.SETTINGS,
                        screenNameArabic = "إعدادات النظام والطابعة",
                        spokenFeedback = "حاضر، فتحتلك شاشة الإعدادات."
                    )
                }
                norm.contains("المحذوفات") || norm.contains("سله المهملات") || norm.contains("المهملات") -> {
                    return BasheerVoiceIntent.Navigate(
                        targetTab = MainTab.RECYCLE_BIN,
                        screenNameArabic = "سلة المحذوفات",
                        spokenFeedback = "حاضر يا فندم، فتحتلك سلة المحذوفات."
                    )
                }
            }
        }

        // 9. START A SALE / INVOICE
        val isSaleIntent = norm.contains("عايز ابيع") ||
                norm.contains("عاوز ابيع") ||
                norm.contains("بيع جهاز") ||
                norm.contains("بيع شاش") ||
                norm.contains("بيع ثلاج") ||
                norm.contains("بيع غسال") ||
                norm.contains("بيع بوتاجاز") ||
                norm.contains("بيع تكييف") ||
                norm.contains("بيع سخان") ||
                norm.contains("طلع فاتوره") ||
                norm.contains("اعمل فاتوره") ||
                norm.contains("اعمل بيعه")
        if (isSaleIntent) {
            var query = norm
                .replace("عايز ابيع", "")
                .replace("عاوز ابيع", "")
                .replace("بيع جهاز", "")
                .replace("طلع فاتوره لـ", "")
                .replace("طلع فاتوره ل", "")
                .replace("طلع فاتوره", "")
                .replace("اعمل فاتوره لـ", "")
                .replace("اعمل فاتوره ل", "")
                .replace("اعمل فاتوره", "")
                .replace("اعمل بيعه", "")
                .replace("بيع", "")
                .trim()

            return BasheerVoiceIntent.StartSale(
                query = query,
                spokenFeedback = "ألف مبروك البيعة مقدماً يا باشا، هفتحلك نافذة تجهيز الفاتورة حالا."
            )
        }

        // 10. RETURNS WORKFLOW
        val isReturnIntent = norm.contains("اعمل مرتجع") ||
                norm.contains("سجل مرتجع") ||
                norm.contains("عايز ارجع") ||
                norm.contains("عاوز ارجع") ||
                norm.contains("ترجيع بضاع") ||
                norm.contains("مرتجع عميل") ||
                norm.contains("مرتجع مورد")
        if (isReturnIntent) {
            val returnType = when {
                norm.contains("مورد") -> "مرتجع إلى مورد"
                norm.contains("تاجر") -> "مرتجع تاجر"
                else -> "مرتجع من عميل"
            }
            return BasheerVoiceIntent.InitiateReturn(
                returnTypeArabic = returnType,
                spokenFeedback = "حاضر يا باشا، فتحتلك شاشة المرتجعات لتسجيل $returnType."
            )
        }

        // 11. DAMAGE & REPAIR WORKFLOW
        val isDamageIntent = norm.contains("سجل هالك") ||
                norm.contains("سجل تالف") ||
                norm.contains("جهاز مكسور") ||
                norm.contains("ابعت للصلح") ||
                norm.contains("ابعت للورشه") ||
                norm.contains("ابعت للصيانه") ||
                norm.contains("جهاز بايظ")
        if (isDamageIntent) {
            val mode = if (norm.contains("صيان") || norm.contains("صلح") || norm.contains("ورشه")) "إصلاح وصيانة" else "هالك وتالف"
            return BasheerVoiceIntent.InitiateDamageRepair(
                modeArabic = mode,
                spokenFeedback = "حاضر يا فندم، فتحتلك شاشة قسم $mode لتسجيل العملية."
            )
        }

        // 12. INVENTORY AUDIT
        val isAuditIntent = norm.contains("عايز اعمل جرد") ||
                norm.contains("عاوز اعمل جرد") ||
                norm.contains("ابدأ جرد") ||
                norm.contains("جرد المحل") ||
                norm.contains("جرد المخزن")
        if (isAuditIntent) {
            return BasheerVoiceIntent.InitiateAudit(
                spokenFeedback = "حاضر يا فندم، فتحتلك شاشة الجرد عشان تبدأ جرد المخزن وتقارن الفروق."
            )
        }

        // 13. SEARCH PRICE & STOCK INQUIRY
        val isPriceInquiry = norm.contains("سعر") ||
                norm.contains("بكام") ||
                norm.contains("كام سعر") ||
                norm.contains("ثمن") ||
                norm.contains("ابحث عن") ||
                norm.contains("فيه عندنا") ||
                norm.contains("في عندنا") ||
                norm.contains("متاح") ||
                norm.contains("فاضل كام من") ||
                norm.contains("استفسار عن")

        // 14. ADD PRODUCT (Explicit or Casual with Appliance + Price)
        val hasExplicitAdd = norm.contains("ضيف") ||
                norm.contains("اضف") ||
                norm.contains("إضافة") ||
                norm.contains("اضافة") ||
                norm.contains("سجل منتج") ||
                norm.contains("منتج جديد") ||
                norm.contains("بضاعه جديده") ||
                norm.contains("سجل جهاز جديد")

        val hasAppliance = norm.contains("تكييف") || norm.contains("ثلاج") || norm.contains("غسال") || norm.contains("شاش") ||
                norm.contains("بوتاجاز") || norm.contains("سخان") || norm.contains("مكيف") || norm.contains("فريزر") ||
                norm.contains("حصان") || norm.contains("قدم") || norm.contains("كيلو") || norm.contains("بوصه")

        val hasLargePrice = findAllNumbers(norm).isNotEmpty()

        val isAddIntent = hasExplicitAdd || (hasAppliance && hasLargePrice && !isPriceInquiry &&
                !norm.contains("بيع") && !norm.contains("احذف") && !norm.contains("امسح") && !norm.contains("عدل"))

        if (isPriceInquiry && !hasExplicitAdd) {
            val query = norm
                .replace("سعر", "")
                .replace("بكام", "")
                .replace("كام", "")
                .replace("هو", "")
                .replace("عايز اعرف", "")
                .replace("عاوز اعرف", "")
                .replace("فيه عندنا", "")
                .replace("في عندنا", "")
                .replace("ابحث عن", "")
                .replace("متاح", "")
                .replace("فاضل كام من", "")
                .trim()

            return BasheerVoiceIntent.SearchPrice(
                query = query,
                spokenFeedback = "حاضر يا باشا، بدورلك في المخزن على $query دلوقتي."
            )
        }

        if (isAddIntent) {
            if (!isAdmin) {
                return BasheerVoiceIntent.PermissionDenied(
                    attemptedAction = "إضافة منتجات جديدة",
                    spokenFeedback = "عفواً يا باشا، إضافة الأصناف والمنتجات الجديدة في المخزن خاص بصلاحيات المدير فقط."
                )
            }

            // 1. Detect Category
            val detectedCategory = categories.firstOrNull { cat ->
                norm.contains(cat.name) ||
                        (cat.name.contains("غسال") && (norm.contains("غسال") || norm.contains("غسالة"))) ||
                        (cat.name.contains("ثلاج") && (norm.contains("ثلاج") || norm.contains("ديب فريزر"))) ||
                        (cat.name.contains("تكييف") && (norm.contains("تكييف") || norm.contains("مكيف") || norm.contains("مروحة"))) ||
                        (cat.name.contains("شاش") && (norm.contains("شاش") || norm.contains("تلفزيون"))) ||
                        (cat.name.contains("بوتاجاز") && (norm.contains("بوتاجاز") || norm.contains("فرن"))) ||
                        (cat.name.contains("سخان") && (norm.contains("سخان") || norm.contains("مبرد"))) ||
                        (cat.name.contains("صغير") && (norm.contains("قلاي") || norm.contains("ميكروويف") || norm.contains("مكنس")))
            }?.name ?: categories.firstOrNull()?.name ?: "غسالات"

            // 2. Detect Brand
            val detectedBrand = brands.firstOrNull { b ->
                norm.contains(b.name) ||
                        (b.name.contains("شارب") && norm.contains("شارب")) ||
                        (b.name.contains("توشيبا") && norm.contains("توشيبا")) ||
                        (b.name.contains("إل جي") && (norm.contains("ال جي") || norm.contains("إل جي") || norm.contains("lg"))) ||
                        (b.name.contains("بيكو") && norm.contains("بيكو")) ||
                        (b.name.contains("فريش") && norm.contains("فريش")) ||
                        (b.name.contains("تورنيدو") && norm.contains("تورنيدو")) ||
                        (b.name.contains("زانوسي") && norm.contains("زانوسي")) ||
                        (b.name.contains("سامسونج") && norm.contains("سامسونج")) ||
                        (b.name.contains("كريازي") && norm.contains("كريازي"))
            }?.name ?: "شارب"

            // 3. Extract Prices
            val extractedPrices = extractPrices(norm)
            val finalPrice = extractedPrices.first
            val minPrice = if (extractedPrices.second > 0.0) extractedPrices.second else (finalPrice * 0.95)

            // 4. Extract Quantity
            val stockQty = extractQuantity(norm)

            // 5. Clean Names
            val cleanProductName = extractProductName(norm, detectedCategory, detectedBrand)
            val cleanModelName = extractModelName(norm, cleanProductName)

            return BasheerVoiceIntent.AddProduct(
                data = ParsedProductVoiceData(
                    categoryName = detectedCategory,
                    brandName = detectedBrand,
                    productName = cleanProductName,
                    modelName = cleanModelName,
                    finalPrice = finalPrice,
                    minAllowedPrice = minPrice,
                    stockQuantity = stockQty,
                    originalSpokenText = spokenText
                ),
                spokenFeedback = "جهزتلك بيانات $cleanProductName في نموذج المراجعة، راجعها يا مدير واضغط تأكيد الحفظ."
            )
        }

        // 15. If nothing matched clearly: POLITELY REQUEST CLARIFICATION (DO NOT INVENT DUMMY DATA)
        return BasheerVoiceIntent.NeedClarification(
            rawText = raw,
            spokenFeedback = "معلش يا فندم مافهمتش قصدك بالظبط. تحب تسأل عن سعر جهاز، ولا تسجل بيعة، ولا نفتح قسم معين في المعرض؟"
        )
    }

    private fun extractPartyName(text: String, prefixes: List<String>): String {
        for (p in prefixes) {
            val idx = text.indexOf(p)
            if (idx != -1) {
                val after = text.substring(idx + p.length).trim()
                val words = after.split(Regex("""\s+"""))
                val nameWords = mutableListOf<String>()
                for (w in words) {
                    if (w.any { it.isDigit() } || w == "بمبلغ" || w == "مبلغ" || w == "جنيه") break
                    nameWords.add(w)
                    if (nameWords.size >= 3) break
                }
                if (nameWords.isNotEmpty()) {
                    return nameWords.joinToString(" ")
                }
            }
        }
        return ""
    }

    private fun extractPrices(text: String): Pair<Double, Double> {
        var finalPrice = 0.0
        var minPrice = 0.0

        val minKeywords = listOf("الحد الادنى", "اقل سعر", "ادنى سعر", "مايقلش عن", "مش اقل من", "حد ادنى")
        var minPriceIndex = -1
        for (kw in minKeywords) {
            val idx = text.indexOf(kw)
            if (idx != -1) {
                minPriceIndex = idx
                break
            }
        }

        if (minPriceIndex != -1) {
            val beforeMin = text.substring(0, minPriceIndex)
            val afterMin = text.substring(minPriceIndex)

            val numsBefore = findAllNumbers(beforeMin)
            val numsAfter = findAllNumbers(afterMin)

            finalPrice = numsBefore.firstOrNull() ?: 0.0
            minPrice = numsAfter.firstOrNull() ?: 0.0
        } else {
            val numbers = findAllNumbers(text)
            if (numbers.isNotEmpty()) {
                finalPrice = numbers[0]
                if (numbers.size > 1 && numbers[1] < finalPrice) {
                    minPrice = numbers[1]
                }
            }
        }

        if (finalPrice <= 0.0) {
            finalPrice = 15000.0
        }
        if (minPrice <= 0.0 || minPrice > finalPrice) {
            minPrice = (finalPrice * 0.95)
        }

        return Pair(finalPrice, minPrice)
    }

    private fun extractQuantity(text: String): Int {
        val regex = Regex("""(?:كميه|كمية|الكميه|الكمية|عدد|قطعه|قطعة|قطع)\s*(?:هي|هو)?\s*(\d+)""")
        val match = regex.find(text)
        if (match != null) {
            return match.groupValues[1].toIntOrNull() ?: 1
        }
        return 1
    }

    private fun findFirstNumber(segment: String): Double {
        val numbers = findAllNumbers(segment)
        return numbers.firstOrNull() ?: 0.0
    }

    private fun findAllNumbers(segment: String): List<Double> {
        val results = mutableListOf<Double>()

        // 1. Patterns like "25 ألف"
        val thousandRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:الف|آلاف|الاف)""")
        val thousandMatches = thousandRegex.findAll(segment)
        for (m in thousandMatches) {
            val base = m.groupValues[1].toDoubleOrNull() ?: 0.0
            if (base > 0) results.add(base * 1000.0)
        }

        // 2. Plain digits (appliance prices in EGP are >= 500)
        val digitRegex = Regex("""(?<!\S)(\d{3,7})(?!\S)""")
        val digitMatches = digitRegex.findAll(segment)
        for (m in digitMatches) {
            val num = m.groupValues[1].toDoubleOrNull() ?: 0.0
            if (num >= 500 && !results.contains(num)) {
                results.add(num)
            }
        }

        return results
    }

    private fun extractProductName(text: String, category: String, brand: String): String {
        var cleaned = text
            .replace("ضيف", "")
            .replace("اضف", "")
            .replace("إضافة", "")
            .replace("منتج جديد", "")
            .replace("في قسم", "")
            .replace("قسم", "")
            .replace("شركة", "")
            .replace("ماركة", "")
            .trim()

        val priceIdx = cleaned.indexOf("سعر")
        if (priceIdx != -1) {
            cleaned = cleaned.substring(0, priceIdx).trim()
        }

        if (cleaned.length < 5) {
            cleaned = "$category $brand"
        }

        return cleaned
    }

    private fun extractModelName(text: String, productName: String): String {
        val specRegex = Regex("""(\d+(?:\.\d+)?\s*(?:كيلو|قدم|بوصة|حصان|لتر|سم|شعلة))""")
        val match = specRegex.find(text)

        val spec = match?.value ?: ""
        return if (spec.isNotBlank()) {
            "$spec - ديجيتال نوفروست"
        } else {
            "موديل قياسي ديجيتال"
        }
    }

    fun normalizeArabic(input: String): String {
        return input
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ة', 'ه')
            .replace('ى', 'ي')
            .replace('٠', '0')
            .replace('١', '1')
            .replace('٢', '2')
            .replace('٣', '3')
            .replace('٤', '4')
            .replace('٥', '5')
            .replace('٦', '6')
            .replace('٧', '7')
            .replace('٨', '8')
            .replace('٩', '9')
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
