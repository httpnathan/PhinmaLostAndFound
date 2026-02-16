package com.example.phinmalostfound

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide

/**
 * ============================================================================
 * POST ADAPTER
 * ============================================================================
 * Displays the list of lost/found posts in RecyclerView
 * Supports multiple images from URLs and full database fields
 * ============================================================================
 */
class PostAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewPager: ViewPager2 = itemView.findViewById(R.id.imageViewPager)
        val imageCounter: TextView = itemView.findViewById(R.id.imageCounter)
        val postTypeBadge: TextView = itemView.findViewById(R.id.postTypeBadge)
        val postTitle: TextView = itemView.findViewById(R.id.postTitle)
        val postDescription: TextView = itemView.findViewById(R.id.postDescription)
        val lastSeenLocation: TextView = itemView.findViewById(R.id.lastSeenLocation)
        val lastSeenTime: TextView = itemView.findViewById(R.id.lastSeenTime)
        val contactPerson: TextView = itemView.findViewById(R.id.contactPerson)
        val messageButton: Button = itemView.findViewById(R.id.messageButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Setup image scroller with URLs using Glide inside ImagePagerAdapter
        val imageAdapter = ImagePagerAdapter(post.imageUrls) { imageUrl, imageView ->
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .into(imageView)
        }
        holder.imageViewPager.adapter = imageAdapter

        // Update image counter when page changes
        holder.imageViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(pos: Int) {
                holder.imageCounter.text = "${pos + 1}/${post.imageUrls.size}"
            }
        })

        // Initial image counter
        holder.imageCounter.text = "1/${post.imageUrls.size}"

        // Set post type badge
        holder.postTypeBadge.text = post.postType.uppercase()
        if (post.postType.lowercase() == "lost") {
            holder.postTypeBadge.setBackgroundColor(Color.parseColor("#D32F2F"))
        } else {
            holder.postTypeBadge.setBackgroundColor(Color.parseColor("#1B5E20"))
        }

        // Set post details
        holder.postTitle.text = post.itemName
        holder.postDescription.text = post.description
        holder.lastSeenLocation.text =
            "${post.locationFound}, ${post.building}, Floor ${post.floorNumber ?: "-"}"
        holder.lastSeenTime.text = post.dateLostFound
        holder.contactPerson.text = post.contactNumber ?: "Not Provided"

        // Message button click - open private chat
        holder.messageButton.setOnClickListener {
            val intent =
                android.content.Intent(holder.itemView.context, PrivateMessageActivity::class.java)
            intent.putExtra("USER_NAME", post.postedBy)
            intent.putExtra("POST_TITLE", post.itemName)
            holder.itemView.context.startActivity(intent)
        }

        // Click on card to view full post details
        holder.itemView.setOnClickListener {
            val intent =
                android.content.Intent(holder.itemView.context, PostDetailActivity::class.java)
            intent.putExtra("POST_ID", post.postId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = posts.size
}