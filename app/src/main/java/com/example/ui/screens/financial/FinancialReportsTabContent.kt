package com.example.ui.screens.financial

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.local.FinancialAdjustmentEntity
import com.example.data.repository.FinancialSummaryReport
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun FinancialReportsTabContent(
    isAdmin: Boolean,
    report: FinancialSummaryReport,
    adjustments: List<FinancialAdjustmentEntity>,
    onFilterPeriod: (startDate: Long?, endDate: Long?) -> Unit,
    onAdjustmentClick: () -> Unit,
    onExportCsv: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG")) }

    var selectedPeriod by remember { mutableStateOf("ALL") }

    LaunchedEffect(selectedPeriod) {
        val now = Calendar.getInstance()
        when (selectedPeriod) {
            "TODAY" -> {
                now.set(Calendar.HOUR_OF_DAY, 0)
                now.set(Calendar.MINUTE, 0)
                now.set(Calendar.SECOND, 0)
                now.set(Calendar.MILLISECOND, 0)
                onFilterPeriod(now.timeInMillis, System.currentTimeMillis())
            }
            "WEEK" -> {
                now.add(Calendar.DAY_OF_YEAR, -7)
                onFilterPeriod(now.timeInMillis, System.currentTimeMillis())
            }
            "MONTH" -> {
                now.set(Calendar.DAY_OF_MONTH, 1)
                now.set(Calendar.HOUR_OF_DAY, 0)
                now.set(Calendar.MINUTE, 0)
                now.set(Calendar.SECOND, 0)
                onFilterPeriod(now.timeInMillis, System.currentTimeMillis())
            }
            "YEAR" -> {
                now.set(Calendar.DAY_OF_YEAR, 1)
                now.set(Calendar.HOUR_OF_DAY, 0)
                now.set(Calendar.MINUTE, 0)
                now.set(Calendar.SECOND, 0)
                onFilterPeriod(now.timeInMillis, System.currentTimeMillis())
            }
            else -> onFilterPeriod(null, null)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Header & Export Section ---
        item {
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
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(28.dp))
                            Text("التقارير المالية والتدفقات النقدية", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }

                        Button(
                            onClick = onExportCsv,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصدير التقرير CSV", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Period Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val periods = listOf(
                            "ALL" to "جميع الفترات",
                            "TODAY" to "اليوم",
                            "WEEK" to "آخر 7 أيام",
                            "MONTH" to "الشهر الحالي",
                            "YEAR" to "العام الحالي"
                        )
                        periods.forEach { (key, label) ->
                            FilterChip(
                                selected = selectedPeriod == key,
                                onClick = { selectedPeriod = key },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    if (isAdmin) {
                        OutlinedButton(
                            onClick = onAdjustmentClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إجراء تسوية مالية معتمدة للحسابات أو الخزينة", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // --- 1. Cash Flow Statement (حركة التدفقات النقدية للخزينة) ---
        item {
            ReportSectionCard(
                title = "1. التدفقات النقدية للخزينة (Cash Flow)",
                icon = Icons.Default.AccountBalance,
                iconTint = GoldPrimary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportMetricRow(
                        label = "مقبوضات الخزينة (التدفقات الداخلة +)",
                        value = "+${numberFormat.format(report.totalCashIn)} ج.م",
                        valueColor = SuccessGreen
                    )
                    ReportMetricRow(
                        label = "مدفوعات الخزينة (التدفقات الخارجة -)",
                        value = "-${numberFormat.format(report.totalCashOut)} ج.م",
                        valueColor = ErrorRed
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NavyCardBorder)
                    )
                    ReportMetricRow(
                        label = "صافي التدفق النقدي للفترة",
                        value = "${if (report.netCashFlow >= 0) "+" else ""}${numberFormat.format(report.netCashFlow)} ج.م",
                        valueColor = if (report.netCashFlow >= 0) SuccessGreen else ErrorRed,
                        isBold = true
                    )
                    ReportMetricRow(
                        label = "رصيد الخزينة النقدية الحالي الفعلي",
                        value = "${numberFormat.format(report.treasuryCashBalance)} ج.م",
                        valueColor = GoldPrimary,
                        isBold = true
                    )
                }
            }
        }

        // --- 2. Sales Summary (المبيعات النقدية والآجلة) ---
        item {
            ReportSectionCard(
                title = "2. المبيعات (نقدية وآجلة)",
                icon = Icons.Default.MonetizationOn,
                iconTint = ElectricBlue
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportMetricRow(
                        label = "المبيعات النقدية (نقد الخزينة)",
                        value = "${numberFormat.format(report.totalCashSales)} ج.م",
                        valueColor = SuccessGreen
                    )
                    ReportMetricRow(
                        label = "المبيعات الآجلة (ديون في ذمة العملاء)",
                        value = "${numberFormat.format(report.totalCreditSales)} ج.م",
                        valueColor = ElectricBlue
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NavyCardBorder)
                    )
                    ReportMetricRow(
                        label = "إجمالي المبيعات الكلي للفترة",
                        value = "${numberFormat.format(report.totalSales)} ج.م",
                        valueColor = TextWhite,
                        isBold = true
                    )
                }
            }
        }

        // --- 3. Receivables & Payables (الديون والمستحقات) ---
        item {
            ReportSectionCard(
                title = "3. المستحقات والديون (أصول والتزامات)",
                icon = Icons.Default.People,
                iconTint = GoldPrimary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportMetricRow(
                        label = "مستحقات المعرض لدى العملاء (آجل)",
                        value = "${numberFormat.format(report.totalCustomerReceivables)} ج.م",
                        valueColor = ElectricBlue
                    )
                    ReportMetricRow(
                        label = "مستحقات المعرض لدى التجار (بضائع آجلة)",
                        value = "${numberFormat.format(report.totalMerchantReceivables)} ج.م",
                        valueColor = ElectricBlue
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NavyCardBorder)
                    )
                    ReportMetricRow(
                        label = "إجمالي ديون السوق لصالح المعرض (أصول متداولة)",
                        value = "${numberFormat.format(report.totalCustomerReceivables + report.totalMerchantReceivables)} ج.م",
                        valueColor = SuccessGreen,
                        isBold = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ReportMetricRow(
                        label = "إجمالي مستحقات الموردين (ديون واجبة السداد علينا)",
                        value = "${numberFormat.format(report.totalSupplierPayables)} ج.م",
                        valueColor = ErrorRed,
                        isBold = true
                    )
                }
            }
        }

        // --- 4. Estimated Profitability (الأرباح التقديرية) ---
        item {
            ReportSectionCard(
                title = "4. الأرباح التقديرية",
                icon = if (report.estimatedNetProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                iconTint = if (report.estimatedNetProfit >= 0) SuccessGreen else ErrorRed
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportMetricRow(
                        label = "إجمالي قيمة المبيعات",
                        value = "${numberFormat.format(report.totalSales)} ج.م",
                        valueColor = TextWhite
                    )
                    ReportMetricRow(
                        label = "مجمل الربح التقديري (المبيعات - سعر التكلفة)",
                        value = "${numberFormat.format(report.estimatedGrossProfit)} ج.م",
                        valueColor = if (report.estimatedGrossProfit >= 0) SuccessGreen else ErrorRed
                    )
                    ReportMetricRow(
                        label = "إجمالي المصروفات الإدارية والتشغيلية",
                        value = "-${numberFormat.format(report.totalExpenses)} ج.م",
                        valueColor = ErrorRed
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NavyCardBorder)
                    )
                    ReportMetricRow(
                        label = "صافي الربح التقديري بعد المصروفات",
                        value = "${if (report.estimatedNetProfit >= 0) "+" else ""}${numberFormat.format(report.estimatedNetProfit)} ج.م",
                        valueColor = if (report.estimatedNetProfit >= 0) SuccessGreen else ErrorRed,
                        isBold = true
                    )
                }
            }
        }

        // --- 5. Expenses Breakdown by Category ---
        if (report.expensesByCategory.isNotEmpty()) {
            item {
                ReportSectionCard(
                    title = "5. توزيع المصروفات حسب البند",
                    icon = Icons.Default.Assessment,
                    iconTint = ErrorRed
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        report.expensesByCategory.forEach { (cat, amount) ->
                            val pct = if (report.totalExpenses > 0) ((amount / report.totalExpenses) * 100).toInt() else 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$cat ($pct%)", color = TextWhite, fontSize = 12.sp)
                                Text("${numberFormat.format(amount)} ج.م", color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Recent Financial Adjustments History ---
        if (adjustments.isNotEmpty()) {
            item {
                ReportSectionCard(
                    title = "6. سجل التسويات المالية المعتمدة (${adjustments.size})",
                    icon = Icons.Default.Tune,
                    iconTint = GoldPrimary
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        adjustments.take(10).forEach { adj ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavyDarkBackground),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${adj.targetAccountName} (${if (adj.adjustmentType == "CREDIT") "إضافة +" else "خصم -"})", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${numberFormat.format(adj.amount)} ج.م", color = if (adj.adjustmentType == "CREDIT") SuccessGreen else ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Text("السبب: ${adj.reason}", color = TextWhite, fontSize = 11.sp)
                                    Text("الرصيد: ${numberFormat.format(adj.balanceBefore)} -> ${numberFormat.format(adj.balanceAfter)} ج.م • ${dateFormat.format(Date(adj.timestamp))} • المسؤول: ${adj.approvedByUserName}", color = TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            content()
        }
    }
}

@Composable
private fun ReportMetricRow(
    label: String,
    value: String,
    valueColor: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isBold) TextWhite else TextMuted,
            fontSize = if (isBold) 13.sp else 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = if (isBold) 15.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold
        )
    }
}
