package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {

    val languageList = arrayOf("English", "Spanish")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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
                   "English" -> return
                    "Spanish" -> return
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

        //Update

        //back to home screen
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
    }

    private fun cancelSetting() {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
    }
}
