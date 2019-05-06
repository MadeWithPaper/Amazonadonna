package com.amazonadonna.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.amazonadonna.sync.Synchronizer
import com.beust.klaxon.Json
import java.io.Serializable
import java.util.*

@Entity(tableName = "product")
data class Product (
        @ColumnInfo(name = "price") @Json(name = "price") var price : Double,
        @PrimaryKey @Json(name = "itemId") var itemId : String,
        @ColumnInfo(name = "description") @Json(name = "description") var description : String,
        @ColumnInfo(name = "artisanId") @Json(name = "artisanId") var artisanId : String,
        @ColumnInfo(name = "pictureURLs") @Json(name = "pictureURLs") var pictureURLs : Array<String>,
        @Json(name = "pic0URL") var pic0URL : String,
        @Json(name = "pic1URL") var pic1URL : String,
        @Json(name = "pic2URL") var pic2URL : String,
        @Json(name = "pic3URL") var pic3URL : String,
        @Json(name = "pic4URL") var pic4URL : String,
        @Json(name = "pic5URL") var pic5URL : String,
        @ColumnInfo(name = "category") @Json(name = "category") var category:  String,
        @ColumnInfo(name = "subCategory") @Json(name = "subCategory") var subCategory: String,
        @ColumnInfo(name = "specificCategory") @Json(name = "specificCategory") var specificCategory : String,
        @ColumnInfo(name = "itemName") @Json(name = "itemName") var itemName : String,
        @ColumnInfo(name = "shippingOption") @Json(name = "shippingOption") var shippingOption : String,
        @ColumnInfo(name = "itemQuantity") @Json(name = "itemQuantity") var itemQuantity : Int,
        @ColumnInfo(name = "synced") var synced : Int = Synchronizer.SYNCED,
        @ColumnInfo(name = "productionTime") @Json(name = "productionTime") var productionTime : Int) : Serializable {

    fun generateTempID() {
        var num = Random().nextInt()
        itemId = ((artisanId.hashCode() + itemName.hashCode()) * 13).toString()
    }

}