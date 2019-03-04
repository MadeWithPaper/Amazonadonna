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
import kotlinx.android.synthetic.main.activity_edit_artisan.*


object ArtisanSync: Syncronizer(), CoroutineScope {
    //var cgaId : String = "0"
    private const val listAllArtisansURL = "https://99956e2a.ngrok.io/artisan/listAllForCgo"
    private const val addArtisanURL = "https://99956e2a.ngrok.io/artisan/add"
    private const val artisanPicURL = "https://99956e2a.ngrok.io/artisan/updateImage"
    private val editArtisanURL = "https://99956e2a.ngrok.io/artisan/edit"
    private val updateArtisanURL = "https://99956e2a.ngrok.io/artisan/updateImage"

    override fun sync(context: Context, cgaId: String) {
        super.sync(context, cgaId)

        Log.i("ArtisanSync", "Syncing now!")
        uploadNewArtisans(context)
        Log.i("ArtisanSync", "Done uploading, now downloading")
        downloadArtisans(context)
        Log.i("ArtisanSync", "Done syncing!")

    }

    fun addArtisan(context : Context, artisan : Artisan, photoFile: File? = null) {
        stageImageUpdate(context, artisan, photoFile)

        launch {
            addArtisanHelper(context, artisan)
        }

    }

    fun updateArtisan(context : Context, artisan : Artisan, newPhoto: File? = null) {
        stageImageUpdate(context, artisan, newPhoto)
        artisan.synced = SYNC_EDIT

        launch {
            updateArtisanHelper(context, artisan)
        }

    }

    private fun stageImageUpdate(context : Context, artisan : Artisan, photoFile: File? = null) {
        if (photoFile != null) {
            val sourceFile = photoFile!!
            var fileName = artisan.artisanId + ".png"
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
            var isp = ImageStorageProvider(context)
            //isp.deleteBitmap(fileName)
            isp.saveBitmap(bitmap, ImageStorageProvider.ARTISAN_IMAGE_PREFIX + fileName)
            artisan.picURL = fileName
        }
    }

    private suspend fun addArtisanHelper(context : Context, artisan : Artisan) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().insert(artisan)
    }

    private suspend fun updateArtisanHelper(context : Context, artisan : Artisan) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().update(artisan)
    }

    private fun downloadArtisans(context : Context) {
        val requestBody = FormBody.Builder().add("cgoId", mCgaId)
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
                artisanDao.deleteAll()
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
                uploadSingleArtisan(context, artisan)
            }
            val updateArtisans = getUpdateArtisans(context)
            for (artisan in updateArtisans) {
                updateSingleArtisan(context, artisan)
            }
        }
    }

    private suspend fun getNewArtisans(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().getAllBySyncState(SYNC_NEW)
    }

    private suspend fun getUpdateArtisans(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().getAllBySyncState(SYNC_EDIT)
    }


    private suspend fun setSyncedState(artisan: Artisan, context : Context) = withContext(Dispatchers.IO) {
        //AppDatabase.getDatabase(context).artisanDao().setSyncedState(artisan.artisanId, SYNCED)
        AppDatabase.getDatabase(context).artisanDao().delete(artisan)
    }

    fun updateArtisanImage(context : Context, artisan: Artisan) {
        val sourceFile: File = context.getFileStreamPath(ImageStorageProvider.ARTISAN_IMAGE_PREFIX + artisan.picURL)
        //val sourceFile = photoFile!!
        Log.d("EditArtisan", "submitPictureToDB file" + sourceFile + " : " + sourceFile!!.exists())

        val MEDIA_TYPE = MediaType.parse("image/png")

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("artisanId", artisan.artisanId)
                .addFormDataPart("image", "editProfilePic.png", RequestBody.create(MEDIA_TYPE, sourceFile))
                .build()

        val request = Request.Builder()
                .url(updateArtisanURL)
                .post(requestBody)
                .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("EditArtisan", body)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("EditArtisan", "failed to do POST request to database" + updateArtisanURL)
            }
        })
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
                .add("contactNumber", artisan.contactNumber)
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
                artisan.artisanId = body!!.substring(1, body!!.length - 1)
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

    private fun updateSingleArtisan(context: Context, artisan: Artisan) {
        var updatePic = false

        val requestBody = FormBody.Builder().add("artisanId", artisan.artisanId)
                .add("cgoId", artisan.cgoId)
                .add("bio", artisan.bio)
                .add("city", artisan.city)
                .add("country", artisan.country)
                .add("artisanName", artisan.artisanName)
                .add("contactNumber", artisan.contactNumber)
                .add("lat", artisan.lat.toString())
                .add("lon", artisan.lat.toString())
                .add("balance", artisan.balance.toString())

        if (artisan.picURL!!.substring(0, 5) == "https") {
            requestBody.add("picURL", artisan.picURL)
        } else {
            updatePic = true
        }

        val client = OkHttpClient()
        val request = Request.Builder()
                .url(editArtisanURL)
                .post(requestBody.build())
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("EditArtisan", body)

                if (updatePic) {
                    updateArtisanImage(context, artisan)
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("EditArtisan", "failed to do POST request to database" + editArtisanURL)
            }
        })

    }

}