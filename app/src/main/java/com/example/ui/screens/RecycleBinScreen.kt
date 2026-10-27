package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecycleBinItemEntity
import com.example.data.local.UserAccountEntity
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GlassBorder
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecycleBinScreen(
    currentUser: UserAccountEntity,
    recycleBinItems: List<RecycleBinItemEntity>,
    onNavigateBack: () -> Unit,
    onRestoreItem: (RecycleBinItemEntity) -> Unit,
    onRestoreAll: () -> Unit,
    onPermanentDeleteItem: (RecycleBinItemEntity) -> Unit,
    onEmptyBin: () -> Unit
) {
    val isAdmin = currentUser.role == "ADMIN"
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    // Dialog states
    var itemToRestore by remember { mutableStateOf<RecycleBinItemEntity?>(null) }
    var itemToDeletePermanently by remember { mutableStateOf<RecycleBinItemEntity?>(null) }
    var showRestoreAllDialog by remember { mutableStateOf(false) }
    var showEmptyBinDialog by remember { mutableStateOf(false) }

    // Filter items
    val filteredItems = remember(recycleBinItems, searchQuery, selectedCategoryFilter) {
        recycleBinItems.filter { item ->
            val matchesCategory = when (selectedCategoryFilter) {
                "MODELS" -> item.itemType == "MODEL" || item.itemType == "PRODUCT"
                "CATALOG" -> item.itemType == "CATEGORY" || item.itemType == "BRAND"
                "SUPPLIERS" -> item.itemType == "SUPPLIER"
                "ACCOUNTS" -> item.itemType == "MERCHANT" || item.itemType == "CUSTOMER"
                "TX" -> item.itemType.endsWith("_TX")
                else -> true
            }

            val query = searchQuery.trim().lowercase(Locale.getDefault())
            val matchesSearch = query.isEmpty() ||
                item.title.lowercase(Locale.getDefault()).contains(query) ||
                item.subtitle.lowercase(Locale.getDefault()).contains(query) ||
                item.deletedBy.lowercase(Locale.getDefault()).contains(query)

            matchesCategory && matchesSearch
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDarkBackground)
            .padding(16.dp)
            .testTag("recycle_bin_screen")
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NavySurfaceElevated)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = GoldPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Recycling,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سلة المحذوفات",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricBlueContainer)
                            .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${recycleBinItems.size} عناصر",
                            color = ElectricBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "نظام استعادة آمن لجميع السجلات والبيانات المحذوفة",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bulk Actions Bar (Restore All & Empty Bin)
        if (recycleBinItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showRestoreAllDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("restore_all_button")
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("استعادة الكل", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (isAdmin) {
                    OutlinedButton(
                        onClick = { showEmptyBinDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("empty_recycle_bin_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إفراغ السلة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في المحذوفات بالاسم أو الموديل أو من قام بالحذف...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextMuted)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NavySurface,
                unfocusedContainerColor = NavySurface,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = NavyCardBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recycle_bin_search_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf(
                "ALL" to "الكل (${recycleBinItems.size})",
                "MODELS" to "الأجهزة والموديلات",
                "CATALOG" to "الأقسام والشركات",
                "SUPPLIERS" to "الموردين",
                "ACCOUNTS" to "التجار والآجل",
                "TX" to "الحركات المالية"
            )

            items(filters) { (key, label) ->
                val isSelected = selectedCategoryFilter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) GoldPrimary else NavySurfaceVariant)
                        .clickable { selectedCategoryFilter = key }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) TextDark else TextWhite,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // List of Deleted Items
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(NavySurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Recycling,
                                contentDescription = null,
                                tint = if (recycleBinItems.isEmpty()) SuccessGreen else TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (recycleBinItems.isEmpty()) "سلة المحذوفات فارغة تماماً" else "لا توجد نتائج مطابقة للبحث",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (recycleBinItems.isEmpty())
                                "كافة عناصر وموديلات وحسابات المحل تعمل بشكل نشط، ولن تفقد أي بيانات في حال الحذف."
                            else
                                "جرب تعديل كلمة البحث أو تغيير الفلتر لعرض العناصر المطلوبة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    RecycleBinItemCard(
                        item = item,
                        isAdmin = isAdmin,
                        formattedDate = dateFormat.format(Date(item.deletedAt)),
                        onRestore = { itemToRestore = item },
                        onPermanentDelete = { itemToDeletePermanently = item }
                    )
                }
            }
        }
    }

    // --- Confirmation Dialogs ---

    // Confirm Single Item Restore
    if (itemToRestore != null) {
        val target = itemToRestore!!
        AlertDialog(
            onDismissRequest = { itemToRestore = null },
            icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(32.dp)) },
            title = { Text("استعادة العنصر", fontWeight = FontWeight.Bold, color = TextWhite) },
            text = {
                Text(
                    "هل ترغب في استعادة «${target.title}» إلى مكانه الأصلي في التطبيق بكامل بياناته وعلاقاته؟",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreItem(target)
                        itemToRestore = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                ) {
                    Text("استعادة الآن", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToRestore = null }) {
                    Text("إلغاء", color = TextWhite)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Confirm Single Item Permanent Delete (Admin Only)
    if (itemToDeletePermanently != null) {
        val target = itemToDeletePermanently!!
        AlertDialog(
            onDismissRequest = { itemToDeletePermanently = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(32.dp)) },
            title = { Text("حذف نهائي لا رجعة فيه", fontWeight = FontWeight.Bold, color = ErrorRed) },
            text = {
                Column {
                    Text(
                        "أنت على وشك حذف «${target.title}» نهائياً من قاعدة البيانات.",
                        color = TextWhite,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "تنبيه: لن تتمكن من استرجاع هذا السجل أو بياناته بعد تنفيذ هذا الإجراء.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPermanentDeleteItem(target)
                        itemToDeletePermanently = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Text("حذف نهائي", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDeletePermanently = null }) {
                    Text("إلغاء", color = TextWhite)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Confirm Restore All
    if (showRestoreAllDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreAllDialog = false },
            icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(32.dp)) },
            title = { Text("استعادة كافة المحذوفات", fontWeight = FontWeight.Bold, color = TextWhite) },
            text = {
                Text(
                    "هل أنت متأكد من استعادة جميع العناصر الموجودة في سلة المحذوفات (${recycleBinItems.size} عنصر) إلى أماكنها الأصلية؟",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreAll()
                        showRestoreAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                ) {
                    Text("استعادة الكل", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreAllDialog = false }) {
                    Text("إلغاء", color = TextWhite)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Confirm Empty Bin (Admin Only)
    if (showEmptyBinDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyBinDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(36.dp)) },
            title = { Text("إفراغ سلة المحذوفات بالكامل", fontWeight = FontWeight.Bold, color = ErrorRed) },
            text = {
                Column {
                    Text(
                        "تحذير فائق: سيتم مسح جميع العناصر المحذوفة نهائياً (${recycleBinItems.size} عنصر) ولا يمكن التراجع عن ذلك.",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "هل أنت متأكد تماماً من إفراغ السلة الآن؟",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEmptyBin()
                        showEmptyBinDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Text("إفراغ نهائي", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEmptyBinDialog = false }) {
                    Text("تراجع", color = TextWhite)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun RecycleBinItemCard(
    item: RecycleBinItemEntity,
    isAdmin: Boolean,
    formattedDate: String,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
) {
    val (typeLabel, typeColor, typeIcon) = when (item.itemType) {
        "MODEL" -> Triple("موديل جهاز", ElectricBlue, Icons.Default.Devices)
        "PRODUCT" -> Triple("منتج شامل", ElectricBlue, Icons.Default.Inventory2)
        "BRAND" -> Triple("شركة مصنعة", GoldSecondary, Icons.Default.Verified)
        "CATEGORY" -> Triple("قسم رئيسي", GoldPrimary, Icons.Default.GridView)
        "SUPPLIER" -> Triple("شركة موردة", WarningAmber, Icons.Default.LocalShipping)
        "MERCHANT" -> Triple("تاجر جملة", GoldPrimary, Icons.Default.Handshake)
        "CUSTOMER" -> Triple("آجل بالخارج", SuccessGreen, Icons.Default.Person)
        "SUPPLIER_TX" -> Triple("حركة مورد", WarningAmber, Icons.Default.ReceiptLong)
        "MERCHANT_TX" -> Triple("حركة تاجر", GoldSecondary, Icons.Default.ReceiptLong)
        "CUSTOMER_TX" -> Triple("حركة آجل", SuccessGreen, Icons.Default.ReceiptLong)
        else -> Triple("عنصر عام", ElectricBlue, Icons.Default.Recycling)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recycle_bin_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type Badge + Deletion Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type Pill Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeColor.copy(alpha = 0.15f))
                        .border(1.dp, typeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = typeLabel, color = typeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Date
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Item Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 15.sp
            )

            // Subtitle / Details
            if (item.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Deleted By user chip
            if (item.deletedBy.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "قام بالحذف: ${item.deletedBy}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NavySurfaceElevated)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Restore Button
                Button(
                    onClick = onRestore,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("استعادة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Permanent Delete Button (Admin Only)
                if (isAdmin) {
                    OutlinedButton(
                        onClick = onPermanentDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حذف نهائي", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "الحذف النهائي للمدير فقط",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
