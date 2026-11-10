package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SaleRecordEntity
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import com.example.utils.BluetoothPrinterHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog to search and select from paired Bluetooth thermal printers.
 */
@Composable
fun BluetoothPrinterChooserDialog(
    pairedDevices: List<BluetoothPrinterHelper.PrinterDevice>,
    currentSelectedAddress: String?,
    onSelectDevice: (BluetoothPrinterHelper.PrinterDevice) -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اختر طابعة الفواتير",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "تحديث القائمة",
                        tint = GoldSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "الطابعات الحرارية المقترنة بالبلوتوث (بروتوكول ESC/POS):",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (pairedDevices.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WarningAmber.copy(alpha = 0.5f))),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.BluetoothSearching,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "لم يتم العثور على أجهزة بلوتوث مقترنة",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1. تأكد من تشغيل البلوتوث بالطابعة وهاتفك.\n2. اذهب إلى إعدادات الهاتف واقترن بالطابعة أولاً (رمز الاقتران عادة 0000 أو 1234).\n3. ثم اضغط زر التحديث أعلاه.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Right,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pairedDevices) { device ->
                            val isSelected = device.address == currentSelectedAddress
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectDevice(device) }
                                    .testTag("printer_item_${device.address}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else NavySurfaceElevated
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) GoldPrimary else NavyCardBorder)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) GoldPrimary else NavySurface)
                                                .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Print,
                                                contentDescription = null,
                                                tint = if (isSelected) TextDark else GoldPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = device.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                            Text(
                                                text = device.address,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) GoldSecondary else TextMuted
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "محددة",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
            ) {
                Text("إغلاق")
            }
        }
    )
}

/**
 * Dialog shown immediately after a successful sale to prompt for receipt printing.
 */
@Composable
fun PostSaleReceiptDialog(
    sale: SaleRecordEntity,
    isPrinting: Boolean,
    onPrintReceipt: () -> Unit,
    onDismiss: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        modifier = Modifier.testTag("post_sale_receipt_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "تم تسجيل البيع بنجاح!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "تم خصم الكمية من المخزن وإضافة العملية لسجل المبيعات.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Receipt Details Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardBorder)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "رقم الفاتورة: #${sale.id}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dateFormat.format(Date(sale.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${sale.productName} - ${sale.modelName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        Text(
                            text = "${sale.quantity} قطعة × ${numberFormat.format(sale.soldPrice)} ج.م",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )

                        if (sale.notes.isNotBlank()) {
                            Text(
                                text = "العميل: ${sale.notes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(NavyCardBorder)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إجمالي الفاتورة:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "${numberFormat.format(sale.totalPrice)} ج.م",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPrintReceipt,
                enabled = !isPrinting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = TextDark
                ),
                modifier = Modifier.testTag("print_receipt_action_button")
            ) {
                if (isPrinting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("جاري الطباعة...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("طباعة إيصال بيع", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
            ) {
                Text("إغلاق")
            }
        }
    )
}

/**
 * Dialog shown when user taps "طباعة إيصال" but hasn't paired/saved a printer yet.
 */
@Composable
fun NoPrinterAlertDialog(
    onNavigateToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "لم يتم تعيين طابعة بعد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
            }
        },
        text = {
            Text(
                text = "للطباعة الحرارية المباشرة عبر البلوتوث، يجب أولاً ربط طابعة من شاشة الإعدادات.\n\nهل تود الانتقال إلى الإعدادات الآن لاختيار طابعتك؟",
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onNavigateToSettings()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("الانتقال للإعدادات", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
            ) {
                Text("إلغاء")
            }
        }
    )
}
