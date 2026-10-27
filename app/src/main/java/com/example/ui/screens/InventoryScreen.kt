package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.data.local.ProductModelEntity
import com.example.data.local.UserAccountEntity
import com.example.ui.components.EditStockDialog
import com.example.ui.theme.ErrorRed
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
import java.util.Locale

@Composable
fun InventoryScreen(
    currentUser: UserAccountEntity,
    models: List<ProductModelEntity>,
    categories: List<CategoryEntity>,
    searchQuery: String,
    categoryFilter: String?,
    sortOption: String,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onSortOptionChange: (String) -> Unit,
    onOpenSale: (ProductModelEntity) -> Unit,
    onUpdateStock: (modelId: Long, newStock: Int) -> Unit
) {
    val isAdmin = currentUser.role == "ADMIN"
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }

    var modelForStockEdit by remember { mutableStateOf<ProductModelEntity?>(null) }
    var filterLowStockOnly by remember { mutableStateOf(false) }

    // Filter & Sort Logic
    val filteredModels = models
        .filter { model ->
            val matchesCategory = categoryFilter == null || model.categoryName == categoryFilter
            val matchesLowStock = !filterLowStockOnly || model.stockQuantity <= 2
            val matchesSearch = searchQuery.isBlank() ||
                model.modelName.contains(searchQuery, ignoreCase = true) ||
                model.productName.contains(searchQuery, ignoreCase = true) ||
                model.brandName.contains(searchQuery, ignoreCase = true) ||
                model.code.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesLowStock && matchesSearch
        }
        .let { list ->
            when (sortOption) {
                "LOW_STOCK" -> list.sortedBy { it.stockQuantity }
                "HIGH_STOCK" -> list.sortedByDescending { it.stockQuantity }
                "PRICE_DESC" -> list.sortedByDescending { it.finalPrice }
                "PRICE_ASC" -> list.sortedBy { it.finalPrice }
                else -> list.sortedWith(compareBy({ it.categoryName }, { it.brandName }))
            }
        }

    val totalStockCount = models.sumOf { it.stockQuantity }
    val lowStockCount = models.count { it.stockQuantity <= 2 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Metrics Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Models
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardBorder))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "إجمالي الموديلات", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "${models.size} موديل",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }

                // Total Units
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardBorder))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "إجمالي رصيد المخزن", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "$totalStockCount قطعة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                }

                // Low Stock Alert (Interactive Card to show low-stock items in 1 click)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            filterLowStockOnly = !filterLowStockOnly
                            if (filterLowStockOnly) onSortOptionChange("LOW_STOCK")
                        }
                        .testTag("metric_card_low_stock"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (filterLowStockOnly) WarningAmber.copy(alpha = 0.22f) else NavySurface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            when {
                                filterLowStockOnly -> WarningAmber
                                lowStockCount > 0 -> WarningAmber.copy(alpha = 0.7f)
                                else -> NavyCardBorder
                            }
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نواقص (<=2)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (filterLowStockOnly) WarningAmber else TextMuted
                            )
                            if (lowStockCount > 0) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = "$lowStockCount جهاز",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (lowStockCount > 0) WarningAmber else SuccessGreen
                        )
                    }
                }
            }

            // PROMINENT INTERACTIVE LOW STOCK ALERT BANNER
            if (lowStockCount > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable {
                            filterLowStockOnly = !filterLowStockOnly
                            if (filterLowStockOnly) onSortOptionChange("LOW_STOCK")
                        }
                        .testTag("low_stock_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (filterLowStockOnly) WarningAmber.copy(alpha = 0.25f) else WarningAmber.copy(alpha = 0.12f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(WarningAmber.copy(alpha = 0.85f))
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "تنبيه اقتراب نفاد المخزون",
                                tint = WarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "تنبيه اقتراب نفاد المخزون",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber
                                )
                                Text(
                                    text = "يوجد $lowStockCount أجهزة رصيدها 2 قطعة أو أقل (أوشكت على النفاد)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextWhite.copy(alpha = 0.95f)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (filterLowStockOnly) WarningAmber else NavySurface)
                                .border(1.dp, WarningAmber, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (filterLowStockOnly) "عرض الكل ✕" else "عرض النواقص فقط",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (filterLowStockOnly) TextDark else WarningAmber
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("بحث في المخزون الشامل...", color = TextMuted) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "بحث", tint = GoldPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextMuted)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NavySurface,
                    unfocusedContainerColor = NavySurface,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = NavyCardBorder,
                    cursorColor = GoldPrimary,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("inventory_search_bar")
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = categoryFilter == null,
                    onClick = { onCategoryFilterChange(null) },
                    label = { Text("الكل") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = TextDark,
                        containerColor = NavySurface,
                        labelColor = TextWhite
                    )
                )
                categories.forEach { cat ->
                    FilterChip(
                        selected = categoryFilter == cat.name,
                        onClick = { onCategoryFilterChange(cat.name) },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = TextDark,
                            containerColor = NavySurface,
                            labelColor = TextWhite
                        )
                    )
                }
            }

            // Sort & Filter Options Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Toggle Chip for Low Stock Only
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (filterLowStockOnly) WarningAmber.copy(alpha = 0.25f) else NavySurface)
                        .border(
                            1.dp,
                            if (filterLowStockOnly) WarningAmber else NavyCardBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            filterLowStockOnly = !filterLowStockOnly
                            if (filterLowStockOnly) onSortOptionChange("LOW_STOCK")
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("filter_low_stock_chip")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (filterLowStockOnly) WarningAmber else TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (filterLowStockOnly) "نواقص فقط ($lowStockCount) ✓" else "فلتر النواقص (<=2)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (filterLowStockOnly) FontWeight.Bold else FontWeight.Normal,
                            color = if (filterLowStockOnly) WarningAmber else TextWhite
                        )
                    }
                }

                Text(
                    text = "الترتيب:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                listOf(
                    "ALL" to "الافتراضي",
                    "LOW_STOCK" to "الأقل كمية أولاً",
                    "HIGH_STOCK" to "الأعلى كمية",
                    "PRICE_DESC" to "الأعلى سعراً",
                    "PRICE_ASC" to "الأقل سعراً"
                ).forEach { (key, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (sortOption == key) NavySurfaceElevated else NavySurface)
                            .border(
                                1.dp,
                                if (sortOption == key) GoldPrimary else NavyCardBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSortOptionChange(key) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (sortOption == key) FontWeight.Bold else FontWeight.Normal,
                            color = if (sortOption == key) GoldPrimary else TextWhite
                        )
                    }
                }
            }

            // Inventory List
            if (filteredModels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد أجهزة مطابقة لخيارات الفلترة الحالية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredModels, key = { it.id }) { model ->
                        InventoryItemCard(
                            model = model,
                            isAdmin = isAdmin,
                            numberFormat = numberFormat,
                            onOpenSale = { onOpenSale(model) },
                            onEditStock = { modelForStockEdit = model },
                            onQuickStockChange = { delta ->
                                val newStock = maxOf(0, model.stockQuantity + delta)
                                onUpdateStock(model.id, newStock)
                            }
                        )
                    }
                }
            }
        }
    }

    if (modelForStockEdit != null) {
        EditStockDialog(
            modelName = "${modelForStockEdit!!.productName} - ${modelForStockEdit!!.modelName}",
            currentStock = modelForStockEdit!!.stockQuantity,
            onDismiss = { modelForStockEdit = null },
            onConfirm = { newStock ->
                onUpdateStock(modelForStockEdit!!.id, newStock)
                modelForStockEdit = null
            }
        )
    }
}

