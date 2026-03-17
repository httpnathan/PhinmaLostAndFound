package com.example.phinmalostandfound

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isOffline = MutableLiveData<Boolean>(false)
    val isOffline: LiveData<Boolean> = _isOffline

    private val PREFS_CACHE = "PhinmaCache"
    private val KEY_POSTS_CACHE = "posts_json_cache"

    private var allPosts: List<Post> = listOf()
    private var currentFilter = "All"
    private var currentSort = "newest"

    private val PAGE_SIZE = 50
    private var currentOffset = 0
    var isLastPage = false
        private set

    fun loadPosts() {
        currentOffset = 0
        isLastPage = false

        if (!isNetworkAvailable()) {
            _isOffline.value = true
            val cached = loadPostsFromCache()
            if (cached != null) {
                try {
                    val postsJson = JSONObject(cached).optJSONArray("posts")
                    val list = mutableListOf<Post>()
                    if (postsJson != null) for (i in 0 until postsJson.length()) {
                        try { list.add(parsePost(postsJson.getJSONObject(i))) } catch (e: Exception) {}
                    }
                    allPosts = list
                    currentOffset = list.size
                    if (list.size < PAGE_SIZE) isLastPage = true
                    applyFilterAndSort()
                } catch (e: Exception) {
                    _error.value = "Offline — no cached data available"
                }
            } else {
                _error.value = "Offline — no cached data available"
            }
            _isLoading.value = false
            return
        }

        _isOffline.value = false
        _isLoading.value = true
        val url = ApiConfig.buildUrl(ApiConfig.GET_ALL_POSTS, "limit" to PAGE_SIZE.toString(), "offset" to "0")
        fetchPosts(url, isFirstPage = true) { posts ->
            allPosts = posts
            currentOffset = posts.size
            if (posts.size < PAGE_SIZE) isLastPage = true
            applyFilterAndSort()
        }
    }

    fun loadMorePosts() {
        if (_isLoadingMore.value == true || isLastPage || _isOffline.value == true) return
        _isLoadingMore.value = true
        val url = ApiConfig.buildUrl(ApiConfig.GET_ALL_POSTS, "limit" to PAGE_SIZE.toString(), "offset" to currentOffset.toString())
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                _isLoadingMore.value = false
                try {
                    val jsonStart = response.indexOf("{")
                    if (jsonStart != -1) {
                        val json = JSONObject(response.substring(jsonStart))
                        if (json.optBoolean("success", false)) {
                            val postsJson = json.optJSONArray("posts")
                            if (postsJson != null) {
                                val newPosts = mutableListOf<Post>()
                                for (i in 0 until postsJson.length()) {
                                    try {
                                        newPosts.add(parsePost(postsJson.getJSONObject(i)))
                                    } catch (e: Exception) { /* skip */ }
                                }
                                if (newPosts.size < PAGE_SIZE) isLastPage = true
                                allPosts = allPosts + newPosts
                                currentOffset += newPosts.size
                                applyFilterAndSort()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error loading more posts", e)
                }
            },
            { error ->
                _isLoadingMore.value = false
                Log.e("HomeViewModel", "Network error loading more", error)
            }
        )
        AppSingleton.getRequestQueue(getApplication()).add(request)
    }

    private fun fetchPosts(url: String, isFirstPage: Boolean = false, onSuccess: (List<Post>) -> Unit) {
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                _isLoading.value = false
                try {
                    val jsonStart = response.indexOf("{")
                    if (jsonStart == -1) {
                        _error.value = "Invalid server response"
                    } else {
                        val rawJson = response.substring(jsonStart)
                        val json = JSONObject(rawJson)
                        if (json.optBoolean("success", false)) {
                            val postsJson = json.optJSONArray("posts")
                            if (postsJson == null) {
                                _error.value = "Unexpected response structure"
                            } else {
                                if (isFirstPage) savePostsToCache(rawJson)
                                val postsList = mutableListOf<Post>()
                                for (i in 0 until postsJson.length()) {
                                    try {
                                        postsList.add(parsePost(postsJson.getJSONObject(i)))
                                    } catch (e: Exception) {
                                        Log.e("HomeViewModel", "Error parsing post at index $i", e)
                                    }
                                }
                                onSuccess(postsList)
                            }
                        } else {
                            _error.value = json.optString("message", "Failed to load posts")
                        }
                    }
                } catch (e: Exception) {
                    _error.value = "Response format error"
                }
            },
            { _ ->
                _isLoading.value = false
                _isOffline.value = true
                val cached = loadPostsFromCache()
                if (isFirstPage && cached != null) {
                    try {
                        val postsJson = JSONObject(cached).optJSONArray("posts")
                        val list = mutableListOf<Post>()
                        if (postsJson != null) for (i in 0 until postsJson.length()) {
                            try { list.add(parsePost(postsJson.getJSONObject(i))) } catch (e: Exception) {}
                        }
                        onSuccess(list)
                    } catch (e: Exception) {
                        _error.value = "Could not connect — check your Wi-Fi"
                    }
                } else {
                    _error.value = "Could not connect — check your Wi-Fi"
                }
            }
        )
        AppSingleton.getRequestQueue(getApplication()).add(request)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnectedOrConnecting == true
        }
    }

    private fun savePostsToCache(jsonStr: String) {
        getApplication<Application>().getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE)
            .edit().putString(KEY_POSTS_CACHE, jsonStr).apply()
    }

    private fun loadPostsFromCache(): String? {
        return getApplication<Application>().getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE)
            .getString(KEY_POSTS_CACHE, null)
    }

    fun applyFilter(filter: String) {
        currentFilter = filter
        applyFilterAndSort()
    }

    fun setSortOrder(sort: String) {
        currentSort = sort
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        val filtered = when (currentFilter) {
            "Lost" -> allPosts.filter { it.postType.lowercase() == "lost" }
            "Found" -> allPosts.filter { it.postType.lowercase() == "found" }
            else -> allPosts
        }
        val sorted = when (currentSort) {
            "oldest" -> filtered.sortedBy { it.createdAt }
            else -> filtered.sortedByDescending { it.createdAt }
        }
        _posts.value = sorted
    }

    private fun parsePost(postObj: JSONObject): Post = Post.fromJson(postObj)
}
