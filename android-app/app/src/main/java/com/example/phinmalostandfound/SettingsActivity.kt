package com.example.phinmalostandfound

import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var notificationsSwitch: SwitchCompat
    private lateinit var darkModeSwitch: SwitchCompat
    private lateinit var locationSwitch: SwitchCompat
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        initializeViews()
        loadSettings()
        setupSwitchListeners()
    }
    
    private fun initializeViews() {
        notificationsSwitch = findViewById(R.id.notificationsSwitch)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        locationSwitch = findViewById(R.id.locationSwitch)
    }
    
    private fun loadSettings() {
        // TODO: Load settings from SharedPreferences
    }
    
    private fun setupSwitchListeners() {
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Save notification preference
        }
        
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Toggle dark mode
        }
        
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Toggle location services
        }
    }
}
