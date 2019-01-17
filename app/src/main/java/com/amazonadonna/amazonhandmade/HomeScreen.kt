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

        //actionBar.set
        //List All Artisan button
        listAllArtisan.setOnClickListener{
            queryAllArtisan()
        }

        addArtisan.setOnClickListener{
            addSingleArtisan()
        }
    }


    private fun queryAllArtisan() {
        //go to list all artisan screen
        val intent = Intent(this, ListAllArtisans::class.java)
        startActivity(intent)

    }

    private fun addSingleArtisan() {
        //go to add artisan screen
        val intent = Intent(this, AddArtisan::class.java)
        startActivity(intent)

    }
}
