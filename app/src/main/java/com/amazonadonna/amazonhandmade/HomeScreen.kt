package com.amazonadonna.amazonhandmade

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_home_screen.*

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

        //starts new screen
        val intent = Intent(this, ListAllArtisans::class.java)
        // To pass any data to next activity
        //OPTIONAL: intent.putExtra("keyIdentifier", value)
        // start your next activity
        startActivity(intent)
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
