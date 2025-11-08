package com.kivoa.controlhub.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "raw_images")
data class RawImage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageUri: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class RawImagesApiResponse(
    val success: Boolean,
    val data: List<ApiRawImage>,
    val pagination: ProductsApiPagination
)

data class ApiRawImage(
    val id: Long,
    @Json(name = "image_url")
    val imageUrl: String,
    @Json(name = "created_at")
    val createdAt: String
)