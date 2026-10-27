package com.example

import com.example.data.local.UserAccountEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsGreetingTest {

    private fun resolveGreeting(displayName: String, customGreeting: String? = null): String {
        return if (!customGreeting.isNullOrBlank()) {
            customGreeting.trim()
        } else {
            if (displayName.contains("المدير") || displayName.contains("مدير")) {
                "أهلاً بك يا مدير"
            } else {
                "أهلاً بك يا $displayName"
            }
        }
    }

    @Test
    fun testDefaultAdminGreeting() {
        val greeting = resolveGreeting("المدير", "")
        assertEquals("أهلاً بك يا مدير", greeting)
    }

    @Test
    fun testDefaultSellerGreeting() {
        val greeting = resolveGreeting("بائع 1", null)
        assertEquals("أهلاً بك يا بائع 1", greeting)
    }

    @Test
    fun testCustomAdminGreeting() {
        val custom = "  مرحباً بالحاج البشيري في متاجره الكريمة  "
        val greeting = resolveGreeting("المدير", custom)
        assertEquals("مرحباً بالحاج البشيري في متاجره الكريمة", greeting)
    }

    @Test
    fun testCustomSellerGreeting() {
        val custom = "صباح الخير يا أحمد، مبيعات موفقة اليوم إن شاء الله"
        val greeting = resolveGreeting("بائع 2", custom)
        assertEquals("صباح الخير يا أحمد، مبيعات موفقة اليوم إن شاء الله", greeting)
    }

    @Test
    fun testUserAccountEntityDefaultGreeting() {
        val user = UserAccountEntity(id = "seller3", displayName = "بائع 3", role = "SELLER", pin = "3333")
        assertTrue(user.customGreeting.isEmpty())
    }
}
