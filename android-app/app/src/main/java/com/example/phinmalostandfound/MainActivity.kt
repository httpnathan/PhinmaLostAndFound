package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private val SPLASH_DELAY: Long = 2000 // 2 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Check if user is already logged in
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, SPLASH_DELAY)
    }
    
    private fun checkUserSession() {
        // TODO: Check if user is logged in using SharedPreferences or Firebase Auth
        val isLoggedIn = checkIfUserIsLoggedIn()
        
        if (isLoggedIn) {
            // User is logged in, go to Home
            navigateToHome()
        } else {
            // User is not logged in, go to Login
            navigateToLogin()
        }
    }
    
    private fun checkIfUserIsLoggedIn(): Boolean {
        // TODO: Implement actual login check
        // For now, always return false to show login screen
        val sharedPreferences = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }
    
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
