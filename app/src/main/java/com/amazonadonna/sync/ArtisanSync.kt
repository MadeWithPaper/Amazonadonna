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


object ArtisanSync: Synchronizer(), CoroutineScope {
    //var cgaId : String = "0"
    private const val listAllArtisansURL = "https://99956e2a.ngrok.io/artisan/listAllForCga"
    private const val addArtisanURL = "https://99956e2a.ngrok.io/artisan/add"
    private const val artisanPicURL = "https://99956e2a.ngrok.io/artisan/updateImage"
    private const val editArtisanURL = "https://99956e2a.ngrok.io/artisan/edit"
    private const val updateArtisanURL = "https://99956e2a.ngrok.io/artisan/updateImage"
    private const val deleteArtisanURL = "https://99956e2a.ngrok.io/artisan/delete"

    override fun sync(context: Context, cgaId: String) {
        super.sync(context, cgaId)

        Log.i("ArtisanSync", "Syncing now!")
        numInProgress = 1
        Log.i("ArtisanSync", numInProgress.toString())

        runBlocking {
            PayoutSync.sync(context, cgaId)
        }

        uploadNewArtisans(context)
       /* Log.i("ArtisanSync", "Done uploading, now downloading")
        downloadArtisans(context)
        Log.i("ArtisanSync", "Done syncing!")*/

        /*ProductSync.sync(context, cgaId)
        OrderSync.sync(context, cgaId)*/
        Log.i("ArtisanSync", numInProgress.toString())
        numInProgress--
    }

    private fun uploadNewArtisans(context : Context) {
        runBlocking {
            val newArtisans = getNewArtisans(context)
            for (artisan in newArtisans) {
                uploadSingleArtisan(context, artisan)
            }
            val updateArtisans = getUpdateArtisans(context)
            for (artisan in updateArtisans) {
                updateSingleArtisan(context, artisan)
            }
            val deleteArtisans = getDeleteArtisans(context)
            for (artisan in deleteArtisans) {
                Log.i("ArtisanSync", "Delete artisan " + artisan.artisanId)
                deleteSingleArtisan(context, artisan)
            }
            Log.i("ArtisanSync", "Done uploading, now downloading")
            downloadArtisans(context)
            Log.i("ArtisanSync", "Done syncing!")
        }
    }

