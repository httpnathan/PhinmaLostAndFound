package com.example.phinmalostandfound

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MessagePollingWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = appContext.getSharedPreferences("PhinmaLostAndFound", Context.MODE_PRIVATE)
            val userId = prefs.getInt("userId", -1)
            if (userId == -1) return@withContext Result.success()

            val urlStr = "${ApiConfig.GET_CHATS}&user_id=$userId"
            val conn = URL(urlStr).openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                return@withContext Result.retry()
            }

            val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            conn.disconnect()

            val jsonStart = response.indexOf("{")
            if (jsonStart == -1) return@withContext Result.success()
            val json = JSONObject(response.substring(jsonStart))

            if (json.optBoolean("success", false)) {
                val chatsJson = json.optJSONArray("data") ?: return@withContext Result.success()

                var totalUnread = 0
                for (i in 0 until chatsJson.length()) {
                    totalUnread += chatsJson.getJSONObject(i).optInt("unread_count", 0)
                }

                val lastUnread = NotificationHelper.getLastUnreadCount(appContext)
                if (totalUnread > lastUnread) {
                    // Find first chat with unread messages and notify
                    for (i in 0 until chatsJson.length()) {
                        val c = chatsJson.getJSONObject(i)
                        if (c.optInt("unread_count", 0) > 0) {
                            val firstName = c.optString("first_name", "Someone")
                            val lastName = c.optString("last_name", "")
                            val senderName = "$firstName $lastName".trim()
                            val lastMsg = c.optString("last_message", "New message")
                            NotificationHelper.showMessageNotification(appContext, senderName, lastMsg)
                            break
                        }
                    }
                }
                NotificationHelper.saveLastUnreadCount(appContext, totalUnread)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
