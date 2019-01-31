package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_artisan_item_list.*

class ArtisanItemList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_item_list)

        val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan

        artisanItemList_addItemButton.setOnClickListener{
            addItem(artisan)
        }
    }

    private fun addItem(artisan: Artisan) {
        //go to list all artisan screen
        val intent = Intent(this, AddItemCategory::class.java)
        intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
    }

    //TODO GET request to query for all items associated to selected artisan
    //TODO need search bar

}
