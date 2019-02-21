package com.amazonadonna.view

import android.arch.persistence.room.Room
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager
import com.amazon.identity.auth.device.api.authorization.User
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.sync.ArtisanSync
import com.amazonadonna.view.R
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_home_screen.*
import okhttp3.*
import java.io.IOException

class HomeScreen : AppCompatActivity() {
    private var cgaID : String = "0" // initialize to prevent crash while testing

    private var getUserInfoListener = object : Listener<User, AuthError> {
        override fun onSuccess(p0: User?) {
            cgaID = p0!!.userId.substringAfter("amzn1.account.")
//            cgaID = "0" //******** Uncomment this to go back to default for testing ****
            //--------------------------------------------------------//
            // UNCOMMENT THE METHOD CALL BELOW TO CLEAR SQLITE TABLES //
            //--------------------------------------------------------//
            //ArtisanSync.resetLocalDB(applicationContext)
            //--------------------------------------------------------//
            ArtisanSync.sync(applicationContext, cgaID)
            fetchJSONCGA()
            Log.d("HomeScreen", cgaID)
        }

        override fun onError(ae: AuthError?) {
            //To change body of created functions use File | Settings | File Templates.
            Log.d("HomeScreen", "no work")
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

        if (extras != null) {
            cgaID = extras.getString("cgaId")
        } else {
            User.fetch(this, getUserInfoListener)
        }

        //actionBar.set
        //List All com.amazonadonna.model.Artisan button
        listAllArtisan.setOnClickListener{
            queryAllArtisan()
        }

        listOrders.setOnClickListener {
            queryAllOrder()
        }

        logoutButton.setOnClickListener{
            AuthorizationManager.signOut(this, signoutListener)
            val intent = Intent(this, LoginScreen::class.java)
            finishAffinity()
            startActivity(intent)
        }

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
        val url = "https://7bd92aed.ngrok.io/cgo/getByAmznId"
        val requestBody = FormBody.Builder().add("amznId", cgaID!!)
                .build()
        val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "amazonadonna-main"
        ).fallbackToDestructiveMigration().build()
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
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
                    addCGOToDB()
                }

//                val artisans : List<Artisan> = gson.fromJson(body,  object : TypeToken<List<Artisan>>() {}.type)

            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("HomeScreen", "failed to do POST request to database" + url)
            }
        })
    }

    private fun addCGOToDB() {
        val url = "https://7bd92aed.ngrok.io/cgo/add"
        val requestBody = FormBody.Builder().add("amznId", cgaID!!)
                .add("city", "San Francisco").add("country","USA")
                .add("name", "Victor").add("lat", "32.19").add("lon", "77.398").build()
        val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "amazonadonna-main"
        ).fallbackToDestructiveMigration().build()
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
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
                Log.e("HomeScreen", "failed to do POST request to database" + url)
            }
        })
    }


}
