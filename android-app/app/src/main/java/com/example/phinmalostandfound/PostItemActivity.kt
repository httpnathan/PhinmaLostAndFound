package com.example.phinmalostandfound

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PostItemActivity : AppCompatActivity() {

    private lateinit var postImageView: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var postButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var locationSpinner: Spinner
    private lateinit var postTypeSpinner: Spinner
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var postingAsTextView: TextView

    // User data read directly from SharedPreferences — no hidden EditText needed
    private var currentUserId: Int = -1
    private var currentFirstName: String = ""
    private var currentLastName: String = ""

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_item)

        initializeViews()
        loadUserSession()
        setupSpinners()
        setupBottomNavigation()
        setupClickListeners()
    }

    private fun loadUserSession() {
        val sharedPreferences = getSharedPreferences("PhinmaLostAndFound", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("userId", -1)
        currentFirstName = sharedPreferences.getString("userFirstName", "") ?: ""
        currentLastName = sharedPreferences.getString("userLastName", "") ?: ""

        if (currentUserId != -1) {
            postingAsTextView.text = "Posting as: $currentFirstName $currentLastName"
        } else {
            // No valid session, redirect to login
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun initializeViews() {
        postImageView = findViewById(R.id.postImageView)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        postButton = findViewById(R.id.postButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        categorySpinner = findViewById(R.id.categorySpinner)
        locationSpinner = findViewById(R.id.locationSpinner)
        postTypeSpinner = findViewById(R.id.postTypeSpinner)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        postingAsTextView = findViewById(R.id.postingAsTextView)
    }

    private fun setupSpinners() {
        val categories = listOf("General", "Electronics", "Documents", "Bags & Accessories", "Jewelry", "Other")
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        val locations = listOf("Main Building", "Library", "Cafeteria", "Student Plaza", "Gymnasium", "Engineering Lab", "Science Lab", "Other")
        locationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)

        val postTypes = listOf("Lost", "Found")
        postTypeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, postTypes)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_post
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { navigateToHome(); true }
                R.id.nav_search -> { navigateToSearch(); true }
                R.id.nav_post -> true
                R.id.nav_chat -> { navigateToChatSection(); true }
                R.id.nav_menu -> { navigateToMenu(); true }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        selectImageButton.setOnClickListener { openImagePicker() }
        postButton.setOnClickListener { createPost() }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            postImageView.setImageURI(selectedImageUri)
        }
    }

    private fun createPost() {
        val description = descriptionEditText.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()
        val location = locationSpinner.selectedItem.toString()
        val postType = postTypeSpinner.selectedItem.toString().lowercase()

        // Always read fresh from SharedPreferences — guarantees user_id is valid
        val sharedPreferences = getSharedPreferences("PhinmaLostAndFound", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)
        val firstName = sharedPreferences.getString("userFirstName", "") ?: ""
        val lastName = sharedPreferences.getString("userLastName", "") ?: ""
        val userIdText = userId.toString()

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == -1) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Creating post...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        var imageBytes: ByteArray? = null
        selectedImageUri?.let { uri ->
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
                imageBytes = byteArrayOutputStream.toByteArray()
            } catch (e: Exception) {
                Log.e("PostItemActivity", "Image conversion failed", e)
            }
        }

        // Add user_id to the URL to ensure it's captured by the backend
        val uploadUrl = ApiConfig.buildUrl(ApiConfig.CREATE_POST, "user_id" to userIdText)

        val request = object : VolleyMultipartRequest(
            Request.Method.POST,
            uploadUrl,
            Response.Listener { response ->
                progressDialog.dismiss()
                try {
                    val responseString = String(response.data)
                    val jsonResponse = JSONObject(responseString)
                    val success = jsonResponse.optBoolean("success", false)
                    val message = jsonResponse.optString("message", "Unknown error")

                    if (success) {
                        Toast.makeText(this, "Post Created Successfully!", Toast.LENGTH_LONG).show()
                        navigateToHome()
                    } else {
                        Toast.makeText(this, "Server Error: $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    val responseBody = String(response.data)
                    Log.e("PostItemActivity", "Response Error: $responseBody", e)
                    Toast.makeText(this, "Unexpected response format", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                progressDialog.dismiss()
                val responseBody = error.networkResponse?.data?.let { String(it) } ?: ""
                Log.e("PostItemActivity", "Upload failed: $responseBody", error)
                Toast.makeText(this, "Connection Error. Check internet/IP.", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_id" to userIdText,
                    "first_name" to firstName,
                    "last_name" to lastName,
                    "post_type" to postType,
                    "item_name" to if (description.length > 20) description.substring(0, 20) else description,
                    "description" to description,
                    "category" to category,
                    "location_found" to location,
                    "building" to location,
                    "floor_number" to "1",
                    "date_lost_found" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                )
            }

            override fun getByteData(): Map<String, DataPart> {
                val params = HashMap<String, DataPart>()
                imageBytes?.let {
                    params["image"] = DataPart("post_${System.currentTimeMillis()}.jpg", it, "image/jpeg")
                }
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun navigateToSearch() {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    private fun navigateToChatSection() {
        startActivity(Intent(this, ChatSectionActivity::class.java))
    }

    private fun navigateToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
    }
}