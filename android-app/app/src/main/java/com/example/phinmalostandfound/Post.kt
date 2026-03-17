package com.example.phinmalostandfound

import org.json.JSONArray
import org.json.JSONObject

data class Post(
    val postId: Int,
    val userId: Int,
    val postType: String,
    val itemName: String,
    val description: String,
    val category: String,
    val locationFound: String,
    val building: String,
    val floorNumber: String?,
    val status: String, // "active", "resolved"
    val dateLostFound: String,
    val contactNumber: String?,
    val imageUrls: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val postedBy: String,
    val securityQuestion: String? = null // For Proof of Ownership
) {
    companion object {
        fun fromJson(p: JSONObject): Post = Post(
            postId = p.optInt("post_id", 0),
            userId = p.optInt("user_id", 0),
            postType = p.optString("post_type", "unknown"),
            itemName = p.optString("item_name", "Unnamed Item"),
            description = p.optString("description", ""),
            category = p.optString("category", "General"),
            locationFound = p.optString("location_found", "Unknown"),
            building = p.optString("building", ""),
            floorNumber = if (p.isNull("floor_number")) null else p.optString("floor_number"),
            status = p.optString("status", "active"),
            dateLostFound = p.optString("date_lost_found", ""),
            contactNumber = if (p.isNull("contact_number")) null else p.optString("contact_number"),
            imageUrls = p.optJSONArray("image_urls")?.let { jsonArrayToList(it) } ?: emptyList(),
            createdAt = p.optString("created_at", ""),
            updatedAt = p.optString("updated_at", ""),
            postedBy = p.optString("posted_by", "Anonymous"),
            securityQuestion = p.optString("security_question", "").takeIf { it.isNotEmpty() }
        )
    }
}

fun jsonArrayToList(jsonArray: JSONArray): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until jsonArray.length()) {
        list.add(jsonArray.getString(i))
    }
    return list
}
