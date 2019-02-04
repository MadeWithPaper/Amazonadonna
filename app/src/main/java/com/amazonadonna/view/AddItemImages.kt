package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_item_images.*

class AddItemImages : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_images)

        addItemImage_continueButton.setOnClickListener {
            addItemImageContinue()
        }
    }

    //TODO

    private fun addItemImageContinue() {
        val intent = Intent(this, AddItemReview::class.java)
        //intent.putExtra("product", product)
        startActivity(intent)
    }
}
