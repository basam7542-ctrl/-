package com.example.voice

import com.example.data.local.BrandEntity
import com.example.data.local.CategoryEntity

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
    data class AddProduct(val data: ParsedProductVoiceData) : BasheerVoiceIntent()
    data class SearchPrice(val query: String) : BasheerVoiceIntent()
    data class Unknown(val rawText: String) : BasheerVoiceIntent()
}

object BasheerVoiceParser {

    fun parse(
        spokenText: String,
        categories: List<CategoryEntity>,
        brands: List<BrandEntity>
    ): BasheerVoiceIntent {
        val normalized = normalizeArabic(spokenText)

        // Check if intent is a price inquiry
        val isPriceInquiry = normalized.contains("سعر") ||
                normalized.contains("بكام") ||
                normalized.contains("كام سعر") ||
                normalized.contains("ثمن") ||
                normalized.contains("ابحث عن")

        val isAddIntent = normalized.contains("ضيف") ||
                normalized.contains("اضف") ||
                normalized.contains("إضافة") ||
                normalized.contains("تسجيل منتج") ||
                normalized.contains("منتج جديد") ||
                normalized.contains("سعرها") ||
                normalized.contains("سعره") ||
                normalized.contains("قسم") ||
                normalized.contains("شركة")

        if (isPriceInquiry && !isAddIntent) {
            val query = normalized
                .replace("سعر", "")
                .replace("بكام", "")
                .replace("كام", "")
                .replace("هو", "")
                .replace("عايز اعرف", "")
                .trim()
            return BasheerVoiceIntent.SearchPrice(query)
        }

        // Parse Add Product fields
        // 1. Detect Category
        val detectedCategory = categories.firstOrNull { cat ->
            normalized.contains(cat.name) ||
            (cat.name.contains("غسال") && (normalized.contains("غسال") || normalized.contains("غسالة"))) ||
            (cat.name.contains("ثلاج") && (normalized.contains("ثلاج") || normalized.contains("ديب فريزر"))) ||
            (cat.name.contains("تكييف") && (normalized.contains("تكييف") || normalized.contains("مكيف") || normalized.contains("مروحة"))) ||
            (cat.name.contains("شاش") && (normalized.contains("شاش") || normalized.contains("تلفزيون"))) ||
            (cat.name.contains("بوتاجاز") && (normalized.contains("بوتاجاز") || normalized.contains("فرن"))) ||
            (cat.name.contains("سخان") && (normalized.contains("سخان") || normalized.contains("مبرد"))) ||
            (cat.name.contains("صغير") && (normalized.contains("قلاي") || normalized.contains("ميكروويف") || normalized.contains("مكنس")))
        }?.name ?: categories.firstOrNull()?.name ?: "غسالات"

        // 2. Detect Brand
        val detectedBrand = brands.firstOrNull { b ->
            normalized.contains(b.name) ||
            (b.name.contains("شارب") && normalized.contains("شارب")) ||
            (b.name.contains("توشيبا") && normalized.contains("توشيبا")) ||
            (b.name.contains("إل جي") && (normalized.contains("ال جي") || normalized.contains("إل جي") || normalized.contains("lg"))) ||
            (b.name.contains("بيكو") && normalized.contains("بيكو")) ||
            (b.name.contains("فريش") && normalized.contains("فريش")) ||
            (b.name.contains("تورنيدو") && normalized.contains("تورنيدو")) ||
            (b.name.contains("زانوسي") && normalized.contains("زانوسي")) ||
            (b.name.contains("سامسونج") && normalized.contains("سامسونج")) ||
            (b.name.contains("كريازي") && normalized.contains("كريازي"))
        }?.name ?: "شارب"

        // 3. Extract Prices (Final Price & Minimum Price)
        val extractedPrices = extractPrices(normalized)
        val finalPrice = extractedPrices.first
        val minPrice = if (extractedPrices.second > 0.0) extractedPrices.second else (finalPrice * 0.95)

        // 4. Extract Quantity
        val stockQty = extractQuantity(normalized)

        // 5. Clean Product Name & Model Name
        val cleanProductName = extractProductName(normalized, detectedCategory, detectedBrand)
        val cleanModelName = extractModelName(normalized, cleanProductName)

        return BasheerVoiceIntent.AddProduct(
            ParsedProductVoiceData(
                categoryName = detectedCategory,
                brandName = detectedBrand,
                productName = cleanProductName,
                modelName = cleanModelName,
                finalPrice = finalPrice,
                minAllowedPrice = minPrice,
                stockQuantity = stockQty,
                originalSpokenText = spokenText
            )
        )
    }

    private fun extractPrices(text: String): Pair<Double, Double> {
        var finalPrice = 0.0
        var minPrice = 0.0

        // Look for keywords like "الحد الأدنى" or "اقل سعر"
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

            finalPrice = findFirstNumber(beforeMin)
            minPrice = findFirstNumber(afterMin)
        } else {
            // Check numbers in whole text
            val numbers = findAllNumbers(text)
            if (numbers.isNotEmpty()) {
                finalPrice = numbers[0]
                if (numbers.size > 1 && numbers[1] < finalPrice) {
                    minPrice = numbers[1]
                }
            }
        }

        if (finalPrice <= 0.0) {
            finalPrice = 15000.0 // reasonable default fallback
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

        // 1. Check patterns like "25 ألف" or "25 الف"
        val thousandRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:الف|آلاف|الاف)""")
        val thousandMatches = thousandRegex.findAll(segment)
        for (m in thousandMatches) {
            val base = m.groupValues[1].toDoubleOrNull() ?: 0.0
            if (base > 0) results.add(base * 1000.0)
        }

        // 2. Check plain digits (>= 100)
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

        // Remove pricing parts from name
        val priceIdx = cleaned.indexOf("سعر")
        if (priceIdx != -1) {
            cleaned = cleaned.substring(0, priceIdx).trim()
        }

        // If too short, construct an informative appliance name
        if (cleaned.length < 5) {
            cleaned = "$category $brand"
        }

        return cleaned
    }

    private fun extractModelName(text: String, productName: String): String {
        // Extract size / specs (e.g. 18 قدم, 10 كيلو, 55 بوصة, 1.5 حصان)
        val specRegex = Regex("""(\d+(?:\.\d+)?\s*(?:كيلو|قدم|بوصة|حصان|لتر|سم|شعلة))""")
        val match = specRegex.find(text)

        val spec = match?.value ?: ""
        return if (spec.isNotBlank()) {
            "$spec - موديل حديث ديجيتال"
        } else {
            "موديل قياسي ديجيتال"
        }
    }

    private fun normalizeArabic(input: String): String {
        return input
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ة', 'ه')
            .replace('ى', 'ي')
            // Convert Eastern Arabic numerals (٠-٩) to Western (0-9)
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
