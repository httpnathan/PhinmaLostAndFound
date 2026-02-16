package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuActivity : AppCompatActivity() {
    
    private lateinit var profileOption: LinearLayout
    private lateinit var settingsOption: LinearLayout
    private lateinit var aboutOption: LinearLayout
    private lateinit var reportOption: LinearLayout
    private lateinit var faqsOption: LinearLayout
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        
        initializeViews()
        setupBottomNavigation()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        profileOption = findViewById(R.id.profileOption)
        settingsOption = findViewById(R.id.settingsOption)
        aboutOption = findViewById(R.id.aboutOption)
        reportOption = findViewById(R.id.reportOption)
        faqsOption = findViewById(R.id.faqsOption)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_menu
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navigateToHome()
                    true
                }
                R.id.nav_search -> {
                    navigateToSearch()
                    true
                }
                R.id.nav_post -> {
                    navigateToPostItem()
                    true
                }
                R.id.nav_chat -> {
                    navigateToChatSection()
                    true
                }
                R.id.nav_menu -> {
                    // Already on menu
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupClickListeners() {
        profileOption.setOnClickListener {
            navigateToProfile()
        }
        
        settingsOption.setOnClickListener {
            navigateToSettings()
        }
        
        aboutOption.setOnClickListener {
            navigateToAbout()
        }
        
        reportOption.setOnClickListener {
            navigateToReport()
        }
        
        faqsOption.setOnClickListener {
            navigateToFAQs()
        }
    }
    
    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToAbout() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToReport() {
        val intent = Intent(this, ReportActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToFAQs() {
        val intent = Intent(this, FAQsActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToSearch() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToPostItem() {
        val intent = Intent(this, PostItemActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToChatSection() {
        val intent = Intent(this, ChatSectionActivity::class.java)
        startActivity(intent)
    }
}
