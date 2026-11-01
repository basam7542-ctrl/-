package com.example.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.local.InvoiceWithItems
import com.example.data.local.ReturnWithItems
import com.example.data.local.SaleRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Universal Bluetooth Thermal Receipt Printer Helper (ESC/POS).
 * Supports any standard 58mm / 80mm Bluetooth thermal printer.
 * Renders Arabic receipts into high-contrast monochrome raster bitmaps,
 * ensuring flawless Arabic shaping and compatibility with all printer chipsets.
 */
object BluetoothPrinterHelper {

    private const val PREFS_NAME = "albashiri_bluetooth_printer_prefs"
    private const val KEY_PRINTER_NAME = "pref_printer_name"
    private const val KEY_PRINTER_ADDRESS = "pref_printer_address"
    private const val KEY_STORE_NAME = "pref_store_name"
    const val DEFAULT_STORE_NAME = "مؤسسة البشيري للأجهزة"

    // Standard SPP (Serial Port Profile) UUID for Bluetooth thermal printers
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Standard 58mm printer printable width (384 dots = 48 bytes per line)
    private const val PRINTER_WIDTH_DOTS = 384
    private const val PRINTER_WIDTH_BYTES = PRINTER_WIDTH_DOTS / 8 // 48 bytes

    data class PrinterDevice(
        val name: String,
        val address: String
    )

    data class PrintResult(
        val isSuccess: Boolean,
        val message: String
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSavedPrinter(context: Context): PrinterDevice? {
        val prefs = getPrefs(context)
        val address = prefs.getString(KEY_PRINTER_ADDRESS, null) ?: return null
        val name = prefs.getString(KEY_PRINTER_NAME, "طابعة حرارية") ?: "طابعة حرارية"
        return PrinterDevice(name = name, address = address)
    }

    fun savePrinter(context: Context, printer: PrinterDevice) {
        getPrefs(context).edit()
            .putString(KEY_PRINTER_NAME, printer.name)
            .putString(KEY_PRINTER_ADDRESS, printer.address)
            .apply()
    }

    fun clearSavedPrinter(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_PRINTER_NAME)
            .remove(KEY_PRINTER_ADDRESS)
            .apply()
    }

    fun getStoreName(context: Context): String {
        return getPrefs(context).getString(KEY_STORE_NAME, DEFAULT_STORE_NAME) ?: DEFAULT_STORE_NAME
    }

    fun saveStoreName(context: Context, storeName: String) {
        getPrefs(context).edit()
            .putString(KEY_STORE_NAME, storeName.trim().ifBlank { DEFAULT_STORE_NAME })
            .apply()
    }

