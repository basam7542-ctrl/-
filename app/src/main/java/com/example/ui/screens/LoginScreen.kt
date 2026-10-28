package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDarkBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

data class AccountOption(
    val id: String,
    val name: String,
    val roleTitle: String,
    val isAdmin: Boolean
)

enum class StartupScreenStage {
    WELCOME,
    ACCOUNT_SELECTION
}

// Deep charcoal to midnight navy gradient
private val NavyGradientBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF050811),
        Color(0xFF090F1E),
        Color(0xFF101C34),
        Color(0xFF0A1224),
        Color(0xFF050811)
    )
)

@Composable
fun LoginScreen(
    onLoginSubmit: (userId: String, pin: String) -> Unit,
    errorMessage: String? = null
) {
    var stage by rememberSaveable { mutableStateOf(StartupScreenStage.WELCOME) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyGradientBrush)
    ) {
        AnimatedContent(
            targetState = stage,
            transitionSpec = {
                (fadeIn(animationSpec = tween(500)) + slideInVertically(animationSpec = tween(500)) { 40 })
                    .togetherWith(fadeOut(animationSpec = tween(300)))
            },
            label = "StartupStageTransition"
        ) { currentStage ->
            when (currentStage) {
                StartupScreenStage.WELCOME -> {
                    WelcomeSplashScreen(
                        onEnterClick = { stage = StartupScreenStage.ACCOUNT_SELECTION }
                    )
                }
                StartupScreenStage.ACCOUNT_SELECTION -> {
                    AccountSelectionScreen(
                        onLoginSubmit = onLoginSubmit,
                        errorMessage = errorMessage,
                        onBackToWelcome = { stage = StartupScreenStage.WELCOME }
                    )
                }
            }
        }
    }
}

/**
 * Stage 1: Welcome/Splash Screen with animated glowing emblem,
 * title, subtitle, and instant tap anywhere or "دخول" button to proceed.
 */
@Composable
fun WelcomeSplashScreen(
    onEnterClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Tap anywhere on screen to proceed
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onEnterClick()
            }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(animationSpec = tween(800)) { 50 }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pulsing glowing emblem
                PulsingEmblem(
                    size = 140.dp,
                    haloMaxSize = 190.dp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Brand Title
                Text(
                    text = "البشيري للأجهزة الكهربائية",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "نظام المبيعات وإدارة المخزون الذكي",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoldSecondary.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "مرحباً بكم في نظام الإدارة الداخلي المعتمد",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Feature Highlights Chips
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("مخزون ذكي", "كشوف حسابات", "سلة محذوفات", "طباعة فواتير").forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(NavySurfaceElevated.copy(alpha = 0.85f))
                                .border(0.8.dp, GlassBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Prominent Enter Button
                Button(
                    onClick = onEnterClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(54.dp)
                        .testTag("welcome_enter_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "دخول",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "دخول",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Hint label
                Text(
                    text = "اضغط في أي مكان بالشاشة أو على زر دخول للمتابعة",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Stage 2: Account Selection & Login Screen with smooth fade-in,
 * animated account selection feedback, and PIN input.
 */
@Composable
fun AccountSelectionScreen(
    onLoginSubmit: (userId: String, pin: String) -> Unit,
    errorMessage: String? = null,
    onBackToWelcome: () -> Unit
) {
    val accounts = listOf(
        AccountOption("admin", "المدير", "صلاحيات كاملة للمحل", true),
        AccountOption("seller1", "بائع 1", "تصفح الأسعار وتسجيل البيع", false),
        AccountOption("seller2", "بائع 2", "تصفح الأسعار وتسجيل البيع", false),
        AccountOption("seller3", "بائع 3", "تصفح الأسعار وتسجيل البيع", false)
    )

    var selectedAccountId by remember { mutableStateOf("admin") }
    var pin by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(animationSpec = tween(600)) { 30 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top bar with back to welcome screen button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = onBackToWelcome,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NavySurfaceElevated.copy(alpha = 0.6f))
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة للشاشة الترحيبية",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Compact glowing logo
                PulsingEmblem(
                    size = 80.dp,
                    haloMaxSize = 106.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "البشيري للأجهزة الكهربائية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "اختر الحساب وأدخل كلمة المرور للمتابعة",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 3.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Main Login Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(NavyCardBorder)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "حسابات المستخدمين",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Accounts List with dynamic scale and glow on selection
                        accounts.forEach { account ->
                            val isSelected = selectedAccountId == account.id

                            // Animated scale-up and border glow when selected
                            val cardScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.025f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "CardScale_${account.id}"
                            )

                            val borderAlpha by animateFloatAsState(
                                targetValue = if (isSelected) 1.0f else 0.4f,
                                animationSpec = tween(250),
                                label = "BorderAlpha_${account.id}"
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .graphicsLayer {
                                        scaleX = cardScale
                                        scaleY = cardScale
                                    }
                                    .clickable {
                                        selectedAccountId = account.id
                                        pin = ""
                                    }
                                    .testTag("account_item_${account.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        NavySurfaceElevated
                                    } else {
                                        NavySurfaceVariant
                                    }
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = SolidColor(
                                        if (isSelected) GoldPrimary.copy(alpha = borderAlpha) else NavyCardBorder
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) GoldContainer else NavySurface
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (account.isAdmin) Icons.Default.Security else Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (isSelected) GoldPrimary else TextMuted,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = account.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) GoldPrimary else TextWhite
                                            )
                                            Text(
                                                text = account.roleTitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "تم الاختيار",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // PIN / Password Input
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it },
                            label = { Text("أدخل كلمة المرور") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (pin.isNotBlank()) onLoginSubmit(selectedAccountId, pin)
                                }
                            ),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showPassword) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                        tint = TextMuted
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = NavyCardBorder,
                                cursorColor = GoldPrimary,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pin_input")
                        )

                        if (!errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = errorMessage,
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Login Submit Button
                        Button(
                            onClick = {
                                if (pin.isNotBlank()) {
                                    onLoginSubmit(selectedAccountId, pin)
                                }
                            },
                            enabled = pin.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = TextDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_submit_button")
                        ) {
                            Text(
                                text = "دخول النظام",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ملاحظة: هذا النظام مخصص للاستخدام الداخلي بالمحل فقط",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Animated circular emblem with soft pulsing golden halo
 */
@Composable
fun PulsingEmblem(
    size: Dp,
    haloMaxSize: Dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EmblemHaloPulse")

    // Outer halo pulse scale and alpha
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = modifier.size(haloMaxSize),
        contentAlignment = Alignment.Center
    ) {
        // Outer soft radiant halo
        Box(
            modifier = Modifier
                .size(haloMaxSize)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = pulseAlpha * 0.45f),
                            GoldSecondary.copy(alpha = pulseAlpha * 0.20f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Mid soft ring halo
        Box(
            modifier = Modifier
                .size(size + 18.dp)
                .scale((pulseScale + 1.0f) / 2f)
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    color = GoldPrimary.copy(alpha = pulseAlpha * 0.8f),
                    shape = CircleShape
                )
        )

        // Inner circular logo
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(2.5.dp, GoldPrimary, CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.albashiri_logo),
                contentDescription = "شعار البشيري",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        }
    }
}
