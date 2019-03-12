package com.amazonadonna.view

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*
import android.content.ComponentName
import android.content.DialogInterface
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager
import com.amazonadonna.sync.ArtisanSync
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import kotlin.coroutines.CoroutineContext

class Settings : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val languageList = arrayOf("English", "Spanish", "French")
    private val SETTING_INTENT = 13
    private var languageSelected = "en_US"
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

        cancelSettingButton.setOnClickListener {
            cancelSetting()
        }

        settingLogOut.setOnClickListener {
            logout()
        }

        syncDataButton.setOnClickListener {
            syncData()
        }
    }

    private fun updateSetting(){
        val intent = Intent(this, HomeScreen::class.java)
       // intent.putExtra("cgaId", cgaID)
        startActivity(intent)
        finish()
    }

    private fun syncData() {
        job = Job()

        if (ArtisanSync.hasInternet(applicationContext)) {

            runOnUiThread {
                alertDialog = AlertDialog.Builder(this@Settings).create()
                alertDialog.setTitle("Synchronizing Account")
                alertDialog.setMessage("Please wait while your account data is synchronized...")
                alertDialog.show()
            }

            launch {
                val task = async {
                    ArtisanSync.sync(applicationContext, cgaID)
                    Log.d("Settings", cgaID)

                    // Wait for sync to finish
                    do {
                        Log.i("Settings", ArtisanSync.inProgress().toString())
                        sleep(1000)
                    } while (ArtisanSync.inProgress())
                }
                task.await()

                val task2 = async {
                    Log.d("Settings", "First sync done, now one more to verify data integrity")

                    // Perform one more data fetch to ensure data integrity is goodandroid button do asynch
                    ArtisanSync.sync(applicationContext, cgaID)
                    do {
                        sleep(500)
                    } while (ArtisanSync.inProgress())
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
        AuthorizationManager.signOut(this, signoutListener)
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

    private fun cancelSetting() {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
        finish()
    }
}
