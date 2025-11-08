package com.kivoa.controlhub.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawImageRequest(
    @Json(name = "image_url") val imageUrl: String
)

@JsonClass(generateAdapter = true)
data class BulkCreateRawImagesRequest(
    @Json(name = "raw_images") val rawImages: List<RawImageRequest>
)

@JsonClass(generateAdapter = true)
data class RawImage(
    val id: Int,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class BulkCreateRawImagesResponseData(
    val created: Int,
    val total: Int,
    val skipped: Int,
    @Json(name = "raw_images") val rawImages: List<RawImage>
)

@JsonClass(generateAdapter = true)
data class BulkCreateRawImagesResponse(
    val success: Boolean,
    val message: String,
    val data: BulkCreateRawImagesResponseData
)
