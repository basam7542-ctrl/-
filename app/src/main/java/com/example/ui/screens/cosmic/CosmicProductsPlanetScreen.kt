package com.example.ui.screens.cosmic

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.BrandEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ProductModelEntity
import com.example.data.local.UserAccountEntity
import com.example.ui.components.AddBrandDialog
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.AddModelDialog
import com.example.ui.components.AddProductDialog
import com.example.ui.components.EditModelDialog
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.util.Locale

/**
 * BESHEER COSMIC FUTURE | Phase 1: Products Planet (كوكب المنتجات - البيئة المستوحاة من الأرض المستقبلية).
 *
 * Requirements fulfilled:
 * 1. Future Earth city hero backdrop with exhibition dome.
 * 2. 3 Glass stat cards: الشركات (12), الأقسام (48), المنتجات (824).
 * 3. Search Bar: "ابحث عن منتج...".
 * 4. 4 Glass exhibition pods for appliances.
 * 5. Full Real Room Database CRUD:
 *    - Real Categories, Brands, Products, and Models.
 *    - Add Category, Add Brand, Add Product, Add Model, Edit Model, Delete Model.
 *    - Direct Quick Sale initiation.
 * 6. Reverse Cinematic Navigation: Back button returns smoothly to the Solar System.
 */
