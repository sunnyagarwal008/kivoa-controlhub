package com.kivoa.controlhub.api

import com.kivoa.controlhub.data.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/search")
    suspend fun searchS(@Query("sku") sku: String): ApiResponse
}