package com.amazonadonna.view

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
import android.util.DisplayMetrics





class Settings : AppCompatActivity() {

    val languageList = arrayOf("English", "Spanish", "French")
    private var languageSelected = "en_US"
    private lateinit var cgaID : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        cgaID = intent.extras!!.getString("cgaID")!!
        // Create an ArrayAdapter
        val mainArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageList)
        // Set layout to use when the list of choices appear
        mainArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        languageSpinner.adapter = mainArrayAdapter

        languageSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val spinnerValue = languageSpinner.getSelectedItem().toString()
                when (spinnerValue) {
                   "English" -> languageSelected = "en_US"
                    "Spanish" -> languageSelected = "es_US"
                    "French" -> languageSelected = ""
                }
            }
        }

        saveSettingsButton.setOnClickListener {
            updateSetting()
        }

        cancelSettingButton.setOnClickListener {
            cancelSetting()
        }
    }

    private fun updateSetting(){
        //change language
        Log.d("HomeScreen", "old locale ${Locale.getDefault()}")
        val locale = Locale(languageSelected)
        Locale.setDefault(locale)
        Log.d("HomeScreen", "new locale ${Locale.getDefault()}")
        //back to home screen
        val intent = Intent(this, HomeScreen::class.java)
        Log.d("Settings", "new language picked: " + languageSelected)
        intent.putExtra("languageSelected", languageSelected)
        intent.putExtra("cgaId", cgaID)
        val res = this.resources
        res.configuration.setLocale(locale)

        recreate()
        //startActivity(intent)
    }

    private fun cancelSetting() {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
    }
}
