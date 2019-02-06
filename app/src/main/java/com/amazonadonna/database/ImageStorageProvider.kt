package com.amazonadonna.database

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ImageStorageProvider(var context: Context) {
    companion object {
        const val ARTISAN_IMAGE_PREFIX = "artisanimg"
        const val ITEM_IMAGE_PREFIX = "itemimg"
    }

    fun saveBitmap(bitmap: Bitmap, imageName: String) {
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = context.openFileOutput(imageName, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileOutputStream?.close()
        }
    }

    fun imageExists(picName: String): Boolean {
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = context.openFileInput(picName)
        } catch (e: FileNotFoundException) {
            return false
        }

        return true
    }

    fun loadBitmap(picName: String): Bitmap? {
        var bitmap: Bitmap? = null
        var fileInputStream: FileInputStream? = null

        try {
            fileInputStream = context.openFileInput(picName)
            bitmap = BitmapFactory.decodeStream(fileInputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileInputStream?.close()
        }

        return bitmap
    }
}