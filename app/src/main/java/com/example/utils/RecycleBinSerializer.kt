package com.example.utils

import com.example.data.local.BrandEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.CustomerAccountEntity
import com.example.data.local.CustomerTransactionEntity
import com.example.data.local.MerchantAccountEntity
import com.example.data.local.MerchantTransactionEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ProductModelEntity
import com.example.data.local.SupplierAccountEntity
import com.example.data.local.SupplierTransactionEntity
import org.json.JSONArray
import org.json.JSONObject

data class CategoryRestorationBundle(
    val category: CategoryEntity,
    val brands: List<BrandEntity>,
    val products: List<ProductEntity>,
    val models: List<ProductModelEntity>
)

data class BrandRestorationBundle(
    val brand: BrandEntity,
    val products: List<ProductEntity>,
    val models: List<ProductModelEntity>
)

data class ProductRestorationBundle(
    val product: ProductEntity,
    val models: List<ProductModelEntity>
)

data class SupplierRestorationBundle(
    val supplier: SupplierAccountEntity,
    val transactions: List<SupplierTransactionEntity>
)

data class MerchantRestorationBundle(
    val merchant: MerchantAccountEntity,
    val transactions: List<MerchantTransactionEntity>
)

data class CustomerRestorationBundle(
    val customer: CustomerAccountEntity,
    val transactions: List<CustomerTransactionEntity>
)

object RecycleBinSerializer {

    // --- ProductModelEntity ---
    fun serializeModel(model: ProductModelEntity): String {
        val json = JSONObject()
        json.put("id", model.id)
        json.put("productId", model.productId)
        json.put("productName", model.productName)
        json.put("brandName", model.brandName)
        json.put("categoryName", model.categoryName)
        json.put("modelName", model.modelName)
        json.put("finalPrice", model.finalPrice)
        json.put("minAllowedPrice", model.minAllowedPrice)
        json.put("stockQuantity", model.stockQuantity)
        json.put("code", model.code)
        return json.toString()
    }

    fun deserializeModel(jsonStr: String): ProductModelEntity {
        val json = JSONObject(jsonStr)
        return ProductModelEntity(
            id = json.optLong("id", 0L),
            productId = json.optLong("productId", 0L),
            productName = json.optString("productName", ""),
            brandName = json.optString("brandName", ""),
            categoryName = json.optString("categoryName", ""),
            modelName = json.optString("modelName", ""),
            finalPrice = json.optDouble("finalPrice", 0.0),
            minAllowedPrice = json.optDouble("minAllowedPrice", 0.0),
            stockQuantity = json.optInt("stockQuantity", 0),
            code = json.optString("code", "")
        )
    }

    // --- ProductEntity & its models ---
    fun serializeProduct(product: ProductEntity, models: List<ProductModelEntity>): String {
        val root = JSONObject()
        val prodJson = JSONObject().apply {
            put("id", product.id)
            put("name", product.name)
            put("categoryId", product.categoryId)
            put("categoryName", product.categoryName)
            put("brandId", product.brandId)
            put("brandName", product.brandName)
            put("description", product.description)
        }
        root.put("product", prodJson)

        val modelsArray = JSONArray()
        models.forEach { m ->
            modelsArray.put(JSONObject(serializeModel(m)))
        }
        root.put("models", modelsArray)
        return root.toString()
    }

    fun deserializeProduct(jsonStr: String): ProductRestorationBundle {
        val root = JSONObject(jsonStr)
        val p = root.getJSONObject("product")
        val product = ProductEntity(
            id = p.optLong("id", 0L),
            name = p.optString("name", ""),
            categoryId = p.optLong("categoryId", 0L),
            categoryName = p.optString("categoryName", ""),
            brandId = p.optLong("brandId", 0L),
            brandName = p.optString("brandName", ""),
            description = p.optString("description", "")
        )
        val modelsList = mutableListOf<ProductModelEntity>()
        val arr = root.optJSONArray("models") ?: JSONArray()
        for (i in 0 until arr.length()) {
            modelsList.add(deserializeModel(arr.getJSONObject(i).toString()))
        }
        return ProductRestorationBundle(product, modelsList)
    }

