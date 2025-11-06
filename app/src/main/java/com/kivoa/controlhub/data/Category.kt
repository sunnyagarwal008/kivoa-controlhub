package com.kivoa.controlhub.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiCategory(
    val id: Long,
    val name: String,
    val prefix: String,
    @Json(name = "sku_sequence_number") val skuSequenceNumber: Int,
    val tags: String?,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class CreateCategoryRequest(
    val name: String,
    val prefix: String,
    val tags: String,
)

@JsonClass(generateAdapter = true)
data class CreateCategoryResponse(
    val data: ApiCategory,
    val success: Boolean,
    val message: String
)

@JsonClass(generateAdapter = true)
data class UpdateCategoryRequest(
    val name: String? = null,
    val prefix: String? = null,
    @Json(name = "sku_sequence_number") val skuSequenceNumber: Int? = null,
    val tags: String? = null
)

@JsonClass(generateAdapter = true)
data class CategoriesApiResponse(
    val data: List<ApiCategory>,
    val success: Boolean
)
