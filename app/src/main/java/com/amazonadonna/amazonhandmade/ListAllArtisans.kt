package com.amazonadonna.amazonhandmade

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.list_all_artisans.*
import java.net.URL
import Artisan
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson






class ListAllArtisans : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_all_artisans)
        fetchJSON()
        //TODO remove test data
//        artisans.add(Artisan("jaz", "testID", "Mexico City", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))
//        artisans.add(Artisan("Jacky", "testID", "SLO", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))
//        artisans.add(Artisan("Mitchell", "testID", "Azure City", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))
//        artisans.add(Artisan("Victor", "testID", "AWS", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))
//        artisans.add(Artisan("Liam", "testID", "Cal Poly", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))
//        artisans.add(Artisan("Quinn", "testID", "Mexico City", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))


        recyclerView_listAllartisans.layoutManager = LinearLayoutManager(this)
        //recyclerView_listAllartisans.adapter = ListArtisanAdapter(artisans)

    }

    fun fetchJSON() {
        val url = "https://29d4c6b3.ngrok.io/artisans"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()

                println(body) //To change body of created functions use File | Settings | File Templates.
                val gson = GsonBuilder().create()
                val artisans : List<Artisan> =  gson.fromJson(body, mutableListOf<Artisan>().javaClass)
                System.out.print(artisans.get(0))

                //val listType = object : TypeToken<ArrayList<Artisan>>() {}.type
                val yourClassList : List<Artisan> = gson.fromJson(body,  object : TypeToken<List<Artisan>>() {}.type)


                runOnUiThread {
                    recyclerView_listAllartisans.adapter = ListArtisanAdapter(yourClassList)
                }

                //println("i did it" + artisans)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request") //To change body of created functions use File | Settings | File Templates.
            }
        })
//        // To pass any data to next activity
//        //OPTIONAL: intent.putExtra("keyIdentifier", value)
//        // start your next activity
    }
}

//class ArtisanList(val artisanFeed : List<Artisan>)

//runOnUiThread {
//    recyclerView_listAllartisans.adapter = ListArtisanAdapter(artisans)
//}