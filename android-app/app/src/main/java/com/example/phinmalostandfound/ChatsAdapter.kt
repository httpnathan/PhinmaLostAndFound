package com.example.phinmalostandfound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class ChatItem(
    val otherUserId: String,
    val firstName: String,
    val lastName: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int
)

class ChatsAdapter(
    private val chats: List<ChatItem>,
    private val onChatClick: (ChatItem) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.chatUserNameTextView)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMessageTextView)
        val messageTime: TextView = itemView.findViewById(R.id.messageTimeTextView)
        val unreadBadge: TextView = itemView.findViewById(R.id.unreadBadgeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.userName.text = "${chat.firstName} ${chat.lastName}".trim()
        holder.lastMessage.text = chat.lastMessage.ifEmpty { "No messages yet" }
        holder.messageTime.text = formatRelativeTime(chat.lastMessageTime)

        if (chat.unreadCount > 0) {
            holder.unreadBadge.visibility = View.VISIBLE
            holder.unreadBadge.text = if (chat.unreadCount > 9) "9+" else chat.unreadCount.toString()
        } else {
            holder.unreadBadge.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onChatClick(chat) }
    }

    override fun getItemCount() = chats.size

    private fun formatRelativeTime(timestamp: String): String {
        if (timestamp.isEmpty()) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(timestamp) ?: return timestamp.take(10)
            val now = System.currentTimeMillis()
            val diff = now - date.time
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
