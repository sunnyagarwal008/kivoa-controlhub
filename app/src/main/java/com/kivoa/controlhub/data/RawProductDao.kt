package com.kivoa.controlhub.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RawProductDao {
    @Insert
    suspend fun insert(rawProduct: RawProduct): Long

    @Delete
    suspend fun delete(rawProduct: RawProduct)

    @Update
    suspend fun update(rawProduct: RawProduct)

    @Query("SELECT * FROM raw_products")
    fun getAllRawProducts(): Flow<List<RawProduct>>
}
