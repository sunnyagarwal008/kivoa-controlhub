package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class GenerateProductImageRequest(
    @Json(name = "prompt_type") val promptType: String?,
    @Json(name = "prompt_text") val promptText: String?
)
