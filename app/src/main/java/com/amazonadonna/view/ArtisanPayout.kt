package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_artisan_payout.*

class ArtisanPayout : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_payout)

        artisanPayout_continue.setOnClickListener {
            continueToSignature()
        }
    }

    private fun continueToSignature() {
        val intent = Intent(this, PayoutSignature::class.java)
        startActivity(intent)
    }
}
