package com.example.phinmalostandfound

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.phinmalostandfound.databinding.ActivityHomeBinding
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter
    private var isGalleryView = false

    private val PREFS = "PhinmaLostAndFound"
    private val KEY_GALLERY_VIEW = "isGalleryView"

    private var latestKnownCreatedAt = ""
    private val newPostsHandler = Handler(Looper.getMainLooper())
    private val newPostsRunnable = object : Runnable {
        override fun run() {
            checkForNewPosts()
            newPostsHandler.postDelayed(this, 60_000)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isGalleryView = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_GALLERY_VIEW, false)

        setupRecyclerView()
        setupObservers()
        setupBottomNavigation()
        setupClickListeners()
        setupInfiniteScroll()

        askNotificationPermission()
        setupNotificationPolling()

        handleDeepLink(intent)
        viewModel.loadPosts()

        binding.bottomNavigation.elevation = 0f
        binding.scanFab.bringToFront()
    }

    override fun onResume() {
        super.onResume()
        newPostsHandler.postDelayed(newPostsRunnable, 30_000)
    }

    override fun onPause() {
        super.onPause()
        newPostsHandler.removeCallbacks(newPostsRunnable)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        updateLayoutManager()
        binding.postsRecyclerView.adapter = postAdapter
        binding.postsRecyclerView.setHasFixedSize(true)
    }

    private fun updateLayoutManager() {
        binding.postsRecyclerView.layoutManager = if (isGalleryView) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
        postAdapter.setViewType(if (isGalleryView) PostAdapter.VIEW_TYPE_GALLERY else PostAdapter.VIEW_TYPE_LIST)
    }

    private fun setupInfiniteScroll() {
        binding.postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lastVisible = when (val lm = recyclerView.layoutManager) {
                    is LinearLayoutManager -> lm.findLastVisibleItemPosition()
                    is GridLayoutManager -> lm.findLastVisibleItemPosition()
                    else -> return
                }
                val total = recyclerView.adapter?.itemCount ?: return
                if (lastVisible >= total - 5) {
                    viewModel.loadMorePosts()
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.posts.observe(this) { posts ->
            postAdapter.submitList(posts)
            val isEmpty = posts.isEmpty()
            binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.postsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE

            posts.firstOrNull()?.createdAt?.let { created ->
                if (latestKnownCreatedAt.isEmpty()) latestKnownCreatedAt = created
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                if (!binding.swipeRefreshLayout.isRefreshing) {
                    binding.shimmerViewContainer.visibility = View.VISIBLE
                    binding.shimmerViewContainer.startShimmer()
                    binding.postsRecyclerView.visibility = View.GONE
                }
            } else {
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                if (viewModel.posts.value?.isNotEmpty() == true) {
                    binding.postsRecyclerView.visibility = View.VISIBLE
                }
            }
        }

        viewModel.error.observe(this) { error ->
            if (!error.isNullOrEmpty()) Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }

        viewModel.isOffline.observe(this) { offline ->
            binding.offlineBanner.visibility = if (offline) View.VISIBLE else View.GONE
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_post -> { startActivity(Intent(this, PostItemActivity::class.java)); true }
                R.id.nav_chat -> { startActivity(Intent(this, ChatSectionActivity::class.java)); true }
                R.id.nav_menu -> { startActivity(Intent(this, MenuActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun updateViewModeIcon() {
        binding.viewModeToggle.setImageResource(
            if (isGalleryView) R.drawable.ic_list_view else R.drawable.ic_grid_view
        )
    }

    private fun setupClickListeners() {
        updateViewModeIcon()

        binding.viewModeToggle.setOnClickListener {
            isGalleryView = !isGalleryView
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putBoolean(KEY_GALLERY_VIEW, isGalleryView)
                .apply()
            updateLayoutManager()
            updateViewModeIcon()
            postAdapter.notifyDataSetChanged()
        }

        binding.notificationIcon.setOnClickListener {
            NotificationHelper.markAllRead(this)
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        binding.searchCardView.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.filterButton.setOnClickListener { showFilterDialog() }
        binding.sortButton.setOnClickListener { showSortDialog() }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.newPostsBanner.visibility = View.GONE
            latestKnownCreatedAt = ""
            viewModel.loadPosts()
        }

        binding.newPostsBanner.setOnClickListener {
            binding.newPostsBanner.visibility = View.GONE
            latestKnownCreatedAt = ""
            viewModel.loadPosts()
            binding.postsRecyclerView.scrollToPosition(0)
        }

        binding.scanFab.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
    }

    private fun showFilterDialog() {
        val options = arrayOf("All Items", "Lost Items", "Found Items")
        AlertDialog.Builder(this)
            .setTitle("Filter Posts")
            .setItems(options) { _, which ->
                val filter = when (which) { 1 -> "Lost"; 2 -> "Found"; else -> "All" }
                binding.filterButton.text = filter
                viewModel.applyFilter(filter)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSortDialog() {
        val options = arrayOf("Newest First", "Oldest First")
        AlertDialog.Builder(this)
            .setTitle("Sort Posts")
            .setItems(options) { _, which ->
                val (key, label) = if (which == 1) "oldest" to "Oldest" else "newest" to "Newest"
                binding.sortButton.text = label
                viewModel.setSortOrder(key)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkForNewPosts() {
        if (latestKnownCreatedAt.isEmpty()) return
        val url = ApiConfig.buildUrl(ApiConfig.GET_ALL_POSTS, "limit" to "1", "offset" to "0")
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val idx = response.indexOf("{")
                    if (idx != -1) {
                        val json = JSONObject(response.substring(idx))
                        if (json.optBoolean("success", false)) {
                            val arr = json.optJSONArray("posts")
                            if (arr != null && arr.length() > 0) {
                                val newest = arr.getJSONObject(0).optString("created_at", "")
                                if (newest.isNotEmpty() && newest > latestKnownCreatedAt) {
                                    binding.newPostsBanner.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                } catch (e: Exception) { /* ignore */ }
            },
            { /* ignore */ }
        )
        AppSingleton.getRequestQueue(this).add(request)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupNotificationPolling() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val pollingRequest = PeriodicWorkRequestBuilder<NotificationPollingWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "NotificationPolling",
            ExistingPeriodicWorkPolicy.KEEP,
            pollingRequest
        )
    }

    private fun handleDeepLink(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null && data.pathSegments.size > 0) {
            if (data.pathSegments[0] == "post") {
                val postId = data.lastPathSegment?.toIntOrNull()
                if (postId != null) {
                    startActivity(Intent(this, PostDetailActivity::class.java).apply {
                        putExtra("POST_ID", postId)
                    })
                }
            }
        }
    }
}
