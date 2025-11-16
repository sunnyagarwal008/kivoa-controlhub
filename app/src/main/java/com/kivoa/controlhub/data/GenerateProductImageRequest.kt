package com.kivoa.controlhub.data

import com.squareup.moshi.Json

data class GenerateProductImageRequest(
    @Json(name = "prompt_id") val promptId: Long?,
    @Json(name = "prompt_text") val promptText: String? = null
)
