package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView
    private lateinit var rememberMeCheckBox: CheckBox

    private val PREFS = "PhinmaLostAndFound"
    private val KEY_SAVED_EMAIL = "savedEmail"

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
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox)

        // Pre-fill saved email if available
        val savedEmail = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_SAVED_EMAIL, "") ?: ""
        if (savedEmail.isNotEmpty()) {
            emailEditText.setText(savedEmail)
            rememberMeCheckBox.isChecked = true
        }

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
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun performLogin(email: String, password: String) {
        loginButton.isEnabled = false
        loginButton.text = "Signing in..."

        val jsonBody = JSONObject().apply {
            put("action", "login")
            put("email", email)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, ApiConfig.LOGIN, jsonBody,
            { response ->
                val success = response.getBoolean("success")
                val message = response.getString("message")

                if (success) {
                    val data = response.getJSONObject("data")
                    val userId = data.getInt("user_id")
                    val firstName = data.getString("first_name")
                    val lastName = data.getString("last_name")

                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().apply {
                        putBoolean("isLoggedIn", true)
                        putInt("userId", userId)
                        putString("userEmail", email)
                        putString("userFirstName", firstName)
                        putString("userLastName", lastName)
                        // Remember Me: save/clear email
                        if (rememberMeCheckBox.isChecked) {
                            putString(KEY_SAVED_EMAIL, email)
                        } else {
                            remove(KEY_SAVED_EMAIL)
                        }
                        apply()
                    }

                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    loginButton.isEnabled = true
                    loginButton.text = "Login"
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                loginButton.isEnabled = true
                loginButton.text = "Login"
                Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        AppSingleton.getRequestQueue(this).add(request)
    }
}
