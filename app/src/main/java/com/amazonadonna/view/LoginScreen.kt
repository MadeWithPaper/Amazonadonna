package com.amazonadonna.view

import android.arch.persistence.room.Room
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.util.Log
import android.content.Intent

import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.*
import com.amazon.identity.auth.device.api.workflow.RequestContext
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.Artisan
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

import kotlinx.android.synthetic.main.activity_login_screen.*
import kotlinx.android.synthetic.main.list_all_artisans.*
import okhttp3.*
import java.io.IOException


class LoginScreen : AppCompatActivity() {

    private var cgaID : String? = null
    private var requestContext : RequestContext = RequestContext.create(this)

    private var signUpListener = object  : AuthorizeListener() {
        /* Authorization was completed successfully. */
        override fun onSuccess(result: AuthorizeResult) {
            User.fetch(this@LoginScreen, getUserInfoListener)
            /* Your app is now authorized for the requested scopes */
            val intent = Intent(this@LoginScreen, HomeScreen::class.java)
//            intent.putExtra("cgaId", cgaID!!)
            startActivity(intent)
            finish()
        }

        /* There was an error during the attempt to authorize the application. */
        override fun onError(ae: AuthError) {
            /* Inform the user of the error */
        }

        /* Authorization was cancelled before it could be completed. */
        override fun onCancel(cancellation: AuthCancellation) {
            /* Reset the UI to a ready-to-login state */
        }
    }

    private var checkTokenListener = object  : Listener<AuthorizeResult, AuthError> {
        override fun onSuccess(ar: AuthorizeResult?) {
            if(ar?.accessToken != null) { //user already signed in to app
                User.fetch(this@LoginScreen, getUserInfoListener)

                val intent = Intent(this@LoginScreen, HomeScreen::class.java)
//                intent.putExtra("cgaId", cgaID!!)
                startActivity(intent)
                Log.d("LoginScreen", ar?.accessToken)
                finish()
            }
        }

        override fun onError(ae: AuthError?) {
            //To change body of created functions use File | Settings | File Templates.
        }
    }

    private var getUserInfoListener = object : Listener<User, AuthError> {
        override fun onSuccess(p0: User?) {
            cgaID = p0!!.userId.substringAfter("amzn1.account.")
            fetchJSONCGA()
            Log.d("LoginScreen",cgaID)
        }

        override fun onError(ae: AuthError?) {
            //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        requestContext.registerListener(signUpListener)


        email_sign_in_button.setOnClickListener { test()/*attemptLogin()*/ }

        login_with_amazon.setOnClickListener(View.OnClickListener {
            _ -> AuthorizationManager.authorize(AuthorizeRequest
                        .Builder(requestContext)
                        .addScopes(ProfileScope.profile(),  ProfileScope.userId())
                        .build())
        })
    }

    override fun onStart() {
        super.onStart()
        val scopes : Array<Scope> = arrayOf(ProfileScope.profile(), ProfileScope.postalCode())
        AuthorizationManager.getToken(this, scopes, checkTokenListener)
    }

    override fun onResume() {
        super.onResume()
        requestContext.onResume()
    }

    private fun test() {
        // go to home screen
        val intent =  Intent(this, HomeScreen::class.java)
        startActivity(intent)
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
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

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("LoginScreen", "response body: " + body)

                val gson = GsonBuilder().create()

                if (body == "{}") {
                    Log.d("LoginScreen", "artisan not in db")
                    addCGOToDB()
                }

//                val artisans : List<Artisan> = gson.fromJson(body,  object : TypeToken<List<Artisan>>() {}.type)

            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("LoginScreen", "failed to do POST request to database" + url)
            }
        })
    }

    private fun addCGOToDB() {
        val url = "https://7bd92aed.ngrok.io/cgo/add"

        val requestBody = FormBody.Builder().add("cgoId", cgaID!!)
                .add("city", "San Francisco").add("country","USA")
                .add("name", "Dean").add("lat", "32.19").add("lon", "77.398").build()
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
                Log.i("LoginScreen", "response body: " + body)

                val gson = GsonBuilder().create()

//                val artisans : List<Artisan> = gson.fromJson(body,  object : TypeToken<List<Artisan>>() {}.type)

            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("LoginScreen", "failed to do POST request to database" + url)
            }
        })
    }
}
