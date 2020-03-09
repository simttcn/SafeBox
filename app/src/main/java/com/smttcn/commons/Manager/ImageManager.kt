package com.smttcn.commons.Manager

import android.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.smttcn.commons.helpers.SIZE_THUMBNAIL_HEIGHT
import com.smttcn.commons.helpers.SIZE_THUMBNAIL_WIDTH
import com.smttcn.safebox.MyApplication
import java.io.ByteArrayOutputStream
import java.io.File


object ImageManager {

    // Todo: to get thumbnail image of various file types here
    fun getThumbnailByteArrayFromEncryptedFile(file: File) : ByteArray? {
        val decryptedFile = FileManager.decryptFileContentToByteArray(file, MyApplication.getUS())

        // Image file type
        if (decryptedFile != null) {
            val result = resizeToByteArray(decryptedFile, SIZE_THUMBNAIL_WIDTH, SIZE_THUMBNAIL_HEIGHT)
            return result
        }

        return null
    }

    fun toBitmap(img: ByteArray) : Bitmap {
        // Get the bitmap from byte array since, the bitmap has the the resize function
        return BitmapFactory.decodeByteArray(img, 0, img.size)
    }

    fun resizeToBitmap(img: ByteArray, width: Int, height: Int) : Bitmap {
        // New bitmap with the correct size, may not return a null object
        return Bitmap.createScaledBitmap(toBitmap(img), width, height, false)
    }

    // Todo: to retain the aspect ration when resizing, choose to crop or padded with empty space
    fun resizeToByteArray(img: ByteArray, width: Int, height: Int) : ByteArray {
        // New bitmap with the correct size, may not return a null object
        val newImg = Bitmap.createScaledBitmap(toBitmap(img), width, height, false)
        val result = toByteArray(newImg)
        newImg.recycle()

        return result
    }

    fun toByteArray(img: Bitmap) : ByteArray {
        // Get the byte array from tbe bitmap to be returned
        val outputStream = ByteArrayOutputStream()
        img.compress(Bitmap.CompressFormat.PNG, 40, outputStream)
        return outputStream.toByteArray()
    }
}