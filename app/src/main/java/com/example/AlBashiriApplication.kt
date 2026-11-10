package com.example

import android.app.Application

class AlBashiriApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Startup recovery is executed sequentially by the unified boot gate
        // (BackupManager.ensureRecoveryCompleted) before any Room/ViewModel creation.
        // Do not trigger recovery here to avoid duplicate/concurrent execution.
    }
}
