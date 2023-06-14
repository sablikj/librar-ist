package pt.ulisboa.tecnico.cmov.librarist.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ImageUtils @Inject constructor() {

    suspend fun uriToByteArray(contentResolver: ContentResolver, uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            var parcelFileDescriptor: ParcelFileDescriptor? = null
            try {
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)

                // Read the orientation from the Exif data
                val exif = fileDescriptor?.let { ExifInterface(it) }
                val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

                // Create a matrix to perform transformations on the bitmap
                val matrix = Matrix()

                // Rotate the bitmap according to the orientation
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                }

                // Create a new bitmap that has been rotated correctly
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                // Resizing the image
                val targetWidth = 800  // specify desired width
                val scaleFactor = targetWidth.toDouble() / rotatedBitmap.width.toDouble()
                val targetHeight = (rotatedBitmap.height * scaleFactor).toInt()
                val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, targetWidth, targetHeight, true)

                // Compressing the image and converting to ByteArray
                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

                return@withContext outputStream.toByteArray()
            } finally {
                // Ensure the ParcelFileDescriptor is closed
                parcelFileDescriptor?.close()
            }
        }
    }
}