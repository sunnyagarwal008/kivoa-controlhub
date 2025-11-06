package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class BulkProductRequest(
    val products: List<ProductDetailRequest>
)

data class ProductDetailRequest(
    @Json(name = "raw_image")
    val rawImage: String,
    val mrp: Double,
    val price: Double,
    val discount: Double,
    val gst: Double,
    @Json(name = "purchase_month")
    val purchaseMonth: String,
    val category: String,
    @Json(name = "price_code")
    val priceCode: String,
    @Json(name = "is_raw_image")
    val isRawImage: Boolean,
    @Json(name = "box_number")
    val boxNumber: Int?,
    val tags: String
)
