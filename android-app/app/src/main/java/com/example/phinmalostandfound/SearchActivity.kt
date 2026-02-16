package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.text.Editable
import android.text.TextWatcher

class SearchActivity : AppCompatActivity() {
    
    private lateinit var searchEditText: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        
        initializeViews()
        setupRecyclerView()
        setupBottomNavigation()
        setupSearchListener()
    }
    
    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }
    
    private fun setupRecyclerView() {
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Set adapter with search results
        // searchResultsRecyclerView.adapter = SearchResultsAdapter(resultsList)
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_search
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navigateToHome()
                    true
                }
                R.id.nav_search -> {
                    // Already on search
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
    
    private fun setupSearchListener() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun performSearch(query: String) {
        // TODO: Implement search logic
        // Search for lost items, users, or posts based on query
    }
    
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
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
