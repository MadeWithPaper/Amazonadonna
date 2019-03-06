package com.amazonadonna.sync

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.database.PictureListTypeConverter
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.File
import java.io.IOException
import java.util.*

object ProductSync: Synchronizer(), CoroutineScope {
    private const val TAG = "ProductSync"
    private const val addItemURL = "https://99956e2a.ngrok.io/item/add"
    private const val addItemImageURL = "https://99956e2a.ngrok.io/item/updateImage"
    private const val editItemURL = "https://99956e2a.ngrok.io/item/editItem"
    private const val listAllItemsURL = "https://99956e2a.ngrok.io/item/listAllForArtisan"

    override fun sync(context: Context, cgaId: String) {
        super.sync(context, cgaId)

        Log.i(TAG, "Syncing now!")
        uploadProducts(context)

    }

    private fun uploadProducts(context: Context) {
        launch {
            val newProducts = getNewProducts(context)
            for (product in newProducts) {
                uploadSingleProduct(context, product)
            }
            val updateProducts = getUpdateProducts(context)
            for (product in updateProducts) {
                //updateSingleProduct(context, product)
            }
            Log.i(TAG, "Done uploading, now downloading")
            downloadProducts(context)
            Log.i(TAG, "Done syncing!")
        }
    }

    private fun downloadProducts(context: Context) {
        launch {
            var artisans = getAllArtisans(context)

            for (artisan in artisans) {
                downloadProductsForArtisan(context, artisan)
            }

            OrderSync.sync(context, ArtisanSync.mCgaId)
        }
    }

    private fun downloadProductsForArtisan(context: Context, artisan : Artisan) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder().add("artisanId",artisan.artisanId).build()

        val request = Request.Builder()
                .url(listAllItemsURL)
                .post(requestBody)
                .build()

        val productDao = AppDatabase.getDatabase(context).productDao()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("ArtisanItemList", body)
                val gson = GsonBuilder().create()

                val products : List<Product> = gson.fromJson(body,  object : TypeToken<List<Product>>() {}.type)
                for (product in products) {
                    product.pictureURLs = Array(PictureListTypeConverter.NUM_PICS, { i -> "undefined"})
                    product.pictureURLs[0] = product.pic0URL
                    product.pictureURLs[1] = product.pic1URL
                    product.pictureURLs[2] = product.pic2URL
                    product.pictureURLs[3] = product.pic3URL
                    product.pictureURLs[4] = product.pic4URL
                    product.pictureURLs[5] = product.pic5URL
                    Log.i(TAG, product.itemId + " " + product.pictureURLs[0])
                }
                //productDao.deleteAll()
                productDao.insertAll(products)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ArtisanItemList", "failed to do POST request to database")
            }
        })
    }

    private suspend fun getAllArtisans(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().getAll()
    }

    private suspend fun getNewProducts(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().getAllBySyncState(SYNC_NEW)
    }

    private suspend fun getUpdateProducts(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().getAllBySyncState(SYNC_EDIT)
    }

    fun addProduct(context : Context, product: Product, artisan: Artisan, photos: ArrayList<File?>) {
        var i = 0
        for (photo in photos) {
            stageImageUpdate(context, product, photo, i)
            i++
        }

        launch {
            addProductHelper(context, product)
        }

    }

    private suspend fun addProductHelper(context : Context, product : Product) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().insert(product)
    }

    private fun uploadSingleProduct(context: Context, product: Product) {

        val requestBody = FormBody.Builder().add("itemName", product.itemName)
                .add("price", product.price.toString())
                .add("description", product.description)
                .add("artisanId", product.artisanId)
                .add("category", product.category)
                .add("subCategory", product.subCategory)
                .add("specificCategory", product.specificCategory)
                .add("shippingOption", product.shippingOption)
                .add("itemQuantity", product.itemQuantity.toString())
                .add("productionTime", product.productionTime.toString())

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(addItemURL)
                .post(requestBody.build())
                .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                product.itemId = body!!.substring(1, body!!.length - 1)
                Log.i("AddArtisan", "success $body")
                /*if (artisan.picURL != "Not set") {
                    ArtisanSync.uploadArtisanImage(context, artisan)
                }
                else {
                    launch {
                        ArtisanSync.setSyncedState(artisan, context)
                    }
                }*/
                var i = 0
                for (picURL in product.pictureURLs) {
                    uploadProductImage(context, product, i++)
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddProduct", "failed to do POST request to database ${addItemURL}")
            }
        })
    }

    private fun uploadProductImage(context : Context, product : Product, index : Int) {
        val sourceFile: File = context.getFileStreamPath(ImageStorageProvider.ITEM_IMAGE_PREFIX + product.pictureURLs[index])

        val MEDIA_TYPE = MediaType.parse("image/png")

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("itemId", product.itemId)
                .addFormDataPart("picIndex", index.toString())
                .addFormDataPart("image", "itemImage.png", RequestBody.create(MEDIA_TYPE, sourceFile))
                .build()

        val request = Request.Builder()
                .url(addItemImageURL)
                .post(requestBody)
                .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("AddArtisan", body)

                // Done uploading last image, so now get rid of the temp product
                if (index == product.pictureURLs.size - 1 || product.pictureURLs[index + 1] == "undefined") {
                    launch {
                        setSyncedState(product, context)
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddArtisan", "failed to do POST request to database${addItemImageURL}")
                Log.e("AddArtisan", e!!.message)
            }
        })
    }

    private suspend fun setSyncedState(product : Product, context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().delete(product)
    }

    private fun stageImageUpdate(context : Context, product : Product, photoFile: File? = null, index : Int) {
        if (photoFile != null) {
            val sourceFile = photoFile!!
            var fileName = product.itemId + index + ".png"
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
            var isp = ImageStorageProvider(context)
            isp.saveBitmap(bitmap, ImageStorageProvider.ITEM_IMAGE_PREFIX + fileName)
            product.pictureURLs[index] = fileName
        }
    }
}