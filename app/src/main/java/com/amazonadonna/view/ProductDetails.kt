package com.amazonadonna.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        val product = intent.extras?.getSerializable("product") as Product

        itemDetail_ToolBarText.text = product.itemName

        val categoryString = product.category + " > " + product.subCategory + " > " + product.specificCategory
        val priceString = "$ " + product.price.toString()
        val productionTimeString = "Usuall shipped within " + product.productionTime
        val productQuantityString =product.itemQuantity.toString() + " In Stock"
        itemDetail_categories.text = categoryString
        itemDetail_ProductNameTF.text = product.itemName
        itemDetail_itemPrice.text = priceString
        itemDetail_itemDescription.text = product.description
        itemDetail_shippingOption.text = product.ShippingOption
        itemDetail_ItemQuantity.text = productQuantityString
        itemDetail_itemTime.text = productionTimeString

    }
}
