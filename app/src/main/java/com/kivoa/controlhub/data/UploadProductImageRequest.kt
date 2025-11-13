package com.kivoa.controlhub.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadProductImageRequest(
    @Json(name = "image_url")
    val imageUrl: String
)
