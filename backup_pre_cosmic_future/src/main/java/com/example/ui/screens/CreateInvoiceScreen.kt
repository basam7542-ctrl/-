package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.InvoiceWithItems
import com.example.data.local.ProductModelEntity
import com.example.data.local.UserAccountEntity
import com.example.data.repository.InvoiceDraftItem
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDarkBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated Multi-Item Invoice Creation Screen.
 *
 * Requirements fulfilled:
 * 1. Independent from products/catalog browsing.
 * 2. Displays invoice sequence and date.
 * 3. Customer name and phone inputs (phone preserves leading zero).
 * 4. «+ إضافة منتج» button to pick and add multiple different products.
 * 5. Quantity and negotiated price configuration per item with min price validation.
 * 6. Dynamic deletion and quantity adjustments per item.
 * 7. Live calculation of subtotal, discount, paid amount, and remaining balance.
 * 8. Actions: «حفظ الفاتورة» and «معاينة وطباعة الفاتورة».
 */
@Composable
fun CreateInvoiceScreen(
    currentUser: UserAccountEntity,
    availableModels: List<ProductModelEntity>,
    onClose: () -> Unit,
    onSubmitInvoice: (
        customerName: String,
        customerPhone: String,
        items: List<InvoiceDraftItem>,
        discount: Double,
        paidAmount: Double?,
        notes: String,
        andPrintPreview: Boolean
    ) -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG")) }
    val currentDateStr = remember { dateFormat.format(Date()) }

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("0") }
    var paidAmountText by remember { mutableStateOf("") }
    var isManualPaidSet by remember { mutableStateOf(false) }

    val draftItems = remember { mutableStateListOf<InvoiceDraftItem>() }
    var showProductPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val subtotal = draftItems.sumOf { it.totalPrice }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val finalTotal = (subtotal - discount).coerceAtLeast(0.0)

    val actualPaid = if (!isManualPaidSet || paidAmountText.isBlank()) {
        finalTotal
    } else {
        paidAmountText.toDoubleOrNull() ?: 0.0
    }
    val remainingAmount = (finalTotal - actualPaid).coerceAtLeast(0.0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
            .testTag("create_invoice_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // 1. Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NavySurfaceElevated)
                            .border(1.dp, GlassBorder, CircleShape)
                            .testTag("close_create_invoice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "استخراج فاتورة بيع جديدة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontFamily = CairoFontFamily
                        )
                        Text(
                            text = "التاريخ: $currentDateStr • البائع: ${currentUser.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = CairoFontFamily
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldContainer)
                        .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "فاتورة جديدة",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 11.sp
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Error Alert Banner
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. Customer Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بيانات المشتري / العميل (اختياري)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it; errorMessage = null },
                                label = { Text("اسم العميل") },
                                placeholder = { Text("مثال: محمد أحمد") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("invoice_customer_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = NavyCardBorder,
                                    cursorColor = GoldPrimary
                                )
                            )

                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { input ->
                                    // Preserve leading zeros strictly as String
                                    customerPhone = input.filter { it.isDigit() || it == '+' || it == ' ' || it == '-' }
                                    errorMessage = null
                                },
                                label = { Text("رقم الهاتف") },
                                placeholder = { Text("010...") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("invoice_customer_phone_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = NavyCardBorder,
                                    cursorColor = GoldPrimary
                                )
                            )
                        }
                    }
                }

                // 3. Invoice Items Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorderGlow),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section Header + "+ إضافة منتج" button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                    .background(GoldContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(17.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "أصناف الفاتورة",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${draftItems.size} أصناف مضافة (عدد غير محدود)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { showProductPicker = true; errorMessage = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = TextDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("add_item_to_invoice_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ إضافة منتج", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(thickness = 0.8.dp, color = GlassBorder)

                        // Empty State if no items added yet
                        if (draftItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "لم تتم إضافة أي أجهزة للفاتورة بعد",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "اضغط على «+ إضافة منتج» لاختيار أجهزة وموديلات من المخزون",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        } else {
                            // Render Each Added Draft Item
                            draftItems.forEachIndexed { index, draftItem ->
                                DraftItemRow(
                                    index = index + 1,
                                    draftItem = draftItem,
                                    numberFormat = numberFormat,
                                    onQuantityChange = { newQty ->
                                        if (newQty > 0 && newQty <= draftItem.model.stockQuantity) {
                                            draftItems[index] = draftItem.copy(quantity = newQty)
                                        }
                                    },
                                    onPriceChange = { newPrice ->
                                        draftItems[index] = draftItem.copy(negotiatedUnitPrice = newPrice)
                                    },
                                    onRemove = {
                                        draftItems.removeAt(index)
                                    }
                                )
                            }

                            // Dedicated button directly after item list: «+ إضافة منتج آخر» (open-ended with no limits)
                            Button(
                                onClick = { showProductPicker = true; errorMessage = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = TextDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .padding(top = 4.dp)
                                    .testTag("add_another_item_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+ إضافة منتج آخر إلى الفاتورة (صنف رقم ${draftItems.size + 1})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextDark
                                )
                            }
                        }
                    }
                }

                // 4. Financial Calculations Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorderGlow),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ملخص الحساب والمدفوعات",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )

                        // Subtotal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "إجمالي الأصناف قبل الخصم:", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                            Text(text = "${numberFormat.format(subtotal)} ج.م", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextWhite)
                        }

                        // Discount & Paid Inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = discountText,
                                onValueChange = { input ->
                                    discountText = input.filter { it.isDigit() || it == '.' }
                                },
                                label = { Text("قيمة الخصم (ج.م)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("invoice_discount_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = NavyCardBorder,
                                    cursorColor = GoldPrimary
                                )
                            )

                            OutlinedTextField(
                                value = if (isManualPaidSet) paidAmountText else numberFormat.format(finalTotal).replace(",", ""),
                                onValueChange = { input ->
                                    isManualPaidSet = true
                                    paidAmountText = input.filter { it.isDigit() || it == '.' }
                                },
                                label = { Text("المبلغ المدفوع نقداً") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("invoice_paid_amount_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = NavyCardBorder,
                                    cursorColor = GoldPrimary
                                )
                            )
                        }

                        HorizontalDivider(thickness = 0.8.dp, color = GlassBorder)

                        // Final Total Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldPrimary.copy(alpha = 0.12f))
                                .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "المبلغ الإجمالي النهائي المطلوب:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${numberFormat.format(finalTotal)} ج.م",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldSecondary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "المبلغ المتبقي (آجل):",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (remainingAmount > 0) WarningAmber else TextMuted
                                    )
                                    Text(
                                        text = if (remainingAmount <= 0) "خالصة بالكامل (0 ج.م)" else "${numberFormat.format(remainingAmount)} ج.م",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remainingAmount > 0) WarningAmber else SuccessGreen
                                    )
                                }
                            }
                        }

                        // Notes input
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات إضافية على الفاتورة (اختياري)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invoice_notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = NavyCardBorder,
                                cursorColor = GoldPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Sticky Bottom Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Save and preview/print button
                Button(
                    onClick = {
                        if (draftItems.isEmpty()) {
                            errorMessage = "يرجى إضافة صنف واحد على الأقل للفاتورة"
                            return@Button
                        }
                        onSubmitInvoice(
                            customerName.trim(),
                            customerPhone.trim(),
                            draftItems.toList(),
                            discount,
                            actualPaid,
                            notes.trim(),
                            true // andPrintPreview
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("save_and_preview_invoice_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("معاينة وطباعة الفاتورة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Save only button
                OutlinedButton(
                    onClick = {
                        if (draftItems.isEmpty()) {
                            errorMessage = "يرجى إضافة صنف واحد على الأقل للفاتورة"
                            return@OutlinedButton
                        }
                        onSubmitInvoice(
                            customerName.trim(),
                            customerPhone.trim(),
                            draftItems.toList(),
                            discount,
                            actualPaid,
                            notes.trim(),
                            false // andPrintPreview
                        )
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_invoice_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ الفاتورة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Product Picker Modal Dialog
        if (showProductPicker) {
            ProductPickerModalDialog(
                availableModels = availableModels,
                numberFormat = numberFormat,
                onDismiss = { showProductPicker = false },
                onSelectModel = { model, qty, unitPrice ->
                    // Check if already in draft
                    val existingIndex = draftItems.indexOfFirst { it.model.id == model.id }
                    if (existingIndex >= 0) {
                        val existing = draftItems[existingIndex]
                        val combinedQty = existing.quantity + qty
                        val cappedQty = combinedQty.coerceAtMost(model.stockQuantity)
                        draftItems[existingIndex] = existing.copy(quantity = cappedQty, negotiatedUnitPrice = unitPrice)
                    } else {
                        draftItems.add(InvoiceDraftItem(model = model, quantity = qty, negotiatedUnitPrice = unitPrice))
                    }
                    showProductPicker = false
                }
            )
        }
    }
}

/**
 * Single Draft Item Card inside the Invoice builder
 */
@Composable
private fun DraftItemRow(
    index: Int,
    draftItem: InvoiceDraftItem,
    numberFormat: NumberFormat,
    onQuantityChange: (Int) -> Unit,
    onPriceChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    var isEditingPrice by remember { mutableStateOf(false) }
    var priceInput by remember { mutableStateOf(draftItem.unitPrice.toInt().toString()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("draft_item_row_${draftItem.model.id}"),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, GlassBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Item name + Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(GoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${draftItem.model.productName} - ${draftItem.model.modelName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        val specs = listOf(draftItem.model.brandName, draftItem.model.categoryName).filter { it.isNotBlank() }.joinToString(" • ")
                        if (specs.isNotBlank()) {
                            Text(
                                text = specs,
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp).testTag("remove_item_${draftItem.model.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف الصنف",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = GlassBorder)

            // Quantity controls and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "الكمية:", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    Spacer(modifier = Modifier.width(6.dp))

                    FilledTonalIconButton(
                        onClick = { onQuantityChange(draftItem.quantity - 1) },
                        enabled = draftItem.quantity > 1,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "تقليل", modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "${draftItem.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    FilledTonalIconButton(
                        onClick = { onQuantityChange(draftItem.quantity + 1) },
                        enabled = draftItem.quantity < draftItem.model.stockQuantity,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(المتوفر: ${draftItem.model.stockQuantity})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                // Line Total
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${numberFormat.format(draftItem.unitPrice)} ج.م / للقطعة",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${numberFormat.format(draftItem.totalPrice)} ج.م",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldPrimary
                    )
                }
            }
        }
    }
}

/**
 * Product Picker Modal Dialog to choose products from inventory
 */
@Composable
private fun ProductPickerModalDialog(
    availableModels: List<ProductModelEntity>,
    numberFormat: NumberFormat,
    onDismiss: () -> Unit,
    onSelectModel: (model: ProductModelEntity, quantity: Int, unitPrice: Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedModelForConfig by remember { mutableStateOf<ProductModelEntity?>(null) }

    val filtered = remember(searchQuery, availableModels) {
        val query = searchQuery.trim().lowercase()
        if (query.isBlank()) {
            availableModels
        } else {
            availableModels.filter {
                it.productName.lowercase().contains(query) ||
                it.modelName.lowercase().contains(query) ||
                it.brandName.lowercase().contains(query) ||
                it.categoryName.lowercase().contains(query) ||
                it.code.lowercase().contains(query)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = NavyDarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorderGlow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اختيار جهاز من المخزون",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث باسم الجهاز، الموديل، الماركة...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder,
                        cursorColor = GoldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Model Selection List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filtered.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد أجهزة مطابقة للبحث",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        items(filtered, key = { it.id }) { model ->
                            val isOutOfStock = model.stockQuantity <= 0
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isOutOfStock) {
                                        selectedModelForConfig = model
                                    }
                                    .testTag("picker_model_${model.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isOutOfStock) NavySurface.copy(alpha = 0.5f) else NavySurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    if (isOutOfStock) GlassBorder else NavyCardBorder
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${model.productName} - ${model.modelName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOutOfStock) TextMuted else TextWhite
                                        )
                                        Text(
                                            text = "${model.brandName} • ${model.categoryName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldSecondary,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isOutOfStock) "نفد من المخزن (0)" else "المتوفر بالمخزن: ${model.stockQuantity} قطعة",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOutOfStock) ErrorRed else if (model.stockQuantity <= 2) WarningAmber else SuccessGreen,
                                            fontSize = 10.5.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${numberFormat.format(model.finalPrice)} ج.م",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOutOfStock) TextMuted else GoldPrimary
                                        )
                                        Text(
                                            text = "الحد الأدنى: ${numberFormat.format(model.minAllowedPrice)} ج.م",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Configure Quantity and Negotiated Price for Selected Model
    if (selectedModelForConfig != null) {
        val model = selectedModelForConfig!!
        var qty by remember { mutableIntStateOf(1) }
        var unitPriceText by remember { mutableStateOf(model.finalPrice.toInt().toString()) }
        var priceError by remember { mutableStateOf<String?>(null) }

        val enteredPrice = unitPriceText.toDoubleOrNull() ?: 0.0
        val isPriceBelowMin = enteredPrice < model.minAllowedPrice

        AlertDialog(
            onDismissRequest = { selectedModelForConfig = null },
            containerColor = NavySurface,
            title = {
                Text(
                    text = "إضافة: ${model.productName} - ${model.modelName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "المتوفر في المخزن: ${model.stockQuantity} قطعة",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )

                    // Quantity selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الكمية المطلوبة:", style = MaterialTheme.typography.bodyMedium, color = TextWhite)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = { if (qty > 1) qty-- },
                                enabled = qty > 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$qty",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            FilledTonalIconButton(
                                onClick = { if (qty < model.stockQuantity) qty++ },
                                enabled = qty < model.stockQuantity,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Price input
                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = {
                            unitPriceText = it.filter { char -> char.isDigit() || char == '.' }
                            priceError = null
                        },
                        label = { Text("سعر بيع الوحدة المتفق عليه (ج.م)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = isPriceBelowMin,
                        supportingText = {
                            if (isPriceBelowMin) {
                                Text(
                                    text = "تنبيه: لا يمكن البيع بأقل من الحد الأدنى (${numberFormat.format(model.minAllowedPrice)} ج.م)",
                                    color = ErrorRed
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("picker_unit_price_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isPriceBelowMin) ErrorRed else GoldPrimary,
                            unfocusedBorderColor = if (isPriceBelowMin) ErrorRed else NavyCardBorder,
                            cursorColor = GoldPrimary
                        )
                    )

                    // Subtotal Preview
                    val lineTotal = enteredPrice * qty
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "إجمالي هذا الصنف:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(
                            text = "${numberFormat.format(lineTotal)} ج.م",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isPriceBelowMin) {
                            priceError = "السعر أقل من الحد الأدنى"
                            return@Button
                        }
                        onSelectModel(model, qty, enteredPrice)
                        selectedModelForConfig = null
                    },
                    enabled = !isPriceBelowMin && qty > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    ),
                    modifier = Modifier.testTag("confirm_add_product_to_invoice")
                ) {
                    Text("إضافة للفاتورة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedModelForConfig = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
