package com.kivoa.controlhub.ui.screens

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.AppDatabase
import com.kivoa.controlhub.data.RawProduct
import com.kivoa.controlhub.data.RawProductDao
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import com.kivoa.controlhub.data.BulkProductRequest
import com.kivoa.controlhub.data.PresignedUrlRequest
import com.kivoa.controlhub.data.ProductDetailRequest
import android.provider.OpenableColumns
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.UpdateProductStatusRequest
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf


class CreateViewModel(application: Application) : AndroidViewModel(application) {

    private val rawProductDao: RawProductDao
    private val apiService: ApiService
    private val okHttpClient: OkHttpClient = OkHttpClient()
    private val gson: Gson = Gson()

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

    private val _selectedInReviewProductIds = MutableStateFlow<PersistentList<Long>>(persistentListOf())
    val selectedInReviewProductIds: StateFlow<PersistentList<Long>> = _selectedInReviewProductIds.asStateFlow()


    init {
        val database = AppDatabase.getDatabase(application)
        rawProductDao = database.rawProductDao()
        apiService = RetrofitInstance.api

        viewModelScope.launch {
            rawProductDao.getAllRawProducts().collect {
                _rawProducts.value = it
            }
        }
    }

    fun onImagesSelected(imageUris: List<Uri>, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            imageUris.forEach { uri ->
                uploadImageToS3(uri, context)
            }
            _isLoading.value = false
        }
    }

    private suspend fun uploadImageToS3(imageUri: Uri, context: Context) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes == null) {
                Log.e(TAG, "Could not read image bytes from URI: $imageUri")
                return
            }

            // Extract filename from ContentResolver
            var fileName = "image.jpg" // Default filename
            contentResolver.query(imageUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                    }
                }
            }

            val contentType = contentResolver.getType(imageUri) ?: "image/jpeg"

            val presignedUrlResponse = apiService.generatePresignedUrl(
                PresignedUrlRequest(filename = fileName, contentType = contentType)
            )

            if (presignedUrlResponse.success) {
                val presignedUrlData = presignedUrlResponse.data
                val s3UploadUrl = presignedUrlData.presignedUrl
                val s3FileUrl = presignedUrlData.fileUrl

                val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())

                val request = okhttp3.Request.Builder()
                    .url(s3UploadUrl)
                    .put(requestBody)
                    .header("Content-Type", contentType) // Explicitly set Content-Type header
                    .build()

                withContext(Dispatchers.IO) {
                    val response = okHttpClient.newCall(request).execute()

                    if (response.isSuccessful) {
                        // On successful S3 upload, insert into Room with S3 URL
                        val rawProduct = RawProduct(imageUri = s3FileUrl)
                        rawProductDao.insert(rawProduct)
                        Log.d(TAG, "S3 upload successful. File URL: $s3FileUrl")
                    } else {
                        // Handle S3 upload failure
                        Log.e(TAG, "S3 upload failed: ${response.code} ${response.message}")
                        Log.e(TAG, "Response body: ${response.body?.string()}")
                    }
                    response.close()
                }
            } else {
                // Handle presigned URL generation failure
                Log.e(TAG, "Failed to generate presigned URL: $presignedUrlResponse")
            }

        } catch (e: IOException) {
            // Handle upload failure due to IO issues
            Log.e(TAG, "S3 upload failed due to IO exception", e)
        } catch (e: Exception) {
            // Handle other exceptions during upload
            Log.e(TAG, "S3 upload failed due to unexpected exception", e)
        }
    }

    fun createProducts(productFormStates: List<ProductFormState>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val productDetails = productFormStates.map { formState ->
                    ProductDetailRequest(
                        rawImage = formState.rawImage,
                        mrp = formState.mrp.toDoubleOrNull() ?: 0.0,
                        price = formState.price.toDoubleOrNull() ?: 0.0,
                        discount = formState.discount.toDoubleOrNull() ?: 0.0,
                        gst = formState.gst.toDoubleOrNull() ?: 0.0,
                        purchaseMonth = formState.purchaseMonth,
                        category = formState.category
                    )
                }
                val bulkProductRequest = BulkProductRequest(products = productDetails)
                val response = apiService.createBulkProducts(bulkProductRequest)

                if (response.success) {
                    Log.d(TAG, "Bulk product creation successful")
                    _bulkProductCreationSuccess.value = true
                    // Optionally, clear raw products from local DB if they are successfully created on backend
                    // For now, let's assume successful API call means they are processed.
                    // You might want to remove them specifically based on their imageUri or another identifier
                    productFormStates.forEach { formState ->
                        val rawProductToRemove = _rawProducts.value.find { it.imageUri == formState.rawImage }
                        if (rawProductToRemove != null) {
                            rawProductDao.delete(rawProductToRemove)
                        }
                    }
                } else {
                    Log.e(TAG, "Bulk product creation failed: ${response.message}")
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
        viewModelScope.launch(Dispatchers.IO) {
            val imageUriStrings = urisToDelete.map { it.toString() }
            val rawProductsToDelete = rawProductDao.findRawProductsByImageUris(imageUriStrings)
            rawProductDao.delete(rawProductsToDelete)
        }
    }

    fun resetBulkProductCreationSuccess() {
        _bulkProductCreationSuccess.value = false
    }

    fun fetchInReviewProducts(page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch {
            _inReviewProductsLoading.value = true
            try {
                val response = apiService.getProducts(page = page, perPage = perPage, status = "pending_review")
                if (response.success) {
                    _inReviewProducts.value = response.data
                    Log.d(TAG, "Fetched ${response.data.size} in review products")
                } else {
                    Log.e(TAG, "Failed to fetch in review products: ${response.pagination}")
                }
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
                val response = apiService.getProducts(page = page, perPage = perPage, status = "pending")
                if (response.success) {
                    _inProgressProducts.value = response.data
                    Log.d(TAG, "Fetched ${response.data.size} in progress products")
                } else {
                    Log.e(TAG, "Failed to fetch in progress products: ${response.pagination}")
                }
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
            _inReviewProductsLoading.value = true // Show loading for status update
            try {
                productIds.forEach { productId ->
                    val request = UpdateProductStatusRequest(status = status)
                    // Convert Long productId to Int for the API call
                    val response = apiService.updateProductStatus(productId, request)
                    if (response.success) {
                        Log.d(TAG, "Product $productId status updated to $status successfully")
                    } else {
                        Log.e(TAG, "Failed to update status for product $productId: ${response.message}")
                    }
                }
                fetchInReviewProducts() // Refresh the list after updates
                clearSelectedInReviewProductIds() // Clear selection after action
            } catch (e: Exception) {
                Log.e(TAG, "Error updating product status: ${e.message}", e)
            } finally {
                _inReviewProductsLoading.value = false
            }
        }
    }

    companion object {
        private const val TAG = "CreateViewModel"
    }
}