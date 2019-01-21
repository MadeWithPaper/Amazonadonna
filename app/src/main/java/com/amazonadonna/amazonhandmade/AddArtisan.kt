package com.amazonadonna.amazonhandmade

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_artisan.*
import Artisan
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import okhttp3.*
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.app.Activity
import android.graphics.BitmapFactory
import android.support.v4.app.ActivityCompat
import java.io.*
import java.util.jar.Manifest


class AddArtisan : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_artisan)
        val GET_FROM_GALLERY = 1
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 3)
//        button_addArtisan.setOnClickListener{
//            //Toast.makeText(this@AddArtisan, "add button clicked.", Toast.LENGTH_SHORT).show()
//            makeNewArtisan()
//        }


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
            val newArtisan = Artisan(name, "", "", "", bio, "0",0.0,0.0)
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

        return true
    }




    fun submitPictureToDB(artisan: Artisan) {

//        val url = "https://4585da82.ngrok.io/updateArtisanImage"
        Log.d("hitFunction", "we here")
            val sourceFile = File(Environment.getExternalStorageDirectory().path+"/handmade_logo.png")
            //val sourceFile = File("file:///android_asset/handmade_logo.png")
            Log.d("drake", "File...::::" + sourceFile + " : " + sourceFile.exists())

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
                    .addFormDataPart("artisanId", artisan.artisanID)
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
        val url = "https://4585da82.ngrok.io/addArtisanToDatabase"

        val requestBody = FormBody.Builder().add("artisanId",artisan.artisanID)
                .add("cgoId", artisan.cgoID)
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
