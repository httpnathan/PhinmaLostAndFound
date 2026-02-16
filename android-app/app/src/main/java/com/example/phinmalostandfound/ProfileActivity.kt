package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var profileImage: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var signOutButton: Button
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        initializeViews()
        setupBottomNavigation()
        setupClickListeners()
        loadUserProfile()
    }
    
    private fun initializeViews() {
        profileImage = findViewById(R.id.profileImage)
        usernameTextView = findViewById(R.id.usernameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        editProfileButton = findViewById(R.id.editProfileButton)
        signOutButton = findViewById(R.id.signOutButton)
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
                    // Already on profile/menu
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupClickListeners() {
        editProfileButton.setOnClickListener {
            // TODO: Navigate to edit profile screen
        }
        
        signOutButton.setOnClickListener {
            performSignOut()
        }
    }
    
    private fun loadUserProfile() {
        // TODO: Load user data from database/preferences
        // usernameTextView.text = "Username"
        // emailTextView.text = "user@example.com"
    }
    
    private fun performSignOut() {
        // Clear user session
        val sharedPreferences = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
