package com.amazonadonna.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetails : AppCompatActivity() {

    private var fromOrderScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        val product = intent.extras?.getSerializable("product") as Product
        if (intent.hasExtra("fromOrderScreen")){
            fromOrderScreen = intent.extras!!.getBoolean("fromOrderScreen")
        }
        setSupportActionBar(itemDetail_toolBar)

        supportActionBar!!.title = product.itemName
        var categoryString = ""
        if (product.specificCategory == "-- Not Applicable --") {
            categoryString = product.category + " > " + product.subCategory
        } else {
            categoryString = product.category + " > " + product.subCategory + " > " + product.specificCategory
        }

        val priceString = "$ " + product.price.toString()
        val productionTimeString = this.resources.getString(R.string.product_detail_usually_ships_in) + product.productionTime + this.resources.getString(R.string.utility_days)
        val productQuantityString = product.itemQuantity.toString() + " " + this.resources.getString(R.string.number_in_stock)
        itemDetail_categories.text = categoryString
        itemDetail_ProductNameTF.text = product.itemName
        itemDetail_itemPrice.text = priceString
        itemDetail_itemDescription.text = product.description
        itemDetail_itemDescription.setMovementMethod(ScrollingMovementMethod())
        itemDetail_shippingOption.text = product.shippingOption
        itemDetail_ItemQuantity.text = productQuantityString
        itemDetail_itemTime.text = productionTimeString

        var isp = ImageStorageProvider(applicationContext)
        val inflater = LayoutInflater.from(this)

        for (pic in product.pictureURLs) {
            val view = inflater.inflate(R.layout.gallery_item, gallery, false)
            val imageView = view.findViewById<ImageView>(R.id.imageView_ProductDetails)

            if (pic != "Not set" && pic != "undefined") {
                Log.d("ProductDetails", "adding url: "+pic)
                isp.loadImageIntoUI(pic, imageView, ImageStorageProvider.ITEM_IMAGE_PREFIX, applicationContext)
                gallery.addView(view)
            }
        }

        itemDetail_edit.setOnClickListener {
            editItem(product)
        }

        setSupportActionBar(itemDetail_toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun editItem(product: Product) {
        val intent = Intent(this, AddItemCategory::class.java)
        intent.putExtra("product", product)
        intent.putExtra("fromOrderScreen", fromOrderScreen)
        startActivity(intent)
        finish()
    }
}