@Composable
fun InventoryItemCard(
    model: ProductModelEntity,
    isAdmin: Boolean,
    numberFormat: NumberFormat,
    onOpenSale: () -> Unit,
    onEditStock: () -> Unit,
    onQuickStockChange: (delta: Int) -> Unit
) {
    val isZeroStock = model.stockQuantity == 0
    val isLowStock = model.stockQuantity in 1..2

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${model.id}"),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                when {
                    isZeroStock -> ErrorRed.copy(alpha = 0.85f)
                    isLowStock -> WarningAmber.copy(alpha = 0.85f)
                    else -> NavyCardBorder
                }
            )
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Category & Brand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${model.categoryName} • ${model.brandName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldSecondary,
                    fontWeight = FontWeight.Bold
                )

                // Large Stock Badge with clear warning icon
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isZeroStock -> ErrorRed.copy(alpha = 0.22f)
                                isLowStock -> WarningAmber.copy(alpha = 0.22f)
                                else -> SuccessGreen.copy(alpha = 0.2f)
                            }
                        )
                        .border(
                            0.8.dp,
                            when {
                                isZeroStock -> ErrorRed.copy(alpha = 0.6f)
                                isLowStock -> WarningAmber.copy(alpha = 0.7f)
                                else -> SuccessGreen.copy(alpha = 0.4f)
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isZeroStock || isLowStock) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "تحذير المخزون",
                                tint = if (isZeroStock) ErrorRed else WarningAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = when {
                                isZeroStock -> "نفد من المخزن (0)"
                                model.stockQuantity == 1 -> "متبقي قطعة واحدة فقط!"
                                model.stockQuantity == 2 -> "متبقي قطعتان (أوشك على النفاد)"
                                else -> "${model.stockQuantity} قطعة متوفرة"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isZeroStock -> ErrorRed
                                isLowStock -> WarningAmber
                                else -> SuccessGreen
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Product & Model Name
            Text(
                text = "${model.productName} - ${model.modelName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            if (model.code.isNotBlank()) {
                Text(
                    text = "كود: ${model.code}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            // Visual Low Stock Alert Strip inside card
            if (isZeroStock || isLowStock) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isZeroStock) ErrorRed.copy(alpha = 0.12f) else WarningAmber.copy(alpha = 0.12f))
                        .border(
                            0.6.dp,
                            if (isZeroStock) ErrorRed.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isZeroStock) ErrorRed else WarningAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isZeroStock) {
                            "تنبيه: هذا الموديل نفد بالكامل من المخزون، يرجى طلب شحنة توريد جديدة."
                        } else {
                            "تنبيه اقتراب النفاد: الكمية المتبقية حرجة (${model.stockQuantity} قطعة فقط بالمخزن)."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isZeroStock) ErrorRed else WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "سعر البيع النهائي:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "${numberFormat.format(model.finalPrice)} ج.م",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "الحد الأدنى للبيع:", style = MaterialTheme.typography.labelSmall, color = GoldSecondary)
                    Text(
                        text = "${numberFormat.format(model.minAllowedPrice)} ج.م",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sale Button
                Button(
                    onClick = onOpenSale,
                    enabled = model.stockQuantity > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (model.stockQuantity > 0) "بيع فوري" else "نفد من المخزن",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // If Admin: Quick Stock Controls
                if (isAdmin) {
                    Spacer(modifier = Modifier.width(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = { onQuickStockChange(-1) },
                            enabled = model.stockQuantity > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تقليل رصيد المخزن 1")
                        }

                        IconButton(
                            onClick = onEditStock,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل رصيد المخزن", tint = GoldSecondary)
                        }

                        FilledTonalIconButton(
                            onClick = { onQuickStockChange(1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة رصيد المخزن 1")
                        }
                    }
                }
            }
        }
    }
}
