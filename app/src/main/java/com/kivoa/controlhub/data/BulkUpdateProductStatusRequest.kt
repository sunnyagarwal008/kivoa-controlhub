package com.kivoa.controlhub.data

data class BulkUpdateProductStatusRequest(
    val product_ids: List<Long>,
    val status: String
)

data class BulkUpdateProductStatusResponse(
    val success: Boolean,
    val message: String,
    val data: Any
)
