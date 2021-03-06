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
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class HomeScreenArtisan : AppCompatActivity() , CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val getArtisanUrl = App.BACKEND_BASE_URL + "/artisan/getById"
    private lateinit var alertDialog : AlertDialog
    lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen_artisan)

        App.artisanMode = true

        val extras = intent.extras
        if (extras != null) {
            var artisan = extras.get("artisan") as Artisan
            App.currentArtisan = artisan
            artisanNameTV.text = App.currentArtisan.artisanName
            syncData()
        } else {
            artisanNameTV.text = App.currentArtisan.artisanName
        }

        artisanProfile.setOnClickListener {
            openArtisanProfile(App.currentArtisan)
        }

        artisanItemList_cga.setOnClickListener {
            itemListForArtisan(App.currentArtisan)
        }

        artisanOrderList.setOnClickListener {
            orderListForArtisan(App.currentArtisan)
        }

        setting.setOnClickListener {
            openSetting(App.currentArtisan)
        }

        artisanHomeScreenSwipeRefreshLayout.setOnRefreshListener{
            syncData()
            artisanHomeScreenSwipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        artisanNameTV.text = App.currentArtisan.artisanName
    }

    private fun syncData() {
        job = Job()

        if (ArtisanSync.hasInternet(applicationContext)) {

            runOnUiThread {
                alertDialog = AlertDialog.Builder(this@HomeScreenArtisan).create()
                alertDialog.setTitle("Synchronizing Account")
                alertDialog.setMessage("Please wait while your account data is synchronized. Image uploads may take a few minutes...")
                alertDialog.show()
            }

            launch {
                val query = async(Dispatchers.IO) {
                    AppDatabase.getDatabase(applicationContext).artisanDao().insert(App.currentArtisan)
                }
            }

            launch {
                val task = async {
                    Synchronizer.getArtisanSync().syncArtisanMode(applicationContext, this@HomeScreenArtisan, App.currentArtisan.artisanId)

                    // Wait for sync to finish
                    do {
                        Log.i("Settings", Synchronizer.getArtisanSync().inProgress().toString())
                        Thread.sleep(1000)
                    } while (Synchronizer.getArtisanSync().inProgress())
                }
                task.await()

                val task2 = async {
                    Log.d("Settings", "First sync done, now one more to verify data integrity")

                    // Perform one more data fetch to ensure data integrity is goodandroid button do asynch
                    Synchronizer.getArtisanSync().syncArtisanMode(applicationContext, this@HomeScreenArtisan, App.currentArtisan.artisanId)

                    do {
                        Thread.sleep(500)
                    } while (Synchronizer.getArtisanSync().inProgress())
                }
                task2.await()

                runOnUiThread {
                    alertDialog.dismiss()
                }
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
