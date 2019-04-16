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
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

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
        numInProgress++
        //Thread.sleep(5000)
        runBlocking {
            val newProducts = getNewProducts(context)
            for (product in newProducts) {
                uploadSingleProduct(context, product)
            }
            val updateProducts = getUpdateProducts(context)
            for (product in updateProducts) {
                updateSingleProduct(context, product)
            }
        }
        Log.i(TAG, "Done uploading, now downloading")
        runBlocking {
            downloadProducts(context)
        }
        Log.i(TAG, "Done syncing!")
        numInProgress--
    }

    private fun downloadProducts(context: Context) {
        numInProgress++
        runBlocking {
            var artisans = getAllArtisans(context)

            deleteAllProducts(context)
            for (artisan in artisans) {
                downloadProductsForArtisan(context, artisan)
            }

            OrderSync.sync(context, ArtisanSync.mCgaId)
        }
        numInProgress--
    }

    fun updateProduct(context : Context, product: Product, artisan: Artisan, photos: ArrayList<File?>) {
        job = Job()
        var i = 0
        for (photo in photos) {
            stageImageUpdate(context, product, photo, i)
            i++
        }

        if (product.synced != SYNC_NEW)
            product.synced = SYNC_EDIT

        launch {
            updateProductHelper(context, product)
        }

    }

    private suspend fun updateProductHelper(context : Context, product: Product) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().update(product)
    }

    private fun downloadProductsForArtisan(context: Context, artisan : Artisan) {
        numInProgress++
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
                productDao.insertAll(products)
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ArtisanItemList", "failed to do POST request to database")
                numInProgress--
            }
        })
    }

    private suspend fun getAllArtisans(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().getAll()
    }

    private suspend fun deleteAllProducts(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().deleteAll()
    }

    private suspend fun getNewProducts(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().getAllBySyncState(SYNC_NEW)
    }

    private suspend fun getUpdateProducts(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().getAllBySyncState(SYNC_EDIT)
    }

    fun addProduct(context : Context, product: Product, artisan: Artisan, photos: ArrayList<File?>) {
        job = Job()
        var i = 0
        for (photo in photos) {
            stageImageUpdate(context, product, photo, i)
            i++
        }

        runBlocking {
            addProductHelper(context, product)
        }

    }

    private suspend fun addProductHelper(context : Context, product : Product) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().insert(product)
    }

    private fun updateSingleProduct(context: Context, product: Product) {
        numInProgress++

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
                .add("itemId", product.itemId)

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(editItemURL)
                .post(requestBody.build())
                .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                //product.itemId = body!!.substring(1, body!!.length - 1)
                Log.i(TAG, "success $body")

                /*var i = 0
                for (picURL in product.pictureURLs) {
                    if (picURL != "Not set" && picURL != "undefined" && picURL.substring(0, 5) != "https") {
                        uploadProductImage(context, product, i)
                        i++
                    }
                }*/
                for (i in 0..5) {
                    if (product.pictureURLs[i] != "Not set" && product.pictureURLs[i] != "undefined" && product.pictureURLs[i].substring(0, 5) != "https") {
                        uploadProductImage(context, product, i)
                    }
                }
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e(TAG, "failed to do POST request to database ${addItemURL}")
                numInProgress--
            }
        })
    }

    private fun uploadSingleProduct(context: Context, product: Product) {
        numInProgress++

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
                runBlocking {
                    setSyncedState(product, context)
                }
                product.itemId = body!!.substring(1, body!!.length - 1)
                Log.i(TAG, "success $body")

                runBlocking {
                    var i = 0
                    for (picURL in product.pictureURLs) {
                        if (picURL != "Not set" && picURL != "undefined") {
                            uploadProductImage(context, product, i++)
                        }
                    }
                }
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e(TAG, "failed to do POST request to database ${addItemURL}")
                numInProgress--
            }
        })
    }

    private fun uploadProductImage(context : Context, product : Product, index : Int) {
        numInProgress++
        var i = 0
        Log.i(TAG, "Uploading image with index " + index)
        product.pictureURLs.forEach { Log.i(TAG, (i++).toString() + " " + it) }

        //if (product.pictureURLs[index].substring(0, 5) == "https")
            //return

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

        val client = OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d(TAG, body)

                // Done uploading last image, so now get rid of the temp product
                Log.i(TAG, "Am I done uploading images? " + product.pictureURLs[index + 1])
                //if (index == product.pictureURLs.size - 1 || product.pictureURLs[index + 1] == "undefined" || product.pictureURLs[index + 1] == "Not set") {
                 //   runBlocking {
                       // Log.i(TAG, "Deleting temp product")
                        //setSyncedState(product, context)
                        //downloadProducts(context)
                   // }
               // }
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e(TAG, "failed to do POST request to database${addItemImageURL}")
                Log.e(TAG, e!!.message)
                numInProgress--
            }
        })
    }

    private suspend fun setSyncedState(product : Product, context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().deleteById(product.itemId)
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