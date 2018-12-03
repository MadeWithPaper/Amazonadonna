package com.amazonadonna.amazonhandmade

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_home_screen.*
import okhttp3.*
import java.io.IOException

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
       // val intent = Intent(this, ListAllArtisans::class.java)
        val url = "https://api.letsbuildthatapp.com/youtube/home_feed"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                println(body) //To change body of created functions use File | Settings | File Templates.
            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request") //To change body of created functions use File | Settings | File Templates.
            }
        })
//        // To pass any data to next activity
//        //OPTIONAL: intent.putExtra("keyIdentifier", value)
//        // start your next activity
//        startActivity(intent)
        Log.d("INFO", "Starting to fetch")
    }

    private fun addSingleArtisan() {
        //starts new screen
        val intent = Intent(this, AddArtisan::class.java)
        // To pass any data to next activity
        //OPTIONAL: intent.putExtra("keyIdentifier", value)
        // start your next activity
        startActivity(intent)
    }
}
