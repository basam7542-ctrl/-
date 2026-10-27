package com.example

import com.example.data.local.BrandEntity
import com.example.data.local.CategoryEntity
import com.example.voice.BasheerVoiceIntent
import com.example.voice.BasheerVoiceParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BasheerVoiceParserTest {

    private val sampleCategories = listOf(
        CategoryEntity(id = 1L, name = "غسالات", iconType = "APPLIANCE"),
        CategoryEntity(id = 2L, name = "ثلاجات", iconType = "APPLIANCE"),
        CategoryEntity(id = 3L, name = "شاشات", iconType = "APPLIANCE")
    )

    private val sampleBrands = listOf(
        BrandEntity(id = 1L, name = "شارب", categoryId = 2L),
        BrandEntity(id = 2L, name = "توشيبا", categoryId = 1L),
        BrandEntity(id = 3L, name = "إل جي", categoryId = 1L)
    )

    @Test
    fun testParseAddProductSharpRefrigerator() {
        val voiceInput = "ضيف ثلاجة شارب 18 قدم قسم الثلاجات شركة شارب السعر 32500 والحد الادنى 31000 والكمية 4"
        val intent = BasheerVoiceParser.parse(voiceInput, sampleCategories, sampleBrands)

        assertTrue(intent is BasheerVoiceIntent.AddProduct)
        val data = (intent as BasheerVoiceIntent.AddProduct).data
        assertEquals("ثلاجات", data.categoryName)
        assertEquals("شارب", data.brandName)
        assertEquals(32500.0, data.finalPrice, 0.1)
        assertEquals(31000.0, data.minAllowedPrice, 0.1)
        assertEquals(4, data.stockQuantity)
        assertEquals(voiceInput, data.originalSpokenText)
    }

    @Test
    fun testParsePriceInquiry() {
        val voiceInput = "سعر غسالة توشيبا 10 كيلو كام؟"
        val intent = BasheerVoiceParser.parse(voiceInput, sampleCategories, sampleBrands)

        assertTrue(intent is BasheerVoiceIntent.SearchPrice)
        val query = (intent as BasheerVoiceIntent.SearchPrice).query
        assertTrue(query.contains("غسالة") || query.contains("توشيبا"))
    }

    @Test
    fun testParseCasualInputPreservesOriginalText() {
        val voiceInput = "تكييف فريش بارد ساخن 2 وربع حصان 25000"
        val intent = BasheerVoiceParser.parse(voiceInput, sampleCategories, sampleBrands)

        assertTrue(intent is BasheerVoiceIntent.AddProduct)
        val data = (intent as BasheerVoiceIntent.AddProduct).data
        assertEquals(voiceInput, data.originalSpokenText)
        assertEquals(25000.0, data.finalPrice, 0.1)
    }
}
