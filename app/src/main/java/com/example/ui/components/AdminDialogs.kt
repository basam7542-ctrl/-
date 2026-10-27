package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.ProductModelEntity
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("إضافة قسم جديد", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم القسم (مثال: أجهزة تبريد، غسالات...)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder,
                        cursorColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("إضافة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun AddBrandDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("إضافة شركة في قسم $categoryName", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الشركة (مثال: توشيبا، شارب، إل جي...)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder,
                        cursorColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("إضافة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun AddProductDialog(
    categoryName: String,
    brandName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("إضافة منتج لـ $brandName", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "القسم: $categoryName • الشركة: $brandName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المنتج (مثال: غسالة أوتوماتيك إنفرتر)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder,
                        cursorColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("إضافة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun AddModelDialog(
    productName: String,
    onDismiss: () -> Unit,
    onConfirm: (modelName: String, finalPrice: Double, minAllowedPrice: Double, stock: Int, code: String) -> Unit
) {
    var modelName by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var finalPriceText by remember { mutableStateOf("") }
    var minPriceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("1") }

    val finalPrice = finalPriceText.toDoubleOrNull() ?: 0.0
    val minPrice = minPriceText.toDoubleOrNull() ?: 0.0
    val stock = stockText.toIntOrNull() ?: 0
    val isValid = modelName.isNotBlank() && finalPrice > 0 && minPrice > 0 && minPrice <= finalPrice && stock >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("إضافة نوع / موديل جديد", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "المنتج: $productName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("اسم الموديل / المواصفة / المقاس") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("كود الموديل (اختياري)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = finalPriceText,
                        onValueChange = { finalPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("سعر البيع النهائي") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minPriceText,
                        onValueChange = { minPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("الحد الأدنى للبيع") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter { c -> c.isDigit() } },
                    label = { Text("الكمية المتوفرة بالمخزن") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(modelName.trim(), finalPrice, minPrice, stock, code.trim())
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("حفظ الموديل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun EditModelDialog(
    model: ProductModelEntity,
    onDismiss: () -> Unit,
    onConfirm: (updated: ProductModelEntity) -> Unit
) {
    var modelName by remember { mutableStateOf(model.modelName) }
    var code by remember { mutableStateOf(model.code) }
    var finalPriceText by remember { mutableStateOf(model.finalPrice.toInt().toString()) }
    var minPriceText by remember { mutableStateOf(model.minAllowedPrice.toInt().toString()) }
    var stockText by remember { mutableStateOf(model.stockQuantity.toString()) }

    val finalPrice = finalPriceText.toDoubleOrNull() ?: 0.0
    val minPrice = minPriceText.toDoubleOrNull() ?: 0.0
    val stock = stockText.toIntOrNull() ?: 0
    val isValid = modelName.isNotBlank() && finalPrice > 0 && minPrice > 0 && minPrice <= finalPrice && stock >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تعديل بيانات الموديل والأسعار", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("اسم الموديل / المواصفة") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("كود الموديل") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = finalPriceText,
                        onValueChange = { finalPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("سعر البيع النهائي") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minPriceText,
                        onValueChange = { minPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("الحد الأدنى للبيع") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter { c -> c.isDigit() } },
                    label = { Text("الكمية المتوفرة بالمخزن") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(
                            model.copy(
                                modelName = modelName.trim(),
                                code = code.trim(),
                                finalPrice = finalPrice,
                                minAllowedPrice = minPrice,
                                stockQuantity = stock
                            )
                        )
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("تحديث", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun EditStockDialog(
    modelName: String,
    currentStock: Int,
    onDismiss: () -> Unit,
    onConfirm: (newStock: Int) -> Unit
) {
    var stockText by remember { mutableStateOf(currentStock.toString()) }
    val newStock = stockText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تعديل رصيد المخزن", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "الموديل: $modelName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWhite,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter { c -> c.isDigit() } },
                    label = { Text("الكمية الجديدة بالمخزن") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder,
                        cursorColor = GoldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (newStock != null && newStock >= 0) onConfirm(newStock) },
                enabled = newStock != null && newStock >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("حفظ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, initialPurchases: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialPurchasesText by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    val purchases = initialPurchasesText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("إضافة شركة موردة جديدة", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الشركة الموردة") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف / الخط الساخن") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initialPurchasesText,
                    onValueChange = { initialPurchasesText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("رصيد مشتريات افتتاحي (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), phone.trim(), purchases, notes.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("إضافة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun AddSupplierTransactionDialog(
    supplierName: String,
    onDismiss: () -> Unit,
    onConfirm: (type: String, amount: Double, notes: String) -> Unit
) {
    var type by remember { mutableStateOf("INVOICE") } // "INVOICE" or "PAYMENT"
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تسجيل حركة حساب لمورد", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "المورد: $supplierName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Radio Options for Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "INVOICE" }
                    ) {
                        RadioButton(
                            selected = type == "INVOICE",
                            onClick = { type = "INVOICE" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("توريد بضاعة (+مشتريات)", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "PAYMENT" }
                    ) {
                        RadioButton(
                            selected = type == "PAYMENT",
                            onClick = { type = "PAYMENT" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("سداد دفعة (+مدفوع)", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("المبلغ (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("البيان / رقم الفاتورة أو الشيك") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (amount > 0) onConfirm(type, amount, notes.trim()) },
                enabled = amount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("تسجيل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun AddMerchantDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, initialDelivered: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialDeliveredText by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    val delivered = initialDeliveredText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("إضافة تاجر بضاعة بالأجل", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم التاجر / المعرض") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف التاجر") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initialDeliveredText,
                    onValueChange = { initialDeliveredText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("رصيد بضاعة مسلمة افتتاحي (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), phone.trim(), delivered, notes.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("إضافة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun AddMerchantTransactionDialog(
    merchantName: String,
    onDismiss: () -> Unit,
    onConfirm: (type: String, amount: Double, notes: String) -> Unit
) {
    var type by remember { mutableStateOf("GOODS_DELIVERED") } // "GOODS_DELIVERED" or "COLLECTION"
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تسجيل حركة حساب تاجر", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "التاجر: $merchantName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Radio Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "GOODS_DELIVERED" }
                    ) {
                        RadioButton(
                            selected = type == "GOODS_DELIVERED",
                            onClick = { type = "GOODS_DELIVERED" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("تسليم بضاعة (+مستحق لي)", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "COLLECTION" }
                    ) {
                        RadioButton(
                            selected = type == "COLLECTION",
                            onClick = { type = "COLLECTION" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("تحصيل دفعة (+مسدد)", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("المبلغ (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("البيان / تفاصيل البضاعة أو إيصال التحصيل") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (amount > 0) onConfirm(type, amount, notes.trim()) },
                enabled = amount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("تسجيل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun EditSupplierTransactionDialog(
    supplierName: String,
    initialType: String,
    initialAmount: Double,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (type: String, amount: Double, notes: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var amountText by remember {
        mutableStateOf(if (initialAmount % 1.0 == 0.0) initialAmount.toLong().toString() else initialAmount.toString())
    }
    var notes by remember { mutableStateOf(initialNotes) }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تعديل حركة حساب مورد", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "المورد: $supplierName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Radio Options for Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "INVOICE" }
                    ) {
                        RadioButton(
                            selected = type == "INVOICE",
                            onClick = { type = "INVOICE" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("توريد بضاعة", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "PAYMENT" }
                    ) {
                        RadioButton(
                            selected = type == "PAYMENT",
                            onClick = { type = "PAYMENT" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("سداد دفعة", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("المبلغ (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("البيان / تفاصيل الحركة") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (amount > 0) onConfirm(type, amount, notes.trim()) },
                enabled = amount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("حفظ التعديل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun EditMerchantTransactionDialog(
    merchantName: String,
    initialType: String,
    initialAmount: Double,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (type: String, amount: Double, notes: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var amountText by remember {
        mutableStateOf(if (initialAmount % 1.0 == 0.0) initialAmount.toLong().toString() else initialAmount.toString())
    }
    var notes by remember { mutableStateOf(initialNotes) }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تعديل حركة حساب تاجر", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "التاجر: $merchantName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Radio Options for Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "GOODS_DELIVERED" }
                    ) {
                        RadioButton(
                            selected = type == "GOODS_DELIVERED",
                            onClick = { type = "GOODS_DELIVERED" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("تسليم بضاعة", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "COLLECTION" }
                    ) {
                        RadioButton(
                            selected = type == "COLLECTION",
                            onClick = { type = "COLLECTION" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("تحصيل دفعة", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("المبلغ (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("البيان / تفاصيل الحركة") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (amount > 0) onConfirm(type, amount, notes.trim()) },
                enabled = amount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("حفظ التعديل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, initialCreditSales: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialCreditSalesText by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    val initialCreditSales = initialCreditSalesText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("إضافة عميل آجل بالخارج جديد", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العميل") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف العميل (للتواصل وواتساب)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initialCreditSalesText,
                    onValueChange = { initialCreditSalesText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("رصيد آجل افتتاحي إن وجد (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات / العنوان") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), phone.trim(), initialCreditSales, notes.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("إضافة العميل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun EditCustomerDialog(
    initialName: String,
    initialPhone: String,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تعديل بيانات عميل آجل بالخارج", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العميل") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات / العنوان") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), phone.trim(), notes.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("حفظ التعديل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun EditMerchantDialog(
    initialName: String,
    initialPhone: String,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تعديل بيانات التاجر", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم التاجر / المعرض") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), phone.trim(), notes.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("حفظ التعديل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun EditSupplierDialog(
    initialName: String,
    initialPhone: String,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تعديل بيانات الشركة الموردة", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الشركة") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), phone.trim(), notes.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("حفظ التعديل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun AddCustomerTransactionDialog(
    customerName: String,
    onDismiss: () -> Unit,
    onConfirm: (type: String, amount: Double, productDescription: String, notes: String) -> Unit
) {
    var type by remember { mutableStateOf("CREDIT_SALE") } // "CREDIT_SALE" or "COLLECTION"
    var amountText by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تسجيل حركة آجل بالخارج", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "العميل: $customerName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Radio Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "CREDIT_SALE" }
                    ) {
                        RadioButton(
                            selected = type == "CREDIT_SALE",
                            onClick = { type = "CREDIT_SALE" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("بيع آجل (+مستحق)", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "COLLECTION" }
                    ) {
                        RadioButton(
                            selected = type == "COLLECTION",
                            onClick = { type = "COLLECTION" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("تحصيل دفعة (+مسدد)", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (type == "CREDIT_SALE") {
                    OutlinedTextField(
                        value = productDescription,
                        onValueChange = { productDescription = it },
                        label = { Text("وصف الجهاز / المنتج (مثال: شاشة 43 بوصة)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("المبلغ (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("البيان / ملاحظات أو رقم الإيصال") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (amount > 0) onConfirm(type, amount, productDescription.trim(), notes.trim()) },
                enabled = amount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("تسجيل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}

@Composable
fun EditCustomerTransactionDialog(
    customerName: String,
    initialType: String,
    initialAmount: Double,
    initialProductDescription: String,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (type: String, amount: Double, productDescription: String, notes: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var amountText by remember {
        mutableStateOf(if (initialAmount % 1.0 == 0.0) initialAmount.toLong().toString() else initialAmount.toString())
    }
    var productDescription by remember { mutableStateOf(initialProductDescription) }
    var notes by remember { mutableStateOf(initialNotes) }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("تعديل حركة آجل بالخارج", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "العميل: $customerName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Radio Options for Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "CREDIT_SALE" }
                    ) {
                        RadioButton(
                            selected = type == "CREDIT_SALE",
                            onClick = { type = "CREDIT_SALE" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("بيع آجل", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { type = "COLLECTION" }
                    ) {
                        RadioButton(
                            selected = type == "COLLECTION",
                            onClick = { type = "COLLECTION" },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                        Text("تحصيل دفعة", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (type == "CREDIT_SALE") {
                    OutlinedTextField(
                        value = productDescription,
                        onValueChange = { productDescription = it },
                        label = { Text("وصف الجهاز / المنتج") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("المبلغ (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("البيان / ملاحظات") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (amount > 0) onConfirm(type, amount, productDescription.trim(), notes.trim()) },
                enabled = amount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Text("حفظ التعديل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء", color = TextWhite) }
        }
    )
}
