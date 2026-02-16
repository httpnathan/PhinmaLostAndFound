package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        
        initializeViews()
        setupTextWatchers()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        loginTextView = findViewById(R.id.loginTextView)
        
        updateSignUpButtonState()
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSignUpButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        firstNameEditText.addTextChangedListener(textWatcher)
        lastNameEditText.addTextChangedListener(textWatcher)
        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)
        confirmPasswordEditText.addTextChangedListener(textWatcher)
    }

    private fun updateSignUpButtonState() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        signUpButton.isEnabled = firstName.isNotEmpty() && 
                                 lastName.isNotEmpty() && 
                                 email.isNotEmpty() && 
                                 password.isNotEmpty() && 
                                 confirmPassword.isNotEmpty()
    }
    
    private fun setupClickListeners() {
        signUpButton.setOnClickListener {
            performSignUp()
        }
        
        loginTextView.setOnClickListener {
            navigateToLogin()
        }
    }
    
    private fun performSignUp() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email"
            emailEditText.requestFocus()
            return
        }
        
        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            passwordEditText.requestFocus()
            return
        }

        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            confirmPasswordEditText.requestFocus()
            return
        }

        val url = "http://192.168.1.30/phinma-api/backend/auth.php?action=register"

        val jsonObject = JSONObject().apply {
            put("first_name", firstName)
            put("last_name", lastName)
            put("email", email)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonObject,
            { response ->
                val success = response.getBoolean("success")
                val message = response.getString("message")
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                if (success) {
                    val sharedPreferences = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putBoolean("isLoggedIn", true)
                        putString("userEmail", email)
                        putString("userFirstName", firstName)
                        putString("userLastName", lastName)
                        apply()
                    }

                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            },
            { error ->
                val data = error.networkResponse?.data?.let { String(it) } ?: "No response from server"
                Toast.makeText(this, "Signup failed: $data", Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
