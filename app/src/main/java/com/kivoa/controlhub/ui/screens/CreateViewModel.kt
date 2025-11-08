package com.kivoa.controlhub.ui.screens

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiCategory
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.ProductApiRepository
import com.kivoa.controlhub.data.RawImageRequest
import com.kivoa.controlhub.data.RawProduct
import com.kivoa.controlhub.utils.S3ImageUploader
import com.google.gson.Gson
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient


class CreateViewModel(application: Application) : AndroidViewModel(application) {

    private val s3ImageUploader: S3ImageUploader
    private val productApiRepository: ProductApiRepository

    private val _rawProducts = MutableStateFlow<List<RawProduct>>(emptyList())
    val rawProducts: StateFlow<List<RawProduct>> = _rawProducts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _bulkProductCreationSuccess = MutableStateFlow(false)
    val bulkProductCreationSuccess: StateFlow<Boolean> = _bulkProductCreationSuccess.asStateFlow()

    private val _inReviewProducts = MutableStateFlow<List<ApiProduct>>(emptyList())
    val inReviewProducts: StateFlow<List<ApiProduct>> = _inReviewProducts.asStateFlow()

    private val _inReviewProductsLoading = MutableStateFlow(false)
    val inReviewProductsLoading: StateFlow<Boolean> = _inReviewProductsLoading.asStateFlow()

    private val _inProgressProducts = MutableStateFlow<List<ApiProduct>>(emptyList())
    val inProgressProducts: StateFlow<List<ApiProduct>> = _inProgressProducts.asStateFlow()

    private val _inProgressProductsLoading = MutableStateFlow(false)
    val inProgressProductsLoading: StateFlow<Boolean> = _inProgressProductsLoading.asStateFlow()

    private val _selectedInReviewProductIds =
        MutableStateFlow<PersistentList<Long>>(persistentListOf())
    val selectedInReviewProductIds: StateFlow<PersistentList<Long>> =
        _selectedInReviewProductIds.asStateFlow()

    private val _categories = MutableStateFlow<List<ApiCategory>>(emptyList())
    val categories: StateFlow<List<ApiCategory>> = _categories.asStateFlow()


    init {
        val apiService = RetrofitInstance.api
        val okHttpClient = OkHttpClient()
        val gson = Gson()
        s3ImageUploader = S3ImageUploader(apiService, okHttpClient, gson)
        productApiRepository = ProductApiRepository(apiService)

        fetchCategories()
    }

    fun onImagesSelected(imageUris: List<Uri>, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val imageUrls = imageUris.mapNotNull { uri ->
                s3ImageUploader.uploadImageToS3(uri, context)
            }
            val rawImageRequests = imageUrls.map { RawImageRequest(imageUrl = it) }
            try {
                val response = productApiRepository.bulkCreateRawImages(rawImageRequests)
                if (response.success) {
                    val newRawProducts = response.data.rawImages.map {
                        RawProduct(imageUri = it.imageUrl)
                    }
                    _rawProducts.value = _rawProducts.value + newRawProducts
                } else {
                    Log.e(TAG, "Bulk raw image creation failed: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating raw images: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createProducts(productFormStates: List<ProductFormState>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = productApiRepository.createBulkProducts(productFormStates)
                if (success) {
                    Log.d(TAG, "Bulk product creation successful")
                    _bulkProductCreationSuccess.value = true
                    productFormStates.forEach { formState ->
                        _rawProducts.value = _rawProducts.value.filter { it.imageUri != formState.rawImage }
                    }
                } else {
                    Log.e(TAG, "Bulk product creation failed")
                    _bulkProductCreationSuccess.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating products: ${e.message}", e)
                _bulkProductCreationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRawProducts(urisToDelete: List<Uri>) {
        viewModelScope.launch {
            val imageUriStrings = urisToDelete.map { it.toString() }
            _rawProducts.value = _rawProducts.value.filter { !imageUriStrings.contains(it.imageUri) }
        }
    }

    fun resetBulkProductCreationSuccess() {
        _bulkProductCreationSuccess.value = false
    }

    fun fetchInReviewProducts(page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch {
            _inReviewProductsLoading.value = true
            try {
                _inReviewProducts.value = productApiRepository.getProducts(
                    page = page,
                    perPage = perPage,
                    status = "pending_review"
                )
                Log.d(TAG, "Fetched ${_inReviewProducts.value.size} in review products")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching in review products: ${e.message}", e)
            } finally {
                _inReviewProductsLoading.value = false
            }
        }
    }

    fun fetchInProgressProducts(page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch {
            _inProgressProductsLoading.value = true
            try {
                _inProgressProducts.value = productApiRepository.getProducts(
                    page = page,
                    perPage = perPage,
                    status = "pending"
                )
                Log.d(TAG, "Fetched ${_inProgressProducts.value.size} in progress products")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching in progress products: ${e.message}", e)
            } finally {
                _inProgressProductsLoading.value = false
            }
        }
    }

    fun updateSelectedInReviewProductIds(productId: Long, isSelected: Boolean) {
        _selectedInReviewProductIds.value = if (isSelected) {
            _selectedInReviewProductIds.value.add(productId)
        } else {
            _selectedInReviewProductIds.value.remove(productId)
        }
    }

    fun clearSelectedInReviewProductIds() {
        _selectedInReviewProductIds.value = persistentListOf()
    }

    fun updateProductsStatus(productIds: List<Long>, status: String) {
        viewModelScope.launch {
            _inReviewProductsLoading.value = true
            try {
                productIds.forEach { productId ->
                    productApiRepository.updateProductStatus(productId, status)
                }
                fetchInReviewProducts()
                clearSelectedInReviewProductIds()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating product status: ${e.message}", e)
            } finally {
                _inReviewProductsLoading.value = false
            }
        }
    }

    fun deleteProducts(productIds: List<Long>) {
        viewModelScope.launch {
            _inReviewProductsLoading.value = true
            try {
                productIds.forEach { productId ->
                    productApiRepository.deleteProduct(productId)
                }
                fetchInReviewProducts()
                clearSelectedInReviewProductIds()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting products: ${e.message}", e)
            } finally {
                _inReviewProductsLoading.value = false
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCategories()
                if (response.success) {
                    _categories.value = response.data
                } else {
                    Log.e(TAG, "Error fetching categories: ${response.success}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching categories: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val TAG = "CreateViewModel"
    }
}
