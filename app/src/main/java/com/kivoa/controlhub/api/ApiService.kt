package com.kivoa.controlhub.api

import com.kivoa.controlhub.data.ApiResponse
import com.kivoa.controlhub.data.BrowseApiResponse
import com.kivoa.controlhub.data.BulkProductRequest
import com.kivoa.controlhub.data.CreateProductsResponse
import com.kivoa.controlhub.data.PresignedUrlRequest
import com.kivoa.controlhub.data.PresignedUrlResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("api/search")
    suspend fun searchS(@Query("sku") sku: String): ApiResponse

    @GET("api/browse")
    suspend fun browse(
        @Query("category") category: String,
        @Query("page") page: Int,
        @Query("excludeOutOfStock") excludeOutOfStock: Boolean,
        @Query("minPrice") minPrice: Int? = null,
        @Query("maxPrice") maxPrice: Int? = null
    ): BrowseApiResponse

    @POST("api/presigned-url")
    suspend fun generatePresignedUrl(@Body request: PresignedUrlRequest): PresignedUrlResponse

    @POST("api/products/bulk")
    suspend fun createBulkProducts(@Body request: BulkProductRequest): CreateProductsResponse

}
