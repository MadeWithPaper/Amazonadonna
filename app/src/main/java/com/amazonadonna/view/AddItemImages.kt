package com.amazonadonna.view

import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.util.ArrayMap
import android.util.Log
import android.util.SparseArray
import android.widget.ImageView
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import com.amazonadonna.view.R
import kotlinx.android.synthetic.main.activity_add_item_images.*
import java.io.File
import java.util.*


class AddItemImages : AppCompatActivity() {

    private var photoFile: File? = null
    private val CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE = 1046
    private var imageNum : Int = 0
    private var photoFilesArr : HashMap<Int, File?> = HashMap(6)

    private val imageViewMap = SparseArray<ImageView>()
    //TODO add editMode functionality
    //TODO limit pics to 300kb size

    var editMode : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_images)

        imageViewMap.put(0, addItemImage0)
        imageViewMap.put(1, addItemImage1)
        imageViewMap.put(2, addItemImage2)
        imageViewMap.put(3, addItemImage3)
        imageViewMap.put(4, addItemImage4)
        imageViewMap.put(5, addItemImage5)

        val IMAGE_UPLOADING_PERMISSION = 3
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_UPLOADING_PERMISSION)

        val product = intent.extras?.getSerializable("product") as Product
        val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan
        editMode = intent.extras?.get("editMode") as Boolean

        addItemImage_continueButton.setOnClickListener {
            addItemImageContinue(product, artisan)
        }

        addItemImage0.setOnClickListener {
            //add main image
            imageNum = 0
            selectImageInAlbum()
        }

        addItemImage1.setOnClickListener {
            //add main image
            imageNum = 1
            selectImageInAlbum()
        }

        addItemImage2.setOnClickListener {
            //add main image
            imageNum = 2
            selectImageInAlbum()
        }

        addItemImage3.setOnClickListener {
            //add main image
            imageNum = 3
            selectImageInAlbum()
        }

        addItemImage4.setOnClickListener {
            //add main image
            imageNum = 4
            selectImageInAlbum()
        }

        addItemImage5.setOnClickListener {
            //add main image
            imageNum = 5
            selectImageInAlbum()
        }

        if (editMode) {
            var isp = ImageStorageProvider(applicationContext)
            isp.loadImageIntoUI(product.pictureURLs[0], addItemImage0, ImageStorageProvider.ITEM_IMAGE_PREFIX, applicationContext)

            if (product.pictureURLs[1] != "Not set" && product.pictureURLs[1] != "undefined") {
                isp.loadImageIntoUI(product.pictureURLs[1], addItemImage1, ImageStorageProvider.ITEM_IMAGE_PREFIX, applicationContext)

            }
            if (product.pictureURLs[2] != "Not set" && product.pictureURLs[2] != "undefined") {
                isp.loadImageIntoUI(product.pictureURLs[2], addItemImage2, ImageStorageProvider.ITEM_IMAGE_PREFIX, applicationContext)

            }
            if (product.pictureURLs[3] != "Not set" && product.pictureURLs[3] != "undefined") {
                isp.loadImageIntoUI(product.pictureURLs[3], addItemImage3, ImageStorageProvider.ITEM_IMAGE_PREFIX, applicationContext)

            }
            if (product.pictureURLs[4] != "Not set" && product.pictureURLs[4] != "undefined") {
                isp.loadImageIntoUI(product.pictureURLs[4], addItemImage4, ImageStorageProvider.ITEM_IMAGE_PREFIX, applicationContext)

            }
            if (product.pictureURLs[5] != "Not set" && product.pictureURLs[5] != "undefined") {
                isp.loadImageIntoUI(product.pictureURLs[5], addItemImage5, ImageStorageProvider.ITEM_IMAGE_PREFIX, applicationContext)

            }
        }

    }

    private fun addItemImageContinue(product: Product, artisan: Artisan) {
        val intent = Intent(this, AddItemReview::class.java)

        intent.putExtra("product", product)
        intent.putExtra("selectedArtisan", artisan)
        intent.putExtra("photoFiles", photoFilesArr)
//        val bitmap = (addItemImage0.drawable as BitmapDrawable).bitmap
//        val pic = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
//
//        intent.putExtra("image0", pic)
        intent.putExtra("editMode", editMode)
        Log.i("AddItemImage", "product updated 3/4: " + product)
        startActivity(intent)
        finish()
    }

    private fun selectImageInAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun setImageView() {
        val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
        photoFilesArr[imageNum] = photoFile!!
        // RESIZE BITMAP, see section below
        // Load the taken image into a preview
        imageViewMap.get(imageNum).setImageBitmap(takenImage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            //TODO discuss if we want to have pic taken in list item
//            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE ->
//                if (resultCode == Activity.RESULT_OK) {
//                    try {
//                        Log.d("AFTERPHOTO", "IT WORK 34")
//                        Log.d("AFTERPHOTO", "Exists?: " + photoFile!!.exists())
//                        setImageView()
//                    }
//                    catch(e: Error) {
//                        Log.d("AFTERPHOTO", "AINT WORK 34")
//                    }
//                }
            CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        createImageFile(data)
                        Log.d("AddItemImage", "File:  Exists?: " + photoFile!!.exists())
                        setImageView()
                    }
                    else {
                        Log.d("AddItemImage", "Data was null")
                    }
                }
        }
    }

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

        photoFile = File(imagePath)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            3 -> {
                Log.i("AddItemImage", "onrequestpermissionresults 3")
                }
            }
        }

    //TODO save images to cache or sqlite to be fetched in the next screen.
    //TODO clear image scources after saving the pics

}
