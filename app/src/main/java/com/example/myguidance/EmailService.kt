package com.example.myguidance


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


object EmailService {

    private val client = OkHttpClient()

    suspend fun sendNotificationEmail(
        recipientEmail: String,
        messageText: String
    ): Boolean = withContext(Dispatchers.IO) {
        val url = "https://api.emailjs.com/api/v1.0/email/send"

        // Build parameters to match your EmailJS dynamic variables
        val templateParams = JSONObject().apply {
            put("to_email", recipientEmail)
            put("message", messageText)
        }

        val jsonBody = JSONObject().apply {
            put("service_id", BuildConfig.EMAILJS_SERVICE_ID)
            put("template_id", BuildConfig.EMAILJS_TEMPLATE_ID)
            put("user_id", BuildConfig.EMAILJS_PUBLIC_KEY) // Public key ONLY
            put("template_params", templateParams)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("EmailJS", "Email sent successfully!")
                    true
                } else {
                    Log.e("EmailJS", "Failed to send: ${response.code} - ${response.body?.string()}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("EmailJS", "Network error", e)
            false
        }
    }
}