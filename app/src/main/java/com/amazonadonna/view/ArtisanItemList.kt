package com.amazonadonna.view

import android.content.ClipData
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.util.Log
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.App
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import com.amazonadonna.sync.Synchronizer
import com.amazonadonna.view.R
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_artisan_item_list.*
import kotlinx.android.synthetic.main.list_all_artisans.*
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class ArtisanItemList : AppCompatActivity() , CoroutineScope {
    lateinit var job: Job
    lateinit var deleteIcon: Drawable

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    lateinit var artisan : Artisan
    private val listAllItemsURL = App.BACKEND_BASE_URL + "/item/listAllForArtisan"
    private val originalItems: MutableList<Product> = mutableListOf()
    private val filteredItems: MutableList<Product> = mutableListOf()
    private val oldFilteredItems: MutableList<Product> = mutableListOf()
    var swipeBackground = ColorDrawable(Color.parseColor("#FF0000"))

    override fun onCreate(savedInstanceState: Bundle?) {
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!
        filteredItems.clear()
        originalItems.clear()
        oldFilteredItems.clear()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_item_list)

        artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan

        artisanItemList_recyclerView.layoutManager = LinearLayoutManager(this)

        //load an empty list as placeholder before GET request completes

        //TODO remove testing
        //val testItem = Product(1.0, "id", "des", "aid", "url", "Jewelry", "Earrings", "Hoop Earrings", "item name", "shipping", 1, 2)
        val emptyProductList : MutableList<Product> = mutableListOf()
//        if (originalItems.isEmpty()) {
//            artisanItemList_recyclerView.adapter = ListItemsAdapter(this, emptyProductList)
//        }
//        else {
//            artisanItemList_recyclerView.adapter = ListItemsAdapter(this, originalItems)
//        }
        artisanItemList_recyclerView.adapter = ListItemsAdapter(this, originalItems)
        artisanItemList_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        artisanItemList_addItemButton.setOnClickListener{
            addItem(artisan)
        }

        listItems_Search
                .textChanges()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe {
                    search(it.toString().toLowerCase())
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                val diffResult = DiffUtil.calculateDiff(PostsDiffUtilCallback(oldFilteredItems, filteredItems))
                                oldFilteredItems.clear()
                                oldFilteredItems.addAll(filteredItems)
                                diffResult.dispatchUpdatesTo((artisanItemList_recyclerView.adapter as ListItemsAdapter))
                            }
                }




        //item swipe to delete functionality
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                val builder = AlertDialog.Builder(this@ArtisanItemList)
                builder.setTitle("Confirm Item Deletion")
                builder.setMessage("Are you sure you want to delete this item? This action cannot be undone.")

                builder.setPositiveButton("Permanently Delete"){dialog, which ->
                    (artisanItemList_recyclerView.adapter as ListItemsAdapter).removeItem(viewHolder)
                    dialog.cancel()
                }

                builder.setNeutralButton("Cancel") { dialog, which ->
                    (artisanItemList_recyclerView.adapter as ListItemsAdapter).notifyItemChanged(viewHolder.adapterPosition)
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
        itemTouchHelper.attachToRecyclerView(artisanItemList_recyclerView)

    }

    override fun onStart() {
        filteredItems.clear()
        originalItems.clear()
        oldFilteredItems.clear()

        super.onStart()

        job = Job()

        launch {
            val dbProducts: List<Product> = getProductsFromDb()
            originalItems.addAll(dbProducts)
            oldFilteredItems.addAll(dbProducts)
            filteredItems.addAll(dbProducts)
            runOnUiThread {
                artisanItemList_recyclerView.adapter = ListItemsAdapter(applicationContext, oldFilteredItems, artisan)
            }
        }
        //fetchJSON()
    }

    private suspend fun getProductsFromDb() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(application).productDao().getAllByArtisanIdWithoutSyncState(artisan.artisanId, Synchronizer.SYNC_DELETE)
    }

    private fun search(query: String): Completable = Completable.create {
        val wanted = originalItems.filter {
            it.itemName.contains(query, true) || it.category.contains(query, true) || it.subCategory.contains(query, true) || it.specificCategory.contains(query, true)
        }.toList()

        if (listItems_Search.text.toString() == "") { // empty search bar
            filteredItems.clear()
            filteredItems.addAll(originalItems)
        } else {
            filteredItems.clear()
            filteredItems.addAll(wanted)
        }
        Log.d("ListItems", "editText: " + listItems_Search.text.toString())
        Log.d("ListItems", "originalOrders: " + originalItems.toString())
        Log.d("ListItems", "filteredOrders: " + filteredItems.toString())
        it.onComplete()
    }

    private fun addItem(artisan: Artisan) {
        //go to list all artisan screen
        val intent = Intent(this, AddItemCategory::class.java)
        intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
        finish()
    }

    //TODO GET request to query for all items associated to selected artisan
    //TODO need search bar

    private fun fetchJSON() {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder().add("artisanId",artisan.artisanId).build()


        val request = Request.Builder()
                .url(listAllItemsURL)
                .post(requestBody)
                .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("ArtisanItemList", body)
                val gson = GsonBuilder().create()
                //val artisans : List<com.amazonadonna.model.Artisan> =  gson.fromJson(body, mutableListOf<com.amazonadonna.model.Artisan>().javaClass)
                //System.out.print(artisans.get(0))
                val products : List<Product> = gson.fromJson(body,  object : TypeToken<List<Product>>() {}.type)
                originalItems.addAll(products)
                oldFilteredItems.addAll(products)
                filteredItems.addAll(products)

                runOnUiThread {
                    artisanItemList_recyclerView.adapter = ListItemsAdapter(applicationContext, oldFilteredItems, artisan)
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ArtisanItemList", "failed to do POST request to database")
            }
        })
    }

    inner class PostsDiffUtilCallback(private val oldList: List<Product>, private val newList: List<Product>) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].itemId == newList[newItemPosition].itemId

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true // for the sake of simplicity we return true here but it can be changed to reflect a fine-grained control over which part of our views are updated

    }
}
