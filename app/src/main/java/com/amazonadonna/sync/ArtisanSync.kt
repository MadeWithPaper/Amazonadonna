package com.amazonadonna.sync

import android.content.Context
import android.util.Log
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.Artisan
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.IOException
import android.graphics.BitmapFactory
import android.graphics.Bitmap



object ArtisanSync: Syncronizer(), CoroutineScope {
    var cgaId : String = "0"
    private const val listAllArtisansURL = "https://7bd92aed.ngrok.io/artisan/listAllForCgo"
    private const val addArtisanURL = "https://7bd92aed.ngrok.io/artisan/add"
    private const val artisanPicURL = "https://7bd92aed.ngrok.io/artisan/updateImage"

    override fun sync(context: Context) {
        super.sync(context)

        Log.i("ArtisanSync", "Syncing now!")
        uploadNewArtisans(context)
        downloadArtisans(context)
        Log.i("ArtisanSync", "Done syncing!")

    }

    fun addArtisan(context : Context, artisan : Artisan, photoFile: File? = null) {
        if (photoFile != null) {
            val sourceFile = photoFile!!
            var fileName = artisan.artisanId + ".png"
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
            var isp = ImageStorageProvider(context)
            isp.saveBitmap(bitmap, ImageStorageProvider.ARTISAN_IMAGE_PREFIX + fileName)
            artisan.picURL = fileName
        }

        launch {
            addArtisanHelper(context, artisan)
        }

    }

    private suspend fun addArtisanHelper(context : Context, artisan : Artisan) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().insert(artisan)
    }

    private fun downloadArtisans(context : Context) {
        //TODO update cgo id to real
        val requestBody = FormBody.Builder().add("cgoId", cgaId)
                .build()

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(listAllArtisansURL)
                .post(requestBody)
                .build()

        val artisanDao = AppDatabase.getDatabase(context).artisanDao()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("ListAllArtisan", "response body: " + body)

                val gson = GsonBuilder().create()
                val artisans : List<Artisan> = gson.fromJson(body,  object : TypeToken<List<Artisan>>() {}.type)
                Log.d("HOTFIX2", artisans.toString())
                artisanDao.insertAll(artisans)
                Log.d("HOTFIX3", artisanDao.toString())

                Log.i("ArtisanSync", "Successfully synced Artisan data")
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ListAllArtisan", "failed to do POST request to database" + listAllArtisansURL)
            }
        })
    }

    private fun uploadNewArtisans(context : Context) {
        launch {
            val newArtisans = getNewArtisans(context)
            for (artisan in newArtisans) {
                Log.i("ArtisanSync", artisan.synced.toString())
                uploadSingleArtisan(context, artisan)
            }
        }
    }

    private suspend fun getNewArtisans(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().getAllBySyncState(SYNC_NEW)
    }

    private suspend fun setSyncedState(artisan: Artisan, context : Context) = withContext(Dispatchers.IO) {
        //AppDatabase.getDatabase(context).artisanDao().setSyncedState(artisan.artisanId, SYNCED)
        AppDatabase.getDatabase(context).artisanDao().delete(artisan)
    }

    private fun uploadArtisanImage(context : Context, artisan: Artisan) {
        val sourceFile: File = context.getFileStreamPath(ImageStorageProvider.ARTISAN_IMAGE_PREFIX + artisan.picURL)

        val MEDIA_TYPE = MediaType.parse("image/png")

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("artisanId", artisan.artisanId)
                .addFormDataPart("image", "profile.png", RequestBody.create(MEDIA_TYPE, sourceFile))
                .build()

        val request = Request.Builder()
                .url(artisanPicURL)
                .post(requestBody)
                .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("AddArtisan", body)
                launch {
                    setSyncedState(artisan, context)
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddArtisan", "failed to do POST request to database$artisanPicURL")
                Log.e("AddArtisan", e!!.message)
            }
        })
    }

    private fun uploadSingleArtisan(context: Context, artisan: Artisan) {

        val requestBody = FormBody.Builder().add("cgoId", artisan.cgoId)
                .add("bio", artisan.bio)
                .add("city",artisan.city)
                .add("country", artisan.country)
                .add("artisanName", artisan.artisanName)
                .add("lat", artisan.lat.toString())
                .add("lon", artisan.lon.toString())
                .add("balance", "5000.0")
                .build()

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(addArtisanURL)
                .post(requestBody)
                .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                artisan.artisanId = body!!
                Log.i("AddArtisan", "success $body")
                if (artisan.picURL != "Not set") {
                    uploadArtisanImage(context, artisan)
                }
                else {
                    launch {
                        setSyncedState(artisan, context)
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddArtisan", "failed to do POST request to database $addArtisanURL")
            }
        })
    }

}