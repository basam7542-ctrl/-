package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BrandEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ProductModelEntity
import com.example.ui.MainTab
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
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
import com.example.voice.BasheerVoiceIntent
import com.example.voice.BasheerVoiceParser
import com.example.voice.ParsedProductVoiceData
import java.text.NumberFormat
import java.util.Locale

sealed class PendingConfirmAction {
    data class DeleteModelAction(val model: ProductModelEntity, val prompt: String) : PendingConfirmAction()
    data class ExpenseAction(val categoryName: String, val amount: Double, val description: String, val prompt: String) : PendingConfirmAction()
    data class SupplierPaymentAction(val supplierName: String, val amount: Double, val prompt: String) : PendingConfirmAction()
    data class CustomerCollectionAction(val customerName: String, val amount: Double, val prompt: String) : PendingConfirmAction()
    data class UpdatePriceAction(val model: ProductModelEntity, val newPrice: Double, val prompt: String) : PendingConfirmAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasheerVoiceAssistantDialog(
    isListening: Boolean,
    spokenText: String,
    rmsLevel: Float,
    errorMessage: String?,
    categories: List<CategoryEntity>,
    brands: List<BrandEntity>,
    models: List<ProductModelEntity>,
    isAdmin: Boolean,
    treasuryCashBalance: Double = 0.0,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onLaunchSystemSpeechIntent: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmAddProduct: (
        categoryName: String,
        brandName: String,
        productName: String,
        modelName: String,
        finalPrice: Double,
        minAllowedPrice: Double,
        stockQuantity: Int
    ) -> Unit,
    onOpenSale: (ProductModelEntity) -> Unit,
    onNavigateToTab: (MainTab) -> Unit = {},
    onDeleteModel: (Long) -> Unit = {},
    onUpdatePrice: (Long, Double) -> Unit = { _, _ -> },
    onRecordExpense: (categoryName: String, amount: Double, desc: String) -> Unit = { _, _, _ -> },
    onSupplierPayment: (supplierName: String, amount: Double) -> Unit = { _, _ -> },
    onCustomerCollection: (customerName: String, amount: Double) -> Unit = { _, _ -> },
    onSpeakFeedback: (String) -> Unit = {}
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }

    // Dialog Modes: LISTENING, SEARCH_RESULT, REVIEW_PRODUCT, CONFIRM_ACTION, TREASURY_BALANCE, FEEDBACK
    var currentMode by remember { mutableStateOf("LISTENING") }
    var parsedProductData by remember { mutableStateOf<ParsedProductVoiceData?>(null) }
    var searchResultModels by remember { mutableStateOf<List<ProductModelEntity>>(emptyList()) }
    var manualInputText by remember { mutableStateOf("") }
    var feedbackTitle by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackIconType by remember { mutableStateOf("INFO") } // INFO, WARNING, SUCCESS
    var targetNavTab by remember { mutableStateOf<MainTab?>(null) }
    var pendingAction by remember { mutableStateOf<PendingConfirmAction?>(null) }

