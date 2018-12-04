package com.amazonadonna.amazonhandmade

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_artisan.*
import Artisan
import android.widget.Toast

class AddArtisan : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_artisan)

        button_addArtisan.setOnClickListener{
            Toast.makeText(this@AddArtisan, "You clicked me.", Toast.LENGTH_SHORT).show()

            makeNewArtisan()
        }
    }

    fun makeNewArtisan() {
        //Log.d("INFO", "add button clicked")
        System.out.print("button clicked")
        val name = editText_Name.toString()
        val bio = editText_bio.toString()
        val cgo = editText_CGOName.toString()
        val number = editText_ContactNumber.toString()

        val newArtisan = Artisan(name, "", "", "", bio, cgo,0.0,0.0)
        parseLoc(newArtisan)

        System.out.print(newArtisan)

        //Log.d("INFO", "created new Artisan" + newArtisan.toString())
    }

    fun parseLoc (artisan: Artisan) {
        val rawLoc = editText_loc.text
        val ind = rawLoc.indexOf(',')

        artisan.city = rawLoc.substring(0, ind)
        artisan.country = rawLoc.substring(ind, -1)
    }
}
