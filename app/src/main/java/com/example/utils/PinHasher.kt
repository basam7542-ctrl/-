package com.example.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory

object PinHasher {
    private const val VERSION = 1
    private const val SALT_BYTES = 16
    private const val KEY_LENGTH = 256
    private const val ITERATIONS = 10000

    fun hash(pin: String, salt: ByteArray = generateSalt()): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return "v$VERSION:${bytesToHex(salt)}:${bytesToHex(hash)}"
    }

    fun verify(pin: String, stored: String): Boolean {
        if (stored.isEmpty() || !stored.contains(":")) return false
        val parts = stored.split(":")
        // Legacy format (no version): salt:hash
        // New format: v1:salt:hash
        return if (parts.size == 2 && !parts[0].startsWith("v")) {
            verifyLegacy(pin, stored)
        } else if (parts.size == 3 && parts[0] == "v$VERSION") {
            verifyVersioned(pin, parts[1], parts[2])
        } else {
            false
        }
    }

    private fun verifyLegacy(pin: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        return try {
            val salt = hexToBytes(parts[0])
            // Use legacy hash method matching original PinHasher.hash
            val digest = MessageDigest.getInstance("SHA-256")
            var result = salt + pin.toByteArray(StandardCharsets.UTF_8)
            repeat(ITERATIONS) {
                result = digest.digest(result)
                digest.reset()
            }
            bytesToHex(result) == parts[1]
        } catch (e: Exception) {
            false
        }
    }

    private fun verifyVersioned(pin: String, saltHex: String, hashHex: String): Boolean {
        return try {
            val salt = hexToBytes(saltHex)
            val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            bytesToHex(hash) == hashHex
        } catch (e: Exception) {
            false
        }
    }

    fun needsMigration(storedHash: String): Boolean =
        storedHash.isEmpty() || !storedHash.contains(":")

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun bytesToHex(b: ByteArray): String = b.joinToString("") { "%02x".format(it) }
    private fun hexToBytes(hex: String): ByteArray =
        hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
