package com.example.phinmalostandfound

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
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

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var recentSearchesTextView: TextView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initializeViews()
        setupRecyclerView()
        setupBottomNavigation()
        setupSearchListener()

        // Support pre-filled query passed from HomeActivity
        intent.getStringExtra("SEARCH_QUERY")?.takeIf { it.isNotEmpty() }?.let {
            searchEditText.setText(it)
            searchEditText.setSelection(it.length)
            performSearch(it)
        }
    }

    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        recentSearchesTextView = findViewById(R.id.recentSearchesTextView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupRecyclerView() {
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchResultsRecyclerView.adapter = PostAdapter(emptyList())
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_search
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { navigateToHome(); true }
                R.id.nav_search -> true
                R.id.nav_post -> { navigateToPostItem(); true }
                R.id.nav_chat -> { navigateToChatSection(); true }
                R.id.nav_menu -> { navigateToMenu(); true }
                else -> false
            }
        }
    }

    private fun setupSearchListener() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    recentSearchesTextView.visibility = View.GONE
                    performSearch(query)
                } else {
                    recentSearchesTextView.visibility = View.VISIBLE
                    searchResultsRecyclerView.adapter = PostAdapter(emptyList())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        val url = ApiConfig.buildUrl(ApiConfig.SEARCH_POSTS, "keyword" to query)

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonStart = response.indexOf("{")
                    if (jsonStart == -1) return@StringRequest
                    val json = JSONObject(response.substring(jsonStart))

                    if (json.optBoolean("success", false)) {
                        val postsJson = json.optJSONArray("posts")
                        val postsList = mutableListOf<Post>()

                        if (postsJson != null) {
                            for (i in 0 until postsJson.length()) {
                                try {
                                    val p = postsJson.getJSONObject(i)
                                    postsList.add(Post(
                                        postId = p.optInt("post_id", 0),
                                        userId = p.optInt("user_id", 0),
                                        postType = p.optString("post_type", "unknown"),
                                        itemName = p.optString("item_name", "Unnamed Item"),
                                        description = p.optString("description", ""),
                                        category = p.optString("category", "General"),
                                        locationFound = p.optString("location_found", ""),
                                        building = p.optString("building", ""),
                                        floorNumber = if (p.isNull("floor_number")) null else p.optString("floor_number"),
                                        status = p.optString("status", "active"),
                                        dateLostFound = p.optString("date_lost_found", ""),
                                        contactNumber = if (p.isNull("contact_number")) null else p.optString("contact_number"),
                                        imageUrls = p.optJSONArray("image_urls")?.let { jsonArrayToList(it) } ?: emptyList(),
                                        createdAt = p.optString("created_at", ""),
                                        updatedAt = p.optString("updated_at", ""),
                                        postedBy = p.optString("posted_by", "Anonymous")
                                    ))
                                } catch (e: Exception) { /* skip malformed post */ }
                            }
                        }

                        searchResultsRecyclerView.adapter = PostAdapter(postsList)

                        if (postsList.isEmpty()) {
                            Toast.makeText(this, "No results for \"$query\"", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, json.optString("message", "Search failed"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing search results", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    private fun navigateToPostItem() {
        startActivity(Intent(this, PostItemActivity::class.java))
    }

    private fun navigateToChatSection() {
        startActivity(Intent(this, ChatSectionActivity::class.java))
    }

    private fun navigateToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
    }
}
