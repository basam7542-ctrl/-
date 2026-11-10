package com.example.data.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Safe database backup/restore helper.
 *
 * - Backup: checkpoint WAL, then copy the DB file to a target Uri.
 * - Restore: validate backup, keep a recoverable copy of the current DB,
 *   close Room, replace files atomically using temp file in same directory,
 *   verify new DB opens, then cleanup. On failure, restore the previous DB.
 *
 * All I/O runs on IO dispatcher. Uses DatabaseLockManager for centralized locking.
 */
object BackupManager {

    private const val DB_NAME = "albashiri_store.db"
    private const val MIN_SCHEMA_VERSION = 1
    private const val MAX_SCHEMA_VERSION = 15
    private const val PRE_RESTORE_PREFIX = ".pre_restore_"
    private const val RESTORING_PREFIX = ".restoring_"

    private val recoveryMutex = Mutex()
    @Volatile
    private var recoveryResult: Result<Unit>? = null

    /**
     * Single, sequential startup recovery gate.
     * - Successful recovery is cached and reused for the lifetime of the process.
     * - Failed recovery is NOT cached as a final state, so the next call can retry.
     * - Cancellation is propagated (never treated as a normal recovery failure).
     * - Mutex guarantees sequential execution (no concurrent recovery attempts).
     */
    suspend fun ensureRecoveryCompleted(context: Context): Result<Unit> {
        recoveryResult?.takeIf { it.isSuccess }?.let { return it }
        return recoveryMutex.withLock {
            recoveryResult?.takeIf { it.isSuccess }?.let { return@withLock it }
            try {
                recoverIfNeeded(context.applicationContext)
                val success = Result.success(Unit)
                recoveryResult = success
                success
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    fun dbFile(context: Context): File = context.getDatabasePath(DB_NAME)

    fun dbDirectory(context: Context): File = dbFile(context).parentFile!!

    /**
     * Pre-Room guard for v15 migration safety.
     * Detects duplicate invoice numbers (invoices.invoiceNumber) and duplicate return
     * numbers (return_records.returnNumber) before Room opens/migrates. If any duplicates
     * exist in either type, exports a safe CSV report and throws to block Room open/migration.
     * No invoice/return numbers or records are modified or deleted.
     */
    private fun assertNoDuplicateNumbersBlockingV15Migration(context: Context) {
        val dbPath = dbFile(context)
        if (!dbPath.exists() || dbPath.length() == 0L) return

        var db: android.database.sqlite.SQLiteDatabase? = null
        try {
            db = android.database.sqlite.SQLiteDatabase.openDatabase(
                dbPath.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
            )

            // Ensure WAL contents are visible/consistent before inspection (no DB copy).
            try { db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { it.moveToFirst() } } catch (_: Exception) {}

            val version = db.rawQuery("PRAGMA user_version", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
            if (version >= 15) return

            fun hasTable(name: String): Boolean = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                arrayOf(name)
            ).use { it.moveToFirst() }

            val duplicateInvoiceNumbers = mutableListOf<String>()
            if (hasTable("invoices")) {
                db.rawQuery(
                    "SELECT invoiceNumber, COUNT(*) AS c FROM invoices GROUP BY invoiceNumber HAVING c > 1 ORDER BY invoiceNumber"
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        duplicateInvoiceNumbers.add(cursor.getString(0) ?: "")
                    }
                }
            }

            val duplicateReturnNumbers = mutableListOf<String>()
            if (hasTable("return_records")) {
                db.rawQuery(
                    "SELECT returnNumber, COUNT(*) AS c FROM return_records GROUP BY returnNumber HAVING c > 1 ORDER BY returnNumber"
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        duplicateReturnNumbers.add(cursor.getString(0) ?: "")
                    }
                }
            }

            if (duplicateInvoiceNumbers.isEmpty() && duplicateReturnNumbers.isEmpty()) return

            val exportFile = exportDuplicateNumbersReport(
                context = context,
                db = db,
                dbVersion = version,
                duplicateInvoiceNumbers = duplicateInvoiceNumbers,
                duplicateReturnNumbers = duplicateReturnNumbers
            )

            val parts = mutableListOf<String>()
            if (duplicateInvoiceNumbers.isNotEmpty()) parts.add("أرقام فواتير مكررة (${duplicateInvoiceNumbers.size})")
            if (duplicateReturnNumbers.isNotEmpty()) parts.add("أرقام مرتجعات مكررة (${duplicateReturnNumbers.size})")
            throw IllegalStateException(
                "تم اكتشاف تكرارات قبل ترحيل/فتح قاعدة البيانات (v15): ${parts.joinToString("، ")}. " +
                    "لحماية البيانات لم يتم تعديل أو حذف أي سجل ولم يتم فتح/ترحيل قاعدة البيانات. " +
                    "تم تصدير تقرير التكرارات للمراجعة إلى: ${exportFile.absolutePath}"
            )
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            // If pre-v15 duplicate safety check cannot be completed reliably, block Room open
            // rather than risking an unsafe migration on a database that may contain duplicates.
            throw IllegalStateException(
                "تعذر إكمال فحص سلامة أرقام الفواتير/المرتجعات قبل ترحيل قاعدة البيانات إلى v15. " +
                    "تم إيقاف الفتح الآمن لحماية البيانات. التفاصيل: ${e.message ?: e.javaClass.simpleName}",
                e
            )
        } finally {
            try { db?.close() } catch (_: Exception) {}
        }
    }

    private fun exportDuplicateNumbersReport(
        context: Context,
        db: android.database.sqlite.SQLiteDatabase,
        dbVersion: Int,
        duplicateInvoiceNumbers: List<String>,
        duplicateReturnNumbers: List<String>
    ): File {
        val reportDir = (context.getExternalFilesDir("migration_reports") ?: context.filesDir).apply { mkdirs() }
        val ts = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
            .format(java.util.Date())
        val file = File(reportDir, "duplicate_numbers_v15_${ts}.csv")

        fun csv(value: Any?): String {
            val s = (value?.toString() ?: "").replace("\"", "\"\"")
            return "\"$s\""
        }

        fun hasTable(name: String): Boolean = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(name)
        ).use { it.moveToFirst() }

        val hasInvoiceItems = hasTable("invoice_items")
        val hasReturnItems = hasTable("return_items")

        java.io.BufferedWriter(java.io.OutputStreamWriter(java.io.FileOutputStream(file), Charsets.UTF_8)).use { out ->
            out.append('\uFEFF') // BOM for Excel/Arabic readability
            out.appendLine("# Duplicate invoice/return numbers report (pre-Room, pre-v15 migration)")
            out.appendLine("# GeneratedAt=${java.util.Date()} ; dbVersion=$dbVersion ; duplicateInvoiceNumbers=${duplicateInvoiceNumbers.size} ; duplicateReturnNumbers=${duplicateReturnNumbers.size}")
            out.appendLine("recordType,duplicateNumber,recordId,itemCount,timestamp,partyOrCustomer,sellerOrCreatedBy,amount,status,extraRef,notes")

            // Invoice duplicates
            for (num in duplicateInvoiceNumbers) {
                val itemCountExpr = if (hasInvoiceItems) {
                    "(SELECT COUNT(*) FROM invoice_items ii WHERE ii.invoiceId = i.id)"
                } else {
                    "0"
                }
                db.rawQuery(
                    """
                    SELECT i.id, i.invoiceNumber, i.timestamp, i.customerName, i.sellerName,
                           i.finalTotal, i.paymentStatus, i.notes, $itemCountExpr AS itemCount
                    FROM invoices i
                    WHERE i.invoiceNumber = ?
                    ORDER BY i.timestamp ASC, i.id ASC
                    """.trimIndent(),
                    arrayOf(num)
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        val row = listOf(
                            "INVOICE",
                            cursor.getString(1),
                            cursor.getLong(0),
                            cursor.getLong(8),
                            cursor.getLong(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getDouble(5),
                            cursor.getString(6),
                            "",
                            cursor.getString(7)
                        )
                        out.appendLine(row.joinToString(",") { csv(it) })
                    }
                }
            }

            // Return duplicates
            for (num in duplicateReturnNumbers) {
                val itemCountExpr = if (hasReturnItems) {
                    "(SELECT COUNT(*) FROM return_items ri WHERE ri.returnId = r.id)"
                } else {
                    "0"
                }
                // Detect created-by column name defensively (schema evolved across versions).
                val createdByCol = when {
                    columnExists(db, "return_records", "createdByName") -> "createdByName"
                    columnExists(db, "return_records", "createdByUserName") -> "createdByUserName"
                    else -> "''"
                }
                db.rawQuery(
                    """
                    SELECT r.id, r.returnNumber, r.timestamp, r.partyName, $createdByCol AS createdBy,
                           r.totalAmount, r.status, r.originalInvoiceNumber, r.notes, $itemCountExpr AS itemCount
                    FROM return_records r
                    WHERE r.returnNumber = ?
                    ORDER BY r.timestamp ASC, r.id ASC
                    """.trimIndent(),
                    arrayOf(num)
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        val row = listOf(
                            "RETURN",
                            cursor.getString(1),
                            cursor.getLong(0),
                            cursor.getLong(9),
                            cursor.getLong(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getDouble(5),
                            cursor.getString(6),
                            cursor.getString(7),
                            cursor.getString(8)
                        )
                        out.appendLine(row.joinToString(",") { csv(it) })
                    }
                }
            }
        }
        return file
    }

