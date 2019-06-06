package com.amazonadonna.sync

import android.app.Activity
import android.content.Context
import android.widget.Toast
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
import androidx.appcompat.app.AlertDialog
import com.amazonadonna.model.App


object ArtisanSync: Synchronizer(), CoroutineScope {
    //var cgaId : String = "0"
    private val listAllArtisansURL =  App.BACKEND_BASE_URL + "/artisan/listAllForCga"
    private val getArtisanURL =  App.BACKEND_BASE_URL + "/artisan/getById"
    private val addArtisanURL = App.BACKEND_BASE_URL + "/artisan/add"
    private val artisanPicURL = App.BACKEND_BASE_URL + "/artisan/updateImage"
    private val editArtisanURL = App.BACKEND_BASE_URL + "/artisan/edit"
    private val updateArtisanURL = App.BACKEND_BASE_URL + "/artisan/updateImage"
    private val deleteArtisanURL = App.BACKEND_BASE_URL + "/artisan/delete"

    override fun sync(context: Context, activity: Activity, cgaId: String) {
        super.sync(context, activity, cgaId)

        Log.i("ArtisanSync", "Syncing now!")
        numInProgress = 1
        Log.i("ArtisanSync", numInProgress.toString())

        runBlocking {
            PayoutSync.sync(context, activity, cgaId)
        }

        uploadNewArtisans(context, activity)

        Log.i("ArtisanSync", numInProgress.toString())
        numInProgress--
    }

    override fun syncArtisanMode(context: Context, activity: Activity, artisanId: String) {
        super.syncArtisanMode(context, activity, artisanId)

        Log.i("ArtisanSync", "Syncing now!")
        numInProgress = 1
        Log.i("ArtisanSync", numInProgress.toString())

        runBlocking {
            PayoutSync.syncArtisanMode(context, activity, mArtisanId)
        }

        runBlocking {
            val updateArtisans = getUpdateArtisans(context)
            for (artisan in updateArtisans) {
                updateSingleArtisan(context, artisan)
            }
            downloadArtisan(context, activity)
        }

        numInProgress--
    }

    private fun uploadNewArtisans(context : Context, activity: Activity) {
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
            downloadArtisans(context, activity)
            Log.i("ArtisanSync", "Done syncing!")
        }
    }

    private fun downloadArtisans(context : Context, activity: Activity) {
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
                var alertDialog : AlertDialog
                val body = response?.body()?.string()
                var artisans = listOf<Artisan>()
                Log.i("ListAllArtisan", "response body: " + body)

                val gson = GsonBuilder().create()

                try {
                    artisans = gson.fromJson(body, object : TypeToken<List<Artisan>>() {}.type)
                } catch (e: Exception) {
                    Log.d("ArtisanSync", "Caught exception")
                    activity.runOnUiThread {
                        Toast.makeText(context,"Please try again later. There may be unexpected behavior until a sync is complete.",Toast.LENGTH_LONG).show()
                    }
                }

                for (artisan in artisans) {
                    if(artisan.phoneNumber == null)
                        artisan.phoneNumber = "1234567890"
                    if(artisan.email == null)
                        artisan.email = "foo@gmail.com"
                }

                Log.d("HOTFIX2", artisans.toString())
                artisanDao.deleteAll()
                artisanDao.insertAll(artisans)
                Log.d("HOTFIX3", artisanDao.toString())

                Log.i("ArtisanSync", "Successfully synced Artisan data")

                runBlocking {
                    ProductSync.sync(context, activity, mCgaId)
                }
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ListAllArtisan", "failed to do POST request to database" + listAllArtisansURL)
                numInProgress--
            }
        })
    }

    private fun downloadArtisan(context : Context, activity: Activity) {
        numInProgress++
        val requestBody = FormBody.Builder().add("artisanId", mArtisanId)
                .build()

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(getArtisanURL)
                .post(requestBody)
                .build()

        val artisanDao = AppDatabase.getDatabase(context).artisanDao()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                var artisan: Artisan
                val body = response?.body()?.string()
                Log.i("ListAllArtisan", "response body: " + body)

                val gson = GsonBuilder().create()

                artisan = gson.fromJson(body, object : TypeToken<Artisan>() {}.type)

                if(artisan.phoneNumber == null)
                    artisan.phoneNumber = "1234567890"
                if(artisan.email == null)
                    artisan.email = "foo@gmail.com"


                artisanDao.deleteAll()
                artisanDao.insert(artisan)

                Log.i("ArtisanSync", "Successfully synced Artisan data")

                runBlocking {
                    ProductSync.syncArtisanMode(context, activity, mArtisanId)
                }
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ListAllArtisan", "failed to do POST request to database" + getArtisanURL)
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
                .add("email", artisan.email)
                .add("newAccount", artisan.newAccount.toString())
                .add("lat", artisan.lat.toString())
                .add("lon", artisan.lon.toString())
                .add("balance", artisan.balance.toString())
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
                if (body != null && !body!!.contains("<title>Error")) {
                    val newArtisanId = body!!.substring(1, body!!.length - 1)

                    Log.d("ArtisanSync", "OLDID " + artisan.artisanId)
                    runBlocking {
                        updateItemsForArtisan(context, artisan.artisanId, newArtisanId)
                        updatePayoutsForArtisan(context, artisan.artisanId, newArtisanId)
                    } 

                    artisan.artisanId = newArtisanId

                    Log.i("AddArtisan", "success $body")
                    if (artisan.picURL != "Not set") {
                        uploadArtisanImage(context, artisan)
                    } else {
                        numInProgress--
                    }
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
                .add("email", artisan.email)
                .add("newAccount", artisan.newAccount.toString())
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

                if (body != null && !body!!.contains("<title>Error") && updatePic) {
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

    private suspend fun updatePayoutsForArtisan(context : Context, oldArtisanId : String, newArtisanId : String) = withContext(Dispatchers.IO) {
        Log.d("ArtisanSync", "Updating payouts " + oldArtisanId + " " + newArtisanId)
        AppDatabase.getDatabase(context).payoutDao().updateArtisanIdAndUnsync(oldArtisanId, newArtisanId, SYNC_NEW)
    }

    private suspend fun setSyncedState(artisan: Artisan, context : Context) = withContext(Dispatchers.IO) {
        //AppDatabase.getDatabase(context).artisanDao().setSyncedState(artisan.artisanId, SYNCED)
        AppDatabase.getDatabase(context).artisanDao().delete(artisan)
    }

}