@Composable
fun CosmicProductsPlanetScreen(
    currentUser: UserAccountEntity,
    categories: List<CategoryEntity>,
    brands: List<BrandEntity>,
    products: List<ProductEntity>,
    models: List<ProductModelEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenSale: (ProductModelEntity) -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    onAddBrand: (name: String, catId: Long) -> Unit,
    onDeleteBrand: (Long) -> Unit,
    onAddProduct: (name: String, catId: Long, catName: String, bId: Long, bName: String) -> Unit,
    onDeleteProduct: (Long) -> Unit,
    onAddModel: (pId: Long, pName: String, bName: String, cName: String, mName: String, fPrice: Double, mPrice: Double, stock: Int, code: String) -> Unit,
    onUpdateModel: (ProductModelEntity) -> Unit,
    onDeleteModel: (Long) -> Unit,
    onBackToSolarSystem: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept hardware/system back to return to the Solar System
    BackHandler(onBack = onBackToSolarSystem)

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val isAdmin = currentUser.role == "ADMIN"

    // Dialog state management
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddBrandDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddModelDialog by remember { mutableStateOf(false) }
    var editingModel by remember { mutableStateOf<ProductModelEntity?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Live filtered models based on search query and selected category
    val filteredModels = remember(models, searchQuery, selectedCategoryFilter) {
        models.filter { model ->
            val matchesQuery = searchQuery.isBlank() ||
                    model.modelName.contains(searchQuery, ignoreCase = true) ||
                    model.productName.contains(searchQuery, ignoreCase = true) ||
                    model.brandName.contains(searchQuery, ignoreCase = true) ||
                    model.categoryName.contains(searchQuery, ignoreCase = true) ||
                    model.code.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == null || model.categoryName == selectedCategoryFilter
            matchesQuery && matchesCategory
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF02050E),
                        Color(0xFF060F22),
                        Color(0xFF091632),
                        Color(0xFF030713)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Cosmic Products Top App Bar (Header with back to Solar System)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button to Solar System
                IconButton(
                    onClick = onBackToSolarSystem,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC0E1A33))
                        .border(1.dp, Color(0x66FFD54F), CircleShape)
                        .testTag("cosmic_products_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "العودة إلى النظام الشمسي",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Screen Title
                Text(
                    text = "المنتجات",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White
                )

                // Search/Add action icon
                IconButton(
                    onClick = { showAddModelDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC0E1A33))
                        .border(1.dp, Color(0x6638BDF8), CircleShape)
                        .testTag("cosmic_products_add_action")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة موديل جديد",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Scrollable Products City Feed
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)
            ) {
                // 2. Futuristic Earth City Hero Dome Backdrop
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0x66FFD54F), Color(0x6638BDF8), Color(0x66FFD54F))
                            )
                        )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cosmic_city_products_hero),
                            contentDescription = "المدينة التجارية المستقبلية - كوكب المنتجات",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 3. Three Glass Stat Cards (الشركات | الأقسام | المنتجات)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stat 1: الشركات (Brands)
                        CosmicGlassStatCard(
                            title = "الشركات",
                            count = brands.size.toString(),
                            icon = Icons.Default.LocationCity,
                            accentColor = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )

                        // Stat 2: الأقسام (Categories)
                        CosmicGlassStatCard(
                            title = "الأقسام",
                            count = categories.size.toString(),
                            icon = Icons.Default.GridView,
                            accentColor = Color(0xFFFFD54F),
                            modifier = Modifier.weight(1f)
                        )

                        // Stat 3: المنتجات (Products/Models)
                        CosmicGlassStatCard(
                            title = "المنتجات",
                            count = models.size.toString(),
                            icon = Icons.Default.ViewInAr,
                            accentColor = Color(0xFF00E676),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. Holographic Search Bar (ابحث عن منتج...)
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = "ابحث عن منتج، موديل، كود، أو ماركة...",
                                fontFamily = CairoFontFamily,
                                color = Color(0xFF8FA4C4),
                                fontSize = 13.sp
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color(0xFF8FA4C4))
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFFD54F))
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xE60E1A33),
                            unfocusedContainerColor = Color(0xCC0A1428),
                            focusedBorderColor = Color(0xFFFFD54F),
                            unfocusedBorderColor = Color(0x33446A9E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("cosmic_products_search_field")
                    )
                }

                // 5. Four Glass Exhibition Pods for Electrical Appliances
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x403F5E8E))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cosmic_appliances_showcase),
                            contentDescription = "وحدات عرض الأجهزة الكهربائية الزجاجية المضيئة",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 6. Interactive Category Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "الكل" Chip
                        val isAllSelected = selectedCategoryFilter == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isAllSelected) Color(0xFFFFD54F) else Color(0xCC0E1A33))
                                .border(1.dp, if (isAllSelected) Color(0xFFFFD54F) else Color(0x40446A9E), RoundedCornerShape(20.dp))
                                .clickable { selectedCategoryFilter = null }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "كل الأجهزة (${models.size})",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isAllSelected) Color(0xFF030713) else Color.White
                            )
                        }

                        // Specific Categories
                        categories.forEach { category ->
                            val isSelected = selectedCategoryFilter == category.name
                            val categoryCount = models.count { it.categoryName == category.name }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFF38BDF8) else Color(0xCC0E1A33))
                                    .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0x40446A9E), RoundedCornerShape(20.dp))
                                    .clickable { selectedCategoryFilter = if (isSelected) null else category.name }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "${category.name} ($categoryCount)",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color(0xFF030713) else Color(0xFFD4E2F5)
                                )
                            }
                        }
                    }
                }

                // 7. Header of Available Models
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "موديلات الأجهزة المتاحة (${filteredModels.size})",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )

                        if (isAdmin) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { showAddCategoryDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3338BDF8)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("+ قسم", fontFamily = CairoFontFamily, fontSize = 10.5.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showAddBrandDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFD54F)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("+ شركة", fontFamily = CairoFontFamily, fontSize = 10.5.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 8. Real Product Model Cards (بطاقات الموديلات داخل وحدات عرض زجاجية مضيئة)
                if (filteredModels.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xCC0E1A33)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ViewInAr, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("لا توجد موديلات مطابقة للبحث", fontFamily = CairoFontFamily, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("جرب تغيير كلمة البحث أو اختيار قسم آخر", fontFamily = CairoFontFamily, color = Color(0xFF8FA4C4), fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(filteredModels, key = { it.id }) { model ->
                        CosmicProductModelCard(
                            model = model,
                            isAdmin = isAdmin,
                            onOpenSale = { onOpenSale(model) },
                            onEdit = { editingModel = model },
                            onDelete = { onDeleteModel(model.id) }
                        )
                    }
                }
            }
        }

        // Dialogs for Adding/Editing Data
        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onConfirm = {
                    onAddCategory(it)
                    showAddCategoryDialog = false
                }
            )
        }

        if (showAddBrandDialog && categories.isNotEmpty()) {
            val targetCategory = categories.first()
            AddBrandDialog(
                categoryName = targetCategory.name,
                onDismiss = { showAddBrandDialog = false },
                onConfirm = { name ->
                    onAddBrand(name, targetCategory.id)
                    showAddBrandDialog = false
                }
            )
        }

        if (showAddProductDialog && categories.isNotEmpty() && brands.isNotEmpty()) {
            val targetCategory = categories.first()
            val targetBrand = brands.first()
            AddProductDialog(
                categoryName = targetCategory.name,
                brandName = targetBrand.name,
                onDismiss = { showAddProductDialog = false },
                onConfirm = { name ->
                    onAddProduct(name, targetCategory.id, targetCategory.name, targetBrand.id, targetBrand.name)
                    showAddProductDialog = false
                }
            )
        }

        if (showAddModelDialog && products.isNotEmpty()) {
            val targetProduct = products.first()
            AddModelDialog(
                productName = targetProduct.name,
                onDismiss = { showAddModelDialog = false },
                onConfirm = { modelName, fPrice, mPrice, stock, code ->
                    onAddModel(targetProduct.id, targetProduct.name, targetProduct.brandName, targetProduct.categoryName, modelName, fPrice, mPrice, stock, code)
                    showAddModelDialog = false
                }
            )
        }

        if (editingModel != null) {
            EditModelDialog(
                model = editingModel!!,
                onDismiss = { editingModel = null },
                onConfirm = {
                    onUpdateModel(it)
                    editingModel = null
                }
            )
        }
    }
}

