package com.amazonadonna.view

import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.amazonadonna.model.Artisan
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_artisan.*
import okhttp3.*
import java.io.File
import java.io.IOException

class EditArtisan : AppCompatActivity() {

    private var photoFile: File? = null
    private val editArtisanURL = "https://7bd92aed.ngrok.io/artisan/edit"
    private val updateArtisanURL = "https://7bd92aed.ngrok.io/artisan/updateImage"
    private lateinit var pic : Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_artisan)

        val oldArtisan = intent.extras?.getSerializable("artisan") as Artisan

        //fill in information from old artisan
        editArtisanBio.setText(oldArtisan.bio)
        editArtisan_cc.setText(oldArtisan.city + "," + oldArtisan.country)
        editArtisan_name.setText(oldArtisan.artisanName)
        editArtisan_number.setText("1234567")

        if (oldArtisan.picURL != "Not set") {
            Picasso.with(this).load(oldArtisan.picURL).into(this.editArtisan_pic)
        } else {
            this.editArtisan_pic.setImageResource(R.drawable.placeholder)
        }

        pic = editArtisan_pic.drawable

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

            oldArtisan.artisanName = editArtisan_name.text.toString()
            oldArtisan.bio = editArtisanBio.text.toString()
            oldArtisan.city = parseLoc().first
            oldArtisan.country = parseLoc().second

            submitToDB(oldArtisan)
            val intent = Intent(this, ArtisanProfile::class.java)
            intent.putExtra("artisan", oldArtisan)
            startActivity(intent)
            finish()
        }
    }

    private fun submitToDB(oldArtisan: Artisan) {
        var updatePic = false

        val requestBody = FormBody.Builder().add("artisanId", oldArtisan.artisanId)
                .add("cgoId", oldArtisan.cgoId)
                .add("bio", oldArtisan.bio)
                .add("city", oldArtisan.city)
                .add("country", oldArtisan.country)
                .add("artisanName", oldArtisan.artisanName)
                .add("lat", 0.0.toString())
                .add("lon", 0.0.toString())

        if (editArtisan_pic.drawable == pic) {
            requestBody.add("picURL", oldArtisan.picURL)
        } else {
            updatePic = true
        }

        val client = OkHttpClient()
        val request = Request.Builder()
                .url(editArtisanURL)
                .post(requestBody.build())
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("EditArtisan", body)

                if (updatePic) {
                    updateArtisanPic(oldArtisan)
                }
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

    fun updateArtisanPic(artisan: Artisan) {
        val sourceFile = photoFile!!
        Log.d("AddArtisan", "submitPictureToDB file" + sourceFile + " : " + sourceFile!!.exists())

        val MEDIA_TYPE = MediaType.parse("image/png")

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("artisanId", artisan.artisanId)
                .addFormDataPart("image", "profile.png", RequestBody.create(MEDIA_TYPE, sourceFile))
                .build()

        val request = Request.Builder()
                .url(updateArtisanURL)
                .post(requestBody)
                .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("AddArtisan", body)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("AddArtisan", "failed to do POST request to database" + updateArtisanURL)
            }
        })
    }
}
