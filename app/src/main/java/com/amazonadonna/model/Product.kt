package com.amazonadonna.model

class Product (var itemName : String, var price : Double, var owner : Artisan) {
    var SKU : String = ""

    init {
        generateSKU()
    }

    private fun generateSKU () {
        SKU = "x"
    }


}