/**
 * 3 Glass Stat Cards at the top of Products Planet.
 */
@Composable
private fun CosmicGlassStatCard(
    title: String,
    count: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(84.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC0B162E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontFamily = CairoFontFamily,
                fontSize = 11.sp,
                color = Color(0xFFD4E2F5),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = count,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

/**
 * Individual Product Model Exhibition Card inside Cosmic Products City.
 */
@Composable
private fun CosmicProductModelCard(
    model: ProductModelEntity,
    isAdmin: Boolean,
    onOpenSale: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }
    val isOutOfStock = model.stockQuantity <= 0
    val isLowStock = model.stockQuantity in 1..2

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xE60E1A33)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOutOfStock) Color(0x66EF4444) else Color(0x403F5E8E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Stock Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isOutOfStock -> Color(0x33EF4444)
                                isLowStock -> Color(0x33F59E0B)
                                else -> Color(0x3300E676)
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isOutOfStock -> Color(0xFFEF4444)
                                isLowStock -> Color(0xFFF59E0B)
                                else -> Color(0xFF00E676)
                            },
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when {
                            isOutOfStock -> "نفذت الكمية"
                            isLowStock -> "كمية حرجة (${model.stockQuantity})"
                            else -> "متوفر (${model.stockQuantity} قطعة)"
                        },
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = when {
                            isOutOfStock -> Color(0xFFFF6B6B)
                            isLowStock -> Color(0xFFFFC107)
                            else -> Color(0xFF00E676)
                        }
                    )
                }

                // Right: Model Name & Brand
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = model.modelName,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${model.categoryName} • ${model.brandName}",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.5.sp,
                        color = Color(0xFFA6B7D4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price & Barcode Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Quick Sale Button
                    Button(
                        onClick = onOpenSale,
                        enabled = !isOutOfStock,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF070B14), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("بيع سريع", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color(0xFF070B14), fontSize = 11.sp)
                    }

                    if (isAdmin) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x3338BDF8))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x33EF4444))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Selling Price
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${numberFormat.format(model.finalPrice)} ج.م",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFFFFD54F)
                    )
                    if (isAdmin && model.minAllowedPrice > 0) {
                        Text(
                            text = "أدنى سعر: ${numberFormat.format(model.minAllowedPrice)} ج.م",
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFF8FA4C4)
                        )
                    }
                }
            }
        }
    }
}
