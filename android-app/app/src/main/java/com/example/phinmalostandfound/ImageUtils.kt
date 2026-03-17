package com.example.phinmalostandfound

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Compresses and resizes an image from a Uri to a ByteArray.
     * @param context Context for contentResolver
     * @param uri The Uri of the image to compress
     * @param maxWidth Max width for resizing
     * @param maxHeight Max height for resizing
     * @param quality Compression quality (1-100)
     * @return Compressed image as ByteArray
     */
    fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate scale factor
            var inSampleSize = 1
            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                    inSampleSize *= 2
                }
            }

            // Decode with inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val scaledInputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(scaledInputStream, null, decodeOptions)
            scaledInputStream?.close()

            bitmap?.let {
                val outputStream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                val result = outputStream.toByteArray()
                it.recycle()
                result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
