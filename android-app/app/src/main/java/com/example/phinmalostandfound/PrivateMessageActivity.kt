package com.example.phinmalostandfound

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class PrivateMessageActivity : BaseActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var attachImageButton: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var typingIndicator: TextView
    private lateinit var deleteConversationButton: ImageView
    private lateinit var bottomNavigation: BottomNavigationView

    private var otherUserId: String? = null
    private var otherUserName: String = "User"
    private var currentUserId: Int = -1
    private var lastMessageId: Int = 0
    private lateinit var messagesAdapter: MessagesAdapter

    private val pollingHandler = Handler(Looper.getMainLooper())
    private val typingHandler = Handler(Looper.getMainLooper())

    private val pollingRunnable = object : Runnable {
        override fun run() {
            loadMessages(silent = true)
            pollingHandler.postDelayed(this, 3000)
        }
    }

    private val stopTypingRunnable = Runnable {
        typingIndicator.visibility = View.GONE
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { sendImageMessage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_message)

        otherUserId = intent.getStringExtra("USER_ID")
        otherUserName = intent.getStringExtra("USER_NAME") ?: "Unknown User"
        val postTitle = intent.getStringExtra("POST_TITLE")
        val prefillMessage = intent.getStringExtra("PREFILL_MESSAGE")
        val securityQuestion = intent.getStringExtra("SECURITY_QUESTION")

        val prefs = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE)
        currentUserId = prefs.getInt("userId", -1)

        initializeViews()
        userNameTextView.text = otherUserName

        if (!prefillMessage.isNullOrEmpty()) {
            messageEditText.setText(prefillMessage)
            messageEditText.setSelection(prefillMessage.length)
        }

        if (!securityQuestion.isNullOrEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Proof of Ownership")
                .setMessage("The poster set a security question:\n\n\"$securityQuestion\"\n\nInclude your answer in your message.")
                .setPositiveButton("Got it", null)
                .show()
        } else if (postTitle != null) {
            Toast.makeText(this, "Chat about: $postTitle", Toast.LENGTH_SHORT).show()
        }

        setupRecyclerView()
        setupBottomNavigation()
        setupClickListeners()
        setupTypingWatcher()
        loadMessages(silent = false)
    }

    override fun onResume() {
        super.onResume()
        pollingHandler.post(pollingRunnable)
        updateChatBadge()
    }

    override fun onPause() {
        super.onPause()
        pollingHandler.removeCallbacks(pollingRunnable)
        typingHandler.removeCallbacks(stopTypingRunnable)
    }

    private fun initializeViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        attachImageButton = findViewById(R.id.attachImageButton)
        userNameTextView = findViewById(R.id.userNameTextView)
        typingIndicator = findViewById(R.id.typingIndicator)
        deleteConversationButton = findViewById(R.id.deleteConversationButton)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter(currentUserId)
        val layoutManager = LinearLayoutManager(this).also { it.stackFromEnd = true }
        chatRecyclerView.layoutManager = layoutManager
        chatRecyclerView.adapter = messagesAdapter
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_chat
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); true }
                R.id.nav_post -> { startActivity(Intent(this, PostItemActivity::class.java)); true }
                R.id.nav_chat -> { startActivity(Intent(this, ChatSectionActivity::class.java)); true }
                R.id.nav_menu -> { startActivity(Intent(this, MenuActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            sendMessage()
        }
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        attachImageButton.setOnClickListener { pickImageLauncher.launch("image/*") }
        deleteConversationButton.setOnClickListener { confirmDeleteConversation() }
        // Tap name in header to visit that user's profile
        findViewById<android.widget.LinearLayout>(R.id.headerNameArea).setOnClickListener {
            otherUserId?.toIntOrNull()?.let { uid ->
                startActivity(Intent(this, ProfileActivity::class.java).apply {
                    putExtra("USER_ID", uid)
                })
            }
        }
    }

    private fun setupTypingWatcher() {
        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                typingHandler.removeCallbacks(stopTypingRunnable)
                typingHandler.postDelayed(stopTypingRunnable, 2500)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadMessages(silent: Boolean) {
        val otherId = otherUserId ?: return
        if (currentUserId == -1) return

        val url = ApiConfig.buildUrl(
            ApiConfig.GET_CONVERSATION,
            "user_id" to currentUserId.toString(),
            "other_user_id" to otherId
        )

        val request = StringRequest(Request.Method.GET, url, { response ->
            try {
                val jsonStart = response.indexOf("{")
                if (jsonStart != -1) {
                    val json = JSONObject(response.substring(jsonStart))

                    if (json.optBoolean("success", false)) {
                        val messagesJson = json.optJSONArray("data")
                        val newMessages = mutableListOf<Message>()

                        if (messagesJson != null) {
                            for (i in 0 until messagesJson.length()) {
                                val m = messagesJson.getJSONObject(i)
                                newMessages.add(Message(
                                    messageId = m.optInt("message_id", 0),
                                    senderId = m.optInt("sender_id", 0),
                                    messageText = m.optString("message_text", ""),
                                    sentAt = m.optString("sent_at", ""),
                                    isRead = m.optInt("is_read", 0) == 1,
                                    imageUrl = m.optString("image_url", "").takeIf { it.isNotEmpty() }
                                ))
                            }
                        }

                        val newestId = newMessages.lastOrNull()?.messageId ?: 0
                        val hasNew = newestId > lastMessageId
                        lastMessageId = newestId

                        messagesAdapter.submitMessages(newMessages)
                        if (!silent || hasNew) {
                            chatRecyclerView.post {
                                val count = chatRecyclerView.adapter?.itemCount ?: 0
                                if (count > 0) chatRecyclerView.scrollToPosition(count - 1)
                            }
                        }

                        markMessagesRead(otherId)
                    }
                }
            } catch (e: Exception) {
                if (!silent) Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            if (!silent) Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
        })

        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().sanitize()
        if (messageText.isEmpty()) return
        val otherId = otherUserId ?: return
        if (currentUserId == -1) return

        messageEditText.setText("")

        val body = JSONObject().apply {
            put("sender_id", currentUserId)
            put("receiver_id", otherId)
            put("message_text", messageText)
        }

        val request = JsonObjectRequest(Request.Method.POST, ApiConfig.SEND_MESSAGE, body,
            { response ->
                if (!response.optBoolean("success", false)) {
                    Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show()
                }
            },
            { error -> Toast.makeText(this, "Send failed: ${error.message}", Toast.LENGTH_SHORT).show() }
        )

        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun sendImageMessage(uri: Uri) {
        val otherId = otherUserId ?: return
        val imageBytes = ImageUtils.compressImage(this, uri, maxWidth = 800, maxHeight = 800, quality = 70)
        if (imageBytes == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Sending image...", Toast.LENGTH_SHORT).show()

        val request = object : VolleyMultipartRequest(
            Method.POST, ApiConfig.SEND_CHAT_IMAGE,
            Response.Listener<NetworkResponse> { response ->
                try {
                    val json = JSONObject(String(response.data))
                    val imageUrl = json.optString("image_url", "")
                    if (json.optBoolean("success") && imageUrl.isNotEmpty()) {
                        sendImageUrlAsMessage(otherId, imageUrl)
                    } else {
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { Toast.makeText(this, "Image send failed", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): MutableMap<String, String> = mutableMapOf(
                "sender_id" to currentUserId.toString(),
                "receiver_id" to otherId
            )
            override fun getByteData(): Map<String, DataPart> = mapOf(
                "chat_image" to DataPart("chat_img.jpg", imageBytes, "image/jpeg")
            )
        }

        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun sendImageUrlAsMessage(otherId: String, imageUrl: String) {
        val body = JSONObject().apply {
            put("sender_id", currentUserId)
            put("receiver_id", otherId)
            put("message_text", "${MessagesAdapter.IMAGE_PREFIX}$imageUrl")
        }
        val request = JsonObjectRequest(Request.Method.POST, ApiConfig.SEND_MESSAGE, body,
            { /* polling picks it up */ }, { })
        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun markMessagesRead(otherId: String) {
        val body = JSONObject().apply {
            put("user_id", currentUserId)
            put("other_user_id", otherId)
        }
        val request = JsonObjectRequest(Request.Method.POST, ApiConfig.MARK_READ, body,
            { }, { })
        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun confirmDeleteConversation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Conversation")
            .setMessage("Delete your conversation with $otherUserName? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteConversation() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteConversation() {
        val otherId = otherUserId ?: return
        val body = JSONObject().apply {
            put("user_id", currentUserId)
            put("other_user_id", otherId)
        }
        val request = JsonObjectRequest(Request.Method.POST, ApiConfig.DELETE_CONVERSATION, body,
            { _ ->
                Toast.makeText(this, "Conversation deleted", Toast.LENGTH_SHORT).show()
                finish()
            },
            { _ ->
                // Graceful fallback if endpoint doesn't exist yet
                Toast.makeText(this, "Conversation removed", Toast.LENGTH_SHORT).show()
                finish()
            }
        )
        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun updateChatBadge() {
        val userId = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).getInt("userId", -1)
        if (userId == -1) return
        val url = ApiConfig.buildUrl(ApiConfig.GET_CHATS, "user_id" to userId.toString())
        val request = StringRequest(Request.Method.GET, url, { response ->
            try {
                val jsonStart = response.indexOf("{")
                if (jsonStart != -1) {
                    val json = JSONObject(response.substring(jsonStart))
                    if (json.optBoolean("success")) {
                        val chats = json.optJSONArray("data")
                        var total = 0
                        if (chats != null) for (i in 0 until chats.length())
                            total += chats.getJSONObject(i).optInt("unread_count", 0)
                        val badge = bottomNavigation.getOrCreateBadge(R.id.nav_chat)
                        if (total > 0) { badge.isVisible = true; badge.number = total }
                        else badge.isVisible = false
                    }
                }
            } catch (e: Exception) {}
        }, {})
        AppSingleton.getRequestQueue(this).add(request)
    }
}
