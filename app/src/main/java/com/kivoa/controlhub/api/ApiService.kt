package com.kivoa.controlhub.api

import com.kivoa.controlhub.data.ApiResponse
import com.kivoa.controlhub.data.BrowseApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/search")
    suspend fun searchS(@Query("sku") sku: String): ApiResponse

    @GET("api/browse")
    suspend fun browse(
        @Query("category") category: String,
        @Query("page") page: Int,
        @Query("excludeOutOfStock") excludeOutOfStock: Boolean
    ): BrowseApiResponse
}