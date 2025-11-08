package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class BulkCreateRawImagesRequest(
    @Json(name = "raw_images")
    val rawImages: List<RawImageRequest>
)

data class RawImageRequest(
    @Json(name = "image_url")
    val imageUrl: String
)

data class BulkCreateRawImagesResponse(
    val success: Boolean,
    val message: String,
    val data: RawImagesData
)

data class RawImagesData(
    @Json(name = "raw_images")
    val rawImages: List<ApiRawImage>
)