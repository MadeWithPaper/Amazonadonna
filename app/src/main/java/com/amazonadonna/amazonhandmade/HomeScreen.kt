package com.amazonadonna.amazonhandmade

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_home_screen.*
import okhttp3.*
import java.io.IOException
import Artisan
import com.google.gson.GsonBuilder

class HomeScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        //List All Artisan button
        listAllArtisan.setOnClickListener{
            queryAllArtisan()
        }

        addArtisan.setOnClickListener{
            addSingleArtisan()
        }
    }


    private fun queryAllArtisan() {
        //TODO add funtion to do GET reqest to back end
//
//        //starts new screen
        val intent = Intent(this, ListAllArtisans::class.java)
        startActivity(intent)

    }

    private fun addSingleArtisan() {
        //starts new screen
        val intent = Intent(this, AddArtisan::class.java)
        // To pass any data to next activity
        //OPTIONAL: intent.putExtra("keyIdentifier", value)
        // start your next activity
        startActivity(intent)
//
//        val url = "https://api.letsbuildthatapp.com/youtube/home_feed"
//        val requestBody = FormBody.Builder().add("artisanId","parse_artisanId")
//                .add("cgoId","parse_cgoID")
//                .add("bio", "parse_bio")
//                .add("city","parse_city")
//                .add("country", "parse_country")
//                .add("name", "parse_name")
//                .add("lat", "parse_lat")
//                .add("lon", "parse_lon")
//                .build()
//        val client = OkHttpClient()
//
//        val request = Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build()
//
//        client.newCall(request).enqueue(object: Callback {
//            override fun onResponse(call: Call?, response: Response?) {
//                val body = response?.body()?.string()
//                println(body) //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun onFailure(call: Call?, e: IOException?) {
//                println("Failed to execute request") //To change body of created functions use File | Settings | File Templates.
//            }
//        })




    }
}
