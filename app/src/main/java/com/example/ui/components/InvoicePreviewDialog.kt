package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.utils.StoreProfileManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceItemEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.SaleRecordEntity
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Professional Sales Invoice Preview & Thermal Print Dialog.
 *
 * Supports Multi-Item Invoices (InvoiceWithItems) and Legacy Sales (SaleRecordEntity).
 * - 100% Pure White Background (#FFFFFF)
 * - 100% Solid Black typography, borders, and accents (#000000) for zero ink waste on thermal/laser printers
 * - Cairo Arabic typography with native RTL layout
 * - Official Monochrome Store Logo at top
 * - Customer Details (Name & Phone with preserved leading zero)
 * - Itemized Table of ALL products in the invoice
 * - Subtotal, Discount, Net Total, Paid, and Remaining calculations
 * - Direct Bluetooth thermal printing trigger
 */
@Composable
fun InvoicePreviewDialog(
    invoiceWithItems: InvoiceWithItems,
    isPrinting: Boolean = false,
    onPrintReceipt: () -> Unit,
    onDismiss: () -> Unit
) {
    val invoice = invoiceWithItems.invoice
    val items = invoiceWithItems.items
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG")) }
    val formattedDate = remember(invoice.timestamp) { dateFormat.format(Date(invoice.timestamp)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 14.dp)
                .testTag("invoice_preview_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = NavySurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "فاتورة بيع رسمية #${invoice.invoiceNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontFamily = CairoFontFamily
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable White Paper Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Official Monochrome Store Logo (Custom if uploaded, else default)
                        val customLogoBitmap = remember(invoice.storeLogoUri) {
                            StoreProfileManager.loadLogoBitmap(invoice.storeLogoUri)
                        }

                        val displayStoreName = invoice.storeName.ifBlank { "البشيري للأجهزة الكهربائية" }

                        if (customLogoBitmap != null) {
                            Image(
                                bitmap = customLogoBitmap.asImageBitmap(),
                                contentDescription = "شعار $displayStoreName",
                                modifier = Modifier
                                    .height(85.dp)
                                    .fillMaxWidth()
                                    .testTag("invoice_official_logo"),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_albashiri_invoice_logo),
                                contentDescription = "شعار البشيري للأجهزة الكهربائية",
                                modifier = Modifier
                                    .height(85.dp)
                                    .fillMaxWidth()
                                    .testTag("invoice_official_logo"),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = displayStoreName,
                            fontFamily = CairoFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        // Contact line (Address and Phone) - only render if at least one is not blank
                        val contactLine = listOfNotNull(
                            if (invoice.storeAddress.isNotBlank()) "العنوان: ${invoice.storeAddress.trim()}" else null,
                            if (invoice.storePhone.isNotBlank()) "هاتف: ${invoice.storePhone.trim()}" else null
                        ).joinToString(" • ")

                        if (contactLine.isNotBlank()) {
                            Text(
                                text = contactLine,
                                fontFamily = CairoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Legal and Tax line (Commercial Register, Tax Card, VAT) - only render non-empty fields
                        val legalLine = listOfNotNull(
                            if (invoice.commercialRegister.isNotBlank()) "سجل تجاري: ${invoice.commercialRegister.trim()}" else null,
                            if (invoice.taxCard.isNotBlank()) "بطاقة ضريبية: ${invoice.taxCard.trim()}" else null,
                            if (invoice.vatNumber.isNotBlank()) "تسجيل ضريبي: ${invoice.vatNumber.trim()}" else null
                        ).joinToString(" • ")

                        if (legalLine.isNotBlank()) {
                            Text(
                                text = legalLine,
                                fontFamily = CairoFontFamily,
                                fontSize = 9.sp,
                                color = Color(0xFF333333),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 2.dp, color = Color.Black)
                        Spacer(modifier = Modifier.height(2.dp))
                        HorizontalDivider(thickness = 0.8.dp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Invoice Header Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "فاتورة بيع معتمدة",
                                fontFamily = CairoFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "رقم الفاتورة: #${invoice.invoiceNumber}",
                                fontFamily = CairoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "التاريخ: $formattedDate",
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFF222222)
                            )
                            Text(
                                text = "البائع / المحصل: ${invoice.sellerName}",
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF222222)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Customer Information Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "بيانات المشتري / العميل:",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "الاسم: " + if (invoice.customerName.isNotBlank()) invoice.customerName else "عميل نقدي",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = if (invoice.customerName.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                            color = Color.Black
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "الهاتف: " + if (invoice.customerPhone.isNotBlank()) invoice.customerPhone else "—",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = if (invoice.customerPhone.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Multi-Item Invoice Table
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Black)
                        ) {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEEEEEE))
                                    .border(0.5.dp, Color.Black)
                                    .padding(vertical = 5.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "م",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    text = "الصنف والمواصفات",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.weight(2.0f)
                                )
                                Text(
                                    text = "العدد",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.6f)
                                )
                                Text(
                                    text = "سعر الوحدة",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1.0f)
                                )
                                Text(
                                    text = "الإجمالي",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.1f)
                                )
                            }

                            // Table Content Rows for Each Item
                            items.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(0.4f)
                                    )
                                    Column(modifier = Modifier.weight(2.0f)) {
                                        Text(
                                            text = "${item.productName} - ${item.modelName}",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        val specs = listOf(item.brandName, item.categoryName).filter { it.isNotBlank() }.joinToString(" • ")
                                        if (specs.isNotBlank()) {
                                            Text(
                                                text = specs,
                                                fontFamily = CairoFontFamily,
                                                fontSize = 9.sp,
                                                color = Color(0xFF444444)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${item.quantity}",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(0.6f)
                                    )
                                    Text(
                                        text = numberFormat.format(item.unitPrice),
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1.0f)
                                    )
                                    Text(
                                        text = "${numberFormat.format(item.totalPrice)} ج.م",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1.1f)
                                    )
                                }
                                if (index < items.size - 1) {
                                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFDDDDDD))
                                }
                            }

                            if (invoice.notes.isNotBlank()) {
                                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF888888))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 6.dp)
                                ) {
                                    Text(
                                        text = "ملاحظات الفاتورة: ${invoice.notes}",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 9.sp,
                                        color = Color(0xFF333333)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Financial Totals Summary Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (invoice.discount > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "إجمالي الأصناف:", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF333333))
                                        Text(text = "${numberFormat.format(invoice.subtotal)} ج.م", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Black)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "قيمة الخصم التجاري:", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF333333))
                                        Text(text = "-${numberFormat.format(invoice.discount)} ج.م", fontFamily = CairoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    HorizontalDivider(thickness = 0.5.dp, color = Color.Black)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "صافي المبلغ المطلوب:",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${numberFormat.format(invoice.finalTotal)} ج.م",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "المبلغ المسدد نقداً: ${numberFormat.format(invoice.paidAmount)} ج.م",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = Color(0xFF222222)
                                    )
                                    if (invoice.remainingAmount > 0) {
                                        Text(
                                            text = "المتبقي (آجل): ${numberFormat.format(invoice.remainingAmount)} ج.م",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    } else {
                                        Text(
                                            text = "الحالة: مسددة بالكامل (PAID)",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF222222)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Terms & Policy
                        HorizontalDivider(thickness = 1.dp, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "• البضاعة المباعة ترد وتستبدل خلال 14 يوماً من تاريخ الشراء بشرط سلامة الكرتونة ووجود هذه الفاتورة.",
                            fontFamily = CairoFontFamily,
                            fontSize = 8.5.sp,
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "• الضمان يسري من تاريخ هذه الفاتورة عبر مراكز الصيانة المعتمدة للشركات المصنعة.",
                            fontFamily = CairoFontFamily,
                            fontSize = 8.5.sp,
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "★ نسعد دائماً بخدمتكم • شكراً لثقتكم بمؤسسة البشيري للأجهزة الكهربائية ★",
                            fontFamily = CairoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPrintReceipt,
                        enabled = !isPrinting,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("print_invoice_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = TextDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isPrinting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TextDark,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جارٍ الإرسال للطابعة...", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طباعة حرارية (Bluetooth)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إغلاق", fontFamily = CairoFontFamily)
                    }
                }
            }
        }
    }
}

