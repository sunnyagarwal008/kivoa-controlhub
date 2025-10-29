package com.kivoa.controlhub.data

import com.google.gson.annotations.SerializedName

data class BulkProductRequest(
    val products: List<ProductDetailRequest>
)

data class ProductDetailRequest(
    @SerializedName("raw_image")
    val rawImage: String,
    val mrp: Double,
    val price: Double,
    val discount: Double,
    val gst: Double,
    @SerializedName("purchase_month")
    val purchaseMonth: String,
    val category: String
)
