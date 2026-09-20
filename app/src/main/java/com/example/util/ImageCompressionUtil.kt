package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.roundToInt

/**
 * Utility for client-side image compression:
 * - Max resolution 800x800 px
 * - JPEG / WebP quality 70-75%
 * - Target file size < 150 KB
 */
object ImageCompressionUtil {

    suspend fun compressImageFile(
        context: Context,
        inputUri: Uri,
        maxDimension: Int = 800,
        quality: Int = 75
    ): File = withContext(Dispatchers.IO) {
        val inputStream: InputStream? = context.contentResolver.openInputStream(inputUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val scaledBitmap = scaleDownPreserveRatio(originalBitmap, maxDimension)

        val outputFile = File(
            context.cacheDir,
            "compressed_selfie_${System.currentTimeMillis()}.jpg"
        )

        var currentQuality = quality
        var stream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, stream)

        // Ensure file is < 150 KB (153,600 bytes)
        while (stream.toByteArray().size > 150 * 1024 && currentQuality > 40) {
            stream.reset()
            currentQuality -= 10
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, stream)
        }

        val fos = FileOutputStream(outputFile)
        fos.write(stream.toByteArray())
        fos.flush()
        fos.close()
        stream.close()

        outputFile
    }

    suspend fun compressBitmapToBase64(
        bitmap: Bitmap,
        maxDimension: Int = 800,
        quality: Int = 75
    ): String = withContext(Dispatchers.IO) {
        val scaled = scaleDownPreserveRatio(bitmap, maxDimension)
        var currentQuality = quality
        var stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, currentQuality, stream)

        while (stream.toByteArray().size > 150 * 1024 && currentQuality > 40) {
            stream.reset()
            currentQuality -= 10
            scaled.compress(Bitmap.CompressFormat.JPEG, currentQuality, stream)
        }

        val bytes = stream.toByteArray()
        stream.close()
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun scaleDownPreserveRatio(realImage: Bitmap, maxImageSize: Int): Bitmap {
        val ratio = Math.min(
            maxImageSize.toFloat() / realImage.width,
            maxImageSize.toFloat() / realImage.height
        )
        if (ratio >= 1.0f) {
            return realImage
        }

        val width = (ratio * realImage.width).roundToInt()
        val height = (ratio * realImage.height).roundToInt()

        return Bitmap.createScaledBitmap(realImage, width, height, true)
    }
}
