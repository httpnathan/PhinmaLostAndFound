package com.example.phinmalostandfound

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.phinmalostandfound.databinding.ActivityProfileBinding
import com.google.android.material.tabs.TabLayout
import org.json.JSONArray
import org.json.JSONObject

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var postAdapter: PostAdapter
    private var profileUserId: Int = -1
    private var isOwnProfile: Boolean = false
    private var allUserPosts: List<Post> = emptyList()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.profileImage.setImageURI(it)
            uploadProfilePicture(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileUserId = intent.getIntExtra("USER_ID", -1)
        val currentUserId = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).getInt("userId", -1)

        if (profileUserId == -1) profileUserId = currentUserId
        isOwnProfile = (profileUserId == currentUserId)

        setupUI()
        setupRecyclerView()
        setupBottomNavigation()
        setupTabListener()
        loadUserProfile()
        loadUserPosts()

        binding.bottomNavigation.elevation = 0f
        binding.scanFab.bringToFront()
    }

    private fun setupUI() {
        if (!isOwnProfile) {
            binding.ownProfileActions.visibility = View.GONE
            binding.changeProfilePicFab.visibility = View.GONE
        }

        binding.backButton.setOnClickListener { finish() }
        binding.editProfileButton.setOnClickListener { showEditUsernameDialog() }
        binding.changePasswordButton.setOnClickListener { showChangePasswordDialog() }
        binding.changeProfilePicFab.setOnClickListener { pickImageLauncher.launch("image/*") }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        binding.userPostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity)
            adapter = postAdapter
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = if (isOwnProfile) R.id.nav_menu else 0
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); true }
                R.id.nav_post -> { startActivity(Intent(this, PostItemActivity::class.java)); true }
                R.id.nav_chat -> { startActivity(Intent(this, ChatSectionActivity::class.java)); true }
                R.id.nav_menu -> { startActivity(Intent(this, MenuActivity::class.java)); true }
                else -> false
            }
        }
        binding.scanFab.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
    }

    private fun setupTabListener() {
        binding.profileTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                applyTabFilter(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun applyTabFilter(tabIndex: Int) {
        val filtered = when (tabIndex) {
            0 -> allUserPosts.filter { it.status == "active" }
            1 -> allUserPosts.filter { it.status == "resolved" }
            2 -> allUserPosts.filter { it.status == "claimed" }
            else -> allUserPosts
        }
        postAdapter.submitList(filtered)
    }

    private fun loadUserProfile() {
        if (isOwnProfile) {
            val prefs = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
            val firstName = prefs.getString("userFirstName", "") ?: ""
            val lastName = prefs.getString("userLastName", "") ?: ""
            binding.usernameTextView.text = "$firstName $lastName".trim()
            binding.emailTextView.text = prefs.getString("userEmail", "")
        } else {
            fetchOtherUserProfile()
        }
    }

    private fun fetchOtherUserProfile() {
        val url = ApiConfig.buildUrl(ApiConfig.GET_USER_PROFILE, "user_id" to profileUserId.toString())
        val request = StringRequest(Request.Method.GET, url, { response ->
            try {
                val jsonStart = response.indexOf("{")
                if (jsonStart != -1) {
                    val json = JSONObject(response.substring(jsonStart))
                    if (json.optBoolean("success")) {
                        val data = json.getJSONObject("data")
                        binding.usernameTextView.text = "${data.getString("first_name")} ${data.getString("last_name")}"
                        binding.emailTextView.text = data.getString("email")
                    }
                }
            } catch (e: Exception) {}
        }, {})
        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun loadUserPosts() {
        val url = ApiConfig.buildUrl(ApiConfig.GET_USER_POSTS, "user_id" to profileUserId.toString())
        val request = StringRequest(Request.Method.GET, url, { response ->
            try {
                val jsonStart = response.indexOf("{")
                if (jsonStart != -1) {
                    val json = JSONObject(response.substring(jsonStart))
                    if (json.optBoolean("success")) {
                        val postsJson = json.optJSONArray("posts")
                        val postsList = mutableListOf<Post>()

                        if (postsJson != null) {
                            for (i in 0 until postsJson.length()) {
                                val p = postsJson.getJSONObject(i)
                                postsList.add(Post(
                                    postId = p.optInt("post_id"),
                                    userId = p.optInt("user_id"),
                                    postType = p.optString("post_type"),
                                    itemName = p.optString("item_name"),
                                    description = p.optString("description"),
                                    category = p.optString("category"),
                                    locationFound = p.optString("location_found"),
                                    building = p.optString("building"),
                                    floorNumber = p.optString("floor_number").takeIf { it.isNotEmpty() },
                                    status = p.optString("status"),
                                    dateLostFound = p.optString("date_lost_found"),
                                    contactNumber = p.optString("contact_number").takeIf { it.isNotEmpty() },
                                    imageUrls = p.optJSONArray("image_urls")?.let { jsonArrayToList(it) } ?: emptyList(),
                                    createdAt = p.optString("created_at"),
                                    updatedAt = p.optString("updated_at"),
                                    postedBy = p.optString("posted_by")
                                ))
                            }
                        }

                        allUserPosts = postsList
                        binding.postsCountTextView.text = postsList.size.toString()
                        applyTabFilter(binding.profileTabs.selectedTabPosition)
                    }
                }
            } catch (e: Exception) {}
        }, {})
        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun jsonArrayToList(jsonArray: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }

    private fun showEditUsernameDialog() {
        val prefs = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
        val currentFirst = prefs.getString("userFirstName", "") ?: ""
        val currentLast = prefs.getString("userLastName", "") ?: ""

        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)

        val firstNameInput = EditText(this).apply {
            hint = "First Name"
            setText(currentFirst)
            setSingleLine()
        }
        val lastNameInput = EditText(this).apply {
            hint = "Last Name"
            setText(currentLast)
            setSingleLine()
        }

        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad / 2, pad, 0)
            addView(firstNameInput)
            addView(lastNameInput.also {
                (it.layoutParams as? android.widget.LinearLayout.LayoutParams)?.topMargin =
                    (8 * resources.displayMetrics.density).toInt()
            })
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Name")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newFirst = firstNameInput.text.toString().trim()
                val newLast = lastNameInput.text.toString().trim()
                if (newFirst.isEmpty()) {
                    Toast.makeText(this, "First name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                updateProfile(newFirst, newLast)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfile(firstName: String, lastName: String) {
        val userId = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).getInt("userId", -1)
        val request = object : StringRequest(Method.POST, ApiConfig.UPDATE_PROFILE,
            { response ->
                try {
                    val jsonStart = response.indexOf("{")
                    if (jsonStart != -1) {
                        val json = JSONObject(response.substring(jsonStart))
                        if (json.optBoolean("success")) {
                            getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).edit()
                                .putString("userFirstName", firstName)
                                .putString("userLastName", lastName)
                                .apply()
                            binding.usernameTextView.text = "$firstName $lastName".trim()
                            Toast.makeText(this, "Name updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, json.optString("message", "Update failed"), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                }
            },
            { Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): MutableMap<String, String> = mutableMapOf(
                "user_id" to userId.toString(),
                "first_name" to firstName,
                "last_name" to lastName
            )
        }
        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun showChangePasswordDialog() {
        val currentPwInput = EditText(this).apply {
            hint = "Current Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine()
        }
        val newPwInput = EditText(this).apply {
            hint = "New Password (min 6 chars)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine()
        }
        val confirmPwInput = EditText(this).apply {
            hint = "Confirm New Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine()
        }

        val dp8 = (8 * resources.displayMetrics.density).toInt()
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, dp8, pad, 0)
            addView(currentPwInput)
            addView(newPwInput.also { v ->
                val lp = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = dp8
                v.layoutParams = lp
            })
            addView(confirmPwInput.also { v ->
                val lp = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = dp8
                v.layoutParams = lp
            })
        }

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(container)
            .setPositiveButton("Update") { _, _ ->
                val current = currentPwInput.text.toString()
                val newPw = newPwInput.text.toString()
                val confirm = confirmPwInput.text.toString()

                when {
                    current.isEmpty() -> Toast.makeText(this, "Enter your current password", Toast.LENGTH_SHORT).show()
                    newPw.length < 6 -> Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    newPw != confirm -> Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    else -> changePassword(current, newPw)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changePassword(currentPw: String, newPw: String) {
        val userId = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).getInt("userId", -1)
        val request = object : StringRequest(Method.POST, ApiConfig.CHANGE_PASSWORD,
            { response ->
                try {
                    val jsonStart = response.indexOf("{")
                    if (jsonStart != -1) {
                        val json = JSONObject(response.substring(jsonStart))
                        if (json.optBoolean("success")) {
                            Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, json.optString("message", "Failed to change password"), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error changing password", Toast.LENGTH_SHORT).show()
                }
            },
            { Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): MutableMap<String, String> = mutableMapOf(
                "user_id" to userId.toString(),
                "current_password" to currentPw,
                "new_password" to newPw
            )
        }
        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun uploadProfilePicture(uri: Uri) {
        val userId = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).getInt("userId", -1)
        val imageBytes = ImageUtils.compressImage(this, uri, maxWidth = 512, maxHeight = 512, quality = 75)
        if (imageBytes == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

        val request = object : VolleyMultipartRequest(
            Method.POST, ApiConfig.UPDATE_PROFILE,
            Response.Listener<NetworkResponse> { response ->
                try {
                    val json = JSONObject(String(response.data))
                    if (json.optBoolean("success")) {
                        Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, json.optString("message", "Upload failed"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Network error during upload", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> = mutableMapOf(
                "user_id" to userId.toString()
            )

            override fun getByteData(): Map<String, VolleyMultipartRequest.DataPart> = mapOf(
                "profile_image" to VolleyMultipartRequest.DataPart("profile.jpg", imageBytes, "image/jpeg")
            )
        }

        AppSingleton.getRequestQueue(this).add(request)
    }
}
