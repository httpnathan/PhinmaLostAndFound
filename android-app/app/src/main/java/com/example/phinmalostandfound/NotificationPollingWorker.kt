package com.example.phinmalostandfound

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * A background worker that polls the MariaDB/XAMPP server for new notifications.
 * This replaces Firebase for local server setups.
 */
class NotificationPollingWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences("PhinmaLostAndFound", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)
        
        if (userId == -1) return Result.success()

        val lastNotificationId = sharedPreferences.getInt("last_notif_id", 0)
        
        val url = ApiConfig.buildUrl(ApiConfig.GET_CHATS, 
            "action" to "get_updates", 
            "user_id" to userId.toString(),
            "since_id" to lastNotificationId.toString()
        )

        return try {
            val future = RequestFuture.newFuture<String>()
            val request = StringRequest(Request.Method.GET, url, future, future)
            AppSingleton.getRequestQueue(applicationContext).add(request)
            
            // Wait for response (Sync in worker thread)
            val response = future.get(10, TimeUnit.SECONDS)
            val json = JSONObject(response)
            
            if (json.optBoolean("success")) {
                val updates = json.optJSONArray("updates")
                if (updates != null && updates.length() > 0) {
                    var maxId = lastNotificationId
                    for (i in 0 until updates.length()) {
                        val update = updates.getJSONObject(i)
                        val updateId = update.optInt("id")
                        
                        sendNotification(
                            update.optString("title", "Phinma Lost & Found"),
                            update.optString("message", "You have a new update."),
                            updateId
                        )
                        
                        if (updateId > maxId) maxId = updateId
                    }
                    sharedPreferences.edit().putInt("last_notif_id", maxId).apply()
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(title: String, messageBody: String, id: Int) {
        val intent = Intent(applicationContext, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, id, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "local_updates_channel"
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "Application Updates", 
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(id, notificationBuilder.build())
    }
}
