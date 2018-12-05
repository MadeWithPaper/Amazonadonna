package com.amazonadonna.amazonhandmade

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_artisan.*
import Artisan
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import okhttp3.*
import java.io.IOException

class AddArtisan : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_artisan)

        button_addArtisan.setOnClickListener{
            //Toast.makeText(this@AddArtisan, "add button clicked.", Toast.LENGTH_SHORT).show()
            makeNewArtisan()
        }
    }


    //TODO clean up
    fun makeNewArtisan() {
        //Log.d("INFO", "add button clicked")
        var validFields = false
        //validate fields
        validFields = validateFields()

        val name = editText_Name.text.toString()
        val bio = editText_bio.text.toString()
        val cgo = editText_CGOName.text.toString()
        val number = editText_ContactNumber.text.toString()


        if (validFields) {
        val newArtisan = Artisan(name, "e", "", "", bio, cgo,0.0,0.0)
            newArtisan.generateArtisanID()
        //parse location info
        parseLoc(newArtisan)
//
//        System.out.print(newArtisan)

        Log.d("INFO", "created new Artisan" + newArtisan.toString())

        //pop screen and add
        submitToDB(newArtisan)
        //clear all fields
        clearFields() }
        Toast.makeText(this@AddArtisan, "Artisan added to database.", Toast.LENGTH_SHORT).show()

    }

    fun parseLoc (artisan: Artisan) {
        val rawLoc = editText_loc.text.toString()

        val ind = rawLoc.indexOf(',')

        System.out.print(rawLoc)
        System.out.print(ind)
        System.out.print(rawLoc.substring(0, ind))


        artisan.city = rawLoc.substring(0, ind)
        artisan.country = rawLoc.substring(ind+1)

        System.out.print(artisan)
    }

    fun clearFields() {
        editText_Name.text.clear()
        editText_CGOName.text.clear()
        editText_ContactNumber.text.clear()
        editText_bio.text.clear()
        editText_loc.text.clear()
    }

    //Validate all fields entered
    //TODO add more checks
    fun validateFields() : Boolean {
        if (TextUtils.isEmpty(editText_Name.text.toString())){
            editText_Name.setError("Artisan Name can not be empty")
            return false
        }

        if (editText_loc.text.isEmpty()) {
            editText_loc.setError("Location field can not be empty")
            return false
        }

        if ((!editText_loc.text.toString().contains(","))) {
            editText_loc.setError("Missing ' , ' between City and Country")
            return false
        }

        if (editText_CGOName.text.isEmpty()){
            editText_CGOName.setError("CGO Name field can not be empty")
            return false
        }

        if (editText_ContactNumber.text.isEmpty()){
            editText_ContactNumber.setError("Contact Number can not be empty")
            return false
        }

        if (editText_bio.text.isEmpty()){
            editText_bio.setError("bio is empty")
            return false
        }

        return true
    }


    fun submitToDB(artisan: Artisan) {
        val url = "https://29d4c6b3.ngrok.io/addArtisanToDatabase"
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
                println(body) //To change body of created functions use File | Settings | File Templates.
            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }
}
