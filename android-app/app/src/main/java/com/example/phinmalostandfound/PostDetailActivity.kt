package com.example.phinmalostandfound

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONObject

class PostDetailActivity : BaseActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var imageViewPager: ViewPager2
    private lateinit var imageCounter: TextView
    private lateinit var postTypeBadge: TextView
    private lateinit var postTitle: TextView
    private lateinit var postDescription: TextView
    private lateinit var categoryText: TextView
    private lateinit var postedByText: TextView
    private lateinit var lastSeenLocation: TextView
    private lateinit var lastSeenTime: TextView
    private lateinit var contactPerson: TextView
    private lateinit var contactButton: Button
    private lateinit var callButton: Button
    private lateinit var claimButton: Button
    private lateinit var shareButton: Button
    private lateinit var markResolvedButton: Button
    private lateinit var deletePostButton: Button

    private var postOwnerId: Int = -1
    private var postOwnerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        toolbar = findViewById(R.id.toolbar)
        imageViewPager = findViewById(R.id.imageViewPager)
        imageCounter = findViewById(R.id.imageCounter)
        postTypeBadge = findViewById(R.id.postTypeBadge)
        postTitle = findViewById(R.id.postTitle)
        postDescription = findViewById(R.id.postDescription)
        categoryText = findViewById(R.id.categoryText)
        postedByText = findViewById(R.id.postedByText)
        lastSeenLocation = findViewById(R.id.lastSeenLocation)
        lastSeenTime = findViewById(R.id.lastSeenTime)
        contactPerson = findViewById(R.id.contactPerson)
        contactButton = findViewById(R.id.contactButton)
        callButton = findViewById(R.id.callButton)
        claimButton = findViewById(R.id.claimButton)
        shareButton = findViewById(R.id.shareButton)
        markResolvedButton = findViewById(R.id.markResolvedButton)
        deletePostButton = findViewById(R.id.deletePostButton)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val postId = intent.getIntExtra("POST_ID", -1)
        if (postId != -1) {
            fetchPostDetails(postId)
        } else {
            Toast.makeText(this, "Invalid Post ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchPostDetails(postId: Int) {
        val url = ApiConfig.buildUrl(ApiConfig.GET_POST, "post_id" to postId.toString())

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                if (isFinishing || isDestroyed) return@JsonObjectRequest
                try {
                    if (response.getBoolean("success")) {
                        displayPostDetails(Post.fromJson(response.getJSONObject("post")))
                    } else {
                        Toast.makeText(this, response.optString("message", "Post not found"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing post details", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                if (isFinishing || isDestroyed) return@JsonObjectRequest
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun displayPostDetails(post: Post) {
        toolbar.title = post.itemName

        imageViewPager.adapter = ImagePagerAdapter(post.imageUrls)
        if (post.imageUrls.isEmpty()) {
            imageCounter.visibility = View.GONE
        } else {
            imageCounter.visibility = View.VISIBLE
            imageCounter.text = "1/${post.imageUrls.size}"
            imageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(pos: Int) {
                    imageCounter.text = "${pos + 1}/${post.imageUrls.size}"
                }
            })
        }

        postTypeBadge.text = post.postType.uppercase()
        postTypeBadge.setBackgroundColor(
            if (post.postType.lowercase() == "lost") Color.parseColor("#D32F2F")
            else Color.parseColor("#1B5E20")
        )

        postTitle.text = post.itemName
        postDescription.text = post.description
        categoryText.text = "Category: ${post.category}"
        postedByText.text = "Posted by: ${post.postedBy}"
        lastSeenLocation.text = "${post.locationFound}, ${post.building}, Floor ${post.floorNumber ?: "-"}"
        lastSeenTime.text = post.dateLostFound

        val contactText = post.contactNumber?.takeIf { it.isNotEmpty() } ?: "Not Provided"
        contactPerson.text = contactText

        // Tap contact number to copy; long-press also supported
        if (!post.contactNumber.isNullOrEmpty()) {
            contactPerson.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Contact", post.contactNumber))
                Toast.makeText(this, "Contact number copied!", Toast.LENGTH_SHORT).show()
            }

            // Call button
            callButton.visibility = View.VISIBLE
            callButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${post.contactNumber}")))
            }
        }

        postOwnerId = post.userId
        postOwnerName = post.postedBy

        val currentUserId = getSharedPreferences("PhinmaLostAndFound", MODE_PRIVATE).getInt("userId", -1)
        val isOwner = currentUserId != -1 && currentUserId == post.userId

        if (isOwner) {
            if (post.status == "active") {
                markResolvedButton.visibility = View.VISIBLE
                markResolvedButton.setOnClickListener { confirmMarkResolved(post.postId) }
            }
            deletePostButton.visibility = View.VISIBLE
            deletePostButton.setOnClickListener { confirmDeletePost(post.postId) }
        } else {
            // Non-owner: show claim button on "found" posts
            if (post.postType.lowercase() == "found" && post.status == "active") {
                claimButton.visibility = View.VISIBLE
                claimButton.setOnClickListener {
                    val intent = Intent(this, PrivateMessageActivity::class.java)
                    intent.putExtra("USER_ID", post.userId.toString())
                    intent.putExtra("USER_NAME", post.postedBy)
                    intent.putExtra("POST_TITLE", post.itemName)
                    intent.putExtra("PREFILL_MESSAGE", "Hi, I think this item is mine: ${post.itemName}. ")
                    intent.putExtra("SECURITY_QUESTION", post.securityQuestion)
                    startActivity(intent)
                }
            }
        }

        // Share
        shareButton.setOnClickListener {
            val shareText = buildString {
                append("PHINMA Lost & Found\n\n")
                append("${post.postType.uppercase()}: ${post.itemName}\n")
                append("Location: ${post.locationFound}\n")
                append("Date: ${post.dateLostFound}\n")
                if (!post.contactNumber.isNullOrEmpty()) append("Contact: ${post.contactNumber}\n")
                append("\n${post.description}")
            }
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }, "Share this post"
            ))
        }

        // Contact (message)
        contactButton.setOnClickListener {
            val intent = Intent(this, PrivateMessageActivity::class.java)
            intent.putExtra("USER_ID", postOwnerId.toString())
            intent.putExtra("USER_NAME", postOwnerName)
            intent.putExtra("POST_TITLE", post.itemName)
            intent.putExtra("SECURITY_QUESTION", post.securityQuestion)
            startActivity(intent)
        }
    }

    private fun confirmMarkResolved(postId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Mark as Resolved")
            .setMessage("Has this item been found/returned? This will mark the post as resolved.")
            .setPositiveButton("Yes, Resolved") { _, _ -> markPostResolved(postId) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markPostResolved(postId: Int) {
        val body = JSONObject().apply {
            put("post_id", postId)
            put("status", "resolved")
        }

        val request = JsonObjectRequest(
            Request.Method.POST, ApiConfig.UPDATE_POST, body,
            { response ->
                if (response.optBoolean("success", false)) {
                    Toast.makeText(this, "Post marked as resolved!", Toast.LENGTH_SHORT).show()
                    markResolvedButton.visibility = View.GONE
                } else {
                    Toast.makeText(this, "Could not update post status", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun confirmDeletePost(postId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deletePost(postId) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost(postId: Int) {
        val url = ApiConfig.buildUrl(ApiConfig.DELETE_POST, "post_id" to postId.toString())
        val request = JsonObjectRequest(
            Request.Method.DELETE, url, null,
            { response ->
                if (response.optBoolean("success", false)) {
                    Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Could not delete post", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        AppSingleton.getRequestQueue(this).add(request)
    }
}
