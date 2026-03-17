package com.example.phinmalostandfound

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Back button
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        // Dynamic version from build config
        val versionTextView = findViewById<TextView>(R.id.versionTextView)
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            versionTextView.text = "v$versionName"
        } catch (e: Exception) {
            versionTextView.text = "v1.0.0"
        }

        // Send feedback via email
        findViewById<LinearLayout>(R.id.sendFeedbackRow).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("feedback@phinma.edu.ph"))
                putExtra(Intent.EXTRA_SUBJECT, "PHINMA Lost & Found Feedback")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }

        // Privacy policy (static dialog)
        findViewById<LinearLayout>(R.id.privacyPolicyRow).setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("PHINMA Lost and Found collects minimal personal information (name, email, contact number) solely to facilitate the recovery of lost items. Your data is never sold or shared with third parties.\n\nImages uploaded are stored securely on PHINMA servers. You may request deletion of your account and associated data at any time by contacting the admin.")
                .setPositiveButton("Close", null)
                .show()
        }
    }
}