/**
 * Backward compatibility overload for legacy single-item sale records.
 */
@Composable
fun InvoicePreviewDialog(
    sale: SaleRecordEntity,
    isPrinting: Boolean = false,
    onPrintReceipt: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val storeProfile = remember { StoreProfileManager.getStoreProfile(context) }
    val syntheticInvoice = remember(sale, storeProfile) {
        InvoiceWithItems(
            invoice = InvoiceEntity(
                id = sale.id,
                invoiceNumber = "INV-${sale.id}",
                customerName = sale.customerName,
                customerPhone = sale.customerPhone,
                sellerId = sale.sellerId,
                sellerName = sale.sellerName,
                subtotal = sale.totalPrice,
                discount = 0.0,
                finalTotal = sale.totalPrice,
                paidAmount = sale.totalPrice,
                remainingAmount = 0.0,
                paymentStatus = "PAID",
                notes = sale.notes,
                timestamp = sale.timestamp,
                storeName = storeProfile.storeName,
                storeAddress = storeProfile.storeAddress,
                storePhone = storeProfile.storePhone,
                commercialRegister = storeProfile.commercialRegister,
                taxCard = storeProfile.taxCard,
                vatNumber = storeProfile.vatNumber,
                storeLogoUri = storeProfile.logoPath
            ),
            items = listOf(
                InvoiceItemEntity(
                    id = sale.id,
                    invoiceId = sale.id,
                    modelId = sale.modelId,
                    productName = sale.productName,
                    modelName = sale.modelName,
                    categoryName = sale.categoryName,
                    brandName = sale.brandName,
                    quantity = sale.quantity,
                    unitPrice = sale.soldPrice,
                    minAllowedPrice = sale.minAllowedPrice,
                    originalPrice = sale.originalFinalPrice,
                    totalPrice = sale.totalPrice
                )
            )
        )
    }

    InvoicePreviewDialog(
        invoiceWithItems = syntheticInvoice,
        isPrinting = isPrinting,
        onPrintReceipt = onPrintReceipt,
        onDismiss = onDismiss
    )
}
