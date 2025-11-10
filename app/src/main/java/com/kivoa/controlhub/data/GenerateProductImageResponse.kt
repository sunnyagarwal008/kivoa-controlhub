package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class GenerateProductImageResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: ProductImage
)
