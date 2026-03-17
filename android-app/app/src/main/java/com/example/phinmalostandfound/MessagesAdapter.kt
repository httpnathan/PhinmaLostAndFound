package com.example.phinmalostandfound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val messageId: Int,
    val senderId: Int,
    val messageText: String,
    val sentAt: String,
    val isRead: Boolean = false,
    val imageUrl: String? = null
)

private sealed class MessageListItem {
    data class DateHeader(val label: String) : MessageListItem()
    data class MessageRow(val message: Message) : MessageListItem()
}

class MessagesAdapter(
    private val currentUserId: Int
) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    private val items = mutableListOf<MessageListItem>()

    companion object {
        private const val TYPE_DATE_HEADER = 0
        private const val TYPE_MESSAGE = 1
        const val IMAGE_PREFIX = "[IMAGE]"
    }

    fun submitMessages(messages: List<Message>) {
        items.clear()
        var lastDateLabel = ""
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateSdf.format(Date())
        val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -1) }
        val yesterday = dateSdf.format(cal.time)
        val displaySdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

        for (msg in messages) {
            val dateLabel = try {
                val msgDate = msg.sentAt.substring(0, 10)
                when (msgDate) {
                    today -> "Today"
                    yesterday -> "Yesterday"
                    else -> displaySdf.format(dateSdf.parse(msgDate) ?: Date())
                }
            } catch (e: Exception) { "" }

            if (dateLabel != lastDateLabel && dateLabel.isNotEmpty()) {
                items.add(MessageListItem.DateHeader(dateLabel))
                lastDateLabel = dateLabel
            }
            items.add(MessageListItem.MessageRow(msg))
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is MessageListItem.DateHeader -> TYPE_DATE_HEADER
        is MessageListItem.MessageRow -> TYPE_MESSAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = items[position]) {
            is MessageListItem.DateHeader -> {
                holder.dayHeaderLayout.visibility = View.VISIBLE
                holder.dayHeaderText.text = item.label
                holder.sentMessageLayout.visibility = View.GONE
                holder.receivedMessageLayout.visibility = View.GONE
            }
            is MessageListItem.MessageRow -> {
                holder.dayHeaderLayout.visibility = View.GONE
                val msg = item.message
                val imageUrl = msg.imageUrl ?: if (msg.messageText.startsWith(IMAGE_PREFIX))
                    msg.messageText.removePrefix(IMAGE_PREFIX) else null
                val isImage = imageUrl != null
                val timeDisplay = formatTime(msg.sentAt)

                if (msg.senderId == currentUserId) {
                    holder.sentMessageLayout.visibility = View.VISIBLE
                    holder.receivedMessageLayout.visibility = View.GONE

                    if (isImage) {
                        holder.sentMessageText.visibility = View.GONE
                        holder.sentImageView.visibility = View.VISIBLE
                        Glide.with(holder.itemView.context).load(imageUrl).centerCrop()
                            .placeholder(R.drawable.ic_image_placeholder).into(holder.sentImageView)
                    } else {
                        holder.sentMessageText.visibility = View.VISIBLE
                        holder.sentImageView.visibility = View.GONE
                        holder.sentMessageText.text = msg.messageText
                    }

                    holder.sentMessageTime.text = timeDisplay
                    holder.readReceiptText.text = if (msg.isRead) "✓✓" else "✓"
                    holder.readReceiptText.setTextColor(
                        if (msg.isRead)
                            holder.itemView.context.getColor(R.color.phinma_dark_green)
                        else
                            holder.itemView.context.getColor(R.color.text_secondary)
                    )
                } else {
                    holder.sentMessageLayout.visibility = View.GONE
                    holder.receivedMessageLayout.visibility = View.VISIBLE

                    if (isImage) {
                        holder.receivedMessageText.visibility = View.GONE
                        holder.receivedImageView.visibility = View.VISIBLE
                        Glide.with(holder.itemView.context).load(imageUrl).centerCrop()
                            .placeholder(R.drawable.ic_image_placeholder).into(holder.receivedImageView)
                    } else {
                        holder.receivedMessageText.visibility = View.VISIBLE
                        holder.receivedImageView.visibility = View.GONE
                        holder.receivedMessageText.text = msg.messageText
                    }

                    holder.receivedMessageTime.text = timeDisplay
                }
            }
        }
    }

    override fun getItemCount() = items.size

    private fun formatTime(sentAt: String): String {
        if (sentAt.length < 16) return sentAt
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(sentAt) ?: return sentAt.substring(11, 16)
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            if (sentAt.length >= 16) sentAt.substring(11, 16) else sentAt
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayHeaderLayout: LinearLayout = itemView.findViewById(R.id.dayHeaderLayout)
        val dayHeaderText: TextView = itemView.findViewById(R.id.dayHeaderText)
        val sentMessageLayout: LinearLayout = itemView.findViewById(R.id.sentMessageLayout)
        val receivedMessageLayout: LinearLayout = itemView.findViewById(R.id.receivedMessageLayout)
        val sentMessageText: TextView = itemView.findViewById(R.id.sentMessageText)
        val sentMessageTime: TextView = itemView.findViewById(R.id.sentMessageTime)
        val sentImageView: ImageView = itemView.findViewById(R.id.sentImageView)
        val readReceiptText: TextView = itemView.findViewById(R.id.readReceiptText)
        val receivedMessageText: TextView = itemView.findViewById(R.id.receivedMessageText)
        val receivedMessageTime: TextView = itemView.findViewById(R.id.receivedMessageTime)
        val receivedImageView: ImageView = itemView.findViewById(R.id.receivedImageView)
    }
}
