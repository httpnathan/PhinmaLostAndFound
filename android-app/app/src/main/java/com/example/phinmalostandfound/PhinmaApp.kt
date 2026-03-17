package com.example.phinmalostandfound

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class PhinmaApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences("PhinmaSettings", MODE_PRIVATE)
        val darkMode = prefs.getBoolean("darkMode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        NotificationHelper.createChannels(this)

        val pollingWork = PeriodicWorkRequestBuilder<MessagePollingWorker>(
            15, TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "message_polling",
            ExistingPeriodicWorkPolicy.KEEP,
            pollingWork
        )
    }
}
