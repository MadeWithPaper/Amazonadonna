package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_add_item_images.*

class AddItemImages : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_images)

        val product = intent.extras?.getSerializable("product") as Product
        val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan

        addItemImage_continueButton.setOnClickListener {
            addItemImageContinue(product, artisan)
        }

    }

    //TODO

    private fun addItemImageContinue(product: Product, artisan: Artisan) {
        val intent = Intent(this, AddItemReview::class.java)
        intent.putExtra("product", product)
        intent.putExtra("selectedArtisan", artisan)
        Log.i("AddItemImage", "product updated 3/4: " + product)
        startActivity(intent)
    }
}
