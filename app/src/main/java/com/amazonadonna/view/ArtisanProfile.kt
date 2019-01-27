package com.amazonadonna.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_artisan_profile.*


class ArtisanProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile)

        val artisan = intent.extras?.getSerializable("artisan") as? Artisan

        artisanProfileName.text = artisan?.name
        artisanProfileBio.text = artisan?.bio

        artisanProfileItemListButton.setOnClickListener {

            artisanItemList()
        }
    }

    fun artisanItemList(){

        val intent = Intent(this, ArtisanItemList::class.java)
        startActivity(intent)
    }

    //TODO add new intent to orders
    //TODO profile pic
    //TODO rating system

}
