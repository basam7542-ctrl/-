package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.MerchantAccountEntity
import com.example.data.local.ProductModelEntity
import com.example.data.local.StockMovementEntity
import com.example.data.local.SupplierAccountEntity
import com.example.data.local.UserAccountEntity
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockMovementScreen(
    currentUser: UserAccountEntity,
    movements: List<StockMovementEntity>,
    models: List<ProductModelEntity>,
    suppliers: List<SupplierAccountEntity>,
    merchants: List<MerchantAccountEntity>,
    searchQuery: String,
    typeFilter: String?,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String?) -> Unit,
    onReceiveStock: (
        modelId: Long,
        quantity: Int,
        unitCostPrice: Double,
        supplierId: Long?,
        merchantId: Long?,
        supplierOrMerchantName: String,
        purchaseInvoiceNumber: String,
        notes: String,
        onSuccess: (ProductModelEntity) -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    onExportCsv: () -> Unit
) {
    val context = LocalContext.current
    val isAdmin = currentUser.role == "ADMIN"
    var showReceiveDialog by remember { mutableStateOf(false) }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar", "EG")) }

    // Metrics calculations
    val totalMovements = movements.size
    val totalInboundQty = movements.filter { it.quantityChanged > 0 }.sumOf { it.quantityChanged }
    val totalOutboundQty = movements.filter { it.quantityChanged < 0 }.sumOf { -it.quantityChanged }

    // Filter movements
    val filteredMovements = movements.filter { m ->
        val matchesType = when (typeFilter) {
            null, "ALL" -> true
            else -> m.movementType == typeFilter
        }
        val query = searchQuery.trim().lowercase(Locale.ROOT)
        val matchesQuery = query.isEmpty() ||
                m.productName.lowercase(Locale.ROOT).contains(query) ||
                m.modelName.lowercase(Locale.ROOT).contains(query) ||
                m.code.lowercase(Locale.ROOT).contains(query) ||
                m.movementType.lowercase(Locale.ROOT).contains(query) ||
                m.referenceNumber.lowercase(Locale.ROOT).contains(query) ||
                m.userName.lowercase(Locale.ROOT).contains(query) ||
                m.notes.lowercase(Locale.ROOT).contains(query)

        matchesType && matchesQuery
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "سجل حركة المخزون",
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "توثيق شامل وتاريخي لكل إضافة وخصم وتعديل على أرصدة المخزن",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onExportCsv,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(ElectricBlue, GoldPrimary))),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_export_movements_csv")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصدير CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (isAdmin) {
                        Button(
                            onClick = { showReceiveDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_receive_stock")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("توريد بضاعة واردة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MovementMetricCard(
                    title = "إجمالي الحركات",
                    value = "$totalMovements",
                    sub = "عملية موثقة",
                    color = GoldPrimary,
                    modifier = Modifier.weight(1f)
                )
                MovementMetricCard(
                    title = "إجمالي الوارد",
                    value = "+$totalInboundQty",
                    sub = "قطع مضافة",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                MovementMetricCard(
                    title = "إجمالي المنصرف",
                    value = "-$totalOutboundQty",
                    sub = "قطع مخصومة",
                    color = ErrorRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_movement_search"),
                placeholder = { Text("بحث باسم الجهاز، الموديل، الكود، رقم المستند، أو المسؤول...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = NavySurfaceElevated,
                    focusedContainerColor = NavySurface,
                    unfocusedContainerColor = NavySurface,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Movement Types Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "الكل ($totalMovements)",
                    "PURCHASE" to "شراء / توريد وارد",
                    "SALE" to "بيع / فاتورة",
                    "RETURN_CUSTOMER" to "مرتجع من عميل",
                    "RETURN_SUPPLIER" to "مرتجع لمورد",
                    "DAMAGE_SCRAP" to "هالك وتالف",
                    "REPAIR_RESTORE" to "إعادة بعد إصلاح",
                    "INVENTORY_AUDIT_SURPLUS" to "تسوية جرد (زيادة)",
                    "INVENTORY_AUDIT_DEFICIT" to "تسوية جرد (عجز)",
                    "MANUAL_ADJUSTMENT" to "تعديل يدوي"
                )

                items(filters) { (key, label) ->
                    val isSelected = (typeFilter == key) || (typeFilter == null && key == "ALL")
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(if (key == "ALL") "ALL" else key) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = TextDark,
                            containerColor = NavySurface,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) GoldPrimary else NavySurfaceElevated,
                            selectedBorderColor = GoldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Movements List
            if (filteredMovements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "لا توجد حركات مخزون مطابقة لبحثك" else "لا توجد حركات مخزون مسجلة بعد",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMovements, key = { it.id }) { movement ->
                        StockMovementCard(
                            movement = movement,
                            dateFormat = dateFormat
                        )
                    }
                }
            }
        }
    }

    // Dialog: Receive Stock from Suppliers / Merchants
    if (showReceiveDialog) {
        ReceiveStockDialog(
            models = models,
            suppliers = suppliers,
            merchants = merchants,
            onDismiss = { showReceiveDialog = false },
            onSubmit = { modelId, qty, cost, sId, mId, partyName, invNum, notes ->
                onReceiveStock(
                    modelId, qty, cost, sId, mId, partyName, invNum, notes,
                    {
                        showReceiveDialog = false
                    },
                    { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
private fun MovementMetricCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.4f), NavySurfaceElevated)))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(sub, color = TextMuted.copy(alpha = 0.8f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun StockMovementCard(
    movement: StockMovementEntity,
    dateFormat: SimpleDateFormat
) {
    val isPositive = movement.quantityChanged > 0
    val changeText = if (isPositive) "+${movement.quantityChanged}" else "${movement.quantityChanged}"

    val typeDetails = when (movement.movementType) {
        "PURCHASE" -> Triple("شراء / توريد وارد", SuccessGreen, Icons.Default.ArrowDownward)
        "SALE" -> Triple("بيع / فاتورة", ElectricBlue, Icons.Default.ArrowUpward)
        "RETURN_CUSTOMER" -> Triple("مرتجع من عميل", Color(0xFF26C6DA), Icons.Default.ArrowDownward)
        "RETURN_SUPPLIER" -> Triple("مرتجع إلى مورد", WarningAmber, Icons.Default.ArrowUpward)
        "DAMAGE_SCRAP" -> Triple("هالك وتالف", ErrorRed, Icons.Default.Dangerous)
        "REPAIR_RESTORE" -> Triple("إعادة بعد الإصلاح", Color(0xFFAB47BC), Icons.Default.Build)
        "INVENTORY_AUDIT_SURPLUS" -> Triple("تسوية جرد (زيادة)", Color(0xFF00B0FF), Icons.Default.ArrowDownward)
        "INVENTORY_AUDIT_DEFICIT" -> Triple("تسوية جرد (عجز)", Color(0xFFFF7043), Icons.Default.ArrowUpward)
        "MANUAL_ADJUSTMENT" -> Triple("تعديل يدوي", GoldPrimary, Icons.Default.Edit)
        else -> Triple(movement.movementType, TextWhite, Icons.Default.SwapHoriz)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stock_movement_card_${movement.id}"),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(typeDetails.second.copy(alpha = 0.35f), NavySurfaceElevated)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Type Badge, Reference, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = typeDetails.second.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(typeDetails.second, typeDetails.second.copy(alpha = 0.3f))))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(typeDetails.third, contentDescription = null, tint = typeDetails.second, modifier = Modifier.size(13.dp))
                        Text(
                            text = typeDetails.first,
                            color = typeDetails.second,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (movement.referenceNumber.isNotBlank()) {
                    Surface(
                        color = NavySurfaceElevated,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${movement.referenceType}: ${movement.referenceNumber}",
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product & Model Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${movement.productName} - ${movement.modelName}",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${movement.categoryName} • ${movement.brandName} ${if (movement.code.isNotBlank()) "• كود: ${movement.code}" else ""}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Balance flow badge: before -> change -> after
                Surface(
                    color = NavySurfaceElevated,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("${movement.quantityBefore}", color = TextMuted, fontSize = 12.sp)
                        Text("➔", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = changeText,
                            color = if (isPositive) SuccessGreen else ErrorRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text("➔", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = "${movement.quantityAfter}",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (movement.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = movement.notes,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = NavySurfaceElevated)
            Spacer(modifier = Modifier.height(6.dp))

            // Footer: User & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Text(
                        text = "المسؤول: ${movement.userName}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = dateFormat.format(Date(movement.timestamp)),
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// Dialog: Receive Stock from Suppliers / Merchants
@Composable
private fun ReceiveStockDialog(
    models: List<ProductModelEntity>,
    suppliers: List<SupplierAccountEntity>,
    merchants: List<MerchantAccountEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        modelId: Long,
        quantity: Int,
        unitCostPrice: Double,
        supplierId: Long?,
        merchantId: Long?,
        supplierOrMerchantName: String,
        purchaseInvoiceNumber: String,
        notes: String
    ) -> Unit
) {
    var selectedModel by remember { mutableStateOf<ProductModelEntity?>(null) }
    var quantityText by remember { mutableStateOf("1") }
    var unitCostText by remember { mutableStateOf("") }
    var purchaseInvoiceNum by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var partyType by remember { mutableStateOf("SUPPLIER") } // "SUPPLIER", "MERCHANT", "EXTERNAL"
    var selectedSupplier by remember { mutableStateOf<SupplierAccountEntity?>(null) }
    var selectedMerchant by remember { mutableStateOf<MerchantAccountEntity?>(null) }
    var externalPartyName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = NavySurface,
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, NavySurfaceElevated)))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "تسجيل بضاعة واردة (إضافة مخزون)",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "إضافة كميات واردة من الموردين والتجار وتحديث رصيد المخزن تلقائيًا",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Select Product Model
                Text("اختر المنتج / الموديل الوارد:", color = TextWhite, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                var modelMenuExpanded by remember { mutableStateOf(false) }

                Box {
                    OutlinedButton(
                        onClick = { modelMenuExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_select_model_inbound"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                    ) {
                        Text(
                            text = selectedModel?.let { "${it.productName} - ${it.modelName} (رصيد حالي: ${it.stockQuantity})" }
                                ?: "اضغط لاختيار الصنف من قائمة المخزن",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = modelMenuExpanded,
                        onDismissRequest = { modelMenuExpanded = false },
                        modifier = Modifier.background(NavySurface)
                    ) {
                        models.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${m.productName} - ${m.modelName}", color = TextWhite, fontWeight = FontWeight.Bold)
                                        Text("كود: ${m.code} • الرصيد الحالي: ${m.stockQuantity} قطع", color = TextMuted, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    selectedModel = m
                                    unitCostText = m.minAllowedPrice.toString()
                                    modelMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity & Cost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("الكمية الواردة") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_inbound_quantity"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = NavySurfaceElevated
                        )
                    )

                    OutlinedTextField(
                        value = unitCostText,
                        onValueChange = { unitCostText = it },
                        label = { Text("سعر التكلفة (ج.م)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_inbound_cost"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = NavySurfaceElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Supplier / Merchant Linkage
                Text("مصدر التوريد:", color = TextWhite, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = partyType == "SUPPLIER",
                        onClick = { partyType = "SUPPLIER" },
                        label = { Text("مورد مسجل") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = TextDark),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = partyType == "MERCHANT",
                        onClick = { partyType = "MERCHANT" },
                        label = { Text("تاجر مسجل") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = TextDark),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = partyType == "EXTERNAL",
                        onClick = { partyType = "EXTERNAL" },
                        label = { Text("جهة أخرى") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = TextDark),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (partyType) {
                    "SUPPLIER" -> {
                        var supplierMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { supplierMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                            ) {
                                Text(selectedSupplier?.supplierName ?: "اختر المورد من كشوف الحسابات")
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = supplierMenuExpanded,
                                onDismissRequest = { supplierMenuExpanded = false },
                                modifier = Modifier.background(NavySurface)
                            ) {
                                suppliers.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s.supplierName, color = TextWhite) },
                                        onClick = {
                                            selectedSupplier = s
                                            supplierMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    "MERCHANT" -> {
                        var merchantMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { merchantMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                            ) {
                                Text(selectedMerchant?.merchantName ?: "اختر التاجر من كشوف الحسابات")
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = merchantMenuExpanded,
                                onDismissRequest = { merchantMenuExpanded = false },
                                modifier = Modifier.background(NavySurface)
                            ) {
                                merchants.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m.merchantName, color = TextWhite) },
                                        onClick = {
                                            selectedMerchant = m
                                            merchantMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    "EXTERNAL" -> {
                        OutlinedTextField(
                            value = externalPartyName,
                            onValueChange = { externalPartyName = it },
                            label = { Text("اسم المورد / الشركة الموردة") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = NavySurfaceElevated
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = purchaseInvoiceNum,
                    onValueChange = { purchaseInvoiceNum = it },
                    label = { Text("رقم فاتورة الشراء / إذن الاستلام") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavySurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavySurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("إلغاء", color = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val model = selectedModel ?: return@Button
                            val qty = quantityText.toIntOrNull() ?: 1
                            val cost = unitCostText.toDoubleOrNull() ?: model.minAllowedPrice
                            val partyName = when (partyType) {
                                "SUPPLIER" -> selectedSupplier?.supplierName ?: ""
                                "MERCHANT" -> selectedMerchant?.merchantName ?: ""
                                else -> externalPartyName
                            }

                            onSubmit(
                                model.id,
                                qty,
                                cost,
                                selectedSupplier?.id,
                                selectedMerchant?.id,
                                partyName,
                                purchaseInvoiceNum,
                                notes
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_confirm_receive_stock")
                    ) {
                        Text("اعتماد وإضافة للمخزن", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
