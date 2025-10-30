package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class PresignedUrlRequest(
    val filename: String,
    @Json(name = "content_type")
    val contentType: String
)

data class PresignedUrlResponse(
    val data: PresignedUrlData,
    val success: Boolean
)

data class PresignedUrlData(
    @Json(name = "expires_in")
    val expiresIn: Int,
    @Json(name = "file_url")
    val fileUrl: String,
    @Json(name = "presigned_url")
    val presignedUrl: String
)
