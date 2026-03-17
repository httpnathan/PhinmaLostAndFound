package com.example.phinmalostandfound

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.phinmalostandfound.databinding.ItemPostBinding
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter : ListAdapter<Post, RecyclerView.ViewHolder>(PostDiffCallback()) {

    companion object {
        const val VIEW_TYPE_LIST = 0
        const val VIEW_TYPE_GALLERY = 1
        private val TODAY_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val TODAY_STRING = TODAY_FMT.format(Date())
    }

    private var currentViewType = VIEW_TYPE_LIST

    fun setViewType(viewType: Int) {
        currentViewType = viewType
    }

    override fun getItemViewType(position: Int): Int = currentViewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_GALLERY) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_gallery, parent, false)
            GalleryViewHolder(view)
        } else {
            val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PostViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post = getItem(position)
        if (holder is PostViewHolder) holder.bind(post)
        else if (holder is GalleryViewHolder) holder.bind(post)
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                imageViewPager.adapter = ImagePagerAdapter(post.imageUrls)

                if (post.imageUrls.isEmpty()) {
                    imageCounter.visibility = View.GONE
                } else {
                    imageCounter.visibility = View.VISIBLE
                    imageCounter.text = "1/${post.imageUrls.size}"
                    imageViewPager.registerOnPageChangeCallback(object :
                        ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(pos: Int) {
                            imageCounter.text = "${pos + 1}/${post.imageUrls.size}"
                        }
                    })
                }

                postTypeBadge.text = post.postType.uppercase()
                postTypeBadge.setBackgroundColor(
                    if (post.postType.lowercase() == "lost") Color.parseColor("#D32F2F")
                    else Color.parseColor("#1B5E20")
                )

                postTitle.text = post.itemName
                postDescription.text = post.description
                lastSeenLocation.text = "${post.locationFound}, ${post.building}, Floor ${post.floorNumber ?: "-"}"
                contactPerson.text = post.contactNumber ?: "Not Provided"

                // Today badge: highlight timestamp in green if post date is today
                val isToday = post.dateLostFound.startsWith(TODAY_STRING)
                lastSeenTime.text = if (isToday) "Today, ${post.dateLostFound}" else post.dateLostFound
                lastSeenTime.setTextColor(
                    if (isToday) Color.parseColor("#1B5E20") else Color.parseColor("#757575")
                )

                if (post.status == "resolved") {
                    postTitle.text = "[RESOLVED] ${post.itemName}"
                    postTitle.setTextColor(Color.GRAY)
                    messageButton.visibility = View.GONE
                } else {
                    postTitle.setTextColor(Color.BLACK)
                    messageButton.visibility = View.VISIBLE
                }

                messageButton.setOnClickListener {
                    val intent = android.content.Intent(root.context, PrivateMessageActivity::class.java)
                    intent.putExtra("USER_ID", post.userId.toString())
                    intent.putExtra("USER_NAME", post.postedBy)
                    intent.putExtra("POST_TITLE", post.itemName)
                    intent.putExtra("SECURITY_QUESTION", post.securityQuestion)
                    root.context.startActivity(intent)
                }

                root.setOnClickListener {
                    val intent = android.content.Intent(root.context, PostDetailActivity::class.java)
                    intent.putExtra("POST_ID", post.postId)
                    root.context.startActivity(intent)
                }
            }
        }
    }

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.galleryImageView)
        private val title: TextView = itemView.findViewById(R.id.galleryPostTitle)
        private val location: TextView = itemView.findViewById(R.id.galleryPostLocation)
        private val typeBadge: TextView = itemView.findViewById(R.id.galleryPostTypeBadge)
        private val resolvedBadge: View = itemView.findViewById(R.id.resolvedBadge)

        fun bind(post: Post) {
            if (post.imageUrls.isNotEmpty()) {
                imageView.loadImage(post.imageUrls[0])
            } else {
                imageView.setImageResource(R.drawable.ic_image_placeholder)
            }
            title.text = post.itemName
            location.text = post.locationFound
            typeBadge.text = post.postType.uppercase()
            typeBadge.setBackgroundColor(
                if (post.postType.lowercase() == "lost") Color.parseColor("#D32F2F")
                else Color.parseColor("#1B5E20")
            )
            resolvedBadge.visibility = if (post.status == "resolved") View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                val intent = android.content.Intent(itemView.context, PostDetailActivity::class.java)
                intent.putExtra("POST_ID", post.postId)
                itemView.context.startActivity(intent)
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.postId == newItem.postId
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}
