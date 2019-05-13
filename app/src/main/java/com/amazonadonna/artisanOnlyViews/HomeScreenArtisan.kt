package com.amazonadonna.artisanOnlyViews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amazonadonna.model.App
import com.amazonadonna.model.Artisan
import com.amazonadonna.view.ArtisanItemList
import com.amazonadonna.view.ListOrders
import com.amazonadonna.view.R
import com.amazonadonna.view.Settings
import kotlinx.android.synthetic.main.activity_home_screen_artisan.*

class HomeScreenArtisan : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen_artisan)

        App.artisanMode = true
        //TODO replace test data with artisan logged in
        val testArtisan = App.testArtisan
        //TODO set global artisan
        App.currentArtisan = testArtisan

        val extras = intent.extras
        if (extras != null) {
            artisanNameTV.text  = extras.getString("artisanName")
        } else {
            //TODO fetch for artisan?
            artisanNameTV.text = testArtisan.artisanName
        }

        artisanProfile.setOnClickListener {
            openArtisanProfile(testArtisan)
        }

        artisanItemList_cga.setOnClickListener {
            itemListForArtisan(testArtisan)
        }

        artisanOrderList.setOnClickListener {
            orderListForArtisan(testArtisan)
        }

        setting.setOnClickListener {
            openSetting(testArtisan)
        }
    }

    private fun openArtisanProfile(artisan: Artisan){
        val intent = Intent(this, ArtisanProfile::class.java)
        intent.putExtra("artisan", artisan)
        startActivity(intent)
    }

    private fun itemListForArtisan(artisan: Artisan) {
        val intent = Intent(this, ArtisanItemList::class.java)
        intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
    }

    private fun openSetting(artisan: Artisan) {
        val intent = Intent(this, Settings::class.java)
        intent.putExtra("cgaID", artisan.cgaId)
        intent.putExtra("artisanName", artisan.artisanName)
        startActivity(intent)
    }

    private fun orderListForArtisan(artisan: Artisan) {
        val intent = Intent(this, ListOrders::class.java)
        intent.putExtra("cgaId", artisan.cgaId)
        intent.putExtra("artisanId", artisan.artisanId)
        startActivity(intent)
    }
}
