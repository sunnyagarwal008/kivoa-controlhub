package com.kivoa.controlhub.data

data class CreatePromptResponse(
    val success: Boolean,
    val message: String,
    val data: Prompt
)
