package com.kivoa.controlhub.api

import com.kivoa.controlhub.data.ApiCategory
import com.kivoa.controlhub.data.BulkProductRequest
import com.kivoa.controlhub.data.CategoriesApiResponse
import com.kivoa.controlhub.data.CreateCategoryRequest
import com.kivoa.controlhub.data.CreateCategoryResponse
import com.kivoa.controlhub.data.CreateProductsResponse
import com.kivoa.controlhub.data.PresignedUrlRequest
import com.kivoa.controlhub.data.PresignedUrlResponse
import com.kivoa.controlhub.data.ProductsApiResponse
import com.kivoa.controlhub.data.SearchProductsResponse
import com.kivoa.controlhub.data.UpdateCategoryRequest
import com.kivoa.controlhub.data.UpdateProductApiResponse
import com.kivoa.controlhub.data.UpdateProductStatusRequest
import com.kivoa.controlhub.data.UpdateProductStockRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    @PUT("api/products/{product_id}/stock")
    suspend fun updateProductStock(
        @Path("product_id") productId: Long,
        @Body request: UpdateProductStockRequest
    ): UpdateProductApiResponse

    @GET("api/categories")
    suspend fun getCategories(): CategoriesApiResponse

    @POST("api/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): CreateCategoryResponse

    @GET("api/categories/{category_id}")
    suspend fun getCategoryById(@Path("category_id") categoryId: String): ApiCategory

    @PUT("api/categories/{category_id}")
    suspend fun updateCategory(
        @Path("category_id") categoryId: Long,
        @Body request: UpdateCategoryRequest
    ): CreateCategoryResponse

    @DELETE("api/categories/{category_id}")
    suspend fun deleteCategory(@Path("category_id") categoryId: String): Unit
}
