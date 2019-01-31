package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_home_screen.*


class HomeScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        //actionBar.set
        //List All com.amazonadonna.model.Artisan button
        listAllArtisan.setOnClickListener{
            queryAllArtisan()
        }

        listOrders.setOnClickListener {
            queryAllOrder()
        }

    }


    private fun queryAllArtisan() {
        //go to list all artisan screen
        val intent = Intent(this, ListAllArtisans::class.java)
        startActivity(intent)

    }


    private fun queryAllOrder() {
        //go to list all artisan screen
        val intent = Intent(this, ListOrders::class.java)
        startActivity(intent)

    }


}
