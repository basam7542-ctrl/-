package com.example.ui.screens.financial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TreasuryTransactionEntity
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDarkBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TreasuryTabContent(
    isAdmin: Boolean,
    transactions: List<TreasuryTransactionEntity>,
    onRecordTransactionClick: (initialType: String) -> Unit,
    onAdjustmentClick: () -> Unit,
    onReverseClick: (transactionId: Long, referenceText: String) -> Unit,
    onExportCsv: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG")) }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val activeTxs = transactions.filter { !it.isReversed }
    val currentBalance = transactions.firstOrNull { !it.isReversed }?.balanceAfter ?: 0.0
    val totalCashIn = activeTxs.filter { it.type == "CASH_IN" }.sumOf { it.amount }
    val totalCashOut = activeTxs.filter { it.type == "CASH_OUT" }.sumOf { it.amount }

    val filteredList = when (selectedFilter) {
        "CASH_IN" -> transactions.filter { it.type == "CASH_IN" }
        "CASH_OUT" -> transactions.filter { it.type == "CASH_OUT" }
        "SALE_CASH" -> transactions.filter { it.category == "SALE_CASH" }
        "SUPPLIER_PAYMENT" -> transactions.filter { it.category == "SUPPLIER_PAYMENT" }
        "COLLECTIONS" -> transactions.filter { it.category == "CUSTOMER_COLLECTION" || it.category == "MERCHANT_COLLECTION" }
        "EXPENSE" -> transactions.filter { it.category == "EXPENSE" }
        "RETURN_REFUND" -> transactions.filter { it.category == "RETURN_REFUND" }
        "ADJUSTMENT" -> transactions.filter { it.category == "ADJUSTMENT" }
        else -> transactions
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- Live Cash Treasury Header Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "الخزينة النقدية (الدرج)",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onExportCsv,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصدير CSV", fontSize = 12.sp)
                    }
                }

                // Balance Display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyDarkBackground, RoundedCornerShape(8.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("الرصيد النقدي المتوفر حالياً", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${numberFormat.format(currentBalance)} ج.م",
                        color = GoldPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                }

                // Stats: Cash In vs Cash Out
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NavyDarkBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إجمالي المقبوضات (وارد)", color = TextMuted, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${numberFormat.format(totalCashIn)} ج.م",
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NavyDarkBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إجمالي المدفوعات (صادر)", color = TextMuted, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "-${numberFormat.format(totalCashOut)} ج.م",
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Action Buttons (Admin only for manual modifications)
                if (isAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onRecordTransactionClick("CASH_IN") },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مقبوضات", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onRecordTransactionClick("CASH_OUT") },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مدفوعات", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onAdjustmentClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسوية", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- Filter Chips ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filterOptions = listOf(
                "ALL" to "الكل (${transactions.size})",
                "CASH_IN" to "المقبوضات (الوارد)",
                "CASH_OUT" to "المدفوعات (المنصرف)",
                "SALE_CASH" to "مبيعات نقدية",
                "COLLECTIONS" to "تحصيلات عملاء/تجار",
                "SUPPLIER_PAYMENT" to "سداد موردين",
                "EXPENSE" to "مصروفات",
                "RETURN_REFUND" to "مرتجعات",
                "ADJUSTMENT" to "تسويات"
            )

            filterOptions.forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // --- Transactions Ledger List ---
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد حركات نقدية مسجلة في الخزينة لهذا الفلتر", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { tx ->
                    TreasuryTransactionItemCard(
                        tx = tx,
                        isAdmin = isAdmin,
                        numberFormat = numberFormat,
                        dateFormat = dateFormat,
                        onReverse = { onReverseClick(tx.id, "#${tx.id} - ${tx.description}") }
                    )
                }
            }
        }
    }
}

@Composable
fun TreasuryTransactionItemCard(
    tx: TreasuryTransactionEntity,
    isAdmin: Boolean,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onReverse: () -> Unit
) {
    val isCashIn = tx.type == "CASH_IN"
    val categoryArabic = when (tx.category) {
        "SALE_CASH" -> "مبيعات نقدية"
        "CUSTOMER_COLLECTION" -> "تحصيل من عميل"
        "MERCHANT_COLLECTION" -> "تحصيل من تاجر"
        "SUPPLIER_PAYMENT" -> "سداد دفعة لمورد"
        "EXPENSE" -> "مصروفات"
        "RETURN_REFUND" -> "مرتجعات"
        "ADJUSTMENT" -> "تسوية خزينة"
        "CAPITAL_INJECTION" -> "توريد رأس مال"
        "TRANSFER" -> "تحويل أموال"
        else -> tx.category
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (tx.isReversed) Modifier.border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (tx.isReversed) NavyDarkBackground else NavySurface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header Row: Type badge + Amount + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isCashIn) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isCashIn) "مقبوضات +" else "مدفوعات -",
                            color = if (isCashIn) SuccessGreen else ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = categoryArabic,
                        color = GoldPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )

                    if (tx.partyName.isNotBlank()) {
                        Text(
                            text = "• ${tx.partyName}",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "${if (isCashIn) "+" else "-"}${numberFormat.format(tx.amount)} ج.م",
                    color = if (isCashIn) SuccessGreen else ErrorRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            // Description & Reference
            if (tx.description.isNotBlank()) {
                Text(
                    text = tx.description,
                    color = TextWhite,
                    fontSize = 12.sp
                )
            }

            // Footer Row: Balance after movement + Date + User + Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الرصيد بعد الحركة: ${numberFormat.format(tx.balanceAfter)} ج.م",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${dateFormat.format(Date(tx.timestamp))} • المسؤول: ${tx.userName}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                if (tx.isReversed) {
                    Box(
                        modifier = Modifier
                            .background(ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ملغاة بحركة عكسية: ${tx.reversalNotes}",
                            color = ErrorRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (isAdmin) {
                    OutlinedButton(
                        onClick = onReverse,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حركة عكسية", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
