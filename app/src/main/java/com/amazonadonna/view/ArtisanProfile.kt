package com.amazonadonna.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import com.amazonadonna.model.Artisan
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_artisan_profile.*
import android.text.method.ScrollingMovementMethod
import android.R.id
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager


class ArtisanProfile() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile)

        val artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanProfileBio.setMovementMethod(ScrollingMovementMethod())

        populateSelectedArtisan(artisan)

        artisanProfileItemListButton.setOnClickListener {
            artisanItemList(artisan)
        }

        artisanProfilePayoutButton.setOnClickListener {
            artisanPayout(artisan)
        }

        artisanProfile_edit.setOnClickListener {
            editArtisan(artisan)
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

    private fun artisanPayout(artisan: Artisan){
        val intent = Intent(this, ArtisanPayout::class.java)
        intent.putExtra("balance", 0.0)
        startActivity(intent)
    }

    private fun editArtisan(artisan: Artisan) {
        val intent = Intent(this, EditArtisan::class.java)
        intent.putExtra("artisan", artisan)
        startActivity(intent)
    }
    //TODO rating system
}
