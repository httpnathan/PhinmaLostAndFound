package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        initializeViews()
        setupTextWatchers()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpTextView = findViewById(R.id.signUpTextView)
        
        // Initial check to set button state
        updateLoginButtonState()
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)
    }

    private fun updateLoginButtonState() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        loginButton.isEnabled = email.isNotEmpty() && password.isNotEmpty()
    }
    
    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (password.length < 6) {
                passwordEditText.error = "Password must be at least 6 characters"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            performLogin(email, password)
        }
        
        signUpTextView.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun performLogin(email: String, password: String) {
        val url = "http://192.168.1.30/phinma-api/backend/auth.php?action=login" // Use PC IP if testing on a real device

        val jsonBody = JSONObject()
        jsonBody.put("action", "login")
        jsonBody.put("email", email)
        jsonBody.put("password", password)

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                val success = response.getBoolean("success")
                val message = response.getString("message")

                if (success) {
                    // Save login state
                    val sharedPreferences = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putBoolean("isLoggedIn", true)
                        putString("userEmail", email)
                        apply()
                    }

                    // Navigate to home
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }
    
    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
}
