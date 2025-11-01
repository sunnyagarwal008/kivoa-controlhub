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

// New data classes for the /api/products endpoint

data class ProductsApiResponse(
    val success: Boolean,
    val data: List<ApiProduct>,
    val pagination: ProductsApiPagination
)

data class ProductsApiPagination(
    val page: Int,
    val pages: Int,
    @Json(name = "per_page") val perPage: Int,
    val total: Int
)

data class ApiProduct(
    val category: String,
    @Json(name = "category_details") val categoryDetails: CategoryDetails,
    @Json(name = "category_id") val categoryId: Long,
    @Json(name = "created_at") val createdAt: String,
    val discount: Double,
    val gst: Double,
    val id: Long,
    val images: List<Image>,
    @Json(name = "in_stock") val inStock: Boolean,
    val mrp: Double,
    val price: Double,
    @Json(name = "purchase_month") val purchaseMonth: String,
    @Json(name = "raw_image") val rawImage: String,
    val sku: String,
    val status: String,
    @Json(name = "updated_at") val updatedAt: String
)

data class CategoryDetails(
    @Json(name = "created_at") val createdAt: String,
    val id: Long,
    val name: String,
    val prefix: String,
    @Json(name = "sku_sequence_number") val skuSequenceNumber: Int,
    @Json(name = "updated_at") val updatedAt: String
)

data class Image(
    @Json(name = "created_at") val createdAt: String,
    val id: Long,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "product_id") val productId: Long,
    val status: String,
    @Json(name = "updated_at") val updatedAt: String
)
