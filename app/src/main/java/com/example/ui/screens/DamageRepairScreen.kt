package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.example.data.local.DamageRepairRecordEntity
import com.example.data.local.ProductModelEntity
import com.example.data.local.ReturnWithItems
import com.example.data.local.UserAccountEntity
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DamageRepairScreen(
    currentUser: UserAccountEntity,
    records: List<DamageRepairRecordEntity>,
    catalogModels: List<ProductModelEntity>,
    returnsWithItems: List<ReturnWithItems>,
    searchQuery: String,
    statusFilter: String?,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String?) -> Unit,
    onRegisterDamageOrScrap: (
        modelId: Long,
        quantity: Int,
        reason: String,
        damageDetails: String,
        initialStatus: String,
        sourceType: String,
        returnRecordId: Long?,
        returnNumber: String,
        unitCost: Double?,
        totalLossAmount: Double?,
        imageUri: String,
        notes: String,
        onSuccess: (DamageRepairRecordEntity) -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    onSendToRepair: (recordId: Long, reason: String, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onCompleteRepair: (recordId: Long, outcome: String, sparePartsCost: Double, laborCost: Double, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onApproveReturnToStock: (recordId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onExportCsv: () -> Unit
) {
    val context = LocalContext.current
    val isAdmin = currentUser.role == "ADMIN"

    var showRegisterDialog by remember { mutableStateOf(false) }
    var recordToSendToRepair by remember { mutableStateOf<DamageRepairRecordEntity?>(null) }
    var recordToCompleteRepair by remember { mutableStateOf<DamageRepairRecordEntity?>(null) }
    var recordToApproveStock by remember { mutableStateOf<DamageRepairRecordEntity?>(null) }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val currencyFormat: (Double) -> String = { "${numberFormat.format(it)} ج.م" }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar", "EG")) }

    // Summary calculations
    val totalScrapCount = records.filter { it.status == "TOTAL_SCRAP" }.sumOf { it.quantity }
    val underInspectionCount = records.filter { it.status == "UNDER_INSPECTION" }.sumOf { it.quantity }
    val underRepairCount = records.filter { it.status == "UNDER_REPAIR" }.sumOf { it.quantity }
    val pendingApprovalCount = records.filter { it.status == "REPAIRED_PENDING_APPROVAL" }.sumOf { it.quantity }
    val restoredStockCount = records.filter { it.status == "RESTORED_TO_STOCK" }.sumOf { it.quantity }
    val totalLoss = records.sumOf { it.totalLossAmount }
    val totalRepairExpenses = records.sumOf { it.totalRepairCost }

    // Filtered list
    val filteredRecords = records.filter { record ->
        val matchesStatus = when (statusFilter) {
            null, "ALL" -> true
            else -> record.status == statusFilter
        }
        val query = searchQuery.trim().lowercase(Locale.ROOT)
        val matchesQuery = query.isEmpty() ||
                record.recordNumber.lowercase(Locale.ROOT).contains(query) ||
                record.productName.lowercase(Locale.ROOT).contains(query) ||
                record.modelName.lowercase(Locale.ROOT).contains(query) ||
                record.reason.lowercase(Locale.ROOT).contains(query) ||
                record.registeredByName.lowercase(Locale.ROOT).contains(query) ||
                record.returnNumber.lowercase(Locale.ROOT).contains(query)

        matchesStatus && matchesQuery
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
                        text = "الهالك والتالف والإصلاح",
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "متابعة المنتجات التالفة، حركات الصيانة وإعادة الاعتماد للمخزون",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onExportCsv,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(ElectricBlue, GoldPrimary))),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_export_damage_csv")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصدير CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (isAdmin) {
                        Button(
                            onClick = { showRegisterDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = TextDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_add_damage_record")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل هالك / تالف", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Metrics Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    MetricCard(
                        title = "تالف نهائيًا (هالك)",
                        count = totalScrapCount,
                        subText = "قطع مستبعدة كليًا",
                        color = Color(0xFFEF5350),
                        icon = Icons.Default.Dangerous
                    )
                }
                item {
                    MetricCard(
                        title = "تحت الفحص",
                        count = underInspectionCount,
                        subText = "بانتظار التقييم الفني",
                        color = Color(0xFFFFA726),
                        icon = Icons.Default.HourglassTop
                    )
                }
                item {
                    MetricCard(
                        title = "تحت الإصلاح",
                        count = underRepairCount,
                        subText = "بمركز الصيانة",
                        color = Color(0xFF29B6F6),
                        icon = Icons.Default.Handyman
                    )
                }
                item {
                    MetricCard(
                        title = "بانتظار الاعتماد",
                        count = pendingApprovalCount,
                        subText = "تم إصلاحه وينتظر المدير",
                        color = Color(0xFFAB47BC),
                        icon = Icons.Default.Warning
                    )
                }
                item {
                    MetricCard(
                        title = "أعيد للبيع",
                        count = restoredStockCount,
                        subText = "أعيد للمخزون الصالح",
                        color = Color(0xFF66BB6A),
                        icon = Icons.Default.CheckCircle
                    )
                }
                item {
                    FinancialMetricCard(
                        title = "إجمالي الخسائر",
                        amount = currencyFormat(totalLoss),
                        subText = "قيمة المنتجات التالفة",
                        color = Color(0xFFE57373)
                    )
                }
                item {
                    FinancialMetricCard(
                        title = "مصاريف الإصلاح",
                        amount = currencyFormat(totalRepairExpenses),
                        subText = "قطع الغيار والمصنعيات",
                        color = Color(0xFF4FC3F7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar & Filter Chips
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_damage_search"),
                placeholder = { Text("بحث برقم السجل، اسم الجهاز، الموديل، السبب، أو المسؤول...", color = TextMuted, fontSize = 13.sp) },
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

            // Status Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "الكل (${records.size})",
                    "TOTAL_SCRAP" to "تالف نهائيًا",
                    "UNDER_INSPECTION" to "تحت الفحص",
                    "UNDER_REPAIR" to "تحت الإصلاح",
                    "REPAIRED_PENDING_APPROVAL" to "بانتظار الاعتماد",
                    "RESTORED_TO_STOCK" to "أعيد للمخزون"
                )

                items(filters) { (key, label) ->
                    val isSelected = (statusFilter == key) || (statusFilter == null && key == "ALL")
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(if (key == "ALL") "ALL" else key) },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

            // Records List
            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة لبحثك" else "لا توجد سجلات هالك أو إصلاح مسجلة حالياً",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        DamageRecordCard(
                            record = record,
                            isAdmin = isAdmin,
                            currencyFormat = currencyFormat,
                            dateFormat = dateFormat,
                            onSendToRepairClick = { recordToSendToRepair = record },
                            onCompleteRepairClick = { recordToCompleteRepair = record },
                            onApproveReturnStockClick = { recordToApproveStock = record }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Register Damage / Scrap
    if (showRegisterDialog) {
        RegisterDamageDialog(
            catalogModels = catalogModels,
            returnsWithItems = returnsWithItems,
            onDismiss = { showRegisterDialog = false },
            onSubmit = { modelId, qty, reason, details, status, srcType, retId, retNum, cost, loss, img, notes ->
                onRegisterDamageOrScrap(
                    modelId, qty, reason, details, status, srcType, retId, retNum, cost, loss, img, notes,
                    {
                        showRegisterDialog = false
                    },
                    { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Dialog: Send to Repair
    recordToSendToRepair?.let { record ->
        SendToRepairDialog(
            record = record,
            onDismiss = { recordToSendToRepair = null },
            onSubmit = { reason, notes ->
                onSendToRepair(
                    record.id,
                    reason,
                    notes,
                    {
                        recordToSendToRepair = null
                    },
                    { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Dialog: Complete Repair
    recordToCompleteRepair?.let { record ->
        CompleteRepairDialog(
            record = record,
            onDismiss = { recordToCompleteRepair = null },
            onSubmit = { outcome, spareParts, labor, notes ->
                onCompleteRepair(
                    record.id,
                    outcome,
                    spareParts,
                    labor,
                    notes,
                    {
                        recordToCompleteRepair = null
                    },
                    { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Dialog: Approve Return to Stock
    recordToApproveStock?.let { record ->
        ApproveReturnStockDialog(
            record = record,
            onDismiss = { recordToApproveStock = null },
            onConfirm = {
                onApproveReturnToStock(
                    record.id,
                    {
                        recordToApproveStock = null
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
private fun MetricCard(
    title: String,
    count: Int,
    subText: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.5f), NavySurfaceElevated)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count",
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(subText, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun FinancialMetricCard(
    title: String,
    amount: String,
    subText: String,
    color: Color
) {
    Card(
        modifier = Modifier.width(170.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.5f), NavySurfaceElevated)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = amount,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(subText, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun DamageRecordCard(
    record: DamageRepairRecordEntity,
    isAdmin: Boolean,
    currencyFormat: (Double) -> String,
    dateFormat: SimpleDateFormat,
    onSendToRepairClick: () -> Unit,
    onCompleteRepairClick: () -> Unit,
    onApproveReturnStockClick: () -> Unit
) {
    val statusInfo = when (record.status) {
        "TOTAL_SCRAP" -> Triple("تالف نهائيًا", Color(0xFFEF5350), Color(0x33EF5350))
        "UNDER_INSPECTION" -> Triple("تحت الفحص", Color(0xFFFFA726), Color(0x33FFA726))
        "REPAIRABLE" -> Triple("قابل للإصلاح", Color(0xFFFFCA28), Color(0x33FFCA28))
        "UNDER_REPAIR" -> Triple("تحت الإصلاح", Color(0xFF29B6F6), Color(0x3329B6F6))
        "REPAIRED_PENDING_APPROVAL" -> Triple("تم إصلاحه (بانتظار الاعتماد)", Color(0xFFAB47BC), Color(0x33AB47BC))
        "RESTORED_TO_STOCK" -> Triple("أعيد للمخزون الصالح", Color(0xFF66BB6A), Color(0x3366BB6A))
        else -> Triple(record.status, TextWhite, NavySurfaceElevated)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("damage_record_card_${record.id}"),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(statusInfo.second.copy(alpha = 0.4f), NavySurfaceElevated)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Record Number, Source, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "#${record.recordNumber}",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    val sourceBadge = if (record.sourceType == "CUSTOMER_RETURN") "مرتجع #${record.returnNumber}" else "من المخزن"
                    Surface(
                        color = NavySurfaceElevated,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = sourceBadge,
                            color = TextMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    color = statusInfo.third,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(statusInfo.second, statusInfo.second.copy(alpha = 0.4f))))
                ) {
                    Text(
                        text = statusInfo.first,
                        color = statusInfo.second,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
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
                        text = "${record.productName} - ${record.modelName}",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${record.categoryName} • ${record.brandName}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    color = NavySurfaceElevated,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("الكمية:", color = TextMuted, fontSize = 11.sp)
                        Text("${record.quantity} قطع", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = NavySurfaceElevated)
            Spacer(modifier = Modifier.height(10.dp))

            // Damage reason and financial impact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("السبب:", color = TextMuted, fontSize = 11.sp)
                        Text(record.reason, color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    if (record.damageDetails.isNotBlank()) {
                        Text(
                            text = record.damageDetails,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("الخسارة المقدرة:", color = TextMuted, fontSize = 11.sp)
                        Text(currencyFormat(record.totalLossAmount), color = Color(0xFFEF5350), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    if (record.totalRepairCost > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("تكاليف الإصلاح:", color = TextMuted, fontSize = 11.sp)
                            Text(currencyFormat(record.totalRepairCost), color = Color(0xFF29B6F6), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Repair outcome / notes if available
            if (record.repairNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = NavySurfaceElevated.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ملاحظات الإصلاح: ${record.repairNotes}",
                        color = TextWhite,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Operator and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Text(
                        text = "المسؤول: ${record.registeredByName}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = dateFormat.format(Date(record.registeredTimestamp)),
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            // Action Buttons for Admin based on state
            if (isAdmin) {
                when (record.status) {
                    "UNDER_INSPECTION", "REPAIRABLE" -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onSendToRepairClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1), contentColor = TextWhite),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_send_to_repair_${record.id}")
                        ) {
                            Icon(Icons.Default.Handyman, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إرسال إلى مركز الإصلاح والصيانة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "UNDER_REPAIR" -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onCompleteRepairClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2), contentColor = TextWhite),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_complete_repair_${record.id}")
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل نتيجة الإصلاح والتكاليف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "REPAIRED_PENDING_APPROVAL" -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onApproveReturnStockClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = TextWhite),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_approve_stock_${record.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اعتماد عودة المنتج للمخزون المتاح للبيع", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Dialog to Register Damage / Scrap
@Composable
private fun RegisterDamageDialog(
    catalogModels: List<ProductModelEntity>,
    returnsWithItems: List<ReturnWithItems>,
    onDismiss: () -> Unit,
    onSubmit: (
        modelId: Long,
        quantity: Int,
        reason: String,
        damageDetails: String,
        initialStatus: String,
        sourceType: String,
        returnRecordId: Long?,
        returnNumber: String,
        unitCost: Double?,
        totalLossAmount: Double?,
        imageUri: String,
        notes: String
    ) -> Unit
) {
    var sourceMode by remember { mutableStateOf("INVENTORY") } // "INVENTORY" or "RETURN"
    var selectedModel by remember { mutableStateOf<ProductModelEntity?>(null) }
    var quantityText by remember { mutableStateOf("1") }
    var selectedReason by remember { mutableStateOf("كسر") }
    var damageDetails by remember { mutableStateOf("") }
    var initialStatus by remember { mutableStateOf("UNDER_INSPECTION") } // "TOTAL_SCRAP", "UNDER_INSPECTION", "REPAIRABLE"
    var unitCostText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Selected Return if coming from customer returns
    var selectedReturnItem by remember {
        mutableStateOf<Triple<ReturnWithItems, com.example.data.local.ReturnItemEntity, ProductModelEntity?>?>(null)
    }

    // Filter available return items with damage
    val damagedReturnItems = remember(returnsWithItems, catalogModels) {
        val list = mutableListOf<Triple<ReturnWithItems, com.example.data.local.ReturnItemEntity, ProductModelEntity?>>()
        returnsWithItems.filter { it.returnRecord.status == "APPROVED" }.forEach { ret ->
            ret.items.filter { it.stockCondition != "RESELLABLE" }.forEach { item ->
                val model = catalogModels.firstOrNull { it.id == item.modelId }
                list.add(Triple(ret, item, model))
            }
        }
        list
    }

    val reasons = listOf("كسر", "حرق كهربائي", "عيب تصنيع", "فقد", "تلف أثناء النقل", "أخرى")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp),
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
                    text = "تسجيل هالك / تالف جديد",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "تحديد المنتج وسبب التلف واستبعاده من المخزون الصالح للبيع",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Source Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = sourceMode == "INVENTORY",
                        onClick = {
                            sourceMode = "INVENTORY"
                            selectedReturnItem = null
                        },
                        label = { Text("من المخزون الحالي") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = TextDark
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = sourceMode == "RETURN",
                        onClick = { sourceMode = "RETURN" },
                        label = { Text("من المرتجعات التالفة (${damagedReturnItems.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = TextDark
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (sourceMode == "INVENTORY") {
                    // Dropdown / Picker for inventory models
                    Text("اختر المنتج / الموديل من المخزن:", color = TextWhite, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    var modelMenuExpanded by remember { mutableStateOf(false) }

                    Box {
                        OutlinedButton(
                            onClick = { modelMenuExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_select_model_damage"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                        ) {
                            Text(
                                text = selectedModel?.let { "${it.productName} - ${it.modelName} (رصيد: ${it.stockQuantity})" }
                                    ?: "اضغط لاختيار المنتج من المخزن",
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
                            catalogModels.forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${m.productName} - ${m.modelName}", color = TextWhite, fontWeight = FontWeight.Bold)
                                            Text("الرصيد: ${m.stockQuantity} • الكود: ${m.code}", color = TextMuted, fontSize = 11.sp)
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
                } else {
                    // Pick from damaged returns
                    Text("اختر الصنف المرتجع المتضرر:", color = TextWhite, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    var returnMenuExpanded by remember { mutableStateOf(false) }

                    if (damagedReturnItems.isEmpty()) {
                        Text("لا توجد مرتجعات بها أصناف تالفة حالياً", color = TextMuted, fontSize = 12.sp)
                    } else {
                        Box {
                            OutlinedButton(
                                onClick = { returnMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                            ) {
                                Text(
                                    text = selectedReturnItem?.let { "${it.first.returnRecord.returnNumber} • ${it.second.productName} (${it.second.quantity} قطع)" }
                                        ?: "اضغط لاختيار الصنف المرتجع",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = returnMenuExpanded,
                                onDismissRequest = { returnMenuExpanded = false },
                                modifier = Modifier.background(NavySurface)
                            ) {
                                damagedReturnItems.forEach { itemTriple ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("مرتجع #${itemTriple.first.returnRecord.returnNumber} - ${itemTriple.second.productName}", color = TextWhite, fontWeight = FontWeight.Bold)
                                                Text("الكمية: ${itemTriple.second.quantity} • الحالة: ${itemTriple.second.stockCondition} • السبب: ${itemTriple.second.itemNotes}", color = TextMuted, fontSize = 11.sp)
                                            }
                                        },
                                        onClick = {
                                            selectedReturnItem = itemTriple
                                            selectedModel = itemTriple.third ?: catalogModels.firstOrNull { it.id == itemTriple.second.modelId }
                                            quantityText = itemTriple.second.quantity.toString()
                                            damageDetails = itemTriple.second.itemNotes
                                            unitCostText = itemTriple.second.unitPrice.toString()
                                            returnMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity & Unit Cost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("الكمية") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_damage_quantity"),
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
                        label = { Text("تكلفة الوحدة (ج.م)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_damage_cost"),
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

                // Reason Selection
                Text("سبب التلف:", color = TextWhite, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(reasons) { r ->
                        FilterChip(
                            selected = selectedReason == r,
                            onClick = { selectedReason = r },
                            label = { Text(r, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Initial Status
                Text("الحالة الأولية للمنتج:", color = TextWhite, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = initialStatus == "TOTAL_SCRAP",
                        onClick = { initialStatus = "TOTAL_SCRAP" },
                        label = { Text("تالف نهائيًا (هالك)", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF5350),
                            selectedLabelColor = TextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = initialStatus == "UNDER_INSPECTION",
                        onClick = { initialStatus = "UNDER_INSPECTION" },
                        label = { Text("تحت الفحص", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFA726),
                            selectedLabelColor = TextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = initialStatus == "REPAIRABLE",
                        onClick = { initialStatus = "REPAIRABLE" },
                        label = { Text("قابل للإصلاح", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF29B6F6),
                            selectedLabelColor = TextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Details & Notes
                OutlinedTextField(
                    value = damageDetails,
                    onValueChange = { damageDetails = it },
                    label = { Text("تفاصيل ومواصفات التلف") },
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

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("إلغاء", color = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val model = selectedModel
                            if (model == null) {
                                return@Button
                            }
                            val qty = quantityText.toIntOrNull() ?: 1
                            val cost = unitCostText.toDoubleOrNull()
                            val loss = if (cost != null) cost * qty else null

                            val retItem = selectedReturnItem
                            val retId = retItem?.first?.returnRecord?.id
                            val retNum = retItem?.first?.returnRecord?.returnNumber ?: ""

                            onSubmit(
                                model.id,
                                qty,
                                selectedReason,
                                damageDetails,
                                initialStatus,
                                sourceMode,
                                retId,
                                retNum,
                                cost,
                                loss,
                                "",
                                notes
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_confirm_damage_submission")
                    ) {
                        Text("اعتماد وتسجيل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Dialog to Send to Repair
@Composable
private fun SendToRepairDialog(
    record: DamageRepairRecordEntity,
    onDismiss: () -> Unit,
    onSubmit: (repairReason: String, technicianNotes: String) -> Unit
) {
    var reason by remember { mutableStateOf(record.reason) }
    var technicianNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Text("تحويل إلى مركز الإصلاح والصيانة", color = TextWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "سيتم تحويل المنتج «${record.productName} - ${record.modelName}» (عدد: ${record.quantity}) إلى قسم الصيانة بحالة «تحت الإصلاح».",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب الإصلاح المطلوب") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavySurfaceElevated
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = technicianNotes,
                    onValueChange = { technicianNotes = it },
                    label = { Text("ملاحظات الفني ومركز الصيانة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavySurfaceElevated
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(reason, technicianNotes) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1), contentColor = TextWhite),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("تأكيد التحويل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("إلغاء", color = TextWhite)
            }
        }
    )
}

// Dialog to Complete Repair
@Composable
private fun CompleteRepairDialog(
    record: DamageRepairRecordEntity,
    onDismiss: () -> Unit,
    onSubmit: (outcome: String, sparePartsCost: Double, laborCost: Double, notes: String) -> Unit
) {
    var outcome by remember { mutableStateOf("FIXED") } // "FIXED", "CANNOT_BE_FIXED", "PARTIALLY_FIXED"
    var sparePartsText by remember { mutableStateOf("0") }
    var laborText by remember { mutableStateOf("0") }
    var repairNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Text("تسجيل نتيجة الصيانة والتكاليف", color = TextWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "سجل نتيجة الإصلاح للجهاز: ${record.productName} - ${record.modelName}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Outcome Chips
                Text("نتيجة الإصلاح:", color = TextWhite, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = outcome == "FIXED",
                        onClick = { outcome = "FIXED" },
                        label = { Text("تم الإصلاح بنجاح", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CAF50), selectedLabelColor = TextWhite)
                    )
                    FilterChip(
                        selected = outcome == "CANNOT_BE_FIXED",
                        onClick = { outcome = "CANNOT_BE_FIXED" },
                        label = { Text("تعذر الإصلاح (هالك)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFEF5350), selectedLabelColor = TextWhite)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sparePartsText,
                        onValueChange = { sparePartsText = it },
                        label = { Text("قطع الغيار (ج.م)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = NavySurfaceElevated
                        )
                    )

                    OutlinedTextField(
                        value = laborText,
                        onValueChange = { laborText = it },
                        label = { Text("المصنعية (ج.م)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = NavySurfaceElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = repairNotes,
                    onValueChange = { repairNotes = it },
                    label = { Text("ملاحظات ورشة الصيانة والقطع المستبدلة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavySurfaceElevated
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parts = sparePartsText.toDoubleOrNull() ?: 0.0
                    val labor = laborText.toDoubleOrNull() ?: 0.0
                    onSubmit(outcome, parts, labor, repairNotes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2), contentColor = TextWhite),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("حفظ النتيجة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("إلغاء", color = TextWhite)
            }
        }
    )
}

// Dialog to Approve Return to Stock
@Composable
private fun ApproveReturnStockDialog(
    record: DamageRepairRecordEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Text("اعتماد عودة المنتج للمخزون", color = TextWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "هل تؤكد صلاحية واكتمال إصلاح المنتج «${record.productName} - ${record.modelName}» وإعادته إلى المخزون المتاح للبيع؟",
                    color = TextWhite,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = NavySurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("• الكمية التي ستضاف للمخزن: ${record.quantity} قطع", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("• تكاليف الإصلاح المسجلة: ${record.totalRepairCost} ج.م", color = TextMuted, fontSize = 11.sp)
                        Text("• سيتم تحديث حالة السجل إلى «أعيد للمخزون الصالح» ولا يمكن التكرار.", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = TextWhite),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("تأكيد واعتماد الإضافة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("إلغاء", color = TextWhite)
            }
        }
    )
}
