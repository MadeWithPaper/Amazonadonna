package com.amazonadonna.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import com.amazonadonna.model.Artisan
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_artisan_profile.*
import kotlinx.android.synthetic.main.list_artisan_cell.view.*
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.util.DisplayMetrics
import android.view.Display
import android.text.method.ScrollingMovementMethod


class ArtisanProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile)

        val artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanProfileBio.setMovementMethod(ScrollingMovementMethod())

        populateSelectedArtisan(artisan)

        artisanProfileItemListButton.setOnClickListener {
            artisanItemList(artisan)
        }

        artisanProfileOrdersButton.setOnClickListener {
            listArtisanOrders(artisan)
        }
    }

    private fun populateSelectedArtisan(artisan : Artisan) {
        if (artisan.picURL != "Not set")
            Picasso.with(this).load(artisan.picURL).into(this.artisanProfilePicture)
        //DownLoadImageTask(view.imageView_artisanProfilePic).execute(artisan.picURL)
        else
            this.artisanProfilePicture.setImageResource(R.drawable.placeholder)

        artisanProfileName.text = artisan.name
        artisanProfileBio.text = artisan.bio

    }

    private fun artisanItemList(artisan : Artisan){
        val intent = Intent(this, ArtisanItemList::class.java)
        intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
    }

    private fun listArtisanOrders(artisan: Artisan){

        val intent = Intent(this, ListOrders::class.java)
        startActivity(intent)
    }

    //TODO add new intent to orders
    //TODO profile pic
    //TODO rating system

}
