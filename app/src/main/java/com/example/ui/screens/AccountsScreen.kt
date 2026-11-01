package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import com.example.data.local.ExpenseCategoryEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.FinancialAdjustmentEntity
import com.example.data.local.TreasuryTransactionEntity
import com.example.data.repository.FinancialSummaryReport
import com.example.ui.screens.financial.ExpensesTabContent
import com.example.ui.screens.financial.FinancialReportsTabContent
import com.example.ui.screens.financial.ManageExpenseCategoriesDialog
import com.example.ui.screens.financial.RecordExpenseDialog
import com.example.ui.screens.financial.RecordFinancialAdjustmentDialog
import com.example.ui.screens.financial.RecordTreasuryTxDialog
import com.example.ui.screens.financial.ReverseTransactionDialog
import com.example.ui.screens.financial.TreasuryTabContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomerAccountEntity
import com.example.data.local.CustomerTransactionEntity
import com.example.data.local.MerchantAccountEntity
import com.example.data.local.MerchantTransactionEntity
import com.example.data.local.SupplierAccountEntity
import com.example.data.local.SupplierTransactionEntity
import com.example.data.local.UserAccountEntity
import com.example.ui.components.AddCustomerDialog
import com.example.ui.components.AddCustomerTransactionDialog
import com.example.ui.components.AddMerchantDialog
import com.example.ui.components.AddMerchantTransactionDialog
import com.example.ui.components.AddSupplierDialog
import com.example.ui.components.AddSupplierTransactionDialog
import com.example.ui.components.EditCustomerTransactionDialog
import com.example.ui.components.EditMerchantTransactionDialog
import com.example.ui.components.EditSupplierTransactionDialog
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldContainer
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
import com.example.utils.WhatsAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountsScreen(
    currentUser: UserAccountEntity,
    suppliers: List<SupplierAccountEntity>,
    merchants: List<MerchantAccountEntity>,
    customers: List<CustomerAccountEntity> = emptyList(),
    treasuryTransactions: List<TreasuryTransactionEntity> = emptyList(),
    expenseCategories: List<ExpenseCategoryEntity> = emptyList(),
    expenses: List<ExpenseEntity> = emptyList(),
    financialAdjustments: List<FinancialAdjustmentEntity> = emptyList(),
    financialSummary: FinancialSummaryReport = FinancialSummaryReport(),
    onAddSupplier: (name: String, phone: String, initialPurchases: Double, notes: String) -> Unit,
    onAddSupplierTransaction: (supplierId: Long, type: String, amount: Double, notes: String) -> Unit,
    onUpdateSupplierTransaction: (transactionId: Long, type: String, amount: Double, notes: String) -> Unit,
    onDeleteSupplierTransaction: (transactionId: Long) -> Unit,
    onDeleteSupplier: (supplierId: Long) -> Unit,
    onAddMerchant: (name: String, phone: String, initialDelivered: Double, notes: String) -> Unit,
    onAddMerchantTransaction: (merchantId: Long, type: String, amount: Double, notes: String) -> Unit,
    onUpdateMerchantTransaction: (transactionId: Long, type: String, amount: Double, notes: String) -> Unit,
    onDeleteMerchantTransaction: (transactionId: Long) -> Unit,
    onDeleteMerchant: (merchantId: Long) -> Unit,
    onAddCustomer: (name: String, phone: String, initialCreditSales: Double, notes: String) -> Unit = { _, _, _, _ -> },
    onAddCustomerTransaction: (customerId: Long, type: String, amount: Double, productDescription: String, notes: String) -> Unit = { _, _, _, _, _ -> },
    onUpdateCustomerTransaction: (transactionId: Long, type: String, amount: Double, productDescription: String, notes: String) -> Unit = { _, _, _, _, _ -> },
    onDeleteCustomerTransaction: (transactionId: Long) -> Unit = {},
    onDeleteCustomer: (customerId: Long) -> Unit = {},
    onRecordTreasuryTransaction: (type: String, category: String, amount: Double, referenceType: String, partyName: String, paymentMethod: String, description: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onReverseTreasuryTransaction: (transactionId: Long, reason: String) -> Unit = { _, _ -> },
    onRecordExpense: (categoryName: String, amount: Double, description: String, beneficiary: String, paymentMethod: String, referenceNumber: String, notes: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onReverseExpense: (expenseId: Long, reason: String) -> Unit = { _, _ -> },
    onAddExpenseCategory: (name: String, description: String) -> Unit = { _, _ -> },
    onDeleteExpenseCategory: (id: Long) -> Unit = {},
    onRecordFinancialAdjustment: (targetAccountType: String, targetAccountId: Long?, targetAccountName: String, adjustmentType: String, amount: Double, reason: String) -> Unit = { _, _, _, _, _, _ -> },
    onRefreshFinancialSummary: (startDate: Long?, endDate: Long?) -> Unit = { _, _ -> },
    onExportTreasuryLedger: () -> String = { "" },
    onExportExpenses: () -> String = { "" },
    onExportFinancialReport: () -> String = { "" },
    onShareCsv: (title: String, csvContent: String) -> Unit = { _, _ -> },
    getSupplierTransactionsFlow: (Long) -> StateFlow<List<SupplierTransactionEntity>>,
    getMerchantTransactionsFlow: (Long) -> StateFlow<List<MerchantTransactionEntity>>,
    getCustomerTransactionsFlow: (Long) -> StateFlow<List<CustomerTransactionEntity>> = { MutableStateFlow(emptyList()) }
) {
    val context = LocalContext.current
    val isAdmin = currentUser.role == "ADMIN"
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG")) }

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Suppliers, 1: Merchants, 2: Customers, 3: Treasury, 4: Expenses, 5: Reports

    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showAddMerchantDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    // Financial Dialog States
    var showRecordTreasuryDialog by remember { mutableStateOf(false) }
    var recordTxInitialType by remember { mutableStateOf("CASH_IN") }
    var showRecordExpenseDialog by remember { mutableStateOf(false) }
    var showManageExpenseCategoriesDialog by remember { mutableStateOf(false) }
    var showFinancialAdjustmentDialog by remember { mutableStateOf(false) }
    var reversingTreasuryTxId by remember { mutableStateOf<Long?>(null) }
    var reversingTreasuryTxText by remember { mutableStateOf("") }
    var reversingExpenseId by remember { mutableStateOf<Long?>(null) }
    var reversingExpenseNumber by remember { mutableStateOf("") }

    var detailedSupplierId by remember { mutableStateOf<Long?>(null) }
    var detailedMerchantId by remember { mutableStateOf<Long?>(null) }
    var detailedCustomerId by remember { mutableStateOf<Long?>(null) }

    var showAddSupplierTxDialog by remember { mutableStateOf(false) }
    var showAddMerchantTxDialog by remember { mutableStateOf(false) }
    var showAddCustomerTxDialog by remember { mutableStateOf(false) }

    // Admin Edit & Delete States for Transactions
    var editingSupplierTx by remember { mutableStateOf<SupplierTransactionEntity?>(null) }
    var deletingSupplierTx by remember { mutableStateOf<SupplierTransactionEntity?>(null) }
    var editingMerchantTx by remember { mutableStateOf<MerchantTransactionEntity?>(null) }
    var deletingMerchantTx by remember { mutableStateOf<MerchantTransactionEntity?>(null) }
    var editingCustomerTx by remember { mutableStateOf<CustomerTransactionEntity?>(null) }
    var deletingCustomerTx by remember { mutableStateOf<CustomerTransactionEntity?>(null) }

    // Admin Delete Account States (Supplier / Merchant / Customer)
    var supplierToDelete by remember { mutableStateOf<SupplierAccountEntity?>(null) }
    var merchantToDelete by remember { mutableStateOf<MerchantAccountEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerAccountEntity?>(null) }

    // Current active entity from reactive list
    val currentDetailedSupplier = suppliers.firstOrNull { it.id == detailedSupplierId }
    val currentDetailedMerchant = merchants.firstOrNull { it.id == detailedMerchantId }
    val currentDetailedCustomer = customers.firstOrNull { it.id == detailedCustomerId }

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
            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = NavySurface,
                contentColor = GoldPrimary,
                edgePadding = 6.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = GoldPrimary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "الموردين (${suppliers.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 0) GoldPrimary else TextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "التجار (${merchants.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 1) GoldPrimary else TextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            text = "العملاء (${customers.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 2) GoldPrimary else TextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = {
                        Text(
                            text = "الخزينة النقدية",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 3) GoldPrimary else TextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 4,
                    onClick = { selectedTabIndex = 4 },
                    text = {
                        Text(
                            text = "المصروفات (${expenses.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 4) GoldPrimary else TextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 5,
                    onClick = { selectedTabIndex = 5 },
                    text = {
                        Text(
                            text = "التقارير والتدفقات",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 5) GoldPrimary else TextMuted
                        )
                    }
                )
            }

            // Tab 1: SUPPLIERS
            if (selectedTabIndex == 0) {
                val totalPurchases = suppliers.sumOf { it.totalPurchases }
                val totalPaid = suppliers.sumOf { it.totalPaid }
                val totalRemainingDue = suppliers.sumOf { it.remainingBalance }

                // Specialized Executive Balance Sheet for Suppliers
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
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
                                        .size(36.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.15f))
                                    .border(1.dp, GoldPrimary, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "الموقف المالي مع الموردين",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${suppliers.size} شركة موردة مسجلة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (totalRemainingDue > 0) ErrorRed.copy(alpha = 0.18f) else SuccessGreen.copy(alpha = 0.18f))
                                    .border(
                                        0.8.dp,
                                        if (totalRemainingDue > 0) ErrorRed.copy(alpha = 0.6f) else SuccessGreen.copy(alpha = 0.6f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (totalRemainingDue > 0) "مستحقات قائمة للموردين" else "الحسابات مسددة بالكامل ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalRemainingDue > 0) ErrorRed else SuccessGreen,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Net Outstanding Balance Hero Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavySurface)
                                .border(0.8.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "صافي المبالغ المستحقة عليّ للموردين", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        text = "${numberFormat.format(totalRemainingDue)} ج.م",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (totalRemainingDue > 0) GoldPrimary else SuccessGreen
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "إجمالي المشتريات", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${numberFormat.format(totalPurchases)} ج.م",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "إجمالي المسدد", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${numberFormat.format(totalPaid)} ج.م",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Supplier Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الشركات الموردة للبضاعة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    if (isAdmin) {
                        Button(
                            onClick = { showAddSupplierDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة مورد", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(suppliers, key = { it.id }) { supplier ->
                        SupplierCard(
                            supplier = supplier,
                            numberFormat = numberFormat,
                            isAdmin = isAdmin,
                            onOpenStatement = { detailedSupplierId = supplier.id },
                            onDeleteSupplier = { supplierToDelete = supplier }
                        )
                    }
                }

            // Tab 2: MERCHANTS (تجار بالأجل)
            } else if (selectedTabIndex == 1) {
                val totalGoodsDelivered = merchants.sumOf { it.totalGoodsDelivered }
                val totalCollected = merchants.sumOf { it.totalCollected }
                val totalRemainingDueToMe = merchants.sumOf { it.remainingDueToMe }

                // Specialized Executive Balance Sheet for Merchants
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
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
                                        .size(36.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(ElectricBlue.copy(alpha = 0.15f))
                                    .border(1.dp, ElectricBlue, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Handshake, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "الموقف المالي مع تجار الجملة",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${merchants.size} تاجر جملة معتمد",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(GoldPrimary.copy(alpha = 0.15f))
                                    .border(0.8.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (totalRemainingDueToMe > 0) "مستحقات لنا طرف التجار" else "الحسابات مصفاة بالكامل ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Net Outstanding Hero Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavySurface)
                                .border(0.8.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "صافي المبالغ المستحقة لنا طرف التجار", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        text = "${numberFormat.format(totalRemainingDueToMe)} ج.م",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldPrimary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "بضاعة مسلمة", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${numberFormat.format(totalGoodsDelivered)} ج.م",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "إجمالي المحصل", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${numberFormat.format(totalCollected)} ج.م",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Merchant Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تجار الجملة بالأجل",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    if (isAdmin) {
                        Button(
                            onClick = { showAddMerchantDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة تاجر", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(merchants, key = { it.id }) { merchant ->
                        MerchantCard(
                            merchant = merchant,
                            numberFormat = numberFormat,
                            isAdmin = isAdmin,
                            onOpenStatement = { detailedMerchantId = merchant.id },
                            onDeleteMerchant = { merchantToDelete = merchant }
                        )
                    }
                }
            // Tab 3: RETAIL CUSTOMERS (عملاء التجزئة بالأجل)
            } else if (selectedTabIndex == 2) {
                val totalCreditSales = customers.sumOf { it.totalCreditSales }
                val totalCollected = customers.sumOf { it.totalCollected }
                val totalRemainingDueToMe = customers.sumOf { it.remainingDueToMe }

                // Specialized Executive Balance Sheet for Retail Customers
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
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
                                        .size(36.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(GoldContainer)
                                    .border(1.dp, GoldPrimary, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "الموقف المالي لحسابات الآجل بالخارج",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${customers.size} حساب عميل آجل مسجل",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(GoldPrimary.copy(alpha = 0.15f))
                                    .border(0.8.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (totalRemainingDueToMe > 0) "أقساط وآجل بالخارج" else "لا توجد مستحقات متأخرة ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Net Outstanding Hero Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavySurface)
                                .border(0.8.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "صافي المبالغ المستحقة لنا طرف الزبائن", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        text = "${numberFormat.format(totalRemainingDueToMe)} ج.م",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldPrimary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "مبيعات الآجل", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${numberFormat.format(totalCreditSales)} ج.م",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "إجمالي المحصل", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${numberFormat.format(totalCollected)} ج.م",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Customer Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "حسابات آجل بالخارج",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    if (isAdmin) {
                        Button(
                            onClick = { showAddCustomerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة عميل آجل", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (customers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = "لا توجد حسابات آجل بالخارج مسجلة بعد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(customers, key = { it.id }) { customer ->
                            CustomerCard(
                                customer = customer,
                                numberFormat = numberFormat,
                                isAdmin = isAdmin,
                                onOpenStatement = { detailedCustomerId = customer.id },
                                onDeleteCustomer = { customerToDelete = customer }
                            )
                        }
                    }
                }
            } else if (selectedTabIndex == 3) {
                // Tab 4: TREASURY (الخزينة النقدية)
                TreasuryTabContent(
                    isAdmin = isAdmin,
                    transactions = treasuryTransactions,
                    onRecordTransactionClick = { initialType ->
                        recordTxInitialType = initialType
                        showRecordTreasuryDialog = true
                    },
                    onAdjustmentClick = { showFinancialAdjustmentDialog = true },
                    onReverseClick = { txId, refText ->
                        reversingTreasuryTxId = txId
                        reversingTreasuryTxText = refText
                    },
                    onExportCsv = {
                        val csv = onExportTreasuryLedger()
                        onShareCsv("دفتر_الخزينة_النقدية.csv", csv)
                    }
                )
            } else if (selectedTabIndex == 4) {
                // Tab 5: EXPENSES (المصروفات)
                ExpensesTabContent(
                    isAdmin = isAdmin,
                    expenses = expenses,
                    categories = expenseCategories,
                    onRecordExpenseClick = { showRecordExpenseDialog = true },
                    onManageCategoriesClick = { showManageExpenseCategoriesDialog = true },
                    onReverseExpenseClick = { expId, expNum ->
                        reversingExpenseId = expId
                        reversingExpenseNumber = expNum
                    },
                    onExportCsv = {
                        val csv = onExportExpenses()
                        onShareCsv("سجل_المصروفات_الإدارية.csv", csv)
                    }
                )
            } else if (selectedTabIndex == 5) {
                // Tab 6: FINANCIAL REPORTS & CASH FLOW (التقارير المالية والتدفقات)
                FinancialReportsTabContent(
                    isAdmin = isAdmin,
                    report = financialSummary,
                    adjustments = financialAdjustments,
                    onFilterPeriod = { start, end -> onRefreshFinancialSummary(start, end) },
                    onAdjustmentClick = { showFinancialAdjustmentDialog = true },
                    onExportCsv = {
                        val csv = onExportFinancialReport()
                        onShareCsv("التقرير_المالي_الشامل.csv", csv)
                    }
                )
            }
        }
    }

    // Dialogs: Add Supplier & Merchant
    if (showAddSupplierDialog) {
        AddSupplierDialog(
            onDismiss = { showAddSupplierDialog = false },
            onConfirm = { name, phone, initialPurchases, notes ->
                onAddSupplier(name, phone, initialPurchases, notes)
                showAddSupplierDialog = false
            }
        )
    }

    if (showAddMerchantDialog) {
        AddMerchantDialog(
            onDismiss = { showAddMerchantDialog = false },
            onConfirm = { name, phone, initialDelivered, notes ->
                onAddMerchant(name, phone, initialDelivered, notes)
                showAddMerchantDialog = false
            }
        )
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onConfirm = { name, phone, initialCreditSales, notes ->
                onAddCustomer(name, phone, initialCreditSales, notes)
                showAddCustomerDialog = false
            }
        )
    }

    // Detailed Statement for Supplier
    if (currentDetailedSupplier != null) {
        val supplier = currentDetailedSupplier
        val txs by getSupplierTransactionsFlow(supplier.id).collectAsState()

        AlertDialog(
            onDismissRequest = { detailedSupplierId = null },
            containerColor = NavySurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "كشف حساب: ${supplier.supplierName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        if (supplier.phone.isNotBlank()) {
                            Text(
                                text = "هاتف: ${supplier.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAdmin) {
                            IconButton(
                                onClick = { supplierToDelete = supplier },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "حذف المورد بالكامل",
                                    tint = ErrorRed.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { detailedSupplierId = null },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Balance Header
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("المشتريات:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${numberFormat.format(supplier.totalPurchases)} ج.م", color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("المدفوع:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${numberFormat.format(supplier.totalPaid)} ج.م", color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("المستحق للمورد:", style = MaterialTheme.typography.labelSmall, color = ErrorRed)
                                Text("${numberFormat.format(supplier.remainingBalance)} ج.م", color = GoldPrimary, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    // WhatsApp Share Button
                    Button(
                        onClick = {
                            val message = WhatsAppHelper.buildSupplierStatementMessage(supplier, txs)
                            WhatsAppHelper.sendWhatsAppMessage(context, supplier.phone, message)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إرسال كشف الحساب عبر واتساب", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل الحركات (${txs.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        if (isAdmin) {
                            Button(
                                onClick = { showAddSupplierTxDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تسجيل حركة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (txs.isEmpty()) {
                        Text(
                            text = "لا توجد حركات مسجلة لهذا المورد بعد",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.height(260.dp)
                        ) {
                            items(txs, key = { it.id }) { tx ->
                                val isInvoice = tx.type == "INVOICE"
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (isInvoice) "توريد بضاعة" else "سداد دفعة",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isInvoice) TextWhite else SuccessGreen
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${numberFormat.format(tx.amount)} ج.م",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isInvoice) GoldPrimary else SuccessGreen
                                                )
                                            }
                                            if (tx.notes.isNotBlank()) {
                                                Text(text = tx.notes, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            }
                                            Text(
                                                text = dateFormat.format(Date(tx.timestamp)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted.copy(alpha = 0.8f)
                                            )
                                        }

                                        // Admin Edit and Delete Actions
                                        if (isAdmin) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { editingSupplierTx = tx },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "تعديل العملية",
                                                        tint = GoldPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { deletingSupplierTx = tx },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "حذف العملية",
                                                        tint = ErrorRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { detailedSupplierId = null }) { Text("إغلاق", color = TextWhite) }
            }
        )
    }

    if (showAddSupplierTxDialog && currentDetailedSupplier != null) {
        AddSupplierTransactionDialog(
            supplierName = currentDetailedSupplier.supplierName,
            onDismiss = { showAddSupplierTxDialog = false },
            onConfirm = { type, amount, notes ->
                onAddSupplierTransaction(currentDetailedSupplier.id, type, amount, notes)
                showAddSupplierTxDialog = false
            }
        )
    }

    // Detailed Statement for Merchant
    if (currentDetailedMerchant != null) {
        val merchant = currentDetailedMerchant
        val txs by getMerchantTransactionsFlow(merchant.id).collectAsState()

        AlertDialog(
            onDismissRequest = { detailedMerchantId = null },
            containerColor = NavySurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "كشف حساب: ${merchant.merchantName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        if (merchant.phone.isNotBlank()) {
                            Text(
                                text = "هاتف: ${merchant.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAdmin) {
                            IconButton(
                                onClick = { merchantToDelete = merchant },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "حذف التاجر بالكامل",
                                    tint = ErrorRed.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { detailedMerchantId = null },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Balance Header
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("البضاعة المسلمة:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${numberFormat.format(merchant.totalGoodsDelivered)} ج.م", color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("المحصل:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${numberFormat.format(merchant.totalCollected)} ج.م", color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("المستحق لي:", style = MaterialTheme.typography.labelSmall, color = GoldSecondary)
                                Text("${numberFormat.format(merchant.remainingDueToMe)} ج.م", color = GoldPrimary, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    // WhatsApp Share Button
                    Button(
                        onClick = {
                            val message = WhatsAppHelper.buildMerchantStatementMessage(merchant, txs)
                            WhatsAppHelper.sendWhatsAppMessage(context, merchant.phone, message)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إرسال كشف الحساب عبر واتساب", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل الحركات (${txs.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        if (isAdmin) {
                            Button(
                                onClick = { showAddMerchantTxDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تسجيل حركة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (txs.isEmpty()) {
                        Text(
                            text = "لا توجد حركات مسجلة لهذا التاجر بعد",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.height(260.dp)
                        ) {
                            items(txs, key = { it.id }) { tx ->
                                val isGoods = tx.type == "GOODS_DELIVERED"
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (isGoods) "تسليم بضاعة بالأجل" else "تحصيل دفعة",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isGoods) TextWhite else SuccessGreen
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${numberFormat.format(tx.amount)} ج.م",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isGoods) GoldPrimary else SuccessGreen
                                                )
                                            }
                                            if (tx.notes.isNotBlank()) {
                                                Text(text = tx.notes, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            }
                                            Text(
                                                text = dateFormat.format(Date(tx.timestamp)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted.copy(alpha = 0.8f)
                                            )
                                        }

                                        // Admin Edit and Delete Actions
                                        if (isAdmin) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { editingMerchantTx = tx },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "تعديل العملية",
                                                        tint = GoldPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { deletingMerchantTx = tx },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "حذف العملية",
                                                        tint = ErrorRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { detailedMerchantId = null }) { Text("إغلاق", color = TextWhite) }
            }
        )
    }

    if (showAddMerchantTxDialog && currentDetailedMerchant != null) {
        AddMerchantTransactionDialog(
            merchantName = currentDetailedMerchant.merchantName,
            onDismiss = { showAddMerchantTxDialog = false },
            onConfirm = { type, amount, notes ->
                onAddMerchantTransaction(currentDetailedMerchant.id, type, amount, notes)
                showAddMerchantTxDialog = false
            }
        )
    }

    // Detailed Statement for Retail Customer (آجل بالخارج)
    if (currentDetailedCustomer != null) {
        val customer = currentDetailedCustomer
        val txs by getCustomerTransactionsFlow(customer.id).collectAsState()

        AlertDialog(
            onDismissRequest = { detailedCustomerId = null },
            containerColor = NavySurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "كشف حساب: ${customer.customerName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        if (customer.phone.isNotBlank()) {
                            Text(
                                text = "هاتف: ${customer.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAdmin) {
                            IconButton(
                                onClick = { customerToDelete = customer },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "حذف الحساب بالكامل",
                                    tint = ErrorRed.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { detailedCustomerId = null },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Balance Header
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("مبيعات الآجل:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${numberFormat.format(customer.totalCreditSales)} ج.م", color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("المحصل:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${numberFormat.format(customer.totalCollected)} ج.م", color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("المستحق لي:", style = MaterialTheme.typography.labelSmall, color = GoldSecondary)
                                Text("${numberFormat.format(customer.remainingDueToMe)} ج.م", color = GoldPrimary, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    // WhatsApp Share Button
                    Button(
                        onClick = {
                            val message = WhatsAppHelper.buildCustomerStatementMessage(customer, txs)
                            WhatsAppHelper.sendWhatsAppMessage(context, customer.phone, message)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إرسال كشف الحساب عبر واتساب", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل الحركات (${txs.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        if (isAdmin) {
                            Button(
                                onClick = { showAddCustomerTxDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تسجيل حركة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (txs.isEmpty()) {
                        Text(
                            text = "لا توجد حركات مسجلة لهذا الحساب بعد",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.height(260.dp)
                        ) {
                            items(txs, key = { it.id }) { tx ->
                                val isCreditSale = tx.type == "CREDIT_SALE"
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (isCreditSale) "بيع آجل" else "تحصيل دفعة",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCreditSale) GoldPrimary else SuccessGreen
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${numberFormat.format(tx.amount)} ج.م",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCreditSale) TextWhite else SuccessGreen
                                                )
                                            }
                                            if (tx.productDescription.isNotBlank()) {
                                                Text(
                                                    text = "الجهاز: ${tx.productDescription}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = GoldSecondary
                                                )
                                            }
                                            if (tx.notes.isNotBlank()) {
                                                Text(text = tx.notes, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            }
                                            Text(
                                                text = dateFormat.format(Date(tx.timestamp)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted.copy(alpha = 0.8f)
                                            )
                                        }

                                        if (isAdmin) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { editingCustomerTx = tx },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "تعديل العملية",
                                                        tint = GoldPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { deletingCustomerTx = tx },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "حذف العملية",
                                                        tint = ErrorRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { detailedCustomerId = null }) { Text("إغلاق", color = TextWhite) }
            }
        )
    }

    if (showAddCustomerTxDialog && currentDetailedCustomer != null) {
        AddCustomerTransactionDialog(
            customerName = currentDetailedCustomer.customerName,
            onDismiss = { showAddCustomerTxDialog = false },
            onConfirm = { type, amount, productDescription, notes ->
                onAddCustomerTransaction(currentDetailedCustomer.id, type, amount, productDescription, notes)
                showAddCustomerTxDialog = false
            }
        )
    }

    // --- Admin Edit Transaction Dialogs ---
    if (editingSupplierTx != null && currentDetailedSupplier != null) {
        val tx = editingSupplierTx!!
        EditSupplierTransactionDialog(
            supplierName = currentDetailedSupplier.supplierName,
            initialType = tx.type,
            initialAmount = tx.amount,
            initialNotes = tx.notes,
            onDismiss = { editingSupplierTx = null },
            onConfirm = { type, amount, notes ->
                onUpdateSupplierTransaction(tx.id, type, amount, notes)
                editingSupplierTx = null
            }
        )
    }

    if (editingMerchantTx != null && currentDetailedMerchant != null) {
        val tx = editingMerchantTx!!
        EditMerchantTransactionDialog(
            merchantName = currentDetailedMerchant.merchantName,
            initialType = tx.type,
            initialAmount = tx.amount,
            initialNotes = tx.notes,
            onDismiss = { editingMerchantTx = null },
            onConfirm = { type, amount, notes ->
                onUpdateMerchantTransaction(tx.id, type, amount, notes)
                editingMerchantTx = null
            }
        )
    }

    if (editingCustomerTx != null && currentDetailedCustomer != null) {
        val tx = editingCustomerTx!!
        EditCustomerTransactionDialog(
            customerName = currentDetailedCustomer.customerName,
            initialType = tx.type,
            initialAmount = tx.amount,
            initialProductDescription = tx.productDescription,
            initialNotes = tx.notes,
            onDismiss = { editingCustomerTx = null },
            onConfirm = { type, amount, productDescription, notes ->
                onUpdateCustomerTransaction(tx.id, type, amount, productDescription, notes)
                editingCustomerTx = null
            }
        )
    }

    // --- Admin Delete Transaction Confirmations ---
    if (deletingSupplierTx != null) {
        val tx = deletingSupplierTx!!
        AlertDialog(
            onDismissRequest = { deletingSupplierTx = null },
            containerColor = NavySurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("تأكيد حذف الحركة", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف حركة \"${if (tx.type == "INVOICE") "توريد بضاعة" else "سداد دفعة"}\" بمبلغ ${numberFormat.format(tx.amount)} ج.م؟ سيتم تعديل رصيد حساب المورد تلقائياً.",
                    color = TextWhite,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSupplierTransaction(tx.id)
                        deletingSupplierTx = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Text("حذف", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingSupplierTx = null }) {
                    Text("إلغاء", color = TextWhite)
                }
            }
        )
    }

    if (deletingMerchantTx != null) {
        val tx = deletingMerchantTx!!
        AlertDialog(
            onDismissRequest = { deletingMerchantTx = null },
            containerColor = NavySurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("تأكيد حذف الحركة", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف حركة \"${if (tx.type == "GOODS_DELIVERED") "تسليم بضاعة" else "تحصيل دفعة"}\" بمبلغ ${numberFormat.format(tx.amount)} ج.م؟ سيتم تعديل رصيد حساب التاجر تلقائياً.",
                    color = TextWhite,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMerchantTransaction(tx.id)
                        deletingMerchantTx = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Text("حذف", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingMerchantTx = null }) {
                    Text("إلغاء", color = TextWhite)
                }
            }
        )
    }

    if (deletingCustomerTx != null) {
        val tx = deletingCustomerTx!!
        AlertDialog(
            onDismissRequest = { deletingCustomerTx = null },
            containerColor = NavySurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("تأكيد حذف الحركة", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف حركة \"${if (tx.type == "CREDIT_SALE") "بيع آجل" else "تحصيل دفعة"}\" بمبلغ ${numberFormat.format(tx.amount)} ج.م؟ سيتم تعديل رصيد حساب العميل تلقائياً.",
                    color = TextWhite,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomerTransaction(tx.id)
                        deletingCustomerTx = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Text("حذف", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingCustomerTx = null }) {
                    Text("إلغاء", color = TextWhite)
                }
            }
        )
    }

    // --- Admin Delete Supplier Confirmation Dialog ---
    if (supplierToDelete != null) {
        val sup = supplierToDelete!!
        val hasActiveBalance = sup.remainingBalance != 0.0 || sup.totalPurchases > 0.0

        AlertDialog(
            onDismissRequest = { supplierToDelete = null },
            containerColor = NavySurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (hasActiveBalance) ErrorRed else GoldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (hasActiveBalance) "تحذير: حذف حساب المورد نهائياً" else "تأكيد حذف المورد",
                    color = if (hasActiveBalance) ErrorRed else TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "هل أنت متأكد من حذف حساب المورد: \"${sup.supplierName}\" بالكامل من القائمة؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite
                    )

                    if (hasActiveBalance) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "• إجمالي المشتريات المسجلة: ${numberFormat.format(sup.totalPurchases)} ج.م",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                                Text(
                                    text = "• الرصيد المستحق للمورد: ${numberFormat.format(sup.remainingBalance)} ج.م",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ملاحظة أمان: سيتم نقل حساب المورد وكافة سجلات الفواتير والدفعات بأمان إلى «سلة المحذوفات» مع إمكانية استعادتها في أي وقت.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricBlue,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToDelete = sup.id
                        onDeleteSupplier(idToDelete)
                        if (detailedSupplierId == idToDelete) {
                            detailedSupplierId = null
                        }
                        supplierToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Text("نقل لسلة المحذوفات", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { supplierToDelete = null }) {
                    Text("إلغاء", color = TextWhite)
                }
            }
        )
    }

    // --- Admin Delete Merchant Confirmation Dialog ---
    if (merchantToDelete != null) {
        val mer = merchantToDelete!!
        val hasActiveBalance = mer.remainingDueToMe != 0.0 || mer.totalGoodsDelivered > 0.0

        AlertDialog(
            onDismissRequest = { merchantToDelete = null },
            containerColor = NavySurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (hasActiveBalance) ErrorRed else GoldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "حذف حساب التاجر",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "هل أنت متأكد من حذف حساب التاجر: \"${mer.merchantName}\" ونقله إلى سلة المحذوفات؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite
                    )

                    if (hasActiveBalance) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "• إجمالي البضاعة المسلمة: ${numberFormat.format(mer.totalGoodsDelivered)} ج.م",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                                Text(
                                    text = "• المستحق لك طرف التاجر: ${numberFormat.format(mer.remainingDueToMe)} ج.م",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ملاحظة أمان: سيتم نقل حساب التاجر وسندات تسليم البضاعة والتحصيلات بأمان إلى «سلة المحذوفات» مع إمكانية استعادتها.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricBlue,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToDelete = mer.id
                        onDeleteMerchant(idToDelete)
                        if (detailedMerchantId == idToDelete) {
                            detailedMerchantId = null
                        }
                        merchantToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Text("نقل لسلة المحذوفات", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { merchantToDelete = null }) {
                    Text("إلغاء", color = TextWhite)
                }
            }
        )
    }

    // --- Admin Delete Customer Confirmation Dialog ---
    if (customerToDelete != null) {
        val cust = customerToDelete!!
        val hasActiveBalance = cust.remainingDueToMe != 0.0 || cust.totalCreditSales > 0.0

        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            containerColor = NavySurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (hasActiveBalance) ErrorRed else GoldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "حذف حساب العميل",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "هل أنت متأكد من حذف حساب: \"${cust.customerName}\" ونقله إلى سلة المحذوفات؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite
                    )

                    if (hasActiveBalance) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "• إجمالي مبيعات الآجل: ${numberFormat.format(cust.totalCreditSales)} ج.م",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                                Text(
                                    text = "• المستحق لك طرف العميل: ${numberFormat.format(cust.remainingDueToMe)} ج.م",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ملاحظة أمان: سيتم نقل حساب العميل وسجل عملياته بالكامل بأمان إلى «سلة المحذوفات» مع إمكانية استعادتها.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricBlue,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToDelete = cust.id
                        onDeleteCustomer(idToDelete)
                        if (detailedCustomerId == idToDelete) {
                            detailedCustomerId = null
                        }
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Text("نقل لسلة المحذوفات", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { customerToDelete = null }) {
                    Text("إلغاء", color = TextWhite)
                }
            }
        )
    }

    // --- Financial Dialogs ---
    if (showRecordTreasuryDialog) {
        RecordTreasuryTxDialog(
            initialType = recordTxInitialType,
            onDismiss = { showRecordTreasuryDialog = false },
            onConfirm = { type, category, amount, partyName, paymentMethod, description ->
                onRecordTreasuryTransaction(type, category, amount, "MANUAL", partyName, paymentMethod, description)
                showRecordTreasuryDialog = false
            }
        )
    }

    if (showRecordExpenseDialog) {
        RecordExpenseDialog(
            categories = expenseCategories,
            onDismiss = { showRecordExpenseDialog = false },
            onConfirm = { categoryName, amount, description, beneficiary, paymentMethod, referenceNumber, notes ->
                onRecordExpense(categoryName, amount, description, beneficiary, paymentMethod, referenceNumber, notes)
                showRecordExpenseDialog = false
            }
        )
    }

    if (showManageExpenseCategoriesDialog) {
        ManageExpenseCategoriesDialog(
            categories = expenseCategories,
            onDismiss = { showManageExpenseCategoriesDialog = false },
            onAddCategory = { name, desc -> onAddExpenseCategory(name, desc) },
            onDeleteCategory = { id -> onDeleteExpenseCategory(id) }
        )
    }

    if (showFinancialAdjustmentDialog) {
        RecordFinancialAdjustmentDialog(
            suppliers = suppliers,
            merchants = merchants,
            customers = customers,
            onDismiss = { showFinancialAdjustmentDialog = false },
            onConfirm = { targetType, targetId, targetName, adjType, amount, reason ->
                onRecordFinancialAdjustment(targetType, targetId, targetName, adjType, amount, reason)
                showFinancialAdjustmentDialog = false
            }
        )
    }

    if (reversingTreasuryTxId != null) {
        ReverseTransactionDialog(
            title = "إلغاء حركة خزينة بحركة عكسية",
            targetNumber = reversingTreasuryTxText,
            onDismiss = { reversingTreasuryTxId = null },
            onConfirm = { reason ->
                onReverseTreasuryTransaction(reversingTreasuryTxId!!, reason)
                reversingTreasuryTxId = null
            }
        )
    }

    if (reversingExpenseId != null) {
        ReverseTransactionDialog(
            title = "إلغاء مصروف بحركة عكسية",
            targetNumber = reversingExpenseNumber,
            onDismiss = { reversingExpenseId = null },
            onConfirm = { reason ->
                onReverseExpense(reversingExpenseId!!, reason)
                reversingExpenseId = null
            }
        )
    }
}

@Composable
fun SupplierCard(
    supplier: SupplierAccountEntity,
    numberFormat: NumberFormat,
    isAdmin: Boolean = false,
    onOpenStatement: () -> Unit,
    onDeleteSupplier: () -> Unit = {}
) {
    val isDue = supplier.remainingBalance > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenStatement() },
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isDue) GoldBorderGlow else GlassBorder)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar + Supplier Name + Admin Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(GoldContainer)
                            .border(1.dp, GoldPrimary, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = supplier.supplierName.take(1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = supplier.supplierName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        if (supplier.phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = TextMuted, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = supplier.phone, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Balance status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDue) ErrorRed.copy(alpha = 0.18f) else SuccessGreen.copy(alpha = 0.18f))
                        .border(
                            0.8.dp,
                            if (isDue) ErrorRed.copy(alpha = 0.6f) else SuccessGreen.copy(alpha = 0.6f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDue) "مستحق: ${numberFormat.format(supplier.remainingBalance)} ج.م" else "خالص ✓",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDue) ErrorRed else SuccessGreen,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurfaceVariant)
                    .border(0.6.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("إجمالي المشتريات", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                        Text("${numberFormat.format(supplier.totalPurchases)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("المسدد للمورد", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                        Text("${numberFormat.format(supplier.totalPaid)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("المتبقي له", style = MaterialTheme.typography.labelSmall, color = GoldSecondary, fontSize = 10.sp)
                        Text("${numberFormat.format(supplier.remainingBalance)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenStatement,
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceElevated, contentColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("عرض كشف الحساب والعمليات", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, // In RTL, points left into the details
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDeleteSupplier,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف المورد",
                            tint = ErrorRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantCard(
    merchant: MerchantAccountEntity,
    numberFormat: NumberFormat,
    isAdmin: Boolean = false,
    onOpenStatement: () -> Unit,
    onDeleteMerchant: () -> Unit = {}
) {
    val isDue = merchant.remainingDueToMe > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenStatement() },
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isDue) GoldBorderGlow else GlassBorder)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar + Merchant Name + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(ElectricBlueContainer)
                            .border(1.dp, ElectricBlue, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = merchant.merchantName.take(1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = merchant.merchantName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        if (merchant.phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = TextMuted, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = merchant.phone, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Balance status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDue) GoldPrimary.copy(alpha = 0.18f) else SuccessGreen.copy(alpha = 0.18f))
                        .border(
                            0.8.dp,
                            if (isDue) GoldPrimary.copy(alpha = 0.6f) else SuccessGreen.copy(alpha = 0.6f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDue) "لنا طرفه: ${numberFormat.format(merchant.remainingDueToMe)} ج.م" else "مصفى بالكامل ✓",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDue) GoldPrimary else SuccessGreen,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurfaceVariant)
                    .border(0.6.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("بضاعة مسلمة", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                        Text("${numberFormat.format(merchant.totalGoodsDelivered)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("المحصل منه", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                        Text("${numberFormat.format(merchant.totalCollected)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("المستحق لي", style = MaterialTheme.typography.labelSmall, color = GoldSecondary, fontSize = 10.sp)
                        Text("${numberFormat.format(merchant.remainingDueToMe)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenStatement,
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceElevated, contentColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("عرض كشف الحساب والعمليات", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDeleteMerchant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف التاجر",
                            tint = ErrorRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(
    customer: CustomerAccountEntity,
    numberFormat: NumberFormat,
    isAdmin: Boolean = false,
    onOpenStatement: () -> Unit,
    onDeleteCustomer: () -> Unit = {}
) {
    val isDue = customer.remainingDueToMe > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenStatement() },
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isDue) GoldBorderGlow else GlassBorder)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar + Customer Name + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(GoldContainer)
                            .border(1.dp, GoldPrimary, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.customerName.take(1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = customer.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        if (customer.phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = TextMuted, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = customer.phone, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Balance status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDue) GoldPrimary.copy(alpha = 0.18f) else SuccessGreen.copy(alpha = 0.18f))
                        .border(
                            0.8.dp,
                            if (isDue) GoldPrimary.copy(alpha = 0.6f) else SuccessGreen.copy(alpha = 0.6f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDue) "آجل: ${numberFormat.format(customer.remainingDueToMe)} ج.م" else "خالص ✓",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDue) GoldPrimary else SuccessGreen,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurfaceVariant)
                    .border(0.6.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("مبيعات الآجل", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                        Text("${numberFormat.format(customer.totalCreditSales)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("المحصل منه", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                        Text("${numberFormat.format(customer.totalCollected)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("المستحق لي", style = MaterialTheme.typography.labelSmall, color = GoldSecondary, fontSize = 10.sp)
                        Text("${numberFormat.format(customer.remainingDueToMe)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenStatement,
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceElevated, contentColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("عرض كشف الحساب والعمليات", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDeleteCustomer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف الحساب",
                            tint = ErrorRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
