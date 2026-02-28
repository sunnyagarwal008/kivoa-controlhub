package com.kivoa.controlhub.data

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class AmazonListingResponse(
    val success: Boolean,
    val channel: String,
    val data: List<AmazonProduct>,
    val pagination: Pagination
)

data class AmazonProduct(
    @Json(name = "product_id")
    val productId: Long?,
    @Json(name = "channel_listing_id")
    val channelListingId: String,
    @Json(name = "product_image")
    val productImage: String,
    val title: String
)

data class Pagination(
    val page: Int,
    @Json(name = "per_page")
    val perPage: Int,
    val total: Int,
    val pages: Int
)