package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceItemEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.SaleRecordEntity
import com.example.data.local.UserAccountEntity
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
import com.example.ui.theme.WarningAmber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Invoices Management Screen («شاشة إدارة الفواتير»).
 *
 * Requirements:
 * 1. Prominent button: «استخراج فاتورة جديدة» to open multi-product invoice creator.
 * 2. Section «سجل الفواتير» to display previous invoices, search, view all items, preview, and print.
 * 3. Shows complete itemized list of all products per invoice.
 * 4. Backward compatible with legacy sales.
 */
@Composable
fun SalesLogScreen(
    currentUser: UserAccountEntity,
    invoicesWithItems: List<InvoiceWithItems> = emptyList(),
    legacySales: List<SaleRecordEntity> = emptyList(),
    onOpenCreateInvoice: () -> Unit,
    onOpenInvoiceWithItemsPreview: (InvoiceWithItems) -> Unit,
    onPrintInvoiceWithItems: (InvoiceWithItems) -> Unit,
    onOpenInvoicePreview: ((SaleRecordEntity) -> Unit)? = null,
    onPrintSaleReceipt: ((SaleRecordEntity) -> Unit)? = null
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG")) }

    // Merge multi-item invoices with any legacy unmigrated sales
    val allInvoices = remember(invoicesWithItems, legacySales) {
        if (invoicesWithItems.isNotEmpty()) {
            invoicesWithItems
        } else {
            legacySales.map { sale ->
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
                        timestamp = sale.timestamp
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
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSellerFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) } // "ALL", "PAID", "PARTIAL"

    val filteredInvoices = remember(allInvoices, searchQuery, selectedSellerFilter, selectedStatusFilter) {
        allInvoices.filter { invoiceWithItems ->
            val inv = invoiceWithItems.invoice
            val matchesSeller = selectedSellerFilter == null ||
                    inv.sellerId == selectedSellerFilter ||
                    inv.sellerName == selectedSellerFilter

            val matchesStatus = selectedStatusFilter == null || inv.paymentStatus == selectedStatusFilter

            val query = searchQuery.trim()
            val matchesQuery = if (query.isEmpty()) {
                true
            } else {
                inv.invoiceNumber.contains(query, ignoreCase = true) ||
                        inv.customerName.contains(query, ignoreCase = true) ||
                        inv.customerPhone.contains(query, ignoreCase = true) ||
                        inv.sellerName.contains(query, ignoreCase = true) ||
                        invoiceWithItems.items.any { item ->
                            item.productName.contains(query, ignoreCase = true) ||
                                    item.modelName.contains(query, ignoreCase = true) ||
                                    item.brandName.contains(query, ignoreCase = true)
                        }
            }

            matchesSeller && matchesStatus && matchesQuery
        }
    }

    val totalRevenue = filteredInvoices.sumOf { it.invoice.finalTotal }
    val totalPaid = filteredInvoices.sumOf { it.invoice.paidAmount }
    val totalRemaining = filteredInvoices.sumOf { it.invoice.remainingAmount }
    val totalInvoicesCount = filteredInvoices.size
    val totalItemsCount = filteredInvoices.sumOf { it.items.sumOf { item -> item.quantity } }

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
            // 1. Executive Top Header & Action Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorderGlow),
                shape = RoundedCornerShape(16.dp)
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(GoldContainer)
                                    .border(1.dp, GoldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "شاشة إدارة الفواتير والمبيعات",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "استخراج فواتير متعددة الأصناف وسجل المعاملات",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = CairoFontFamily,
                                    color = GoldSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // PROMINENT BUTTON: «استخراج فاتورة جديدة»
                        Button(
                            onClick = onOpenCreateInvoice,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = TextDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier.testTag("create_new_invoice_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "استخراج فاتورة جديدة",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4-way specialized financial summary strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Invoices
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavySurface)
                                .border(0.8.dp, GlassBorder, RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = "عدد الفواتير", style = MaterialTheme.typography.labelSmall, fontFamily = CairoFontFamily, color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = "$totalInvoicesCount فاتورة",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                        }

                        // Total Items
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavySurface)
                                .border(0.8.dp, GlassBorder, RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = "إجمالي القطع", style = MaterialTheme.typography.labelSmall, fontFamily = CairoFontFamily, color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = "$totalItemsCount قطعة",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                            }
                        }

                        // Total Revenue
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavySurface)
                                .border(0.8.dp, GoldBorderGlow, RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = "إجمالي الفواتير", style = MaterialTheme.typography.labelSmall, fontFamily = CairoFontFamily, color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = "${numberFormat.format(totalRevenue)} ج.م",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldPrimary
                                )
                            }
                        }

                        // Remaining Balance (if any debt)
                        if (totalRemaining > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavySurface)
                                    .border(0.8.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(text = "متبقي آجل بالخارج", style = MaterialTheme.typography.labelSmall, fontFamily = CairoFontFamily, color = TextMuted, fontSize = 10.sp)
                                    Text(
                                        text = "${numberFormat.format(totalRemaining)} ج.م",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Section: «سجل الفواتير» (Search & Filter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل الفواتير السابقة",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Text(
                    text = "${filteredInvoices.size} من إجمالي ${allInvoices.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = CairoFontFamily,
                    color = TextMuted
                )
            }

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("invoices_search_field"),
                placeholder = {
                    Text(
                        "ابحث برقم الفاتورة، اسم العميل، الهاتف، أو اسم المنتج...",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح البحث", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NavySurface,
                    unfocusedContainerColor = NavySurface,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Seller Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedSellerFilter == null,
                        onClick = { selectedSellerFilter = null },
                        label = { Text("جميع البائعين", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = TextDark,
                            containerColor = NavySurface,
                            labelColor = TextWhite
                        )
                    )
                }
                listOf(
                    "بائع 1" to "seller1",
                    "بائع 2" to "seller2",
                    "بائع 3" to "seller3",
                    "المدير" to "admin"
                ).forEach { (displayName, id) ->
                    item {
                        FilterChip(
                            selected = selectedSellerFilter == id,
                            onClick = { selectedSellerFilter = id },
                            label = { Text(displayName, fontFamily = CairoFontFamily, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = TextDark,
                                containerColor = NavySurface,
                                labelColor = TextWhite
                            )
                        )
                    }
                }

                item {
                    FilterChip(
                        selected = selectedStatusFilter == "PARTIAL",
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == "PARTIAL") null else "PARTIAL"
                        },
                        label = { Text("فواتير عليها متبقي آجل", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarningAmber,
                            selectedLabelColor = TextDark,
                            containerColor = NavySurface,
                            labelColor = WarningAmber
                        )
                    )
                }
            }

            // 3. Invoices List with Itemized Breakdown
            if (filteredInvoices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "لا توجد فواتير مطابقة لبحثك" else "لا توجد فواتير مسجلة في السجل بعد",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = CairoFontFamily,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onOpenCreateInvoice,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استخراج أول فاتورة الآن", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredInvoices, key = { it.invoice.id }) { invoiceWithItems ->
                        InvoiceRecordCard(
                            invoiceWithItems = invoiceWithItems,
                            numberFormat = numberFormat,
                            dateFormat = dateFormat,
                            onPreviewInvoice = { onOpenInvoiceWithItemsPreview(invoiceWithItems) },
                            onPrintInvoice = { onPrintInvoiceWithItems(invoiceWithItems) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Invoice Card showing Header, Customer info, All itemized products, Totals, and Print/Preview.
 */
@Composable
fun InvoiceRecordCard(
    invoiceWithItems: InvoiceWithItems,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onPreviewInvoice: () -> Unit,
    onPrintInvoice: () -> Unit
) {
    val invoice = invoiceWithItems.invoice
    val items = invoiceWithItems.items

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("invoice_card_${invoice.id}"),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(GlassBorder)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar: Invoice Number + Date + Seller + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Serial Number badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldContainer)
                        .border(0.6.dp, GoldPrimary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "#فاتورة-${invoice.invoiceNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Date & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(invoice.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = CairoFontFamily,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                // Seller Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavySurfaceElevated)
                        .border(0.8.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = invoice.sellerName,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = GoldSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Customer Details Block (if recorded)
            if (invoice.customerName.isNotBlank() || invoice.customerPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurfaceElevated)
                        .border(0.6.dp, GlassBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "العميل: ${if (invoice.customerName.isNotBlank()) invoice.customerName else "عميل نقدي"}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 11.sp
                            )
                        }

                        if (invoice.customerPhone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = invoice.customerPhone,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = CairoFontFamily,
                                    color = GoldSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Itemized Products Section (Shows ALL products in the invoice)
            Text(
                text = "الأصناف المسجلة بالفاتورة (${items.size} أصناف):",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurfaceVariant)
                    .border(0.6.dp, GlassBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${index + 1}. ",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = CairoFontFamily,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${item.productName} - ${item.modelName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "${item.quantity} × ${numberFormat.format(item.unitPrice)} = ${numberFormat.format(item.totalPrice)} ج.م",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            fontSize = 10.sp
                        )
                    }
                    if (index < items.size - 1) {
                        HorizontalDivider(color = GlassBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurfaceElevated)
                    .border(0.6.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (invoice.discount > 0) {
                            Text(
                                text = "الإجمالي: ${numberFormat.format(invoice.subtotal)} ج.م (خصم: ${numberFormat.format(invoice.discount)} ج.م)",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = CairoFontFamily,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "المحصل: ${numberFormat.format(invoice.paidAmount)} ج.م",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = CairoFontFamily,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            if (invoice.remainingAmount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "متبقي: ${numberFormat.format(invoice.remainingAmount)} ج.م",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = CairoFontFamily,
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "صافي الفاتورة:",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = CairoFontFamily,
                            color = GoldSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${numberFormat.format(invoice.finalTotal)} ج.م",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary
                        )
                    }
                }
            }

            if (invoice.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ملاحظات الفاتورة: ${invoice.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = CairoFontFamily,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Action Buttons: Preview Invoice & Direct Thermal Print
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPreviewInvoice,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("preview_invoice_button_${invoice.id}")
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp), tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("معاينة الفاتورة", fontFamily = CairoFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onPrintInvoice,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("reprint_invoice_button_${invoice.id}")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("طباعة حرارية", fontFamily = CairoFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
