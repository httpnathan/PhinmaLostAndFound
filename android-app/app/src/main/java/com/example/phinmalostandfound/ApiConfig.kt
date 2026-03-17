package com.example.phinmalostandfound

/**
 * ============================================================================
 * API CONFIGURATION
 * ============================================================================
 * This file contains API endpoint URLs
 * 
 * 🔧 WHAT TO EDIT:
 * Change BASE_URL to your computer's IP address
 * 
 * HOW TO FIND YOUR IP:
 * - Windows: CMD → ipconfig → look for IPv4
 * - Mac: System Preferences → Network → IP Address
 * - Linux: Terminal → ifconfig or ip addr
 * ============================================================================
 */

object ApiConfig {
    
    // 🔧 CHANGE THIS to your computer's IP address
    // Example: "http://192.168.1.100/phinma-api/backend/"
    private const val BASE_URL = "http://192.168.1.30/phinma-api/backend/"
    
    // Authentication Endpoints
    const val LOGIN = "${BASE_URL}auth.php?action=login"
    const val REGISTER = "${BASE_URL}auth.php?action=register"
    const val ADMIN_LOGIN = "${BASE_URL}auth.php?action=admin_login"
    
    // User Endpoints
    const val GET_USER_PROFILE = "${BASE_URL}auth.php?action=get_profile"
    const val UPDATE_PROFILE = "${BASE_URL}auth.php?action=update_profile"
    const val CHANGE_PASSWORD = "${BASE_URL}auth.php?action=change_password"
    
    // Posts Endpoints
    const val CREATE_POST = "${BASE_URL}posts.php?action=create"
    const val GET_ALL_POSTS = "${BASE_URL}posts.php?action=get_all"
    const val GET_POST = "${BASE_URL}posts.php?action=get_one"
    const val UPDATE_POST = "${BASE_URL}posts.php?action=update"
    const val DELETE_POST = "${BASE_URL}posts.php?action=delete"
    const val SEARCH_POSTS = "${BASE_URL}posts.php?action=search"
    const val GET_USER_POSTS = "${BASE_URL}posts.php?action=get_user_posts"
    const val REPORT_POST = "${BASE_URL}posts.php?action=report"
    
    // Messages Endpoints
    const val SEND_MESSAGE = "${BASE_URL}messages.php?action=send"
    const val GET_CONVERSATION = "${BASE_URL}messages.php?action=get_conversation"
    const val GET_CHATS = "${BASE_URL}messages.php?action=get_chats"
    const val MARK_READ = "${BASE_URL}messages.php?action=mark_read"
    const val DELETE_CONVERSATION = "${BASE_URL}messages.php?action=delete_conversation"
    const val SEND_CHAT_IMAGE = "${BASE_URL}messages.php?action=send_image"
    
    // Notifications Endpoints
    const val UPDATE_FCM_TOKEN = "${BASE_URL}auth.php?action=update_fcm_token"
    
    /**
     * Build URL with query parameters
     * Example: buildUrl(GET_POST, "post_id" to "1")
     */
    fun buildUrl(baseUrl: String, vararg params: Pair<String, String>): String {
        if (params.isEmpty()) return baseUrl

        val queryParams = params.joinToString("&") {
            "${java.net.URLEncoder.encode(it.first, "UTF-8")}=${java.net.URLEncoder.encode(it.second, "UTF-8")}"
        }
        val separator = if (baseUrl.contains("?")) "&" else "?"
        return "$baseUrl$separator$queryParams"
    }
}
