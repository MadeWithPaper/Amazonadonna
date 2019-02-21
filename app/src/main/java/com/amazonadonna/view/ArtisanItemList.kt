package com.amazonadonna.view

import android.content.ClipData
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import com.amazonadonna.view.R
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_artisan_item_list.*
import kotlinx.android.synthetic.main.list_all_artisans.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class ArtisanItemList : AppCompatActivity() {

    lateinit var artisan : Artisan
    val listAllItemsURL = "https://7bd92aed.ngrok.io/item/listAllForArtisan"
    val originalItems: MutableList<Product> = mutableListOf()
    val filteredItems: MutableList<Product> = mutableListOf()
    val oldFilteredItems: MutableList<Product> = mutableListOf()

    private fun search(query: String): Completable = Completable.create {
        val wanted = originalItems.filter {
            it.itemId.toLowerCase().contains(query) || it.itemName.toLowerCase().contains(query)
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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_item_list)

        artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan

        artisanItemList_recyclerView.layoutManager = LinearLayoutManager(this)

        //load an empty list as placeholder before GET request completes

        //TODO remove testing
        //val testItem = Product(1.0, "id", "des", "aid", "url", "Jewelry", "Earrings", "Hoop Earrings", "item name", "shipping", 1, 2)
        val emptyProductList : List<Product> = emptyList()
        artisanItemList_recyclerView.adapter = ListItemsAdapter(this, originalItems)

        artisanItemList_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
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

        artisanItemList_addItemButton.setOnClickListener{
            addItem(artisan)
        }
    }

    override fun onStart() {
        super.onStart()
        fetchJSON()
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
                    artisanItemList_recyclerView.adapter = ListItemsAdapter(applicationContext, products, artisan)
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

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].artisanId == newList[newItemPosition].artisanId

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true // for the sake of simplicity we return true here but it can be changed to reflect a fine-grained control over which part of our views are updated

    }
}
