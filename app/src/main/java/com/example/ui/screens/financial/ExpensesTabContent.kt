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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.data.local.ExpenseCategoryEntity
import com.example.data.local.ExpenseEntity
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDarkBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpensesTabContent(
    isAdmin: Boolean,
    expenses: List<ExpenseEntity>,
    categories: List<ExpenseCategoryEntity>,
    onRecordExpenseClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    onReverseExpenseClick: (expenseId: Long, expenseNumber: String) -> Unit,
    onExportCsv: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale("ar", "EG")) }

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val activeExpenses = expenses.filter { !it.isReversed }
    val totalAmount = activeExpenses.sumOf { it.amount }

    val filteredList = if (selectedCategoryFilter == "ALL") {
        expenses
    } else {
        expenses.filter { it.categoryName == selectedCategoryFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- Header Card ---
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
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "المصروفات الإدارية والتشغيلية",
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

                // Summary Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyDarkBackground, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("إجمالي المصروفات المعتمدة", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "${numberFormat.format(totalAmount)} ج.م",
                            color = ErrorRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(NavyCardBorder)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("عدد المصروفات المسجلة", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "${activeExpenses.size} إيصال",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Action Buttons
                if (isAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRecordExpenseClick,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسجيل مصروف جديد", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onManageCategoriesClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("بنود المصروفات", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- Category Filter Chips ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategoryFilter == "ALL",
                onClick = { selectedCategoryFilter = "ALL" },
                label = { Text("الكل (${expenses.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = Color.Black
                )
            )

            categories.forEach { cat ->
                val count = expenses.count { it.categoryName == cat.name }
                FilterChip(
                    selected = selectedCategoryFilter == cat.name,
                    onClick = { selectedCategoryFilter = cat.name },
                    label = { Text("${cat.name} ($count)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // --- Expenses List ---
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد مصروفات مسجلة ضمن هذا التصنيف", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        isAdmin = isAdmin,
                        numberFormat = numberFormat,
                        dateFormat = dateFormat,
                        onReverse = { onReverseExpenseClick(expense.id, expense.expenseNumber) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: ExpenseEntity,
    isAdmin: Boolean,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onReverse: () -> Unit
) {
    val payMethodArabic = when (expense.paymentMethod) {
        "CASH_TREASURY" -> "نقدًا من الخزينة"
        "BANK" -> "تحويل بنكي"
        "WALLET" -> "محفظة إلكترونية"
        else -> expense.paymentMethod
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (expense.isReversed) Modifier.border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (expense.isReversed) NavyDarkBackground else NavySurface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header Row: Category Badge + Amount
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
                            .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = expense.categoryName,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = payMethodArabic,
                            color = ElectricBlue,
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = "${numberFormat.format(expense.amount)} ج.م",
                    color = ErrorRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            // Description & Beneficiary
            Text(
                text = expense.description,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            if (expense.beneficiary.isNotBlank()) {
                Text(
                    text = "الجهة المستفيدة: ${expense.beneficiary}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            // Footer Row: Metadata + Reverse action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dateFormat.format(Date(expense.date))} • ${expense.expenseNumber} • المسؤول: ${expense.createdByUserName}",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                if (expense.isReversed) {
                    Box(
                        modifier = Modifier
                            .background(ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ملغى بحركة عكسية", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                        Text("إلغاء بحركة عكسية", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
