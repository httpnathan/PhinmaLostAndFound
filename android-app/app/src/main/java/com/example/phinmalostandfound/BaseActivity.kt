package com.example.phinmalostandfound

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.work.WorkManager

/**
 * Base activity that provides:
 * - Session validation (redirects to login if not logged in)
 * - Auto-logout after 15 minutes of inactivity
 * - Biometric lock when returning from background (if enabled in settings)
 */
abstract class BaseActivity : AppCompatActivity() {

    private val inactivityHandler = Handler(Looper.getMainLooper())
    private val inactivityTimeoutMs = 15 * 60 * 1000L // 15 minutes

    private val inactivityRunnable = Runnable {
        WorkManager.getInstance(applicationContext).cancelUniqueWork("NotificationPolling")
        getSharedPreferences(PREFS_USER, MODE_PRIVATE).edit().clear().apply()
        Toast.makeText(this, "Logged out due to inactivity", Toast.LENGTH_LONG).show()
        redirectToLogin()
    }

    companion object {
        private const val PREFS_USER = "PhinmaLostAndFound"
        private const val PREFS_SETTINGS = "PhinmaSettings"
        private const val BACKGROUND_LOCK_THRESHOLD_MS = 2 * 60 * 1000L // 2 minutes

        /** App-level timestamp: set when any BaseActivity goes to onPause */
        var lastPausedAt = 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkSession()
    }

    override fun onResume() {
        super.onResume()
        checkSession()

        val prefs = getSharedPreferences(PREFS_USER, MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        if (isLoggedIn && lastPausedAt > 0) {
            val awayMs = System.currentTimeMillis() - lastPausedAt
            val biometricEnabled = getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE)
                .getBoolean("biometricLock", false)
            if (biometricEnabled && awayMs > BACKGROUND_LOCK_THRESHOLD_MS) {
                showBiometricPrompt()
            }
        }
        lastPausedAt = 0L

        resetInactivityTimer()
    }

    override fun onPause() {
        super.onPause()
        lastPausedAt = System.currentTimeMillis()
        inactivityHandler.removeCallbacks(inactivityRunnable)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetInactivityTimer()
    }

    private fun checkSession() {
        val isLoggedIn = getSharedPreferences(PREFS_USER, MODE_PRIVATE)
            .getBoolean("isLoggedIn", false)
        if (!isLoggedIn) redirectToLogin()
    }

    private fun resetInactivityTimer() {
        inactivityHandler.removeCallbacks(inactivityRunnable)
        val isLoggedIn = getSharedPreferences(PREFS_USER, MODE_PRIVATE)
            .getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            inactivityHandler.postDelayed(inactivityRunnable, inactivityTimeoutMs)
        }
    }

    fun redirectToLogin() {
        inactivityHandler.removeCallbacks(inactivityRunnable)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) return

        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // Access granted, continue normally
            }
            override fun onAuthenticationFailed() {
                // Fingerprint not recognised — dialog stays open
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED
                ) {
                    // User tapped "Log Out" or cancelled — sign out for security
                    WorkManager.getInstance(applicationContext).cancelUniqueWork("NotificationPolling")
                    getSharedPreferences(PREFS_USER, MODE_PRIVATE).edit().clear().apply()
                    redirectToLogin()
                }
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify your identity")
            .setSubtitle("PHINMA Lost & Found")
            .setNegativeButtonText("Log Out")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        BiometricPrompt(this, executor, callback).authenticate(promptInfo)
    }
}
