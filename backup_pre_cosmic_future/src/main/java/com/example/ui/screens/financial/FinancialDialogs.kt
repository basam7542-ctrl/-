package com.example.ui.screens.financial

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomerAccountEntity
import com.example.data.local.ExpenseCategoryEntity
import com.example.data.local.MerchantAccountEntity
import com.example.data.local.SupplierAccountEntity
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDarkBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun RecordTreasuryTxDialog(
    initialType: String = "CASH_IN",
    onDismiss: () -> Unit,
    onConfirm: (type: String, category: String, amount: Double, partyName: String, paymentMethod: String, description: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) } // "CASH_IN", "CASH_OUT"
    var category by remember {
        mutableStateOf(if (initialType == "CASH_IN") "SALE_CASH" else "SUPPLIER_PAYMENT")
    }
    var amountText by remember { mutableStateOf("") }
    var partyName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val inCategories = listOf(
        "SALE_CASH" to "مبيعات نقدية مباشرة",
        "CUSTOMER_COLLECTION" to "تحصيل من عميل",
        "MERCHANT_COLLECTION" to "تحصيل من تاجر",
        "CAPITAL_INJECTION" to "توريد رأس مال للخزينة",
        "RETURN_REFUND" to "استرداد نقدي من مورد",
        "ADJUSTMENT" to "تسوية نقدية واردة"
    )

    val outCategories = listOf(
        "SUPPLIER_PAYMENT" to "سداد دفعة لمورد",
        "EXPENSE" to "مصروفات إدارية وتشغيلية",
        "RETURN_REFUND" to "رد نقدية لمرتجع عميل",
        "ADJUSTMENT" to "تسوية نقدية منصرفة",
        "TRANSFER" to "تحويل لحساب بنكي أو عهدة"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = {
            Text(
                text = if (type == "CASH_IN") "تسجيل مقبوضات نقدية (وارد)" else "تسجيل مدفوعات نقدية (صادر)",
                color = if (type == "CASH_IN") SuccessGreen else ErrorRed,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "CASH_IN",
                        onClick = {
                            type = "CASH_IN"
                            category = "SALE_CASH"
                        },
                        label = { Text("مقبوضات (وارد +)", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SuccessGreen,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "CASH_OUT",
                        onClick = {
                            type = "CASH_OUT"
                            category = "SUPPLIER_PAYMENT"
                        },
                        label = { Text("مدفوعات (صادر -)", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ErrorRed,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Category Selector
                Text("تصنيف الحركة:", color = TextMuted, fontSize = 12.sp)
                var expandedCategory by remember { mutableStateOf(false) }
                val currentCategoryLabel = if (type == "CASH_IN") {
                    inCategories.find { it.first == category }?.second ?: category
                } else {
                    outCategories.find { it.first == category }?.second ?: category
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedCategory = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentCategoryLabel, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                        modifier = Modifier.background(NavySurface)
                    ) {
                        val activeCategories = if (type == "CASH_IN") inCategories else outCategories
                        activeCategories.forEach { (catKey, catLabel) ->
                            DropdownMenuItem(
                                text = { Text(catLabel, color = TextWhite) },
                                onClick = {
                                    category = catKey
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("المبلغ (ج.م) *", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder
                    )
                )

                // Party Name
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text("الجهة / الشخص / المستلم", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder
                    )
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("بيان وتفاصيل الحركة", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "يرجى إدخال مبلغ صحيح أكبر من الصفر"
                        return@Button
                    }
                    onConfirm(type, category, amount, partyName.trim(), paymentMethod, description.trim())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "CASH_IN") SuccessGreen else ErrorRed
                )
            ) {
                Text("حفظ وتأكيد", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}

@Composable
fun RecordExpenseDialog(
    categories: List<ExpenseCategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (categoryName: String, amount: Double, description: String, beneficiary: String, paymentMethod: String, referenceNumber: String, notes: String) -> Unit
) {
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull()?.name ?: "مصاريف إدارية أخرى")
    }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var beneficiary by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH_TREASURY") } // "CASH_TREASURY", "BANK", "WALLET"
    var referenceNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expandedCat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = {
            Text("تسجيل مصروف جديد", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Picker
                Text("بند المصروف *", color = TextMuted, fontSize = 12.sp)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedCat = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCategory, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false },
                        modifier = Modifier.background(NavySurface)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name, color = TextWhite) },
                                onClick = {
                                    selectedCategory = cat.name
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("المبلغ (ج.م) *", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder
                    )
                )

                // Description (Required)
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        errorMessage = null
                    },
                    label = { Text("بيان المصروف وتفاصيله *", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder
                    )
                )

                // Beneficiary
                OutlinedTextField(
                    value = beneficiary,
                    onValueChange = { beneficiary = it },
                    label = { Text("الجهة المستفيدة (مثلاً: شركة الكهرباء / السائق)", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder
                    )
                )

                // Payment Method
                Text("طريقة الدفع:", color = TextMuted, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = paymentMethod == "CASH_TREASURY",
                        onClick = { paymentMethod = "CASH_TREASURY" },
                        label = { Text("نقدًا (الخزينة)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = paymentMethod == "BANK",
                        onClick = { paymentMethod = "BANK" },
                        label = { Text("تحويل بنكي", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = paymentMethod == "WALLET",
                        onClick = { paymentMethod = "WALLET" },
                        label = { Text("محفظة إلكترونية", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF9C27B0),
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // Reference / Bill number
                OutlinedTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    label = { Text("رقم الفاتورة / الإيصال إن وجد", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "يرجى إدخال مبلغ صحيح أكبر من الصفر"
                        return@Button
                    }
                    if (description.isBlank()) {
                        errorMessage = "يرجى كتابة بيان المصروف"
                        return@Button
                    }
                    onConfirm(
                        selectedCategory,
                        amount,
                        description.trim(),
                        beneficiary.trim(),
                        paymentMethod,
                        referenceNumber.trim(),
                        notes.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("حفظ المصروف", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}

@Composable
fun ManageExpenseCategoriesDialog(
    categories: List<ExpenseCategoryEntity>,
    onDismiss: () -> Unit,
    onAddCategory: (name: String, description: String) -> Unit,
    onDeleteCategory: (id: Long) -> Unit
) {
    var newCatName by remember { mutableStateOf("") }
    var newCatDesc by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = {
            Text("إدارة بنود وتصنيفات المصروفات", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add new category section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = {
                            newCatName = it
                            errorMessage = null
                        },
                        label = { Text("اسم البند الجديد", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = NavyCardBorder
                        )
                    )
                    Button(
                        onClick = {
                            if (newCatName.isBlank()) {
                                errorMessage = "يرجى كتابة اسم البند"
                                return@Button
                            }
                            onAddCategory(newCatName.trim(), newCatDesc.trim())
                            newCatName = ""
                            newCatDesc = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    }
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = ErrorRed, fontSize = 12.sp)
                }

                Text("البنود المسجلة حالياً:", color = TextMuted, fontSize = 12.sp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavySurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cat.name, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    if (cat.isDefault) {
                                        Text("بند أساسي للنظام", color = TextMuted, fontSize = 11.sp)
                                    }
                                }
                                if (!cat.isDefault) {
                                    IconButton(onClick = { onDeleteCategory(cat.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)) {
                Text("إغلاق", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun RecordFinancialAdjustmentDialog(
    suppliers: List<SupplierAccountEntity>,
    merchants: List<MerchantAccountEntity>,
    customers: List<CustomerAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (targetType: String, targetId: Long?, targetName: String, adjType: String, amount: Double, reason: String) -> Unit
) {
    var targetAccountType by remember { mutableStateOf("TREASURY") } // "TREASURY", "SUPPLIER", "MERCHANT", "CUSTOMER"
    var selectedEntityId by remember { mutableStateOf<Long?>(null) }
    var selectedEntityName by remember { mutableStateOf("") }
    var adjustmentType by remember { mutableStateOf("CREDIT") } // "CREDIT", "DEBIT"
    var amountText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expandedEntityPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = {
            Text("إجراء تسوية مالية معتمدة", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("الحساب المستهدف بالتسوية:", color = TextMuted, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = targetAccountType == "TREASURY",
                        onClick = {
                            targetAccountType = "TREASURY"
                            selectedEntityId = null
                            selectedEntityName = "الخزينة النقدية"
                        },
                        label = { Text("الخزينة", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = Color.Black)
                    )
                    FilterChip(
                        selected = targetAccountType == "SUPPLIER",
                        onClick = {
                            targetAccountType = "SUPPLIER"
                            selectedEntityId = suppliers.firstOrNull()?.id
                            selectedEntityName = suppliers.firstOrNull()?.supplierName ?: ""
                        },
                        label = { Text("مورد", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = Color.Black)
                    )
                    FilterChip(
                        selected = targetAccountType == "MERCHANT",
                        onClick = {
                            targetAccountType = "MERCHANT"
                            selectedEntityId = merchants.firstOrNull()?.id
                            selectedEntityName = merchants.firstOrNull()?.merchantName ?: ""
                        },
                        label = { Text("تاجر", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = Color.Black)
                    )
                    FilterChip(
                        selected = targetAccountType == "CUSTOMER",
                        onClick = {
                            targetAccountType = "CUSTOMER"
                            selectedEntityId = customers.firstOrNull()?.id
                            selectedEntityName = customers.firstOrNull()?.customerName ?: ""
                        },
                        label = { Text("عميل", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = Color.Black)
                    )
                }

                // If not treasury, pick specific party
                if (targetAccountType != "TREASURY") {
                    Text("اختر الطرف المعني:", color = TextMuted, fontSize = 12.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedEntityPicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (selectedEntityName.isNotBlank()) selectedEntityName else "اختر الحساب", fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expandedEntityPicker,
                            onDismissRequest = { expandedEntityPicker = false },
                            modifier = Modifier.background(NavySurface)
                        ) {
                            when (targetAccountType) {
                                "SUPPLIER" -> {
                                    suppliers.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text("${s.supplierName} (متبقي له: ${s.remainingBalance} ج.م)", color = TextWhite) },
                                            onClick = {
                                                selectedEntityId = s.id
                                                selectedEntityName = s.supplierName
                                                expandedEntityPicker = false
                                            }
                                        )
                                    }
                                }
                                "MERCHANT" -> {
                                    merchants.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text("${m.merchantName} (مستحق لي: ${m.remainingDueToMe} ج.م)", color = TextWhite) },
                                            onClick = {
                                                selectedEntityId = m.id
                                                selectedEntityName = m.merchantName
                                                expandedEntityPicker = false
                                            }
                                        )
                                    }
                                }
                                "CUSTOMER" -> {
                                    customers.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text("${c.customerName} (مستحق لي: ${c.remainingDueToMe} ج.م)", color = TextWhite) },
                                            onClick = {
                                                selectedEntityId = c.id
                                                selectedEntityName = c.customerName
                                                expandedEntityPicker = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Adjustment Type: CREDIT vs DEBIT
                Text("نوع التسوية:", color = TextMuted, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = adjustmentType == "CREDIT",
                        onClick = { adjustmentType = "CREDIT" },
                        label = {
                            Text(
                                if (targetAccountType == "TREASURY") "إيداع / زيادة رصيد (+)" else "إضافة رصيد / دائن (+)",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SuccessGreen, selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = adjustmentType == "DEBIT",
                        onClick = { adjustmentType = "DEBIT" },
                        label = {
                            Text(
                                if (targetAccountType == "TREASURY") "سحب / خصم رصيد (-)" else "خصم رصيد / مدين (-)",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ErrorRed, selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("مبلغ التسوية (ج.م) *", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder
                    )
                )

                // Mandatory Reason
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        errorMessage = null
                    },
                    label = { Text("سبب وتبرير التسوية بالتفصيل *", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyCardBorder
                    )
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "يرجى كتابة مبلغ صحيح أكبر من الصفر"
                        return@Button
                    }
                    if (reason.isBlank()) {
                        errorMessage = "يجب ذكر سبب التسوية بالتفصيل للتوثيق المالي"
                        return@Button
                    }
                    val finalTargetName = if (targetAccountType == "TREASURY") "الخزينة النقدية" else selectedEntityName
                    onConfirm(targetAccountType, selectedEntityId, finalTargetName, adjustmentType, amount, reason.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("اعتماد التسوية", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}

@Composable
fun ReverseTransactionDialog(
    title: String,
    targetNumber: String,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(36.dp))
        },
        title = {
            Text(title, color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "سيتم إجراء حركة عكسية تصحيحية لإلغاء المعاملة رقم $targetNumber وضبط الأرصدة بدقة مع الاحتفاظ بالسجل التاريخي دون حذف عشوائي.",
                    color = TextWhite,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        errorMessage = null
                    },
                    label = { Text("سبب الإلغاء والحركة العكسية *", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ErrorRed,
                        unfocusedBorderColor = NavyCardBorder
                    )
                )
                if (errorMessage != null) {
                    Text(errorMessage!!, color = ErrorRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        errorMessage = "يجب توثيق سبب الحركة العكسية"
                        return@Button
                    }
                    onConfirm(reason.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text("تأكيد الحركة العكسية", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}
