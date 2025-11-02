package com.kivoa.controlhub.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.google.gson.Gson
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.data.PresignedUrlRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class S3ImageUploader(
    private val apiService: ApiService,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {

    suspend fun uploadImageToS3(imageUri: Uri, context: Context): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes == null) {
                Log.e(TAG, "Could not read image bytes from URI: $imageUri")
                return null
            }

            var fileName = "image.jpg"
            contentResolver.query(imageUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                    }
                }
            }

            val contentType = contentResolver.getType(imageUri) ?: "image/jpeg"

            val presignedUrlResponse = apiService.generatePresignedUrl(
                PresignedUrlRequest(filename = fileName, contentType = contentType)
            )

            if (presignedUrlResponse.success) {
                val presignedUrlData = presignedUrlResponse.data
                val s3UploadUrl = presignedUrlData.presignedUrl
                val s3FileUrl = presignedUrlData.fileUrl

                val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())

                val request = okhttp3.Request.Builder()
                    .url(s3UploadUrl)
                    .put(requestBody)
                    .header("Content-Type", contentType)
                    .build()

                withContext(Dispatchers.IO) {
                    val response = okHttpClient.newCall(request).execute()

                    if (response.isSuccessful) {
                        Log.d(TAG, "S3 upload successful. File URL: $s3FileUrl")
                        s3FileUrl
                    } else {
                        Log.e(TAG, "S3 upload failed: ${response.code} ${response.message}")
                        Log.e(TAG, "Response body: ${response.body?.string()}")
                        null
                    }
                }
            } else {
                Log.e(TAG, "Failed to generate presigned URL: $presignedUrlResponse")
                null
            }

        } catch (e: IOException) {
            Log.e(TAG, "S3 upload failed due to IO exception", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "S3 upload failed due to unexpected exception", e)
            null
        }
    }

    companion object {
        private const val TAG = "S3ImageUploader"
    }
}