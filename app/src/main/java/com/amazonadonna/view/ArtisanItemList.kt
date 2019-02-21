package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import com.amazonadonna.view.R
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_artisan_item_list.*
import okhttp3.*
import java.io.IOException

class ArtisanItemList : AppCompatActivity() {

    lateinit var artisan : Artisan
    val listAllItemsURL = "https://7bd92aed.ngrok.io/item/listAllForArtisan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_item_list)

        artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan

        artisanItemList_recyclerView.layoutManager = LinearLayoutManager(this)

        //load an empty list as placeholder before GET request completes
        //TODO remove testing
        //val testItem = Product(1.0, "id", "des", "aid", "url", "Jewelry", "Earrings", "Hoop Earrings", "item name", "shipping", 1, 2)
        val emptyProductList : List<Product> = emptyList()
        artisanItemList_recyclerView.adapter = ListItemsAdapter(this, emptyProductList, artisan)

        artisanItemList_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

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

                runOnUiThread {
                    artisanItemList_recyclerView.adapter = ListItemsAdapter(applicationContext, products, artisan)
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("ArtisanItemList", "failed to do POST request to database")
            }
        })
    }
}
