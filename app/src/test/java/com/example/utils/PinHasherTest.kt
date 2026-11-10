package com.example.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {
    @Test
    fun hashAndVerify_roundTrip() {
        val hash = PinHasher.hash("1234")
        assertTrue(hash.startsWith("v1:"))
        assertTrue(PinHasher.verify("1234", hash))
        assertFalse(PinHasher.verify("9999", hash))
    }

    @Test
    fun differentSalts_produceDifferentHashes() {
        val a = PinHasher.hash("1234")
        val b = PinHasher.hash("1234")
        assertTrue(a != b)
        assertTrue(PinHasher.verify("1234", a))
        assertTrue(PinHasher.verify("1234", b))
    }

    @Test
    fun needsMigration_emptyOrLegacy() {
        assertTrue(PinHasher.needsMigration(""))
        assertTrue(PinHasher.needsMigration("plaintext"))
        assertFalse(PinHasher.needsMigration("v1:aa:bb"))
    }
}
