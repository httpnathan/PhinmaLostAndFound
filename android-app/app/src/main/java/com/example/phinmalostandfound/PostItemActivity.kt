package com.example.phinmalostandfound

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.android.volley.Request
import com.android.volley.Response
import com.example.phinmalostandfound.databinding.ActivityPostItemBinding
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PostItemActivity : BaseActivity() {

    private lateinit var binding: ActivityPostItemBinding
    private var currentUserId: Int = -1
    private var currentFirstName: String = ""
    private var currentLastName: String = ""
    private var selectedDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private val DESC_MAX_CHARS = 500
    private val ITEM_NAME_MAX_CHARS = 80
    private val PREFS = "PhinmaLostAndFound"
    private val DRAFT_ITEM_NAME = "draft_item_name"
    private val DRAFT_DESCRIPTION = "draft_description"
    private val DRAFT_CONTACT = "draft_contact"
    private val DRAFT_DATE = "draft_date"
    private val DRAFT_POST_TYPE = "draft_post_type"
    private val DRAFT_CATEGORY = "draft_category"
    private val DRAFT_LOCATION = "draft_location"
    private val DRAFT_SECURITY_Q = "draft_security_question"

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.postImageView.setImageURI(uri)
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null) {
            selectedImageUri = cameraImageUri
            binding.postImageView.setImageURI(cameraImageUri)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserSession()
        setupSpinners()
        setupBottomNavigation()
        setupClickListeners()
        setupDescriptionCounter()
        setupItemNameCounter()
        setDefaultDate()
        restoreDraftIfExists()

        binding.bottomNavigation.elevation = 0f
        binding.scanFab.bringToFront()
    }

    private fun loadUserSession() {
        val sharedPreferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("userId", -1)
        currentFirstName = sharedPreferences.getString("userFirstName", "") ?: ""
        currentLastName = sharedPreferences.getString("userLastName", "") ?: ""

        if (currentUserId == -1) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            binding.postingAsTextView.text = "Posting as: $currentFirstName $currentLastName"
        }
    }

    private fun setDefaultDate() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        selectedDateStr = today
        binding.dateLostFoundEditText.setText(today)
    }

    private fun setupDescriptionCounter() {
        binding.descriptionCharCounter.text = "0/$DESC_MAX_CHARS"
        binding.descriptionEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val len = s?.length ?: 0
                binding.descriptionCharCounter.text = "$len/$DESC_MAX_CHARS"
                binding.descriptionCharCounter.setTextColor(when {
                    len > DESC_MAX_CHARS -> android.graphics.Color.RED
                    len >= (DESC_MAX_CHARS * 0.9).toInt() -> android.graphics.Color.parseColor("#FF6F00")
                    else -> getColor(R.color.text_secondary)
                })
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupItemNameCounter() {
        binding.itemNameCharCounter.text = "0/$ITEM_NAME_MAX_CHARS"
        binding.itemNameEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val len = s?.length ?: 0
                binding.itemNameCharCounter.text = "$len/$ITEM_NAME_MAX_CHARS"
                binding.itemNameCharCounter.setTextColor(when {
                    len >= ITEM_NAME_MAX_CHARS -> android.graphics.Color.RED
                    len >= (ITEM_NAME_MAX_CHARS * 0.9).toInt() -> android.graphics.Color.parseColor("#FF6F00")
                    else -> getColor(R.color.text_secondary)
                })
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupSpinners() {
        val categories = listOf("General", "Electronics", "Documents", "Bags & Accessories", "Jewelry", "Other")
        binding.categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        val locations = listOf("Main Building", "Library", "Cafeteria", "Student Plaza", "Gymnasium", "Engineering Lab", "Science Lab", "Other")
        binding.locationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)

        val postTypes = listOf("Lost", "Found")
        binding.postTypeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, postTypes)

        binding.postTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val type = postTypes[position]
                if (type == "Found") {
                    binding.securityQuestionLabel.visibility = View.VISIBLE
                    binding.securityQuestionEditText.visibility = View.VISIBLE
                } else {
                    binding.securityQuestionLabel.visibility = View.GONE
                    binding.securityQuestionEditText.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_post
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { navigateToHome(); true }
                R.id.nav_post -> true
                R.id.nav_chat -> { startActivity(Intent(this, ChatSectionActivity::class.java)); true }
                R.id.nav_menu -> { startActivity(Intent(this, MenuActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        binding.selectImageButton.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.cameraButton.setOnClickListener { openCamera() }
        binding.postButton.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            createPost()
        }
        binding.previewButton.setOnClickListener { showPreviewDialog() }
        binding.dateLostFoundEditText.setOnClickListener { showDatePicker() }
        binding.discardDraftButton.setOnClickListener { discardDraft() }
        binding.scanFab.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            launchCamera()
        }
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile("post_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        cameraImageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(cameraImageUri)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.dateLostFoundEditText.setText(selectedDateStr)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun showPreviewDialog() {
        val itemName = binding.itemNameEditText.text.toString().trim().ifEmpty { "(not set)" }
        val description = binding.descriptionEditText.text.toString().trim().ifEmpty { "(not set)" }
        val postType = binding.postTypeSpinner.selectedItem?.toString() ?: "-"
        val category = binding.categorySpinner.selectedItem?.toString() ?: "-"
        val location = binding.locationSpinner.selectedItem?.toString() ?: "-"
        val contact = binding.contactNumberEditText.text.toString().trim().ifEmpty { "Not provided" }
        val secQ = binding.securityQuestionEditText.text.toString().trim().ifEmpty { "None" }

        val preview = """
            Type: $postType
            Item: $itemName
            Category: $category
            Location: $location
            Date: $selectedDateStr
            Contact: $contact
            Security Question: $secQ

            Description:
            $description
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Post Preview")
            .setMessage(preview)
            .setPositiveButton("Looks Good") { d, _ -> d.dismiss() }
            .setNegativeButton("Edit") { d, _ -> d.dismiss() }
            .show()
    }

    private fun hasContent(): Boolean {
        val itemName = binding.itemNameEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val contact = binding.contactNumberEditText.text.toString().trim()
        return itemName.isNotEmpty() || description.isNotEmpty() || contact.isNotEmpty()
    }

    private fun saveDraft() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().apply {
            putString(DRAFT_ITEM_NAME, binding.itemNameEditText.text.toString())
            putString(DRAFT_DESCRIPTION, binding.descriptionEditText.text.toString())
            putString(DRAFT_CONTACT, binding.contactNumberEditText.text.toString())
            putString(DRAFT_DATE, selectedDateStr)
            putInt(DRAFT_POST_TYPE, binding.postTypeSpinner.selectedItemPosition)
            putInt(DRAFT_CATEGORY, binding.categorySpinner.selectedItemPosition)
            putInt(DRAFT_LOCATION, binding.locationSpinner.selectedItemPosition)
            putString(DRAFT_SECURITY_Q, binding.securityQuestionEditText.text.toString())
            apply()
        }
        Toast.makeText(this, "Draft saved", Toast.LENGTH_SHORT).show()
    }

    private fun restoreDraftIfExists() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val savedItemName = prefs.getString(DRAFT_ITEM_NAME, "") ?: ""
        if (savedItemName.isEmpty()) return

        binding.itemNameEditText.setText(savedItemName)
        binding.descriptionEditText.setText(prefs.getString(DRAFT_DESCRIPTION, ""))
        binding.contactNumberEditText.setText(prefs.getString(DRAFT_CONTACT, ""))

        val savedDate = prefs.getString(DRAFT_DATE, "") ?: ""
        if (savedDate.isNotEmpty()) {
            selectedDateStr = savedDate
            binding.dateLostFoundEditText.setText(savedDate)
        }

        binding.postTypeSpinner.setSelection(prefs.getInt(DRAFT_POST_TYPE, 0))
        binding.categorySpinner.setSelection(prefs.getInt(DRAFT_CATEGORY, 0))
        binding.locationSpinner.setSelection(prefs.getInt(DRAFT_LOCATION, 0))
        binding.securityQuestionEditText.setText(prefs.getString(DRAFT_SECURITY_Q, ""))

        binding.draftBanner.visibility = View.VISIBLE
    }

    private fun discardDraft() {
        clearDraft()
        binding.draftBanner.visibility = View.GONE
        binding.itemNameEditText.text?.clear()
        binding.descriptionEditText.text?.clear()
        binding.contactNumberEditText.text?.clear()
        binding.securityQuestionEditText.text?.clear()
        setDefaultDate()
        binding.postTypeSpinner.setSelection(0)
        binding.categorySpinner.setSelection(0)
        binding.locationSpinner.setSelection(0)
        selectedImageUri = null
        binding.postImageView.setImageResource(R.drawable.ic_image_placeholder)
    }

    private fun clearDraft() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().apply {
            remove(DRAFT_ITEM_NAME)
            remove(DRAFT_DESCRIPTION)
            remove(DRAFT_CONTACT)
            remove(DRAFT_DATE)
            remove(DRAFT_POST_TYPE)
            remove(DRAFT_CATEGORY)
            remove(DRAFT_LOCATION)
            remove(DRAFT_SECURITY_Q)
            apply()
        }
    }

    override fun onBackPressed() {
        if (hasContent()) {
            AlertDialog.Builder(this)
                .setTitle("Save Draft?")
                .setMessage("Do you want to save your progress as a draft?")
                .setPositiveButton("Save Draft") { _, _ -> saveDraft(); super.onBackPressed() }
                .setNegativeButton("Discard") { _, _ -> clearDraft(); super.onBackPressed() }
                .setNeutralButton("Cancel", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun createPost() {
        val itemName = binding.itemNameEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val contactNumber = binding.contactNumberEditText.text.toString().trim()
        val category = binding.categorySpinner.selectedItem.toString()
        val location = binding.locationSpinner.selectedItem.toString()
        val postType = binding.postTypeSpinner.selectedItem.toString().lowercase()
        val securityQuestion = binding.securityQuestionEditText.text.toString().trim()

        if (itemName.isEmpty()) {
            binding.itemNameEditText.error = "Please enter an item name"
            return
        }
        if (description.isEmpty()) {
            binding.descriptionEditText.error = "Please enter a description"
            return
        }
        if (postType == "found" && securityQuestion.isEmpty()) {
            binding.securityQuestionEditText.error = "Please enter a security question to verify the owner"
            return
        }

        binding.postButton.isEnabled = false

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Optimizing image and creating post...")
            setCancelable(false)
            show()
        }

        Thread {
            val imageBytes = selectedImageUri?.let { uri ->
                ImageUtils.compressImage(this, uri, maxWidth = 1080, maxHeight = 1080, quality = 80)
            }

            runOnUiThread {
                sendPostRequest(itemName, description, contactNumber, category, location, postType, securityQuestion, imageBytes, progressDialog)
            }
        }.start()
    }

    private fun sendPostRequest(
        itemName: String, description: String, contactNumber: String,
        category: String, location: String, postType: String, securityQuestion: String,
        imageBytes: ByteArray?, progressDialog: ProgressDialog
    ) {
        val userIdText = currentUserId.toString()
        val uploadUrl = ApiConfig.buildUrl(ApiConfig.CREATE_POST, "user_id" to userIdText)

        val request = object : VolleyMultipartRequest(
            Request.Method.POST,
            uploadUrl,
            Response.Listener { response ->
                progressDialog.dismiss()
                handleSuccess(response.data)
            },
            Response.ErrorListener { error ->
                progressDialog.dismiss()
                handleNetworkError(error)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_id" to userIdText,
                    "first_name" to currentFirstName,
                    "last_name" to currentLastName,
                    "post_type" to postType,
                    "item_name" to itemName,
                    "description" to description,
                    "category" to category,
                    "location_found" to location,
                    "building" to location,
                    "floor_number" to "1",
                    "contact_number" to contactNumber,
                    "date_lost_found" to selectedDateStr,
                    "security_question" to securityQuestion,
                    "status" to "active"
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
        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun handleSuccess(data: ByteArray) {
        try {
            val jsonResponse = JSONObject(String(data))
            if (jsonResponse.optBoolean("success", false)) {
                clearDraft()
                showSuccessAnimation()
            } else {
                binding.postButton.isEnabled = true
                showError("Server Error: ${jsonResponse.optString("message")}")
            }
        } catch (e: Exception) {
            binding.postButton.isEnabled = true
            showError("Unexpected response format from server")
        }
    }

    private fun showSuccessAnimation() {
        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        dialog.setContentView(R.layout.dialog_success)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val icon = dialog.findViewById<ImageView>(R.id.successIcon)
        dialog.show()

        val scaleAnim = ScaleAnimation(
            0f, 1f, 0f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 450
            interpolator = OvershootInterpolator(1.5f)
            fillAfter = true
        }
        icon.startAnimation(scaleAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                dialog.dismiss()
                navigateToHome()
            }
        }, 1800)
    }

    private fun handleNetworkError(error: com.android.volley.VolleyError) {
        binding.postButton.isEnabled = true
        val message = when {
            error.networkResponse == null -> "Connection failed. Please check your internet."
            error.networkResponse.statusCode == 413 -> "Image file size too large for server."
            else -> "Server error (${error.networkResponse.statusCode}). Please try again."
        }
        showError(message)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") { createPost() }
            .show()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val focused = currentFocus
            if (focused is EditText) {
                val rect = android.graphics.Rect()
                focused.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    focused.clearFocus()
                    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(focused.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

}
