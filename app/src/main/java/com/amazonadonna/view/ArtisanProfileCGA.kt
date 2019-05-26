package com.amazonadonna.view

import android.util.Log
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import kotlinx.android.synthetic.main.activity_artisan_profile_cga.*
import android.text.method.ScrollingMovementMethod
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.App


class ArtisanProfileCGA() : AppCompatActivity() {

    //private lateinit var artisan : Artisan
    private val TAG = "ArtisanProfileCGACGA.kt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile_cga)
        //ArtisanSync.sync(this)
       // artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanProfileBio_cga.movementMethod = ScrollingMovementMethod()
        //Log.d("ArtisanProfileCGA", "in onCreate payout ${artisan.balance}")

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

        setSupportActionBar(artisanProfileToolbar_cga)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        //App.currentArtisan = intent.extras?.getSerializable("artisan") as Artisan
        //Log.d("ArtisanProfileCGA", "in onResume payout ${artisan.balance}")
        populateSelectedArtisan()
    }

    private fun artisanMessage() {
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

    private fun populateSelectedArtisan() {
        //val artisan = App.currentArtisan
        /*if (artisan.picURL != "Not set")
            Picasso.with(this).load(artisan.picURL).into(this.artisanProfilePicture)
        //DownLoadImageTask(view.imageView_artisanProfilePic).execute(artisan.picURL)
        else
            this.artisanProfilePicture.setImageResource(R.drawable.placeholder)*/

        var isp = ImageStorageProvider(applicationContext)
        isp.loadImageIntoUI(App.currentArtisan.picURL, this.artisanProfilePicture_cga, ImageStorageProvider.ARTISAN_IMAGE_PREFIX, applicationContext)

        Log.d("ArtisanProfileCGA", "$App.currentArtisan")
        artisanProfileName_cga.text = App.currentArtisan.artisanName
        artisanProfileBio_cga.text = App.currentArtisan.bio
        artisanProfileBalance_cga.text = "$${App.currentArtisan.balance}"
        artisanProfileLoc_cga.text = "${App.currentArtisan.city}, ${App.currentArtisan.country}"
        artisanProfileContact_cga.text = App.currentArtisan.phoneNumber
    }

    private fun artisanItemList(){
        val intent = Intent(this, ArtisanItemList::class.java)
        //intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
        //finish()
    }

    private fun artisanPayout(){
        val intent = Intent(this, ArtisanPayout::class.java)
        //intent.putExtra("artisan", artisan)
        startActivity(intent)
        //finish()
    }

    private fun editArtisan() {
        val intent = Intent(this, EditArtisan::class.java)
        //intent.putExtra("artisan", artisan)
        startActivity(intent)
        //finish()
    }

    private fun payoutHistory() {
        val intent = Intent(this, PayoutHistory::class.java)
        //intent.putExtra("cgaID", artisan.cgaId)
        //Log.d(TAG, "payout history screen with cgaID: ${artisan.cgaId}")
        startActivity(intent)
    }
}
