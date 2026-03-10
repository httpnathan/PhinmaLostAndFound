package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {
    
    private lateinit var notificationIcon: ImageView
    private lateinit var searchCardView: CardView
    private lateinit var filterButton: MaterialButton
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    
    private lateinit var postAdapter: PostAdapter
    private var allPosts: List<Post> = listOf()
    private var currentFilter = "All" // All, Lost, Found
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        initializeViews()
        setupRecyclerView()
        loadPosts()
        setupBottomNavigation()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        notificationIcon = findViewById(R.id.notificationIcon)
        searchCardView = findViewById(R.id.searchCardView)
        filterButton = findViewById(R.id.filterButton)
        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }
    
    private fun loadPosts() {
        val url = ApiConfig.GET_ALL_POSTS
        
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                Log.d("HomeActivity", "Raw Response: $response")
                try {
                    // Try to find the start of the JSON object
                    val jsonStartIndex = response.indexOf("{")
                    if (jsonStartIndex == -1) {
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_LONG).show()
                        return@StringRequest
                    }
                    
                    val jsonResponse = JSONObject(response.substring(jsonStartIndex))
                    val success = jsonResponse.getBoolean("success")
                    
                    if (success) {
                        val postsJson = jsonResponse.getJSONArray("posts")
                        val postsList = mutableListOf<Post>()
                        
                        for (i in 0 until postsJson.length()) {
                            val postObj = postsJson.getJSONObject(i)
                            val post = Post(
                                postId = postObj.getInt("post_id"),
                                userId = postObj.getInt("user_id"),
                                postType = postObj.getString("post_type"),
                                itemName = postObj.getString("item_name"),
                                description = postObj.getString("description"),
                                category = postObj.getString("category"),
                                locationFound = postObj.getString("location_found"),
                                building = postObj.getString("building"),
                                floorNumber = if (postObj.isNull("floor_number")) null else postObj.getString("floor_number"),
                                status = postObj.getString("status"),
                                dateLostFound = postObj.getString("date_lost_found"),
                                contactNumber = if (postObj.isNull("contact_number")) null else postObj.getString("contact_number"),
                                imageUrls = jsonArrayToList(postObj.getJSONArray("image_urls")),
                                createdAt = postObj.getString("created_at"),
                                updatedAt = postObj.getString("updated_at"),
                                postedBy = postObj.getString("posted_by")
                            )
                            postsList.add(post)
                        }
                        
                        allPosts = postsList
                        refreshPosts()
                    } else {
                        Toast.makeText(this, jsonResponse.optString("message", "Failed to load posts"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Parsing error", e)
                    Toast.makeText(this, "Response format error. Check Logcat.", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                val responseBody = error.networkResponse?.data?.let { String(it) } ?: "No details"
                Log.e("HomeActivity", "Network error. Body: $responseBody", error)
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        
        Volley.newRequestQueue(this).add(request)
    }
    
    private fun setupRecyclerView() {
        // Setup adapter with filtered posts
        val filteredPosts = getFilteredPosts()
        postAdapter = PostAdapter(filteredPosts)
        
        // Setup RecyclerView
        postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = postAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun getFilteredPosts(): List<Post> {
        return when (currentFilter) {
            "Lost" -> allPosts.filter { it.postType.lowercase() == "lost" }
            "Found" -> allPosts.filter { it.postType.lowercase() == "found" }
            else -> allPosts
        }
    }
    
    private fun refreshPosts() {
        val filteredPosts = getFilteredPosts()
        postAdapter = PostAdapter(filteredPosts)
        postsRecyclerView.adapter = postAdapter
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_search -> {
                    navigateToSearch()
                    true
                }
                R.id.nav_post -> {
                    navigateToPostItem()
                    true
                }
                R.id.nav_chat -> {
                    navigateToChatSection()
                    true
                }
                R.id.nav_menu -> {
                    navigateToMenu()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupClickListeners() {
        // Notification icon click
        notificationIcon.setOnClickListener {
            // TODO: Open notifications
        }
        
        // Search bar click - navigate to search activity
        searchCardView.setOnClickListener {
            navigateToSearch()
        }
        
        // Filter button click - show filter dialog
        filterButton.setOnClickListener {
            showFilterDialog()
        }
    }
    
    private fun showFilterDialog() {
        val options = arrayOf("All Items", "Lost Items", "Found Items")
        val currentSelection = when (currentFilter) {
            "All" -> 0
            "Lost" -> 1
            "Found" -> 2
            else -> 0
        }
        
        AlertDialog.Builder(this)
            .setTitle("Filter Posts")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                currentFilter = when (which) {
                    0 -> "All"
                    1 -> "Lost"
                    2 -> "Found"
                    else -> "All"
                }
                
                // Update filter button text
                filterButton.text = currentFilter
                
                // Refresh posts with new filter
                refreshPosts()
                
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun navigateToSearch() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToPostItem() {
        val intent = Intent(this, PostItemActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToChatSection() {
        val intent = Intent(this, ChatSectionActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}
