package com.amazonadonna.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.amazonadonna.sync.Synchronizer
import com.beust.klaxon.Json
import java.io.Serializable
import java.util.*

@Entity(tableName = "artisan")
data class Artisan (
        @ColumnInfo(name = "artisanName") @Json(name = "artisanName") var artisanName : String,
        @PrimaryKey @ColumnInfo(name = "artisanId") @Json(name = "artisanId") var artisanId : String,
        @ColumnInfo(name = "phoneNumber") @Json (name = "phoneNumber") var phoneNumber : String,
        @ColumnInfo(name = "artisanEmail") @Json (name = "artisanEmail") var email : String,
        @ColumnInfo(name = "city") @Json(name = "city") var city : String,
        @ColumnInfo(name = "country") @Json(name = "country") var country : String,
        @ColumnInfo(name = "bio") @Json(name = "bio")var bio : String,
        @ColumnInfo(name = "cgaId") @Json(name = "cgaId") var cgaId : String,
        @ColumnInfo(name = "lat") @Json(name = "lon") var lon : Double,
        @ColumnInfo(name = "lon") @Json(name = "lat") var lat : Double,
        @ColumnInfo(name = "picURL") @Json(name = "picURL") var picURL : String?,
        @ColumnInfo(name = "synced") var synced : Int = Synchronizer.SYNCED,
        @ColumnInfo(name = "balance") @Json(name = "balance") var balance : Double) : Serializable {

    fun generateTempID() {
       //TODO fill in logic for generating unique ID for artisan
        var num = Random().nextInt()
        artisanId = artisanName + cgaId + num.toString()
    }


    //TODO need to add maps functionality
}