package com.kivoa.controlhub.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.AmazonSyncRequest
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.CustomerAddress
import com.kivoa.controlhub.data.GenerateProductImageRequest
import com.kivoa.controlhub.data.ImagePriority
import com.kivoa.controlhub.data.PlaceOrderRequest
import com.kivoa.controlhub.data.ProductApiRepository
import com.kivoa.controlhub.data.Prompt
import com.kivoa.controlhub.data.UpdateProductFlaggedRequest
import com.kivoa.controlhub.data.UpdateProductStockRequest
import com.kivoa.controlhub.data.UploadProductImageRequest
import com.kivoa.controlhub.utils.S3ImageUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class ProductDetailViewModel : ViewModel() {

    private val productRepository = ProductApiRepository(RetrofitInstance.api)
    private val s3ImageUploader = S3ImageUploader(RetrofitInstance.api, OkHttpClient(), Gson())

    private val _product = MutableStateFlow<ApiProduct?>(null)
    val product: StateFlow<ApiProduct?> = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _productNotFound = MutableStateFlow(false)
    val productNotFound: StateFlow<Boolean> = _productNotFound.asStateFlow()

    private val _prompts = MutableStateFlow<List<Prompt>>(emptyList())
    val prompts: StateFlow<List<Prompt>> = _prompts.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun getProductById(productId: Long) {
        viewModelScope.launch {
            try {
                _product.value = RetrofitInstance.api.getProductById(productId).data
            } catch (_: Exception) {
                _productNotFound.value = true
            }
        }
    }

    fun syncWithAmazon(productId: Long, request: AmazonSyncRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                productRepository.syncAmazonChannel(productId, request)
                getProductById(productId)
            } catch (e: Exception) {
                _error.value = "Failed to sync with Amazon: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun placeOrder(
        customerName: String,
        customerPhone: String,
        address1: String,
        city: String,
        province: String,
        zip: String,
        shippingCharges: Double,
        perUnitPrice: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val product = _product.value
                if (product != null) {
                    val request = PlaceOrderRequest(
                        sku = product.sku,
                        quantity = 1,
                        perUnitPrice = perUnitPrice,
                        shippingCharges = shippingCharges,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        customerAddress = CustomerAddress(
                            address1 = address1,
                            city = city,
                            province = province,
                            country = "India",
                            zip = zip
                        )
                    )
                    productRepository.placeOrder(request)
                    getProductById(product.id)
                    onSuccess()
                } else {
                    onError("Product not found")
                }
            } catch (e: Exception) {
                onError("Failed to place order: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProductStock(productId: Long, inStock: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                RetrofitInstance.api.updateProductStock(
                    productId,
                    UpdateProductStockRequest(inStock)
                )
                getProductById(productId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProductFlagged(productId: Long, flagged: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                RetrofitInstance.api.updateProductFlagged(
                    productId,
                    UpdateProductFlaggedRequest(flagged)
                )
                getProductById(productId)
            } catch (e: Exception) {
                _error.value = "Failed to update flagged status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProductImagePriorities(productId: Long, priorities: List<ImagePriority>) {
        viewModelScope.launch {
            try {
                productRepository.updateProductImagePriorities(productId, priorities)
                getProductById(productId)
            } catch (e: Exception) {
                _error.value = "Failed to update image priorities: ${e.message}"
            }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            RetrofitInstance.api.deleteProduct(productId)
        }
    }

    fun deleteProductImage(productId: Long, imageId: Long) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.rejectProductImage(productId, imageId)
                getProductById(productId)
            } catch (e: Exception) {
                _error.value = "Failed to delete image: ${e.message}"
            }
        }
    }

    fun getPrompts(category: String) {
        viewModelScope.launch {
            try {
                val allPrompts = RetrofitInstance.api.getPrompts(category = category).data
                _prompts.value = allPrompts
            } catch (_: Exception) {
                // Handle error
            }
        }
    }

    suspend fun generateProductImage(productId: Long, promptId: Long): Boolean {
        return try {
            val request = GenerateProductImageRequest(promptId)
            RetrofitInstance.api.generateProductImage(productId, request)
            getProductById(productId)
            true
        } catch (e: Exception) {
            _error.value = "Failed to generate image: ${e.message}"
            false
        }
    }

    fun uploadProductImage(productId: Long, imageUri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val imageUrl = s3ImageUploader.uploadImageToS3(imageUri, context)
                if (imageUrl != null) {
                    RetrofitInstance.api.uploadProductImage(
                        productId,
                        UploadProductImageRequest(imageUrl)
                    )
                    getProductById(productId)
                } else {
                    _error.value = "Failed to upload image"
                }
            } catch (e: Exception) {
                _error.value = "Failed to upload image: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
