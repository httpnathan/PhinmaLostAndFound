package com.example.phinmalostandfound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Message(
    val messageId: Int,
    val senderId: Int,
    val messageText: String,
    val sentAt: String
)

class MessagesAdapter(
    private val messages: List<Message>,
    private val currentUserId: Int
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentLayout: LinearLayout = itemView.findViewById(R.id.sentMessageLayout)
        val receivedLayout: LinearLayout = itemView.findViewById(R.id.receivedMessageLayout)
        val sentText: TextView = itemView.findViewById(R.id.sentMessageText)
        val sentTime: TextView = itemView.findViewById(R.id.sentMessageTime)
        val receivedText: TextView = itemView.findViewById(R.id.receivedMessageText)
        val receivedTime: TextView = itemView.findViewById(R.id.receivedMessageTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messages[position]
        val timeDisplay = msg.sentAt.take(16)

        if (msg.senderId == currentUserId) {
            holder.sentLayout.visibility = View.VISIBLE
            holder.receivedLayout.visibility = View.GONE
            holder.sentText.text = msg.messageText
            holder.sentTime.text = timeDisplay
        } else {
            holder.sentLayout.visibility = View.GONE
            holder.receivedLayout.visibility = View.VISIBLE
            holder.receivedText.text = msg.messageText
            holder.receivedTime.text = timeDisplay
        }
    }

    override fun getItemCount() = messages.size
}