    private fun columnExists(
        db: android.database.sqlite.SQLiteDatabase,
        table: String,
        column: String
    ): Boolean = try {
        db.rawQuery("PRAGMA table_info($table)", null).use { cursor ->
            val nameIdx = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                val colName = if (nameIdx >= 0) cursor.getString(nameIdx) else null
                if (colName == column) return@use true
            }
            false
        }
    } catch (_: Exception) {
        false
    }

    private fun checkDatabaseIntegrity(dbFile: File): Boolean {
        return try {
            if (!dbFile.exists() || dbFile.length() == 0L) return false
            val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                dbFile.absolutePath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )
            val cursor = db.rawQuery("PRAGMA integrity_check", null)
            val ok = cursor.moveToFirst() && cursor.getString(0) == "ok"
            cursor.close()
            db.close()
            ok
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks for interrupted restore on app startup and recovers automatically.
     * Call this early in Application/Activity startup before any DB access.
     * Also checks integrity of existing DB if pre-restore copies exist.
     *
     * Throws IllegalStateException with Arabic message if DB is corrupt and
     * no recoverable copy exists (safe recovery mode).
     */
    suspend fun recoverIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        val dir = dbDirectory(context)
        val preRestoreFiles = dir.listFiles { _, name -> name.startsWith(PRE_RESTORE_PREFIX) }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
        val restoringFiles = dir.listFiles { _, name -> name.startsWith(RESTORING_PREFIX) } ?: emptyArray()

        val mainDb = dbFile(context)

        // If main DB missing but pre-restore exists, restore from best pre-restore
        if (!mainDb.exists() && preRestoreFiles.isNotEmpty()) {
            val restored = preRestoreFiles.any { pre ->
                try {
                    pre.copyTo(mainDb, overwrite = true)
                    checkDatabaseIntegrity(mainDb)
                } catch (_: Exception) { false }
            }
            if (!restored) {
                throw IllegalStateException("قاعدة البيانات مفقودة ولا توجد نسخة احتياطية سليمة للاستعادة. يرجى استعادة نسخة احتياطية من الإعدادات أو التواصل مع الدعم.")
            }
        }

        // If main DB exists, check integrity when pre-restore copies exist
        if (mainDb.exists() && preRestoreFiles.isNotEmpty()) {
            val integrityOk = try { checkDatabaseIntegrity(mainDb) } catch (_: Exception) { false }
            if (!integrityOk) {
                val restored = preRestoreFiles.any { pre ->
                    try {
                        pre.copyTo(mainDb, overwrite = true)
                        checkDatabaseIntegrity(mainDb)
                    } catch (_: Exception) { false }
                }
                if (!restored) {
                    throw IllegalStateException("قاعدة البيانات تالفة ولا توجد نسخة احتياطية سليمة للاستعادة. يرجى استعادة نسخة احتياطية أو التواصل مع الدعم. لن يتم إنشاء قاعدة فارغة لحماية بياناتك.")
                }
            }
        }

        // Clean up stale restoring files
        restoringFiles.forEach { it.delete() }

        // Clean up old pre-restore copies (keep latest  defensible logic: clean up old pre-restore copies
        if (preRestoreFiles.size > 3) {
            val filesToDelete = preRestoreFiles.drop(3)
            filesToDelete.forEach { it.delete() }
        }

        // Pre-Room safety gate for v15: detect/export duplicate invoice and return numbers before migration/open.
        assertNoDuplicateNumbersBlockingV15Migration(context)
    }

    suspend fun checkpoint(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context).openHelper.writableDatabase
            db.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
        } catch (_: Exception) {
            // Ignore if DB not open
        }
    }

    suspend fun backup(
        context: Context,
        targetUri: Uri,
        contentResolver: android.content.ContentResolver
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            checkpoint(context)
            val src = dbFile(context)
            val tempUri = Uri.parse(targetUri.toString() + ".tmp")
            contentResolver.openOutputStream(tempUri)?.use { out ->
                FileInputStream(src).use { input -> input.copyTo(out) }
            } ?: return@withContext false
            contentResolver.openOutputStream(targetUri)?.use { out ->
                contentResolver.openInputStream(tempUri)?.use { input -> input.copyTo(out) }
            }
            contentResolver.delete(tempUri, null, null)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun validateBackup(
        context: Context,
        backupUri: Uri,
        contentResolver: android.content.ContentResolver
    ): BackupValidationResult = withContext(Dispatchers.IO) {
        val tmp = File(context.cacheDir, "backup_validate_${System.currentTimeMillis()}.db")
        return@withContext try {
            contentResolver.openInputStream(backupUri)?.use { input ->
                FileOutputStream(tmp).use { out -> input.copyTo(out) }
            } ?: return@withContext BackupValidationResult(false, "لا يمكن قراءة ملف النسخة الاحتياطية")
            val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                tmp.absolutePath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )
            val integrityCursor = db.rawQuery("PRAGMA integrity_check", null)
            val integrityOk = integrityCursor.moveToFirst() && integrityCursor.getString(0) == "ok"
            integrityCursor.close()
            if (!integrityOk) {
                db.close(); tmp.delete()
                return@withContext BackupValidationResult(false, "فشل فحص سلامة قاعدة البيانات (integrity check failed)")
            }

            val versionCursor = db.rawQuery("PRAGMA user_version", null)
            val schemaVersion = if (versionCursor.moveToFirst()) versionCursor.getInt(0) else 0
            versionCursor.close()
            if (schemaVersion < MIN_SCHEMA_VERSION || schemaVersion > MAX_SCHEMA_VERSION) {
                db.close(); tmp.delete()
                return@withContext BackupValidationResult(false, "إصدار مخطط قاعدة البيانات غير مدعوم (v$schemaVersion). الإصدار الحالي: v$MAX_SCHEMA_VERSION")
            }

            val requiredTables = mutableSetOf(
                "user_accounts", "categories", "brands", "products", "product_models",
                "sales_records", "supplier_accounts", "supplier_transactions",
                "merchant_accounts", "merchant_transactions"
            )
            if (schemaVersion >= 3) {
                requiredTables.add("customer_accounts")
                requiredTables.add("customer_transactions")
            }
            if (schemaVersion >= 4) {
                requiredTables.add("recycle_bin")
            }
            if (schemaVersion >= 6) {
                requiredTables.add("invoices")
                requiredTables.add("invoice_items")
            }
            if (schemaVersion >= 8) {
                requiredTables.add("return_records")
                requiredTables.add("return_items")
            }
            if (schemaVersion >= 9) {
                requiredTables.add("damage_repair_records")
            }
            if (schemaVersion >= 10) {
                requiredTables.add("stock_movements")
                requiredTables.add("inventory_audits")
                requiredTables.add("inventory_audit_items")
            }
            if (schemaVersion >= 11) {
                requiredTables.add("treasury_transactions")
                requiredTables.add("expense_categories")
                requiredTables.add("expenses")
                requiredTables.add("financial_adjustments")
            }

            val tablesCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
            val foundTables = mutableSetOf<String>()
            while (tablesCursor.moveToNext()) foundTables.add(tablesCursor.getString(0))
            tablesCursor.close()
            val missingTables = requiredTables - foundTables
            if (missingTables.isNotEmpty()) {
                db.close(); tmp.delete()
                return@withContext BackupValidationResult(false, "النسخة الاحتياطية ناقصة: جداول مفقودة (${missingTables.joinToString(", ")})")
            }

            db.close(); tmp.delete()
            BackupValidationResult(true, "النسخة الاحتياطية صالحة (مخطط v$schemaVersion)")
        } catch (e: Exception) {
            tmp.delete()
            BackupValidationResult(false, "خطأ في التحقق: ${e.message}")
        }
    }

    /**
     * Restores a validated backup with atomic file replacement and rollback capability.
     * Does NOT delete current DB until new DB is verified open and healthy.
     */
    suspend fun restore(
        context: Context,
        backupUri: Uri,
        contentResolver: android.content.ContentResolver
    ): Boolean = withContext(Dispatchers.IO) {
        val src = dbFile(context)
        val preRestoreCopy = File(src.parentFile, "${src.name}${PRE_RESTORE_PREFIX}${System.currentTimeMillis()}")
        val tempRestoreFile = File(src.parentFile, "${src.name}${RESTORING_PREFIX}${System.currentTimeMillis()}")

        // Block all new database operations during restore
        DatabaseLockManager.beginRestore()

        return@withContext try {
            val validation = validateBackup(context, backupUri, contentResolver)
            if (!validation.isValid) {
                DatabaseLockManager.endRestore(false)
                return@withContext false
            }

            if (src.exists()) src.copyTo(preRestoreCopy, overwrite = true)

            contentResolver.openInputStream(backupUri)?.use { input ->
                FileOutputStream(tempRestoreFile).use { out -> input.copyTo(out) }
            } ?: throw IllegalStateException("لا يمكن قراءة ملف النسخة الاحتياطية")

            try { FileOutputStream(tempRestoreFile, true).fd.sync() } catch (_: Exception) {}

            AppDatabase.closeDatabase()

            // Remove WAL/SHM for target to avoid stale journal against replaced main file
            try { File(src.absolutePath + "-wal").delete() } catch (_: Exception) {}
            try { File(src.absolutePath + "-shm").delete() } catch (_: Exception) {}

            if (!tempRestoreFile.renameTo(src)) {
                if (src.exists()) src.delete()
                tempRestoreFile.copyTo(src, overwrite = true)
                try { FileOutputStream(src, true).fd.sync() } catch (_: Exception) {}
            }

            AppDatabase.getDatabase(context)

            if (!checkDatabaseIntegrity(src)) {
                throw IllegalStateException("قاعدة البيانات المستعادة تالفة")
            }

            preRestoreCopy.delete()
            DatabaseLockManager.endRestore(true)
            true
        } catch (_: Exception) {
            try {
                AppDatabase.closeDatabase()
                if (preRestoreCopy.exists()) {
                    preRestoreCopy.copyTo(src, overwrite = true)
                    AppDatabase.getDatabase(context)
                }
            } catch (_: Exception) {
                // NEVER delete the last good copy - keep preRestoreCopy for manual recovery
            }
            DatabaseLockManager.endRestore(false)
            false
        } finally {
            tempRestoreFile.delete()
            // Clean up stale restoring files
            val restoringFiles = dbDirectory(context).listFiles { _, name -> name.startsWith(RESTORING_PREFIX) } ?: emptyArray()
            restoringFiles.forEach { it.delete() }
        }
    }

    data class BackupValidationResult(val isValid: Boolean, val message: String)
}
