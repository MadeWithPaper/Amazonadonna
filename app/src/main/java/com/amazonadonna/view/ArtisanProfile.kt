package com.amazonadonna.view

import android.util.Log
import android.content.Intent
import android.content.Intent.createChooser
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_artisan_profile.*
import android.text.method.ScrollingMovementMethod
import com.amazonadonna.database.ImageStorageProvider


class ArtisanProfile() : AppCompatActivity() {

    private var artisan : Artisan? = null
    private val TAG = "ArtisanProfile.kt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile)
        //ArtisanSync.sync(this)
        artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanProfileBio.movementMethod = ScrollingMovementMethod()

        populateSelectedArtisan(artisan as Artisan)

        artisanItemList.setOnClickListener {
            artisanItemList(artisan as Artisan)
        }

        artisanMessageButton.setOnClickListener {
            artisanMessage(artisan as Artisan)
        }
        artisanPayoutButton.setOnClickListener {
            artisanPayout(artisan as Artisan)
        }

        artisanProfileEditButton.setOnClickListener {
            editArtisan(artisan as Artisan)
        }

        artisanPayoutHistory.setOnClickListener {
            payoutHistory(artisan as Artisan)
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
        artisanProfileBalance.text = "$${artisan.balance}"
        artisanProfileLoc.text = "${artisan.city}, ${artisan.country}"
        artisanProfileContact.text = artisan.phoneNumber
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

    private fun payoutHistory(artisan: Artisan) {
        val intent = Intent(this, PayoutHistoryCGA::class.java)
        intent.putExtra("cgaID", artisan.cgaId)
        Log.d(TAG, "payout history screen with cgaID: ${artisan.cgaId}")
        startActivity(intent)
    }
}
