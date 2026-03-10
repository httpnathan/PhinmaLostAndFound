package com.example.phinmalostandfound

import org.json.JSONArray

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
    val status: String,
    val dateLostFound: String,
    val contactNumber: String?,
    val imageUrls: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val postedBy: String
)

fun jsonArrayToList(jsonArray: JSONArray): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until jsonArray.length()) {
        list.add(jsonArray.getString(i))
    }
    return list
}