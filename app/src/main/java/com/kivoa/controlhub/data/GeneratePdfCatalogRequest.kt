package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class GeneratePdfCatalogRequest(
    val status: String = "live",
    val category: String? = null,
    val tags: String? = null,
    val excludeOutOfStock: Boolean = false,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val sortBy: String? = null,
    val sortOrder: String? = null,
    val name: String? = null
)
