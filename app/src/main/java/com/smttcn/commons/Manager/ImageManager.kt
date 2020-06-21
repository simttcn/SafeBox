package com.smttcn.commons.Manager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.smttcn.commons.helpers.SIZE_THUMBNAIL_HEIGHT
import com.smttcn.commons.helpers.SIZE_THUMBNAIL_WIDTH
import com.smttcn.safebox.MyApplication
import java.io.ByteArrayOutputStream
import java.io.File


object ImageManager {

    fun toBitmap(img: ByteArray) : Bitmap {
        // Get the bitmap from byte array since, the bitmap has the the resize function
        return BitmapFactory.decodeByteArray(img, 0, img.size)
    }

    fun toByteArray(img: Bitmap) : ByteArray {
        // Get the byte array from tbe bitmap to be returned
        val outputStream = ByteArrayOutputStream()
        img.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
        return outputStream.toByteArray()
    }

    fun resizeToBitmap(img: ByteArray, width: Int, height: Int) : Bitmap? {
        // New bitmap with the correct size, may not return a null object
        // New bitmap with the correct size, may not return a null object
        val originalImg = toBitmap(img)
        val newImg = resize(originalImg, width, height)

        if (newImg == null)
            return null

        //val result = toByteArray(newImg)
        originalImg.recycle()

        return newImg
    }

    fun resizeToByteArray(img: ByteArray, width: Int, height: Int) : ByteArray? {
        // New bitmap with the correct size, may not return a null object
        val originalImg = toBitmap(img)
        val newImg = resize(originalImg, width, height)

        if (newImg == null)
            return null

        val result = toByteArray(newImg)
        originalImg.recycle()
        newImg.recycle()

        return result
    }

    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap? {

        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        } else {
            null
        }
    }
}