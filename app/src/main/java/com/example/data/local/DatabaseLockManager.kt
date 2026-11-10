package com.example.data.local

import androidx.lifecycle.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Centralized Database Lock Manager
 *
 * Provides a comprehensive locking mechanism that:
 * 1. Prevents all new DB operations during restore
 * 2. Waits for in-flight operations to complete before allowing restore
 * 3. Covers all access paths: DAO, Repository, ViewModel, Flow collections
 *
 * Uses an atomic counter and channel for synchronization.
 */
object DatabaseLockManager {

    private val isRestoreInProgress = AtomicBoolean(false)
    private val activeOperationCount = AtomicInteger(0)
    private val operationCompleteChannel = Channel<Unit>(Channel.UNLIMITED)

    /**
     * Starts a database operation. Returns a token that must be completed when the operation finishes.
     * Throws if restore is in progress.
     */
    fun startOperation(): OperationToken {
        if (isRestoreInProgress.get()) {
            throw IllegalStateException("قاعدة البيانات قيد الاستعادة، لا يمكن تنفيذ العمليات حالياً")
        }
        activeOperationCount.incrementAndGet()
        // Double-check after incrementing
        if (isRestoreInProgress.get()) {
            activeOperationCount.decrementAndGet()
            throw IllegalStateException("قاعدة البيانات قيد الاستعادة، لا يمكن تنفيذ العمليات حالياً")
        }
        return OperationToken()
    }

    private fun completeOperation() {
        val remaining = activeOperationCount.decrementAndGet()
        if (remaining <= 0 && isRestoreInProgress.get()) {
            operationCompleteChannel.trySend(Unit)
        }
    }

    /**
     * Starts a suspend operation with automatic cleanup.
     * Throws if restore is in progress.
     */
    suspend fun <T> withDbLock(block: suspend () -> T): T {
        val token = startOperation()
        try {
            return withContext(Dispatchers.IO) {
                block()
            }
        } finally {
            token.complete()
        }
    }

    /**
     * Called by Flow collectors to check if they should cancel.
     */
    fun checkNotRestoring() {
        if (isRestoreInProgress.get()) {
            throw IllegalStateException("قاعدة البيانات قيد الاستعادة، لا يمكن تنفيذ العمليات حالياً")
        }
    }

    /**
     * Checks if a restore is currently in progress.
     */
    fun isRestoring(): Boolean = isRestoreInProgress.get()

    /**
     * Initiates a database restore. Blocks until all in-flight operations complete.
     * Must be called from a non-UI blocking context (IO dispatcher).
     */
    suspend fun beginRestore(): CompletableDeferred<Boolean> {
        if (!isRestoreInProgress.compareAndSet(false, true)) {
            throw IllegalStateException("استعادة قيد التنفيذ بالفعل")
        }
        val deferred = CompletableDeferred<Boolean>()
        // Wait for all in-flight operations to complete
        while (activeOperationCount.get() > 0) {
            operationCompleteChannel.receive()
        }
        return deferred
    }

    /**
     * Called when restore completes (success or failure).
     */
    fun endRestore(success: Boolean) {
        isRestoreInProgress.set(false)
    }

    /**
     * Resets the lock manager (for testing or emergency recovery).
     */
    fun reset() {
        isRestoreInProgress.set(false)
        activeOperationCount.set(0)
    }

    /**
     * Token representing an active database operation.
     */
    class OperationToken {
        private var completed = false
        fun complete() {
            if (!completed) {
                completed = true
                DatabaseLockManager.completeOperation()
            }
        }
    }
}
