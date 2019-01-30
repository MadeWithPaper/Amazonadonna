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

        resizeProfilePicImageView()

        artisanProfileBio.setMovementMethod(ScrollingMovementMethod())

        populateSelectedArtisan()

        artisanProfileItemListButton.setOnClickListener {

            artisanItemList()
        }

        artisanProfileOrdersButton.setOnClickListener {
            listArtisanOrders()
        }
    }

    private fun populateSelectedArtisan() {
        val artisan = intent.extras?.getSerializable("artisan") as? Artisan

        if (artisan?.picURL != "Not set")
            Picasso.with(this).load(artisan!!.picURL).into(this.artisanProfilePicture)
        //DownLoadImageTask(view.imageView_artisanProfilePic).execute(artisan.picURL)
        else
            this.artisanProfilePicture.setImageResource(R.drawable.placeholder)

        artisanProfileName.text = artisan.name
        artisanProfileBio.text = artisan.bio

    }

    private fun resizeProfilePicImageView() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        var screenWidth = displayMetrics.widthPixels
        var screenHeight = displayMetrics.heightPixels

    }
    private fun artisanItemList(){

        val intent = Intent(this, ArtisanItemList::class.java)
        startActivity(intent)
    }

    private fun listArtisanOrders(){

        val intent = Intent(this, ListOrders::class.java)
        startActivity(intent)
    }

    //TODO add new intent to orders
    //TODO profile pic
    //TODO rating system

}
