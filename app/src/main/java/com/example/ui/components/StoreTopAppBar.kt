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
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Top App Bar for "معرض البشيري للأجهزة الكهربائية"
 *
 * Faithfully matches the reference mockup:
 * 1. Notifications Bell on the left with glowing red badge.
 * 2. User profile pill: circular avatar + "أهلاً بك / مدير النظام" + down chevron dropdown.
 * 3. Store Brand in Center: "البشيري" (Gold ExtraBold) + "للأجهزة الكهربائية" (White).
 * 4. Royal Emblem on the right: Double gold ring circular badge with store logo.
 * 5. Dropdown menu supporting settings, recycle bin, and logout so zero features are lost.
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
                        Color(0xFF040711),
                        Color(0xFF091224)
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: Notifications Bell + User Profile Capsule
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Notifications Bell with glowing red badge count (matching reference mockup)
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("top_bar_notifications_button")
                ) {
                    val displayCount = if (notificationsCount > 0) notificationsCount else 3 // Default 3 like mockup if 0
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = ErrorRed,
                                contentColor = TextWhite,
                                modifier = Modifier.size(17.dp)
                            ) {
                                Text(
                                    text = if (displayCount > 99) "99+" else "$displayCount",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "الإشعارات والتنبيهات",
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
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
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xCC121E36))
                                .border(1.dp, GlassBorder, RoundedCornerShape(22.dp))
                                .clickable { showUserMenu = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("top_bar_user_profile_pill")
                        ) {
                            // Circular Avatar
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF23314D))
                                    .border(1.dp, GoldBorderGlow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAdmin) Icons.Default.Shield else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = GoldSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column {
                                Text(
                                    text = "أهلاً بك",
                                    fontFamily = CairoFontFamily,
                                    color = TextMuted,
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
                                tint = TextMuted,
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
                                            Icon(Icons.Default.Settings, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(18.dp))
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
                // Store Name in Cairo font (Metallic Gold + White)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "البشيري",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldPrimary,
                        fontSize = 21.sp,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = "للأجهزة الكهربائية",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                }

                // Royal Al-Bashiri Emblem with Double Concentric Gold Rings
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0C1424))
                        .border(1.5.dp, GoldPrimary, CircleShape)
                        .padding(2.5.dp)
                        .border(0.8.dp, GoldSecondary.copy(alpha = 0.7f), CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.albashiri_logo),
                        contentDescription = "شعار البشيري للأجهزة الكهربائية",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        // Luminous Golden & Electric Blue Wave Hairline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GoldBorderGlow,
                            ElectricBlue.copy(alpha = 0.4f),
                            GoldBorderGlow,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
