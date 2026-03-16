package com.example.phinmalostandfound

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class PrivateMessageActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var backButton: ImageView
    private lateinit var bottomNavigation: BottomNavigationView

    private var otherUserId: String? = null
    private var currentUserId: Int = -1
    private val messagesList = mutableListOf<Message>()
    private lateinit var messagesAdapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_message)

        otherUserId = intent.getStringExtra("USER_ID")
        val userName = intent.getStringExtra("USER_NAME") ?: "Unknown User"
        val postTitle = intent.getStringExtra("POST_TITLE")

        val prefs = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
        currentUserId = prefs.getInt("userId", -1)

        initializeViews()
        userNameTextView.text = userName

        if (postTitle != null) {
            Toast.makeText(this, "Chat about: $postTitle", Toast.LENGTH_SHORT).show()
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
        messagesAdapter = MessagesAdapter(messagesList, currentUserId)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        chatRecyclerView.layoutManager = layoutManager
        chatRecyclerView.adapter = messagesAdapter
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_chat
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener { sendMessage() }
        backButton.setOnClickListener { finish() }
    }

    private fun loadMessages() {
        val otherId = otherUserId ?: return
        if (currentUserId == -1) return

        val url = ApiConfig.buildUrl(
            ApiConfig.GET_CONVERSATION,
            "user_id" to currentUserId.toString(),
            "other_user_id" to otherId
        )

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonStart = response.indexOf("{")
                    if (jsonStart == -1) return@StringRequest
                    val json = JSONObject(response.substring(jsonStart))

                    if (json.optBoolean("success", false)) {
                        val messagesJson = json.optJSONArray("data")
                        messagesList.clear()

                        if (messagesJson != null) {
                            for (i in 0 until messagesJson.length()) {
                                val m = messagesJson.getJSONObject(i)
                                messagesList.add(Message(
                                    messageId = m.optInt("message_id", 0),
                                    senderId = m.optInt("sender_id", 0),
                                    messageText = m.optString("message_text", ""),
                                    sentAt = m.optString("sent_at", "")
                                ))
                            }
                        }

                        messagesAdapter.notifyDataSetChanged()
                        if (messagesList.isNotEmpty()) {
                            chatRecyclerView.scrollToPosition(messagesList.size - 1)
                        }

                        markMessagesRead(otherId)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()
        if (messageText.isEmpty()) return
        val otherId = otherUserId ?: return
        if (currentUserId == -1) return

        val body = JSONObject().apply {
            put("sender_id", currentUserId)
            put("receiver_id", otherId)
            put("message_text", messageText)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, ApiConfig.SEND_MESSAGE, body,
            { response ->
                if (response.optBoolean("success", false)) {
                    messageEditText.setText("")
                    loadMessages()
                } else {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Send failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun markMessagesRead(otherId: String) {
        val body = JSONObject().apply {
            put("user_id", currentUserId)
            put("other_user_id", otherId)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, ApiConfig.MARK_READ, body,
            { /* silent */ }, { /* silent */ }
        )

        Volley.newRequestQueue(this).add(request)
    }
}
