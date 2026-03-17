package com.example.phinmalostandfound

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {

    const val CHANNEL_MESSAGES = "messages_channel"
    const val CHANNEL_POSTS = "posts_channel"

    private const val PREFS_NOTIFICATIONS = "PhinmaNotifications"
    private const val KEY_INBOX = "inbox"
    private const val KEY_LAST_UNREAD = "lastUnreadCount"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_MESSAGES, "Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "New message notifications"
                }
            )
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_POSTS, "Post Updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Post status and match notifications"
                }
            )
        }
    }

    fun showMessageNotification(context: Context, senderName: String, preview: String) {
        val prefs = context.getSharedPreferences("PhinmaSettings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifyMessages", true)) return

        val intent = Intent(context, ChatSectionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(senderName)
            .setContentText(preview)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), notification)

        saveToInbox(context, "New Message from $senderName", preview, "message")
    }

    fun showPostMatchNotification(context: Context, itemName: String) {
        val prefs = context.getSharedPreferences("PhinmaSettings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifyMatches", true)) return

        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_POSTS)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Possible Match Found!")
            .setContentText("A found item may match your lost: $itemName")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), notification)

        saveToInbox(context, "Possible Match Found", "A found item may match your lost: $itemName", "match")
    }

    fun showStatusNotification(context: Context, itemName: String, newStatus: String) {
        val prefs = context.getSharedPreferences("PhinmaSettings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifyStatus", true)) return

        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_POSTS)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Post Status Updated")
            .setContentText("Your post \"$itemName\" is now $newStatus")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), notification)

        saveToInbox(context, "Post Status Updated", "Your post \"$itemName\" is now $newStatus", "status")
    }

    fun saveToInbox(context: Context, title: String, body: String, type: String) {
        val prefs = context.getSharedPreferences(PREFS_NOTIFICATIONS, Context.MODE_PRIVATE)
        val arr = try { JSONArray(prefs.getString(KEY_INBOX, "[]")) } catch (e: Exception) { JSONArray() }
        val item = JSONObject().apply {
            put("title", title)
            put("body", body)
            put("type", type)
            put("time", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            put("read", false)
        }
        // Prepend newest first, keep max 50
        val newArr = JSONArray()
        newArr.put(item)
        for (i in 0 until minOf(arr.length(), 49)) newArr.put(arr.getJSONObject(i))
        prefs.edit().putString(KEY_INBOX, newArr.toString()).apply()
    }

    fun getInbox(context: Context): JSONArray {
        val prefs = context.getSharedPreferences(PREFS_NOTIFICATIONS, Context.MODE_PRIVATE)
        return try { JSONArray(prefs.getString(KEY_INBOX, "[]")) } catch (e: Exception) { JSONArray() }
    }

    fun getUnreadCount(context: Context): Int {
        val arr = getInbox(context)
        var count = 0
        for (i in 0 until arr.length()) {
            if (!arr.getJSONObject(i).optBoolean("read", false)) count++
        }
        return count
    }

    fun markAllRead(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NOTIFICATIONS, Context.MODE_PRIVATE)
        val arr = getInbox(context)
        for (i in 0 until arr.length()) arr.getJSONObject(i).put("read", true)
        prefs.edit().putString(KEY_INBOX, arr.toString()).apply()
    }

    fun clearInbox(context: Context) {
        context.getSharedPreferences(PREFS_NOTIFICATIONS, Context.MODE_PRIVATE)
            .edit().remove(KEY_INBOX).apply()
    }

    fun getLastUnreadCount(context: Context): Int {
        return context.getSharedPreferences(PREFS_NOTIFICATIONS, Context.MODE_PRIVATE)
            .getInt(KEY_LAST_UNREAD, 0)
    }

    fun saveLastUnreadCount(context: Context, count: Int) {
        context.getSharedPreferences(PREFS_NOTIFICATIONS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_LAST_UNREAD, count).apply()
    }
}
