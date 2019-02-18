package com.amazonadonna.view

import android.arch.persistence.room.Room
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_all_artisans.*
import com.amazonadonna.model.Artisan
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException
import com.google.gson.reflect.TypeToken
import android.support.v7.widget.DividerItemDecoration
import android.view.Menu
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.database.ArtisanDao
import kotlinx.android.synthetic.main.activity_edit_artisan.*


class ListAllArtisans : AppCompatActivity() {
    var cgaId : String = "0"
    val listAllArtisansURL = "https://7bd92aed.ngrok.io/artisan/listAllForCgo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_all_artisans)
        cgaId = intent.extras.getString("cgaId")
        //TODO add search bar

        recyclerView_listAllartisans.layoutManager = LinearLayoutManager(this)

        //load an empty list as placeholder before GET request completes
        val emptyArtisanList : List<Artisan> = emptyList()
        recyclerView_listAllartisans.adapter = ListArtisanAdapter(this, emptyArtisanList)

        recyclerView_listAllartisans.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        toolbar_addartisan.setOnClickListener{
            addArtisan()
        }
    }

    override fun onStart() {
        super.onStart()

        // If offline, do this instead
        /*val dbArtisans : List<Artisan> = artisanDao.getAll()
        runOnUiThread {
            recyclerView_listAllartisans.adapter = ListArtisanAdapter(applicationContext, dbArtisans)
        }*/
        Log.d("ListAllArtisans", "fetching")
        fetchJSON()
    }

    private fun addArtisan() {
        //go to add artisan screen
        val intent = Intent(this, AddArtisan::class.java)
        intent.putExtra("cgaId", cgaId)
        startActivity(intent)

    }

    private fun fetchJSON() {
        //TODO update cgo id to real
        val requestBody = FormBody.Builder().add("cgoId", cgaId)
                .build()

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(listAllArtisansURL)
                .post(requestBody)
                .build()

        val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "amazonadonna-main"
        ).fallbackToDestructiveMigration().build()
        val artisanDao = db.artisanDao()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("ListAllArtisan", "response body: " + body)

                val gson = GsonBuilder().create()
                //val artisans : List<com.amazonadonna.model.Artisan> =  gson.fromJson(body, mutableListOf<com.amazonadonna.model.Artisan>().javaClass)
                //System.out.print(artisans.get(0))
                val artisans : List<Artisan> = gson.fromJson(body,  object : TypeToken<List<Artisan>>() {}.type)

                artisanDao.insertAll(artisans)

                runOnUiThread {
                    recyclerView_listAllartisans.adapter = ListArtisanAdapter(applicationContext, artisans)
                }

            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ListAllArtisan", "failed to do POST request to database" + listAllArtisansURL)
            }
        })
    }
}
