package com.kivoa.controlhub.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.ui.screens.ProductFormState
import kotlinx.coroutines.flow.Flow


class ProductApiRepository(private val apiService: ApiService) {

    private var rawImagePagingSource: PagingSource<Int, ApiRawImage>? = null

    val rawImagesFlow: Flow<PagingData<ApiRawImage>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            RawImagePagingSource(apiService).also { rawImagePagingSource = it }
        }
    ).flow

    fun invalidateRawImages() {
        rawImagePagingSource?.invalidate()
    }

    suspend fun bulkCreateRawImages(rawImages: List<RawImageRequest>): BulkCreateRawImagesResponse {
        val request = BulkCreateRawImagesRequest(rawImages = rawImages)
        return apiService.bulkCreateRawImages(request)
    }

    suspend fun bulkDeleteRawImages(ids: List<Long>): BulkDeleteRawImagesResponse {
        val request = BulkDeleteRawImagesRequest(ids = ids)
        return apiService.bulkDeleteRawImages(request)
    }

    suspend fun createBulkProducts(productFormStates: List<ProductFormState>): Boolean {
        val productDetails = productFormStates.map { formState ->
            ProductDetailRequest(
                rawImage = formState.rawImage,
                mrp = formState.mrp.toDoubleOrNull() ?: 0.0,
                price = formState.price.toDoubleOrNull() ?: 0.0,
                discount = formState.discount.toDoubleOrNull() ?: 0.0,
                gst = formState.gst.toDoubleOrNull() ?: 0.0,
                purchaseMonth = formState.purchaseMonth,
                category = formState.category,
                priceCode = formState.priceCode,
                isRawImage = formState.isRawImage,
                boxNumber = formState.boxNumber.toIntOrNull(),
                tags = formState.tags.joinToString(","),
                promptId = formState.promptId
            )
        }
        val bulkProductRequest = BulkProductRequest(products = productDetails)
        val response = apiService.createBulkProducts(bulkProductRequest)
        return response.success
    }

    suspend fun getProducts(page: Int, perPage: Int, status: String): List<ApiProduct> {
        val response = apiService.getProducts(page, perPage, status)
        return if (response.success) response.data else emptyList()
    }

    suspend fun bulkUpdateProductStatus(productIds: List<Long>, status: String): BulkUpdateProductStatusResponse {
        val request = BulkUpdateProductStatusRequest(product_ids = productIds, status = status)
        return apiService.bulkUpdateProductStatus(request)
    }

    suspend fun updateProductImagePriorities(
        productId: Long,
        priorities: List<ImagePriority>
    ): UpdateImagePrioritiesResponse {
        val request = UpdateImagePrioritiesRequest(priorities)
        return apiService.updateProductImagePriorities(productId, request)
    }

    suspend fun placeOrder(request: PlaceOrderRequest): PlaceOrderResponse {
        return apiService.placeOrder(request)
    }

    suspend fun deleteProduct(productId: Long) {
        apiService.deleteProduct(productId)
    }
}