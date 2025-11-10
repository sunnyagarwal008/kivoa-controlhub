package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class ProductImage(
    @Json(name = "id") val id: Long,
    @Json(name = "product_id") val productId: Long,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "status") val status: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)
