package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.work.WorkManager
import com.example.phinmalostandfound.databinding.ActivityMenuBinding

class MenuActivity : BaseActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserInfo()
        setupBottomNavigation()
        setupClickListeners()
        binding.scanFab.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

        binding.bottomNavigation.elevation = 0f
        binding.scanFab.bringToFront()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun loadUserInfo() {
        val prefs = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
        val firstName = prefs.getString("userFirstName", "") ?: ""
        val lastName = prefs.getString("userLastName", "") ?: ""
        val fullName = "$firstName $lastName".trim()
        binding.userNameTextView.text = if (fullName.isNotEmpty()) fullName else "Welcome"
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_menu
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { navigateToHome(); true }
                R.id.nav_post -> { navigateToPostItem(); true }
                R.id.nav_chat -> { navigateToChatSection(); true }
                R.id.nav_menu -> true
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        // Header actions
        binding.settingsGearIcon.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Quick action cards
        binding.myChatsCard.setOnClickListener { navigateToChatSection() }
        binding.settingsQuickCard.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Account section items
        binding.profileOption.setOnClickListener { navigateToProfile() }
        binding.myPostsOption.setOnClickListener { navigateToProfile() }
        binding.notificationsOption.setOnClickListener {
            NotificationHelper.markAllRead(this)
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        binding.settingsOption.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Support items
        binding.aboutOption.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        binding.reportOption.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
        binding.faqsOption.setOnClickListener {
            startActivity(Intent(this, FAQsActivity::class.java))
        }
        binding.logoutButton.setOnClickListener { performLogout() }
    }

    private fun navigateToProfile() {
        val prefs = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("USER_ID", prefs.getInt("userId", -1))
        startActivity(intent)
    }

    private fun performLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                WorkManager.getInstance(applicationContext).cancelUniqueWork("NotificationPolling")
                getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToHome() { startActivity(Intent(this, HomeActivity::class.java)) }
    private fun navigateToPostItem() { startActivity(Intent(this, PostItemActivity::class.java)) }
    private fun navigateToChatSection() { startActivity(Intent(this, ChatSectionActivity::class.java)) }
}