    fun hasBluetoothPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return manager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        val adapter = getBluetoothAdapter(context) ?: return false
        return adapter.isEnabled
    }

    /**
     * Lists paired Bluetooth devices on the system.
     */
    fun getPairedPrinters(context: Context): List<PrinterDevice> {
        val adapter = getBluetoothAdapter(context) ?: return emptyList()
        if (!hasBluetoothPermission(context)) return emptyList()

        return try {
            val bonded = adapter.bondedDevices ?: emptySet()
            bonded.map { device ->
                val name = try {
                    device.name ?: "جهاز بدون اسم"
                } catch (e: SecurityException) {
                    "جهاز بلوتوث"
                }
                PrinterDevice(name = name, address = device.address)
            }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    /**
     * Prints a real sale receipt to the currently saved printer.
     */
    suspend fun printSaleReceipt(
        context: Context,
        sale: SaleRecordEntity
    ): PrintResult = withContext(Dispatchers.IO) {
        val printer = getSavedPrinter(context)
            ?: return@withContext PrintResult(
                isSuccess = false,
                message = "لم يتم تعيين طابعة بلوتوث بعد. يرجى اختيار طابعة من شاشة الإعدادات أولاً."
            )

        val storeName = getStoreName(context)
        val bitmap = generateReceiptBitmap(context, sale, storeName)
        sendBitmapToPrinter(context, printer, bitmap)
    }

    suspend fun printInvoiceWithItems(
        context: Context,
        invoiceWithItems: InvoiceWithItems
    ): PrintResult = withContext(Dispatchers.IO) {
        val printer = getSavedPrinter(context)
            ?: return@withContext PrintResult(
                isSuccess = false,
                message = "لم يتم تعيين طابعة بلوتوث بعد. يرجى اختيار طابعة من شاشة الإعدادات أولاً."
            )

        val storeName = getStoreName(context)
        val bitmap = generateInvoiceWithItemsBitmap(context, invoiceWithItems, storeName)
        sendBitmapToPrinter(context, printer, bitmap)
    }

    suspend fun printReturnReceipt(
        context: Context,
        returnWithItems: ReturnWithItems
    ): PrintResult = withContext(Dispatchers.IO) {
        val printer = getSavedPrinter(context)
            ?: return@withContext PrintResult(
                isSuccess = false,
                message = "لم يتم تعيين طابعة بلوتوث بعد. يرجى اختيار طابعة من شاشة الإعدادات أولاً."
            )

        val storeProfile = StoreProfileManager.getStoreProfile(context)
        val bitmap = generateReturnReceiptBitmap(context, returnWithItems, storeProfile)
        sendBitmapToPrinter(context, printer, bitmap)
    }

    /**
     * Prints a test receipt to verify connection and paper feeding.
     */
    suspend fun printTestReceipt(
        context: Context
    ): PrintResult = withContext(Dispatchers.IO) {
        val printer = getSavedPrinter(context)
            ?: return@withContext PrintResult(
                isSuccess = false,
                message = "لم يتم تعيين طابعة بلوتوث بعد. يرجى اختيار طابعة من القائمة أدناه أولاً."
            )

        val storeName = getStoreName(context)
        val bitmap = generateTestBitmap(storeName)
        sendBitmapToPrinter(context, printer, bitmap)
    }

    /**
     * Connects to the Bluetooth printer and transmits ESC/POS raster image data.
     */
    private fun sendBitmapToPrinter(
        context: Context,
        printer: PrinterDevice,
        bitmap: Bitmap
    ): PrintResult {
        val adapter = getBluetoothAdapter(context)
            ?: return PrintResult(false, "جهازك لا يدعم البلوتوث.")

        if (!adapter.isEnabled) {
            return PrintResult(false, "البلوتوث مغلق. يرجى تشغيل البلوتوث ثم المحاولة مجددًا.")
        }

        if (!hasBluetoothPermission(context)) {
            return PrintResult(false, "إذن البلوتوث غير ممنوح للتطبيق.")
        }

        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        return try {
            val device: BluetoothDevice = adapter.getRemoteDevice(printer.address)

            // Cancel discovery to free RFCOMM bandwidth
            try {
                adapter.cancelDiscovery()
            } catch (_: SecurityException) {}

            // Open RFCOMM Socket (standard SPP)
            socket = try {
                device.createRfcommSocketToServiceRecord(SPP_UUID)
            } catch (e: Exception) {
                // Fallback to reflection method for devices with non-standard Bluetooth stacks
                val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                m.invoke(device, 1) as BluetoothSocket
            }

            socket.connect()
            outputStream = socket.outputStream

            // 1. ESC @ (Initialize printer)
            outputStream.write(byteArrayOf(0x1B, 0x40))

            // 2. ESC a 1 (Align center)
            outputStream.write(byteArrayOf(0x1B, 0x61, 0x01))

            // 3. Convert Bitmap to ESC/POS GS v 0 raster command
            val rasterData = convertBitmapToEscPosRaster(bitmap)
            outputStream.write(rasterData)

            // 4. Feed 4 lines so receipt emerges past the tear bar
            outputStream.write(byteArrayOf(0x1B, 0x64, 0x04))

            // 5. Optional GS V cut command (ignored by manual tear printers)
            outputStream.write(byteArrayOf(0x1D, 0x56, 0x41, 0x00))

            outputStream.flush()
            Thread.sleep(150) // Allow printer buffer to process

            PrintResult(true, "تمت طباعة الإيصال بنجاح عبر طابعة ${printer.name}")
        } catch (e: IOException) {
            PrintResult(false, "تعذر الاتصال بالطابعة [${printer.name}]. تأكد من تشغيلها وأنها قريبة من هاتفك.")
        } catch (e: SecurityException) {
            PrintResult(false, "تم رفض إذن البلوتوث من قبل النظام.")
        } catch (e: Exception) {
            PrintResult(false, "خطأ أثناء الطباعة: ${e.localizedMessage ?: "حدث خطأ غير متوقع"}")
        } finally {
            try {
                outputStream?.close()
            } catch (_: Exception) {}
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * Renders a professional, crisp sale receipt into an Android Bitmap.
     * Uses Canvas and TextPaint with native RTL Arabic text shaping.
     * Integrates official monochrome Al-Bashiri store logo, Cairo font, and customer details.
     */
     private fun generateReceiptBitmap(context: Context, sale: SaleRecordEntity, storeName: String): Bitmap {
        val width = PRINTER_WIDTH_DOTS
        val numberFormat = NumberFormat.getNumberInstance(Locale("ar", "EG"))
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG"))
        val dateString = dateFormat.format(Date(sale.timestamp))

        // Load Cairo Font if available
        val cairoTypeface = try {
            ResourcesCompat.getFont(context, R.font.cairo)
        } catch (_: Exception) {
            null
        }

        // Decode and scale official monochrome store logo
        val logoBitmap = try {
            val original = BitmapFactory.decodeResource(context.resources, R.drawable.ic_albashiri_invoice_logo)
            if (original != null) {
                val targetWidth = 190
                val targetHeight = (original.height * (targetWidth.toFloat() / original.width)).toInt()
                Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true)
            } else null
        } catch (_: Exception) {
            null
        }

        // Paints for clean monochrome receipt
        val paintHeader = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 28f
            typeface = if (cairoTypeface != null) Typeface.create(cairoTypeface, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintSub = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 19f
            typeface = cairoTypeface ?: Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }

        val paintTitle = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = if (cairoTypeface != null) Typeface.create(cairoTypeface, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintRegular = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 19f
            typeface = cairoTypeface ?: Typeface.DEFAULT
            textAlign = Paint.Align.RIGHT
        }

        val paintBold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 21f
            typeface = if (cairoTypeface != null) Typeface.create(cairoTypeface, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val paintTotal = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 26f
            typeface = if (cairoTypeface != null) Typeface.create(cairoTypeface, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintSeparator = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }

        // Calculate height
        var estimatedHeight = 580
        if (logoBitmap != null) estimatedHeight += logoBitmap.height + 25
        if (sale.customerName.isNotBlank()) estimatedHeight += 32
        if (sale.customerPhone.isNotBlank()) estimatedHeight += 32
        if (sale.notes.isNotBlank()) estimatedHeight += 40
        if (sale.brandName.isNotBlank() || sale.categoryName.isNotBlank()) estimatedHeight += 35

        val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val centerX = width / 2f
        val rightX = width - 14f
        var currentY = 30f

        // 1. Draw Official Store Logo
        if (logoBitmap != null) {
            val logoLeft = (width - logoBitmap.width) / 2f
            canvas.drawBitmap(logoBitmap, logoLeft, currentY, null)
            currentY += logoBitmap.height + 14f
        }

        // 2. Store Header
        canvas.drawText(storeName, centerX, currentY, paintHeader)
        currentY += 30f
        canvas.drawText("لتجارة وتوزيع الأجهزة الكهربائية والمنزلية", centerX, currentY, paintSub)
        currentY += 26f

        // Separator
        canvas.drawText("------------------------------------------------", centerX, currentY, paintSeparator)
        currentY += 28f

        // Receipt Title
        canvas.drawText("★ فاتورة بيع نقدي ★", centerX, currentY, paintTitle)
        currentY += 32f

        // Metadata
        canvas.drawText("رقم الفاتورة: #${sale.id}", rightX, currentY, paintBold)
        currentY += 28f
        canvas.drawText("التاريخ: $dateString", rightX, currentY, paintRegular)
        currentY += 28f
        canvas.drawText("اسم البائع: ${sale.sellerName}", rightX, currentY, paintRegular)
        currentY += 28f

        // Customer Information Block (if recorded)
        if (sale.customerName.isNotBlank() || sale.customerPhone.isNotBlank()) {
            canvas.drawText("- - - - - - - - - - - - - - - - - - - - - - - -", centerX, currentY, paintSeparator)
            currentY += 24f
            if (sale.customerName.isNotBlank()) {
                canvas.drawText("العميل: ${sale.customerName}", rightX, currentY, paintBold)
                currentY += 28f
            }
            if (sale.customerPhone.isNotBlank()) {
                canvas.drawText("هاتف العميل: ${sale.customerPhone}", rightX, currentY, paintRegular)
                currentY += 28f
            }
        }

        // Separator
        canvas.drawText("------------------------------------------------", centerX, currentY, paintSeparator)
        currentY += 28f

        // Item details
        canvas.drawText("المنتج: ${sale.productName}", rightX, currentY, paintBold)
        currentY += 28f
        canvas.drawText("الموديل: ${sale.modelName}", rightX, currentY, paintRegular)
        currentY += 28f

        val brandCat = listOf(sale.brandName, sale.categoryName).filter { it.isNotBlank() }.joinToString(" • ")
        if (brandCat.isNotBlank()) {
            canvas.drawText("المواصفة: $brandCat", rightX, currentY, paintRegular)
            currentY += 28f
        }

        val priceStr = "${sale.quantity} قطعة × ${numberFormat.format(sale.soldPrice)} ج.م"
        canvas.drawText("الكمية والسعر: $priceStr", rightX, currentY, paintBold)
        currentY += 30f

        if (sale.notes.isNotBlank()) {
            canvas.drawText("ملاحظات: ${sale.notes}", rightX, currentY, paintRegular)
            currentY += 28f
        }

        // Total block
        canvas.drawText("================================", centerX, currentY, paintSeparator)
        currentY += 34f
        canvas.drawText("الإجمالي: ${numberFormat.format(sale.totalPrice)} ج.م", centerX, currentY, paintTotal)
        currentY += 30f
        canvas.drawText("================================", centerX, currentY, paintSeparator)
        currentY += 30f

        // Terms & Policy
        canvas.drawText("البضاعة المباعة ترد وتستبدل خلال 14 يوماً بالفاتورة", centerX, currentY, paintSub)
        currentY += 24f

        // Footer
        canvas.drawText("شكراً لتعاملكم معنا • نسعد بزيارتكم دائماً", centerX, currentY, paintSub)
        currentY += 24f
        canvas.drawText("مؤسسة البشيري للأجهزة الكهربائية", centerX, currentY, paintSub)
        currentY += 26f

        // Crop bitmap to actual painted height plus bottom padding
        val finalHeight = currentY.toInt().coerceAtMost(estimatedHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, width, finalHeight)
    }

    /**
     * Renders a clean test print receipt.
     */
    private fun generateTestBitmap(storeName: String): Bitmap {
        val width = PRINTER_WIDTH_DOTS
        val height = 360
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val centerX = width / 2f
        var currentY = 40f

        val paintHeader = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintTitle = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintSub = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 19f
            textAlign = Paint.Align.CENTER
        }

        val paintSeparator = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(storeName, centerX, currentY, paintHeader)
        currentY += 34f
        canvas.drawText("طابعة فواتير بلوتوث حرارية", centerX, currentY, paintSub)
        currentY += 28f
        canvas.drawText("------------------------------------------------", centerX, currentY, paintSeparator)
        currentY += 30f

        canvas.drawText("✓ اختبار اتصال الطابعة ناجح", centerX, currentY, paintTitle)
        currentY += 32f
        canvas.drawText("بروتوكول القياسي: ESC/POS (58mm/80mm)", centerX, currentY, paintSub)
        currentY += 28f

        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG"))
        canvas.drawText(dateFormat.format(Date()), centerX, currentY, paintSub)
        currentY += 28f
        canvas.drawText("------------------------------------------------", centerX, currentY, paintSeparator)
        currentY += 30f
        canvas.drawText("جاهزة لطباعة إيصالات المبيعات", centerX, currentY, paintSub)

        return bitmap
    }

    /**
     * Renders a multi-item invoice into a high-contrast monochrome bitmap for Bluetooth thermal printers.
     */
    fun generateInvoiceWithItemsBitmap(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        storeName: String = DEFAULT_STORE_NAME
    ): Bitmap {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items
        val width = PRINTER_WIDTH_DOTS

        val cairoTypeface: Typeface = try {
            ResourcesCompat.getFont(context, R.font.cairo) ?: Typeface.DEFAULT
        } catch (_: Exception) {
            Typeface.DEFAULT
        }

        val activeStoreName = invoice.storeName.ifBlank { storeName }

        val contactLine = listOfNotNull(
            if (invoice.storeAddress.isNotBlank()) "العنوان: ${invoice.storeAddress.trim()}" else null,
            if (invoice.storePhone.isNotBlank()) "هاتف: ${invoice.storePhone.trim()}" else null
        ).joinToString(" • ")

        val legalLine = listOfNotNull(
            if (invoice.commercialRegister.isNotBlank()) "سجل: ${invoice.commercialRegister.trim()}" else null,
            if (invoice.taxCard.isNotBlank()) "ضريبة: ${invoice.taxCard.trim()}" else null,
            if (invoice.vatNumber.isNotBlank()) "تسجيل: ${invoice.vatNumber.trim()}" else null
        ).joinToString(" • ")

        val estimatedHeight = 670 + (items.size * 65) +
            (if (invoice.discount > 0) 50 else 0) +
            (if (invoice.customerName.isNotBlank() || invoice.customerPhone.isNotBlank()) 70 else 0) +
            (if (invoice.notes.isNotBlank()) 50 else 0) +
            (if (contactLine.isNotBlank()) 32 else 0) +
            (if (legalLine.isNotBlank()) 32 else 0)

        val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val centerX = width / 2f
        var currentY = 24f

        val paintTitle = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 28f
            typeface = Typeface.create(cairoTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val paintSub = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 17f
            typeface = cairoTypeface
            textAlign = Paint.Align.CENTER
        }

        val paintText = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            typeface = cairoTypeface
            textAlign = Paint.Align.RIGHT
        }

        val paintTextBold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 19f
            typeface = Typeface.create(cairoTypeface, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val paintTextLeft = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            typeface = cairoTypeface
            textAlign = Paint.Align.LEFT
        }

        val paintTextLeftBold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(cairoTypeface, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        val paintSeparator = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }

        // 1. Draw monochrome store logo (custom if present, else official default)
        try {
            val customBitmap = if (invoice.storeLogoUri.isNotBlank()) {
                StoreProfileManager.loadLogoBitmap(invoice.storeLogoUri)
            } else null
            val logoBitmap = customBitmap ?: BitmapFactory.decodeResource(context.resources, R.drawable.ic_albashiri_invoice_logo)
            if (logoBitmap != null) {
                val logoWidth = 180
                val logoHeight = (logoBitmap.height * (logoWidth.toFloat() / logoBitmap.width)).toInt()
                val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoWidth, logoHeight, true)
                canvas.drawBitmap(scaledLogo, centerX - (logoWidth / 2f), currentY, null)
                currentY += logoHeight + 14f
            }
        } catch (_: Exception) {}

        // 2. Store Header
        canvas.drawText(activeStoreName, centerX, currentY, paintTitle)
        currentY += 30f

        if (contactLine.isNotBlank()) {
            canvas.drawText(contactLine, centerX, currentY, paintSub)
            currentY += 26f
        }

        if (legalLine.isNotBlank()) {
            canvas.drawText(legalLine, centerX, currentY, paintSub)
            currentY += 26f
        }

        canvas.drawText("فاتورة مبيعات ضريبية مبسطة", centerX, currentY, paintSub)
        currentY += 26f
        canvas.drawText("------------------------------------------------", centerX, currentY, paintSeparator)
        currentY += 26f

        // 3. Invoice Meta
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar", "EG"))
        canvas.drawText("رقم الفاتورة: #${invoice.invoiceNumber}", width - 20f, currentY, paintTextBold)
        currentY += 26f
        canvas.drawText("التاريخ: ${dateFormat.format(Date(invoice.timestamp))}", width - 20f, currentY, paintText)
        currentY += 26f
        canvas.drawText("البائع: ${invoice.sellerName}", width - 20f, currentY, paintText)
        currentY += 26f

        if (invoice.customerName.isNotBlank() || invoice.customerPhone.isNotBlank()) {
            if (invoice.customerName.isNotBlank()) {
                canvas.drawText("العميل: ${invoice.customerName}", width - 20f, currentY, paintText)
                currentY += 26f
            }
            if (invoice.customerPhone.isNotBlank()) {
                canvas.drawText("الهاتف: ${invoice.customerPhone}", width - 20f, currentY, paintText)
                currentY += 26f
            }
        }

        canvas.drawText("================================================", centerX, currentY, paintSeparator)
        currentY += 28f

        // 4. Multi-Item Table Header
        canvas.drawText("الصنف / الموديل", width - 20f, currentY, paintTextBold)
        canvas.drawText("الكمية × السعر | الإجمالي", 20f, currentY, paintTextLeftBold)
        currentY += 24f
        canvas.drawText("------------------------------------------------", centerX, currentY, paintSeparator)
        currentY += 24f

        val numberFormat = NumberFormat.getNumberInstance(Locale("ar", "EG"))

        // 5. Render Every Line Item
        items.forEachIndexed { index, item ->
            canvas.drawText("${index + 1}. ${item.productName} - ${item.modelName}", width - 20f, currentY, paintTextBold)
            currentY += 24f
            val details = "${item.quantity} × ${numberFormat.format(item.unitPrice)} = ${numberFormat.format(item.totalPrice)} ج.م"
            canvas.drawText(details, 20f, currentY, paintTextLeft)
            currentY += 24f
        }

        canvas.drawText("================================================", centerX, currentY, paintSeparator)
        currentY += 28f

        // 6. Financial Summary
        if (invoice.discount > 0) {
            canvas.drawText("إجمالي الأصناف:", width - 20f, currentY, paintText)
            canvas.drawText("${numberFormat.format(invoice.subtotal)} ج.م", 20f, currentY, paintTextLeft)
            currentY += 26f
            canvas.drawText("الخصم التجاري:", width - 20f, currentY, paintText)
            canvas.drawText("-${numberFormat.format(invoice.discount)} ج.م", 20f, currentY, paintTextLeft)
            currentY += 26f
        }

        canvas.drawText("الصافي المستحق:", width - 20f, currentY, paintTextBold)
        canvas.drawText("${numberFormat.format(invoice.finalTotal)} ج.م", 20f, currentY, paintTextLeftBold)
        currentY += 28f

        canvas.drawText("المدفوع نقداً:", width - 20f, currentY, paintText)
        canvas.drawText("${numberFormat.format(invoice.paidAmount)} ج.م", 20f, currentY, paintTextLeft)
        currentY += 26f

        if (invoice.remainingAmount > 0) {
            canvas.drawText("المتبقي آجل:", width - 20f, currentY, paintTextBold)
            canvas.drawText("${numberFormat.format(invoice.remainingAmount)} ج.م", 20f, currentY, paintTextLeftBold)
            currentY += 26f
        }

        if (invoice.notes.isNotBlank()) {
            currentY += 10f
            canvas.drawText("ملاحظات: ${invoice.notes}", width - 20f, currentY, paintText)
            currentY += 24f
        }

        currentY += 16f
        canvas.drawText("------------------------------------------------", centerX, currentY, paintSeparator)
        currentY += 26f

        // 7. Footer
        canvas.drawText("شكراً لتعاملكم مع البشيري للأجهزة الكهربائية", centerX, currentY, paintSub)
        currentY += 24f
        canvas.drawText("خدمة العملاء والصيانة: 01000000000", centerX, currentY, paintSub)

        return bitmap
    }

    /**
     * Generates a high-contrast monochrome raster bitmap for Return slips (سند مرتجع).
     */
    fun generateReturnReceiptBitmap(
        context: Context,
        returnWithItems: ReturnWithItems,
        storeProfile: StoreProfile? = null
    ): Bitmap {
        val width = PRINTER_WIDTH_DOTS
        val cairoTypeface: Typeface = try {
            ResourcesCompat.getFont(context, R.font.cairo) ?: Typeface.DEFAULT
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
        val numberFormat = NumberFormat.getNumberInstance(Locale("ar", "EG"))
        val rec = returnWithItems.returnRecord
        val items = returnWithItems.items

        val profile = storeProfile ?: StoreProfileManager.getStoreProfile(context)
        val finalStoreName = profile.storeName.ifBlank { DEFAULT_STORE_NAME }

        val contactLine = listOfNotNull(
            if (profile.storePhone.isNotBlank()) "تليفون: ${profile.storePhone.trim()}" else null,
            if (profile.storeAddress.isNotBlank()) profile.storeAddress.trim() else null
        ).joinToString(" • ")

        val legalLine = listOfNotNull(
            if (profile.commercialRegister.isNotBlank()) "س.ت: ${profile.commercialRegister.trim()}" else null,
            if (profile.taxCard.isNotBlank()) "ضريبة: ${profile.taxCard.trim()}" else null,
            if (profile.vatNumber.isNotBlank()) "تسجيل: ${profile.vatNumber.trim()}" else null
        ).joinToString(" • ")

        val estimatedHeight = 650 + (items.size * 65) +
            (if (rec.partyName.isNotBlank() || rec.partyPhone.isNotBlank()) 60 else 0) +
            (if (rec.originalInvoiceNumber.isNotBlank()) 35 else 0) +
            (if (rec.reason.isNotBlank()) 45 else 0) +
            (if (rec.settlementNotes.isNotBlank()) 45 else 0) +
            (if (contactLine.isNotBlank()) 30 else 0) +
            (if (legalLine.isNotBlank()) 30 else 0)

        val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val centerX = width / 2f
        var currentY = 24f

        val paintTitle = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 28f
            typeface = Typeface.create(cairoTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val paintSub = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 17f
            typeface = cairoTypeface
            textAlign = Paint.Align.CENTER
        }
        val paintText = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            typeface = cairoTypeface
            textAlign = Paint.Align.RIGHT
        }
        val paintTextBold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 19f
            typeface = Typeface.create(cairoTypeface, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val paintTextLeft = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            typeface = cairoTypeface
            textAlign = Paint.Align.LEFT
        }
        val paintTextLeftBold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(cairoTypeface, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val paintSeparator = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }

        // 1. Logo
        try {
            val customBitmap = if (profile.logoPath.isNotBlank()) {
                StoreProfileManager.loadLogoBitmap(profile.logoPath)
            } else null
            val logoBitmap = customBitmap ?: BitmapFactory.decodeResource(context.resources, R.drawable.ic_albashiri_invoice_logo)
            if (logoBitmap != null) {
                val logoWidth = 140
                val logoHeight = (logoBitmap.height.toFloat() / logoBitmap.width * logoWidth).toInt().coerceAtMost(100)
                val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoWidth, logoHeight, true)
                canvas.drawBitmap(scaledLogo, centerX - (logoWidth / 2f), currentY, null)
                currentY += logoHeight + 14f
            }
        } catch (_: Exception) {
            currentY += 10f
        }

        canvas.drawText(finalStoreName, centerX, currentY, paintTitle)
        currentY += 28f

        if (contactLine.isNotBlank()) {
            canvas.drawText(contactLine, centerX, currentY, paintSub)
            currentY += 22f
        }
        if (legalLine.isNotBlank()) {
            canvas.drawText(legalLine, centerX, currentY, paintSub)
            currentY += 22f
        }

        val typeArabicTitle = when (rec.type) {
            "CUSTOMER_TO_STORE" -> "سند إرجاع - من عميل للمعرض"
            "STORE_TO_SUPPLIER" -> "سند إرجاع - من المعرض للمورد"
            "MERCHANT_TO_STORE" -> "سند إرجاع - من تاجر للمعرض"
            "STORE_TO_MERCHANT" -> "سند إرجاع - من المعرض لتاجر"
            else -> "سند مرتجع رسمي"
        }

        currentY += 10f
        canvas.drawText("================================", centerX, currentY, paintSeparator)
        currentY += 26f
        canvas.drawText("【 $typeArabicTitle 】", centerX, currentY, paintTitle)
        currentY += 26f
        canvas.drawText("================================", centerX, currentY, paintSeparator)
        currentY += 26f

        val dateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale("ar"))
        canvas.drawText("رقم المرتجع: ${rec.returnNumber}", width - 18f, currentY, paintTextBold)
        currentY += 24f
        canvas.drawText("التاريخ: ${dateFormat.format(Date(rec.timestamp))}", width - 18f, currentY, paintText)
        currentY += 24f

        if (rec.originalInvoiceNumber.isNotBlank()) {
            canvas.drawText("رقم الفاتورة الأصلية: ${rec.originalInvoiceNumber}", width - 18f, currentY, paintTextBold)
            currentY += 24f
        }

        val partyLabel = when (rec.type) {
            "CUSTOMER_TO_STORE" -> "العميل"
            "STORE_TO_SUPPLIER" -> "المورد"
            else -> "التاجر"
        }
        if (rec.partyName.isNotBlank()) {
            val phonePart = if (rec.partyPhone.isNotBlank()) " (${rec.partyPhone})" else ""
            canvas.drawText("$partyLabel: ${rec.partyName}$phonePart", width - 18f, currentY, paintText)
            currentY += 24f
        }
        canvas.drawText("المسؤول: ${rec.createdByName}", width - 18f, currentY, paintText)
        currentY += 24f

        canvas.drawText("--------------------------------", centerX, currentY, paintSeparator)
        currentY += 22f

        // Table header
        canvas.drawText("الصنف والموديل (الحالة)", width - 18f, currentY, paintTextBold)
        canvas.drawText("الكمية × السعر", 18f, currentY, paintTextLeftBold)
        currentY += 24f
        canvas.drawText("--------------------------------", centerX, currentY, paintSeparator)
        currentY += 22f

        // Items
        for (item in items) {
            val conditionArabic = when (item.stockCondition) {
                "RESELLABLE" -> "صالح للبيع"
                "DAMAGED" -> "تالف"
                "INSPECTION" -> "تحت الفحص"
                "REPAIRABLE" -> "صيانة"
                else -> item.stockCondition
            }
            val title = "${item.productName} - ${item.modelName} [$conditionArabic]"
            canvas.drawText(title, width - 18f, currentY, paintTextBold)
            currentY += 22f

            val qtyPrice = "${item.quantity} × ${numberFormat.format(item.unitPrice)} ج.م"
            val lineTotal = "${numberFormat.format(item.totalPrice)} ج.م"
            canvas.drawText(qtyPrice, width - 18f, currentY, paintText)
            canvas.drawText(lineTotal, 18f, currentY, paintTextLeftBold)
            currentY += 26f
        }

        canvas.drawText("================================", centerX, currentY, paintSeparator)
        currentY += 26f

        // Financial totals
        canvas.drawText("إجمالي قيمة المرتجع:", width - 18f, currentY, paintTextBold)
        canvas.drawText("${numberFormat.format(rec.totalAmount)} ج.م", 18f, currentY, paintTextLeftBold)
        currentY += 28f

        val settlementArabic = when (rec.settlementType) {
            "CASH_REFUND" -> "استرداد نقدي"
            "CUSTOMER_CREDIT" -> "إضافة لرصيد الحساب"
            "EXCHANGE" -> "استبدال بضاعة"
            "DECREASE_DEBT" -> "خصم من مديونية المورد"
            "DECREASE_MERCHANT_DUE" -> "خصم من مستحقات التاجر"
            "CASH_REFUND_FROM_SUPPLIER" -> "استرداد نقدي من المورد"
            else -> rec.settlementType
        }
        canvas.drawText("نوع التسوية: $settlementArabic", width - 18f, currentY, paintTextBold)
        currentY += 24f
        canvas.drawText("المبلغ المسوى:", width - 18f, currentY, paintTextBold)
        canvas.drawText("${numberFormat.format(rec.settlementAmount)} ج.م", 18f, currentY, paintTextLeftBold)
        currentY += 28f

        if (rec.reason.isNotBlank()) {
            canvas.drawText("سبب الإرجاع: ${rec.reason}", width - 18f, currentY, paintText)
            currentY += 24f
        }
        if (rec.notes.isNotBlank()) {
            canvas.drawText("ملاحظات: ${rec.notes}", width - 18f, currentY, paintText)
            currentY += 24f
        }

        currentY += 16f
        canvas.drawText("توقيع المستلم: ....................", width - 18f, currentY, paintText)
        currentY += 24f
        canvas.drawText("اعتماد الإدارة: ....................", width - 18f, currentY, paintText)
        currentY += 28f

        canvas.drawText("--------------------------------", centerX, currentY, paintSeparator)
        currentY += 22f
        canvas.drawText("سند مرتجع معتمد آلياً بالنظام", centerX, currentY, paintSub)

        return bitmap
    }

    /**
     * Converts a Bitmap into ESC/POS GS v 0 (Raster bit image) byte array.
     * Command syntax:
     * [0x1D, 0x76, 0x30, mode, xL, xH, yL, yH, ...data...]
     * where xL + xH * 256 = width in bytes (PRINTER_WIDTH_BYTES = 48)
     * and yL + yH * 256 = height in dots
     */
    private fun convertBitmapToEscPosRaster(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8

        val xL = (widthBytes % 256).toByte()
        val xH = (widthBytes / 256).toByte()
        val yL = (height % 256).toByte()
        val yH = (height / 256).toByte()

        val header = byteArrayOf(0x1D, 0x76, 0x30, 0x00, xL, xH, yL, yH)
        val dataSize = widthBytes * height
        val result = ByteArray(header.size + dataSize)

        System.arraycopy(header, 0, result, 0, header.size)

        var destIndex = header.size
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            for (byteX in 0 until widthBytes) {
                var currentByte = 0
                for (bit in 0 until 8) {
                    val x = byteX * 8 + bit
                    if (x < width) {
                        val pixel = pixels[y * width + x]
                        // Standard luminance formula
                        val r = (pixel shr 16) and 0xFF
                        val g = (pixel shr 8) and 0xFF
                        val b = pixel and 0xFF
                        val luminance = (r * 299 + g * 587 + b * 114) / 1000

                        // Black pixel threshold (1 = dot printed, 0 = white paper)
                        if (luminance < 165) {
                            currentByte = currentByte or (1 shl (7 - bit))
                        }
                    }
                }
                result[destIndex++] = currentByte.toByte()
            }
        }

        return result
    }
}
