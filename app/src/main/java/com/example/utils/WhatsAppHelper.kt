package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.local.CustomerAccountEntity
import com.example.data.local.CustomerTransactionEntity
import com.example.data.local.MerchantAccountEntity
import com.example.data.local.MerchantTransactionEntity
import com.example.data.local.SupplierAccountEntity
import com.example.data.local.SupplierTransactionEntity
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppHelper {

    fun normalizePhoneNumber(rawPhone: String): String {
        val digits = rawPhone.filter { it.isDigit() }
        return when {
            digits.startsWith("00") -> digits.substring(2)
            digits.startsWith("01") && digits.length == 11 -> "2$digits" // Egyptian mobile 01xxxxxxxxx -> 201xxxxxxxxx
            digits.length == 10 && digits.startsWith("1") -> "20$digits"
            else -> digits
        }
    }

    fun sendWhatsAppMessage(context: Context, rawPhone: String, message: String) {
        val cleanedPhone = normalizePhoneNumber(rawPhone)
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            Uri.encode(message)
        }

        try {
            val uri = if (cleanedPhone.isNotBlank() && cleanedPhone.length >= 8) {
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanedPhone&text=$encodedMessage")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=$encodedMessage")
            }

            val directIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (directIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(directIntent)
            } else {
                // Fallback to browser or any app that can handle wa.me / whatsapp links
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            // General share fallback
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "إرسال كشف الحساب").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (ex: Exception) {
                Toast.makeText(context, "تعذر فتح تطبيق واتساب أو المشاركة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    fun buildCustomerStatementMessage(
        customer: CustomerAccountEntity,
        transactions: List<CustomerTransactionEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("📋 *كشف حساب آجل بالخارج*\n")
        sb.append("🏬 *البشيري للأجهزة الكهربائية*\n")
        sb.append("━━━━━━━━━━━━━━━\n")
        sb.append("👤 *العميل:* ${customer.customerName}\n")
        if (customer.phone.isNotBlank()) {
            sb.append("📱 *الهاتف:* ${customer.phone}\n")
        }
        sb.append("📅 *التاريخ:* ${dateFormat.format(Date())}\n\n")

        sb.append("💰 *الملخص المالي:*\n")
        sb.append("• إجمالي المبيعات بالأجل: *${formatAmount(customer.totalCreditSales)} ج.م*\n")
        sb.append("• إجمالي المبلغ المحصل: *${formatAmount(customer.totalCollected)} ج.م*\n")
        val remaining = customer.remainingDueToMe
        if (remaining > 0) {
            sb.append("⚠️ *المتبقي المستحق للمحل:* *${formatAmount(remaining)} ج.م*\n")
        } else if (remaining < 0) {
            sb.append("ℹ️ *رصيد دائن للعميل:* *${formatAmount(-remaining)} ج.م*\n")
        } else {
            sb.append("✅ *الحساب خالص تماماً (0 ج.م)*\n")
        }

        if (transactions.isNotEmpty()) {
            sb.append("\n━━━━━━━━━━━━━━━\n")
            sb.append("📑 *تفاصيل آخر العمليات:*\n")
            transactions.take(10).forEachIndexed { idx, tx ->
                val dateStr = dateFormat.format(Date(tx.timestamp))
                if (tx.type == "CREDIT_SALE") {
                    val desc = if (tx.productDescription.isNotBlank()) " (${tx.productDescription})" else ""
                    sb.append("${idx + 1}. 🔴 بيع آجل: *${formatAmount(tx.amount)} ج.م*$desc - $dateStr\n")
                } else {
                    sb.append("${idx + 1}. 🟢 دفعة مستلمة: *${formatAmount(tx.amount)} ج.م* - $dateStr\n")
                }
                if (tx.notes.isNotBlank()) {
                    sb.append("    _ملاحظة: ${tx.notes}_\n")
                }
            }
            if (transactions.size > 10) {
                sb.append("... وغيرها من العمليات السابقة\n")
            }
        }

        sb.append("\n━━━━━━━━━━━━━━━\n")
        sb.append("شكراً لتعاملكم مع *البشيري للأجهزة الكهربائية* 🌹\n")
        sb.append("للاستفسار: يرجى التواصل معنا.")
        return sb.toString()
    }

    fun buildMerchantStatementMessage(
        merchant: MerchantAccountEntity,
        transactions: List<MerchantTransactionEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("📋 *كشف حساب تاجر جملة بالأجل*\n")
        sb.append("🏬 *البشيري للأجهزة الكهربائية*\n")
        sb.append("━━━━━━━━━━━━━━━\n")
        sb.append("👤 *التاجر:* ${merchant.merchantName}\n")
        if (merchant.phone.isNotBlank()) {
            sb.append("📱 *الهاتف:* ${merchant.phone}\n")
        }
        sb.append("📅 *التاريخ:* ${dateFormat.format(Date())}\n\n")

        sb.append("💰 *الملخص المالي:*\n")
        sb.append("• إجمالي البضاعة المسلمة: *${formatAmount(merchant.totalGoodsDelivered)} ج.م*\n")
        sb.append("• إجمالي المحصل: *${formatAmount(merchant.totalCollected)} ج.م*\n")
        val remaining = merchant.remainingDueToMe
        if (remaining > 0) {
            sb.append("⚠️ *المتبقي المستحق لنا طرفكم:* *${formatAmount(remaining)} ج.م*\n")
        } else if (remaining < 0) {
            sb.append("ℹ️ *رصيد دائن للتاجر:* *${formatAmount(-remaining)} ج.م*\n")
        } else {
            sb.append("✅ *الحساب متزن تماماً (0 ج.م)*\n")
        }

        if (transactions.isNotEmpty()) {
            sb.append("\n━━━━━━━━━━━━━━━\n")
            sb.append("📑 *تفاصيل آخر الحركات:*\n")
            transactions.take(10).forEachIndexed { idx, tx ->
                val dateStr = dateFormat.format(Date(tx.timestamp))
                if (tx.type == "GOODS_DELIVERED") {
                    sb.append("${idx + 1}. 📦 تسليم بضاعة: *${formatAmount(tx.amount)} ج.م* - $dateStr\n")
                } else {
                    sb.append("${idx + 1}. 💵 دفعة محصلة: *${formatAmount(tx.amount)} ج.م* - $dateStr\n")
                }
                if (tx.notes.isNotBlank()) {
                    sb.append("    _ملاحظة: ${tx.notes}_\n")
                }
            }
            if (transactions.size > 10) {
                sb.append("... وغيرها من الحركات السابقة\n")
            }
        }

        sb.append("\n━━━━━━━━━━━━━━━\n")
        sb.append("مع تحيات إدارة *البشيري للأجهزة الكهربائية* 🤝")
        return sb.toString()
    }

    fun buildSupplierStatementMessage(
        supplier: SupplierAccountEntity,
        transactions: List<SupplierTransactionEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("📋 *كشف حساب شركة موردة*\n")
        sb.append("🏬 *البشيري للأجهزة الكهربائية*\n")
        sb.append("━━━━━━━━━━━━━━━\n")
        sb.append("🏢 *المورد:* ${supplier.supplierName}\n")
        if (supplier.phone.isNotBlank()) {
            sb.append("📱 *الهاتف:* ${supplier.phone}\n")
        }
        sb.append("📅 *التاريخ:* ${dateFormat.format(Date())}\n\n")

        sb.append("💰 *الملخص المالي:*\n")
        sb.append("• إجمالي المشتريات / الفواتير: *${formatAmount(supplier.totalPurchases)} ج.م*\n")
        sb.append("• إجمالي المدفوع للمورد: *${formatAmount(supplier.totalPaid)} ج.م*\n")
        val remaining = supplier.remainingBalance
        if (remaining > 0) {
            sb.append("⚠️ *المتبقي المستحق للمورد طرفنا:* *${formatAmount(remaining)} ج.م*\n")
        } else if (remaining < 0) {
            sb.append("ℹ️ *رصيد دائن لنا طرف المورد:* *${formatAmount(-remaining)} ج.م*\n")
        } else {
            sb.append("✅ *الحساب خالص تماماً (0 ج.م)*\n")
        }

        if (transactions.isNotEmpty()) {
            sb.append("\n━━━━━━━━━━━━━━━\n")
            sb.append("📑 *تفاصيل آخر العمليات:*\n")
            transactions.take(10).forEachIndexed { idx, tx ->
                val dateStr = dateFormat.format(Date(tx.timestamp))
                if (tx.type == "INVOICE") {
                    sb.append("${idx + 1}. 📄 فاتورة واردة: *${formatAmount(tx.amount)} ج.م* - $dateStr\n")
                } else {
                    sb.append("${idx + 1}. 💳 دفعة مسددة: *${formatAmount(tx.amount)} ج.م* - $dateStr\n")
                }
                if (tx.notes.isNotBlank()) {
                    sb.append("    _ملاحظة: ${tx.notes}_\n")
                }
            }
            if (transactions.size > 10) {
                sb.append("... وغيرها من العمليات السابقة\n")
            }
        }

        sb.append("\n━━━━━━━━━━━━━━━\n")
        sb.append("مع تحيات إدارة *البشيري للأجهزة الكهربائية* 🤝")
        return sb.toString()
    }

    private fun formatAmount(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", value)
        }
    }
}
