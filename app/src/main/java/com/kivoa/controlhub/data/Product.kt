package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class Product(
    @Json(name = "purchase_monthyear") val purchaseMonthYear: String,
    @Json(name = "image_url") val imageUrl: String,
    val sno: String,
    val sku: String,
    @Json(name = "price_code") val priceCode: String,
    @Json(name = "buy_price") val buyPrice: String,
    val gst: String,
    val mrp: String,
    val discount: String,
    @Json(name = "selling_price") val sellingPrice: String,
    val quantity: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: List<Product>
)


data class CreateProductsResponse(
    val success: Boolean,
    val message: String,
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val totalItems: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean
)


data class BrowseApiResponse(
    val success: Boolean,
    val pagination: Pagination,
    val data: List<Product>
)
