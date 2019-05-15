package com.amazonadonna.artisanOnlyViews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.App
import com.amazonadonna.model.Artisan
import com.amazonadonna.view.ArtisanItemList
import com.amazonadonna.view.ListOrders
import com.amazonadonna.view.R
import com.amazonadonna.view.Settings
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_home_screen_artisan.*
import okhttp3.*
import java.io.IOException

class HomeScreenArtisan : AppCompatActivity() {
    private val getArtisanUrl = App.BACKEND_BASE_URL + "/artisan/getById"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen_artisan)

        App.artisanMode = true
        //TODO replace test data with artisan logged in
        val testArtisan = App.testArtisan
        //TODO set global artisan
        App.currentArtisan = testArtisan

        val extras = intent.extras
        if (extras != null) {
            fetchJSONArtisan(extras.getString("artisanID"))
        } else {
            //TODO fetch for artisan?
            artisanNameTV.text = testArtisan.artisanName
        }

        artisanProfile.setOnClickListener {
            openArtisanProfile(testArtisan)
        }

        artisanItemList_cga.setOnClickListener {
            itemListForArtisan(testArtisan)
        }

        artisanOrderList.setOnClickListener {
            orderListForArtisan(testArtisan)
        }

        setting.setOnClickListener {
            openSetting(testArtisan)
        }
    }

    private fun fetchJSONArtisan(artisanID: String) {
        val requestBody = FormBody.Builder().add("artisanId", artisanID)
                .build()
        val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "amazonadonna-main"
        ).fallbackToDestructiveMigration().build()
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(getArtisanUrl)
                .post(requestBody)
                .build()
        Log.d("HomeScreenArtisan", "In fetchArtisan with id: "+artisanID)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("HomeScreenArtisan", "response body from fetchArtisan: " + body)

                val gson = GsonBuilder().create()

                if (body == "{}") {
                    Log.d("HomeScreenArtisan", "artisan not in db")
                }

                try { // In here, might need to set artisanNameTV.text = artisan.artisanName
                    val artisan: Artisan = gson.fromJson(body, object : TypeToken<Artisan>() {}.type)
                    App.currentArtisan = artisan
                } catch(e: Exception) {
                    Log.d("HomeScreenArtisan", "Caught exception")
                    runOnUiThread {
                        Toast.makeText(this@HomeScreenArtisan,"Error getting user information", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("HomeScreenArtisan", "failed to do POST request to database" + getArtisanUrl)
            }
        })
    }

    private fun openArtisanProfile(artisan: Artisan){
        val intent = Intent(this, ArtisanProfile::class.java)
        intent.putExtra("artisan", artisan)
        startActivity(intent)
    }

    private fun itemListForArtisan(artisan: Artisan) {
        val intent = Intent(this, ArtisanItemList::class.java)
        intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
    }

    private fun openSetting(artisan: Artisan) {
        val intent = Intent(this, Settings::class.java)
        intent.putExtra("cgaID", artisan.cgaId)
        intent.putExtra("artisanName", artisan.artisanName)
        startActivity(intent)
    }

    private fun orderListForArtisan(artisan: Artisan) {
        val intent = Intent(this, ListOrders::class.java)
        intent.putExtra("cgaId", artisan.cgaId)
        intent.putExtra("artisanId", artisan.artisanId)
        startActivity(intent)
    }
}
