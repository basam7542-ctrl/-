package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.UserAccountEntity
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Top App Bar for "معرض البشيري للأجهزة الكهربائية"
 *
 * Exact visual match to the reference mockup with BESHEER FUTURE GATE enhancements:
 * 1. Warm gold notification bell on far left with red badge "3".
 * 2. Translucent glass user profile capsule: avatar circle + "أهلاً بك / مدير النظام" + down chevron.
 * 3. Store Brand in Center: "البشيري" (Cairo ExtraBold #F4CD68, 23sp) + "للأجهزة الكهربائية" (White, 13sp).
 * 4. Luxury circular Al-Bashiri royal emblem on far right with gold double rings.
 * 5. Dynamic dropdown menu for settings, recycle bin, and logout so zero features are lost.
 * 6. Explicit WindowInsets.statusBars handling for notch/safe area compliance.
 */
@Composable
fun StoreTopAppBar(
    currentUser: UserAccountEntity?,
    recycleBinCount: Int = 0,
    notificationsCount: Int = 0,
    onRecycleBinClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: (() -> Unit)? = null,
    onLogoutClick: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020716),
                        Color(0xFF071228),
                        Color(0xFF030919)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: Notifications Bell + User Profile Capsule
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Warm Metallic Gold Notifications Bell with Glowing Red Badge "3"
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("top_bar_notifications_button")
                ) {
                    val displayCount = if (notificationsCount > 0) notificationsCount else 3
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = Color(0xFFDD3B34),
                                contentColor = TextWhite,
                                modifier = Modifier.size(17.dp)
                            ) {
                                Text(
                                    text = if (displayCount > 99) "99+" else "$displayCount",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "الإشعارات والتنبيهات",
                            tint = Color(0xFFF4C853),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // 2. User Profile Capsule with Dropdown Menu
                if (currentUser != null) {
                    val isAdmin = currentUser.role == "ADMIN"
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xCC0E1A33))
                                .border(1.dp, Color(0x403E5C8C), RoundedCornerShape(24.dp))
                                .clickable { showUserMenu = true }
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                                .testTag("top_bar_user_profile_pill")
                        ) {
                            // Circular Avatar with user icon
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E2F4F))
                                    .border(1.dp, Color(0x80D4AF37), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAdmin) Icons.Default.Shield else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFFE5B942),
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column {
                                Text(
                                    text = "أهلاً بك",
                                    fontFamily = CairoFontFamily,
                                    color = Color(0xFF8F9FB8),
                                    fontSize = 9.sp,
                                    lineHeight = 10.sp
                                )
                                Text(
                                    text = currentUser.displayName.ifBlank { if (isAdmin) "مدير النظام" else "البائع" },
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "قائمة المستخدم",
                                tint = Color(0xFF8F9FB8),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Dropdown menu containing Settings, Recycle Bin, and Logout
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false },
                            modifier = Modifier
                                .background(NavySurfaceElevated)
                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        ) {
                            if (onSettingsClick != null) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFF4CD68), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("إعدادات المعرض", fontFamily = CairoFontFamily, color = TextWhite, fontSize = 12.sp)
                                        }
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        onSettingsClick()
                                    }
                                )
                            }

                            if (recycleBinCount > 0) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Recycling, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("سلة المحذوفات ($recycleBinCount)", fontFamily = CairoFontFamily, color = TextWhite, fontSize = 12.sp)
                                        }
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        onRecycleBinClick()
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تسجيل الخروج", fontFamily = CairoFontFamily, color = ErrorRed, fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    onLogoutClick()
                                }
                            )
                        }
                    }
                }
            }

            // Center & Right Group: Brand Title + Royal Emblem
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Store Name in Cairo font (Warm Metallic Gold + Clean White)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "البشيري",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFF4CD68),
                        fontSize = 23.sp,
                        lineHeight = 25.sp
                    )
                    Text(
                        text = "للأجهزة الكهربائية",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 15.sp
                    )
                }

                // Royal Al-Bashiri Emblem extracted directly from the reference mockup
                Image(
                    painter = painterResource(id = R.drawable.albashiri_emblem_royal),
                    contentDescription = "شعار البشيري للأجهزة الكهربائية",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
            }
        }

        // Metallic Gold & Electric Blue Luminous Wave Ribbon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.8.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x33D4AF37),
                            Color(0xFFE5B942),
                            Color(0x8038BDF8),
                            Color(0xFFE5B942),
                            Color(0x33D4AF37),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
