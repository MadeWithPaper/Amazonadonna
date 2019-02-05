package com.amazonadonna.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo
import android.support.annotation.ColorLong
import com.beust.klaxon.Json
import java.io.Serializable
import java.util.*

@Entity(tableName = "product")
data class Product (
        @ColumnInfo(name = "price") @Json(name = "price") var price : Double,
        @PrimaryKey @Json(name = "itemId") var itemId : String,
        @ColumnInfo(name = "description") @Json(name = "description") var description : String,
        @ColumnInfo(name = "artisanId") @Json(name = "artisanId") var artisanId : String,
        @ColumnInfo(name = "pictureURL") @Json(name = "pictureURL") var pictureURL : String,
        @ColumnInfo(name = "category") @Json(name = "category") var category:  String,
        @ColumnInfo(name = "subCategory") @Json(name = "subCategory") var subCategory: String,
        @ColumnInfo(name = "specificCategory") @Json(name = "specificCategory") var specificCategory : String,
        @ColumnInfo(name = "itemName") @Json(name = "itemName") var itemName : String,
        @ColumnInfo(name = "ShippingOption") @Json(name = "ShippingOption") var ShippingOption : String,
        @ColumnInfo(name = "itemQuantity") @Json(name = "itemQuantity") var itemQuantity : Int,
        @ColumnInfo(name = "productionTime") @Json(name = "productionTime") var productionTime : Int) : Serializable {

    fun generateProductID() {
        //TODO fill in logic for generating unique ID for product
        var num = Random().nextInt()
        itemId = itemName + artisanId + num.toString()
    }

}