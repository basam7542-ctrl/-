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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.CustomerAccountEntity
import com.example.data.local.InvoiceItemEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.MerchantAccountEntity
import com.example.data.local.ProductModelEntity
import com.example.data.local.ReturnItemEntity
import com.example.data.local.ReturnWithItems
import com.example.data.local.SupplierAccountEntity
import com.example.data.local.UserAccountEntity
import com.example.data.repository.ReturnDraftItem
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.NavyDarkBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.utils.StoreProfile
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReturnsScreen(
    currentUser: UserAccountEntity,
    returnsList: List<ReturnWithItems>,
    invoicesList: List<InvoiceWithItems>,
    catalogModels: List<ProductModelEntity>,
    customers: List<CustomerAccountEntity>,
    suppliers: List<SupplierAccountEntity>,
    merchants: List<MerchantAccountEntity>,
    storeProfile: StoreProfile?,
    searchQuery: String,
    typeFilter: String?,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String?) -> Unit,
    onOpenCreateReturn: () -> Unit,
    onPreviewReturn: (ReturnWithItems) -> Unit,
    onPrintReturn: (ReturnWithItems) -> Unit,
    onCancelReturn: (Long) -> Unit,
    onExportCsv: () -> Unit,
    onFetchReturnableInvoiceItems: suspend (Long) -> Result<List<Pair<InvoiceItemEntity, Int>>>,
    onSubmitCreateReturn: (
        type: String,
        partyName: String,
        partyPhone: String,
        partyId: Long?,
        originalInvoiceNumber: String,
        originalInvoiceId: Long?,
        items: List<ReturnDraftItem>,
        reason: String,
        settlementType: String,
        settlementAmount: Double?,
        settlementNotes: String,
        notes: String,
        onSuccess: (ReturnWithItems) -> Unit,
        onError: (String) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val isAdmin = currentUser.role == "ADMIN"
    var showCreateDialog by remember { mutableStateOf(false) }
    var returnToCancel by remember { mutableStateOf<ReturnWithItems?>(null) }

    // Filter list
    val filteredList = remember(returnsList, searchQuery, typeFilter) {
        returnsList.filter { item ->
            val rec = item.returnRecord
            val matchesType = when (typeFilter) {
                null, "ALL" -> true
                "CUSTOMER" -> rec.type == "CUSTOMER_TO_STORE"
                "SUPPLIER" -> rec.type == "STORE_TO_SUPPLIER"
                "MERCHANT" -> rec.type == "MERCHANT_TO_STORE" || rec.type == "STORE_TO_MERCHANT"
                else -> rec.type == typeFilter
            }
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            val matchesSearch = q.isBlank() ||
                rec.returnNumber.lowercase(Locale.getDefault()).contains(q) ||
                rec.partyName.lowercase(Locale.getDefault()).contains(q) ||
                rec.partyPhone.contains(q) ||
                rec.originalInvoiceNumber.lowercase(Locale.getDefault()).contains(q) ||
                item.items.any { it.productName.lowercase(Locale.getDefault()).contains(q) || it.modelName.lowercase(Locale.getDefault()).contains(q) }

            matchesType && matchesSearch
        }
    }

    // Metrics
    val totalReturnsSum = remember(returnsList) {
        returnsList.filter { it.returnRecord.status == "APPROVED" }.sumOf { it.returnRecord.totalAmount }
    }
    val resellableCount = remember(returnsList) {
        returnsList.filter { it.returnRecord.status == "APPROVED" }
            .flatMap { it.items }
            .filter { it.stockCondition == "RESELLABLE" }
            .sumOf { it.quantity }
    }
    val defectiveOrQuarantineCount = remember(returnsList) {
        returnsList.filter { it.returnRecord.status == "APPROVED" }
            .flatMap { it.items }
            .filter { it.stockCondition != "RESELLABLE" }
            .sumOf { it.quantity }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDarkBackground)
            .padding(16.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "قسم إدارة المرتجعات",
                    fontFamily = CairoFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = if (isAdmin) "إدارة مرتجعات العملاء والموردين والتجار" else "عرض سجل المرتجعات والإيصالات",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onExportCsv,
                    modifier = Modifier.testTag("btn_export_returns_csv")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "تصدير التقرير",
                        tint = GoldPrimary
                    )
                }

                if (isAdmin) {
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = TextDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_create_return")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تسجيل مرتجع جديد",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Metrics Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Total Value
            Card(
                modifier = Modifier.weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "إجمالي المرتجعات",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${NumberFormat.getNumberInstance(Locale.US).format(totalReturnsSum)} ج.م",
                        fontFamily = CairoFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }
            }

            // Card 2: Resellable Stock
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "صالح للبيع (بالمخزن)",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$resellableCount قطعة",
                        fontFamily = CairoFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            // Card 3: Defective / Repair
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "تالف / فحص / صيانة",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = ErrorRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$defectiveOrQuarantineCount قطعة",
                        fontFamily = CairoFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = "بحث برقم المرتجع، الطرف، الهاتف، أو رقم الفاتورة...",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_returns_search"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NavySurface,
                unfocusedContainerColor = NavySurface,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = typeFilter == null || typeFilter == "ALL",
                    onClick = { onFilterChange("ALL") },
                    label = { Text("الكل (${returnsList.size})", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = TextDark,
                        containerColor = NavySurface,
                        labelColor = TextMuted
                    )
                )
            }
            item {
                FilterChip(
                    selected = typeFilter == "CUSTOMER",
                    onClick = { onFilterChange("CUSTOMER") },
                    label = { Text("مرتجع عملاء", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = TextDark,
                        containerColor = NavySurface,
                        labelColor = TextMuted
                    )
                )
            }
            item {
                FilterChip(
                    selected = typeFilter == "SUPPLIER",
                    onClick = { onFilterChange("SUPPLIER") },
                    label = { Text("مرتجع موردين", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = TextDark,
                        containerColor = NavySurface,
                        labelColor = TextMuted
                    )
                )
            }
            item {
                FilterChip(
                    selected = typeFilter == "MERCHANT",
                    onClick = { onFilterChange("MERCHANT") },
                    label = { Text("مرتجع تجار", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = TextDark,
                        containerColor = NavySurface,
                        labelColor = TextMuted
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Returns List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.AssignmentReturn,
                        contentDescription = null,
                        tint = TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "لا توجد نتائج مطابقة لبحثك" else "لا توجد عمليات مرتجعات مسجلة حتى الآن",
                        fontFamily = CairoFontFamily,
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("returns_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.returnRecord.id }) { returnWithItems ->
                    ReturnItemCard(
                        returnWithItems = returnWithItems,
                        isAdmin = isAdmin,
                        onPreviewClick = { onPreviewReturn(returnWithItems) },
                        onPrintClick = { onPrintReturn(returnWithItems) },
                        onCancelClick = { returnToCancel = returnWithItems }
                    )
                }
            }
        }
    }

    // Cancel Return Confirmation Dialog
    returnToCancel?.let { target ->
        AlertDialog(
            onDismissRequest = { returnToCancel = null },
            title = {
                Text(
                    text = "تأكيد إلغاء المرتجع",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في إلغاء عملية المرتجع #${target.returnRecord.returnNumber}؟ سيتم التراجع عن حركة المخزون وحسابات الطرف المعني تلقائيًا.",
                    fontFamily = CairoFontFamily,
                    color = TextWhite,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelReturn(target.returnRecord.id)
                        returnToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("نعم، إلغاء المرتجع", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { returnToCancel = null }) {
                    Text("تراجع", fontFamily = CairoFontFamily, color = TextWhite)
                }
            },
            containerColor = NavySurface
        )
    }

    // Create Return Dialog
    if (showCreateDialog) {
        CreateReturnDialog(
            currentUser = currentUser,
            invoicesList = invoicesList,
            catalogModels = catalogModels,
            customers = customers,
            suppliers = suppliers,
            merchants = merchants,
            onDismiss = { showCreateDialog = false },
            onFetchReturnableInvoiceItems = onFetchReturnableInvoiceItems,
            onSubmit = { type, partyName, partyPhone, partyId, origInvNum, origInvId, items, reason, setType, setAmt, setNotes, notes ->
                onSubmitCreateReturn(
                    type, partyName, partyPhone, partyId, origInvNum, origInvId, items, reason, setType, setAmt, setNotes, notes,
                    { newRet ->
                        showCreateDialog = false
                        onPreviewReturn(newRet)
                    },
                    { errMsg ->
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
fun ReturnItemCard(
    returnWithItems: ReturnWithItems,
    isAdmin: Boolean,
    onPreviewClick: () -> Unit,
    onPrintClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val rec = returnWithItems.returnRecord
    val isCancelled = rec.status == "CANCELLED"
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale("ar")) }

    val (typeTitle, typeColor) = remember(rec.type) {
        when (rec.type) {
            "CUSTOMER_TO_STORE" -> Pair("مرتجع من عميل للمعرض", ElectricBlue)
            "STORE_TO_SUPPLIER" -> Pair("مرتجع من المعرض لمورد", GoldPrimary)
            "MERCHANT_TO_STORE" -> Pair("مرتجع من تاجر للمعرض", Color(0xFFB388FF))
            "STORE_TO_MERCHANT" -> Pair("مرتجع من المعرض لتاجر", Color(0xFFFFAB40))
            else -> Pair("مرتجع عام", GoldPrimary)
        }
    }

    val settlementLabel = remember(rec.settlementType) {
        when (rec.settlementType) {
            "CASH_REFUND" -> "استرداد نقدي"
            "CUSTOMER_CREDIT" -> "رصيد حساب عميل"
            "EXCHANGE" -> "استبدال بضاعة"
            "DECREASE_DEBT" -> "خصم مديونية مورد"
            "DECREASE_MERCHANT_DUE" -> "خصم مستحقات تاجر"
            "CASH_REFUND_FROM_SUPPLIER" -> "استرداد نقدي من مورد"
            else -> rec.settlementType
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_return_${rec.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCancelled) NavySurface.copy(alpha = 0.6f) else NavySurface
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCancelled) ErrorRed.copy(alpha = 0.4f) else GlassBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Return Number, Type & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, typeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = typeTitle,
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "#${rec.returnNumber}",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (isCancelled) ErrorRed.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isCancelled) "ملغي" else "معتمد",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCancelled) ErrorRed else SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Party & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "الطرف: ${rec.partyName}${if (rec.partyPhone.isNotBlank()) " (${rec.partyPhone})" else ""}",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    if (rec.originalInvoiceNumber.isNotBlank()) {
                        Text(
                            text = "الفاتورة الأصلية: #${rec.originalInvoiceNumber}",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = GoldLight
                        )
                    }
                }

                Text(
                    text = dateFormat.format(Date(rec.timestamp)),
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Items Summary
            Surface(
                color = NavyDarkBackground.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "الأصناف المرتجعة (${returnWithItems.items.size} أصناف):",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    returnWithItems.items.forEach { item ->
                        val conditionArabic = when (item.stockCondition) {
                            "RESELLABLE" -> Pair("صالح للبيع", SuccessGreen)
                            "DAMAGED" -> Pair("تالف", ErrorRed)
                            "INSPECTION" -> Pair("تحت الفحص", Color(0xFFFFB74D))
                            "REPAIRABLE" -> Pair("صيانة", ElectricBlue)
                            else -> Pair(item.stockCondition, TextMuted)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${item.productName} - ${item.modelName} (×${item.quantity})",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = TextWhite,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(conditionArabic.second.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = conditionArabic.first,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = conditionArabic.second
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${NumberFormat.getNumberInstance(Locale.US).format(item.totalPrice)} ج.م",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Totals & Settlement Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "التسوية: $settlementLabel",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = GoldSecondary
                    )
                    if (rec.reason.isNotBlank()) {
                        Text(
                            text = "السبب: ${rec.reason}",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "إجمالي المرتجع",
                        fontFamily = CairoFontFamily,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "${NumberFormat.getNumberInstance(Locale.US).format(rec.totalAmount)} ج.م",
                        fontFamily = CairoFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = GlassBorder
            )

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    OutlinedButton(
                        onClick = onPreviewClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                        modifier = Modifier.testTag("btn_preview_return_${rec.id}")
                    ) {
                        Text("معاينة الإيصال", fontFamily = CairoFontFamily, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onPrintClick,
                        modifier = Modifier.testTag("btn_print_return_${rec.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "طباعة حرارية",
                            tint = GoldPrimary
                        )
                    }
                }

                if (isAdmin && !isCancelled) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("btn_cancel_return_${rec.id}")
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إلغاء المرتجع", fontFamily = CairoFontFamily, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateReturnDialog(
    currentUser: UserAccountEntity,
    invoicesList: List<InvoiceWithItems>,
    catalogModels: List<ProductModelEntity>,
    customers: List<CustomerAccountEntity>,
    suppliers: List<SupplierAccountEntity>,
    merchants: List<MerchantAccountEntity>,
    onDismiss: () -> Unit,
    onFetchReturnableInvoiceItems: suspend (Long) -> Result<List<Pair<InvoiceItemEntity, Int>>>,
    onSubmit: (
        type: String,
        partyName: String,
        partyPhone: String,
        partyId: Long?,
        originalInvoiceNumber: String,
        originalInvoiceId: Long?,
        items: List<ReturnDraftItem>,
        reason: String,
        settlementType: String,
        settlementAmount: Double?,
        settlementNotes: String,
        notes: String
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedType by remember { mutableStateOf("CUSTOMER_TO_STORE") }

    // Invoice linking
    var linkToInvoice by remember { mutableStateOf(false) }
    var selectedInvoice by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var invoiceDropdownExpanded by remember { mutableStateOf(false) }
    var invoiceSearchQuery by remember { mutableStateOf("") }
    var returnableInvoiceItems by remember { mutableStateOf<List<Pair<InvoiceItemEntity, Int>>>(emptyList()) }
    var isLoadingInvoiceItems by remember { mutableStateOf(false) }

    // Party info
    var partyName by remember { mutableStateOf("") }
    var partyPhone by remember { mutableStateOf("") }
    var partyId by remember { mutableStateOf<Long?>(null) }

    // Items list
    val returnDraftItems = remember { mutableStateListOf<ReturnDraftItem>() }

    // Add Item Dialog / Model Picker
    var showAddItemDialog by remember { mutableStateOf(false) }

    // Settlement
    var settlementType by remember { mutableStateOf("CASH_REFUND") }
    var settlementAmountText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("عيوب صناعة") }
    var customReason by remember { mutableStateOf("") }
    var settlementNotes by remember { mutableStateOf("") }
    var generalNotes by remember { mutableStateOf("") }

    val totalItemsSum = returnDraftItems.sumOf { it.totalPrice }

    // Update settlement amount text when total changes if empty or matched
    LaunchedEffect(totalItemsSum) {
        if (settlementAmountText.isBlank() || settlementAmountText.toDoubleOrNull() == 0.0) {
            settlementAmountText = totalItemsSum.toString()
        }
    }

    // When invoice is chosen, load remaining returnable quantities
    LaunchedEffect(selectedInvoice) {
        val inv = selectedInvoice
        if (inv != null) {
            partyName = inv.invoice.customerName
            partyPhone = inv.invoice.customerPhone
            isLoadingInvoiceItems = true
            val res = onFetchReturnableInvoiceItems(inv.invoice.id)
            isLoadingInvoiceItems = false
            if (res.isSuccess) {
                returnableInvoiceItems = res.getOrNull() ?: emptyList()
            }
        } else {
            returnableInvoiceItems = emptyList()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = NavyDarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GoldPrimary)
                        }
                        Text(
                            text = "تسجيل مرتجع جديد",
                            fontFamily = CairoFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }

                    Text(
                        text = "المسؤول: ${currentUser.displayName}",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = GlassBorder)

                // Form body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Step 1: Return Type
                    Text(
                        text = "1. نوع عملية الإرجاع",
                        fontFamily = CairoFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )

                    val typeOptions = listOf(
                        Triple("CUSTOMER_TO_STORE", "مرتجع من عميل إلى المعرض", "استرجاع بضاعة من عميل نقدي أو آجل"),
                        Triple("STORE_TO_SUPPLIER", "مرتجع من المعرض إلى المورد", "إرجاع بضاعة تالفة أو زائدة للشركة الموردة"),
                        Triple("MERCHANT_TO_STORE", "مرتجع من تاجر إلى المعرض", "استرجاع بضاعة من تاجر جملة أو توزيع"),
                        Triple("STORE_TO_MERCHANT", "مرتجع من المعرض إلى تاجر", "إرجاع بضاعة مسحوبة من تاجر")
                    )

                    typeOptions.forEach { (typeKey, title, sub) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedType = typeKey
                                    // Adjust settlement defaults
                                    settlementType = when (typeKey) {
                                        "CUSTOMER_TO_STORE" -> "CASH_REFUND"
                                        "STORE_TO_SUPPLIER" -> "DECREASE_DEBT"
                                        else -> "DECREASE_MERCHANT_DUE"
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedType == typeKey) GoldPrimary.copy(alpha = 0.15f) else NavySurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedType == typeKey) GoldPrimary else GlassBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedType == typeKey,
                                    onClick = { selectedType = typeKey },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = title,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedType == typeKey) GoldPrimary else TextWhite
                                    )
                                    Text(
                                        text = sub,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    // Step 2: Invoice Link Option
                    if (selectedType == "CUSTOMER_TO_STORE") {
                        Text(
                            text = "2. ربط بفاتورة بيع أصلية (اختياري / موصى به)",
                            fontFamily = CairoFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Switch(
                                checked = linkToInvoice,
                                onCheckedChange = {
                                    linkToInvoice = it
                                    if (!it) {
                                        selectedInvoice = null
                                        returnableInvoiceItems = emptyList()
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (linkToInvoice) "مرتبط بفاتورة مسجلة بالنظام" else "مرتجع مباشر بدون فاتورة",
                                fontFamily = CairoFontFamily,
                                fontSize = 13.sp,
                                color = TextWhite
                            )
                        }

                        if (linkToInvoice) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavySurface),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    OutlinedTextField(
                                        value = invoiceSearchQuery,
                                        onValueChange = { invoiceSearchQuery = it },
                                        placeholder = { Text("ابحث برقم الفاتورة أو اسم العميل...", fontSize = 12.sp, color = TextMuted) },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val filteredInvoices = remember(invoicesList, invoiceSearchQuery) {
                                        if (invoiceSearchQuery.isBlank()) invoicesList.take(10)
                                        else invoicesList.filter {
                                            it.invoice.invoiceNumber.contains(invoiceSearchQuery) ||
                                                it.invoice.customerName.contains(invoiceSearchQuery) ||
                                                it.invoice.customerPhone.contains(invoiceSearchQuery)
                                        }.take(10)
                                    }

                                    Text(
                                        text = "اختر الفاتورة المراد الإرجاع منها:",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )

                                    filteredInvoices.forEach { inv ->
                                        val isChosen = selectedInvoice?.invoice?.id == inv.invoice.id
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedInvoice = inv }
                                                .background(
                                                    if (isChosen) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "فاتورة #${inv.invoice.invoiceNumber} - ${inv.invoice.customerName.ifBlank { "عميل نقدي" }}",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isChosen) GoldPrimary else TextWhite
                                                )
                                                Text(
                                                    text = "${inv.items.size} أصناف • الإجمالي: ${inv.invoice.finalTotal} ج.م",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 10.sp,
                                                    color = TextMuted
                                                )
                                            }
                                            if (isChosen) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldPrimary)
                                            }
                                        }
                                    }

                                    // Display Available items to return from this invoice
                                    if (selectedInvoice != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = GlassBorder)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "الأصناف المتاحة للإرجاع من الفاتورة #${selectedInvoice!!.invoice.invoiceNumber}:",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldLight
                                        )

                                        if (isLoadingInvoiceItems) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                                        } else {
                                            returnableInvoiceItems.forEach { (invItem, remainingQty) ->
                                                val alreadyInDraft = returnDraftItems.find { it.modelId == invItem.modelId }
                                                val isExhausted = remainingQty <= 0

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "${invItem.productName} - ${invItem.modelName}",
                                                            fontFamily = CairoFontFamily,
                                                            fontSize = 12.sp,
                                                            color = if (isExhausted) TextMuted else TextWhite
                                                        )
                                                        Text(
                                                            text = "الكمية المباعة: ${invItem.quantity} | المتبقي للإرجاع: $remainingQty | سعر البيع: ${invItem.unitPrice} ج.م",
                                                            fontFamily = CairoFontFamily,
                                                            fontSize = 10.sp,
                                                            color = if (isExhausted) ErrorRed else GoldSecondary
                                                        )
                                                    }

                                                    if (isExhausted) {
                                                        Text(
                                                            text = "تم إرجاعها بالكامل",
                                                            fontFamily = CairoFontFamily,
                                                            fontSize = 10.sp,
                                                            color = ErrorRed
                                                        )
                                                    } else if (alreadyInDraft != null) {
                                                        Text(
                                                            text = "تمت إضافتها (${alreadyInDraft.quantity})",
                                                            fontFamily = CairoFontFamily,
                                                            fontSize = 11.sp,
                                                            color = SuccessGreen
                                                        )
                                                    } else {
                                                        Button(
                                                            onClick = {
                                                                returnDraftItems.add(
                                                                    ReturnDraftItem(
                                                                        modelId = invItem.modelId,
                                                                        productName = invItem.productName,
                                                                        modelName = invItem.modelName,
                                                                        categoryName = invItem.categoryName,
                                                                        brandName = invItem.brandName,
                                                                        quantity = 1,
                                                                        unitPrice = invItem.unitPrice,
                                                                        stockCondition = "RESELLABLE"
                                                                    )
                                                                )
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text("+ إضافة", fontFamily = CairoFontFamily, fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Step 3: Party Details
                    Text(
                        text = "3. بيانات الطرف المعني",
                        fontFamily = CairoFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )

                    OutlinedTextField(
                        value = partyName,
                        onValueChange = { partyName = it },
                        label = {
                            Text(
                                when (selectedType) {
                                    "CUSTOMER_TO_STORE" -> "اسم العميل *"
                                    "STORE_TO_SUPPLIER" -> "اسم المورد / الشركة *"
                                    else -> "اسم التاجر *"
                                },
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_return_party_name"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = partyPhone,
                        onValueChange = { partyPhone = it },
                        label = { Text("رقم الهاتف (اختياري)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_return_party_phone"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Step 4: Items Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "4. الأصناف المرتجعة (${returnDraftItems.size})",
                            fontFamily = CairoFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )

                        Button(
                            onClick = { showAddItemDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_add_manual_return_item")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة صنف من الكتالوج", fontFamily = CairoFontFamily, fontSize = 11.sp)
                        }
                    }

                    if (returnDraftItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavySurface, RoundedCornerShape(10.dp))
                                .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لم تتم إضافة أي أصناف حتى الآن. اختر من الفاتورة أو اضغط «إضافة صنف من الكتالوج».",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        returnDraftItems.forEachIndexed { index, draftItem ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavySurface),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${draftItem.productName} - ${draftItem.modelName}",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                            Text(
                                                text = "الإجمالي: ${NumberFormat.getNumberInstance(Locale.US).format(draftItem.totalPrice)} ج.م",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = GoldPrimary
                                            )
                                        }

                                        IconButton(onClick = { returnDraftItems.removeAt(index) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Quantity & Price Controls
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Quantity Row
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("الكمية: ", fontFamily = CairoFontFamily, fontSize = 12.sp, color = TextMuted)
                                            IconButton(
                                                onClick = {
                                                    if (draftItem.quantity > 1) {
                                                        returnDraftItems[index] = draftItem.copy(quantity = draftItem.quantity - 1)
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "إنقاص", tint = GoldPrimary)
                                            }
                                            Text(
                                                text = "${draftItem.quantity}",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    // Validate if tied to invoice
                                                    val maxAllowed = returnableInvoiceItems.find { it.first.modelId == draftItem.modelId }?.second
                                                    if (maxAllowed == null || draftItem.quantity < maxAllowed) {
                                                        returnDraftItems[index] = draftItem.copy(quantity = draftItem.quantity + 1)
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "زيادة", tint = GoldPrimary)
                                            }
                                        }

                                        // Unit Price Input
                                        OutlinedTextField(
                                            value = draftItem.unitPrice.toString(),
                                            onValueChange = { newPriceStr ->
                                                val newPrice = newPriceStr.toDoubleOrNull() ?: draftItem.unitPrice
                                                returnDraftItems[index] = draftItem.copy(unitPrice = newPrice)
                                            },
                                            label = { Text("سعر الوحدة", fontSize = 10.sp) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Condition Picker
                                    Text(
                                        text = "الحالة المخزنية للصنف المرتجع:",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val conditions = listOf(
                                            Pair("RESELLABLE", "صالح للبيع"),
                                            Pair("DAMAGED", "تالف"),
                                            Pair("INSPECTION", "تحت الفحص"),
                                            Pair("REPAIRABLE", "صيانة")
                                        )

                                        conditions.forEach { (condKey, condLabel) ->
                                            val isSel = draftItem.stockCondition == condKey
                                            FilterChip(
                                                selected = isSel,
                                                onClick = { returnDraftItems[index] = draftItem.copy(stockCondition = condKey) },
                                                label = { Text(condLabel, fontSize = 11.sp, fontFamily = CairoFontFamily) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = if (condKey == "RESELLABLE") SuccessGreen else GoldPrimary,
                                                    selectedLabelColor = TextDark
                                                )
                                            )
                                        }
                                    }

                                    // Explanation note on condition
                                    val condExpl = when (draftItem.stockCondition) {
                                        "RESELLABLE" -> "✓ سيعود للمخزون المتاح للبيع تلقائياً."
                                        "DAMAGED" -> "⚠ صنف تالف: لن يدخل المخزون المتاح للبيع."
                                        "INSPECTION" -> "🔍 صنف تحت الفحص الفني: مستبعد من البيع مؤقتاً."
                                        else -> "🔧 صنف محول للصيانة والإصلاح: مستبعد من البيع."
                                    }
                                    Text(
                                        text = condExpl,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = if (draftItem.stockCondition == "RESELLABLE") SuccessGreen else GoldSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Step 5: Financial Settlement & Reasons
                    Text(
                        text = "5. التسوية المالية والسبب",
                        fontFamily = CairoFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )

                    // Settlement Type Options
                    val settlementOptions = remember(selectedType) {
                        when (selectedType) {
                            "CUSTOMER_TO_STORE" -> listOf(
                                Pair("CASH_REFUND", "استرداد نقدي فوري للعميل"),
                                Pair("CUSTOMER_CREDIT", "إضافة إلى رصيد حساب العميل"),
                                Pair("EXCHANGE", "استبدال بضاعة بأخرى")
                            )
                            "STORE_TO_SUPPLIER" -> listOf(
                                Pair("DECREASE_DEBT", "خصم من مديونية المورد"),
                                Pair("CASH_REFUND_FROM_SUPPLIER", "استرداد نقدي من المورد")
                            )
                            else -> listOf(
                                Pair("DECREASE_MERCHANT_DUE", "خصم من مستحقات التاجر"),
                                Pair("CASH_REFUND", "تسوية نقدية فورية")
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        settlementOptions.forEach { (sKey, sLabel) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settlementType = sKey }
                            ) {
                                RadioButton(
                                    selected = settlementType == sKey,
                                    onClick = { settlementType = sKey },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(sLabel, fontFamily = CairoFontFamily, fontSize = 12.sp, color = TextWhite)
                            }
                        }
                    }

                    // Settlement Amount Input
                    OutlinedTextField(
                        value = settlementAmountText,
                        onValueChange = { settlementAmountText = it },
                        label = { Text("المبلغ المسوى (ج.م) *", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_settlement_amount"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Reason Selector
                    val commonReasons = listOf("عيوب صناعة", "طلب العميل", "استبدال بموديل آخر", "خطأ في المقاس أو اللون", "تلف أثناء الشحن أو النقل", "أخرى")
                    Text("سبب الإرجاع:", fontFamily = CairoFontFamily, fontSize = 12.sp, color = TextMuted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(commonReasons) { rOption ->
                            FilterChip(
                                selected = reason == rOption,
                                onClick = { reason = rOption },
                                label = { Text(rOption, fontFamily = CairoFontFamily, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = TextDark
                                )
                            )
                        }
                    }

                    if (reason == "أخرى") {
                        OutlinedTextField(
                            value = customReason,
                            onValueChange = { customReason = it },
                            placeholder = { Text("اكتب سبب الإرجاع بالتفصيل...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = generalNotes,
                        onValueChange = { generalNotes = it },
                        label = { Text("ملاحظات إضافية (اختياري)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = GlassBorder)

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي قيمة الأصناف:",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "${NumberFormat.getNumberInstance(Locale.US).format(totalItemsSum)} ج.م",
                            fontFamily = CairoFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }

                    Row {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إلغاء", fontFamily = CairoFontFamily, color = TextWhite)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val finalReason = if (reason == "أخرى") customReason.ifBlank { "أخرى" } else reason
                                val setAmt = settlementAmountText.toDoubleOrNull() ?: totalItemsSum
                                val origInvNum = selectedInvoice?.invoice?.invoiceNumber ?: ""
                                val origInvId = selectedInvoice?.invoice?.id

                                onSubmit(
                                    selectedType,
                                    partyName,
                                    partyPhone,
                                    partyId,
                                    origInvNum,
                                    origInvId,
                                    returnDraftItems.toList(),
                                    finalReason,
                                    settlementType,
                                    setAmt,
                                    settlementNotes,
                                    generalNotes
                                )
                            },
                            enabled = returnDraftItems.isNotEmpty() && partyName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = TextDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_confirm_create_return")
                        ) {
                            Text(
                                text = "اعتماد وحفظ المرتجع",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Manual Item from Catalog Dialog
    if (showAddItemDialog) {
        AddManualReturnItemDialog(
            catalogModels = catalogModels,
            onDismiss = { showAddItemDialog = false },
            onItemAdded = { model, qty, price, condition, notes ->
                returnDraftItems.add(
                    ReturnDraftItem(
                        modelId = model.id,
                        productName = model.productName,
                        modelName = model.modelName,
                        categoryName = model.categoryName,
                        brandName = model.brandName,
                        quantity = qty,
                        unitPrice = price,
                        stockCondition = condition,
                        itemNotes = notes
                    )
                )
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun AddManualReturnItemDialog(
    catalogModels: List<ProductModelEntity>,
    onDismiss: () -> Unit,
    onItemAdded: (ProductModelEntity, Int, Double, String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf<ProductModelEntity?>(null) }
    var quantity by remember { mutableIntStateOf(1) }
    var unitPriceText by remember { mutableStateOf("") }
    var stockCondition by remember { mutableStateOf("RESELLABLE") }
    var itemNotes by remember { mutableStateOf("") }

    val filtered = remember(catalogModels, searchQuery) {
        if (searchQuery.isBlank()) catalogModels.take(20)
        else catalogModels.filter {
            it.productName.contains(searchQuery, ignoreCase = true) ||
                it.modelName.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true)
        }.take(20)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = NavySurface,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "اختيار صنف من المخزون للإرجاع",
                    fontFamily = CairoFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث باسم المنتج أو الموديل أو الكود...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Text("الأصناف المتطابقة:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = TextMuted)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filtered.forEach { m ->
                        val isSel = selectedModel?.id == m.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedModel = m
                                    unitPriceText = m.finalPrice.toString()
                                }
                                .background(
                                    if (isSel) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${m.productName} - ${m.modelName}",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) GoldPrimary else TextWhite
                                )
                                Text(
                                    text = "رصيد المخزن: ${m.stockQuantity} | السعر: ${m.finalPrice} ج.م",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            if (isSel) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldPrimary)
                        }
                    }
                }

                if (selectedModel != null) {
                    HorizontalDivider(color = GlassBorder)

                    // Quantity and Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = quantity.toString(),
                            onValueChange = { quantity = it.toIntOrNull()?.coerceAtLeast(1) ?: 1 },
                            label = { Text("الكمية", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = unitPriceText,
                            onValueChange = { unitPriceText = it },
                            label = { Text("سعر الوحدة", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    Text("الحالة المخزنية:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = TextMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair("RESELLABLE", "صالح للبيع"),
                            Pair("DAMAGED", "تالف"),
                            Pair("INSPECTION", "تحت الفحص"),
                            Pair("REPAIRABLE", "صيانة")
                        ).forEach { (cKey, cName) ->
                            FilterChip(
                                selected = stockCondition == cKey,
                                onClick = { stockCondition = cKey },
                                label = { Text(cName, fontSize = 10.sp, fontFamily = CairoFontFamily) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = itemNotes,
                        onValueChange = { itemNotes = it },
                        placeholder = { Text("ملاحظات عن حالة الصنف (اختياري)...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("إلغاء", fontFamily = CairoFontFamily, color = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val m = selectedModel!!
                            val p = unitPriceText.toDoubleOrNull() ?: m.finalPrice
                            onItemAdded(m, quantity, p, stockCondition, itemNotes)
                        },
                        enabled = selectedModel != null,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                    ) {
                        Text("إضافة الصنف", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReturnReceiptPreviewDialog(
    returnWithItems: ReturnWithItems,
    storeProfile: StoreProfile?,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    val rec = returnWithItems.returnRecord
    val items = returnWithItems.items
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale("ar")) }

    val storeName = storeProfile?.storeName?.ifBlank { "مؤسسة البشيري للأجهزة الكهربائية" } ?: "مؤسسة البشيري للأجهزة الكهربائية"

    val typeTitle = when (rec.type) {
        "CUSTOMER_TO_STORE" -> "سند إرجاع - من عميل للمعرض"
        "STORE_TO_SUPPLIER" -> "سند إرجاع - من المعرض لمورد"
        "MERCHANT_TO_STORE" -> "سند إرجاع - من تاجر للمعرض"
        "STORE_TO_MERCHANT" -> "سند إرجاع - من المعرض لتاجر"
        else -> "سند مرتجع رسمي"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = NavyDarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "معاينة إيصال المرتجع",
                        fontFamily = CairoFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )

                    Row {
                        Button(
                            onClick = onPrint,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_print_receipt_dialog")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طباعة حرارية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Clear, contentDescription = "إغلاق", tint = TextWhite)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = GlassBorder)

                // White paper thermal receipt simulation
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = storeName,
                            fontFamily = CairoFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        if (!storeProfile?.storeAddress.isNullOrBlank()) {
                            Text(
                                text = storeProfile!!.storeAddress,
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (!storeProfile?.storePhone.isNullOrBlank()) {
                            Text(
                                text = "تليفون: ${storeProfile!!.storePhone}",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        val legalParts = listOfNotNull(
                            if (!storeProfile?.commercialRegister.isNullOrBlank()) "س.ت: ${storeProfile!!.commercialRegister}" else null,
                            if (!storeProfile?.taxCard.isNullOrBlank()) "بطاقة ضريبية: ${storeProfile!!.taxCard}" else null
                        ).joinToString(" • ")

                        if (legalParts.isNotBlank()) {
                            Text(
                                text = legalParts,
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "================================", color = Color.Black, fontSize = 12.sp)
                        Text(
                            text = "【 $typeTitle 】",
                            fontFamily = CairoFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(text = "================================", color = Color.Black, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Receipt Metadata
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("رقم المرتجع: ${rec.returnNumber}", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                                Text("التاريخ: ${dateFormat.format(Date(rec.timestamp))}", fontFamily = CairoFontFamily, color = Color.Black, fontSize = 11.sp)
                            }
                            if (rec.originalInvoiceNumber.isNotBlank()) {
                                Text("الفاتورة الأصلية: #${rec.originalInvoiceNumber}", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                            }
                            Text("الطرف: ${rec.partyName}${if (rec.partyPhone.isNotBlank()) " (${rec.partyPhone})" else ""}", fontFamily = CairoFontFamily, color = Color.Black, fontSize = 12.sp)
                            Text("المسؤول: ${rec.createdByName}", fontFamily = CairoFontFamily, color = Color.Black, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "--------------------------------", color = Color.Black, fontSize = 12.sp)

                        // Items Table Header
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الصنف والموديل (الحالة)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                            Text("الكمية × السعر", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        }
                        Text(text = "--------------------------------", color = Color.Black, fontSize = 12.sp)

                        items.forEach { itm ->
                            val cond = when (itm.stockCondition) {
                                "RESELLABLE" -> "صالح للبيع"
                                "DAMAGED" -> "تالف"
                                "INSPECTION" -> "تحت الفحص"
                                "REPAIRABLE" -> "صيانة"
                                else -> itm.stockCondition
                            }

                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(
                                    text = "${itm.productName} - ${itm.modelName} [$cond]",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${itm.quantity} × ${NumberFormat.getNumberInstance(Locale.US).format(itm.unitPrice)} ج.م",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${NumberFormat.getNumberInstance(Locale.US).format(itm.totalPrice)} ج.م",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        Text(text = "================================", color = Color.Black, fontSize = 12.sp)

                        // Totals & Settlement
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي قيمة المرتجع:", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                            Text("${NumberFormat.getNumberInstance(Locale.US).format(rec.totalAmount)} ج.م", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                        }

                        val setAr = when (rec.settlementType) {
                            "CASH_REFUND" -> "استرداد نقدي"
                            "CUSTOMER_CREDIT" -> "إضافة لرصيد الحساب"
                            "EXCHANGE" -> "استبدال بضاعة"
                            "DECREASE_DEBT" -> "خصم من مديونية المورد"
                            "DECREASE_MERCHANT_DUE" -> "خصم من مستحقات التاجر"
                            "CASH_REFUND_FROM_SUPPLIER" -> "استرداد نقدي من المورد"
                            else -> rec.settlementType
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("طريقة التسوية: $setAr", fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold, color = Color.Black, fontSize = 12.sp)
                            Text("المبلغ: ${NumberFormat.getNumberInstance(Locale.US).format(rec.settlementAmount)} ج.م", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        }

                        if (rec.reason.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("السبب: ${rec.reason}", fontFamily = CairoFontFamily, color = Color.Black, fontSize = 11.sp)
                            }
                        }
                        if (rec.notes.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("ملاحظات: ${rec.notes}", fontFamily = CairoFontFamily, color = Color.Black, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("توقيع المستلم: ....................", fontFamily = CairoFontFamily, color = Color.Black, fontSize = 11.sp)
                            Text("اعتماد الإدارة: ....................", fontFamily = CairoFontFamily, color = Color.Black, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "--------------------------------", color = Color.Black, fontSize = 12.sp)
                        Text("سند مرتجع معتمد آلياً بالنظام", fontFamily = CairoFontFamily, color = Color.DarkGray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
