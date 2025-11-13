package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class UpdateProductRequest(
    val title: String?,
    val description: String?,
    val category: String,
    @Json(name = "purchase_month") val purchaseMonth: String,
    val mrp: Double,
    val price: Double,
    val discount: Double,
    val gst: Double,
    @Json(name = "price_code") val priceCode: String,
    val tags: String,
    @Json(name = "box_number") val boxNumber: Int? = null
)
