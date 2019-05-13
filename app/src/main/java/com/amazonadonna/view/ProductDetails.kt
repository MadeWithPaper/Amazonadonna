package com.amazonadonna.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.App
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_product_details.*
import kotlinx.android.synthetic.main.gallery_item.*

class ProductDetails : AppCompatActivity() {

    //private lateinit var artisan : Artisan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

       // artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan
        Log.d("Productdetails", App.currentArtisan.artisanId)

        val product = intent.extras?.getSerializable("product") as Product

        itemDetail_ToolBarText.text = product.itemName

        val categoryString = product.category + " > " + product.subCategory + " > " + product.specificCategory
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

        //TODO edit items
        itemDetail_edit.setOnClickListener {
            editItem(product)
        }
    }

    private fun editItem(product: Product) {
        val intent = Intent(this, AddItemCategory::class.java)
        intent.putExtra("product", product)
       // intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
        finish()
    }
}
