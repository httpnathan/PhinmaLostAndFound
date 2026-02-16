package com.example.phinmalostandfound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * ============================================================================
 * IMAGE PAGER ADAPTER
 * ============================================================================
 * Displays a list of image URLs in a ViewPager2
 * Glide (or any loader) is passed via lambda
 * ============================================================================
 */
class ImagePagerAdapter(
    private val imageUrls: List<String>,
    private val imageLoader: (String, ImageView) -> Unit
) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = imageUrls[position]
        imageLoader(url, holder.imageView)
    }

    override fun getItemCount() = imageUrls.size
}