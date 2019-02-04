package com.amazonadonna.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo
import com.beust.klaxon.Json
import java.io.Serializable
import java.util.*

@Entity(tableName = "product")
data class Product (
        @ColumnInfo(name = "price") @Json(name = "price") var price : Double,
        @PrimaryKey @Json(name = "itemId") var itemId : String,
        @ColumnInfo(name = "description") @Json(name = "description") var description : String,
        @ColumnInfo(name = "artisanId") @Json(name = "artisanId") var artisanId : String,
        @ColumnInfo(name = "pictureURL") @Json(name = "pictureURL")var pictureURL : String,
        @ColumnInfo(name = "itemName") @Json(name = "itemName") var itemName : String) : Serializable {

    fun generateProductID() {
        //TODO fill in logic for generating unique ID for artisan
        var num = Random().nextInt()
        itemId = itemName + artisanId + num.toString()
    }

}