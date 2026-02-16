package com.example.phinmalostfound

import org.json.JSONArray

/**
 * ============================================================================
 * POST DATA CLASS
 * ============================================================================
 * Represents a lost/found item post fetched from the backend.
 *
 * Fields match the posts table:
 * - Multiple image URLs (from post_images table or comma-separated field)
 * - Optional contact number
 * - User info (postedBy)
 * ============================================================================
 */
data class Post(
    val postId: Int,
    val userId: Int,
    val postType: String,                 // "lost" or "found"
    val itemName: String,                 // Title / item name
    val description: String,
    val category: String,
    val locationFound: String,            // Dropdown location
    val building: String,
    val floorNumber: String?,             // Optional
    val status: String,                   // "active", "claimed", etc.
    val dateLostFound: String,            // DATETIME from database
    val contactNumber: String?,           // Optional
    val imageUrls: List<String> = emptyList(), // Multiple images
    val createdAt: String,
    val updatedAt: String,
    val postedBy: String                  // Username (from users table)
)

/**
 * ============================================================================
 * HELPER FUNCTION
 * ============================================================================
 * Converts a JSONArray of image URLs from backend into a List<String>
 *
 * Example usage:
 * val imageList = jsonArrayToList(json.getJSONArray("image_urls"))
 * ============================================================================
 */
fun jsonArrayToList(jsonArray: JSONArray): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until jsonArray.length()) {
        list.add(jsonArray.getString(i))
    }
    return list
}