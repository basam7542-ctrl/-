package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProductModelEntity
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SaleDialog(
    model: ProductModelEntity,
    sellerName: String,
    onDismiss: () -> Unit,
    onConfirmSale: (soldPrice: Double, quantity: Int, notes: String, customerName: String, customerPhone: String) -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }

    var quantity by remember { mutableIntStateOf(1) }
    var priceText by remember { mutableStateOf(model.finalPrice.toInt().toString()) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val enteredPrice = priceText.toDoubleOrNull() ?: 0.0
    val isBelowMinPrice = enteredPrice < model.minAllowedPrice
    val isQuantityExceeded = quantity > model.stockQuantity
    val isValid = enteredPrice >= model.minAllowedPrice && quantity > 0 && !isQuantityExceeded

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("sale_dialog"),
        containerColor = NavySurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تسجيل عملية بيع",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product & Model Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardBorder))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = model.productName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "${model.brandName} • ${model.categoryName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الموديل: ${model.modelName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        if (model.code.isNotBlank()) {
                            Text(
                                text = "كود الموديل: ${model.code}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Pricing Reference Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Final Selling Price Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavySurfaceVariant)
                            .border(1.dp, NavyCardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "سعر البيع النهائي",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${numberFormat.format(model.finalPrice)} ج.م",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }

                    // Minimum Allowed Price Box (Strict Rule)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavySurfaceVariant)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "الحد الأدنى للبيع",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldSecondary
                            )
                            Text(
                                text = "${numberFormat.format(model.minAllowedPrice)} ج.م",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }
                    }
                }

                // Available Stock Badge with warning indicator
                val isCurrentlyLow = model.stockQuantity <= 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الكمية المتوفرة بالمخزن:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCurrentlyLow) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "تنبيه",
                                tint = if (model.stockQuantity == 0) ErrorRed else WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (model.stockQuantity == 0) "نفد من المخزن (0)" else "${model.stockQuantity} قطعة" + if (isCurrentlyLow) " (منخفض)" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                model.stockQuantity == 0 -> ErrorRed
                                model.stockQuantity <= 2 -> WarningAmber
                                else -> SuccessGreen
                            }
                        )
                    }
                }

                // Quantity Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الكمية المباعة:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تقليل الكمية")
                        }
                        Text(
                            text = "$quantity",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        FilledTonalIconButton(
                            onClick = { if (quantity < model.stockQuantity) quantity++ },
                            enabled = quantity < model.stockQuantity,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة الكمية")
                        }
                    }
                }

                // Low Stock Warning after sale
                val remainingAfterSale = model.stockQuantity - quantity
                if (remainingAfterSale in 0..2) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (remainingAfterSale == 0) ErrorRed.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (remainingAfterSale == 0) ErrorRed else WarningAmber)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "تنبيه المخزون",
                                tint = if (remainingAfterSale == 0) ErrorRed else WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (remainingAfterSale == 0) {
                                    "تنبيه: بعد إتمام هذا البيع، سينفد هذا الموديل بالكامل من المخزون!"
                                } else {
                                    "تنبيه: بعد إتمام هذا البيع، سيتبقى $remainingAfterSale قطعة فقط في المخزون (أوشك على النفاد)!"
                                },
                                color = if (remainingAfterSale == 0) ErrorRed else WarningAmber,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Negotiated Unit Price Input
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it.filter { char -> char.isDigit() || char == '.' }
                        errorMessage = null
                    },
                    label = { Text("سعر البيع الفعلي للقطعة (ج.م)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isBelowMinPrice,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isBelowMinPrice) ErrorRed else GoldPrimary,
                        unfocusedBorderColor = if (isBelowMinPrice) ErrorRed else NavyCardBorder,
                        focusedLabelColor = if (isBelowMinPrice) ErrorRed else GoldPrimary,
                        cursorColor = GoldPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sale_price_input")
                )

                // Validation Warning if below minimum price
                if (isBelowMinPrice) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ErrorRed)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "تحذير",
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تنبيه: لا يمكن البيع بأقل من الحد الأدنى (${numberFormat.format(model.minAllowedPrice)} ج.م)",
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Customer Details Section Header
                Text(
                    text = "بيانات المشتري / العميل للفاتورة:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldSecondary
                )

                // Customer Name Input
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("اسم العميل (اختياري)") },
                    placeholder = { Text("مثال: الحاج محمد أحمد") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder,
                        focusedLabelColor = GoldPrimary,
                        cursorColor = GoldPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sale_customer_name_input")
                )

                // Customer Phone Input (keeping leading zeros)
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { input ->
                        // Allow digits and '+' for international format, strictly keep string with zeros
                        customerPhone = input.filter { it.isDigit() || it == '+' || it == ' ' || it == '-' }
                    },
                    label = { Text("رقم هاتف العميل (اختياري - مثلاً 010...)") },
                    placeholder = { Text("مثال: 01012345678") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder,
                        focusedLabelColor = GoldPrimary,
                        cursorColor = GoldPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sale_customer_phone_input")
                )

                // Additional Notes input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية على الفاتورة (اختياري)") },
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder,
                        cursorColor = GoldPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sale_notes_input")
                )

                // Total Calculation Box
                val totalAmount = enteredPrice * quantity
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldPrimary.copy(alpha = 0.12f))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إجمالي المبلغ المستحق:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "${numberFormat.format(totalAmount)} ج.م",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirmSale(
                            enteredPrice,
                            quantity,
                            notes.trim(),
                            customerName.trim(),
                            customerPhone.trim()
                        )
                    } else if (isBelowMinPrice) {
                        errorMessage = "السعر المدخل أقل من الحد الأدنى للبيع"
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = TextDark
                ),
                modifier = Modifier.testTag("confirm_sale_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تأكيد البيع", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
            ) {
                Text("إلغاء")
            }
        }
    )
}
