package com.amazonadonna.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_artisan.*
import com.amazonadonna.model.Artisan
import android.annotation.TargetApi
import android.widget.ImageView

import android.text.TextUtils
import android.util.Log
import android.widget.Toast
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

class AddArtisan : AppCompatActivity() {
    private var photoFile: File? = null
    private val fileName: String = "output.png"
    private val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    private val CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE = 1046
    private val addArtisanURL = "https://7bd92aed.ngrok.io/artisan/add"
    private val artisanPicURL = "https://7bd92aed.ngrok.io/artisan/updateImage"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_artisan)
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
                    }
                    catch(e: Error) {
                        Log.d("Add Artisan post Photo", "it failed")
                    }
                }
            CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        createImageFile(data)
                        Log.d("Add Artisan postGallery", "File:  Exists?: " + photoFile!!.exists())
                        setImageView()
                    }
                    else {
                        Log.d("Add Artisan postGallery", "Data was null")
                    }
                }
            }
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
    fun makeNewArtisan() {
        //validate fields
        if (!validateFields()) {
            return
        }
        val name = editText_Name.text.toString()
        val bio = editText_bio.text.toString()
        val number = editText_ContactNumber.text.toString()

         val newArtisan = Artisan(name, "", "", "", bio, "0",0.0,0.0, "", 0.0)
            //TODO move to back end soon
            newArtisan.generateArtisanID()
            //parse location info
            parseLoc(newArtisan)
            Log.i("AddArtisan", "created new Artisan" + newArtisan.toString())

            //pop screen and add
            submitToDB(newArtisan)

            //clear all fields
            clearFields()
            super.onBackPressed()

    }

    fun parseLoc (artisan: Artisan) {
        val rawLoc = editText_loc.text.toString()

        val ind = rawLoc.indexOf(',')

        artisan.city = rawLoc.substring(0, ind)
        artisan.country = rawLoc.substring(ind+1)
    }

    fun clearFields() {
        editText_Name.text.clear()
        editText_ContactNumber.text.clear()
        editText_bio.text.clear()
        editText_loc.text.clear()

        Log.i("AddArtisan", "Clearing fields")
    }

    //Validate all fields entered
    //TODO add more checks
    fun validateFields() : Boolean {
        if (TextUtils.isEmpty(editText_Name.text.toString())){
            editText_Name.setError("Artisan Name can not be empty")
            return false
        }

        if (TextUtils.isEmpty(editText_loc.text.toString())) {
            editText_loc.setError("Location field can not be empty")
            return false
        }

        if ((!editText_loc.text.toString().contains(","))) {
            editText_loc.setError("Missing ' , ' between City and Country")
            return false
        }

        if (TextUtils.isEmpty(editText_ContactNumber.text.toString())){
            editText_ContactNumber.setError("Contact Number can not be empty")
            return false
        }

        if (TextUtils.isEmpty(editText_bio.text.toString())){
            editText_bio.setError("bio is empty")
            return false
        }

        return true
    }

    // source file does not exist
    fun submitPictureToDB(artisan: Artisan) {
        val sourceFile = photoFile!!
        Log.d("AddArtisan", "submitPictureToDB file" + sourceFile + " : " + sourceFile!!.exists())

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
                Log.e("AddArtisan", "failed to do POST request to database" + artisanPicURL)
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
                .add("lat", artisan.lat.toString())
                .add("lon", artisan.lon.toString())
                .build()

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(addArtisanURL)
                .post(requestBody)
                .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("AddArtisan", body)
                submitPictureToDB(artisan)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddArtisan", "failed to do POST request to database" + addArtisanURL)
            }
        })
    }
}