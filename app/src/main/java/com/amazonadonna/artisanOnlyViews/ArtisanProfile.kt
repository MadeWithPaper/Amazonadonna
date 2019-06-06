package com.amazonadonna.artisanOnlyViews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.App
import com.amazonadonna.view.ArtisanItemList
import com.amazonadonna.view.EditArtisan
import com.amazonadonna.view.PayoutHistory
import com.amazonadonna.view.R
import kotlinx.android.synthetic.main.activity_artisan_profile.*

class ArtisanProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile)

        artisanProfileBio.movementMethod = ScrollingMovementMethod()

        setSupportActionBar(artisanProfileToolbar)

        supportActionBar!!.title = App.currentArtisan.artisanName
        artisanProfileBalance.text = App.currentArtisan.balance.toString()
        artisanProfileContact.text = App.currentArtisan.phoneNumber
        artisanProfileLoc.text = "${App.currentArtisan.city},${App.currentArtisan.country}"
        artisanProfileBio.text = App.currentArtisan.bio

        var isp = ImageStorageProvider(applicationContext)
        isp.loadImageIntoUI(App.currentArtisan.picURL, this.artisanProfilePicture, ImageStorageProvider.ARTISAN_IMAGE_PREFIX, applicationContext)

        artisanPayoutHistory.setOnClickListener {
            payoutHistoryForArtisan(App.currentArtisan.artisanId)
        }

        artisanItemList.setOnClickListener {
            itemListForArtisan()
        }

        artisanProfileEditButton.setOnClickListener {
            editArtisan()
        }
    }

    private fun payoutHistoryForArtisan(artisanID: String){
        val intent = Intent(this, PayoutHistory::class.java)
        intent.putExtra("artisanID", artisanID)
        Log.d("ArtisanProfile.kt", "payout history screen with cgaID: ${App.currentArtisan.cgaId}")
        startActivity(intent)
    }

    private fun itemListForArtisan() {
        val intent = Intent(this, ArtisanItemList::class.java)
        startActivity(intent)
    }

    private fun editArtisan() {
        val intent = Intent(this, EditArtisan::class.java)
        startActivity(intent)
        finish()
    }
}
