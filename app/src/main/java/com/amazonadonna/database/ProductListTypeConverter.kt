package com.amazonadonna.database

import android.arch.persistence.room.TypeConverter
import com.amazonadonna.model.Product
import com.amazonaws.auth.policy.Policy.fromJson
import com.google.gson.reflect.TypeToken
import java.util.Collections.emptyList
import com.google.gson.Gson
import java.util.*


class ProductListTypeConverter {
    var gson = Gson()

    @TypeConverter
    fun stringToProductList(data: String): List<Product> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<Product>>() {}.type

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun productListToString(products: List<Product>): String {
        return gson.toJson(products)
    }
}