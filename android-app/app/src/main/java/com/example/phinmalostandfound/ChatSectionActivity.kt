package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class ChatSectionActivity : BaseActivity() {

    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var emptyChatsLayout: View
    private lateinit var chatsProgressBar: ProgressBar

    private val chatsList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_section)

        initializeViews()
        setupBottomNavigation()
        loadChats()

        val scanFab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.scanFab)
        scanFab.setOnClickListener { startActivity(Intent(this, ScanActivity::class.java)) }
        bottomNavigation.elevation = 0f
        scanFab.bringToFront()
    }

    override fun onResume() {
        super.onResume()
        loadChats()
    }

    private fun initializeViews() {
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        emptyChatsLayout = findViewById(R.id.emptyChatsLayout)
        chatsProgressBar = findViewById(R.id.chatsProgressBar)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_chat
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); true }
                R.id.nav_post -> { startActivity(Intent(this, PostItemActivity::class.java)); true }
                R.id.nav_chat -> true
                R.id.nav_menu -> { startActivity(Intent(this, MenuActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun loadChats() {
        val userId = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).getInt("userId", -1)
        if (userId == -1) return

        chatsProgressBar.visibility = View.VISIBLE
        chatsRecyclerView.visibility = View.GONE
        emptyChatsLayout.visibility = View.GONE

        val url = ApiConfig.buildUrl(ApiConfig.GET_CHATS, "user_id" to userId.toString())

        val request = StringRequest(Request.Method.GET, url, { response ->
            chatsProgressBar.visibility = View.GONE
            try {
                val jsonStart = response.indexOf("{")
                if (jsonStart != -1) {
                    val json = JSONObject(response.substring(jsonStart))

                    if (json.optBoolean("success", false)) {
                        val chatsJson = json.optJSONArray("data")
                        chatsList.clear()

                        if (chatsJson != null) {
                            for (i in 0 until chatsJson.length()) {
                                val c = chatsJson.getJSONObject(i)
                                chatsList.add(ChatItem(
                                    otherUserId = c.optString("other_user_id", ""),
                                    firstName = c.optString("first_name", ""),
                                    lastName = c.optString("last_name", ""),
                                    lastMessage = c.optString("last_message", ""),
                                    lastMessageTime = c.optString("last_message_time", ""),
                                    unreadCount = c.optInt("unread_count", 0)
                                ))
                            }
                        }

                        updateChatBadge()

                        if (chatsList.isEmpty()) {
                            emptyChatsLayout.visibility = View.VISIBLE
                            chatsRecyclerView.visibility = View.GONE
                        } else {
                            emptyChatsLayout.visibility = View.GONE
                            chatsRecyclerView.visibility = View.VISIBLE
                            setupChatsAdapter()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error loading chats", Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            chatsProgressBar.visibility = View.GONE
            Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
        })

        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun setupChatsAdapter() {
        chatsRecyclerView.layoutManager = LinearLayoutManager(this)
        chatsRecyclerView.adapter = ChatsAdapter(
            chats = chatsList,
            onChatClick = { chat ->
                startActivity(Intent(this, PrivateMessageActivity::class.java).apply {
                    putExtra("USER_ID", chat.otherUserId)
                    putExtra("USER_NAME", "${chat.firstName} ${chat.lastName}".trim())
                })
            },
            onChatLongClick = { chat -> confirmDeleteConversation(chat) }
        )
    }

    private fun confirmDeleteConversation(chat: ChatItem) {
        val name = "${chat.firstName} ${chat.lastName}".trim()
        AlertDialog.Builder(this)
            .setTitle("Delete Conversation")
            .setMessage("Delete your conversation with $name? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteConversation(chat) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteConversation(chat: ChatItem) {
        val userId = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).getInt("userId", -1)
        val body = JSONObject().apply {
            put("user_id", userId)
            put("other_user_id", chat.otherUserId)
        }
        val request = JsonObjectRequest(Request.Method.POST, ApiConfig.DELETE_CONVERSATION, body,
            { _ ->
                chatsList.remove(chat)
                if (chatsList.isEmpty()) {
                    emptyChatsLayout.visibility = View.VISIBLE
                    chatsRecyclerView.visibility = View.GONE
                } else {
                    setupChatsAdapter()
                }
                updateChatBadge()
                Toast.makeText(this, "Conversation deleted", Toast.LENGTH_SHORT).show()
            },
            { _ ->
                chatsList.remove(chat)
                if (chatsList.isNotEmpty()) setupChatsAdapter()
                Toast.makeText(this, "Conversation removed", Toast.LENGTH_SHORT).show()
            }
        )
        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun updateChatBadge() {
        val total = chatsList.sumOf { it.unreadCount }
        val badge = bottomNavigation.getOrCreateBadge(R.id.nav_chat)
        if (total > 0) { badge.isVisible = true; badge.number = total }
        else badge.isVisible = false
    }
}
