package com.amazonadonna.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_artisan.*
import com.amazonadonna.model.Artisan
import android.annotation.TargetApi
import android.widget.ImageView

import android.text.TextUtils
import android.util.Log
import okhttp3.*
import android.content.Intent
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.app.Activity
import android.support.v4.app.ActivityCompat
import android.provider.DocumentsContract
import android.content.ContentUris
import android.net.Uri
import java.io.*
import android.graphics.BitmapFactory
import com.amazonadonna.sync.ArtisanSync
import com.amazonadonna.sync.Synchronizer
import android.graphics.Bitmap




class AddArtisan : AppCompatActivity() {
    private var cgaId : String = "0"
    private var photoFile: File? = null
    private val fileName: String = "output.png"
    private val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    private val CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE = 1046
    private val addArtisanURL = "https://99956e2a.ngrok.io/artisan/add"
    private val artisanPicURL = "https://99956e2a.ngrok.io/artisan/updateImage"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_artisan)

        cgaId = intent.extras.getString("cgaId")

        val IMAGE_UPLOADING_PERMISSION = 3
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_UPLOADING_PERMISSION)

        takePicture.setOnClickListener{
            takePhoto()
        }

        selectPicture.setOnClickListener{
            selectImageInAlbum()
        }
    }

    private fun selectImageInAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        photoFile = File(externalCacheDir, fileName)

        if(photoFile!!.exists()) {
            photoFile!!.delete()
        }
        photoFile!!.createNewFile()

        val fileProvider = FileProvider.getUriForFile(this@AddArtisan, "com.amazonadonna.amazonhandmade.fileprovider", photoFile!!)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun setImageView() {
        val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
        // RESIZE BITMAP, see section below
        // Load the taken image into a preview
        val ivPreview = findViewById(R.id.imageView_artisanProfilePic) as ImageView
        ivPreview.setImageBitmap(takenImage)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                        try {
                            Log.d("Add Artisan post photo", "Success")
                            Log.d("Add Artisan post photo", "Exists?: " + photoFile!!.exists())
                            setImageView()
                        } catch (e: Error) {
                            Log.d("Add Artisan post Photo", "it failed")
                        }
                    }
            CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val w = imageView_artisanProfilePic.width
                        val h = imageView_artisanProfilePic.height
                        val dataURI = data.data
                        Log.d("HEIGHT", h.toString())
                        Log.d("WIDTH", w.toString())
                        Log.d("dataURI", dataURI.toString())
                        createImageFile(data)


                        try {
                            Log.d("Add Artisan post photo", "Success")
                           Log.d("Add Artisan post photo", "Exists?: " + photoFile!!.exists())
                            val bm = loadScaledBitmap(dataURI, w, h)
                            val ivPreview = findViewById(R.id.imageView_artisanProfilePic) as ImageView
                            ivPreview.setImageBitmap(bm)
                            //setImageView()
                        }
                        catch(e: Error) {
                            Log.d("Add Artisan post Photo", "it failed")
                        }
//                        createImageFile(data)
//                        Log.d("Add Artisan postGallery", "File:  Exists?: " + photoFile!!.exists())
//                        setImageView()
                    }
                    else {
                        Log.d("Add Artisan postGallery", "Data was null")
                    }
                }
            }
    }

    @Throws(FileNotFoundException::class)
    private fun loadScaledBitmap(src: Uri, req_w: Int, req_h: Int): Bitmap? {

        var bm: Bitmap? = null

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(baseContext.contentResolver.openInputStream(src), null, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, req_w, req_h)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        bm = BitmapFactory.decodeStream(
                baseContext.contentResolver.openInputStream(src), null, options)

        return bm
    }

    public fun calculateInSampleSize(options : BitmapFactory.Options,
                                      reqWidth : Int, reqHeight : Int): Int {
        // Raw height and width of image
        val height = options.outHeight;
        val width = options.outWidth;
        var inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            val heightRatio = Math.round((height.toFloat()) / Math.round(reqHeight.toFloat()))
            val widthRatio = Math.round(width.toFloat() / reqHeight.toFloat())

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            if (heightRatio < widthRatio)
                inSampleSize = heightRatio
            else
                inSampleSize = widthRatio
           // inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio
        }

        return inSampleSize
    }



