package com.kivoa.controlhub.data

import com.google.gson.annotations.SerializedName
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
    val message: String,
    val error: String? = null
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

data class Prompt(
    val id: Long,
    val text: String,
    @Json(name = "category_id") val categoryId: Long,
    val category: String,
    val type: String?,
    val tags: String?,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

data class PromptsApiResponse(
    val success: Boolean,
    val data: List<Prompt>,
    val total: Int
)

data class UpdatePromptRequest(
    val text: String?,
    val category: String?,
    val type: String?,
    val tags: String?,
    @SerializedName("is_active") val isActive: Boolean?
)

data class UpdatePromptResponse(
    val success: Boolean,
    val message: String,
    val data: Prompt
)
