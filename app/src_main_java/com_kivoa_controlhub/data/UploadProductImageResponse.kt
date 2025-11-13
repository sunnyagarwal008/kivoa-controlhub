package com.kivoa.controlhub.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadProductImageResponse(
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "message")
    val message: String,
    @Json(name = "data")
    val data: ProductImage
)
