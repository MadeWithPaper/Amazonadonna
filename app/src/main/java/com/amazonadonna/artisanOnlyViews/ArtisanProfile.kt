package com.amazonadonna.artisanOnlyViews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import com.amazonadonna.model.Artisan
import com.amazonadonna.view.ArtisanItemList
import com.amazonadonna.view.EditArtisan
import com.amazonadonna.view.PayoutHistoryCGA
import com.amazonadonna.view.R
import kotlinx.android.synthetic.main.activity_artisan_profile.*

class ArtisanProfile : AppCompatActivity() {

    private lateinit var artisan : Artisan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile)

        artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanProfileBio.movementMethod = ScrollingMovementMethod()

        artisanProfileName.text = artisan.artisanName
        artisanProfileBalance.text = artisan.balance.toString()
        artisanProfileContact.text = artisan.phoneNumber
        artisanProfileLoc.text = "${artisan.city},${artisan.country}"
        artisanProfileBio.text = artisan.bio

        artisanPayoutHistory.setOnClickListener {
            payoutHistoryForArtisan(artisan.artisanId)
        }

        artisanItemList.setOnClickListener {
            itemListForArtisan(artisan)
        }

        artisanProfileEditButton.setOnClickListener {
            editArtisan(artisan)
        }
    }

    private fun payoutHistoryForArtisan(artisanID: String){
        val intent = Intent(this, PayoutHistoryCGA::class.java)
        //TODO wait for db implementation
        intent.putExtra("artisanID", artisanID)
        Log.d("ArtisanProfile.kt", "payout history screen with cgaID: ${artisan.cgaId}")
        startActivity(intent)
    }

    private fun itemListForArtisan(artisan: Artisan) {
        val intent = Intent(this, ArtisanItemList::class.java)
        intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
    }

    private fun editArtisan(artisan: Artisan) {
        val intent = Intent(this, EditArtisan::class.java)
        intent.putExtra("artisan", artisan)
        startActivity(intent)
        finish()
    }
}
