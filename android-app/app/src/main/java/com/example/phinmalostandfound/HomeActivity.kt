package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

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
        loadPosts()
        setupRecyclerView()
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
        // Get preset posts
        allPosts = PresetPosts.getPresetPosts()
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
            "Lost" -> allPosts.filter { it.postType == "lost" }
            "Found" -> allPosts.filter { it.postType == "found" }
            else -> allPosts
        }
    }
    
    private fun refreshPosts() {
        val filteredPosts = getFilteredPosts()
        postAdapter = PostAdapter(filteredPosts)
        postsRecyclerView.adapter = postAdapter
    }
    
    private fun setupBottomNavigation() {
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
