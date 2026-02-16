package com.example.phinmalostandfound

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.ByteArrayOutputStream

class PostItemActivity : AppCompatActivity() {

    private lateinit var postImageView: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var postButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var locationSpinner: Spinner
    private lateinit var bottomNavigation: BottomNavigationView

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    // 🔥 CHANGE THIS TO YOUR SERVER IP
    private val uploadUrl = "http://10.36.42.34/phinma-api/backend/posts.php?action=create"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_item)

        initializeViews()
        setupBottomNavigation()
        setupClickListeners()
        setupSpinners()
    }

    private fun initializeViews() {
        postImageView = findViewById(R.id.postImageView)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        postButton = findViewById(R.id.postButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        categorySpinner = findViewById(R.id.categorySpinner)
        locationSpinner = findViewById(R.id.locationSpinner)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupSpinners() {
        // Example categories
        val categories = listOf("General", "Electronics", "Documents", "Bags & Accessories")
        categorySpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        // Example locations
        val locations = listOf("Main Campus", "Library", "Cafeteria", "Admin Building")
        locationSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)
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

        if (selectedImageUri == null || description.isEmpty()) {
            Toast.makeText(this, "Please select an image and enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading...")
        progressDialog.show()

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        val request = object : VolleyMultipartRequest(
            Request.Method.POST,
            uploadUrl,
            Response.Listener { response ->
                progressDialog.dismiss()
                Toast.makeText(this, "Post uploaded successfully!", Toast.LENGTH_LONG).show()
                navigateToHome()
            },
            Response.ErrorListener { error ->
                progressDialog.dismiss()
                Toast.makeText(this, "Upload failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_id" to "1", // Replace with actual logged-in user ID
                    "post_type" to "lost", // You can make a spinner for lost/found
                    "description" to description,
                    "category" to category,
                    "location_found" to location,
                    "building" to "Admin", // Replace or make another spinner
                    "floor_number" to "1",
                    "date_lost_found" to "2026-02-16 12:00:00" // Could use date picker
                )
            }

            override fun getByteData(): MutableMap<String, DataPart> {
                return hashMapOf(
                    "image" to DataPart(
                        "image_${System.currentTimeMillis()}.jpg",
                        imageBytes,
                        "image/jpeg"
                    )
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
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