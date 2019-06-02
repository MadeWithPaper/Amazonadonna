package com.amazonadonna.view

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_add_item_review.*
import okhttp3.*
import java.io.IOException
import android.annotation.TargetApi
import android.content.ContentUris
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.ImageView
import com.amazonadonna.artisanOnlyViews.ArtisanProfile
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.App
import com.amazonadonna.sync.ProductSync
import java.io.File
import java.util.*


class AddItemReview : AppCompatActivity() {

    //private var photoFile: File? = null
    private val addItemURL = App.BACKEND_BASE_URL + "/item/add"
    private val addItemImageURL = App.BACKEND_BASE_URL + "/item/updateImage"
    private val editItemURL = App.BACKEND_BASE_URL + "/item/editItem"
    var editMode : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_review)

//        val bitmap = this.intent.getParcelableExtra<Parcelable>("image0") as Bitmap
//        addItemReview_Image.setImageBitmap(bitmap)

       // val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan
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

        /*
        if (editMode && (photoFilesArr.size == 0 || photoFilesArr[0] == null)) {
            var isp = ImageStorageProvider(applicationContext)
            isp.loadImageIntoUI(product.pic0URL, addItemReview_Image, ImageStorageProvider.ITEM_IMAGE_PREFIX, applicationContext)
        }
        else {
            val bitmap = BitmapFactory.decodeFile(photoFilesArr[0]!!.path)
            addItemReview_Image.setImageBitmap(bitmap)
        }*/

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

    //TODO user horizontal scroll bar to make a nicer item pic gallery
    private fun reviewDone (product: Product, photosMap: HashMap<Int, File?>) {
        //submitToDB(product, artisan, photos)
        var photos = ArrayList<File?>(6)

        for (i in 0..5) {
            photos.add(i, photosMap[i])
        }

        if (editMode) {
            ProductSync.updateProduct(applicationContext, product, App.currentArtisan, photos)
            //submitToDB(product, artisan, photos)
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

    //TODO change product pic to an array of url as it can have more than one pic or have multiple fields for the images

    @TargetApi(19)
    private fun createImageFile(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        if (DocumentsContract.isDocumentUri(this, uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = imagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = imagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri.scheme, ignoreCase = true)){
            imagePath = imagePath(uri, null)
        }
        else if ("file".equals(uri.scheme, ignoreCase = true)){
            imagePath = uri.path
        }

        //photoFile = File(imagePath)
    }

    private fun imagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }
}
