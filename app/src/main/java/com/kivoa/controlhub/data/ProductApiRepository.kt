package com.kivoa.controlhub.data

import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.ui.screens.ProductFormState

class ProductApiRepository(private val apiService: ApiService) {

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
                priceCode = formState.priceCode
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

    suspend fun updateProductStatus(productId: Long, status: String): Boolean {
        val request = UpdateProductStatusRequest(status = status)
        val response = apiService.updateProductStatus(productId, request)
        return response.success
    }
}