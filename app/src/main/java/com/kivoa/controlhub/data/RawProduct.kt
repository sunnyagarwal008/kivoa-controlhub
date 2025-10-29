package com.kivoa.controlhub.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raw_products")
data class RawProduct(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUri: String // Storing Uri as String
)
