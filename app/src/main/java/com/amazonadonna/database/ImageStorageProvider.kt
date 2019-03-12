package com.amazonadonna.database

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.amazonadonna.view.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_artisan_profile.*
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.drawable.Drawable
import android.util.Log


class ImageStorageProvider(var context: Context) {
    companion object {
        const val ARTISAN_IMAGE_PREFIX = "artisanimg"
        const val ITEM_IMAGE_PREFIX = "itemimg"
        const val PAYOUT_IMAGE_PREFIX = "payoutimg"
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

    private fun imageExists(picName: String): Boolean {
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = context.openFileInput(picName)
        } catch (e: FileNotFoundException) {
            return false
        }

        return true
    }

    private fun loadBitmap(picName: String): Bitmap? {
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

    fun deleteBitmap(picName: String): Bitmap? {
        var bitmap: Bitmap? = null
        var fileInputStream: FileInputStream? = null

        try {
            context.deleteFile(picName)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileInputStream?.close()
        }

        return bitmap
    }

    // Load the given picture into the given ImageView using logic:
    //      If image is available locally, then use local image
    //      Else load the image from the server and store it locally
    fun loadImageIntoUI(picURL: String?, iv: ImageView, prefix: String, viewContext: Context) {
        if (picURL != "Not set" && picURL != "undefined" && picURL != null) {
            var url = picURL

            // If image is already on S3
            if (url.substring(0, 5) == "https") {
                var fileName = prefix +
                        url.substring(url.lastIndexOf('/') + 1, url.length)

                if (!imageExists(fileName!!)) {
                    Log.d("ISP", "Retrieving image from S3")
                    Picasso.with(viewContext).load(url).into(
                            iv,
                            object: com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    var drawable = iv.drawable as BitmapDrawable
                                    saveBitmap(drawable.bitmap, fileName)
                                }
                                override fun onError() {

                                }
                            })
                } else {
                    iv.setImageBitmap(loadBitmap(fileName))
                }
            }
            else {
                var fileName = prefix + url
                iv.setImageBitmap(loadBitmap(fileName))
            }
        }
        else
            iv.setImageResource(R.drawable.placeholder)
    }
}