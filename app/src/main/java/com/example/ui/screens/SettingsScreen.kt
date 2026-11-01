package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.local.UserAccountEntity
import com.example.ui.components.BluetoothPrinterChooserDialog
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
import com.example.ui.theme.WarningAmber
import com.example.utils.BluetoothPrinterHelper
import com.example.utils.StoreProfile
import com.example.utils.StoreProfileManager

@Composable
fun SettingsScreen(
    currentUser: UserAccountEntity,
    allUsers: List<UserAccountEntity>,
    isTtsEnabled: Boolean,
    onToggleTts: (Boolean) -> Unit,
    isTtsOperationsEnabled: Boolean,
    onToggleTtsOperations: (Boolean) -> Unit,
    onUpdateCustomGreeting: (userId: String, greeting: String) -> Unit,
    onPreviewGreeting: (greeting: String?, displayName: String) -> Unit,
    onPasswordChange: (currentPin: String, newPin: String, confirmPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onGenerateBackupCsv: () -> String,
    savedPrinter: BluetoothPrinterHelper.PrinterDevice?,
    storeName: String,
    isPrinting: Boolean,
    onSelectPrinter: (BluetoothPrinterHelper.PrinterDevice) -> Unit,
    onClearPrinter: () -> Unit,
    onUpdateStoreName: (String) -> Unit,
    onPrintTestReceipt: ((Boolean, String) -> Unit) -> Unit,
    recycleBinCount: Int = 0,
    onNavigateToRecycleBin: () -> Unit = {},
    storeProfile: StoreProfile = StoreProfile(),
    onUpdateStoreProfile: (StoreProfile) -> Unit = {},
    onUploadLogo: (android.net.Uri) -> Unit = {},
    onResetLogo: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isAdmin = currentUser.role == "ADMIN"

    // Password change fields
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var passwordChangeSuccess by remember { mutableStateOf(false) }
    var passwordChangeError by remember { mutableStateOf<String?>(null) }

    var backupCopiedSuccess by remember { mutableStateOf(false) }

    // Showroom, Legal & Tax Profile State
    var profileStoreNameInput by remember(storeProfile.storeName) { mutableStateOf(storeProfile.storeName) }
    var profileAddressInput by remember(storeProfile.storeAddress) { mutableStateOf(storeProfile.storeAddress) }
    var profilePhoneInput by remember(storeProfile.storePhone) { mutableStateOf(storeProfile.storePhone) }
    var profileCrInput by remember(storeProfile.commercialRegister) { mutableStateOf(storeProfile.commercialRegister) }
    var profileTaxCardInput by remember(storeProfile.taxCard) { mutableStateOf(storeProfile.taxCard) }
    var profileVatInput by remember(storeProfile.vatNumber) { mutableStateOf(storeProfile.vatNumber) }
    var storeProfileSavedSuccess by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUploadLogo(uri)
            storeProfileSavedSuccess = false
        }
    }

    // Selected user for custom greeting configuration (Admin can switch between all 4 users; Seller sees self)
    val userList = if (allUsers.isNotEmpty()) {
        allUsers
    } else {
        listOf(
            UserAccountEntity("admin", "المدير", "ADMIN", "1234"),
            UserAccountEntity("seller1", "بائع 1", "SELLER", "1111"),
            UserAccountEntity("seller2", "بائع 2", "SELLER", "2222"),
            UserAccountEntity("seller3", "بائع 3", "SELLER", "3333")
        )
    }

    var selectedGreetingUserId by remember(currentUser.id) {
        mutableStateOf(if (isAdmin) "admin" else currentUser.id)
    }

    val selectedTargetUser = userList.find { it.id == selectedGreetingUserId }
        ?: userList.firstOrNull()
        ?: currentUser

    var customGreetingInput by remember(selectedTargetUser.id, selectedTargetUser.customGreeting) {
        mutableStateOf(selectedTargetUser.customGreeting)
    }
    var greetingSavedSuccess by remember { mutableStateOf(false) }

    // Bluetooth Thermal Printer state
    var showPrinterChooser by remember { mutableStateOf(false) }
    var printerFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var storeNameInput by remember(storeName) { mutableStateOf(storeName) }
    var storeNameSavedSuccess by remember { mutableStateOf(false) }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showPrinterChooser = true
        } else {
            Toast.makeText(context, "إذن البلوتوث مطلوب للبحث عن الطابعات المقترنة", Toast.LENGTH_SHORT).show()
        }
    }

    val openPrinterChooser = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                showPrinterChooser = true
            } else {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            showPrinterChooser = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Header Profile Card - Executive Styling
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorderGlow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(GoldContainer)
                            .border(1.5.dp, GoldPrimary, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isAdmin) Icons.Default.Security else Icons.Default.Person,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isAdmin) GoldPrimary.copy(alpha = 0.2f) else ElectricBlueContainer)
                                    .border(0.6.dp, if (isAdmin) GoldPrimary else ElectricBlue, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isAdmin) "مدير" else "بائع",
                                    color = if (isAdmin) GoldPrimary else ElectricBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = if (isAdmin) "صلاحيات كاملة لإدارة النظام، الأسعار، والمخزون" else "صلاحية استعراض الأسعار وتنفيذ المبيعات",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Recycle Bin Entry Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRecycleBin() }
                    .testTag("settings_recycle_bin_card"),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (recycleBinCount > 0) GoldPrimary.copy(alpha = 0.6f) else NavyCardBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurfaceElevated)
                            .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Recycling,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "سلة المحذوفات المتكاملة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            if (recycleBinCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElectricBlueContainer)
                                        .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$recycleBinCount عنصر",
                                        color = ElectricBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (recycleBinCount > 0)
                                "توجد عناصر محذوفة يمكنك استعادتها أو إفراغها"
                            else
                                "نظام الحذف الآمن وحفظ السجلات والعلاقات",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "فتح",
                        tint = GoldSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // SECTION: Showroom, Legal & Tax Information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("showroom_legal_tax_card"),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorderGlow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GoldContainer)
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "بيانات المعرض والبيانات القانونية والضريبية",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "تُدرج تلقائياً في رأس جميع فواتير البيع وإيصالات الطباعة الحرارية",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (!isAdmin) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NavySurfaceElevated)
                                    .border(0.8.dp, GoldSecondary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "للعرض فقط (المدير)",
                                    color = GoldSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isAdmin) {
                        // Editable Form for Admin
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // 1. Store Name
                            Column {
                                Text(
                                    text = "1. اسم المعرض التجاري:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = profileStoreNameInput,
                                    onValueChange = {
                                        profileStoreNameInput = it
                                        storeProfileSavedSuccess = false
                                    },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(Icons.Default.Store, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    },
                                    placeholder = { Text("مثال: البشيري للأجهزة الكهربائية", color = TextMuted) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = NavyCardBorder,
                                        cursorColor = GoldPrimary,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("store_name_input")
                                )
                            }

                            // 2. Store Address
                            Column {
                                Text(
                                    text = "2. عنوان المعرض بالتفصيل:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = profileAddressInput,
                                    onValueChange = {
                                        profileAddressInput = it
                                        storeProfileSavedSuccess = false
                                    },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    },
                                    placeholder = { Text("مثال: شارع الجمهورية - أمام المحطة - المنصورة", color = TextMuted) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = NavyCardBorder,
                                        cursorColor = GoldPrimary,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("store_address_input")
                                )
                            }

                            // 3. Store Phone
                            Column {
                                Text(
                                    text = "3. رقم هاتف المعرض / الإدارة:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = profilePhoneInput,
                                    onValueChange = {
                                        profilePhoneInput = it
                                        storeProfileSavedSuccess = false
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    leadingIcon = {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    },
                                    placeholder = { Text("مثال: 01000000000", color = TextMuted) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = NavyCardBorder,
                                        cursorColor = GoldPrimary,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("store_phone_input")
                                )
                            }

                            // 4 & 5. Commercial Register & Tax Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "4. رقم السجل التجاري:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = profileCrInput,
                                        onValueChange = {
                                            profileCrInput = it
                                            storeProfileSavedSuccess = false
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        placeholder = { Text("مثال: 48921", color = TextMuted) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = NavyCardBorder,
                                            cursorColor = GoldPrimary,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("store_cr_input")
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "5. رقم البطاقة الضريبية:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = profileTaxCardInput,
                                        onValueChange = {
                                            profileTaxCardInput = it
                                            storeProfileSavedSuccess = false
                                        },
                                        singleLine = true,
                                        placeholder = { Text("مثال: 712-340-908", color = TextMuted) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = NavyCardBorder,
                                            cursorColor = GoldPrimary,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("store_tax_card_input")
                                    )
                                }
                            }

                            // 6. VAT Registration Number
                            Column {
                                Text(
                                    text = "6. رقم التسجيل الضريبي (إن وجد):",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = profileVatInput,
                                    onValueChange = {
                                        profileVatInput = it
                                        storeProfileSavedSuccess = false
                                    },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(Icons.Default.Badge, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    },
                                    placeholder = { Text("اختياري - مثال: 300-123-456", color = TextMuted) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = NavyCardBorder,
                                        cursorColor = GoldPrimary,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("store_vat_input")
                                )
                            }

                            // 7. Store Logo Upload & Preview
                            Column {
                                Text(
                                    text = "7. شعار المعرض المطبوع أعلى الفاتورة:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                val customLogo = remember(storeProfile.logoPath) {
                                    StoreProfileManager.loadLogoBitmap(storeProfile.logoPath)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NavySurfaceElevated)
                                        .border(1.dp, NavyCardBorder, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(androidx.compose.ui.graphics.Color.White)
                                            .border(1.dp, androidx.compose.ui.graphics.Color.Black, RoundedCornerShape(8.dp))
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (customLogo != null) {
                                            Image(
                                                bitmap = customLogo.asImageBitmap(),
                                                contentDescription = "شعار المعرض المخصص",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_albashiri_invoice_logo),
                                                contentDescription = "شعار المعرض الافتراضي",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (customLogo != null) "شعار مخصص تم رفعه بنجاح" else "الشعار الافتراضي لمؤسسة البشيري",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (customLogo != null) SuccessGreen else TextWhite
                                        )
                                        Text(
                                            text = "شعار أحادي اللون مناسب للطباعة الحرارية بالأبيض والأسود",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    logoPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("upload_logo_button")
                                            ) {
                                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = if (customLogo != null) "تغيير الشعار" else "رفع شعار جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            if (storeProfile.logoPath.isNotBlank()) {
                                                OutlinedButton(
                                                    onClick = onResetLogo,
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldSecondary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.testTag("reset_logo_button")
                                                ) {
                                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = "استعادة الافتراضي", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Save Button
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    onUpdateStoreProfile(
                                        StoreProfile(
                                            storeName = profileStoreNameInput.trim().ifBlank { StoreProfile.DEFAULT_STORE_NAME },
                                            storeAddress = profileAddressInput.trim(),
                                            storePhone = profilePhoneInput.trim(),
                                            commercialRegister = profileCrInput.trim(),
                                            taxCard = profileTaxCardInput.trim(),
                                            vatNumber = profileVatInput.trim(),
                                            logoPath = storeProfile.logoPath
                                        )
                                    )
                                    storeProfileSavedSuccess = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_store_profile_button")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "حفظ بيانات المعرض والبيانات القانونية والضريبية",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }

                            if (storeProfileSavedSuccess) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SuccessGreen.copy(alpha = 0.15f))
                                        .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "تم حفظ بيانات المعرض والبيانات القانونية والضريبية بنجاح، وستُدرج تلقائياً في جميع الفواتير الجديدة والمعاينة.",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Read-Only Presentation for Sellers
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavySurfaceElevated)
                                .border(1.dp, NavyCardBorder, RoundedCornerShape(10.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "بيانات المعرض الرسمية المعتمدة بالفواتير:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Text(
                                text = "• اسم المعرض: ${storeProfile.storeName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhite
                            )
                            if (storeProfile.storeAddress.isNotBlank()) {
                                Text(
                                    text = "• العنوان: ${storeProfile.storeAddress}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                            }
                            if (storeProfile.storePhone.isNotBlank()) {
                                Text(
                                    text = "• هاتف المعرض: ${storeProfile.storePhone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                            }
                            if (storeProfile.commercialRegister.isNotBlank()) {
                                Text(
                                    text = "• السجل التجاري: ${storeProfile.commercialRegister}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                            }
                            if (storeProfile.taxCard.isNotBlank()) {
                                Text(
                                    text = "• البطاقة الضريبية: ${storeProfile.taxCard}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                            }
                            if (storeProfile.vatNumber.isNotBlank()) {
                                Text(
                                    text = "• التسجيل الضريبي: ${storeProfile.vatNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                            }
                            Text(
                                text = "ملاحظة: تعديل أو إضافة بيانات المعرض والبيانات الضريبية متاح فقط لحساب المدير.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // SECTION 1: Password Change
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(NavyCardBorder)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تغيير كلمة المرور الخاصة بحسابك",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Text(
                        text = "يجب إدخال كلمة المرور الحالية للتأكيد قبل تعيين الكلمة الجديدة",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = {
                            currentPin = it
                            passwordChangeError = null
                            passwordChangeSuccess = false
                        },
                        label = { Text("كلمة المرور الحالية") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("current_pin_input")
                    )

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            newPin = it
                            passwordChangeError = null
                            passwordChangeSuccess = false
                        },
                        label = { Text("كلمة المرور الجديدة") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("new_pin_input")
                    )

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            confirmPin = it
                            passwordChangeError = null
                            passwordChangeSuccess = false
                        },
                        label = { Text("تأكيد كلمة المرور الجديدة") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .testTag("confirm_pin_input")
                    )

                    if (passwordChangeError != null) {
                        Text(
                            text = passwordChangeError!!,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (passwordChangeSuccess) {
                        Text(
                            text = "تم تغيير كلمة المرور بنجاح!",
                            color = SuccessGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            onPasswordChange(
                                currentPin,
                                newPin,
                                confirmPin,
                                {
                                    passwordChangeSuccess = true
                                    passwordChangeError = null
                                    currentPin = ""
                                    newPin = ""
                                    confirmPin = ""
                                },
                                { err ->
                                    passwordChangeError = err
                                    passwordChangeSuccess = false
                                }
                            )
                        },
                        enabled = currentPin.isNotBlank() && newPin.isNotBlank() && confirmPin.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("change_pin_button")
                    ) {
                        Text("حفظ كلمة المرور الجديدة", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECTION 2: Text-to-Speech (TTS) Voice Settings & Custom Greetings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(NavyCardBorder)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إعدادات الصوت والنطق المدمج (TTS)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Login Greeting Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavySurfaceElevated)
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isTtsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = null,
                                    tint = if (isTtsEnabled) GoldPrimary else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "الترحيب الصوتي عند تسجيل الدخول",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = if (isTtsEnabled) "مفعّل (نطق صوتي بصوت عربي رجالي)" else "مكتوم",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isTtsEnabled) GoldSecondary else TextMuted
                                )
                            }
                        }

                        Switch(
                            checked = isTtsEnabled,
                            onCheckedChange = onToggleTts,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldPrimary,
                                checkedTrackColor = GoldPrimary.copy(alpha = 0.35f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = NavySurfaceElevated
                            ),
                            modifier = Modifier.testTag("tts_welcome_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NavyCardBorder)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Operation Confirmation Voice Responses Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavySurfaceElevated)
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isTtsOperationsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = null,
                                    tint = if (isTtsOperationsEnabled) GoldPrimary else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "الردود الصوتية لتأكيد العمليات",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = if (isTtsOperationsEnabled) "مفعّل (تأكيد إضافة منتج أو بيع بصوت رجالي)" else "مكتوم",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isTtsOperationsEnabled) GoldSecondary else TextMuted
                                )
                            }
                        }

                        Switch(
                            checked = isTtsOperationsEnabled,
                            onCheckedChange = onToggleTtsOperations,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldPrimary,
                                checkedTrackColor = GoldPrimary.copy(alpha = 0.35f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = NavySurfaceElevated
                            ),
                            modifier = Modifier.testTag("tts_operations_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Male Arabic Voice Assurance Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(GoldPrimary.copy(alpha = 0.35f)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavySurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "نوع الصوت: صوت عربي رجالي (ذكر)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                                Text(
                                    text = "محرك النطق المدمج بالجهاز مضبوط على الصوت الرجالي الفخم بنبرة هادئة ورصينة لكافة الترحيبات والردود.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NavyCardBorder)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Custom Greeting Section per User
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "تخصيص نص رسالة الترحيب الصوتي",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isAdmin) "يمكن للمدير تخصيص رسالة ترحيب لكل حساب" else "يمكنك تخصيص رسالة ترحيب خاصة بحسابك",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // User Account Selector (Admin can switch between all 4 users; Seller sees their own account)
                    if (isAdmin) {
                        Text(
                            text = "اختر الحساب للتعديل:",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            userList.forEach { user ->
                                val isSelected = user.id == selectedTargetUser.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoldPrimary else NavySurfaceElevated)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) GoldPrimary else NavyCardBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedGreetingUserId = user.id
                                            customGreetingInput = user.customGreeting
                                            greetingSavedSuccess = false
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = user.displayName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = if (isSelected) TextDark else TextWhite
                                        )
                                        if (user.customGreeting.isNotBlank()) {
                                            Text(
                                                text = "مخصصة",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) TextDark.copy(alpha = 0.8f) else GoldSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Active Target Account Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(NavyCardBorder))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (selectedTargetUser.role == "ADMIN") Icons.Default.Security else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "رسالة الترحيب لـ: ${selectedTargetUser.displayName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }

                                if (customGreetingInput.isNotBlank()) {
                                    Text(
                                        text = "مسح / الافتراضي",
                                        color = ErrorRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                customGreetingInput = ""
                                                greetingSavedSuccess = false
                                            }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = customGreetingInput,
                                onValueChange = {
                                    customGreetingInput = it
                                    greetingSavedSuccess = false
                                },
                                label = { Text("نص رسالة الترحيب المخصصة") },
                                placeholder = {
                                    Text(
                                        if (selectedTargetUser.role == "ADMIN") "الافتراضي: أهلاً بك يا مدير" else "الافتراضي: أهلاً بك يا ${selectedTargetUser.displayName}",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                },
                                singleLine = false,
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = NavyCardBorder,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_greeting_input_${selectedTargetUser.id}")
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "رسالة الترحيب المخصصة اختيارية - إذا تُرك الحقل فارغاً، يتم نطق الترحيب الافتراضي تلقائياً.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Preview speech button
                                OutlinedButton(
                                    onClick = {
                                        onPreviewGreeting(
                                            customGreetingInput.ifBlank { null },
                                            selectedTargetUser.displayName
                                        )
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                                    border = ButtonDefaults.outlinedButtonBorder().copy(brush = SolidColor(GoldPrimary)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("preview_greeting_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("استماع للتجربة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                // Save greeting button
                                Button(
                                    onClick = {
                                        onUpdateCustomGreeting(selectedTargetUser.id, customGreetingInput.trim())
                                        greetingSavedSuccess = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_greeting_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("حفظ الترحيب", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            if (greetingSavedSuccess) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SuccessGreen.copy(alpha = 0.15f))
                                        .padding(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (customGreetingInput.isNotBlank())
                                            "تم حفظ الترحيب الصوتي المخصص لـ (${selectedTargetUser.displayName}) بنجاح!"
                                        else
                                            "تمت استعادة الترحيب الافتراضي لـ (${selectedTargetUser.displayName}) بنجاح!",
                                        color = SuccessGreen,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION: Bluetooth Thermal Receipt Printer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bluetooth_printer_card"),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(NavyCardBorder)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavySurfaceElevated)
                                .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "الاتصال بطابعة بلوتوث (حرارية / فواتير)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "طابعات الإيصالات القياسية الشائعة بروتوكول ESC/POS (58mm / 80mm)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Current Connection Status Box
                    if (savedPrinter != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessGreen.copy(alpha = 0.12f))
                                .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "متصلة بـ: ${savedPrinter.name}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                            Text(
                                                text = "العنوان: ${savedPrinter.address}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SuccessGreen
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = onClearPrinter,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = SolidColor(ErrorRed.copy(alpha = 0.7f))),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("إلغاء الربط", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Test Print Button
                                Button(
                                    onClick = {
                                        onPrintTestReceipt { success, msg ->
                                            printerFeedbackMessage = msg
                                        }
                                    },
                                    enabled = !isPrinting,
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("test_print_button")
                                ) {
                                    if (isPrinting) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextDark, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("جاري إرسال الاختبار للطابعة...", fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("طباعة إيصال تجريبي لاختبار الاتصال والورق", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(WarningAmber.copy(alpha = 0.12f))
                                .border(1.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "لا توجد طابعة متصلة حالياً",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "اختر طابعة حرارية من الأجهزة المقترنة بالبلوتوث أدناه ليتم إرسال إيصال البيع إليها فور تسجيله.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    if (printerFeedbackMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = printerFeedbackMessage!!,
                            color = if (printerFeedbackMessage!!.contains("نجاح")) SuccessGreen else WarningAmber,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Select Paired Printer Button
                    Button(
                        onClick = openPrinterChooser,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (savedPrinter == null) GoldPrimary else NavySurfaceElevated,
                            contentColor = if (savedPrinter == null) TextDark else GoldPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_paired_printers_button")
                    ) {
                        Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (savedPrinter == null) "البحث عن الطابعات المقترنة بالجهاز واختيار واحدة" else "تغيير الطابعة المختارة",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NavyCardBorder)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Store Name for Receipt Header
                    Text(
                        text = "اسم المعرض / المحل المطبوع أعلى الإيصال:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = storeNameInput,
                            onValueChange = {
                                storeNameInput = it
                                storeNameSavedSuccess = false
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Store, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = NavyCardBorder,
                                cursorColor = GoldPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                onUpdateStoreName(storeNameInput)
                                storeNameSavedSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("حفظ", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (storeNameSavedSuccess) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تم تحديث اسم المحل على الفواتير بنجاح",
                            color = SuccessGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Printer Chooser Dialog
            if (showPrinterChooser) {
                val pairedPrinters = remember(showPrinterChooser) {
                    BluetoothPrinterHelper.getPairedPrinters(context)
                }
                var dialogList by remember { mutableStateOf(pairedPrinters) }

                BluetoothPrinterChooserDialog(
                    pairedDevices = dialogList,
                    currentSelectedAddress = savedPrinter?.address,
                    onSelectDevice = { printer ->
                        onSelectPrinter(printer)
                        showPrinterChooser = false
                        Toast.makeText(context, "تم تعيين الطابعة: ${printer.name}", Toast.LENGTH_SHORT).show()
                    },
                    onRefresh = {
                        dialogList = BluetoothPrinterHelper.getPairedPrinters(context)
                    },
                    onDismiss = { showPrinterChooser = false }
                )
            }

            // SECTION 3: Data Backup & CSV Export (Admin only)
            if (isAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(GoldPrimary.copy(alpha = 0.5f))),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "النسخة الاحتياطية وتصدير البيانات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Text(
                            text = "تصدير شامل لجميع بيانات المحل (المنتجات، الموديلات، المخزون، سجل المبيعات، كشوف الموردين والتجار) بتنسيق Excel / CSV.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Copy to clipboard button
                            Button(
                                onClick = {
                                    val csvContent = onGenerateBackupCsv()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("AlBashiri Backup", csvContent)
                                    clipboard.setPrimaryClip(clip)
                                    backupCopiedSuccess = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceElevated, contentColor = GoldPrimary),
                                border = ButtonDefaults.outlinedButtonBorder().copy(brush = SolidColor(GoldPrimary)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("نسخ التقرير", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Share / Export File Button
                            Button(
                                onClick = {
                                    val csvContent = onGenerateBackupCsv()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, csvContent)
                                        putExtra(Intent.EXTRA_TITLE, "نسخة احتياطية - البشيري للأجهزة الكهربائية")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "مشاركة النسخة الاحتياطية")
                                    context.startActivity(shareIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مشاركة / تصدير", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (backupCopiedSuccess) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "تم نسخ التقرير الكامل إلى الحافظة بنجاح، يمكنك لصقه في ملف Excel مباشرة",
                                color = SuccessGreen,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // SECTION 4: Store & App Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(NavyCardBorder)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "عن النظام",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "نظام البشيري لإدارة مبيعات ومخزون الأجهزة الكهربائية",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldSecondary
                    )
                    Text(
                        text = "نظام داخلي محمي بقاعدة بيانات SQLite دائمة ومصمم لسرعة الوصول إلى الأسعار وتنفيذ المبيعات وحماية الحدود الدنيا للأسعار.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    OutlinedButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = SolidColor(ErrorRed)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل الخروج والتبديل لحساب آخر", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
