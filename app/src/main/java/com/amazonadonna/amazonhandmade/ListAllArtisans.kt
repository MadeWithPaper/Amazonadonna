package com.amazonadonna.amazonhandmade

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import java.net.URL


class ListAllArtisans : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_all_artisans)
        val result = URL("").readText()

//          JSON data sample
//        { city: { S: ‘CityName’ },
//            bio: { S: ’Hi I\‘m Juan.’ },
//            lon: { N: ‘0’ },
//            artisanId: { S: ‘F5SJ72’ },
//            lat: { N: ‘0’ },
//            country: { S: ‘MX’ },
//            name: { S: ‘Juan Gonzalez’ },
//            cgoId: { S: ‘0’ } }

    }

}