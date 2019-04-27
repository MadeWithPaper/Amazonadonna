package com.amazonadonna.view

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
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
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.view.R
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import com.amazonadonna.database.ArtisanDao
import com.amazonadonna.sync.ArtisanSync
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ListAllArtisans : AppCompatActivity(), CoroutineScope {
    lateinit var job: Job
    lateinit var deleteIcon: Drawable

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    var cgaId: String = "0"
    val listAllArtisansURL = "https://99956e2a.ngrok.io/artisan/listAllForCga"
    val originalArtisans: MutableList<Artisan> = mutableListOf()
    val filteredArtisans: MutableList<Artisan> = mutableListOf()
    val oldFilteredArtisans: MutableList<Artisan> = mutableListOf()
    var swipeBackground = ColorDrawable(Color.parseColor("#FF0000"))


    override fun onCreate(savedInstanceState: Bundle?) {
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!
        filteredArtisans.clear()
        originalArtisans.clear()
        oldFilteredArtisans.clear()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_all_artisans)
        cgaId = intent.extras.getString("cgaId")

        recyclerView_listAllartisans.layoutManager = LinearLayoutManager(this)

        //load an empty list as placeholder before GET request completes
       // originalArtisans.clear()
        recyclerView_listAllartisans.adapter = ListArtisanAdapter(this, originalArtisans)
        recyclerView_listAllartisans.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        toolbar_addartisan.setOnClickListener {
            addArtisan()
        }

        listArtisans_Search
                .textChanges()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe {
                    search(it.toString().toLowerCase())
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                val diffResult = DiffUtil.calculateDiff(PostsDiffUtilCallback(oldFilteredArtisans, filteredArtisans))
                                oldFilteredArtisans.clear()
                                oldFilteredArtisans.addAll(filteredArtisans)
                                diffResult.dispatchUpdatesTo((recyclerView_listAllartisans.adapter as ListArtisanAdapter))
                            }
                }
        //item swipe to delete functionality
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                (recyclerView_listAllartisans.adapter as ListArtisanAdapter).removeItem(viewHolder)
            }
            //drawing the red rectangle with icon when swiping
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

                if (dX < 0) {
                    swipeBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon.setBounds(itemView.right - iconMargin - deleteIcon.intrinsicWidth,itemView.top + iconMargin, itemView.right - iconMargin, itemView.bottom - iconMargin)
                    deleteIcon.level = 1
                }
                swipeBackground.draw(c)

                c.save()


                if (dX < 0) {
                    c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                }

                deleteIcon.draw(c)
                c.restore()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView_listAllartisans)

    }


    override fun onResume() {
        super.onResume()

        //ArtisanSync.sync(this, cgaId)
    }

    override fun onStart() {
        filteredArtisans.clear()
        originalArtisans.clear()
        oldFilteredArtisans.clear()

        super.onStart()
        job = Job()

        launch {
            val dbArtisans: List<Artisan> = getArtisansFromDb()
            originalArtisans.addAll(dbArtisans)
            oldFilteredArtisans.addAll(dbArtisans)
            filteredArtisans.addAll(dbArtisans)
            runOnUiThread {
                recyclerView_listAllartisans.adapter = ListArtisanAdapter(applicationContext, oldFilteredArtisans)
            }
        }

        Log.d("ListAllArtisans", "fetching")
    }

    private fun search(query: String): Completable = Completable.create {
        val wanted = originalArtisans.filter {
            it.artisanName.contains(query, true) || it.city.contains(query, true) || it.country.contains(query, true) || it.bio.contains(query, true)
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
        //TODO update cga id to real
        Log.d("ListAllArtisans", "getting artisans for: "+cgaId)
        val requestBody = FormBody.Builder().add("cgaId", cgaId)
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
                Log.d("ListAllArtisan", "response body: " + body)

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
