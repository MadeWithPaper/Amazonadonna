package com.amazonadonna.view

import android.arch.persistence.room.Room
import android.content.Intent
import android.database.Cursor
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.Loader
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_all_artisans.*
import com.amazonadonna.model.Artisan
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException
import com.google.gson.reflect.TypeToken
import android.support.v7.widget.DividerItemDecoration
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.database.ArtisanDao
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class ListAllArtisans : AppCompatActivity(), LoaderCallbacks<Cursor> {
    var cgaId : String = "0"
    val listAllArtisansURL = "https://7bd92aed.ngrok.io/artisan/listAllForCgo"
    val originalArtisans : MutableList<Artisan> = mutableListOf()
    val filteredArtisans: MutableList<Artisan> = mutableListOf()
    val oldFilteredArtisans: MutableList<Artisan> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_all_artisans)
        cgaId = intent.extras.getString("cgaId")

        recyclerView_listAllartisans.layoutManager = LinearLayoutManager(this)

        //load an empty list as placeholder before GET request completes
        //val emptyArtisanList : List<Artisan> = emptyList()
        recyclerView_listAllartisans.adapter = ListArtisanAdapter(this, originalArtisans)
        recyclerView_listAllartisans.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        listArtisan_Search
                .textChanges()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe {
                    search(it.toString())
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                val diffResult = DiffUtil.calculateDiff(PostsDiffUtilCallback(oldFilteredArtisans, filteredArtisans))
                                oldFilteredArtisans.clear()
                                oldFilteredArtisans.addAll(filteredArtisans)
                                diffResult.dispatchUpdatesTo((recyclerView_listAllartisans.adapter as ListArtisanAdapter))
                            }
                }

        toolbar_addartisan.setOnClickListener{
            addArtisan()
        }
    }

    private fun search(query: String): Completable = Completable.create {
        val wanted = originalArtisans.filter { it ->
            it.artisanName.contains(query) || it.city.contains(query) || it.country.contains(query)
        }.toList()

        if (listArtisan_Search.text.toString() == "") { // empty search bar
            filteredArtisans.clear()
            filteredArtisans.addAll(originalArtisans)
        }
        else {
            filteredArtisans.clear()
            filteredArtisans.addAll(wanted)
        }
        Log.d("ListAllArtisan", "editText: "+listArtisan_Search.text.toString())
        Log.d("ListAllArtisan", "originalArtisans: $originalArtisans")
        Log.d("ListAllArtisan", "filteredArtisans: $filteredArtisans")
        it.onComplete()
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
                originalArtisans.addAll(artisans)
                oldFilteredArtisans.addAll(artisans)
                filteredArtisans.addAll(artisans)

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

    override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<Cursor> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoadFinished(p0: Loader<Cursor>, p1: Cursor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoaderReset(p0: Loader<Cursor>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class PostsDiffUtilCallback(private val oldList: List<Artisan>, private val newList: List<Artisan>) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].artisanId == newList[newItemPosition].artisanId

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true // for the sake of simplicity we return true here but it can be changed to reflect a fine-grained control over which part of our views are updated
    }
}
