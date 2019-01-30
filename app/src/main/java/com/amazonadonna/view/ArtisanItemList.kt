package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_artisan_item_list.*

class ArtisanItemList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_item_list)

        artisanItemList_addItemButton.setOnClickListener{
            addItem()
        }
    }

    private fun addItem() {
        //go to list all artisan screen
        val intent = Intent(this, AddItemCategory::class.java)
        startActivity(intent)
    }

    //TODO GET request to query for all items associated to selected artisan
    //TODO need search bar

}
