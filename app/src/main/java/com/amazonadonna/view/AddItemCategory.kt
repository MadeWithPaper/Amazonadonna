package com.amazonadonna.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.amazonadonna.model.Artisan

class AddItemCategory : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_category)
        val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan

    }

    //TODO on finish return to ArtisanItemList
    //TODO start the item listing process
    //TODO add progress bar

}
