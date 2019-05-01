package com.amazonadonna.view

import android.util.Log
import android.content.Intent
import android.content.Intent.createChooser
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_artisan_profile.*
import android.text.method.ScrollingMovementMethod
import com.amazonadonna.database.ImageStorageProvider


class ArtisanProfile() : AppCompatActivity() {

    var artisan : Artisan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile)
        //ArtisanSync.sync(this)
        artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanProfileBio.setMovementMethod(ScrollingMovementMethod())

        populateSelectedArtisan(artisan as Artisan)

        artisanProfileItemListButton.setOnClickListener {
            artisanItemList(artisan as Artisan)
        }

        artisanProfileMessagesButton.setOnClickListener {
            artisanMessage(artisan as Artisan)
        }
        artisanProfilePayoutButton.setOnClickListener {
            artisanPayout(artisan as Artisan)
        }

        artisanProfile_edit.setOnClickListener {
            editArtisan(artisan as Artisan)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("ArtisanProfile", "in onResume")
        populateSelectedArtisan(artisan as Artisan)
    }

    private fun artisanMessage(artisan: Artisan) {
        //TODO put in real artisan number
        val intent = Intent(this, MessageArtisan::class.java)
        //intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
        finish()
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.type = "text/plain"
//        intent.putExtra("address", artisan.phoneNumber)
//        val messagechooser = createChooser(intent, "Please Choose an Application to Send Messages...")
//        startActivity(messagechooser)
    }

    private fun populateSelectedArtisan(artisan : Artisan) {
        /*if (artisan.picURL != "Not set")
            Picasso.with(this).load(artisan.picURL).into(this.artisanProfilePicture)
        //DownLoadImageTask(view.imageView_artisanProfilePic).execute(artisan.picURL)
        else
            this.artisanProfilePicture.setImageResource(R.drawable.placeholder)*/

        var isp = ImageStorageProvider(applicationContext)
        isp.loadImageIntoUI(artisan.picURL, this.artisanProfilePicture, ImageStorageProvider.ARTISAN_IMAGE_PREFIX, applicationContext)

        Log.d("ArtisanProfile", artisan.bio)
        artisanProfileName.text = artisan.artisanName
        artisanProfileBio.text = artisan.bio
        artisanProfileBalance.text = "Balance: $${artisan.balance}"
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
        //finish()
    }

    private fun editArtisan(artisan: Artisan) {
        val intent = Intent(this, EditArtisan::class.java)
        intent.putExtra("artisan", artisan)
        startActivity(intent)
        //finish()
    }
    //TODO rating system
}
