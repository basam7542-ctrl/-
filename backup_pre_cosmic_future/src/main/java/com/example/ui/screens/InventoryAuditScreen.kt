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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.CategoryEntity
import com.example.data.local.InventoryAuditEntity
import com.example.data.local.InventoryAuditItemEntity
import com.example.data.local.InventoryAuditWithItems
import com.example.data.local.ProductModelEntity
import com.example.data.local.StockMovementEntity
import com.example.data.local.UserAccountEntity
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldPrimary
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

@Composable
fun InventoryAuditScreen(
    currentUser: UserAccountEntity,
    audits: List<InventoryAuditWithItems>,
    categories: List<CategoryEntity>,
    models: List<ProductModelEntity>,
    onCreateAudit: (
        title: String,
        scope: String,
        targetName: String?,
        notes: String,
        onSuccess: (InventoryAuditWithItems) -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    onUpdateActualQuantity: (itemId: Long, actualQuantity: Int) -> Unit,
    onSettleAudit: (
        auditId: Long,
        reason: String,
        onSuccess: (InventoryAuditWithItems) -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    onCancelAudit: (
        auditId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    onExportAuditCsv: (InventoryAuditWithItems) -> Unit
) {
    val context = LocalContext.current
    val isAdmin = currentUser.role == "ADMIN"
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedAuditForDetails by remember { mutableStateOf<InventoryAuditWithItems?>(null) }
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar", "EG")) }

    // Filter audits
    val filteredAudits = audits.filter { auditWithItems ->
        val audit = auditWithItems.audit
        val matchesStatus = statusFilter == null || audit.status == statusFilter
        val matchesSearch = searchQuery.isBlank() ||
                audit.auditNumber.contains(searchQuery, ignoreCase = true) ||
                audit.title.contains(searchQuery, ignoreCase = true) ||
                audit.createdByName.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    val totalAuditsCount = audits.size
    val draftAuditsCount = audits.count { it.audit.status == "DRAFT" }
    val settledAuditsCount = audits.count { it.audit.status == "SETTLED" }

    // Keep selected audit updated if items change
    val activeSelectedAudit = selectedAuditForDetails?.let { selected ->
        audits.find { it.audit.id == selected.audit.id } ?: selected
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Header Action & Metrics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorderGlow),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.FactCheck,
                                    contentDescription = "الجرد الفعلي",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "إدارة الجرد والتسويات الفعلية",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "مطابقة رصيد النظام مع الوجود الفعلي وحصر العجز والزيادة",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        if (isAdmin) {
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = TextDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_new_inventory_audit")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("بدء جرد جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary KPI chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuditMiniKpi(
                            title = "إجمالي الجرد",
                            value = "$totalAuditsCount",
                            color = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                        AuditMiniKpi(
                            title = "مسودات قيد العد",
                            value = "$draftAuditsCount",
                            color = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        AuditMiniKpi(
                            title = "جلسات معتمدة ومسواة",
                            value = "$settledAuditsCount",
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Search and Status Filters
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("input_search_audits"),
                placeholder = { Text("بحث برقم الجرد أو العنوان أو اسم المسؤول...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = NavySurfaceVariant,
                    focusedContainerColor = NavySurface,
                    unfocusedContainerColor = NavySurface,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            // Status Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { statusFilter = null },
                        label = { Text("جميع الحالات ($totalAuditsCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = TextDark
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == "DRAFT",
                        onClick = { statusFilter = if (statusFilter == "DRAFT") null else "DRAFT" },
                        label = { Text("قيد العد (مسودة) ($draftAuditsCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarningAmber,
                            selectedLabelColor = TextDark
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == "SETTLED",
                        onClick = { statusFilter = if (statusFilter == "SETTLED") null else "SETTLED" },
                        label = { Text("تمت التسوية والاعتماد ($settledAuditsCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SuccessGreen,
                            selectedLabelColor = TextDark
                        )
                    )
                }
                item {
                    val cancelledCount = audits.count { it.audit.status == "CANCELLED" }
                    FilterChip(
                        selected = statusFilter == "CANCELLED",
                        onClick = { statusFilter = if (statusFilter == "CANCELLED") null else "CANCELLED" },
                        label = { Text("ملغاة ($cancelledCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ErrorRed,
                            selectedLabelColor = TextWhite
                        )
                    )
                }
            }

            // List of Audits
            if (filteredAudits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || statusFilter != null)
                                "لا توجد نتائج تطابق معايير البحث أو التصفية"
                            else
                                "لم يتم تسجيل أي عمليات جرد حتى الآن",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredAudits, key = { it.audit.id }) { auditWithItems ->
                        AuditListItemCard(
                            auditWithItems = auditWithItems,
                            numberFormat = numberFormat,
                            dateFormat = dateFormat,
                            onClick = { selectedAuditForDetails = auditWithItems }
                        )
                    }
                }
            }
        }
    }

    // Create Audit Dialog
    if (showCreateDialog) {
        CreateInventoryAuditDialog(
            currentUser = currentUser,
            categories = categories,
            models = models,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, scope, targetName, notes ->
                onCreateAudit(
                    title,
                    scope,
                    targetName,
                    notes,
                    { newAudit ->
                        showCreateDialog = false
                        Toast.makeText(context, "تم إنشاء عملية الجرد بنجاح: ${newAudit.audit.auditNumber}", Toast.LENGTH_LONG).show()
                    },
                    { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Audit Details & Counting Dialog
    if (activeSelectedAudit != null) {
        AuditDetailDialog(
            auditWithItems = activeSelectedAudit,
            currentUser = currentUser,
            numberFormat = numberFormat,
            dateFormat = dateFormat,
            onDismiss = { selectedAuditForDetails = null },
            onUpdateActualQuantity = onUpdateActualQuantity,
            onSettleAudit = { reason ->
                onSettleAudit(
                    activeSelectedAudit.audit.id,
                    reason,
                    { settledAudit ->
                        Toast.makeText(context, "تم اعتماد وتسوية فروق الجرد بنجاح", Toast.LENGTH_LONG).show()
                    },
                    { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onCancelAudit = {
                onCancelAudit(
                    activeSelectedAudit.audit.id,
                    {
                        Toast.makeText(context, "تم إلغاء عملية الجرد", Toast.LENGTH_SHORT).show()
                        selectedAuditForDetails = null
                    },
                    { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onExportCsv = { onExportAuditCsv(activeSelectedAudit) }
        )
    }
}

@Composable
private fun AuditMiniKpi(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun AuditListItemCard(
    auditWithItems: InventoryAuditWithItems,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val audit = auditWithItems.audit
    val items = auditWithItems.items

    val statusBg = when (audit.status) {
        "DRAFT" -> WarningAmber.copy(alpha = 0.2f)
        "SETTLED" -> SuccessGreen.copy(alpha = 0.2f)
        "CANCELLED" -> ErrorRed.copy(alpha = 0.2f)
        else -> ElectricBlue.copy(alpha = 0.2f)
    }

    val statusText = when (audit.status) {
        "DRAFT" -> "قيد العد (مسودة)"
        "SETTLED" -> "تمت التسوية والاعتماد"
        "CANCELLED" -> "ملغي"
        else -> audit.status
    }

    val statusTextColor = when (audit.status) {
        "DRAFT" -> WarningAmber
        "SETTLED" -> SuccessGreen
        "CANCELLED" -> ErrorRed
        else -> ElectricBlue
    }

    val scopeArabic = when (audit.scopeType) {
        "CATEGORY" -> "فئة: ${audit.scopeFilter}"
        "BRAND" -> "ماركة: ${audit.scopeFilter}"
        else -> "جرد شامل لجميع المنتجات"
    }

    val deficitsCount = items.count { it.differenceQuantity < 0 }
    val surplusCount = items.count { it.differenceQuantity > 0 }
    val matchCount = items.count { it.differenceQuantity == 0 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("audit_item_${audit.auditNumber}"),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavySurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = audit.auditNumber,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $scopeArabic",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = statusText, color = statusTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = audit.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audit counts bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الأصناف: ${items.size}",
                    fontSize = 11.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "مطابق: $matchCount",
                    fontSize = 11.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "عجز: $deficitsCount",
                    fontSize = 11.sp,
                    color = ErrorRed,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "زيادة: $surplusCount",
                    fontSize = 11.sp,
                    color = ElectricBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "بواسطة: ${audit.createdByName} • ${dateFormat.format(Date(audit.startTimestamp))}",
                    fontSize = 10.sp,
                    color = TextMuted
                )

                if (audit.isSettled) {
                    Text(
                        text = "تمت التسوية",
                        fontSize = 10.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "اضغط للتفاصيل والعد ◄",
                        fontSize = 10.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateInventoryAuditDialog(
    currentUser: UserAccountEntity,
    categories: List<CategoryEntity>,
    models: List<ProductModelEntity>,
    onDismiss: () -> Unit,
    onCreate: (title: String, scope: String, targetName: String?, notes: String) -> Unit
) {
    var title by remember {
        val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        mutableStateOf("جرد دوري - $dateStr")
    }
    var scope by remember { mutableStateOf("ALL") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var brandMenuExpanded by remember { mutableStateOf(false) }

    val distinctBrands = remember(models) {
        models.map { it.brandName }.distinct().sorted()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp)),
            color = NavySurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorderGlow)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("بدء جلسة جرد مخزني جديدة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان أو وصف عملية الجرد *", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavySurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scope Selector
                Text("نطاق الجرد:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = scope == "ALL",
                        onClick = { scope = "ALL" },
                        label = { Text("شامل لكل المخزون", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = TextDark
                        )
                    )
                    FilterChip(
                        selected = scope == "CATEGORY",
                        onClick = {
                            scope = "CATEGORY"
                            if (selectedCategory == null && categories.isNotEmpty()) {
                                selectedCategory = categories.first().name
                            }
                        },
                        label = { Text("حسب الفئة", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = TextDark
                        )
                    )
                    FilterChip(
                        selected = scope == "BRAND",
                        onClick = {
                            scope = "BRAND"
                            if (selectedBrand == null && distinctBrands.isNotEmpty()) {
                                selectedBrand = distinctBrands.first()
                            }
                        },
                        label = { Text("حسب الماركة", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = TextDark
                        )
                    )
                }

                // Scope sub-selector
                if (scope == "CATEGORY") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategory ?: "اختر الفئة...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الفئة المستهدفة للجرد", color = TextMuted, fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(onClick = { categoryMenuExpanded = true }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = GoldPrimary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryMenuExpanded = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = NavySurfaceVariant,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategory = cat.name
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else if (scope == "BRAND") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedBrand ?: "اختر الماركة...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الماركة المستهدفة للجرد", color = TextMuted, fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(onClick = { brandMenuExpanded = true }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = GoldPrimary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { brandMenuExpanded = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = NavySurfaceVariant,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = brandMenuExpanded,
                            onDismissRequest = { brandMenuExpanded = false }
                        ) {
                            distinctBrands.forEach { brand ->
                                DropdownMenuItem(
                                    text = { Text(brand) },
                                    onClick = {
                                        selectedBrand = brand
                                        brandMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية أو سبب الجرد (اختياري)", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavySurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إلغاء", color = TextMuted)
                    }

                    Button(
                        onClick = {
                            val target = when (scope) {
                                "CATEGORY" -> selectedCategory
                                "BRAND" -> selectedBrand
                                else -> null
                            }
                            onCreate(title.trim(), scope, target, notes.trim())
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                        shape = RoundedCornerShape(10.dp),
                        enabled = title.isNotBlank()
                    ) {
                        Text("بدء الجرد الآن", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditDetailDialog(
    auditWithItems: InventoryAuditWithItems,
    currentUser: UserAccountEntity,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onDismiss: () -> Unit,
    onUpdateActualQuantity: (itemId: Long, actualQuantity: Int) -> Unit,
    onSettleAudit: (reason: String) -> Unit,
    onCancelAudit: () -> Unit,
    onExportCsv: () -> Unit
) {
    val audit = auditWithItems.audit
    val items = auditWithItems.items
    val isAdmin = currentUser.role == "ADMIN"
    val isDraft = audit.status == "DRAFT"

    var itemSearchQuery by remember { mutableStateOf("") }
    var itemFilterType by remember { mutableStateOf<String?>(null) } // null = all, "DEFICIT", "SURPLUS", "MATCH"
    var showSettleConfirmDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var settlementReason by remember { mutableStateOf("تسوية فروق الجرد الفعلي واعتماد العجز والزيادة رسمياً") }

    // Filter items
    val filteredItems = items.filter { item ->
        val matchesFilter = when (itemFilterType) {
            "DEFICIT" -> item.differenceQuantity < 0
            "SURPLUS" -> item.differenceQuantity > 0
            "MATCH" -> item.differenceQuantity == 0
            else -> true
        }
        val matchesSearch = itemSearchQuery.isBlank() ||
                item.productName.contains(itemSearchQuery, ignoreCase = true) ||
                item.modelName.contains(itemSearchQuery, ignoreCase = true) ||
                item.code.contains(itemSearchQuery, ignoreCase = true) ||
                item.brandName.contains(itemSearchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    val matchedCount = items.count { it.differenceQuantity == 0 }
    val deficitCount = items.count { it.differenceQuantity < 0 }
    val surplusCount = items.count { it.differenceQuantity > 0 }
    val totalSystemQty = items.sumOf { it.systemQuantity }
    val totalActualQty = items.sumOf { it.actualQuantity }
    val netQtyDiff = totalActualQty - totalSystemQty
    val totalVarianceValue = items.sumOf { it.varianceValue }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = CharcoalBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorderGlow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = audit.auditNumber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Text(
                            text = audit.title,
                            fontSize = 12.sp,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row {
                        IconButton(onClick = onExportCsv) {
                            Icon(Icons.Default.Download, contentDescription = "تصدير CSV", tint = ElectricBlue)
                        }
                    }
                }

                HorizontalDivider(
                    color = NavySurfaceVariant,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // KPI Dashboard Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavySurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "حالة الجلسة: ${if (audit.isSettled) "معتمدة ومسواة" else if (isDraft) "قيد العد الفعلي" else audit.status}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (audit.isSettled) SuccessGreen else WarningAmber
                            )
                            Text(
                                text = "بواسطة: ${audit.createdByName}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        if (audit.isSettled && audit.approvedByName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "اعتماد وتسوية: ${audit.approvedByName} • ${audit.settledTimestamp?.let { dateFormat.format(Date(it)) } ?: ""}",
                                fontSize = 11.sp,
                                color = SuccessGreen
                            )
                            if (audit.notes.isNotBlank()) {
                                Text(
                                    text = "سبب التسوية: ${audit.notes}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Counts row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CountPill(label = "الأصناف", count = "${items.size}", color = TextWhite, modifier = Modifier.weight(1f))
                            CountPill(label = "مطابق", count = "$matchedCount", color = SuccessGreen, modifier = Modifier.weight(1f))
                            CountPill(label = "عجز (-)", count = "$deficitCount", color = ErrorRed, modifier = Modifier.weight(1f))
                            CountPill(label = "زيادة (+)", count = "$surplusCount", color = ElectricBlue, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavySurface)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "النظام: $totalSystemQty | الفعلي: $totalActualQty (الفرق: ${if (netQtyDiff > 0) "+$netQtyDiff" else "$netQtyDiff"})",
                                fontSize = 11.sp,
                                color = TextWhite,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "فارق التكلفة: ${numberFormat.format(totalVarianceValue)} ج.م",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalVarianceValue < 0) ErrorRed else if (totalVarianceValue > 0) ElectricBlue else SuccessGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search & Filters within Audit Items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = itemSearchQuery,
                        onValueChange = { itemSearchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("بحث في بنود الجرد...", fontSize = 11.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = NavySurfaceVariant,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Quick Match All Remaining (if DRAFT)
                    if (isDraft && isAdmin) {
                        Button(
                            onClick = {
                                // Match all uncounted/all items where actual is system
                                items.forEach { itm ->
                                    if (itm.actualQuantity != itm.systemQuantity) {
                                        onUpdateActualQuantity(itm.id, itm.systemQuantity)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceElevated, contentColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مطابقة الكل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Filter chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = itemFilterType == null,
                            onClick = { itemFilterType = null },
                            label = { Text("الكل (${items.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = itemFilterType == "DEFICIT",
                            onClick = { itemFilterType = if (itemFilterType == "DEFICIT") null else "DEFICIT" },
                            label = { Text("عجز فقط ($deficitCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ErrorRed,
                                selectedLabelColor = TextWhite
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = itemFilterType == "SURPLUS",
                            onClick = { itemFilterType = if (itemFilterType == "SURPLUS") null else "SURPLUS" },
                            label = { Text("زيادة فقط ($surplusCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = itemFilterType == "MATCH",
                            onClick = { itemFilterType = if (itemFilterType == "MATCH") null else "MATCH" },
                            label = { Text("مطابق ($matchedCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SuccessGreen,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                }

                // Items List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        AuditItemRowCard(
                            item = item,
                            isDraft = isDraft,
                            numberFormat = numberFormat,
                            onUpdateActualQuantity = { newQty ->
                                onUpdateActualQuantity(item.id, newQty)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Bar (Settle / Cancel / Close)
                if (isDraft && isAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCancelConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إلغاء الجرد", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showSettleConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = TextDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(2f)
                                .testTag("btn_settle_audit")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اعتماد وتسوية فروق الجرد نهائيًا", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إغلاق نافذة الجرد", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Settlement confirmation dialog
    if (showSettleConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSettleConfirmDialog = false },
            title = {
                Text(
                    text = "تأكيد اعتماد وتسوية فروق الجرد",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "عند اعتماد التسوية، سيتم تحديث أرصدة المخزون الفعلية لجميع الأصناف المطابقة والمختلفة، وتسجيل حركات مخزون رسمية (عجز جرد أو زيادة جرد) لكل بند لا يمكن التراجع عنه.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "عدد بنود العجز: $deficitCount | عدد بنود الزيادة: $surplusCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        text = "صافي الفارق المالي: ${numberFormat.format(totalVarianceValue)} ج.م",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalVarianceValue < 0) ErrorRed else SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = settlementReason,
                        onValueChange = { settlementReason = it },
                        label = { Text("سبب وملاحظات التسوية *", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = NavySurfaceVariant,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSettleConfirmDialog = false
                        onSettleAudit(settlementReason.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = TextDark),
                    shape = RoundedCornerShape(8.dp),
                    enabled = settlementReason.isNotBlank()
                ) {
                    Text("تأكيد الاعتماد والتسوية", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSettleConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("رجوع", color = TextMuted)
                }
            },
            containerColor = NavySurfaceElevated
        )
    }

    // Cancel confirmation dialog
    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = {
                Text(
                    text = "إلغاء عملية الجرد",
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في إلغاء عملية الجرد هذه؟ لن يتم تعديل أرصدة المخزون وسيتم وضع الجرد في حالة (ملغي).",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmDialog = false
                        onCancelAudit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = TextWhite),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("تأكيد الإلغاء", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("رجوع", color = TextMuted)
                }
            },
            containerColor = NavySurfaceElevated
        )
    }
}

@Composable
private fun CountPill(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NavySurface)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 10.sp, color = TextMuted)
            Text(text = count, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun AuditItemRowCard(
    item: InventoryAuditItemEntity,
    isDraft: Boolean,
    numberFormat: NumberFormat,
    onUpdateActualQuantity: (Int) -> Unit
) {
    val diff = item.differenceQuantity
    val statusColor = when {
        diff == 0 -> SuccessGreen
        diff < 0 -> ErrorRed
        else -> ElectricBlue
    }

    val statusText = when {
        diff == 0 -> "مطابق"
        diff < 0 -> "عجز: $diff"
        else -> "زيادة: +$diff"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavySurfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${item.productName} - ${item.modelName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "كود: ${item.code} • ${item.categoryName} • ${item.brandName}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // System vs Actual
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "النظام: ", fontSize = 11.sp, color = TextMuted)
                    Text(text = "${item.systemQuantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(text = "الفعلي: ", fontSize = 11.sp, color = TextMuted)

                    if (isDraft) {
                        // Editable control
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (item.actualQuantity > 0) {
                                        onUpdateActualQuantity(item.actualQuantity - 1)
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "إنقاص", tint = ErrorRed, modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = "${item.actualQuantity}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            IconButton(
                                onClick = {
                                    onUpdateActualQuantity(item.actualQuantity + 1)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "زيادة", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            }

                            // Match button
                            if (item.actualQuantity != item.systemQuantity) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SuccessGreen.copy(alpha = 0.2f))
                                        .clickable { onUpdateActualQuantity(item.systemQuantity) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("مطابقة", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Text(text = "${item.actualQuantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                    }
                }

                // Financial variance
                Text(
                    text = "${numberFormat.format(item.varianceValue)} ج.م",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

/**
 * Inventory Reports View
 * Offers:
 * 1. Current stock valuation (total units, total cost valuation, sales potential)
 * 2. Low stock alert list (<= 2 units)
 * 3. Stagnant / Slow-moving products (products with 0 movements in 30 days)
 * 4. Export CSV actions
 */
@Composable
fun InventoryReportsView(
    currentUser: UserAccountEntity,
    models: List<ProductModelEntity>,
    categories: List<CategoryEntity>,
    movements: List<StockMovementEntity>,
    onExportValuationCsv: () -> Unit,
    onExportMovementsCsv: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }

    val totalModels = models.size
    val totalUnits = models.sumOf { it.stockQuantity }
    val totalCostValue = models.sumOf { (it.stockQuantity.toDouble() * it.minAllowedPrice) }
    val totalSalesValue = models.sumOf { (it.stockQuantity.toDouble() * it.finalPrice) }
    val potentialProfit = totalSalesValue - totalCostValue

    val lowStockList = remember(models) {
        models.filter { it.stockQuantity <= 2 }.sortedBy { it.stockQuantity }
    }

    // Stagnant items: models with 0 movements in the past 30 days
    val thirtyDaysAgo = remember { System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 }
    val stagnantList = remember(models, movements) {
        val modelsWithRecentMovements = movements
            .filter { it.timestamp >= thirtyDaysAgo }
            .map { it.modelId }
            .toSet()
        models.filter { it.stockQuantity > 0 && !modelsWithRecentMovements.contains(it.id) }
            .sortedByDescending { it.stockQuantity }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Valuation Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorderGlow),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("تقرير تقييم المخزون الحالي", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("حساب القيمة المالية الإجمالية لرصيد المعرض", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Button(
                        onClick = onExportValuationCsv,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصدير CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuditMiniKpi(title = "الأصناف", value = "$totalModels", color = TextWhite, modifier = Modifier.weight(1f))
                    AuditMiniKpi(title = "إجمالي القطع", value = "$totalUnits", color = ElectricBlue, modifier = Modifier.weight(1f))
                    AuditMiniKpi(title = "نواقص المخزون", value = "${lowStockList.size}", color = ErrorRed, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Financial values
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("قيمة المخزون بسعر التكلفة:", fontSize = 12.sp, color = TextMuted)
                            Text("${numberFormat.format(totalCostValue)} ج.م", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("القيمة البيعية المتوقعة:", fontSize = 12.sp, color = TextMuted)
                            Text("${numberFormat.format(totalSalesValue)} ج.م", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("هامش الربح الإجمالي المتوقع:", fontSize = 12.sp, color = TextMuted)
                            Text("${numberFormat.format(potentialProfit)} ج.م", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Low stock alerts section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تنبيهات نواقص المخزون (${lowStockList.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                    Text("الرصيد ≤ 2 قطعة", fontSize = 11.sp, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (lowStockList.isEmpty()) {
                    Text("المخزون مكتمل ولا توجد أصناف تحت حد الخطر", fontSize = 12.sp, color = SuccessGreen)
                } else {
                    lowStockList.take(6).forEach { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${model.productName} (${model.modelName})",
                                fontSize = 12.sp,
                                color = TextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (model.stockQuantity == 0) ErrorRed.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (model.stockQuantity == 0) "نفد تماماً" else "متبقي: ${model.stockQuantity}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (model.stockQuantity == 0) ErrorRed else WarningAmber
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stagnant inventory section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الرواكد والأصناف بطيئة الحركة (${stagnantList.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                    Text("بدون حركة منذ 30 يوماً", fontSize = 11.sp, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (stagnantList.isEmpty()) {
                    Text("لا توجد أصناف راكدة، جميع البضائع متداولة", fontSize = 12.sp, color = SuccessGreen)
                } else {
                    stagnantList.take(6).forEach { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${model.productName} - ${model.modelName}",
                                fontSize = 12.sp,
                                color = TextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "المخزون: ${model.stockQuantity} قطعة",
                                fontSize = 11.sp,
                                color = WarningAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export movements button
        OutlinedButton(
            onClick = onExportMovementsCsv,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue)
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("تصدير سجل حركات المخزون بالكامل (CSV)", color = ElectricBlue, fontWeight = FontWeight.Bold)
        }
    }
}
