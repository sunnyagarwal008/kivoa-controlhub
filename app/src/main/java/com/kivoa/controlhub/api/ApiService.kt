package com.kivoa.controlhub.api

import com.kivoa.controlhub.data.ApiCategory
import com.kivoa.controlhub.data.BulkCreateRawImagesRequest
import com.kivoa.controlhub.data.BulkCreateRawImagesResponse
import com.kivoa.controlhub.data.BulkDeleteRawImagesRequest
import com.kivoa.controlhub.data.BulkDeleteRawImagesResponse
import com.kivoa.controlhub.data.BulkProductRequest
import com.kivoa.controlhub.data.BulkUpdateProductStatusRequest
import com.kivoa.controlhub.data.BulkUpdateProductStatusResponse
import com.kivoa.controlhub.data.CatalogsResponse
import com.kivoa.controlhub.data.CategoriesApiResponse
import com.kivoa.controlhub.data.CreateCategoryRequest
import com.kivoa.controlhub.data.CreateCategoryResponse
import com.kivoa.controlhub.data.CreateProductsResponse
import com.kivoa.controlhub.data.CreatePromptRequest
import com.kivoa.controlhub.data.CreatePromptResponse
import com.kivoa.controlhub.data.DeletePromptResponse
import com.kivoa.controlhub.data.GeneratePdfCatalogRequest
import com.kivoa.controlhub.data.GenerateProductImageRequest
import com.kivoa.controlhub.data.GenerateProductImageResponse
import com.kivoa.controlhub.data.PdfCatalogResponse
import com.kivoa.controlhub.data.PlaceOrderRequest
import com.kivoa.controlhub.data.PlaceOrderResponse
import com.kivoa.controlhub.data.PresignedUrlRequest
import com.kivoa.controlhub.data.PresignedUrlResponse
import com.kivoa.controlhub.data.ProductDetailResponse
import com.kivoa.controlhub.data.ProductsApiResponse
import com.kivoa.controlhub.data.PromptsApiResponse
import com.kivoa.controlhub.data.RawImagesApiResponse
import com.kivoa.controlhub.data.RefreshCatalogResponse
import com.kivoa.controlhub.data.SearchProductsResponse
import com.kivoa.controlhub.data.UpdateCategoryRequest
import com.kivoa.controlhub.data.UpdateImagePrioritiesRequest
import com.kivoa.controlhub.data.UpdateImagePrioritiesResponse
import com.kivoa.controlhub.data.UpdateProductApiResponse
import com.kivoa.controlhub.data.UpdateProductFlaggedRequest
import com.kivoa.controlhub.data.UpdateProductFlaggedResponse
import com.kivoa.controlhub.data.UpdateProductRequest
import com.kivoa.controlhub.data.UpdateProductStockApiResponse
import com.kivoa.controlhub.data.UpdateProductStockRequest
import com.kivoa.controlhub.data.UpdatePromptRequest
import com.kivoa.controlhub.data.UpdatePromptResponse
import com.kivoa.controlhub.data.UploadProductImageRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/raw-images/bulk")
    suspend fun bulkCreateRawImages(@Body request: BulkCreateRawImagesRequest): BulkCreateRawImagesResponse

    @GET("api/raw-images")
    suspend fun getRawImages(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 10,
        @Query("sortBy") sortBy: String = "created_at",
        @Query("sortOrder") sortOrder: String = "desc"
    ): RawImagesApiResponse


    @HTTP(method = "DELETE", path = "api/raw-images/bulk", hasBody = true)
    suspend fun bulkDeleteRawImages(@Body request: BulkDeleteRawImagesRequest): BulkDeleteRawImagesResponse

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
        @Query("sortOrder") sortOrder: String? = null,
        @Query("tags") tags: String? = null,
        @Query("boxNumber") boxNumber: String? = null,
        @Query("flagged") flagged: Boolean? = null
    ): ProductsApiResponse

    @POST("api/catalogs")
    suspend fun generatePdfCatalog(@Body request: GeneratePdfCatalogRequest): PdfCatalogResponse

    @GET("api/catalogs")
    suspend fun getAllCatalogs(): CatalogsResponse

    @DELETE("api/catalogs/{catalog_id}")
    suspend fun deleteCatalog(@Path("catalog_id") catalogId: Long): Unit

    @POST("api/catalogs/{catalog_id}/refresh")
    suspend fun refreshCatalog(@Path("catalog_id") catalogId: Long): RefreshCatalogResponse

    @GET("api/products/{product_id}")
    suspend fun getProductById(@Path("product_id") productId: Long): ProductDetailResponse

    @PUT("api/products/{product_id}")
    suspend fun updateProduct(
        @Path("product_id") productId: Long,
        @Body request: UpdateProductRequest
    ): UpdateProductApiResponse

    @PUT("api/products/{product_id}/flagged")
    suspend fun updateProductFlagged(
        @Path("product_id") productId: Long,
        @Body request: UpdateProductFlaggedRequest
    ): UpdateProductFlaggedResponse

    @POST("api/products/{product_id}/generate-image")
    suspend fun generateProductImage(
        @Path("product_id") productId: Long,
        @Body request: GenerateProductImageRequest
    ): GenerateProductImageResponse

    @POST("api/products/{product_id}/upload-image")
    suspend fun uploadProductImage(
        @Path("product_id") productId: Long,
        @Body request: UploadProductImageRequest
    ): Any

    @PUT("api/products/{product_id}/images/update-priorities")
    suspend fun updateProductImagePriorities(
        @Path("product_id") productId: Long,
        @Body request: UpdateImagePrioritiesRequest
    ): UpdateImagePrioritiesResponse

    @PUT("api/products/status")
    suspend fun bulkUpdateProductStatus(@Body request: BulkUpdateProductStatusRequest): BulkUpdateProductStatusResponse

    @PUT("api/products/{product_id}/stock")
    suspend fun updateProductStock(
        @Path("product_id") productId: Long,
        @Body request: UpdateProductStockRequest
    ): UpdateProductStockApiResponse

    @DELETE("api/products/{product_id}")
    suspend fun deleteProduct(@Path("product_id") productId: Long): Unit

    @DELETE("api/products/{product_id}/images/{image_id}/reject")
    suspend fun rejectProductImage(
        @Path("product_id") productId: Long,
        @Path("image_id") imageId: Long
    ): Unit

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

    @GET("api/prompts")
    suspend fun getPrompts(
        @Query("category") category: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("type") type: String? = null,
        @Query("is_active") isActive: Boolean? = null,
        @Query("tags") tags: String? = null,
        @Query("sortBy") sortBy: String? = "created_at",
        @Query("sortOrder") sortOrder: String? = "desc"
    ): PromptsApiResponse

    @POST("api/prompts")
    suspend fun createPrompt(@Body request: CreatePromptRequest): CreatePromptResponse

    @PUT("api/prompts/{prompt_id}")
    suspend fun updatePrompt(
        @Path("prompt_id") promptId: Long,
        @Body request: UpdatePromptRequest
    ): UpdatePromptResponse

    @DELETE("api/prompts/{prompt_id}")
    suspend fun deletePrompt(@Path("prompt_id") promptId: Long): DeletePromptResponse

    @POST("api/orders/place")
    suspend fun placeOrder(@Body request: PlaceOrderRequest): PlaceOrderResponse
}
