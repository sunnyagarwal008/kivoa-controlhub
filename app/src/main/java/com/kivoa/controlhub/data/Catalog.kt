package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class ApiCatalog(
    val id: Long,
    val name: String,
    @Json(name = "s3_url") val s3Url: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

data class CatalogsResponse(
    val success: Boolean,
    val data: List<ApiCatalog>,
    val count: Int
)

data class RefreshedCatalog(
    val id: Long,
    val name: String,
    @Json(name = "s3_url") val s3Url: String,
    @Json(name = "total_products") val totalProducts: Int,
    val categories: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

data class RefreshCatalogResponse(
    val success: Boolean,
    val message: String,
    val data: RefreshedCatalog
)
