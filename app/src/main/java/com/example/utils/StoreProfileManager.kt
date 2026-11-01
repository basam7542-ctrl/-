package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Data model for showroom details, legal, and tax information.
 */
data class StoreProfile(
    val storeName: String = DEFAULT_STORE_NAME,
    val storeAddress: String = "",
    val storePhone: String = "",
    val commercialRegister: String = "",
    val taxCard: String = "",
    val vatNumber: String = "",
    val logoPath: String = ""
) {
    companion object {
        const val DEFAULT_STORE_NAME = "البشيري للأجهزة الكهربائية"
    }

    val hasLegalInfo: Boolean
        get() = commercialRegister.isNotBlank() || taxCard.isNotBlank() || vatNumber.isNotBlank()

    val formattedLegalInfo: String
        get() = listOfNotNull(
            if (commercialRegister.isNotBlank()) "سجل تجاري: ${commercialRegister.trim()}" else null,
            if (taxCard.isNotBlank()) "بطاقة ضريبية: ${taxCard.trim()}" else null,
            if (vatNumber.isNotBlank()) "تسجيل ضريبي: ${vatNumber.trim()}" else null
        ).joinToString(" • ")

    val hasContactInfo: Boolean
        get() = storeAddress.isNotBlank() || storePhone.isNotBlank()

    val formattedContactInfo: String
        get() = listOfNotNull(
            if (storeAddress.isNotBlank()) "العنوان: ${storeAddress.trim()}" else null,
            if (storePhone.isNotBlank()) "هاتف: ${storePhone.trim()}" else null
        ).joinToString(" • ")
}

/**
 * Manages persistent storage of the store profile and custom logo.
 */
object StoreProfileManager {

    private const val PREFS_NAME = "albashiri_store_profile_prefs"
    private const val KEY_STORE_NAME = "pref_store_name"
    private const val KEY_STORE_ADDRESS = "pref_store_address"
    private const val KEY_STORE_PHONE = "pref_store_phone"
    private const val KEY_COMMERCIAL_REGISTER = "pref_commercial_register"
    private const val KEY_TAX_CARD = "pref_tax_card"
    private const val KEY_VAT_NUMBER = "pref_vat_number"
    private const val KEY_LOGO_PATH = "pref_logo_path"

    fun getStoreProfile(context: Context): StoreProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_STORE_NAME, StoreProfile.DEFAULT_STORE_NAME)?.ifBlank {
            StoreProfile.DEFAULT_STORE_NAME
        } ?: StoreProfile.DEFAULT_STORE_NAME

        return StoreProfile(
            storeName = name,
            storeAddress = prefs.getString(KEY_STORE_ADDRESS, "") ?: "",
            storePhone = prefs.getString(KEY_STORE_PHONE, "") ?: "",
            commercialRegister = prefs.getString(KEY_COMMERCIAL_REGISTER, "") ?: "",
            taxCard = prefs.getString(KEY_TAX_CARD, "") ?: "",
            vatNumber = prefs.getString(KEY_VAT_NUMBER, "") ?: "",
            logoPath = prefs.getString(KEY_LOGO_PATH, "") ?: ""
        )
    }

    fun saveStoreProfile(context: Context, profile: StoreProfile) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val trimmedName = profile.storeName.trim().ifBlank { StoreProfile.DEFAULT_STORE_NAME }

        prefs.edit()
            .putString(KEY_STORE_NAME, trimmedName)
            .putString(KEY_STORE_ADDRESS, profile.storeAddress.trim())
            .putString(KEY_STORE_PHONE, profile.storePhone.trim())
            .putString(KEY_COMMERCIAL_REGISTER, profile.commercialRegister.trim())
            .putString(KEY_TAX_CARD, profile.taxCard.trim())
            .putString(KEY_VAT_NUMBER, profile.vatNumber.trim())
            .putString(KEY_LOGO_PATH, profile.logoPath.trim())
            .apply()

        // Sync with legacy BluetoothPrinterHelper store name
        BluetoothPrinterHelper.saveStoreName(context, trimmedName)
    }

    /**
     * Copies an image from the given Uri into the internal app files directory
     * so it persists independently of system permissions or media gallery shifts.
     */
    fun saveCustomLogoFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val logoFile = File(context.filesDir, "custom_store_logo_${System.currentTimeMillis()}.png")
            FileOutputStream(logoFile).use { output ->
                inputStream.copyTo(output)
            }
            logoFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clears any uploaded custom logo, resetting to the official default logo.
     */
    fun resetCustomLogo(context: Context) {
        val current = getStoreProfile(context)
        if (current.logoPath.isNotBlank()) {
            try {
                val file = File(current.logoPath)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
        }
        saveStoreProfile(context, current.copy(logoPath = ""))
    }

    /**
     * Decodes the custom logo bitmap if present and readable, or null otherwise.
     */
    fun loadLogoBitmap(logoPath: String): Bitmap? {
        if (logoPath.isBlank()) return null
        return try {
            val file = File(logoPath)
            if (file.exists() && file.canRead()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
