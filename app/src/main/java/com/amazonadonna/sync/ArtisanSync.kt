package com.amazonadonna.syncadapter

import android.content.Context
import android.util.Log
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.Artisan
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext

object ArtisanSync: Syncronizer(), CoroutineScope {
    var cgaId : String = "0"
    private const val listAllArtisansURL = "https://7bd92aed.ngrok.io/artisan/listAllForCgo"
    private const val addArtisanURL = "https://7bd92aed.ngrok.io/artisan/add"
    private const val artisanPicURL = "https://7bd92aed.ngrok.io/artisan/updateImage"

    override fun sync(context: Context) {
        //job = Job()
        super.sync(context)

        Log.i("ArtisanSync", "Syncing now!")
        downloadArtisans(context)
        uploadNewArtisans(context)
        Log.i("ArtisanSync", "Done syncing!")

    }

    fun addArtisan(context : Context, artisan : Artisan) {
        launch {
            addArtisanHelper(context, artisan)
        }
    }

    private suspend fun addArtisanHelper(context : Context, artisan : Artisan) = withContext(Dispatchers.IO) {
        val artisanDao = AppDatabase.getDatabase(context).artisanDao().insert(artisan)
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

                artisanDao.insertAll(artisans)
                //artisanDao.deleteAll()

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
                uploadSingleArtisan(artisan)
            }
        }
    }

    private suspend fun getNewArtisans(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).artisanDao().getAllBySyncState(SYNC_NEW) as List<Artisan>
    }

    private fun uploadSingleArtisan(artisan: Artisan) {

        val requestBody = FormBody.Builder().add("artisanId",artisan.artisanId)
                .add("cgoId", artisan.cgoId)
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
                Log.i("AddArtisan", "success $body")
                //submitPictureToDB(artisan)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddArtisan", "failed to do POST request to database $addArtisanURL")
            }
        })
    }

}