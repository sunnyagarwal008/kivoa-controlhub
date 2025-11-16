package com.kivoa.controlhub.data

data class ApplyDiscountRequest(
    val discount: Int,
    val status: String? = "live",
    val category: String? = null,
    val tags: String? = null,
    val excludeOutOfStock: Boolean? = false,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val boxNumber: String? = null,
    val flagged: Boolean? = null,
    val minDiscount: Int? = null,
    val maxDiscount: Int? = null
)
