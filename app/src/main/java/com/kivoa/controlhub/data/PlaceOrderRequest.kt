package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class PlaceOrderRequest(
    val sku: String,
    val quantity: Int,
    @Json(name = "per_unit_price")
    val perUnitPrice: Double,
    @Json(name = "shipping_charges")
    val shippingCharges: Double,
    @Json(name = "customer_name")
    val customerName: String,
    @Json(name = "customer_phone")
    val customerPhone: String,
    @Json(name = "customer_address")
    val customerAddress: CustomerAddress
)

data class CustomerAddress(
    val address1: String,
    val city: String,
    val province: String,
    val country: String,
    val zip: String
)

data class PlaceOrderResponse(
    val success: Boolean,
    val message: String
)