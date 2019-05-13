package com.amazonadonna.artisanOnlyViews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import com.amazonadonna.model.App
import com.amazonadonna.model.Artisan
import com.amazonadonna.view.ArtisanItemList
import com.amazonadonna.view.EditArtisan
import com.amazonadonna.view.PayoutHistoryCGA
import com.amazonadonna.view.R
import kotlinx.android.synthetic.main.activity_artisan_profile.*

class ArtisanProfile : AppCompatActivity() {

    //private lateinit var artisan : Artisan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile)

        //artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanProfileBio.movementMethod = ScrollingMovementMethod()

        artisanProfileName.text = App.currentArtisan.artisanName
        artisanProfileBalance.text = App.currentArtisan.balance.toString()
        artisanProfileContact.text = App.currentArtisan.phoneNumber
        artisanProfileLoc.text = "${App.currentArtisan.city},${App.currentArtisan.country}"
        artisanProfileBio.text = App.currentArtisan.bio

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
        val intent = Intent(this, PayoutHistoryCGA::class.java)
        //TODO wait for db implementation
        intent.putExtra("artisanID", artisanID)
        Log.d("ArtisanProfile.kt", "payout history screen with cgaID: ${App.currentArtisan.cgaId}")
        startActivity(intent)
    }

    private fun itemListForArtisan() {
        val intent = Intent(this, ArtisanItemList::class.java)
        //intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
    }

    private fun editArtisan() {
        val intent = Intent(this, EditArtisan::class.java)
        //intent.putExtra("artisan", artisan)
        startActivity(intent)
        finish()
    }
}
