package com.example.phinmalostandfound

import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity

class ScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        findViewById<android.widget.ImageView>(R.id.closeScanButton).setOnClickListener { finish() }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.chooseFromGalleryButton)
            .setOnClickListener { finish() }

        animateScanLine()
    }

    private fun animateScanLine() {
        val scanLine = findViewById<android.view.View>(R.id.scanLine) ?: return
        val anim = TranslateAnimation(0f, 0f, 0f, 250f.dp()).apply {
            duration = 2000
            repeatMode = android.view.animation.Animation.REVERSE
            repeatCount = android.view.animation.Animation.INFINITE
            interpolator = LinearInterpolator()
        }
        scanLine.startAnimation(anim)
    }

    private fun Float.dp(): Float = this * resources.displayMetrics.density

}
