package com.example.phinmalostandfound

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class PrivateMessageActivity : AppCompatActivity() {
    
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var backButton: ImageView
    private lateinit var bottomNavigation: BottomNavigationView
    
    private var otherUserId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_message)
        
        // Get user info from intent
        otherUserId = intent.getStringExtra("USER_ID")
        val userName = intent.getStringExtra("USER_NAME") ?: "Unknown User"
        val postTitle = intent.getStringExtra("POST_TITLE")
        
        initializeViews()
        
        // Set user name in header
        userNameTextView.text = userName
        
        // Show post title if available
        if (postTitle != null) {
            android.widget.Toast.makeText(
                this,
                "Chat about: $postTitle",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        
        setupRecyclerView()
        setupBottomNavigation()
        setupClickListeners()
        loadMessages()
    }
    
    private fun initializeViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        userNameTextView = findViewById(R.id.userNameTextView)
        backButton = findViewById(R.id.backButton)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }
    
    private fun setupRecyclerView() {
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Set adapter with messages
        // chatRecyclerView.adapter = MessagesAdapter(messagesList)
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_chat
    }
    
    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }
        
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun loadMessages() {
        // TODO: Load messages from database for this user
        // userNameTextView.text = "Other User's Name"
    }
    
    private fun sendMessage() {
        val messageText = messageEditText.text.toString()
        if (messageText.isNotBlank()) {
            // TODO: Send message to database
            messageEditText.setText("")
        }
    }
}
