package com.kivoa.controlhub.data

data class ApplyDiscountResponse(
    val success: Boolean,
    val message: String,
    val data: DiscountData
)

data class DiscountData(
    val updated_count: Int,
    val discount_percentage: Int
)
