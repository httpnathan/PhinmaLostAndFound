package com.example.phinmalostandfound

import android.text.format.DateUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized image loading using Glide.
 */
fun ImageView.loadImage(url: String?) {
    Glide.with(this.context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(R.drawable.ic_image_placeholder)
        .error(R.drawable.ic_error)
        .into(this)
}

/**
 * Converts a date string (YYYY-MM-DD HH:MM:SS) to a relative time string (e.g., "2 hours ago").
 */
/**
 * Strips leading/trailing whitespace and removes characters that have no place in
 * user-supplied text fields (null bytes, ASCII control chars except tab/newline).
 */
fun String.sanitize(): String =
    this.trim().replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")

/**
 * Converts a date string (YYYY-MM-DD HH:MM:SS) to a relative time string (e.g., "2 hours ago").
 */
fun String.toRelativeTime(): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(this)
        if (date != null) {
            DateUtils.getRelativeTimeSpanString(
                date.time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        } else {
            this
        }
    } catch (e: Exception) {
        this
    }
}
