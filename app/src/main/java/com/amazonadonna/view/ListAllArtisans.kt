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
import android.support.v7.app.AlertDialog
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
import com.amazonadonna.sync.Synchronizer
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
                val builder = AlertDialog.Builder(this@ListAllArtisans)
                builder.setTitle("Confirm Artisan Deletion")
                builder.setMessage("Are you sure you want to delete this artisan? This action cannot be undone.")

                builder.setPositiveButton("Permanently Delete"){dialog, which ->
                    (recyclerView_listAllartisans.adapter as ListArtisanAdapter).removeItem(viewHolder)
                    dialog.cancel()
                }

                builder.setNeutralButton("Cancel") { dialog, which ->
                    (recyclerView_listAllartisans.adapter as ListArtisanAdapter).notifyItemChanged(viewHolder.adapterPosition)
                    dialog.cancel()
                }

                var dialog = builder.create()
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.argb(255, 24, 163, 198))
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
        AppDatabase.getDatabase(application).artisanDao().getAllWithoutSyncState(Synchronizer.SYNC_DELETE) as List<Artisan>
    }

    private fun addArtisan() {
        //go to add artisan screen
        val intent = Intent(this, AddArtisan::class.java)
        intent.putExtra("cgaId", cgaId)
        startActivity(intent)

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
