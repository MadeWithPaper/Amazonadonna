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
        artisans.add(Artisan("jaz"))
        artisans.add(Artisan("quinn"))
        artisans.add(Artisan("jacky"))
        artisans.add(Artisan("cory"))
        artisans.add(Artisan("liam"))
        artisans.add(Artisan("Mitchell"))

//        Log.d("info","in List All Artisan on create")
//        val apiCallUrl = "http://ec2-18-232-77-58.compute-1.amazonaws.com:3000/artisans"
//        val result = URL(apiCallUrl).readText()


        recyclerView_listAllartisans.layoutManager = LinearLayoutManager(this)
        recyclerView_listAllartisans.adapter = ListArtisanAdapter(artisans)
    }

}