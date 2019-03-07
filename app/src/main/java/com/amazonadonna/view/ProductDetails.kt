package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetails : AppCompatActivity() {

    private lateinit var artisan : Artisan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan
        Log.d("Productdetails", artisan.artisanId)

        val product = intent.extras?.getSerializable("product") as Product

        itemDetail_ToolBarText.text = product.itemName

        val categoryString = product.category + " > " + product.subCategory + " > " + product.specificCategory
        val priceString = "$ " + product.price.toString()
        val productionTimeString = this.resources.getString(R.string.product_detail_usually_ships_in) + product.productionTime + this.resources.getString(R.string.utility_days)
        val productQuantityString =product.itemQuantity.toString() + this.resources.getString(R.string.number_in_stock)
        itemDetail_categories.text = categoryString
        itemDetail_ProductNameTF.text = product.itemName
        itemDetail_itemPrice.text = priceString
        itemDetail_itemDescription.text = product.description
        itemDetail_shippingOption.text = product.shippingOption
        itemDetail_ItemQuantity.text = productQuantityString
        itemDetail_itemTime.text = productionTimeString

        var isp = ImageStorageProvider(applicationContext)
        isp.loadImageIntoUI(product.pictureURLs[0], itemDetail_Image, ImageStorageProvider.ITEM_IMAGE_PREFIX, applicationContext)

        //TODO edit items
        itemDetail_edit.setOnClickListener {
            editItem(product, artisan)
        }
    }

    private fun editItem(product: Product, artisan: Artisan) {
        val intent = Intent(this, AddItemCategory::class.java)
        intent.putExtra("product", product)
        intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
    }
}
