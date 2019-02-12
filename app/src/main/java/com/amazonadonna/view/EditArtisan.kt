package com.amazonadonna.view

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_edit_artisan.*
import okhttp3.*
import java.io.IOException

class EditArtisan : AppCompatActivity() {

    private val editArtisanURL = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_artisan)

        val oldArtisan = intent.extras?.getSerializable("artisan") as Artisan

        //fill in information from old artisan
        editArtisanBio.setText(oldArtisan.bio)
        editArtisan_cc.setText(oldArtisan.city + "," + oldArtisan.country)
        editArtisan_name.setText(oldArtisan.name)
        editArtisan_number.setText("1234567")
        editArisan_SaveButton.setOnClickListener {
            updateArtisan(oldArtisan)
        }
    }

    private fun parseLoc () : Pair<String, String> {
        val rawLoc = editArtisan_cc.text.toString()
        val ind = rawLoc.indexOf(',')
        return Pair(rawLoc.substring(0, ind), rawLoc.substring(ind+1))
    }

    private fun updateArtisan(oldArtisan : Artisan) {
        if (!validateFields()) {
            return
        } else {
            //submitToDB(oldArtisan)
            val intent = Intent(this, ArtisanProfile::class.java)
            //TODO
            intent.putExtra("artisan", oldArtisan)
            startActivity(intent)
        }
    }

    private fun submitToDB(oldArtisan: Artisan) {
        val requestBody = FormBody.Builder().add("artisanId", oldArtisan.artisanId)
                .add("cgoId", oldArtisan.cgoId)
                .add("bio", editArtisanBio.text.toString())
                .add("city", parseLoc().first)
                .add("country", parseLoc().second)
                .add("name", editArtisan_name.text.toString())
                .add("lat", 0.0.toString())
                .add("lon", 0.0.toString())
                .build()

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(editArtisanURL)
                .post(requestBody)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("EditArtisan", body)
                //submitPictureToDB(artisan)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("EditArtisan", "failed to do POST request to database" + editArtisanURL)
            }
        })
    }

    private fun validateFields() : Boolean {
        if (TextUtils.isEmpty(editArtisan_name.text.toString())){
            editArtisan_name.error = "Artisan Name can not be empty"
            return false
        }

        if (TextUtils.isEmpty(editArtisan_cc.text.toString())) {
            editArtisan_cc.error = "Location field can not be empty"
            return false
        }

        if ((!editArtisan_cc.text.toString().contains(","))) {
            editArtisan_cc.error = "Missing ' , ' between City and Country"
            return false
        }

        if (TextUtils.isEmpty(editArtisan_number.text.toString())){
            editArtisan_number.error = "Contact Number can not be empty"
            return false
        }

        if (TextUtils.isEmpty(editArtisanBio.text.toString())){
            editArtisanBio.error = "bio is empty"
            return false
        }

        return true
    }
}
