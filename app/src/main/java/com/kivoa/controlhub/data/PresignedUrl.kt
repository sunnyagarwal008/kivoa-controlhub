package com.kivoa.controlhub.data

import com.google.gson.annotations.SerializedName

data class PresignedUrlRequest(
    val filename: String,
    @SerializedName("content_type")
    val contentType: String
)

data class PresignedUrlResponse(
    val data: PresignedUrlData,
    val success: Boolean
)

data class PresignedUrlData(
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("file_url")
    val fileUrl: String,
    @SerializedName("presigned_url")
    val presignedUrl: String
)
