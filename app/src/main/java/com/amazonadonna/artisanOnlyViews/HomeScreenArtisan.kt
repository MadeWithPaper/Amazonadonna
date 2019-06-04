package com.amazonadonna.artisanOnlyViews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.App
import com.amazonadonna.model.Artisan
import com.amazonadonna.sync.ArtisanSync
import com.amazonadonna.sync.Synchronizer
import com.amazonadonna.view.ArtisanItemList
import com.amazonadonna.view.ListOrders
import com.amazonadonna.view.R
import com.amazonadonna.view.Settings
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_home_screen_artisan.*
import kotlinx.android.synthetic.main.activity_home_screen_artisan.artisanNameTV
import kotlinx.android.synthetic.main.activity_home_screen_artisan.setting
import okhttp3.*
import java.io.IOException

class HomeScreenArtisan : AppCompatActivity() {
    private val getArtisanUrl = App.BACKEND_BASE_URL + "/artisan/getById"
    private lateinit var alertDialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen_artisan)

        App.artisanMode = true
        //TODO replace test data with artisan logged in
        val testArtisan = App.testArtisan
        //TODO set global artisan
//        App.currentArtisan = testArtisan

        val extras = intent.extras

        if (extras != null) {
            //TODO: Use actual artisan id once login is returning that correctly
            var artisan = extras.get("artisan") as Artisan
            App.currentArtisan = artisan
            AppDatabase.getDatabase(applicationContext).artisanDao().insert(App.currentArtisan)
            artisanNameTV.text = App.currentArtisan.artisanName
            syncData()
            ArtisanSync.updateArtisan(this@HomeScreenArtisan, artisan)

        } else {
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

        artisanHomeScreenSwipeRefreshLayout.setOnRefreshListener{
            syncData()
            artisanHomeScreenSwipeRefreshLayout.isRefreshing = false
        }
    }

    private fun fetchJSONArtisan(artisanID: String) {
        val requestBody = FormBody.Builder().add("artisanId", artisanID)
                .build()
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
                        //Toast.makeText(this@HomeScreenArtisan,"Error getting user information", Toast.LENGTH_LONG).show()
                    }
                }

                AppDatabase.getDatabase(applicationContext).artisanDao().insert(App.currentArtisan)
                artisanNameTV.text = App.currentArtisan.artisanName
                syncData()
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("HomeScreenArtisan", "failed to do POST request to database" + getArtisanUrl)
            }
        })
    }

    fun syncData() {
        if (ArtisanSync.hasInternet(applicationContext)) {
            runOnUiThread {
                alertDialog = AlertDialog.Builder(this@HomeScreenArtisan).create()
                alertDialog.setTitle("Synchronizing Account")
                alertDialog.setMessage("Please wait while your account data is synchronized, this may take a few minutes...")
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.show()
                Log.i("HomeScreen", "loading start, show dialog")
            }

            //ArtisanSync.resetLocalDB(applicationContext)

            Synchronizer.getArtisanSync().syncArtisanMode(applicationContext,this@HomeScreenArtisan, App.currentArtisan.artisanId)

            // Wait for sync to finish
            do {
                Thread.sleep(1000)
            } while (ArtisanSync.inProgress())

            Log.d("HomeScreen", "First sync done, now one more to verify data integrity")

            // Perform one more data fetch to ensure data integrity is good
            Synchronizer.getArtisanSync().syncArtisanMode(applicationContext,this@HomeScreenArtisan, App.currentArtisan.artisanId)

            do {
                Thread.sleep(500)
            } while (ArtisanSync.inProgress())

            runOnUiThread {
                Log.i("HomeScreen", "end of loading alert dialog dismiss")
                alertDialog.dismiss()
            }
        }
        else {
            runOnUiThread {
                alertDialog = AlertDialog.Builder(this@HomeScreenArtisan).create()
                alertDialog.setTitle("Error Synchronizing Account")
                alertDialog.setMessage("No internet connection active. You may attempt to resync your account on the Settings page when internet is available.")
                alertDialog.show()
            }
        }
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
