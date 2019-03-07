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
import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager

class Settings : AppCompatActivity() {

    val languageList = arrayOf("English", "Spanish", "French")
    private val SETTING_INTENT = 13
    private var languageSelected = "en_US"
    private lateinit var cgaID : String
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
    }

    private fun updateSetting(){
        //change language
        Log.d("HomeScreen", "old locale ${Locale.getDefault()}")
        //val locale = Locale(languageSelected)
       // Locale.setDefault(locale)
        Log.d("HomeScreen", "new locale ${Locale.getDefault()}")
        //back to home screen
        val intent = Intent(this, HomeScreen::class.java)
        Log.d("Settings", "new language picked: " + languageSelected)
        intent.putExtra("languageSelected", languageSelected)
        intent.putExtra("cgaId", cgaID)
       // val res = this.resources
       // res.configuration.setLocale(locale)

       // recreate()
        startActivity(intent)
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
    }
}
