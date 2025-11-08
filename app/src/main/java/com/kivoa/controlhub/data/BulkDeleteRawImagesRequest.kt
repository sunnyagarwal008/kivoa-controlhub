package com.kivoa.controlhub.data

data class BulkDeleteRawImagesRequest(
    val ids: List<Long>
)

data class BulkDeleteRawImagesResponse(
    val success: Boolean,
    val message: String,
    val data: DeleteStats
)

data class DeleteStats(
    val deleted: Int,
    val total: Int,
    val failed: Int,
    val failed_ids: List<Int>
)