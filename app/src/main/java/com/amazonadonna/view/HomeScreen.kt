package com.amazonadonna.view

import androidx.room.Room
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.User
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.sync.ArtisanSync
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_home_screen.*
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import androidx.appcompat.app.AlertDialog
import com.amazonadonna.model.App
import com.amazonadonna.sync.Synchronizer
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import kotlin.coroutines.CoroutineContext


class HomeScreen : AppCompatActivity()  , CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    lateinit var job: Job
    private var cgaID : String = "0" // initialize to prevent crash while testing
    private var cgaAmaznName : String = ""
    private var newLang : String = "en_US"
    private val amaznIdURL = App.BACKEND_BASE_URL + "/cga/getByAmznId"
    private val addAmaznIdURL = App.BACKEND_BASE_URL + "/cga/add"

    private lateinit var alertDialog : AlertDialog
    private var currUser : User? = null
    private var getUserInfoListener = object : Listener<User, AuthError> {
        override fun onSuccess(p0: User?) {
            cgaID = p0!!.userId.substringAfter("amzn1.account.")

            //cgaID = "0" //******** Uncomment this to go back to default for testing ****
            //--------------------------------------------------------//
            // UNCOMMENT THE METHOD CALL BELOW TO CLEAR SQLITE TABLES //
            //--------------------------------------------------------//
            //ArtisanSync.resetLocalDB(applicationContext)
            //--------------------------------------------------------//

            try {
                currUser = p0
                artisanNameTV.text = p0.userName
                cgaAmaznName = p0.userName
            } catch (e : Exception) {
                //do nothing use placeholder text
            }

            syncData()
        }
        override fun onError(ae: AuthError?) {
            //To change body of created functions use File | Settings | File Templates.
            Log.d("HomeScreen", "no work")
            //TODO remove after testing if error it should not fetch
            Synchronizer.getArtisanSync().sync(applicationContext, this@HomeScreen, cgaID)
        }
    }

    override fun onStart() {
        App.artisanMode = false
        super.onStart()
        if (currUser != null){
            artisanNameTV.text = currUser!!.userName
        }
    }

    private fun syncData() {
        job = Job()

        if (ArtisanSync.hasInternet(applicationContext)) {

            runOnUiThread {
                alertDialog = AlertDialog.Builder(this@HomeScreen).create()
                alertDialog.setTitle("Synchronizing Account")
                alertDialog.setMessage("Please wait while your account data is synchronized. Image uploads may take a few minutes...")
                alertDialog.show()
            }

            launch {
                val task = async {
                    Synchronizer.getArtisanSync().sync(applicationContext,this@HomeScreen, cgaID)
                    fetchJSONCGA()
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
                    Synchronizer.getArtisanSync().sync(applicationContext,this@HomeScreen, cgaID)

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
                alertDialog = AlertDialog.Builder(this@HomeScreen).create()
                alertDialog.setTitle("Error Synchronizing Account")
                alertDialog.setMessage("No internet connection active. You may attempt to resync your account on the Settings page when internet is available.")
                alertDialog.show()
            }
        }
    }

    private var signoutListener = object : Listener<Void, AuthError> {
        override fun onSuccess(p0: Void?) {
            Log.d("HomeScreen", "Logout worked")
        }

        override fun onError(ae: AuthError?) {
            Log.d("HomeScreen", "Logout failed :(")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        val extras = intent.extras
        //cgaID = "0"

        if (extras != null) {
            cgaID = extras.getString("cgaId")
        } else {
            User.fetch(this, getUserInfoListener)
        }

        //List All com.amazonadonna.model.Artisan button
        listAllArtisan.setOnClickListener{
            queryAllArtisan()
        }

        listOrders.setOnClickListener {
            queryAllOrder()
        }

        setting.setOnClickListener {
            openSettings()
        }

        reports.setOnClickListener {
            openReports()
        }

        cgaHomeScreenSwipeRefreshLayout.setOnRefreshListener{
            syncData()
            cgaHomeScreenSwipeRefreshLayout.isRefreshing = false
        }

    }

    private fun openReports() {
        val intent = Intent(this, Reports::class.java)
        startActivity(intent)
    }

    private fun openSettings() {
        val intent = Intent(this, Settings::class.java)
        intent.putExtra("cgaID", cgaID)
        intent.putExtra("artisanName", "place_holder")
        startActivity(intent)
    }

    private fun queryAllArtisan() {
        //go to list all artisan screen
        val intent = Intent(this, ListAllArtisans::class.java)
        intent.putExtra("cgaId", cgaID!!)
        startActivity(intent)

    }


    private fun queryAllOrder() {
        //go to list all artisan screen
        val intent = Intent(this, ListOrders::class.java)
        intent.putExtra("cgaId", cgaID!!)
        startActivity(intent)

    }

    private fun fetchJSONCGA() {
        val requestBody = FormBody.Builder().add("amznId", cgaID!!)
                .build()
        val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "amazonadonna-main"
        ).fallbackToDestructiveMigration().build()
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(amaznIdURL)
                .post(requestBody)
                .build()
        Log.d("HomeScreen", "In fetchCGA")
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("HomeScreen", "response body from fetchCga: " + body)

                val gson = GsonBuilder().create()

                if (body == "{}") {
                    Log.d("HomeScreen", "artisan not in db")
                    addCGAToDB()
                }

//                val artisans : List<Artisan> = gson.fromJson(body,  object : TypeToken<List<Artisan>>() {}.type)

            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("HomeScreen", "failed to do POST request to database" + amaznIdURL)
            }
        })
    }

    private fun addCGAToDB() {
       // val url = "https://7bd92aed.ngrok.io/cga/add"
        val requestBody = FormBody.Builder().add("amznId", cgaID!!)
                .add("city", "San Francisco").add("country","USA")
                .add("name", cgaAmaznName).add("lat", "32.19").add("lon", "77.398").build()
        val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "amazonadonna-main"
        ).fallbackToDestructiveMigration().build()
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(addAmaznIdURL)
                .post(requestBody)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("HomeScreen", "response body from addCga: " + body)

                val gson = GsonBuilder().create()

//                val artisans : List<Artisan> = gson.fromJson(body,  object : TypeToken<List<Artisan>>() {}.type)

            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("HomeScreen", "failed to do POST request to database" + addAmaznIdURL)
            }
        })
    }


}
