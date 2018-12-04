package com.amazonadonna.amazonhandmade

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.list_all_artisans.*
import java.net.URL
import Artisan


class ListAllArtisans : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_all_artisans)
        val artisans = ArrayList<Artisan>()
        //TODO remove test data
        artisans.add(Artisan("jaz", "testID", "Mexico City", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))
        artisans.add(Artisan("Jacky", "testID", "SLO", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))
        artisans.add(Artisan("Mitchell", "testID", "Azure City", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))
        artisans.add(Artisan("Victor", "testID", "AWS", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))
        artisans.add(Artisan("Liam", "testID", "Cal Poly", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))
        artisans.add(Artisan("Quinn", "testID", "Mexico City", "Mexico", "hello i am an artisan", "test cgo id", 10.0, 10.0))


//        Log.d("info","in List All Artisan on create")
//        val apiCallUrl = "http://ec2-18-232-77-58.compute-1.amazonaws.com:3000/artisans"
//        val result = URL(apiCallUrl).readText()


        recyclerView_listAllartisans.layoutManager = LinearLayoutManager(this)
        recyclerView_listAllartisans.adapter = ListArtisanAdapter(artisans)
    }

}