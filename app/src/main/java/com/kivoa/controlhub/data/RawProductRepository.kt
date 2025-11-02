package com.kivoa.controlhub.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow

class RawProductRepository(private val rawProductDao: RawProductDao) {

    fun getAllRawProducts(): Flow<List<RawProduct>> {
        return rawProductDao.getAllRawProducts()
    }

    suspend fun insert(rawProduct: RawProduct) {
        rawProductDao.insert(rawProduct)
    }

    suspend fun delete(rawProducts: List<RawProduct>) {
        rawProductDao.delete(rawProducts)
    }

    suspend fun findRawProductsByImageUris(imageUriStrings: List<String>): List<RawProduct> {
        return rawProductDao.findRawProductsByImageUris(imageUriStrings)
    }
}