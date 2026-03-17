package com.example.phinmalostandfound

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.phinmalostandfound.databinding.ActivityReportBinding
import org.json.JSONObject

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private var postId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getIntOf("POST_ID", -1)

        binding.reportReasonGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.otherReasonEditText.visibility = if (checkedId == R.id.radioOther) View.VISIBLE else View.GONE
        }

        binding.submitReportButton.setOnClickListener {
            submitReport()
        }
    }

    private fun submitReport() {
        val selectedId = binding.reportReasonGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a reason", Toast.LENGTH_SHORT).show()
            return
        }

        val reason = when (selectedId) {
            R.id.radioSpam -> "Spam"
            R.id.radioInappropriate -> "Inappropriate Content"
            R.id.radioScam -> "Scam / Fraud"
            R.id.radioOther -> binding.otherReasonEditText.text.toString()
            else -> "Unknown"
        }

        if (reason.isBlank()) {
            Toast.makeText(this, "Please provide a reason", Toast.LENGTH_SHORT).show()
            return
        }

        val url = ApiConfig.buildUrl(ApiConfig.REPORT_POST, 
            "post_id" to postId.toString(),
            "reason" to reason
        )

        val request = StringRequest(Request.Method.POST, url,
            { response ->
                val json = JSONObject(response)
                if (json.optBoolean("success")) {
                    Toast.makeText(this, "Thank you for reporting. We will review it.", Toast.LENGTH_LONG).show()
                    finish()
                }
            },
            { Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show() }
        )
        AppSingleton.getRequestQueue(this).add(request)
    }
    
    private fun android.content.Intent.getIntOf(key: String, defaultValue: Int): Int {
        return if (hasExtra(key)) getIntExtra(key, defaultValue) else defaultValue
    }
}
