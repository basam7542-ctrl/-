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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
    onOpenSale: (ProductModelEntity) -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }

    // Dialog Mode: LISTENING -> REVIEW_PRODUCT -> SEARCH_RESULT
    var currentMode by remember { mutableStateOf("LISTENING") }
    var parsedProductData by remember { mutableStateOf<ParsedProductVoiceData?>(null) }
    var searchResultModels by remember { mutableStateOf<List<ProductModelEntity>>(emptyList()) }
    var manualInputText by remember { mutableStateOf("") }

    // Parse intent function
    fun processSpokenText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val intent = BasheerVoiceParser.parse(trimmed, categories, brands)
        when (intent) {
            is BasheerVoiceIntent.AddProduct -> {
                parsedProductData = intent.data
                currentMode = "REVIEW_PRODUCT"
            }
            is BasheerVoiceIntent.SearchPrice -> {
                val q = intent.query
                val matched = models.filter {
                    it.modelName.contains(q, ignoreCase = true) ||
                    it.productName.contains(q, ignoreCase = true) ||
                    it.brandName.contains(q, ignoreCase = true) ||
                    it.categoryName.contains(q, ignoreCase = true)
                }
                searchResultModels = if (matched.isNotEmpty()) matched else models.take(4)
                currentMode = "SEARCH_RESULT"
            }
            is BasheerVoiceIntent.Unknown -> {
                parsedProductData = ParsedProductVoiceData(
                    categoryName = categories.firstOrNull()?.name ?: "غسالات",
                    brandName = brands.firstOrNull()?.name ?: "شارب",
                    productName = trimmed,
                    modelName = "موديل ديجيتال",
                    finalPrice = 15000.0,
                    minAllowedPrice = 14000.0,
                    stockQuantity = 1,
                    originalSpokenText = trimmed
                )
                currentMode = "REVIEW_PRODUCT"
            }
        }
    }

    // AUTOMATIC TRANSITION: when spokenText is available, automatically process it!
    LaunchedEffect(spokenText) {
        val trimmed = spokenText.trim()
        if (trimmed.isNotBlank()) {
            processSpokenText(trimmed)
        }
    }

    // MODE 1: LISTENING / SPEAKING INTERFACE
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
                                text = "المساعد الصوتي \"بشير\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldPrimary
                            )
                            Text(
                                text = if (isAdmin) "إضافة المنتجات والبحث بالصوت" else "الاستفسار عن الأسعار والموديلات",
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
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing / Glowing Microphone Visualizer
                    PulsingMicrophoneVisualizer(
                        isListening = isListening,
                        rmsLevel = rmsLevel,
                        onMicClick = onLaunchSystemSpeechIntent
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isListening) "بشير يستمع إليك الآن.. تكلّم بالعربية" else "اضغط على الزر للتحدث بالصوت",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) GoldSecondary else TextWhite,
                        textAlign = TextAlign.Center
                    )

                    // Primary System Voice Action Button
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
                            .padding(top = 10.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اضغط وتحدث إلى بشير الآن", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    // Live Speech Transcript Box
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
                                    text = "النتيجة الصوتية المفهومة:",
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
                                    text = if (isListening) "جاري الاستماع لصوتك..." else "جاهز للاستماع، اضغط على زر التحدث بالصوت",
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text input fallback for quick testing or typing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualInputText,
                            onValueChange = { manualInputText = it },
                            placeholder = { Text("أو اكتب الأمر هنا للتجربة...", fontSize = 12.sp, color = TextMuted) },
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Spoken Suggestions (Click to simulate / test)
                    Text(
                        text = "أو اختر أمرًا جاهزًا للتجربة الفورية:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val sampleCommands = listOf(
                        "ضيف ثلاجة شارب 18 قدم، قسم الثلاجات، شركة شارب، السعر 32500 والحد الأدنى 31000 والكمية 4",
                        "ضيف غسالة إل جي 9 كيلو بالبخار، قسم الغسالات، شركة إل جي، السعر 28000 والحد الأدنى 27000",
                        "ضيف شاشة تورنيدو 50 بوصة سمارت، قسم الشاشات، شركة تورنيدو، السعر 14000 والحد الأدنى 13200",
                        "سعر غسالة توشيبا 10 كيلو كام؟"
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
            confirmButton = {
                if (spokenText.isNotBlank()) {
                    Button(
                        onClick = { processSpokenText(spokenText) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تحويل إلى نموذج", fontWeight = FontWeight.Bold)
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

    // MODE 2: REVIEW & CONFIRMATION BEFORE FINAL SAVE (معروض للمراجعة قبل الحفظ النهائي)
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
                            text = "بشير ملأ البيانات تلقائيًا، راجعها قبل الحفظ النهائي",
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
                    // Highlighted Spoken Audio Result Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                        border = BorderStroke(1.5.dp, GoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "النتيجة المفهومة من صوتك:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"${initial.originalSpokenText}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                            IconButton(
                                onClick = onLaunchSystemSpeechIntent,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "تحدث مجدداً",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

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

                    // Model Name / Spec
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

    // MODE 3: PRICE SEARCH RESULT
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
                        text = "نتائج استفسار الأسعار",
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
                    // Highlighted Spoken Query Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                        border = BorderStroke(1.5.dp, GoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الطلب الصوتي المفهوم:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"${spokenText.ifBlank { "استفسار عن الأسعار" }}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                            IconButton(
                                onClick = onLaunchSystemSpeechIntent,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "استفسار صوتي آخر",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "الأجهزة المتاحة المطابقة لطلبك:",
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
                                    text = "${m.brandName} • ${m.categoryName}",
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
                                        Text(text = "سعر البيع النهائي:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
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
                                        Text("بيع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
        modifier = Modifier.size(130.dp)
    ) {
        if (isListening) {
            // Outer glowing ring 1
            Box(
                modifier = Modifier
                    .size((105 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = (glowAlpha * 0.4f).coerceIn(0.1f, 0.4f)))
            )
            // Outer glowing ring 2 modulated by audio volume
            val dynamicScale = 1.15f + (rmsLevel * 0.35f)
            Box(
                modifier = Modifier
                    .size((90 * dynamicScale).dp)
                    .clip(CircleShape)
                    .background(GoldSecondary.copy(alpha = (glowAlpha * 0.6f).coerceIn(0.15f, 0.6f)))
            )
        }

        // Center mic button
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (isListening) GoldPrimary else NavySurfaceElevated)
                .border(2.5.dp, GoldPrimary, CircleShape)
                .clickable { onMicClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Hearing else Icons.Default.Mic,
                contentDescription = "تفعيل الصوت",
                tint = if (isListening) TextDark else GoldPrimary,
                modifier = Modifier.size(36.dp)
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
