package com.example.phinmalostandfound

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONObject

class SearchActivity : BaseActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var staticSearchSection: View
    private lateinit var recentSearchesTextView: TextView
    private lateinit var clearAllText: TextView
    private lateinit var recentSearchesContainer: LinearLayout
    private lateinit var noRecentSearchesText: TextView
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var typeFilterChipGroup: ChipGroup
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchProgressBar: ProgressBar
    private lateinit var emptySearchResultsLayout: View
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var postAdapter: PostAdapter

    private val PREFS_RECENT = "PhinmaLostAndFound"
    private val KEY_RECENT_SEARCHES = "recentSearches"
    private val MAX_RECENT = 5

    private var allSearchResults: List<Post> = emptyList()
    private var currentTypeFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initializeViews()
        setupRecyclerView()
        setupBottomNavigation()
        setupSearchListener()
        setupCategoryChips()
        setupTypeFilterChips()
        loadRecentSearches()
        setupScanFab()

        bottomNavigation.elevation = 0f
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.scanFab).bringToFront()

        // Support pre-filled query passed from HomeActivity
        intent.getStringExtra("SEARCH_QUERY")?.takeIf { it.isNotEmpty() }?.let { q ->
            searchEditText.setText(q)
            searchEditText.setSelection(q.length)
            staticSearchSection.visibility = View.GONE
            performSearch(q)
        }
    }

    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        staticSearchSection = findViewById(R.id.staticSearchSection)
        recentSearchesTextView = findViewById(R.id.recentSearchesTextView)
        clearAllText = findViewById(R.id.clearAllText)
        recentSearchesContainer = findViewById(R.id.recentSearchesContainer)
        noRecentSearchesText = findViewById(R.id.noRecentSearchesText)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        typeFilterChipGroup = findViewById(R.id.typeFilterChipGroup)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        searchProgressBar = findViewById(R.id.searchProgressBar)
        emptySearchResultsLayout = findViewById(R.id.emptySearchResultsLayout)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        clearAllText.setOnClickListener {
            saveRecentSearchList(emptyList())
            loadRecentSearches()
        }
    }

    private fun setupScanFab() {
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.scanFab)
            .setOnClickListener { startActivity(Intent(this, ScanActivity::class.java)) }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchResultsRecyclerView.adapter = postAdapter
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { navigateToHome(); true }
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
                    staticSearchSection.visibility = View.GONE
                    performSearch(query)
                } else {
                    staticSearchSection.visibility = View.VISIBLE
                    searchProgressBar.visibility = View.GONE
                    searchResultsRecyclerView.visibility = View.GONE
                    emptySearchResultsLayout.visibility = View.GONE
                    typeFilterChipGroup.visibility = View.GONE
                    allSearchResults = emptyList()
                    postAdapter.submitList(emptyList())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTypeFilterChips() {
        typeFilterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            currentTypeFilter = when {
                checkedIds.contains(R.id.chipFilterLost) -> "Lost"
                checkedIds.contains(R.id.chipFilterFound) -> "Found"
                else -> "All"
            }
            applyTypeFilter()
        }
    }

    private fun applyTypeFilter() {
        val filtered = when (currentTypeFilter) {
            "Lost" -> allSearchResults.filter { it.postType.lowercase() == "lost" }
            "Found" -> allSearchResults.filter { it.postType.lowercase() == "found" }
            else -> allSearchResults
        }
        postAdapter.submitList(filtered)
    }

    private fun setupCategoryChips() {
        val chipIds = listOf(
            R.id.chipElectronics, R.id.chipClothing, R.id.chipBooks,
            R.id.chipIdCard, R.id.chipPhone, R.id.chipBag,
            R.id.chipKeys, R.id.chipWallet, R.id.chipUmbrella, R.id.chipWaterBottle
        )
        chipIds.forEach { id ->
            categoryChipGroup.findViewById<Chip>(id)?.setOnClickListener { chip ->
                val query = (chip as Chip).text.toString()
                searchEditText.setText(query)
                searchEditText.setSelection(query.length)
            }
        }
    }

    private fun loadRecentSearches() {
        val searches = getRecentSearchList()
        recentSearchesContainer.removeAllViews()

        if (searches.isEmpty()) {
            noRecentSearchesText.visibility = View.VISIBLE
            clearAllText.visibility = View.GONE
        } else {
            noRecentSearchesText.visibility = View.GONE
            clearAllText.visibility = View.VISIBLE
            searches.forEach { query -> addRecentSearchRow(query) }
        }
    }

    private fun addRecentSearchRow(query: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                (16 * resources.displayMetrics.density).toInt(),
                (14 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (14 * resources.displayMetrics.density).toInt()
            )
            isClickable = true
            isFocusable = true
            setBackgroundResource(android.R.drawable.list_selector_background)
        }

        val clockIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_time)
            imageTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.text_secondary, theme)
            )
            layoutParams = LinearLayout.LayoutParams(
                (20 * resources.displayMetrics.density).toInt(),
                (20 * resources.displayMetrics.density).toInt()
            )
        }

        val queryText = TextView(this).apply {
            text = query
            textSize = 15f
            setTextColor(resources.getColor(R.color.text_primary, theme))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (12 * resources.displayMetrics.density).toInt()
            }
        }

        val removeBtn = TextView(this).apply {
            text = "✕"
            textSize = 16f
            setTextColor(resources.getColor(R.color.text_secondary, theme))
            setPadding(
                (8 * resources.displayMetrics.density).toInt(), 0,
                0, 0
            )
            isClickable = true
            isFocusable = true
            setBackgroundResource(android.R.drawable.list_selector_background)
            setOnClickListener {
                val updated = getRecentSearchList().toMutableList().also { it.remove(query) }
                saveRecentSearchList(updated)
                loadRecentSearches()
            }
        }

        row.addView(clockIcon)
        row.addView(queryText)
        row.addView(removeBtn)

        row.setOnClickListener {
            searchEditText.setText(query)
            searchEditText.setSelection(query.length)
        }

        recentSearchesContainer.addView(row)

        // Divider
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                marginStart = (52 * resources.displayMetrics.density).toInt()
            }
            setBackgroundColor(resources.getColor(R.color.light_gray, theme))
        }
        recentSearchesContainer.addView(divider)
    }

    private fun saveRecentSearch(query: String) {
        if (query.isBlank()) return
        val list = getRecentSearchList().toMutableList()
        list.remove(query)            // remove duplicate if exists
        list.add(0, query)            // add to front
        saveRecentSearchList(list.take(MAX_RECENT))
    }

    private fun getRecentSearchList(): List<String> {
        val raw = getSharedPreferences(PREFS_RECENT, MODE_PRIVATE)
            .getString(KEY_RECENT_SEARCHES, "") ?: ""
        return if (raw.isEmpty()) emptyList()
        else raw.split(";").filter { it.isNotBlank() }
    }

    private fun saveRecentSearchList(list: List<String>) {
        getSharedPreferences(PREFS_RECENT, MODE_PRIVATE).edit()
            .putString(KEY_RECENT_SEARCHES, list.joinToString(";"))
            .apply()
    }

    private fun performSearch(query: String) {
        val safeQuery = query.sanitize()
        if (safeQuery.isEmpty()) return

        searchProgressBar.visibility = View.VISIBLE
        searchResultsRecyclerView.visibility = View.GONE
        emptySearchResultsLayout.visibility = View.GONE

        val url = ApiConfig.buildUrl(ApiConfig.SEARCH_POSTS, "keyword" to safeQuery)

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                searchProgressBar.visibility = View.GONE
                searchResultsRecyclerView.visibility = View.VISIBLE
                try {
                    val jsonStart = response.indexOf("{")
                    if (jsonStart != -1) {
                        val json = JSONObject(response.substring(jsonStart))

                        if (json.optBoolean("success", false)) {
                            val postsJson = json.optJSONArray("posts")
                            val postsList = mutableListOf<Post>()

                            if (postsJson != null) {
                                for (i in 0 until postsJson.length()) {
                                    try {
                                        postsList.add(Post.fromJson(postsJson.getJSONObject(i)))
                                    } catch (e: Exception) { /* skip malformed post */ }
                                }
                            }

                            allSearchResults = postsList
                            typeFilterChipGroup.visibility = View.VISIBLE
                            typeFilterChipGroup.check(R.id.chipFilterAll)
                            currentTypeFilter = "All"
                            applyTypeFilter()
                            saveRecentSearch(safeQuery)

                            if (postsList.isEmpty()) {
                                searchResultsRecyclerView.visibility = View.GONE
                                emptySearchResultsLayout.visibility = View.VISIBLE
                                (emptySearchResultsLayout.findViewById<TextView>(R.id.emptySearchTitle))
                                    ?.text = "No results for \"$query\""
                            }
                        } else {
                            Toast.makeText(this, json.optString("message", "Search failed"), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing search results", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                searchProgressBar.visibility = View.GONE
                searchResultsRecyclerView.visibility = View.VISIBLE
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun navigateToHome() { startActivity(Intent(this, HomeActivity::class.java)) }
    private fun navigateToPostItem() { startActivity(Intent(this, PostItemActivity::class.java)) }
    private fun navigateToChatSection() { startActivity(Intent(this, ChatSectionActivity::class.java)) }
    private fun navigateToMenu() { startActivity(Intent(this, MenuActivity::class.java)) }
}
