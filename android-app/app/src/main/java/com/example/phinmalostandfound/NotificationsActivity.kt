package com.example.phinmalostandfound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class NotificationItem(
    val title: String,
    val body: String,
    val type: String,
    val time: String,
    val isRead: Boolean
)

class NotificationsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: View
    private lateinit var markAllReadButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        recyclerView = findViewById(R.id.notificationsRecyclerView)
        emptyView = findViewById(R.id.emptyNotificationsTextView)
        markAllReadButton = findViewById(R.id.markAllReadButton)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        markAllReadButton.setOnClickListener {
            NotificationHelper.markAllRead(this)
            loadNotifications()
            Toast.makeText(this, "All marked as read", Toast.LENGTH_SHORT).show()
        }

        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }

    private fun loadNotifications() {
        val inbox = NotificationHelper.getInbox(this)
        val items = mutableListOf<NotificationItem>()

        for (i in 0 until inbox.length()) {
            val obj = inbox.getJSONObject(i)
            items.add(
                NotificationItem(
                    title = obj.optString("title", ""),
                    body = obj.optString("body", ""),
                    type = obj.optString("type", ""),
                    time = obj.optString("time", ""),
                    isRead = obj.optBoolean("read", false)
                )
            )
        }

        if (items.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            markAllReadButton.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            markAllReadButton.visibility = View.VISIBLE
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = NotificationsAdapter(items)
        }
    }

    inner class NotificationsAdapter(private val items: List<NotificationItem>) :
        RecyclerView.Adapter<NotificationsAdapter.NotifViewHolder>() {

        inner class NotifViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.notifIconView)
            val title: TextView = view.findViewById(R.id.notifTitleTextView)
            val body: TextView = view.findViewById(R.id.notifBodyTextView)
            val time: TextView = view.findViewById(R.id.notifTimeTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NotifViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
            )

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.body.text = item.body
            holder.time.text = formatRelativeTime(item.time)

            val iconRes = when (item.type) {
                "message" -> R.drawable.ic_chat
                "match" -> R.drawable.ic_search
                "status" -> R.drawable.ic_info
                else -> R.drawable.ic_notifications
            }
            holder.icon.setImageResource(iconRes)

            holder.itemView.alpha = if (item.isRead) 0.55f else 1.0f
        }

        private fun formatRelativeTime(timestamp: String): String {
            if (timestamp.isEmpty()) return ""
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(timestamp) ?: return timestamp.take(10)
                val diff = System.currentTimeMillis() - date.time
                when {
                    diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                    diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
                    diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
                    diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
                    else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
                }
            } catch (e: Exception) {
                timestamp.take(10)
            }
        }
    }
}
