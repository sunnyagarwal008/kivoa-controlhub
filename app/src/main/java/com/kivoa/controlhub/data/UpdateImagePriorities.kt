package com.kivoa.controlhub.data

data class ImagePriority(
    val image_id: Long,
    val priority: Int
)

data class UpdateImagePrioritiesRequest(
    val priorities: List<ImagePriority>
)

data class UpdateImagePrioritiesResponse(
    val success: Boolean,
    val message: String,
    val data: List<ProductImage>
)
