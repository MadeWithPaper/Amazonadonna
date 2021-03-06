package com.amazonadonna.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*
import android.content.ComponentName
import androidx.appcompat.app.AlertDialog
import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager
import com.amazonadonna.artisanOnlyViews.HomeScreenArtisan
import com.amazonadonna.model.App
import com.amazonadonna.sync.ArtisanSync
import com.amazonadonna.sync.Synchronizer
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.regions.Regions
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import kotlin.coroutines.CoroutineContext

class Settings : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val SETTING_INTENT = 13
    private lateinit var alertDialog : AlertDialog
    private lateinit var cgaID : String
    lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        cgaID = intent.extras!!.getString("cgaID")!!

        settingCurrentLanguageTV.text = Locale.getDefault().displayLanguage

        settingLanguageButton.setOnClickListener {
            toChangeLanguage()
        }

        saveSettingsButton.setOnClickListener {
            updateSetting()
        }

        settingLogOut.setOnClickListener {
            logout()
        }

        syncDataButton.setOnClickListener {
            syncData()
        }
    }

    override fun onBackPressed() {
        runOnUiThread {
            alertDialog = AlertDialog.Builder(this@Settings).create()
            alertDialog.setTitle("Are you sure?")
            alertDialog.setMessage("Any changed settings will be overridden if you have not clicked 'Update Settings'")
            alertDialog.setButton(-1, "Home") { dialog, which ->
                super.onBackPressed()
            }
            alertDialog.setButton(-2, "Stay Here") { dialog, which ->
                alertDialog.dismiss()
            }
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
            Log.i("Settings", "back pressed, showing dialog")
        }
    }

    private fun updateSetting(){
        var intent = Intent(this, HomeScreen::class.java)
        if (App.artisanMode) {
            intent = Intent(this, HomeScreenArtisan::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun syncData() {
        job = Job()

        if (ArtisanSync.hasInternet(applicationContext)) {

            runOnUiThread {
                alertDialog = AlertDialog.Builder(this@Settings).create()
                alertDialog.setTitle("Synchronizing Account")
                alertDialog.setMessage("Please wait while your account data is synchronized. Image uploads may take a few minutes...")
                alertDialog.show()
            }

            launch {
                val task = async {
                    if (App.artisanMode)
                        Synchronizer.getArtisanSync().syncArtisanMode(applicationContext, this@Settings, App.currentArtisan.artisanId)
                    else
                        Synchronizer.getArtisanSync().sync(applicationContext, this@Settings, cgaID)

                    Log.d("Settings", cgaID)

                    // Wait for sync to finish
                    do {
                        Log.i("Settings", Synchronizer.getArtisanSync().inProgress().toString())
                        sleep(1000)
                    } while (Synchronizer.getArtisanSync().inProgress())
                }
                task.await()

                val task2 = async {
                    Log.d("Settings", "First sync done, now one more to verify data integrity")

                    // Perform one more data fetch to ensure data integrity is goodandroid button do asynch
                    if (App.artisanMode)
                        Synchronizer.getArtisanSync().syncArtisanMode(applicationContext, this@Settings, App.currentArtisan.artisanId)
                    else
                        Synchronizer.getArtisanSync().sync(applicationContext, this@Settings, cgaID)

                    do {
                        sleep(500)
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
                alertDialog = AlertDialog.Builder(this@Settings).create()
                alertDialog.setTitle("Error Synchronizing Account")
                alertDialog.setMessage("No internet connection active. You may attempt to resync your account on the Settings page when internet is available.")
                alertDialog.show()
            }
        }
    }

    private var signoutListener = object : Listener<Void, AuthError> {
        override fun onSuccess(p0: Void?) {
            Log.d("Setting", "Logout worked")
            val intent = Intent(this@Settings, LoginScreen::class.java)
            finishAffinity()
            startActivity(intent)
        }

        override fun onError(ae: AuthError?) {
            Log.d("Setting", "Logout failed :(")
        }
    }

    private fun logout() {
        var userPool = CognitoUserPool(this, this.resources.getString(R.string.userPoolID), this.resources.getString(R.string.clientID), this.resources.getString(R.string.clientScret), Regions.US_EAST_2)
        if (App.artisanMode) {
            App.artisanMode = false
            var user = userPool.getUser(App.currentArtisan.email)
            user.signOut()

            val intent = Intent(this@Settings, LoginScreen::class.java)
            finishAffinity()
            startActivity(intent)
        } else {
            AuthorizationManager.signOut(this, signoutListener)
        }
    }

    private fun toChangeLanguage() {
        val intent = Intent()
        intent.component = ComponentName("com.android.settings", "com.android.settings.LanguageSettings")
        startActivityForResult(intent, SETTING_INTENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SETTING_INTENT) {
           recreate()
        }
    }
}
