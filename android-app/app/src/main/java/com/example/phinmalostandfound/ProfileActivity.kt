package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var postsCountTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var signOutButton: Button
    private lateinit var settingsIcon: ImageView
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
        postsCountTextView = findViewById(R.id.postsCountTextView)
        editProfileButton = findViewById(R.id.editProfileButton)
        signOutButton = findViewById(R.id.signOutButton)
        settingsIcon = findViewById(R.id.settingsIcon)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_menu
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { navigateToHome(); true }
                R.id.nav_search -> { navigateToSearch(); true }
                R.id.nav_post -> { navigateToPostItem(); true }
                R.id.nav_chat -> { navigateToChatSection(); true }
                R.id.nav_menu -> true
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        editProfileButton.setOnClickListener {
            Toast.makeText(this, "Edit Profile coming soon", Toast.LENGTH_SHORT).show()
        }

        settingsIcon.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        signOutButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ -> performSignOut() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadUserProfile() {
        val prefs = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
        val firstName = prefs.getString("userFirstName", "") ?: ""
        val lastName = prefs.getString("userLastName", "") ?: ""
        val email = prefs.getString("userEmail", "") ?: ""
        val userId = prefs.getInt("userId", -1)

        usernameTextView.text = "$firstName $lastName".trim()
        emailTextView.text = email

        if (userId != -1) {
            loadPostCount(userId.toString())
        }
    }

    private fun loadPostCount(userId: String) {
        val url = ApiConfig.buildUrl(ApiConfig.GET_USER_POSTS, "user_id" to userId)

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonStart = response.indexOf("{")
                    if (jsonStart == -1) return@StringRequest
                    val json = JSONObject(response.substring(jsonStart))
                    if (json.optBoolean("success", false)) {
                        val count = json.optJSONArray("posts")?.length() ?: 0
                        postsCountTextView.text = count.toString()
                    }
                } catch (e: Exception) { /* ignore */ }
            },
            { /* ignore */ }
        )

        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun performSignOut() {
        getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() { startActivity(Intent(this, HomeActivity::class.java)) }
    private fun navigateToSearch() { startActivity(Intent(this, SearchActivity::class.java)) }
    private fun navigateToPostItem() { startActivity(Intent(this, PostItemActivity::class.java)) }
    private fun navigateToChatSection() { startActivity(Intent(this, ChatSectionActivity::class.java)) }
}
