package com.example.phinmalostandfound

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.appbar.MaterialToolbar

class PostDetailActivity : AppCompatActivity() {

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
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val postObj = response.getJSONObject("post")
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
                        displayPostDetails(post)
                    } else {
                        Toast.makeText(this, response.optString("message", "Post not found"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing post details", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun displayPostDetails(post: Post) {
        toolbar.title = post.itemName
        
        imageViewPager.adapter = ImagePagerAdapter(post.imageUrls)
        if (post.imageUrls.isEmpty()) {
            imageCounter.visibility = android.view.View.GONE
        } else {
            imageCounter.visibility = android.view.View.VISIBLE
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
        contactPerson.text = post.contactNumber ?: "Not Provided"

        postOwnerId = post.userId
        postOwnerName = post.postedBy

        contactButton.setOnClickListener {
            val intent = Intent(this, PrivateMessageActivity::class.java)
            intent.putExtra("USER_ID", postOwnerId.toString())
            intent.putExtra("USER_NAME", postOwnerName)
            intent.putExtra("POST_TITLE", post.itemName)
            startActivity(intent)
        }
    }
}