    private fun downloadArtisans(context : Context) {
        numInProgress++
        val requestBody = FormBody.Builder().add("cgaId", mCgaId)
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
                for (artisan in artisans) {
                    if(artisan.phoneNumber == null)
                        artisan.phoneNumber = "1234567890"
                }

                Log.d("HOTFIX2", artisans.toString())
                artisanDao.deleteAll()
                artisanDao.insertAll(artisans)
                Log.d("HOTFIX3", artisanDao.toString())

                Log.i("ArtisanSync", "Successfully synced Artisan data")

                runBlocking {
                    ProductSync.sync(context, mCgaId)
                }
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ListAllArtisan", "failed to do POST request to database" + listAllArtisansURL)
                numInProgress--
            }
        })
    }

    private fun uploadSingleArtisan(context: Context, artisan: Artisan) {
        numInProgress++

        val requestBody = FormBody.Builder().add("cgaId", artisan.cgaId)
                .add("bio", artisan.bio)
                .add("city",artisan.city)
                .add("country", artisan.country)
                .add("artisanName", artisan.artisanName)
                .add("phoneNumber", artisan.phoneNumber)
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
                launch {
                    setSyncedState(artisan, context)
                }
                val newArtisanId = body!!.substring(1, body!!.length - 1)

                Log.d("ArtisanSync", "OLDID " + artisan.artisanId)
                runBlocking {
                    updateItemsForArtisan(context, artisan.artisanId, newArtisanId)
                }

                artisan.artisanId = newArtisanId

                Log.i("AddArtisan", "success $body")
                if (artisan.picURL != "Not set") {
                    uploadArtisanImage(context, artisan)
                } else {
                    numInProgress--
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddArtisan", "failed to do POST request to database $addArtisanURL")
                numInProgress--
            }
        })
    }

    private fun updateSingleArtisan(context: Context, artisan: Artisan) {
        numInProgress++
        var updatePic = false

        val requestBody = FormBody.Builder().add("artisanId", artisan.artisanId)
                .add("cgaId", artisan.cgaId)
                .add("bio", artisan.bio)
                .add("city", artisan.city)
                .add("country", artisan.country)
                .add("artisanName", artisan.artisanName)
                .add("phoneNumber", artisan.phoneNumber)
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
                } else {
                    numInProgress--
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("EditArtisan", "failed to do POST request to database" + editArtisanURL)
                numInProgress--
            }
        })

    }

    private fun deleteSingleArtisan(context: Context, artisan: Artisan) {
        numInProgress++

        val requestBody = FormBody.Builder().add("artisanId", artisan.artisanId)

        val client = OkHttpClient()
        val request = Request.Builder()
                .url(deleteArtisanURL)
                .post(requestBody.build())
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("DeleteArtisan", body)

                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("DeleteArtisan", "failed to do POST request to database" + deleteArtisanURL)
                numInProgress--
            }
        })

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
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("EditArtisan", "failed to do POST request to database" + updateArtisanURL)
                numInProgress--
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
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddArtisan", "failed to do POST request to database$artisanPicURL")
                Log.e("AddArtisan", e!!.message)
                numInProgress--
            }
        })
    }

    fun addArtisan(context : Context, artisan : Artisan, photoFile: File? = null) {
        job = Job()
        stageImageUpdate(context, artisan, photoFile)

        launch {
            addArtisanHelper(context, artisan)
        }

    }

    fun updateArtisan(context : Context, artisan : Artisan, newPhoto: File? = null) {
        job = Job()
        stageImageUpdate(context, artisan, newPhoto)

        if (artisan.synced != SYNC_NEW)
            artisan.synced = SYNC_EDIT

        launch {
            updateArtisanHelper(context, artisan)
        }

    }

    fun deleteArtisan(context : Context, artisan : Artisan) {
        job = Job()

        if (artisan.synced != SYNC_NEW) {
            artisan.synced = SYNC_DELETE
            launch {
                updateArtisanHelper(context, artisan)
            }
        }
        else {
            artisan.synced = SYNC_DELETE
            launch {
                deleteArtisanHelper(context, artisan)
            }
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


    // --------------------------------------
    // DATABASE HELPERS
    // --------------------------------------
    private suspend fun addArtisanHelper(context : Context, artisan : Artisan) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().insert(artisan)
    }

    private suspend fun updateArtisanHelper(context : Context, artisan : Artisan) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().update(artisan)
    }

    private suspend fun deleteArtisanHelper(context : Context, artisan : Artisan) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().delete(artisan)
    }

    private suspend fun getNewArtisans(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().getAllBySyncState(SYNC_NEW)
    }

    private suspend fun getDeleteArtisans(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().getAllBySyncState(SYNC_DELETE)
    }

    private suspend fun getUpdateArtisans(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().getAllBySyncState(SYNC_EDIT)
    }

    private suspend fun updateItemsForArtisan(context : Context, oldArtisanId : String, newArtisanId : String) = withContext(Dispatchers.IO) {
        Log.d("ArtisanSync", oldArtisanId + " " + newArtisanId)
        AppDatabase.getDatabase(context).productDao().updateArtisanId(oldArtisanId, newArtisanId)
    }


    private suspend fun setSyncedState(artisan: Artisan, context : Context) = withContext(Dispatchers.IO) {
        //AppDatabase.getDatabase(context).artisanDao().setSyncedState(artisan.artisanId, SYNCED)
        AppDatabase.getDatabase(context).artisanDao().delete(artisan)
    }

}