package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Utilitas untuk validasi format (JPG/PNG), validasi batas ukuran,
 * serta kompresi gambar sebelum diunggah ke Firebase Storage.
 */
object ImageUtils {

    const val DEFAULT_MAX_MB = 5
    const val DEFAULT_MAX_DIMENSION = 1600
    const val DEFAULT_COMPRESSION_QUALITY = 80

    /**
     * Memvalidasi format gambar: hanya JPG dan PNG yang diperbolehkan.
     */
    fun validateImageFormat(context: Context, uri: Uri): Result<String> {
        val mimeType = context.contentResolver.getType(uri)?.lowercase()
        val path = uri.path?.lowercase().orEmpty()

        val isJpeg = mimeType == "image/jpeg" || mimeType == "image/jpg" || path.endsWith(".jpg") || path.endsWith(".jpeg")
        val isPng = mimeType == "image/png" || path.endsWith(".png")

        return if (isJpeg) {
            Result.success("image/jpeg")
        } else if (isPng) {
            Result.success("image/png")
        } else {
            Result.failure(IllegalArgumentException("Format berkas tidak didukung. Harap pilih gambar dengan format JPG atau PNG."))
        }
    }

    /**
     * Memeriksa apakah ukuran berkas melebihi batas maksimum (default 5MB).
     */
    fun validateImageSize(context: Context, uri: Uri, maxMb: Int = DEFAULT_MAX_MB): Result<Long> {
        var sizeBytes: Long = -1L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && cursor.moveToFirst() && !cursor.isNull(sizeIndex)) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        } catch (_: Exception) {
            // Gunakan fallback AssetFileDescriptor / stream
        }

        if (sizeBytes <= 0L) {
            try {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                    if (afd.length > 0L) sizeBytes = afd.length
                }
            } catch (_: Exception) {
                // Abaikan dan lanjut ke pembacaan stream
            }
        }

        val maxBytes = maxMb * 1024L * 1024L
        if (sizeBytes <= 0L) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val read = stream.read(buffer)
                        if (read == -1) break
                        total += read
                        if (total > maxBytes) {
                            return Result.failure(
                                IllegalArgumentException("Ukuran gambar melebihi batas maksimum $maxMb MB.")
                            )
                        }
                    }
                    sizeBytes = total
                }
            } catch (_: Exception) {
                sizeBytes = -1L
            }
        }

        return if (sizeBytes > maxBytes) {
            Result.failure(IllegalArgumentException("Ukuran gambar (${sizeBytes / (1024 * 1024)} MB) melebihi batas maksimum $maxMb MB."))
        } else {
            Result.success(sizeBytes)
        }
    }

    /**
     * Mengompresi gambar dari Uri:
     * - Downsample jika resolusi lebih besar dari maxDimension.
     * - Menyesuaikan orientasi EXIF agar foto kamera tidak miring.
     * - Mempertahankan transparansi PNG bila preserveAlpha = true, atau JPEG untuk ukuran ringan.
     */
    fun compressImage(
        context: Context,
        uri: Uri,
        maxDimension: Int = DEFAULT_MAX_DIMENSION,
        quality: Int = DEFAULT_COMPRESSION_QUALITY,
        preserveAlpha: Boolean = false
    ): Result<ByteArray> {
        return try {
            // 1. Baca dimensi asli tanpa memuat seluruh bitmap ke memori
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return Result.failure(IllegalStateException("Tidak dapat membuka file gambar."))

            val origWidth = options.outWidth
            val origHeight = options.outHeight

            if (origWidth <= 0 || origHeight <= 0) {
                return Result.failure(IllegalStateException("Gambar tidak valid atau korup."))
            }

            // 2. Hitung rasio inSampleSize
            var inSampleSize = 1
            val maxEdge = max(origWidth, origHeight)
            if (maxEdge > maxDimension) {
                while ((maxEdge / inSampleSize) > maxDimension) {
                    inSampleSize *= 2
                }
            }

            // 3. Decode bitmap dengan inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = if (preserveAlpha) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
            }

            val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return Result.failure(IllegalStateException("Gagal memuat gambar ke memori."))

            // 4. Koreksi rotasi EXIF (umum pada foto kamera)
            val rotationDegrees = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    when (ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                } ?: 0f
            } catch (_: Exception) {
                0f
            }

            val finalBitmap = if (rotationDegrees != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                Bitmap.createBitmap(
                    decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true
                ).also {
                    if (it !== decodedBitmap) decodedBitmap.recycle()
                }
            } else {
                decodedBitmap
            }

            // 5. Kompresi sesuai format
            val outputStream = ByteArrayOutputStream()
            val compressFormat = if (preserveAlpha) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            finalBitmap.compress(compressFormat, quality, outputStream)
            finalBitmap.recycle()

            Result.success(outputStream.toByteArray())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
