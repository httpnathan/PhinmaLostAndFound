package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class ChatSectionActivity : AppCompatActivity() {

    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var emptyChatsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_section)

        initializeViews()
        setupRecyclerView()
        setupBottomNavigation()
        loadChats()
    }

    private fun initializeViews() {
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        emptyChatsTextView = findViewById(R.id.emptyChatsTextView)
    }

    private fun setupRecyclerView() {
        chatsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadChats() {
        val prefs = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)
        if (userId == -1) return
        val userIdStr = userId.toString()

        val url = ApiConfig.buildUrl(ApiConfig.GET_CHATS, "user_id" to userIdStr)

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonStart = response.indexOf("{")
                    if (jsonStart == -1) return@StringRequest
                    val json = JSONObject(response.substring(jsonStart))

                    if (json.optBoolean("success", false)) {
                        val chatsJson = json.optJSONArray("data")
                        val chatsList = mutableListOf<ChatItem>()

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

                        if (chatsList.isEmpty()) {
                            emptyChatsTextView.visibility = View.VISIBLE
                            chatsRecyclerView.visibility = View.GONE
                        } else {
                            emptyChatsTextView.visibility = View.GONE
                            chatsRecyclerView.visibility = View.VISIBLE
                            chatsRecyclerView.adapter = ChatsAdapter(chatsList) { chat ->
                                navigateToPrivateMessage(chat.otherUserId, "${chat.firstName} ${chat.lastName}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error loading chats", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_chat
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { navigateToHome(); true }
                R.id.nav_search -> { navigateToSearch(); true }
                R.id.nav_post -> { navigateToPostItem(); true }
                R.id.nav_chat -> true
                R.id.nav_menu -> { navigateToMenu(); true }
                else -> false
            }
        }
    }

    private fun navigateToPrivateMessage(userId: String, userName: String) {
        val intent = Intent(this, PrivateMessageActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.putExtra("USER_NAME", userName)
        startActivity(intent)
    }

    private fun navigateToHome() { startActivity(Intent(this, HomeActivity::class.java)) }
    private fun navigateToSearch() { startActivity(Intent(this, SearchActivity::class.java)) }
    private fun navigateToPostItem() { startActivity(Intent(this, PostItemActivity::class.java)) }
    private fun navigateToMenu() { startActivity(Intent(this, MenuActivity::class.java)) }
}
