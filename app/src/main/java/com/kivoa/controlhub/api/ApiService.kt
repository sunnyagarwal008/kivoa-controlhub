package com.kivoa.controlhub.api

import com.kivoa.controlhub.data.BulkProductRequest
import com.kivoa.controlhub.data.CreateProductsResponse
import com.kivoa.controlhub.data.PresignedUrlRequest
import com.kivoa.controlhub.data.PresignedUrlResponse
import com.kivoa.controlhub.data.ProductsApiResponse
import com.kivoa.controlhub.data.SearchProductsResponse
import com.kivoa.controlhub.data.UpdateProductApiResponse
import com.kivoa.controlhub.data.UpdateProductStatusRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/products/search")
    suspend fun searchS(@Query("sku") sku: String): SearchProductsResponse

    @POST("api/presigned-url")
    suspend fun generatePresignedUrl(@Body request: PresignedUrlRequest): PresignedUrlResponse

    @POST("api/products/bulk")
    suspend fun createBulkProducts(@Body request: BulkProductRequest): CreateProductsResponse

    @GET("api/products")
    suspend fun getProducts(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 10,
        @Query("status") status: String = "live",
        @Query("category") category: String? = null,
        @Query("excludeOutOfStock") excludeOutOfStock: Boolean = false,
        @Query("minPrice") minPrice: Int? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null
    ): ProductsApiResponse

    @PUT("api/products/{product_id}/status")
    suspend fun updateProductStatus(
        @Path("product_id") productId: Long,
        @Body request: UpdateProductStatusRequest
    ): UpdateProductApiResponse
}
