package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_add_item_info.*

class AddItemInfo : AppCompatActivity() {

    val SELECT_SHIPPING_METHOD = "--Select Shipping Method--"
    val shippingSpinnerValues = arrayOf(SELECT_SHIPPING_METHOD, "Fulfilled by Amazon", "")
    var shippmentMethod = SELECT_SHIPPING_METHOD
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_info)

        val product = intent.extras?.getSerializable("product") as Product

        addItemInfo_continueButton.setOnClickListener {
            addItemInfoContinue(product)
        }

        val shippingArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, shippingSpinnerValues)
        // Set layout to use when the list of choices appear
        shippingArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        addItemInfo_ProductShippingSpinner.adapter = shippingArrayAdapter

        addItemInfo_ProductShippingSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                 shippmentMethod = addItemInfo_ProductShippingSpinner.getSelectedItem().toString()
            }
        }
    }

    private fun addItemInfoContinue(product: Product) {
        val intent = Intent(this, AddItemImages::class.java)
        validateFields()
        updateProduct(product)
        intent.putExtra("product", product)
        Log.i("AddItemInfo", "product updated 2/4: " + product)
        clearFields()
        startActivity(intent)
    }

    //TODO add more checks
    fun validateFields() : Boolean {
        if (TextUtils.isEmpty(addItemInfo_ProductNameTF.text.toString())){
            addItemInfo_ProductNameTF.setError("Product Name can not be empty")
            return false
        }

        if (TextUtils.isEmpty(addItemInfo_ProductDescriptionTF.text.toString())) {
            addItemInfo_ProductDescriptionTF.setError("Product Description can not be empty")
            return false
        }

        if (TextUtils.isEmpty(addItemInfo_ProductionTimeTF.text.toString())){
            addItemInfo_ProductionTimeTF.setError("Production time can not be empty")
            return false
        }

        if (TextUtils.isEmpty(addItemInfo_ProductPriceTF.text.toString())){
            addItemInfo_ProductPriceTF.setError("Price can not be empty.")
            return false
        }

        if (shippmentMethod == SELECT_SHIPPING_METHOD) {
            Toast.makeText(this@AddItemInfo, "Please Select a Shipping Method.", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    fun clearFields() {
        addItemInfo_ProductNameTF.text.clear()
        addItemInfo_ProductDescriptionTF.text.clear()
        addItemInfo_ProductionTimeTF.text.clear()
        addItemInfo_ProductPriceTF.text.clear()
        addItemInfo_ProductQuantityTF.text.clear()
        addItemInfo_ProductShippingSpinner.setSelection(0)
        Log.i("AddItemInfo", "Clearing fields")
    }

    fun updateProduct(product: Product) {
        product.itemName = addItemInfo_ProductNameTF.text.toString()
        product.price = addItemInfo_ProductPriceTF.text.toString().toDouble()
        product.description = addItemInfo_ProductDescriptionTF.text.toString()
    }


}
