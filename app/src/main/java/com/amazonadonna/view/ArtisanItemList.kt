package com.amazonadonna.view

import android.app.LauncherActivity
import android.content.ClipData
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_artisan_item_list.*
import okhttp3.*
import java.io.IOException

class ArtisanItemList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_item_list)

        val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan

        artisanItemList_recyclerView.layoutManager = LinearLayoutManager(this)

        //load an empty list as placeholder before GET request completes
        //TODO remove testing
        val testItem = Product(1.0, "id", "des", "aid", "url", "cate", "sub", "spce", "name", "shipping", 1, 2)
        val emptyProductList : List<Product> = listOf(testItem)
        artisanItemList_recyclerView.adapter = ListItemsAdapter(this, emptyProductList)

        artisanItemList_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        artisanItemList_addItemButton.setOnClickListener{
            addItem(artisan)
        }
    }

    private fun addItem(artisan: Artisan) {
        //go to list all artisan screen
        val intent = Intent(this, AddItemCategory::class.java)
        intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
    }

    //TODO GET request to query for all items associated to selected artisan
    //TODO need search bar

    private fun fetchJSON() {
        val url = ""
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()

                val gson = GsonBuilder().create()
                val products : List<Product> = gson.fromJson(body,  object : TypeToken<List<Product>>() {}.type)

                runOnUiThread {
                    artisanItemList_recyclerView.adapter = ListItemsAdapter(applicationContext, products)
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ListArtisanItem", "Failed to execute GET request to " + url)
            }
        })
    }
}
