package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class PdfCatalogResponse(
    val success: Boolean,
    val message: String,
    val data: CatalogData
)

data class CatalogData(
    @Json(name = "catalog_url")
    val catalogUrl: String,
    @Json(name = "total_products")
    val totalProducts: Int,
    val categories: Int
)