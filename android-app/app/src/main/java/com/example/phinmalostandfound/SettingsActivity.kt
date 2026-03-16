package com.example.phinmalostandfound

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var notificationsSwitch: SwitchCompat
    private lateinit var darkModeSwitch: SwitchCompat
    private lateinit var locationSwitch: SwitchCompat
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
        backButton = findViewById(R.id.backButton)
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("PhinmaSettings", MODE_PRIVATE)
        // Temporarily disable listeners so loading saved values doesn't trigger saves
        notificationsSwitch.isChecked = prefs.getBoolean("notifications", true)
        darkModeSwitch.isChecked = prefs.getBoolean("darkMode", false)
        locationSwitch.isChecked = prefs.getBoolean("location", false)
    }

    private fun setupListeners() {
        val prefs = getSharedPreferences("PhinmaSettings", MODE_PRIVATE)

        backButton.setOnClickListener { finish() }

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications", isChecked).apply()
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
    }
}
