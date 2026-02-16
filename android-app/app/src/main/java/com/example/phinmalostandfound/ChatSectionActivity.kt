package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ChatSectionActivity : AppCompatActivity() {
    
    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_section)
        
        initializeViews()
        setupRecyclerView()
        setupBottomNavigation()
    }
    
    private fun initializeViews() {
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }
    
    private fun setupRecyclerView() {
        chatsRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Set adapter with chat list data
        // chatsRecyclerView.adapter = ChatsAdapter(chatsList) { chat ->
        //     navigateToPrivateMessage(chat.userId)
        // }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_chat
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navigateToHome()
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
                    // Already on chat section
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
    
    private fun navigateToPrivateMessage(userId: String) {
        val intent = Intent(this, PrivateMessageActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
    }
    
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToSearch() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToPostItem() {
        val intent = Intent(this, PostItemActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}