    // --- BrandEntity & children ---
    fun serializeBrand(brand: BrandEntity, products: List<ProductEntity>, models: List<ProductModelEntity>): String {
        val root = JSONObject()
        val bJson = JSONObject().apply {
            put("id", brand.id)
            put("name", brand.name)
            put("categoryId", brand.categoryId)
        }
        root.put("brand", bJson)

        val prodsArr = JSONArray()
        products.forEach { p ->
            prodsArr.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("categoryId", p.categoryId)
                put("categoryName", p.categoryName)
                put("brandId", p.brandId)
                put("brandName", p.brandName)
                put("description", p.description)
            })
        }
        root.put("products", prodsArr)

        val modelsArr = JSONArray()
        models.forEach { m ->
            modelsArr.put(JSONObject(serializeModel(m)))
        }
        root.put("models", modelsArr)
        return root.toString()
    }

    fun deserializeBrand(jsonStr: String): BrandRestorationBundle {
        val root = JSONObject(jsonStr)
        val b = root.getJSONObject("brand")
        val brand = BrandEntity(
            id = b.optLong("id", 0L),
            name = b.optString("name", ""),
            categoryId = b.optLong("categoryId", 0L)
        )
        val prods = mutableListOf<ProductEntity>()
        val pArr = root.optJSONArray("products") ?: JSONArray()
        for (i in 0 until pArr.length()) {
            val pj = pArr.getJSONObject(i)
            prods.add(
                ProductEntity(
                    id = pj.optLong("id", 0L),
                    name = pj.optString("name", ""),
                    categoryId = pj.optLong("categoryId", 0L),
                    categoryName = pj.optString("categoryName", ""),
                    brandId = pj.optLong("brandId", 0L),
                    brandName = pj.optString("brandName", ""),
                    description = pj.optString("description", "")
                )
            )
        }
        val models = mutableListOf<ProductModelEntity>()
        val mArr = root.optJSONArray("models") ?: JSONArray()
        for (i in 0 until mArr.length()) {
            models.add(deserializeModel(mArr.getJSONObject(i).toString()))
        }
        return BrandRestorationBundle(brand, prods, models)
    }

    // --- CategoryEntity & children ---
    fun serializeCategory(
        category: CategoryEntity,
        brands: List<BrandEntity>,
        products: List<ProductEntity>,
        models: List<ProductModelEntity>
    ): String {
        val root = JSONObject()
        val cJson = JSONObject().apply {
            put("id", category.id)
            put("name", category.name)
            put("iconType", category.iconType)
        }
        root.put("category", cJson)

        val bArr = JSONArray()
        brands.forEach { b ->
            bArr.put(JSONObject().apply {
                put("id", b.id)
                put("name", b.name)
                put("categoryId", b.categoryId)
            })
        }
        root.put("brands", bArr)

        val pArr = JSONArray()
        products.forEach { p ->
            pArr.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("categoryId", p.categoryId)
                put("categoryName", p.categoryName)
                put("brandId", p.brandId)
                put("brandName", p.brandName)
                put("description", p.description)
            })
        }
        root.put("products", pArr)

        val mArr = JSONArray()
        models.forEach { m ->
            mArr.put(JSONObject(serializeModel(m)))
        }
        root.put("models", mArr)

        return root.toString()
    }

    fun deserializeCategory(jsonStr: String): CategoryRestorationBundle {
        val root = JSONObject(jsonStr)
        val c = root.getJSONObject("category")
        val category = CategoryEntity(
            id = c.optLong("id", 0L),
            name = c.optString("name", ""),
            iconType = c.optString("iconType", "APPLIANCE")
        )

        val brands = mutableListOf<BrandEntity>()
        val bArr = root.optJSONArray("brands") ?: JSONArray()
        for (i in 0 until bArr.length()) {
            val bj = bArr.getJSONObject(i)
            brands.add(BrandEntity(id = bj.optLong("id", 0L), name = bj.optString("name", ""), categoryId = bj.optLong("categoryId", 0L)))
        }

        val prods = mutableListOf<ProductEntity>()
        val pArr = root.optJSONArray("products") ?: JSONArray()
        for (i in 0 until pArr.length()) {
            val pj = pArr.getJSONObject(i)
            prods.add(
                ProductEntity(
                    id = pj.optLong("id", 0L),
                    name = pj.optString("name", ""),
                    categoryId = pj.optLong("categoryId", 0L),
                    categoryName = pj.optString("categoryName", ""),
                    brandId = pj.optLong("brandId", 0L),
                    brandName = pj.optString("brandName", ""),
                    description = pj.optString("description", "")
                )
            )
        }

        val models = mutableListOf<ProductModelEntity>()
        val mArr = root.optJSONArray("models") ?: JSONArray()
        for (i in 0 until mArr.length()) {
            models.add(deserializeModel(mArr.getJSONObject(i).toString()))
        }

        return CategoryRestorationBundle(category, brands, prods, models)
    }

    // --- SupplierAccountEntity & Transactions ---
    fun serializeSupplier(supplier: SupplierAccountEntity, transactions: List<SupplierTransactionEntity>): String {
        val root = JSONObject()
        val sJson = JSONObject().apply {
            put("id", supplier.id)
            put("supplierName", supplier.supplierName)
            put("phone", supplier.phone)
            put("totalPurchases", supplier.totalPurchases)
            put("totalPaid", supplier.totalPaid)
            put("remainingBalance", supplier.remainingBalance)
            put("notes", supplier.notes)
        }
        root.put("supplier", sJson)

        val txArr = JSONArray()
        transactions.forEach { tx ->
            txArr.put(JSONObject().apply {
                put("id", tx.id)
                put("supplierId", tx.supplierId)
                put("type", tx.type)
                put("amount", tx.amount)
                put("notes", tx.notes)
                put("timestamp", tx.timestamp)
            })
        }
        root.put("transactions", txArr)
        return root.toString()
    }

    fun deserializeSupplier(jsonStr: String): SupplierRestorationBundle {
        val root = JSONObject(jsonStr)
        val s = root.getJSONObject("supplier")
        val supplier = SupplierAccountEntity(
            id = s.optLong("id", 0L),
            supplierName = s.optString("supplierName", ""),
            phone = s.optString("phone", ""),
            totalPurchases = s.optDouble("totalPurchases", 0.0),
            totalPaid = s.optDouble("totalPaid", 0.0),
            remainingBalance = s.optDouble("remainingBalance", 0.0),
            notes = s.optString("notes", "")
        )
        val txs = mutableListOf<SupplierTransactionEntity>()
        val arr = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val tj = arr.getJSONObject(i)
            txs.add(
                SupplierTransactionEntity(
                    id = tj.optLong("id", 0L),
                    supplierId = tj.optLong("supplierId", 0L),
                    type = tj.optString("type", "INVOICE"),
                    amount = tj.optDouble("amount", 0.0),
                    notes = tj.optString("notes", ""),
                    timestamp = tj.optLong("timestamp", 0L)
                )
            )
        }
        return SupplierRestorationBundle(supplier, txs)
    }

    // --- MerchantAccountEntity & Transactions ---
    fun serializeMerchant(merchant: MerchantAccountEntity, transactions: List<MerchantTransactionEntity>): String {
        val root = JSONObject()
        val mJson = JSONObject().apply {
            put("id", merchant.id)
            put("merchantName", merchant.merchantName)
            put("phone", merchant.phone)
            put("totalGoodsDelivered", merchant.totalGoodsDelivered)
            put("totalCollected", merchant.totalCollected)
            put("remainingDueToMe", merchant.remainingDueToMe)
            put("notes", merchant.notes)
        }
        root.put("merchant", mJson)

        val txArr = JSONArray()
        transactions.forEach { tx ->
            txArr.put(JSONObject().apply {
                put("id", tx.id)
                put("merchantId", tx.merchantId)
                put("type", tx.type)
                put("amount", tx.amount)
                put("notes", tx.notes)
                put("timestamp", tx.timestamp)
            })
        }
        root.put("transactions", txArr)
        return root.toString()
    }

    fun deserializeMerchant(jsonStr: String): MerchantRestorationBundle {
        val root = JSONObject(jsonStr)
        val m = root.getJSONObject("merchant")
        val merchant = MerchantAccountEntity(
            id = m.optLong("id", 0L),
            merchantName = m.optString("merchantName", ""),
            phone = m.optString("phone", ""),
            totalGoodsDelivered = m.optDouble("totalGoodsDelivered", 0.0),
            totalCollected = m.optDouble("totalCollected", 0.0),
            remainingDueToMe = m.optDouble("remainingDueToMe", 0.0),
            notes = m.optString("notes", "")
        )
        val txs = mutableListOf<MerchantTransactionEntity>()
        val arr = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val tj = arr.getJSONObject(i)
            txs.add(
                MerchantTransactionEntity(
                    id = tj.optLong("id", 0L),
                    merchantId = tj.optLong("merchantId", 0L),
                    type = tj.optString("type", "GOODS_DELIVERED"),
                    amount = tj.optDouble("amount", 0.0),
                    notes = tj.optString("notes", ""),
                    timestamp = tj.optLong("timestamp", 0L)
                )
            )
        }
        return MerchantRestorationBundle(merchant, txs)
    }

    // --- CustomerAccountEntity & Transactions ---
    fun serializeCustomer(customer: CustomerAccountEntity, transactions: List<CustomerTransactionEntity>): String {
        val root = JSONObject()
        val cJson = JSONObject().apply {
            put("id", customer.id)
            put("customerName", customer.customerName)
            put("phone", customer.phone)
            put("totalCreditSales", customer.totalCreditSales)
            put("totalCollected", customer.totalCollected)
            put("remainingDueToMe", customer.remainingDueToMe)
            put("notes", customer.notes)
        }
        root.put("customer", cJson)

        val txArr = JSONArray()
        transactions.forEach { tx ->
            txArr.put(JSONObject().apply {
                put("id", tx.id)
                put("customerId", tx.customerId)
                put("type", tx.type)
                put("amount", tx.amount)
                put("productDescription", tx.productDescription)
                put("notes", tx.notes)
                put("timestamp", tx.timestamp)
            })
        }
        root.put("transactions", txArr)
        return root.toString()
    }

    fun deserializeCustomer(jsonStr: String): CustomerRestorationBundle {
        val root = JSONObject(jsonStr)
        val c = root.getJSONObject("customer")
        val customer = CustomerAccountEntity(
            id = c.optLong("id", 0L),
            customerName = c.optString("customerName", ""),
            phone = c.optString("phone", ""),
            totalCreditSales = c.optDouble("totalCreditSales", 0.0),
            totalCollected = c.optDouble("totalCollected", 0.0),
            remainingDueToMe = c.optDouble("remainingDueToMe", 0.0),
            notes = c.optString("notes", "")
        )
        val txs = mutableListOf<CustomerTransactionEntity>()
        val arr = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val tj = arr.getJSONObject(i)
            txs.add(
                CustomerTransactionEntity(
                    id = tj.optLong("id", 0L),
                    customerId = tj.optLong("customerId", 0L),
                    type = tj.optString("type", "CREDIT_SALE"),
                    amount = tj.optDouble("amount", 0.0),
                    productDescription = tj.optString("productDescription", ""),
                    notes = tj.optString("notes", ""),
                    timestamp = tj.optLong("timestamp", 0L)
                )
            )
        }
        return CustomerRestorationBundle(customer, txs)
    }

    // --- Transactions single entities ---
    fun serializeSupplierTx(tx: SupplierTransactionEntity): String {
        val j = JSONObject()
        j.put("id", tx.id)
        j.put("supplierId", tx.supplierId)
        j.put("type", tx.type)
        j.put("amount", tx.amount)
        j.put("notes", tx.notes)
        j.put("timestamp", tx.timestamp)
        return j.toString()
    }

    fun deserializeSupplierTx(jsonStr: String): SupplierTransactionEntity {
        val j = JSONObject(jsonStr)
        return SupplierTransactionEntity(
            id = j.optLong("id", 0L),
            supplierId = j.optLong("supplierId", 0L),
            type = j.optString("type", "INVOICE"),
            amount = j.optDouble("amount", 0.0),
            notes = j.optString("notes", ""),
            timestamp = j.optLong("timestamp", 0L)
        )
    }

    fun serializeMerchantTx(tx: MerchantTransactionEntity): String {
        val j = JSONObject()
        j.put("id", tx.id)
        j.put("merchantId", tx.merchantId)
        j.put("type", tx.type)
        j.put("amount", tx.amount)
        j.put("notes", tx.notes)
        j.put("timestamp", tx.timestamp)
        return j.toString()
    }

    fun deserializeMerchantTx(jsonStr: String): MerchantTransactionEntity {
        val j = JSONObject(jsonStr)
        return MerchantTransactionEntity(
            id = j.optLong("id", 0L),
            merchantId = j.optLong("merchantId", 0L),
            type = j.optString("type", "GOODS_DELIVERED"),
            amount = j.optDouble("amount", 0.0),
            notes = j.optString("notes", ""),
            timestamp = j.optLong("timestamp", 0L)
        )
    }

    fun serializeCustomerTx(tx: CustomerTransactionEntity): String {
        val j = JSONObject()
        j.put("id", tx.id)
        j.put("customerId", tx.customerId)
        j.put("type", tx.type)
        j.put("amount", tx.amount)
        j.put("productDescription", tx.productDescription)
        j.put("notes", tx.notes)
        j.put("timestamp", tx.timestamp)
        return j.toString()
    }

    fun deserializeCustomerTx(jsonStr: String): CustomerTransactionEntity {
        val j = JSONObject(jsonStr)
        return CustomerTransactionEntity(
            id = j.optLong("id", 0L),
            customerId = j.optLong("customerId", 0L),
            type = j.optString("type", "CREDIT_SALE"),
            amount = j.optDouble("amount", 0.0),
            productDescription = j.optString("productDescription", ""),
            notes = j.optString("notes", ""),
            timestamp = j.optLong("timestamp", 0L)
        )
    }
}
