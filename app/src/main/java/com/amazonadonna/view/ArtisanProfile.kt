package com.amazonadonna.view

import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import com.amazonadonna.model.Artisan
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_artisan_profile.*
import android.text.method.ScrollingMovementMethod
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.sync.ArtisanSync
import com.amazonadonna.view.R
import kotlinx.android.synthetic.main.list_artisan_cell.view.*


class ArtisanProfile() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile)
        //ArtisanSync.sync(this)
        val artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanProfileBio.setMovementMethod(ScrollingMovementMethod())

        populateSelectedArtisan(artisan)

        artisanProfileItemListButton.setOnClickListener {
            artisanItemList(artisan)
        }

        artisanProfileMessagesButton.setOnClickListener {
            artisanMessage(artisan)
        }
        artisanProfilePayoutButton.setOnClickListener {
            artisanPayout(artisan)
        }

        artisanProfile_edit.setOnClickListener {
            editArtisan(artisan)
        }
    }

    private fun artisanMessage(artisan: Artisan) {
        //TODO put in real artisan number 
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra("address", "1234567")

        startActivity(intent)
    }

    private fun populateSelectedArtisan(artisan : Artisan) {
        /*if (artisan.picURL != "Not set")
            Picasso.with(this).load(artisan.picURL).into(this.artisanProfilePicture)
        //DownLoadImageTask(view.imageView_artisanProfilePic).execute(artisan.picURL)
        else
            this.artisanProfilePicture.setImageResource(R.drawable.placeholder)*/

        var isp = ImageStorageProvider(applicationContext)
        isp.loadImageIntoUI(artisan.picURL, this.artisanProfilePicture, ImageStorageProvider.ARTISAN_IMAGE_PREFIX, applicationContext)

        artisanProfileName.text = artisan.artisanName
        artisanProfileBio.text = artisan.bio

    }

    private fun artisanItemList(artisan : Artisan){
        val intent = Intent(this, ArtisanItemList::class.java)
        intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
        finish()
    }

    private fun artisanPayout(artisan: Artisan){
        val intent = Intent(this, ArtisanPayout::class.java)
        intent.putExtra("artisan", artisan)
        startActivity(intent)
    }

    private fun editArtisan(artisan: Artisan) {
        val intent = Intent(this, EditArtisan::class.java)
        intent.putExtra("artisan", artisan)
        startActivity(intent)
        finish()
    }
    //TODO rating system
}
