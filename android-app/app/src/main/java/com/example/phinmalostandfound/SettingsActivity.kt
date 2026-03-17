package com.example.phinmalostandfound

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.biometric.BiometricManager

class SettingsActivity : BaseActivity() {

    private lateinit var notificationsSwitch: SwitchCompat
    private lateinit var darkModeSwitch: SwitchCompat
    private lateinit var locationSwitch: SwitchCompat
    private lateinit var notifyMessagesSwitch: SwitchCompat
    private lateinit var notifyMatchesSwitch: SwitchCompat
    private lateinit var notifyStatusSwitch: SwitchCompat
    private lateinit var biometricLockSwitch: SwitchCompat
    private lateinit var clearCacheRow: LinearLayout
    private lateinit var clearRecentSearchesRow: LinearLayout
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()
        loadSettings()
        setupListeners()
    }

    private fun initializeViews() {
        notificationsSwitch = findViewById(R.id.notificationsSwitch)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        locationSwitch = findViewById(R.id.locationSwitch)
        notifyMessagesSwitch = findViewById(R.id.notifyMessagesSwitch)
        notifyMatchesSwitch = findViewById(R.id.notifyMatchesSwitch)
        notifyStatusSwitch = findViewById(R.id.notifyStatusSwitch)
        biometricLockSwitch = findViewById(R.id.biometricLockSwitch)
        clearCacheRow = findViewById(R.id.clearCacheRow)
        clearRecentSearchesRow = findViewById(R.id.clearRecentSearchesRow)
        backButton = findViewById(R.id.backButton)
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("PhinmaSettings", MODE_PRIVATE)
        notificationsSwitch.isChecked = prefs.getBoolean("notifications", true)
        darkModeSwitch.isChecked = prefs.getBoolean("darkMode", false)
        locationSwitch.isChecked = prefs.getBoolean("location", false)
        notifyMessagesSwitch.isChecked = prefs.getBoolean("notifyMessages", true)
        notifyMatchesSwitch.isChecked = prefs.getBoolean("notifyMatches", true)
        notifyStatusSwitch.isChecked = prefs.getBoolean("notifyStatus", true)
        biometricLockSwitch.isChecked = prefs.getBoolean("biometricLock", false)

        // Disable biometric toggle if hardware not available
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        biometricLockSwitch.isEnabled = canAuth == BiometricManager.BIOMETRIC_SUCCESS ||
                canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    private fun setupListeners() {
        val prefs = getSharedPreferences("PhinmaSettings", MODE_PRIVATE)

        backButton.setOnClickListener { finish() }

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications", isChecked).apply()
            notifyMessagesSwitch.isEnabled = isChecked
            notifyMatchesSwitch.isEnabled = isChecked
            notifyStatusSwitch.isEnabled = isChecked
            Toast.makeText(this, if (isChecked) "Notifications enabled" else "Notifications disabled", Toast.LENGTH_SHORT).show()
        }

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("darkMode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("location", isChecked).apply()
            Toast.makeText(this, if (isChecked) "Location enabled" else "Location disabled", Toast.LENGTH_SHORT).show()
        }

        notifyMessagesSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifyMessages", isChecked).apply()
        }

        notifyMatchesSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifyMatches", isChecked).apply()
        }

        notifyStatusSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifyStatus", isChecked).apply()
        }

        biometricLockSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("biometricLock", isChecked).apply()
            Toast.makeText(this,
                if (isChecked) "Biometric lock enabled" else "Biometric lock disabled",
                Toast.LENGTH_SHORT).show()
        }

        clearCacheRow.setOnClickListener {
            try {
                cacheDir.deleteRecursively()
                Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error clearing cache", Toast.LENGTH_SHORT).show()
            }
        }

        clearRecentSearchesRow.setOnClickListener {
            getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
                .edit().remove("recentSearches").apply()
            Toast.makeText(this, "Recent searches cleared", Toast.LENGTH_SHORT).show()
        }
    }
}
