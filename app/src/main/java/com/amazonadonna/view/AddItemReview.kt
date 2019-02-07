package com.amazonadonna.view

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_add_item_review.*
import okhttp3.*
import java.io.IOException


class AddItemReview : AppCompatActivity() {

    private val addItemURL = "https://7bd92aed.ngrok.io/item/add"
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
            reviewDone(artisan, product)
        }
    }

    //TODO user horizontal scroll bar to make a nicer item pic gallery
    private fun reviewDone (artisan: Artisan, product: Product) {
        submitToDB(product, artisan)
    }

    private fun submitDismiss(artisan: Artisan) {
        val intent = Intent(this, ArtisanProfile::class.java)
        Log.i("AddItemReview", "review done adding item to db")
        intent.putExtra("artisan", artisan)
        startActivity(intent)
    }

    private fun submitToDB(product: Product, artisan: Artisan) {
        //TODO add process bar to show submitting process
        val requestBody = FormBody.Builder().add("itemId", product.itemId)
                .add("itemName", product.itemName)
                .add("price", product.price.toString())
                .add("description", product.description)
                .add("artisanId", product.artisanId)
                .add("category", product.category)
                .add("subCategory", product.subCategory)
                .add("specificCategory", product.specificCategory)
                .add("shippingOption", product.ShippingOption)
                .add("itemQuantity", product.itemQuantity.toString())
                .add("productionTime", product.productionTime.toString())
                .build()

        Log.d("AddItemReview", requestBody.toString())
        val client = OkHttpClient()

        val request = Request.Builder()
                .url(addItemURL)
                .post(requestBody)
                .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("AddItemReview", body)

//                Thread().run {
//                    submitPictureToDB(product)
//                }

               // showResponseDialog(artisan, true)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddItemReview", "failed to do POST request to database")
               // showResponseDialog(artisan, false)
            }
        })
    }

    private fun showResponseDialog(artisan: Artisan, status: Boolean){
        val builder = AlertDialog.Builder(this@AddItemReview)
        if (status) {
            builder.setTitle("Item Listing ...")
            builder.setMessage("Your item have been successfully submitted!")
            builder.setOnDismissListener {
                submitDismiss(artisan)
            }
        } else
        {
            builder.setTitle("Item Listing ...")
            builder.setMessage("Your item was NOT submitted!")
        }

        val dialog : AlertDialog = builder.create()
        dialog.show()
    }
    //TODO change product pic to an array of url as it can have more than one pic or have multiple fields for the images
    fun submitPictureToDB(product: Product) {

//        Log.d("hitFunction", "we here")
//        val sourceFile = photoFile!!
//        Log.d("drake", "File...::::" + sourceFile + " : " + sourceFile!!.exists())
//
//        val MEDIA_TYPE = MediaType.parse("image/png")
//
//        val requestBody = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("artisanId", artisan.artisanId)
//                .addFormDataPart("image", "profile.png", RequestBody.create(MEDIA_TYPE, sourceFile))
//                .build()
//
//        val request = Request.Builder()
//                .url(artisanPicURL)
//                .post(requestBody)
//                .build()
//
//        val client = OkHttpClient()
//        client.newCall(request).enqueue(object: Callback {
//            override fun onResponse(call: Call?, response: Response?) {
//                val body = response?.body()?.string()
//                Log.i("AddItemImage", body)
//            }
//
//            override fun onFailure(call: Call?, e: IOException?) {
//                Log.e("ERROR", "failed to do POST request to database")
//            }
//        })
    }
}
