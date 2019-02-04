package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_item_info.*

class AddItemInfo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_info)

        addItemInfo_continueButton.setOnClickListener {
            addItemInfoContinue()
        }
    }

    private fun addItemInfoContinue() {
            val intent = Intent(this, AddItemImages::class.java)
            //intent.putExtra("product", product)
            startActivity(intent)
    }
}
