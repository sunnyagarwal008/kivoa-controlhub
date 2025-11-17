package com.kivoa.controlhub.data.shopify.order

import com.squareup.moshi.Json

data class OrdersResponse(
    val success: Boolean,
    val data: OrdersData
)

data class OrdersData(
    val orders: List<Order>,
    val pagination: Pagination,
    val count: Int
)

data class Pagination(
    val limit: Int,
    @Json(name = "has_next")
    val hasNext: Boolean,
    @Json(name = "has_previous")
    val hasPrevious: Boolean,
    @Json(name = "next_page_info")
    val nextPageInfo: String?,
    @Json(name = "previous_page_info")
    val previousPageInfo: String?
)

data class Order(
    val id: Long,
    @Json(name = "order_number")
    val orderNumber: Int,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "financial_status")
    val financialStatus: String,
    @Json(name = "fulfillment_status")
    val fulfillmentStatus: String?,
    val customer: Customer?,
    @Json(name = "shipping_address")
    val shippingAddress: Address,
    @Json(name = "line_items")
    val lineItems: List<LineItem>,
    @Json(name = "total_price")
    val totalPrice: String
)

data class Customer(
    @Json(name = "first_name")
    val firstName: String?,
    @Json(name = "last_name")
    val lastName: String?
)

data class LineItem(
    val sku: String?,
    val quantity: Int,
    val price: String,
    val title: String
)

data class Address(
    val address1: String?,
    val city: String?,
    val province: String?,
    val zip: String?,
    val country: String?
)