    // Core processing of spoken text
    fun processSpokenText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        val intent = BasheerVoiceParser.parse(trimmed, categories, brands, isAdmin)
        when (intent) {
            is BasheerVoiceIntent.Help -> {
                feedbackTitle = "المساعد الصوتي «بشير»"
                feedbackMessage = intent.spokenFeedback
                feedbackIconType = "INFO"
                targetNavTab = null
                currentMode = "FEEDBACK"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.CheckTreasuryBalance -> {
                currentMode = "TREASURY_BALANCE"
                onSpeakFeedback("رصيد الخزنة دلوقتي يا فندم هو ${numberFormat.format(treasuryCashBalance)} جنيه.")
            }

            is BasheerVoiceIntent.Navigate -> {
                onNavigateToTab(intent.targetTab)
                feedbackTitle = "التنقل السريع"
                feedbackMessage = intent.spokenFeedback
                feedbackIconType = "SUCCESS"
                targetNavTab = intent.targetTab
                currentMode = "FEEDBACK"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.SearchPrice -> {
                val q = intent.query
                val matched = models.filter {
                    it.modelName.contains(q, ignoreCase = true) ||
                            it.productName.contains(q, ignoreCase = true) ||
                            it.brandName.contains(q, ignoreCase = true) ||
                            it.categoryName.contains(q, ignoreCase = true) ||
                            it.code.contains(q, ignoreCase = true)
                }
                searchResultModels = if (matched.isNotEmpty()) matched else models.take(4)
                currentMode = "SEARCH_RESULT"
                onSpeakFeedback(if (matched.isNotEmpty()) "لقيتلك ${matched.size} موديل مطابق لطلبك يا باشا." else "ما لقيتش تطابق كامل، بس دي أقرب موديلات متاحة بالمخزن.")
            }

            is BasheerVoiceIntent.StartSale -> {
                val q = intent.query
                val matched = models.filter {
                    it.modelName.contains(q, ignoreCase = true) ||
                            it.productName.contains(q, ignoreCase = true) ||
                            it.brandName.contains(q, ignoreCase = true) ||
                            it.categoryName.contains(q, ignoreCase = true)
                }
                if (matched.size == 1) {
                    onDismiss()
                    onOpenSale(matched.first())
                    onSpeakFeedback("اتفضل يا باشا، فتحتلك شاشة البيع للموديل ${matched.first().modelName}.")
                } else if (matched.isNotEmpty()) {
                    searchResultModels = matched
                    currentMode = "SEARCH_RESULT"
                    onSpeakFeedback("لقيت أكتر من موديل، اختار الموديل اللي عاوز تبيعه يا غالي.")
                } else {
                    searchResultModels = models.take(4)
                    currentMode = "SEARCH_RESULT"
                    onSpeakFeedback("اختار الموديل من المخزن لبدء عملية البيع.")
                }
            }

            is BasheerVoiceIntent.AddProduct -> {
                parsedProductData = intent.data
                currentMode = "REVIEW_PRODUCT"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.DeleteModel -> {
                val q = intent.modelQuery
                val match = models.firstOrNull {
                    it.modelName.contains(q, ignoreCase = true) ||
                            it.code.contains(q, ignoreCase = true) ||
                            it.productName.contains(q, ignoreCase = true)
                }
                if (match != null) {
                    pendingAction = PendingConfirmAction.DeleteModelAction(
                        model = match,
                        prompt = "يا باشا، للتأكيد: هل توافق على حذف الموديل «${match.productName} - ${match.modelName}» ونقله لسلة المحذوفات؟"
                    )
                    currentMode = "CONFIRM_ACTION"
                    onSpeakFeedback("يا باشا، للتأكيد: هل أنت متأكد من حذف الموديل «${match.modelName}»؟")
                } else {
                    feedbackTitle = "لم يتم العثور على الموديل"
                    feedbackMessage = "ما لقيتش موديل مطابق لـ «$q» في المخزن يا باشا، اتأكد من الاسم وجرب تاني."
                    feedbackIconType = "WARNING"
                    currentMode = "FEEDBACK"
                    onSpeakFeedback("ما لقيتش موديل مطابق لطلبك في المخزن يا باشا.")
                }
            }

            is BasheerVoiceIntent.UpdatePrice -> {
                val q = intent.modelQuery
                val match = models.firstOrNull {
                    it.modelName.contains(q, ignoreCase = true) ||
                            it.productName.contains(q, ignoreCase = true)
                }
                if (match != null) {
                    pendingAction = PendingConfirmAction.UpdatePriceAction(
                        model = match,
                        newPrice = intent.newPrice,
                        prompt = "يا مدير، للتأكيد: تعديل سعر «${match.modelName}» من ${numberFormat.format(match.finalPrice)} ج.م إلى ${numberFormat.format(intent.newPrice)} ج.م؟"
                    )
                    currentMode = "CONFIRM_ACTION"
                    onSpeakFeedback("يا مدير، للتأكيد: تعديل سعر «${match.modelName}» إلى ${numberFormat.format(intent.newPrice)} جنيه؟")
                } else {
                    feedbackTitle = "لم يتم العثور على الموديل"
                    feedbackMessage = "ما لقيتش موديل بالاسم ده عشان أعدل سعره يا باشا."
                    feedbackIconType = "WARNING"
                    currentMode = "FEEDBACK"
                    onSpeakFeedback("ما لقيتش الموديل عشان أعدل سعره يا فندم.")
                }
            }

            is BasheerVoiceIntent.RecordExpense -> {
                pendingAction = PendingConfirmAction.ExpenseAction(
                    categoryName = intent.categoryName,
                    amount = intent.amount,
                    description = intent.description,
                    prompt = "يا مدير، للتأكيد: هل توافق على صرف مبلغ ${numberFormat.format(intent.amount)} ج.م لبند «${intent.categoryName}» وخصمه من الخزنة؟"
                )
                currentMode = "CONFIRM_ACTION"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.SupplierPayment -> {
                pendingAction = PendingConfirmAction.SupplierPaymentAction(
                    supplierName = intent.supplierName,
                    amount = intent.amount,
                    prompt = "يا مدير، للتأكيد: هل توافق على تسجيل سداد ${numberFormat.format(intent.amount)} ج.م للمورد «${intent.supplierName}» وخصمها من الخزنة؟"
                )
                currentMode = "CONFIRM_ACTION"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.CustomerCollection -> {
                pendingAction = PendingConfirmAction.CustomerCollectionAction(
                    customerName = intent.customerName,
                    amount = intent.amount,
                    prompt = "يا باشا، للتأكيد: تسجيل تحصيل مبلغ ${numberFormat.format(intent.amount)} ج.م من «${intent.customerName}» وتوريده للخزنة؟"
                )
                currentMode = "CONFIRM_ACTION"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.InitiateReturn -> {
                onNavigateToTab(MainTab.RETURNS)
                feedbackTitle = "شاشة المرتجعات"
                feedbackMessage = intent.spokenFeedback
                feedbackIconType = "SUCCESS"
                targetNavTab = MainTab.RETURNS
                currentMode = "FEEDBACK"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.InitiateDamageRepair -> {
                onNavigateToTab(MainTab.DAMAGE_REPAIR)
                feedbackTitle = "الهالك والتالف والإصلاح"
                feedbackMessage = intent.spokenFeedback
                feedbackIconType = "SUCCESS"
                targetNavTab = MainTab.DAMAGE_REPAIR
                currentMode = "FEEDBACK"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.InitiateAudit -> {
                onNavigateToTab(MainTab.INVENTORY)
                feedbackTitle = "شاشة الجرد"
                feedbackMessage = intent.spokenFeedback
                feedbackIconType = "SUCCESS"
                targetNavTab = MainTab.INVENTORY
                currentMode = "FEEDBACK"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.StockMovements -> {
                onNavigateToTab(MainTab.INVENTORY)
                feedbackTitle = "سجل حركة المخزون"
                feedbackMessage = intent.spokenFeedback
                feedbackIconType = "SUCCESS"
                targetNavTab = MainTab.INVENTORY
                currentMode = "FEEDBACK"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.PermissionDenied -> {
                feedbackTitle = "عملية خاصة بصلاحية المدير"
                feedbackMessage = intent.spokenFeedback
                feedbackIconType = "WARNING"
                targetNavTab = null
                currentMode = "FEEDBACK"
                onSpeakFeedback(intent.spokenFeedback)
            }

            is BasheerVoiceIntent.NeedClarification -> {
                feedbackTitle = "لم أفهم الأمر بدقة"
                feedbackMessage = intent.spokenFeedback
                feedbackIconType = "INFO"
                targetNavTab = null
                currentMode = "FEEDBACK"
                onSpeakFeedback(intent.spokenFeedback)
            }
        }
    }

    // Auto-process incoming speech from system intent
    LaunchedEffect(spokenText) {
        val trimmed = spokenText.trim()
        if (trimmed.isNotBlank()) {
            processSpokenText(trimmed)
        }
    }

    // MODE 1: LISTENING & INPUT INTERFACE
    if (currentMode == "LISTENING") {
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
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GoldContainer)
                                .border(1.5.dp, GoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "المساعد الصوتي «بشير»",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldPrimary
                            )
                            Text(
                                text = if (isAdmin) "إدارة المخزن، المبيعات، الخزينة، والمصروفات بالصوت" else "الاستفسار عن الأسعار والمبيعات والتنقل بالصوت",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = TextMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PulsingMicrophoneVisualizer(
                        isListening = isListening,
                        rmsLevel = rmsLevel,
                        onMicClick = onLaunchSystemSpeechIntent
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isListening) "بشير بيسمعك دلوقتي.. اتكلم يا باشا" else "اضغط على الزر للتحدث بالصوت",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) GoldSecondary else TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = onLaunchSystemSpeechIntent,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = TextDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اضغط وتحدث إلى بشير الآن", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavySurfaceElevated)
                            .border(1.dp, if (spokenText.isNotBlank()) GoldPrimary else NavyCardBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (spokenText.isNotBlank()) {
                                Text(
                                    text = "الكلام المفهوم من صوتك:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"$spokenText\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = if (isListening) "جاري الاستماع لصوتك..." else "جاهز، اتكلم باللهجة المصرية الطبيعية وأنا هفهمك",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Input Fallback
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualInputText,
                            onValueChange = { manualInputText = it },
                            placeholder = { Text("أو اكتب الأمر هنا للتجربة السريعة...", fontSize = 12.sp, color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (manualInputText.isNotBlank()) {
                                    processSpokenText(manualInputText)
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = NavyCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (manualInputText.isNotBlank()) {
                                    processSpokenText(manualInputText)
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldPrimary)
                                .size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = TextDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Spoken Egyptian Commands Suggestions
                    Text(
                        text = "جرب تنطق أي أمر من دول باللهجة المصرية:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val sampleCommands = listOf(
                        "سعر غسالة إل جي 9 كيلو كام؟",
                        "كام رصيد الخزنة دلوقتي؟",
                        "عايز ابيع ثلاجة شارب 18 قدم",
                        "افتح شاشة المرتجعات",
                        "سجل مصروف كهربا 500 جنيه",
                        "ضيف شاشة سامسونج 55 بوصة سمارت سعره 15000 وحد ادنى 14000 والكمية 3"
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sampleCommands.forEach { sample ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { processSpokenText(sample) },
                                colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🗣️ \"$sample\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextWhite,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
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

    // MODE 2: SENSITIVE ACTION CONFIRMATION (حماية العمليات الحساسة)
    if (currentMode == "CONFIRM_ACTION" && pendingAction != null) {
        val action = pendingAction!!
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = NavySurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تأكيد العملية الحساسة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldSecondary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                        border = BorderStroke(1.5.dp, GoldSecondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            when (action) {
                                is PendingConfirmAction.DeleteModelAction -> {
                                    Text("حذف موديل من المخزن", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("المنتج: ${action.model.productName}", color = TextWhite, fontSize = 13.sp)
                                    Text("الموديل: ${action.model.modelName}", color = TextWhite, fontSize = 13.sp)
                                    Text("الكود: ${action.model.code}", color = TextMuted, fontSize = 12.sp)
                                    Text("الكمية الحالية: ${action.model.stockQuantity} قطعة", color = TextMuted, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(action.prompt, color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                is PendingConfirmAction.ExpenseAction -> {
                                    Text("تسجيل مصروف وصرف نقدي", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("البند: ${action.categoryName}", color = TextWhite, fontSize = 13.sp)
                                    Text("المبلغ: ${numberFormat.format(action.amount)} ج.م", color = ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("البيان: ${action.description}", color = TextMuted, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(action.prompt, color = GoldLight, fontSize = 12.sp)
                                }
                                is PendingConfirmAction.SupplierPaymentAction -> {
                                    Text("سداد دفعة نقدية لمورد", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("المورد: ${action.supplierName}", color = TextWhite, fontSize = 13.sp)
                                    Text("المبلغ: ${numberFormat.format(action.amount)} ج.م", color = ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(action.prompt, color = GoldLight, fontSize = 12.sp)
                                }
                                is PendingConfirmAction.CustomerCollectionAction -> {
                                    Text("تحصيل نقدي وتوريد للخزنة", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("العميل / التاجر: ${action.customerName}", color = TextWhite, fontSize = 13.sp)
                                    Text("المبلغ: ${numberFormat.format(action.amount)} ج.م", color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(action.prompt, color = GoldLight, fontSize = 12.sp)
                                }
                                is PendingConfirmAction.UpdatePriceAction -> {
                                    Text("تعديل سعر الموديل", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("الموديل: ${action.model.productName} - ${action.model.modelName}", color = TextWhite, fontSize = 13.sp)
                                    Text("السعر الحالي: ${numberFormat.format(action.model.finalPrice)} ج.م", color = TextMuted, fontSize = 12.sp)
                                    Text("السعر الجديد: ${numberFormat.format(action.newPrice)} ج.م", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(action.prompt, color = GoldLight, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (action) {
                            is PendingConfirmAction.DeleteModelAction -> {
                                onDeleteModel(action.model.id)
                                onSpeakFeedback("تمام، تم حذف الموديل ونقله لسلة المحذوفات.")
                            }
                            is PendingConfirmAction.ExpenseAction -> {
                                onRecordExpense(action.categoryName, action.amount, action.description)
                                onSpeakFeedback("تمام يا مدير، اتسجل المصروف واتخصم من الخزنة.")
                            }
                            is PendingConfirmAction.SupplierPaymentAction -> {
                                onSupplierPayment(action.supplierName, action.amount)
                                onSpeakFeedback("حاضر يا مدير، اتسجل سداد المورد واتخصم من الخزنة.")
                            }
                            is PendingConfirmAction.CustomerCollectionAction -> {
                                onCustomerCollection(action.customerName, action.amount)
                                onSpeakFeedback("تمام يا باشا، تم تحصيل المبلغ وتوريده للخزنة.")
                            }
                            is PendingConfirmAction.UpdatePriceAction -> {
                                onUpdatePrice(action.model.id, action.newPrice)
                                onSpeakFeedback("تمام يا مدير، اتعدل سعر الموديل بنجاح.")
                            }
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تأكيد وتنفيذ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { currentMode = "LISTENING" },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                ) {
                    Text("إلغاء وتراجع")
                }
            }
        )
    }

    // MODE 3: TREASURY BALANCE CARD
    if (currentMode == "TREASURY_BALANCE") {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = NavySurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("الخزينة النقدية للمعرض", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldPrimary)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                        border = BorderStroke(1.5.dp, GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("الرصيد النقدي المتوفر حالياً بالخزينة", color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${numberFormat.format(treasuryCashBalance)} ج.م",
                                color = if (treasuryCashBalance >= 0) SuccessGreen else ErrorRed,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("محسوب بدقة من واقع المقبوضات والمدفوعات والمبيعات النقدية المعتمدة", color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismiss()
                        onNavigateToTab(MainTab.ACCOUNTS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("فتح شاشة الحسابات والخزينة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { currentMode = "LISTENING" }) {
                    Text("سؤال صوتي آخر", color = TextWhite)
                }
            }
        )
    }

    // MODE 4: GENERAL FEEDBACK & GUIDANCE
    if (currentMode == "FEEDBACK") {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = NavySurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (feedbackIconType) {
                        "WARNING" -> Icons.Default.Warning
                        "SUCCESS" -> Icons.Default.Check
                        else -> Icons.Default.Info
                    }
                    val iconTint = when (feedbackIconType) {
                        "WARNING" -> GoldSecondary
                        "SUCCESS" -> SuccessGreen
                        else -> GoldPrimary
                    }
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feedbackTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldPrimary)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(feedbackMessage, style = MaterialTheme.typography.bodyMedium, color = TextWhite, lineHeight = 22.sp)
                }
            },
            confirmButton = {
                if (targetNavTab != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onNavigateToTab(targetNavTab!!)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("الانتقال للشاشة الآن", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { currentMode = "LISTENING" },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("التحدث مجدداً", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("إغلاق", color = TextWhite)
                }
            }
        )
    }

    // MODE 5: REVIEW & CONFIRM ADD PRODUCT (معروض للمراجعة قبل الحفظ النهائي للمدير)
    if (currentMode == "REVIEW_PRODUCT" && parsedProductData != null) {
        val initial = parsedProductData!!
        var catName by remember { mutableStateOf(initial.categoryName) }
        var bName by remember { mutableStateOf(initial.brandName) }
        var pName by remember { mutableStateOf(initial.productName) }
        var mName by remember { mutableStateOf(initial.modelName) }
        var finalPriceText by remember { mutableStateOf(initial.finalPrice.toInt().toString()) }
        var minPriceText by remember { mutableStateOf(initial.minAllowedPrice.toInt().toString()) }
        var stockText by remember { mutableStateOf(initial.stockQuantity.toString()) }

        var catExpanded by remember { mutableStateOf(false) }
        var brandExpanded by remember { mutableStateOf(false) }

        val finalPrice = finalPriceText.toDoubleOrNull() ?: 0.0
        val minPrice = minPriceText.toDoubleOrNull() ?: 0.0
        val stock = stockText.toIntOrNull() ?: 1
        val isValid = pName.isNotBlank() && mName.isNotBlank() && finalPrice > 0 && minPrice > 0 && minPrice <= finalPrice

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = NavySurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "مراجعة إضافة المنتج صوتيًا",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary
                        )
                        Text(
                            text = "بشير جهز البيانات، راجعها يا مدير واضغط تأكيد الحفظ",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Selector
                    ExposedDropdownMenuBox(
                        expanded = catExpanded,
                        onExpandedChange = { catExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = catName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("القسم") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        catName = cat.name
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Brand Selector
                    ExposedDropdownMenuBox(
                        expanded = brandExpanded,
                        onExpandedChange = { brandExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = bName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الشركة") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = brandExpanded,
                            onDismissRequest = { brandExpanded = false }
                        ) {
                            brands.map { it.name }.distinct().forEach { brandName ->
                                DropdownMenuItem(
                                    text = { Text(brandName) },
                                    onClick = {
                                        bName = brandName
                                        brandExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Product Name
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("اسم المنتج") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Model Name
                    OutlinedTextField(
                        value = mName,
                        onValueChange = { mName = it },
                        label = { Text("الموديل / المقاس / المواصفة") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Final Price & Min Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = finalPriceText,
                            onValueChange = { finalPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("سعر البيع النهائي") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minPriceText,
                            onValueChange = { minPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("الحد الأدنى للبيع") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Stock Quantity
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it.filter { c -> c.isDigit() } },
                        label = { Text("الكمية بالمخزن") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isValid) {
                            onConfirmAddProduct(catName, bName, pName.trim(), mName.trim(), finalPrice, minPrice, stock)
                            onDismiss()
                        }
                    },
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تأكيد وحفظ في المخزن", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { currentMode = "LISTENING" }) {
                    Text("إعادة التحدث", color = TextWhite)
                }
            }
        )
    }

    // MODE 6: SEARCH RESULT
    if (currentMode == "SEARCH_RESULT") {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = NavySurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نتائج استفسار الأسعار والموديلات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "الموديلات المتوفرة بالمخزن:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    searchResultModels.forEach { m ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${m.productName} - ${m.modelName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "${m.brandName} • ${m.categoryName} • رصيد المخزن: ${m.stockQuantity} قطعة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "سعر البيع:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(text = "${numberFormat.format(m.finalPrice)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                                    }
                                    Column {
                                        Text(text = "الحد الأدنى:", style = MaterialTheme.typography.labelSmall, color = GoldSecondary)
                                        Text(text = "${numberFormat.format(m.minAllowedPrice)} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = GoldPrimary)
                                    }
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onOpenSale(m)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("بيع", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onLaunchSystemSpeechIntent,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("سؤال صوتي آخر", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) { Text("إغلاق", color = TextWhite) }
            }
        )
    }
}

/**
 * Pulsing and glowing circular visualizer for Basheer Voice Assistant
 */
@Composable
fun PulsingMicrophoneVisualizer(
    isListening: Boolean,
    rmsLevel: Float,
    onMicClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(150.dp)
    ) {
        // Cosmic magnetic flare orb from mockup
        Image(
            painter = painterResource(id = R.drawable.cosmic_bashir_orb),
            contentDescription = "نواة بشير المغناطيسية",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(145.dp)
                .scale(if (isListening) pulseScale else 1f)
                .clickable { onMicClick() }
        )

        // Center mic / sound bars icon
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isListening) Color(0xCCFFD54F) else Color(0x990A1428))
                .clickable { onMicClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Hearing else Icons.Default.Mic,
                contentDescription = "تفعيل الصوت",
                tint = if (isListening) Color(0xFF040916) else Color(0xFFFFD54F),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Prominent Fixed Floating Action Button on the screen for "بشير"
 */
@Composable
fun BasheerFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")
    val subtlePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )

    Box(
        modifier = modifier
            .scale(subtlePulse)
            .clip(RoundedCornerShape(28.dp))
            .background(GoldPrimary)
            .border(2.dp, GoldSecondary, RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("basheer_voice_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NavyDarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "المساعد بشير",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        }
    }
}
