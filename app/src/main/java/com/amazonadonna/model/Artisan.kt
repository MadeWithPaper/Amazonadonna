package com.amazonadonna.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo
import com.beust.klaxon.Json
import java.io.Serializable
import java.util.*

@Entity(tableName = "artisan")
data class Artisan (
        @ColumnInfo(name = "name") @Json(name = "name") var name : String,
        @PrimaryKey @Json(name = "artisanId") var artisanId : String,
        @ColumnInfo(name = "city") @Json(name = "city") var city : String,
        @ColumnInfo(name = "country") @Json(name = "country") var country : String,
        @ColumnInfo(name = "bio") @Json(name = "bio")var bio : String,
        @ColumnInfo(name = "cgoId") @Json(name = "cgoId") var cgoId : String,
        @ColumnInfo(name = "lat") @Json(name = "lon") var lon : Double,
        @ColumnInfo(name = "lon") @Json(name = "lat") var lat : Double,
        @ColumnInfo(name = "picURL") @Json(name = "picURL") var picURL : String?) : Serializable {

    fun generateArtisanID() {
        //TODO fill in logic for generating unique ID for artisan
        var num = Random().nextInt()
        artisanId = name + cgoId + num.toString()
    }


    //TODO need to add maps functionality
}