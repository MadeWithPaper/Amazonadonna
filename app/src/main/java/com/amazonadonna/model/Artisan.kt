package com.amazonadonna.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo
import com.beust.klaxon.Json
import java.io.Serializable
import java.util.*

@Entity
data class Artisan (
        @ColumnInfo(name = "name") @Json(name = "name") var name : String,
        @PrimaryKey @Json(name = "artisanID") var artisanID : String,
        @ColumnInfo(name = "city") @Json(name = "city") var city : String,
        @ColumnInfo(name = "city") @Json(name = "country") var country : String,
        @ColumnInfo(name = "city") @Json(name = "bio")var bio : String,
        @ColumnInfo(name = "city") @Json(name = "cgoId") var cgoID : String,
        @ColumnInfo(name = "city") @Json(name = "lon") var lon : Double,
        @ColumnInfo(name = "city") @Json(name = "lat") var lat : Double,
        @ColumnInfo(name = "city") @Json(name = "pictureURL") var pictureURL : String) : Serializable {

    fun generateArtisanID() {
        //TODO fill in logic for generating unique ID for artisan
        var num = Random().nextInt()
        artisanID = name + cgoID + num.toString()
    }


    //TODO need to add maps functionality
}