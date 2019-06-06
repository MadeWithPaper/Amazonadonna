package com.amazonadonna.view

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_add_item_review.*
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.widget.ImageView
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.App
import com.amazonadonna.sync.ProductSync
import java.io.File
import java.util.*

class AddItemReview : AppCompatActivity() {

    var editMode : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_review)

        val product = intent.extras?.getSerializable("product") as Product
        var photoFilesArr = intent.extras?.getSerializable("photoFiles") as HashMap<Int, File?>
        editMode = intent.extras?.get("editMode") as Boolean


        var categoryString = ""
        if (product.specificCategory == "-- Not Applicable --") {
            categoryString = product.category + " > " + product.subCategory
        } else {
            categoryString = product.category + " > " + product.subCategory + " > " + product.specificCategory
        }
        val priceString = "$ " + product.price.toString()
        val productionTimeString = this.resources.getString(R.string.item_review_usually_ships) + product.productionTime
        val productQuantityString = product.itemQuantity.toString() + this.resources.getString(R.string.number_in_stock)
        addItemReview_categories.text = categoryString
        addIemReview_ProductNameTF.text = product.itemName
        addItemReview_itemPrice.text = priceString
        addItemReview_itemDescription.text = product.description
        addItemReview_shippingOption.text = product.shippingOption
        addItemReview_ItemQuantity.text = productQuantityString
        addItemReview_itemTime.text = productionTimeString

        var isp = ImageStorageProvider(applicationContext)
        val inflater = LayoutInflater.from(this)

        for (i in 0..5) {
            val pic = product.pictureURLs[i]
            val view = inflater.inflate(R.layout.gallery_item, gallery, false)
            val imageView = view.findViewById<ImageView>(R.id.imageView_ProductDetails)
                Log.d("ProductDetails", "adding url: "+pic)

            // If in edit mode with no new picture selected for i and existing one avilable, use existing pic
            if (editMode && photoFilesArr[i] == null && pic != "Not set" && pic != "undefined") {
                isp.loadImageIntoUI(pic, imageView, ImageStorageProvider.ITEM_IMAGE_PREFIX, applicationContext)
                gallery.addView(view)
            }
            // else if there is a photo defined at index i, load it
            else if (photoFilesArr[i] != null){
                val bitmap = BitmapFactory.decodeFile(photoFilesArr[i]!!.path)
                imageView.setImageBitmap(bitmap)
                gallery.addView(view)
            }
        }

        addItemReview_continueButton.setOnClickListener {
            reviewDone(product, photoFilesArr)
        }

        setSupportActionBar(addItemReview_toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        return true
    }

    private fun reviewDone (product: Product, photosMap: HashMap<Int, File?>) {
        var photos = ArrayList<File?>(6)

        for (i in 0..5) {
            photos.add(i, photosMap[i])
        }

        if (editMode) {
            ProductSync.updateProduct(applicationContext, product, App.currentArtisan, photos)
            runOnUiThread {
                showResponseDialog(true)
            }
        }
        else {
            product.generateTempID()
            ProductSync.addProduct(applicationContext, product, App.currentArtisan, photos)
            runOnUiThread {
                showResponseDialog(true)
            }
        }
    }

    private fun submitDismiss() {
        Log.i("AddItemReview", "review done adding item to db")
        if (App.artisanMode) {
            onBackPressed()
        } else {
            val intent = Intent(this, ArtisanProfileCGA::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showResponseDialog(status: Boolean) {
        val builder = AlertDialog.Builder(this@AddItemReview)
        if (status) {
            builder.setTitle(this.resources.getString(R.string.item_review_response_dialog_title))
            builder.setMessage(this.resources.getString(R.string.item_review_listing_success))
            builder.setOnDismissListener {
                submitDismiss()
            }
        } else
        {
            builder.setTitle(this.resources.getString(R.string.item_review_response_dialog_title))
            builder.setMessage(this.resources.getString(R.string.item_review_listing_failed))
        }

        val dialog : AlertDialog = builder.create()
        dialog.show()
    }
}
