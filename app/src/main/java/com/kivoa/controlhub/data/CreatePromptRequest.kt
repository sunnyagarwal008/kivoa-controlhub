package com.kivoa.controlhub.data

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class CreatePromptRequest(
    val text: String,
    val category: String,
    val type: String,
    val tags: String,
    @Json(name = "is_active")
    val isActive: Boolean
)
