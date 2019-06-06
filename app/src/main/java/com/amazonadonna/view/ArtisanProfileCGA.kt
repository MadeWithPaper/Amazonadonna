package com.amazonadonna.view

import android.util.Log
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import kotlinx.android.synthetic.main.activity_artisan_profile_cga.*
import android.text.method.ScrollingMovementMethod
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.App

class ArtisanProfileCGA : AppCompatActivity() {

    //private val TAG = "ArtisanProfileCGACGA.kt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile_cga)
        artisanProfileBio_cga.movementMethod = ScrollingMovementMethod()
        //Log.d("ArtisanProfileCGA", "in onCreate payout ${artisan.balance}")

        setSupportActionBar(artisanProfileToolbar_cga)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        populateSelectedArtisan()

        artisanItemList_cga.setOnClickListener {
            artisanItemList()
        }

        artisanMessageButton_cga.setOnClickListener {
            artisanMessage()
        }
        artisanPayoutButton_cga.setOnClickListener {
            artisanPayout()
        }

        artisanProfileEditButton_cga.setOnClickListener {
            editArtisan()
        }

        artisanPayoutHistory_cga.setOnClickListener {
            payoutHistory()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        populateSelectedArtisan()
    }

    private fun artisanMessage() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra("address", App.currentArtisan.phoneNumber)
        val messageChooser = Intent.createChooser(intent, "Please Choose an Application to Send Messages...")
        startActivity(messageChooser)
    }

    private fun populateSelectedArtisan() {
        var isp = ImageStorageProvider(applicationContext)
        isp.loadImageIntoUI(App.currentArtisan.picURL, this.artisanProfilePicture_cga, ImageStorageProvider.ARTISAN_IMAGE_PREFIX, applicationContext)

        Log.d("ArtisanProfileCGA", "$App.currentArtisan")
        supportActionBar!!.title = App.currentArtisan.artisanName
        artisanProfileBio_cga.text = App.currentArtisan.bio
        artisanProfileBalance_cga.text = "$${App.currentArtisan.balance}"
        artisanProfileLoc_cga.text = "${App.currentArtisan.city}, ${App.currentArtisan.country}"
        artisanProfileContact_cga.text = App.currentArtisan.phoneNumber
    }

    private fun artisanItemList(){
        val intent = Intent(this, ArtisanItemList::class.java)
        startActivity(intent)
    }

    private fun artisanPayout(){
        val intent = Intent(this, ArtisanPayout::class.java)
        startActivity(intent)
    }

    private fun editArtisan() {
        val intent = Intent(this, EditArtisan::class.java)
        startActivity(intent)
    }

    private fun payoutHistory() {
        val intent = Intent(this, PayoutHistory::class.java)
        startActivity(intent)
    }
}
