package com.example.ui.screens.cosmic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.MainTab
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.HologramCyan

/**
 * Data class representing a Planet in the Al-Bashiri Cosmic Solar System.
 */
data class CosmicPlanetDef(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val isFeatured: Boolean = false,
    val targetTab: MainTab? = null,
    val isProductsPlanet: Boolean = false,
    val statValue: String = "",
    val statLabel: String = ""
)

/**
 * BESHEER COSMIC FUTURE | Master Screen: The Interactive Solar System (النظام الشمسي).
 *
 * Implements full user specifications:
 * 1. Massive radiant central Sun carrying Al-Bashiri royal branding with pulsating corona.
 * 2. 11 3D-styled interactive spherical planets on balanced cosmic orbits.
 * 3. Clear, legible Arabic name tags on every planet with high-contrast frosted capsules.
 * 4. Interactive Planet Inspection Card (بطاقة تعريف الكوكب) with «دخول القسم» action.
 * 5. Persistent App Sections Directory («أقسام التطبيق») button and organized bottom drawer.
 * 6. Voice Assistant «بشير» energy orb with soundwave visualizer safely positioned above system bars.
 * 7. Zero text collision, clean status/navigation bar insets.
 */
@Composable
fun CosmicSolarSystemScreen(
    onEnterPlanet: (CosmicPlanetDef) -> Unit,
    onBasheerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Planets Registry
    val planetsList = remember {
        listOf(
            CosmicPlanetDef(
                id = "products",
                name = "المنتجات",
                category = "الكتالوج والمعرض الرئيسي",
                description = "إدارة الكتالوج الشامل لجميع الأجهزة الكهربائية، الموديلات، الشركات الموردة، وأسعار البيع مع الحد الأدنى.",
                icon = Icons.Default.ViewInAr,
                accentColor = Color(0xFFFFD54F),
                isFeatured = true,
                isProductsPlanet = true,
                statValue = "824+",
                statLabel = "جهاز متاح"
            ),
            CosmicPlanetDef(
                id = "dashboard",
                name = "لوحة التحكم",
                category = "مركز القيادة المباشر",
                description = "لوحة المؤشرات والعمليات السريعة، إحصائيات المبيعات، واستخراج فواتير البيع الفورية.",
                icon = Icons.Default.Speed,
                accentColor = Color(0xFF38BDF8),
                targetTab = MainTab.CATALOG,
                statValue = "100%",
                statLabel = "كفاءة التشغيل"
            ),
            CosmicPlanetDef(
                id = "inventory",
                name = "المخزون",
                category = "المستودع والأرصدة",
                description = "متابعة كميات الأجهزة الحالية في المخزن، تنبيهات النواقص، وجرد الأرصدة الفعلي.",
                icon = Icons.Default.Inventory2,
                accentColor = Color(0xFF60A5FA),
                targetTab = MainTab.INVENTORY,
                statValue = "1,250",
                statLabel = "إجمالي المخزون"
            ),
            CosmicPlanetDef(
                id = "sales",
                name = "المبيعات والفواتير",
                category = "سجل المبيعات والإيصالات",
                description = "أرشيف الفواتير المصدرة، البحث برقم الفاتورة أو اسم العميل، والطباعة الحرارية المباشرة.",
                icon = Icons.Default.ReceiptLong,
                accentColor = Color(0xFFF59E0B),
                targetTab = MainTab.SALES_LOG,
                statValue = "325",
                statLabel = "فاتورة مصدرة"
            ),
            CosmicPlanetDef(
                id = "customers",
                name = "العملاء",
                category = "حسابات ومديونيات العملاء",
                description = "سجل العملاء، مديونيات البيع الآجل، أرقام الهواتف، والتواصل السريع عبر واتساب.",
                icon = Icons.Default.People,
                accentColor = Color(0xFF34D399),
                targetTab = MainTab.ACCOUNTS,
                statValue = "412",
                statLabel = "عميل مسجل"
            ),
            CosmicPlanetDef(
                id = "suppliers",
                name = "الموردون",
                category = "حسابات الشركات الموردة",
                description = "إدارة مستحقات الموردين، مواعيد سداد الدفعات، وفواتير التوريد الجديدة.",
                icon = Icons.Default.LocalShipping,
                accentColor = Color(0xFF2DD4BF),
                targetTab = MainTab.ACCOUNTS,
                statValue = "38",
                statLabel = "شركة موردة"
            ),
            CosmicPlanetDef(
                id = "wholesalers",
                name = "تجار الجملة",
                category = "التوزيع والصفقات الكبرى",
                description = "أسعار الجملة المعتمدة، الخصومات التنافسية، وحسابات كبار الموزعين في المحافظات.",
                icon = Icons.Default.Store,
                accentColor = Color(0xFFC084FC),
                targetTab = MainTab.ACCOUNTS,
                statValue = "54",
                statLabel = "تاجر جملة"
            ),
            CosmicPlanetDef(
                id = "payments",
                name = "المدفوعات والتحصيل",
                category = "التحصيلات والعهد النقدية",
                description = "تسجيل تحصيلات الأقساط، الدفعات النقدية والبنكية، وإيصالات القبض والصرف.",
                icon = Icons.Default.Payments,
                accentColor = Color(0xFF38BDF8),
                targetTab = MainTab.ACCOUNTS,
                statValue = "100%",
                statLabel = "توثيق آلي"
            ),
            CosmicPlanetDef(
                id = "expenses",
                name = "المصروفات والخزينة",
                category = "الدرج النقدي والمصروفات",
                description = "حركة الخزينة النقدية، المصروفات التشغيلية (نقل، بوفيه، إيجار، كهرباء)، وصافي الأرباح.",
                icon = Icons.Default.AccountBalance,
                accentColor = Color(0xFFFBBF24),
                targetTab = MainTab.ACCOUNTS,
                statValue = "12,500",
                statLabel = "إيراد اليوم"
            ),
            CosmicPlanetDef(
                id = "reports",
                name = "التقارير",
                category = "التحليلات والمؤشرات المالية",
                description = "تقارير الأرباح الدورية، الأصناف الأكثر مبيعاً، وتحليلات الأداء البيعي للمعرض.",
                icon = Icons.Default.Assessment,
                accentColor = Color(0xFF22D3EE),
                targetTab = MainTab.ACCOUNTS,
                statValue = "360°",
                statLabel = "رؤية شاملة"
            ),
            CosmicPlanetDef(
                id = "settings",
                name = "الإعدادات",
                category = "إدارة النظام والطابعات",
                description = "بيانات المعرض الرسمية، طابعة البلوتوث الحرارية، صلاحيات المستخدمين، والنسخ الاحتياطي.",
                icon = Icons.Default.Settings,
                accentColor = Color(0xFF94A3B8),
                targetTab = MainTab.SETTINGS,
                statValue = "v2.4",
                statLabel = "أحدث إصدار"
            )
        )
    }

    // State for Planet Inspection Modal
    var activePlanetForInspection by remember { mutableStateOf<CosmicPlanetDef?>(null) }
    // State for App Sections Directory Drawer
    var showSectionsDirectory by remember { mutableStateOf(false) }

    // Organic Solar and Ambient Animations
    val infiniteTransition = rememberInfiniteTransition(label = "solar_ambient")

    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_pulse"
    )

    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rotation"
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_alpha"
    )

    // Three balanced continuous orbital timelines for natural cosmic motion
    // Inner orbit: 40s full cycle
    val innerOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_orbit_angle"
    )

    // Middle orbit: 65s full cycle
    val middleOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(65000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "middle_orbit_angle"
    )

    // Outer orbit: 90s full cycle
    val outerOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(90000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_orbit_angle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF09142C),
                        Color(0xFF040A18),
                        Color(0xFF02050E),
                        Color(0xFF000206)
                    ),
                    radius = 1200f
                )
            )
    ) {
        // Starfield Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stars = listOf(
                Offset(size.width * 0.08f, size.height * 0.12f),
                Offset(size.width * 0.92f, size.height * 0.14f),
                Offset(size.width * 0.18f, size.height * 0.28f),
                Offset(size.width * 0.84f, size.height * 0.32f),
                Offset(size.width * 0.05f, size.height * 0.48f),
                Offset(size.width * 0.95f, size.height * 0.52f),
                Offset(size.width * 0.22f, size.height * 0.65f),
                Offset(size.width * 0.78f, size.height * 0.68f),
                Offset(size.width * 0.12f, size.height * 0.82f),
                Offset(size.width * 0.88f, size.height * 0.85f),
                Offset(size.width * 0.50f, size.height * 0.18f)
            )
            stars.forEachIndexed { i, pt ->
                val r = if (i % 3 == 0) 2.2f else 1.3f
                drawCircle(
                    color = if (i % 2 == 0) Color(0xFFFDE047) else Color(0xFF38BDF8),
                    radius = r,
                    center = pt
                )
            }
        }

        // Main Responsive Layout
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            val centerX = screenWidth / 2
            val centerY = screenHeight * 0.45f

            // 1. Luminous Cosmic Orbital Tracks Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * 0.45f

                // Inner Golden Synchrotron Track
                drawOval(
                    color = Color(0x40FFD54F),
                    topLeft = Offset(cx - size.width * 0.38f, cy - size.height * 0.19f),
                    size = Size(size.width * 0.76f, size.height * 0.38f),
                    style = Stroke(
                        width = 1.6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                    )
                )

                // Middle Electric Cyan Track
                drawOval(
                    color = Color(0x3338BDF8),
                    topLeft = Offset(cx - size.width * 0.45f, cy - size.height * 0.26f),
                    size = Size(size.width * 0.90f, size.height * 0.52f),
                    style = Stroke(
                        width = 1.4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
                    )
                )

                // Outer Nebula Ring Track
                drawOval(
                    color = Color(0x22C084FC),
                    topLeft = Offset(cx - size.width * 0.48f, cy - size.height * 0.31f),
                    size = Size(size.width * 0.96f, size.height * 0.62f),
                    style = Stroke(width = 1.0f)
                )
            }

            // 2. Central Radiant Golden Sun with Royal AlBashiri Emblem
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .offset(x = centerX - 70.dp, y = centerY - 70.dp)
                    .scale(sunPulse)
                    .clickable {
                        // Tapping the Sun shows central Dashboard overview
                        activePlanetForInspection = planetsList.first { it.id == "dashboard" }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer Solar Corona Flare
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(sunRotation)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xBBFFD700),
                                    Color(0x66F59E0B),
                                    Color(0x22FF8C00),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Radiant Golden Sun Sphere
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFF6D1),
                                    Color(0xFFFFD54F),
                                    Color(0xFFD4AF37),
                                    Color(0xFF8A6812)
                                )
                            )
                        )
                        .border(2.dp, Color(0xFFFFFBEB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.albashiri_emblem_royal_clean),
                        contentDescription = "شعار البشيري المركزي",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(68.dp)
                            .padding(4.dp)
                    )
                }
            }

            // 3. Balanced Planetary System (11 Planets with real continuous orbital motion)
            val innerRx = screenWidth * 0.36f
            val innerRy = screenHeight * 0.18f
            val middleRx = screenWidth * 0.44f
            val middleRy = screenHeight * 0.25f
            val outerRx = screenWidth * 0.47f
            val outerRy = screenHeight * 0.30f

            fun calculateOrbitalPosition(
                baseDeg: Float,
                currentRotationDeg: Float,
                rx: Dp,
                ry: Dp
            ): Pair<Dp, Dp> {
                val totalRad = Math.toRadians(((baseDeg + currentRotationDeg) % 360.0))
                val px = centerX + (rx.value * Math.cos(totalRad).toFloat()).dp
                val py = centerY + (ry.value * Math.sin(totalRad).toFloat()).dp
                return Pair(px, py)
            }

            // --- Inner Orbit Planets (3 Planets: Products, Dashboard, Inventory) ---
            val (p0X, p0Y) = calculateOrbitalPosition(0f, innerOrbitAngle, innerRx, innerRy)
            CosmicPlanetNode(
                planet = planetsList[0], // المنتجات
                xOffset = p0X,
                yOffset = p0Y,
                onInspect = { activePlanetForInspection = it }
            )

            val (p1X, p1Y) = calculateOrbitalPosition(120f, innerOrbitAngle, innerRx, innerRy)
            CosmicPlanetNode(
                planet = planetsList[1], // لوحة التحكم
                xOffset = p1X,
                yOffset = p1Y,
                onInspect = { activePlanetForInspection = it }
            )

            val (p2X, p2Y) = calculateOrbitalPosition(240f, innerOrbitAngle, innerRx, innerRy)
            CosmicPlanetNode(
                planet = planetsList[2], // المخزون
                xOffset = p2X,
                yOffset = p2Y,
                onInspect = { activePlanetForInspection = it }
            )

            // --- Middle Orbit Planets (4 Planets: Sales, Customers, Suppliers, Wholesalers) ---
            val (p3X, p3Y) = calculateOrbitalPosition(30f, middleOrbitAngle, middleRx, middleRy)
            CosmicPlanetNode(
                planet = planetsList[3], // المبيعات والفواتير
                xOffset = p3X,
                yOffset = p3Y,
                onInspect = { activePlanetForInspection = it }
            )

            val (p4X, p4Y) = calculateOrbitalPosition(120f, middleOrbitAngle, middleRx, middleRy)
            CosmicPlanetNode(
                planet = planetsList[4], // العملاء
                xOffset = p4X,
                yOffset = p4Y,
                onInspect = { activePlanetForInspection = it }
            )

            val (p5X, p5Y) = calculateOrbitalPosition(210f, middleOrbitAngle, middleRx, middleRy)
            CosmicPlanetNode(
                planet = planetsList[5], // الموردون
                xOffset = p5X,
                yOffset = p5Y,
                onInspect = { activePlanetForInspection = it }
            )

            val (p6X, p6Y) = calculateOrbitalPosition(300f, middleOrbitAngle, middleRx, middleRy)
            CosmicPlanetNode(
                planet = planetsList[6], // تجار الجملة
                xOffset = p6X,
                yOffset = p6Y,
                onInspect = { activePlanetForInspection = it }
            )

            // --- Outer Orbit Planets (4 Planets: Payments, Expenses, Reports, Settings) ---
            val (p7X, p7Y) = calculateOrbitalPosition(15f, outerOrbitAngle, outerRx, outerRy)
            CosmicPlanetNode(
                planet = planetsList[7], // المدفوعات
                xOffset = p7X,
                yOffset = p7Y,
                onInspect = { activePlanetForInspection = it }
            )

            val (p8X, p8Y) = calculateOrbitalPosition(105f, outerOrbitAngle, outerRx, outerRy)
            CosmicPlanetNode(
                planet = planetsList[8], // المصروفات
                xOffset = p8X,
                yOffset = p8Y,
                onInspect = { activePlanetForInspection = it }
            )

            val (p9X, p9Y) = calculateOrbitalPosition(195f, outerOrbitAngle, outerRx, outerRy)
            CosmicPlanetNode(
                planet = planetsList[9], // التقارير
                xOffset = p9X,
                yOffset = p9Y,
                onInspect = { activePlanetForInspection = it }
            )

            val (p10X, p10Y) = calculateOrbitalPosition(285f, outerOrbitAngle, outerRx, outerRy)
            CosmicPlanetNode(
                planet = planetsList[10], // الإعدادات
                xOffset = p10X,
                yOffset = p10Y,
                onInspect = { activePlanetForInspection = it }
            )

            // 4. Curved Earth Horizon at Bottom
            Image(
                painter = painterResource(id = R.drawable.cosmic_earth_horizon),
                contentDescription = "أفق كوكب الأرض",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(150.dp)
            )

            // 5. Voice Assistant «بشير» (Safely elevated above bottom navigation bar)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sound Waveforms
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val waveHeights = listOf(4, 8, 12, 16, 20, 16, 12, 8, 4)
                    waveHeights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height((h * waveAlpha).dp)
                                .background(Color(0xFFFFD54F).copy(alpha = waveAlpha), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }

                    Spacer(modifier = Modifier.width(66.dp))

                    waveHeights.reversed().forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height((h * waveAlpha).dp)
                                .background(Color(0xFFFFD54F).copy(alpha = waveAlpha), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Basheer Energy Orb
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFF6D1),
                                    Color(0xFFFFD54F),
                                    Color(0xFFE5B63E),
                                    Color(0xFF0C1830)
                                )
                            )
                        )
                        .border(2.5.dp, Color(0xFFFFD54F), CircleShape)
                        .clickable(onClick = onBasheerClick)
                        .testTag("cosmic_basheer_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "المساعد الصوتي بشير",
                        tint = Color(0xFF040916),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "بشير",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = Color(0xFFFFD54F),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 6. Top Header with Crescent Logo & App Directory Button (Clean Status Bar insets)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Crescent Brand Header
            Image(
                painter = painterResource(id = R.drawable.albashiri_crescent_header_clean),
                contentDescription = "شعار البشيري للأجهزة الكهربائية",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(46.dp)
                    .width(180.dp)
            )

            // Button: «أقسام التطبيق» (Persistent App Sections Directory)
            Surface(
                onClick = { showSectionsDirectory = true },
                shape = RoundedCornerShape(20.dp),
                color = Color(0xD90A1733),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x66FFD54F)),
                modifier = Modifier.testTag("btn_open_sections_directory")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "أقسام التطبيق",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFFF6D1)
                    )
                }
            }
        }

        // 7. Planet Inspection Card (بطاقة تعريف الكوكب عند الضغط)
        AnimatedVisibility(
            visible = activePlanetForInspection != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 85.dp, start = 16.dp, end = 16.dp)
        ) {
            activePlanetForInspection?.let { planet ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xF209142A)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, planet.accentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(25.dp, spotColor = planet.accentColor)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        // Header with Title and Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(planet.accentColor.copy(alpha = 0.2f))
                                        .border(1.2.dp, planet.accentColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = planet.icon,
                                        contentDescription = planet.name,
                                        tint = planet.accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = planet.name,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = Color(0xFFFFF6D1)
                                    )
                                    Text(
                                        text = planet.category,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { activePlanetForInspection = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description
                        Text(
                            text = planet.description,
                            fontFamily = CairoFontFamily,
                            fontSize = 12.5.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats & Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stat Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F1E3D),
                                border = androidx.compose.foundation.BorderStroke(1.dp, planet.accentColor.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = planet.statValue,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = planet.accentColor
                                    )
                                    Text(
                                        text = planet.statLabel,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Button «دخول القسم»
                            Button(
                                onClick = {
                                    val target = activePlanetForInspection ?: return@Button
                                    activePlanetForInspection = null
                                    onEnterPlanet(target)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD54F),
                                    contentColor = Color(0xFF040A18)
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                                modifier = Modifier.testTag("btn_enter_section")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "دخول القسم",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Sections Directory Modal Dialog («دليل أقسام التطبيق»)
        if (showSectionsDirectory) {
            Dialog(onDismissRequest = { showSectionsDirectory = false }) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color(0xF7070F22),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 580.dp)
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        // Title Bar
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
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "دليل أقسام المعرض الكوني",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFFFFF6D1)
                                )
                            }

                            IconButton(
                                onClick = { showSectionsDirectory = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Scrollable List of All 11 Sections
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(planetsList) { planet ->
                                Surface(
                                    onClick = {
                                        showSectionsDirectory = false
                                        onEnterPlanet(planet)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0x660F1F3D),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, planet.accentColor.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(planet.accentColor.copy(alpha = 0.15f))
                                                .border(1.dp, planet.accentColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = planet.icon,
                                                contentDescription = planet.name,
                                                tint = planet.accentColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = planet.name,
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = Color(0xFFFFF6D1)
                                            )
                                            Text(
                                                text = planet.category,
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.RocketLaunch,
                                            contentDescription = null,
                                            tint = planet.accentColor,
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
}

/**
 * 3D-styled Planetary Node with High-Contrast Legible Arabic Name Capsule.
 */
@Composable
private fun CosmicPlanetNode(
    planet: CosmicPlanetDef,
    xOffset: Dp,
    yOffset: Dp,
    onInspect: (CosmicPlanetDef) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .offset(x = xOffset - 36.dp, y = yOffset - 36.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onInspect(planet) }
            )
            .testTag("planet_${planet.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(if (planet.isFeatured) 64.dp else 50.dp),
            contentAlignment = Alignment.Center
        ) {
            // Golden Ring for Products Planet
            if (planet.isFeatured) {
                Canvas(modifier = Modifier.size(64.dp)) {
                    drawOval(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0x33FFD54F),
                                Color(0xFFFFD54F),
                                Color(0x77FFD54F),
                                Color.Transparent
                            )
                        ),
                        topLeft = Offset(0f, size.height * 0.35f),
                        size = Size(size.width, size.height * 0.3f),
                        style = Stroke(width = 2.4f)
                    )
                }
            }

            // 3D Spherical Planet Body with Lighting Gradient
            Box(
                modifier = Modifier
                    .size(if (planet.isFeatured) 48.dp else 40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                planet.accentColor,
                                planet.accentColor.copy(alpha = 0.85f),
                                Color(0xFF0B1933),
                                Color(0xFF020612)
                            ),
                            center = Offset(14f, 14f)
                        )
                    )
                    .border(
                        width = if (planet.isFeatured) 2.dp else 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(planet.accentColor, planet.accentColor.copy(alpha = 0.35f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = planet.icon,
                    contentDescription = planet.name,
                    tint = if (planet.isFeatured) Color(0xFF040916) else Color.White,
                    modifier = Modifier.size(if (planet.isFeatured) 24.dp else 19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // High-Contrast Frosted Capsule for Name
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xE6081428),
            border = androidx.compose.foundation.BorderStroke(1.dp, planet.accentColor.copy(alpha = 0.5f))
        ) {
            Text(
                text = planet.name,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (planet.isFeatured) 11.sp else 10.sp,
                color = if (planet.isFeatured) Color(0xFFFFD54F) else Color(0xFFF1F5F9),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
    }
}
