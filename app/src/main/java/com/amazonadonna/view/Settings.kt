package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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
