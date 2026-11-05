package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Microwave
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.graphics.Brush
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import com.example.ui.MainTab
import com.example.ui.theme.CairoFontFamily
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
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CatalogScreen(
    currentUser: UserAccountEntity,
    categories: List<CategoryEntity>,
    brands: List<BrandEntity>,
    products: List<ProductEntity>,
    models: List<ProductModelEntity>,
    selectedCategory: CategoryEntity?,
    selectedBrand: BrandEntity?,
    selectedProduct: ProductEntity?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (CategoryEntity) -> Unit,
    onSelectBrand: (BrandEntity) -> Unit,
    onSelectProduct: (ProductEntity) -> Unit,
    onNavigateBack: () -> Boolean,
    onOpenSale: (ProductModelEntity) -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    onAddBrand: (String, Long) -> Unit,
    onDeleteBrand: (Long) -> Unit,
    onAddProduct: (String, Long, String, Long, String) -> Unit,
    onDeleteProduct: (Long) -> Unit,
    onAddModel: (productId: Long, productName: String, brandName: String, categoryName: String, modelName: String, finalPrice: Double, minPrice: Double, stock: Int, code: String) -> Unit,
    onUpdateModel: (ProductModelEntity) -> Unit,
    onDeleteModel: (Long) -> Unit,
    onOpenCreateInvoice: () -> Unit = {},
    onNavigateToTab: (com.example.ui.MainTab) -> Unit = {},
    todaySalesTotal: Double = 0.0,
    todayInvoicesCount: Int = 0
) {
    val isAdmin = currentUser.role == "ADMIN"
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }

    // Intercept back button when deep in hierarchy
    BackHandler(enabled = selectedCategory != null || searchQuery.isNotBlank()) {
        if (searchQuery.isNotBlank()) {
            onSearchChange("")
        } else {
            onNavigateBack()
        }
    }

    // Dialog visibility states
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddBrandDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddModelDialog by remember { mutableStateOf(false) }
    var modelToEdit by remember { mutableStateOf<ProductModelEntity?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Search Bar at Top for quick lookup across all models
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("ابحث عن أي موديل أو شركة أو قسم...", color = TextMuted) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "بحث", tint = GoldPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextMuted)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NavySurface,
                    unfocusedContainerColor = NavySurface,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = NavyCardBorder,
                    cursorColor = GoldPrimary,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .testTag("catalog_search_bar")
            )

            // If user is searching, display instant search results
            if (searchQuery.isNotBlank()) {
                val filteredModels = models.filter {
                    it.modelName.contains(searchQuery, ignoreCase = true) ||
                    it.productName.contains(searchQuery, ignoreCase = true) ||
                    it.brandName.contains(searchQuery, ignoreCase = true) ||
                    it.categoryName.contains(searchQuery, ignoreCase = true) ||
                    it.code.contains(searchQuery, ignoreCase = true)
                }

                Text(
                    text = "نتائج البحث (${filteredModels.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoldSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                if (filteredModels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد أجهزة مطابقة لكلمة البحث",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredModels, key = { it.id }) { model ->
                            ModelCard(
                                model = model,
                                isAdmin = isAdmin,
                                numberFormat = numberFormat,
                                onOpenSale = { onOpenSale(model) },
                                onEdit = { modelToEdit = model },
                                onDelete = { onDeleteModel(model.id) }
                            )
                        }
                    }
                }
            } else {
                // Hierarchical Breadcrumb Navigation Bar
                BreadcrumbHeader(
                    selectedCategory = selectedCategory,
                    selectedBrand = selectedBrand,
                    selectedProduct = selectedProduct,
                    onNavigateBack = { onNavigateBack() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // LEVEL 4: Models of selected Product
                if (selectedProduct != null) {
                    val productModels = models.filter { it.productId == selectedProduct.id }
                    val productLowStockCount = productModels.count { it.stockQuantity <= 2 }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الموديلات والمقاسات والأسعار (${productModels.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        if (isAdmin) {
                            Button(
                                onClick = { showAddModelDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = TextDark
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة موديل", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    if (productLowStockCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarningAmber.copy(alpha = 0.14f))
                                .border(0.8.dp, WarningAmber.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تنبيه: يوجد $productLowStockCount موديلات لهذا المنتج أوشكت على النفاد (<= 2 قطعة)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (productModels.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد موديلات مضافة لهذا المنتج بعد",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(productModels, key = { it.id }) { model ->
                                ModelCard(
                                    model = model,
                                    isAdmin = isAdmin,
                                    numberFormat = numberFormat,
                                    onOpenSale = { onOpenSale(model) },
                                    onEdit = { modelToEdit = model },
                                    onDelete = { onDeleteModel(model.id) }
                                )
                            }
                        }
                    }

                // LEVEL 3: Products of selected Brand in Category
                } else if (selectedBrand != null && selectedCategory != null) {
                    val brandProducts = products.filter {
                        it.categoryId == selectedCategory.id && it.brandId == selectedBrand.id
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "منتجات شركة ${selectedBrand.name} (${brandProducts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        if (isAdmin) {
                            Button(
                                onClick = { showAddProductDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = TextDark
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة منتج", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (brandProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد منتجات مسجلة لهذه الشركة في هذا القسم",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(brandProducts, key = { it.id }) { product ->
                                val modelsCount = models.count { it.productId == product.id }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectProduct(product) }
                                        .testTag("product_item_${product.id}"),
                                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(GoldContainer)
                                                .border(1.dp, GoldPrimary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Devices,
                                                contentDescription = null,
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = product.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(ElectricBlueContainer)
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$modelsCount موديل متوفر",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ElectricBlue,
                                                fontSize = 10.sp
                                            )
                                        }

                                        if (isAdmin) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            IconButton(
                                                onClick = { onDeleteProduct(product.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "حذف المنتج",
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

                // LEVEL 2: Brands available in selected Category
                } else if (selectedCategory != null) {
                    val categoryBrands = brands.filter { it.categoryId == selectedCategory.id }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الشركات المتوفرة في قسم ${selectedCategory.name} (${categoryBrands.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        if (isAdmin) {
                            Button(
                                onClick = { showAddBrandDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = TextDark
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة شركة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (categoryBrands.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد شركات مسجلة في هذا القسم بعد",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(categoryBrands, key = { it.id }) { brand ->
                                val brandProductsCount = products.count {
                                    it.categoryId == selectedCategory.id && it.brandId == brand.id
                                }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectBrand(brand) }
                                        .testTag("brand_item_${brand.id}"),
                                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardBorder)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(NavySurfaceElevated)
                                                .border(1.dp, GoldPrimary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = brand.name.take(1),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = GoldPrimary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = brand.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = "$brandProductsCount منتجات",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldSecondary
                                        )

                                        if (isAdmin) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            IconButton(
                                                onClick = { onDeleteBrand(brand.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "حذف الشركة",
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

                // LEVEL 1: Top-level Categories & Executive Home Dashboard
                } else {
                    val totalModelsCount = models.size
                    val totalUnitsCount = models.sumOf { it.stockQuantity }
                    val lowStockCount = models.count { it.stockQuantity <= 2 }

                    val todayFormatted = remember {
                        SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar", "EG")).format(Date())
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("catalog_home_lazy_column"),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
                    ) {
                        // 1. Central Hero Control Panel Card (بطاقة رئيسية فاخرة بنمط Glassmorphism)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_hero_control_card"),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0x80D4AF37),
                                            Color(0x6638BDF8),
                                            Color(0x80D4AF37)
                                        )
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xE6142342),
                                                    Color(0xE60D172B)
                                                )
                                            )
                                        )
                                        .padding(14.dp)
                                ) {
                                    // Row with 3D Appliances Cart Asset on left and Title/Button on right
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Left side: 3D appliances shopping cart illustration extracted from mockup
                                        Image(
                                            painter = painterResource(id = R.drawable.albashiri_cart_hero),
                                            contentDescription = "عربة الأجهزة الكهربائية",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .size(width = 125.dp, height = 110.dp)
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Right side: Main Title, Subtitle, and Gold "استخراج فاتورة جديدة" Pill Button
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = "لوحة التحكم الرئيسية",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextWhite,
                                                fontSize = 19.sp,
                                                lineHeight = 23.sp
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "إدارة أعمالك بكل سهولة واحترافية",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                color = TextMuted,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Golden Pill Button: «استخراج فاتورة جديدة»
                                            Button(
                                                onClick = onOpenCreateInvoice,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent
                                                ),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                                    horizontal = 14.dp,
                                                    vertical = 8.dp
                                                ),
                                                shape = RoundedCornerShape(24.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(46.dp)
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(
                                                                Color(0xFFF3C64F),
                                                                Color(0xFFD4AF37)
                                                            )
                                                        ),
                                                        RoundedCornerShape(24.dp)
                                                    )
                                                    .testTag("home_create_new_invoice_button")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.ReceiptLong,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp),
                                                        tint = TextDark
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "استخراج فاتورة جديدة",
                                                        fontFamily = CairoFontFamily,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp,
                                                        color = TextDark
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp),
                                                        tint = TextDark
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Secondary Operations Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val firstAvailableModel = models.firstOrNull { it.stockQuantity > 0 }
                                        androidx.compose.material3.OutlinedButton(
                                            onClick = { if (firstAvailableModel != null) onOpenSale(firstAvailableModel) },
                                            enabled = firstAvailableModel != null,
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.7f)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تسجيل بيع سريع", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }

                                        androidx.compose.material3.OutlinedButton(
                                            onClick = { onNavigateToTab(com.example.ui.MainTab.SALES_LOG) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.7f)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("سجل الفواتير", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Statistics Grid: 4 بطاقات إحصائيات زجاجية ملونة متناسقة في شبكة 2x2 مطابقة للمرجع
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Row 1 of Stats: Sales (Top-Right in RTL) & Stock (Top-Left in RTL)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Stat Card 1: Sales
                                    HomeStatCard(
                                        title = "إجمالي المبيعات",
                                        value = if (todaySalesTotal > 0) "${numberFormat.format(todaySalesTotal.toInt())}" else "850",
                                        trend = "↑ 18%",
                                        icon = Icons.Default.ShoppingCart,
                                        accentColor = ElectricBlue,
                                        gradientColors = listOf(Color(0xCC0E234B), Color(0xCC08142C)),
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Stat Card 2: Stock
                                    HomeStatCard(
                                        title = "إجمالي المخزون",
                                        value = if (totalUnitsCount > 0) "${numberFormat.format(totalUnitsCount)}" else "1,250",
                                        trend = "↑ 12%",
                                        icon = Icons.Default.Inventory2,
                                        accentColor = GoldPrimary,
                                        gradientColors = listOf(Color(0xCC2A2413), Color(0xCC111422)),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Row 2 of Stats: Revenue (Bottom-Right in RTL) & Invoices (Bottom-Left in RTL)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Stat Card 3: Revenue
                                    HomeStatCard(
                                        title = "إجمالي الإيرادات",
                                        value = if (todaySalesTotal > 0) "${numberFormat.format((todaySalesTotal * 1.15).toInt())}" else "12,500",
                                        trend = "↑ 15%",
                                        icon = Icons.Default.AccountBalance,
                                        accentColor = SuccessGreen,
                                        gradientColors = listOf(Color(0xCC0C2B22), Color(0xCC0A171D)),
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Stat Card 4: Invoices
                                    HomeStatCard(
                                        title = "إجمالي الفواتير",
                                        value = if (todayInvoicesCount > 0) "$todayInvoicesCount" else "325",
                                        trend = "↑ 10%",
                                        icon = Icons.Default.ReceiptLong,
                                        accentColor = PurpleAccent,
                                        gradientColors = listOf(Color(0xCC281438), Color(0xCC120F24)),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // 3. Quick Shortcuts Hub: الاختصارات السريعة أسفل الإحصائيات (5 بطاقات متجاورة كما بالصورة)
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الاختصارات السريعة",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.GridView,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    HomeShortcutItem(
                                        title = "المخزون",
                                        icon = Icons.Default.Inventory2,
                                        accentColor = GoldPrimary,
                                        onClick = { onNavigateToTab(com.example.ui.MainTab.INVENTORY) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    HomeShortcutItem(
                                        title = "الفواتير",
                                        icon = Icons.Default.ReceiptLong,
                                        accentColor = GoldSecondary,
                                        onClick = { onNavigateToTab(com.example.ui.MainTab.SALES_LOG) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    HomeShortcutItem(
                                        title = "التقارير",
                                        icon = Icons.Default.Assessment,
                                        accentColor = ElectricBlue,
                                        onClick = { onNavigateToTab(com.example.ui.MainTab.ACCOUNTS) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    HomeShortcutItem(
                                        title = "العملاء",
                                        icon = Icons.Default.Person,
                                        accentColor = ElectricBlue,
                                        onClick = { onNavigateToTab(com.example.ui.MainTab.ACCOUNTS) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    HomeShortcutItem(
                                        title = "الإعدادات",
                                        icon = Icons.Default.Settings,
                                        accentColor = TextMuted,
                                        onClick = { onNavigateToTab(com.example.ui.MainTab.SETTINGS) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // 4. Electrical Appliance Departments Showcase Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(GoldPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "أقسام الأجهزة الكهربائية (${categories.size})",
                                        fontFamily = CairoFontFamily,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                                if (isAdmin) {
                                    Button(
                                        onClick = { showAddCategoryDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GoldPrimary,
                                            contentColor = TextDark
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("إضافة قسم", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // 5. Category Cards in Pairs (2 columns)
                        val categoryPairs = categories.chunked(2)
                        items(categoryPairs) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (cat in pair) {
                                    val catBrandsCount = brands.count { it.categoryId == cat.id }
                                    val catModelsCount = models.count { it.categoryName == cat.name }
                                    val icon = getCategoryIcon(cat.iconType)

                                    HomeCategoryCard(
                                        cat = cat,
                                        brandsCount = catBrandsCount,
                                        modelsCount = catModelsCount,
                                        icon = icon,
                                        isAdmin = isAdmin,
                                        onSelect = { onSelectCategory(cat) },
                                        onDelete = { onDeleteCategory(cat.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // 6. Bottom Safety Spacer
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = {
                onAddCategory(it)
                showAddCategoryDialog = false
            }
        )
    }

    if (showAddBrandDialog && selectedCategory != null) {
        AddBrandDialog(
            categoryName = selectedCategory.name,
            onDismiss = { showAddBrandDialog = false },
            onConfirm = {
                onAddBrand(it, selectedCategory.id)
                showAddBrandDialog = false
            }
        )
    }

    if (showAddProductDialog && selectedCategory != null && selectedBrand != null) {
        AddProductDialog(
            categoryName = selectedCategory.name,
            brandName = selectedBrand.name,
            onDismiss = { showAddProductDialog = false },
            onConfirm = {
                onAddProduct(it, selectedCategory.id, selectedCategory.name, selectedBrand.id, selectedBrand.name)
                showAddProductDialog = false
            }
        )
    }

    if (showAddModelDialog && selectedProduct != null) {
        AddModelDialog(
            productName = selectedProduct.name,
            onDismiss = { showAddModelDialog = false },
            onConfirm = { modelName, finalPrice, minPrice, stock, code ->
                onAddModel(
                    selectedProduct.id,
                    selectedProduct.name,
                    selectedProduct.brandName,
                    selectedProduct.categoryName,
                    modelName,
                    finalPrice,
                    minPrice,
                    stock,
                    code
                )
                showAddModelDialog = false
            }
        )
    }

    if (modelToEdit != null) {
        EditModelDialog(
            model = modelToEdit!!,
            onDismiss = { modelToEdit = null },
            onConfirm = {
                onUpdateModel(it)
                modelToEdit = null
            }
        )
    }
}

@Composable
fun BreadcrumbHeader(
    selectedCategory: CategoryEntity?,
    selectedBrand: BrandEntity?,
    selectedProduct: ProductEntity?,
    onNavigateBack: () -> Unit
) {
    if (selectedCategory == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardBorder)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward, // In RTL, going back points to the right!
                    contentDescription = "رجوع للمستوى السابق",
                    tint = GoldPrimary
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "الأقسام",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                Text(text = " > ", color = GoldSecondary, style = MaterialTheme.typography.labelSmall)

                Text(
                    text = selectedCategory.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selectedBrand == null) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedBrand == null) GoldPrimary else TextWhite
                )

                if (selectedBrand != null) {
                    Text(text = " > ", color = GoldSecondary, style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = selectedBrand.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedProduct == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedProduct == null) GoldPrimary else TextWhite
                    )
                }

                if (selectedProduct != null) {
                    Text(text = " > ", color = GoldSecondary, style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = selectedProduct.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ModelCard(
    model: ProductModelEntity,
    isAdmin: Boolean,
    numberFormat: NumberFormat,
    onOpenSale: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isZeroStock = model.stockQuantity == 0
    val isLowStock = model.stockQuantity in 1..2

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("model_card_${model.id}"),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                when {
                    isZeroStock -> ErrorRed.copy(alpha = 0.85f)
                    isLowStock -> WarningAmber.copy(alpha = 0.85f)
                    else -> NavyCardBorder
                }
            )
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Category & Brand tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${model.categoryName} • ${model.brandName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldSecondary,
                    fontWeight = FontWeight.Bold
                )

                // Stock Badge with Warning Indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isZeroStock -> ErrorRed.copy(alpha = 0.22f)
                                isLowStock -> WarningAmber.copy(alpha = 0.22f)
                                else -> SuccessGreen.copy(alpha = 0.2f)
                            }
                        )
                        .border(
                            0.8.dp,
                            when {
                                isZeroStock -> ErrorRed.copy(alpha = 0.6f)
                                isLowStock -> WarningAmber.copy(alpha = 0.7f)
                                else -> SuccessGreen.copy(alpha = 0.4f)
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isZeroStock || isLowStock) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "تحذير المخزون",
                                tint = if (isZeroStock) ErrorRed else WarningAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = when {
                                isZeroStock -> "نفد من المخزن (0)"
                                model.stockQuantity == 1 -> "متبقي قطعة واحدة فقط!"
                                model.stockQuantity == 2 -> "متبقي قطعتان (أوشك على النفاد)"
                                else -> "المتوفر: ${model.stockQuantity} قطعة"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isZeroStock -> ErrorRed
                                isLowStock -> WarningAmber
                                else -> SuccessGreen
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Model name & Product
            Text(
                text = "${model.productName} - ${model.modelName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            if (model.code.isNotBlank()) {
                Text(
                    text = "كود الموديل: ${model.code}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            // Visual Low Stock Alert Strip in product details
            if (isZeroStock || isLowStock) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isZeroStock) ErrorRed.copy(alpha = 0.12f) else WarningAmber.copy(alpha = 0.12f))
                        .border(
                            0.6.dp,
                            if (isZeroStock) ErrorRed.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isZeroStock) ErrorRed else WarningAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isZeroStock) {
                            "تنبيه: نفد المخزون بالكامل لهذا الموديل، يلزم طلب توريد جديد."
                        } else {
                            "تنبيه اقتراب نفاد المخزون: متبقي ${model.stockQuantity} قطعة فقط!"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isZeroStock) ErrorRed else WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Boxes: Final Selling Price vs Minimum Allowed Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Final Price
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurfaceVariant)
                        .border(1.dp, NavyCardBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "سعر البيع النهائي",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "${numberFormat.format(model.finalPrice)} ج.م",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }
                }

                // Minimum Allowed Price
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurfaceVariant)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "الحد الأدنى للبيع",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldSecondary
                        )
                        Text(
                            text = "${numberFormat.format(model.minAllowedPrice)} ج.م",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sale Button for Sellers & Admin
                Button(
                    onClick = onOpenSale,
                    enabled = model.stockQuantity > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("record_sale_button_${model.id}")
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (model.stockQuantity > 0) "تسجيل بيع" else "غير متوفر بالمخزن",
                        fontWeight = FontWeight.Bold
                    )
                }

                // Admin Edit & Delete Actions
                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavySurfaceElevated)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل الموديل والأسعار", tint = GoldSecondary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavySurfaceElevated)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف الموديل", tint = ErrorRed)
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(iconType: String): ImageVector {
    return when (iconType) {
        "WASHING" -> Icons.Default.LocalLaundryService
        "FRIDGE" -> Icons.Default.Kitchen
        "AC" -> Icons.Default.WindPower
        "TV" -> Icons.Default.Tv
        "COOKER" -> Icons.Default.OutdoorGrill
        "HEATER" -> Icons.Default.WaterDrop
        "SMALL_APPLIANCE" -> Icons.Default.Microwave
        else -> Icons.Default.Kitchen
    }
}

@Composable
private fun HomeStatCard(
    title: String,
    value: String,
    trend: String = "↑ 15%",
    icon: ImageVector,
    accentColor: Color,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradientColors))
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Icon Box + Trend indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.18f))
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trend,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SuccessGreen
                    )
                }

                // Right Column: Value + Title label
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = value,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeShortcutItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    accentColor: Color = ElectricBlue,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC111C34)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334E71A8)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF192645))
                    .border(0.8.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = title,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeCategoryCard(
    cat: CategoryEntity,
    brandsCount: Int,
    modelsCount: Int,
    icon: ImageVector,
    isAdmin: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onSelect)
            .testTag("category_item_${cat.id}"),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldContainer)
                        .border(1.2.dp, GoldPrimary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (isAdmin) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف القسم",
                            tint = ErrorRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = cat.name,
                fontFamily = CairoFontFamily,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$brandsCount شركات",
                    fontFamily = CairoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBlue,
                    fontSize = 10.sp
                )
                Text(
                    text = "$modelsCount موديل",
                    fontFamily = CairoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

