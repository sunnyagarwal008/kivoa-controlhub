package com.kivoa.controlhub.ui.screens

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.Helper
import com.kivoa.controlhub.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ShareViewModel(application: Application) : AndroidViewModel(application) {

    sealed class ShareState {
        object Idle : ShareState()
        object Processing : ShareState()
        data class Error(val message: String) : ShareState()
    }

    var shareState by mutableStateOf<ShareState>(ShareState.Idle)

    fun shareProduct(product: Product) {
        shareProducts(setOf(product))
    }

    fun shareProducts(products: Set<Product>) {
        shareState = ShareState.Processing
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageUris = ArrayList<Uri>()
                products.forEach { product ->
                    val cachePath = File(getApplication<Application>().cacheDir, "images")
                    val file = File(cachePath, "${product.sku}.jpeg")

                    if (!file.exists()) {
                        val imageUrl = Helper.getGoogleDriveImageUrl(product.imageUrl)
                        val bitmap = downloadImage(imageUrl)
                        val processedBitmap = addSkuToImage(bitmap, product.sku)
                        val compressedBitmap = compressImage(processedBitmap)
                        saveBitmapToCache(compressedBitmap, product.sku)
                    }

                    val uri = FileProvider.getUriForFile(
                        getApplication(),
                        "com.kivoa.controlhub.fileprovider",
                        file
                    )
                    imageUris.add(uri)
                }

                val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Share Images")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                getApplication<Application>().startActivity(chooser)

                shareState = ShareState.Idle

            } catch (e: Exception) {
                shareState = ShareState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    private fun downloadImage(imageUrl: String): Bitmap {
        val url = URL(imageUrl)
        return BitmapFactory.decodeStream(url.openConnection().getInputStream())
    }

    private fun addSkuToImage(bitmap: Bitmap, sku: String): Bitmap {
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(newBitmap)
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            isAntiAlias = true
        }

        val gradient = LinearGradient(
            0f, canvas.height - 50f, 0f, canvas.height.toFloat(),
            Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP
        )
        val gradientPaint = Paint().apply {
            shader = gradient
        }

        canvas.drawRect(0f, canvas.height - 50f, canvas.width.toFloat(), canvas.height.toFloat(), gradientPaint)
        canvas.drawText(sku, 10f, canvas.height - 20f, paint)

        return newBitmap
    }

    private fun compressImage(bitmap: Bitmap): Bitmap {
        var quality = 100
        var streamLength = Int.MAX_VALUE
        val file = File.createTempFile("temp", ".jpeg")

        while (streamLength > 5 * 1024 * 1024 && quality > 10) {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
            fos.close()
            streamLength = file.length().toInt()
            quality -= 10
        }

        return BitmapFactory.decodeFile(file.absolutePath)
    }

    private fun saveBitmapToCache(bitmap: Bitmap, sku: String): File {
        val cachePath = File(getApplication<Application>().cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "$sku.jpeg")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()
        return file
    }
}
