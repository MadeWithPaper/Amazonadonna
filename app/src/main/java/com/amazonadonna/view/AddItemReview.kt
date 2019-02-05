package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_add_item_review.*

class AddItemReview : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_review)
        val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan

        val product = intent.extras?.getSerializable("product") as Product
        val categoryString = product.category + " > " + product.subCategory + " > " + product.specificCategory
        val priceString = "$ " + product.price.toString()
        val productionTimeString = "Usuall shipped within " + product.productionTime
        val productQuantityString =product.itemQuantity.toString() + " In Stock"
        addItemReview_categories.text = categoryString
        addIemReview_ProductNameTF.text = product.itemName
        addItemReview_itemPrice.text = priceString
        addItemReview_itemDescription.text = product.description
        addItemReview_shippingOption.text = product.ShippingOption
        addItemReview_ItemQuantity.text = productQuantityString
        addItemReview_itemTime.text = productionTimeString

        addItemReview_continueButton.setOnClickListener {
            reviewDone(artisan)
        }
    }

    private fun reviewDone (artisan: Artisan) {
        val intent = Intent(this, ArtisanProfile::class.java)
        Log.i("AddItemReview", "review done adding item to db")
        intent.putExtra("artisan", artisan)
        startActivity(intent)
    }

    private fun submitToDB() {

    }
}
