package com.example.phinmalostfound

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.bumptech.glide.Glide

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

    private var post: Post? = null

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

        // Get post ID from intent
        val postId = intent.getIntExtra("POST_ID", -1)

        // TODO: Replace PresetPosts with actual database fetch when ready
        post = PresetPosts.getPresetPosts().find { it.postId == postId }

        if (post == null) {
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayPostDetails()
        setupClickListeners()
    }

    private fun displayPostDetails() {
        post?.let { post ->

            // Toolbar
            toolbar.title = post.itemName
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            // Images via ViewPager2 using Glide
            val imageAdapter = ImagePagerAdapter(post.imageUrls) { url, imageView ->
                Glide.with(this).load(url)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imageView)
            }
            imageViewPager.adapter = imageAdapter

            // Image counter
            imageViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(pos: Int) {
                    imageCounter.text = "${pos + 1}/${post.imageUrls.size}"
                }
            })
            imageCounter.text = "1/${post.imageUrls.size}"

            // Post type badge
            postTypeBadge.text = post.postType.uppercase()
            postTypeBadge.setBackgroundColor(
                if (post.postType.lowercase() == "lost") Color.parseColor("#D32F2F")
                else Color.parseColor("#1B5E20")
            )

            // Text fields
            postTitle.text = post.itemName
            postDescription.text = post.description
            categoryText.text = "Category: ${post.category}"
            postedByText.text = "Posted by: ${post.postedBy}"
            lastSeenLocation.text =
                "${post.locationFound}, ${post.building}, Floor ${post.floorNumber ?: "-"}"
            lastSeenTime.text = post.dateLostFound
            contactPerson.text = post.contactNumber ?: "Not Provided"
        }
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener { finish() }

        contactButton.setOnClickListener {
            post?.let { post ->
                val contact = post.contactNumber
                if (!contact.isNullOrEmpty()) {
                    // Dial number
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${contact.replace("-", "")}")
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Contact not provided", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}