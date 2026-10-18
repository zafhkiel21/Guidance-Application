package com.example.myguidance

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object CloudinaryHelper {

    private const val CLOUD_NAME = "ba1crttg"
    private const val UPLOAD_PRESET = "ml_default"

    private val client = OkHttpClient()

    fun uploadMedia(
        context: Context,
        fileUri: Uri,
        onSuccess: (uploadedUrl: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null || bytes.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onError("Failed to read selected image file.")
                    }
                    return@launch
                }

                val mediaType = "image/jpeg".toMediaTypeOrNull()
                val fileRequestBody = bytes.toRequestBody(mediaType)

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("file", "upload_${System.currentTimeMillis()}.jpg", fileRequestBody)
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                    val json = JSONObject(responseData)
                    val secureUrl = json.optString("secure_url")

                    withContext(Dispatchers.Main) {
                        if (secureUrl.isNotEmpty()) {
                            onSuccess(secureUrl)
                        } else {
                            onError("Failed to retrieve upload URL.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError("Cloudinary upload failed: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Failed to process image.")
                }
            }
        }
    }
}