override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            3 -> {
                button_addArtisan.setOnClickListener{
                    //Toast.makeText(this@AddArtisan, "add button clicked.", Toast.LENGTH_SHORT).show()
                    makeNewArtisan()
                }
            }
        }
    }

    //TODO clean up
    private fun makeNewArtisan() {
        //validate fields
        if (!validateFields()) {
            return
        }
        val name = editText_Name.text.toString()
        val bio = editText_bio.text.toString()
        val number = editText_ContactNumber.text.toString()

        val newArtisan = Artisan(name, "", number, "","", bio, cgaId,0.0,0.0, "Not set", Synchronizer.SYNC_NEW, 0.0)
        newArtisan.generateTempID()
        //parse location info
        parseLoc(newArtisan)
        Log.i("AddArtisan", "created new Artisan $newArtisan")

        //pop screen and add
        //submitToDB(newArtisan)
        ArtisanSync.addArtisan(applicationContext, newArtisan, photoFile)

        //clear all fields
        clearFields()
        super.onBackPressed()

    }

    private fun parseLoc (artisan: Artisan) {
        val rawLoc = editText_loc.text.toString()

        val ind = rawLoc.indexOf(',')

        artisan.city = rawLoc.substring(0, ind)
        artisan.country = rawLoc.substring(ind+1)
    }

    private fun clearFields() {
        editText_Name.text.clear()
        editText_ContactNumber.text.clear()
        editText_bio.text.clear()
        editText_loc.text.clear()

        Log.i("AddArtisan", "Clearing fields")
    }

    //Validate all fields entered
    //TODO add more checks
    private fun validateFields() : Boolean {
        if (TextUtils.isEmpty(editText_Name.text.toString())){
            editText_Name.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (TextUtils.isEmpty(editText_loc.text.toString())) {
            editText_loc.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if ((!editText_loc.text.toString().contains(","))) {
            editText_loc.error = this.resources.getString(R.string.loc_missing_comma)
            return false
        }

        if (TextUtils.isEmpty(editText_ContactNumber.text.toString())){
            editText_ContactNumber.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (TextUtils.isEmpty(editText_bio.text.toString())){
            editText_bio.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        return true
    }

    // source file does not exist
    fun submitPictureToDB(artisan: Artisan) {
        val sourceFile = photoFile!!
        Log.d("AddArtisan", "submitPictureToDB file" + sourceFile + " : " + sourceFile.exists())

        val MEDIA_TYPE = MediaType.parse("image/png")

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("artisanId", artisan.artisanId)
                .addFormDataPart("image", "profile.png", RequestBody.create(MEDIA_TYPE, sourceFile))
                .build()

        val request = Request.Builder()
                .url(artisanPicURL)
                .post(requestBody)
                .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("AddArtisan", body)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddArtisan", "failed to do POST request to database$artisanPicURL")
            }
        })
    }

    fun submitToDB(artisan: Artisan) {

        //TODO clean up when id generation is moved to backend
//        if (!validateFields()) {
//            return
//        }
//        val name = editText_Name.text.toString()
//        val bio = editText_bio.text.toString()
//        val number = editText_ContactNumber.text.toString()

        val requestBody = FormBody.Builder().add("artisanId",artisan.artisanId)
                .add("cgoId", artisan.cgoId)
                .add("bio", artisan.bio)
                .add("city",artisan.city)
                .add("country", artisan.country)
                .add("artisanName", artisan.artisanName)
                .add("contactNumber", artisan.contactNumber)
                .add("lat", artisan.lat.toString())
                .add("lon", artisan.lon.toString())
                //TODO remove hard code balance
                .add("balance", "5000.0")
                .build()

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(addArtisanURL)
                .post(requestBody)
                .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("AddArtisan", "success $body")
                submitPictureToDB(artisan)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddArtisan", "failed to do POST request to database $addArtisanURL")
            }
        })
    }
}