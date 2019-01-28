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
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_artisan)
        val IMAGE_UPLOADING_PERMISSION = 3
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_UPLOADING_PERMISSION)
//        button_addArtisan.setOnClickListener{
//            //Toast.makeText(this@AddArtisan, "add button clicked.", Toast.LENGTH_SHORT).show()
//            makeNewArtisan()
//        }

        val takePhoto: Button = findViewById(R.id.takePicture)
        val chooseFromAlbum: Button = findViewById(R.id.selectPicture)

        takePhoto.setOnClickListener{
            takePhoto()
        }

        chooseFromAlbum.setOnClickListener{
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
                        Log.d("AFTERPHOTO", "IT WORK 34")
                        Log.d("AFTERPHOTO", "Exists?: " + photoFile!!.exists())
                    }
                    catch(e: Error) {
                        Log.d("AFTERPHOTO", "AINT WORK 34")
                    }
                }
            CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        createImageFile(data)
                        Log.d("AFTERGALLERY", "File:  Exists?: " + photoFile!!.exists())
                    }
                    else {
                        Log.d("AFTERGALLERY", "Data was null")
                    }
                }
            }
        setImageView()
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
        //Log.d("INFO", "add button clicked")
        var validFields = false
        //validate fields
        validFields = validateFields()
        Log.d("INFO", "validate fields performed, result: " + validFields.toString())
        val name = editText_Name.text.toString()
        val bio = editText_bio.text.toString()
        val number = editText_ContactNumber.text.toString()


        if (validFields) {
            val newArtisan = Artisan(name, "", "", "", bio, "0",0.0,0.0, "")
            newArtisan.generateArtisanID()
            //parse location info
            parseLoc(newArtisan)
            Log.d("INFO", "created new Artisan" + newArtisan.toString())

            //pop screen and add
            submitToDB(newArtisan)
            //clear all fields
            clearFields()
            Toast.makeText(this@AddArtisan, "Artisan added to database.", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
        }
    }

    fun parseLoc (artisan: Artisan) {
        val rawLoc = editText_loc.text.toString()

        val ind = rawLoc.indexOf(',')
//
//        System.out.print(rawLoc)
//        System.out.print(ind)
//        System.out.print(rawLoc.substring(0, ind))


        artisan.city = rawLoc.substring(0, ind)
        artisan.country = rawLoc.substring(ind+1)

//        System.out.print(artisan)
    }

    fun clearFields() {
        editText_Name.text.clear()
        editText_ContactNumber.text.clear()
        editText_bio.text.clear()
        editText_loc.text.clear()

        Log.d("INFO", "Clearing fields")
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

        if(photoFile == null) {
            Toast.makeText(this@AddArtisan, "No photo selected.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // source file does not exist
    fun submitPictureToDB(artisan: Artisan) {

//        val url = "https://4585da82.ngrok.io/updateArtisanImage"
        Log.d("hitFunction", "we here")
//            val sourceFile = File(Environment.getExternalStorageDirectory().path+"/handmade_logo.png")
        val sourceFile = photoFile!!
        //val sourceFile = File("file:///android_asset/handmade_logo.png")
        Log.d("drake", "File...::::" + sourceFile + " : " + sourceFile!!.exists())

//
//
//       // val IMGUR_CLIENT_ID = "...";
//        val MEDIA_TYPE_PNG = MediaType.parse("image/png");
//
//        val client = OkHttpClient();
//
//            // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
//            val requestBody = MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart("artisanId", artisan.artisanID)
//                    .addFormDataPart("image", "logo-square.png",
//                            RequestBody.create(MEDIA_TYPE_PNG, sourceFile))
//                    .build()
//
//            val request = Request.Builder()
//                    .url(url)
//                    .post(requestBody)
//                    .build()
//
//        client.newCall(request).enqueue(object: Callback {
//            override fun onResponse(call: Call?, response: Response?) {
//                val body = response?.body()?.string()
//                Log.d("INFO", body)
//            }
//
//            override fun onFailure(call: Call?, e: IOException?) {
//                Log.d("ERROR", "failed to do POST request to database")
//            }
//        })

//
//           // val MEDIA_TYPE = sourceImageFile.endsWith("png") ?
        val MEDIA_TYPE = MediaType.parse("image/png")


        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("artisanId", artisan.artisanId)
                .addFormDataPart("image", "profile.png", RequestBody.create(MEDIA_TYPE, sourceFile))
                .build()

        val request = Request.Builder()
                .url("https://4585da82.ngrok.io/updateArtisanImage")
                .post(requestBody)
                .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("INFO", body)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("ERROR", "failed to do POST request to database")
            }
        })



//        Log.d("hitFunction", "we here")
//        val file = File("@drawable/handmade_logo.png")
//        val url = "https://4585da82.ngrok.io/updateArtisanImage"
//        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("artisanId", artisan.artisanID)
//                .addFormDataPart("image","test", RequestBody.create(MediaType.parse("image/jpeg"),Environment.getExternalStorageDirectory().path+"/hsadklf.png"))
//                .build()
//
//        val client = OkHttpClient()
//
//        val request = Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build()
//
//        client.newCall(request).enqueue(object: Callback {
//            override fun onResponse(call: Call?, response: Response?) {
//                val body = response?.body()?.string()
//                Log.d("INFO", body)
//            }
//
//            override fun onFailure(call: Call?, e: IOException?) {
//                Log.d("ERROR", "failed to do POST request to database")
//            }
//        })


    }

    fun submitToDB(artisan: Artisan) {
        val url = "https://4585da82.ngrok.io/artisans"

        val requestBody = FormBody.Builder().add("artisanId",artisan.artisanId)
                .add("cgoId", artisan.cgoId)
                .add("bio", artisan.bio)
                .add("city",artisan.city)
                .add("country", artisan.country)
                .add("name", artisan.name)
                .add("lat", artisan.lat.toString())
                .add("lon", artisan.lon.toString())
                .build()

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("INFO", body)
                submitPictureToDB(artisan)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("ERROR", "failed to do POST request to database")
            }
        })
    }
}