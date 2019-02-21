package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
import com.amazonadonna.view.R
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import com.amazonadonna.database.ArtisanDao
import com.amazonadonna.model.Order
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_edit_artisan.*
import kotlinx.android.synthetic.main.activity_list_orders.*
import java.util.concurrent.TimeUnit

class ListAllArtisans : AppCompatActivity(), CoroutineScope {
    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    var cgaId: String = "0"
    val listAllArtisansURL = "https://7bd92aed.ngrok.io/artisan/listAllForCgo"
    val originalArtisans: MutableList<Artisan> = mutableListOf()
    val filteredArtisans: MutableList<Artisan> = mutableListOf()
    val oldFilteredArtisans: MutableList<Artisan> = mutableListOf()

    fun search(query: String): Completable = Completable.create {
        val wanted = originalArtisans.filter {
            it.artisanId.contains(query) || it.artisanName.contains(query) ||
                    it.city.contains(query) || it.country.contains(query) || it.bio.contains(query)
        }.toList()

        if (listArtisans_Search.text.toString() == "") { // empty search bar
            filteredArtisans.clear()
            filteredArtisans.addAll(originalArtisans)
        } else {
            filteredArtisans.clear()
            filteredArtisans.addAll(wanted)
        }
        Log.d("ListOrders", "editText: " + listArtisans_Search.text.toString())
        Log.d("ListOrders", "originalOrders: " + originalArtisans.toString())
        Log.d("ListOrders", "filteredOrders: " + filteredArtisans.toString())
        it.onComplete()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_all_artisans)
        cgaId = intent.extras.getString("cgaId")
        //TODO add search bar

        recyclerView_listAllartisans.layoutManager = LinearLayoutManager(this)

        //load an empty list as placeholder before GET request completes
        val emptyArtisanList: List<Artisan> = emptyList()
        recyclerView_listAllartisans.adapter = ListArtisanAdapter(this, originalArtisans)

        recyclerView_listAllartisans.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        toolbar_addartisan.setOnClickListener {
            addArtisan()
        }

        listArtisans_Search
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
    }

    override fun onStart() {
        super.onStart()
        job = Job()

        // If offline, do this instead
        //val artisanDao = AppDatabase.getDatabase(application).artisanDao()
        //val dbArtisans : List<Artisan> = artisanDao.getAll()
        launch {
            val dbArtisans: List<Artisan> = getArtisansFromDb()
            originalArtisans.addAll(dbArtisans)
            oldFilteredArtisans.addAll(dbArtisans)
            filteredArtisans.addAll(dbArtisans)
            runOnUiThread {
                recyclerView_listAllartisans.adapter = ListArtisanAdapter(applicationContext, oldFilteredArtisans)
            }
        }
        /*runOnUiThread {
            recyclerView_listAllartisans.adapter = ListArtisanAdapter(applicationContext, pullFromDb())
        }*/
        Log.d("ListAllArtisans", "fetching")
        //fetchJSON()
    }

    private suspend fun getArtisansFromDb() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(application).artisanDao().getAll() as List<Artisan>
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

        val artisanDao = AppDatabase.getDatabase(application).artisanDao()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("ListAllArtisan", "response body: " + body)

                val gson = GsonBuilder().create()
                //val artisans : List<com.amazonadonna.model.Artisan> =  gson.fromJson(body, mutableListOf<com.amazonadonna.model.Artisan>().javaClass)
                //System.out.print(artisans.get(0))
                val artisans: List<Artisan> = gson.fromJson(body, object : TypeToken<List<Artisan>>() {}.type)
                originalArtisans.addAll(artisans)
                oldFilteredArtisans.addAll(artisans)
                filteredArtisans.addAll(artisans)

                artisanDao.insertAll(artisans)

                runOnUiThread {
                    recyclerView_listAllartisans.adapter = ListArtisanAdapter(applicationContext, oldFilteredArtisans)
                }

            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ListAllArtisan", "failed to do POST request to database" + listAllArtisansURL)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

        inner class PostsDiffUtilCallback(private val oldList: List<Artisan>, private val newList: List<Artisan>) : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].artisanId == newList[newItemPosition].artisanId

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true // for the sake of simplicity we return true here but it can be changed to reflect a fine-grained control over which part of our views are updated

        }
}
