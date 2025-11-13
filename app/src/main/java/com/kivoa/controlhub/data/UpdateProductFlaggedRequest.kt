package com.kivoa.controlhub.data

data class UpdateProductFlaggedRequest(
    val flagged: Boolean
)

data class UpdateProductFlaggedResponse(
    val success: Boolean,
    val message: String,
    val data: Any
)
