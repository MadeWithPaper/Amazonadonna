package com.amazonadonna.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_add_item_info.*

class AddItemInfo : AppCompatActivity() {

    val SELECT_SHIPPING_METHOD = "--Select Shipping Method--"
    val shippingSpinnerValues = arrayOf(SELECT_SHIPPING_METHOD, "Fulfilled by Amazon", "Self Shipping")
    var shippmentMethod = SELECT_SHIPPING_METHOD
    var editMode : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_info)

        val product = intent.extras?.getSerializable("product") as Product
        editMode = intent.extras?.get("editMode") as Boolean

        if (editMode) {
            addItemInfo_ProductName_et.setText(product.itemName)
            addItemInfo_ProductPrice_et.setText(product.price.toString())
            addItemInfo_ProductDescription_et.setText(product.description)
            addItemInfo_ProductQuantity_et.setText(product.itemQuantity.toString())
            addItemInfo_ProductionTime_et.setText(product.productionTime.toString())
        }

        addItemInfo_continueButton.setOnClickListener {
            addItemInfoContinue(product)
        }

        setSupportActionBar(addItemInfo_toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

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

        if (editMode) {
            addItemInfo_ProductShippingSpinner.setSelection(shippingArrayAdapter.getPosition(product.shippingOption))
        }

        addItemInfo_scrollViewContents.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                hideKeyboard(v)
                return true
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun addItemInfoContinue(product: Product) {
        val intent = Intent(this, AddItemImages::class.java)
        if (validateFields()) {
            updateProduct(product)
            intent.putExtra("product", product)
            //intent.putExtra("selectedArtisan", artisan)
            intent.putExtra("editMode", editMode)
            Log.i("AddItemInfo", "product updated 2/4: " + product)
            //clearFields()
            startActivity(intent)
            finish()
        }
    }

    //TODO add more checks
    private fun validateFields() : Boolean {
        var error_check = 0
        if (addItemInfo_ProductName_et.text.toString().isEmpty()){
            addItemInfo_ProductName_til.error = this.resources.getString(R.string.requiredFieldError)
            error_check += 1
            //return false
        }

        if (addItemInfo_ProductDescription_et.text.toString().isEmpty()) {
            addItemInfo_ProductDescription_til.error = this.resources.getString(R.string.requiredFieldError)
            error_check += 1
            //return false
        }

        if (addItemInfo_ProductionTime_et.text.toString().isEmpty()){
            addItemInfo_ProductionTime_til.error = this.resources.getString(R.string.requiredFieldError)
            error_check += 1
            //return false
        }

        if (addItemInfo_ProductPrice_et.text.toString().isEmpty()){
            addItemInfo_ProductPrice_til.error = this.resources.getString(R.string.requiredFieldError)
            error_check += 1
            //return false
        }

        if (addItemInfo_ProductQuantity_et.text.toString().isEmpty()){
            addItemInfo_ProductQuantity_til.error = this.resources.getString(R.string.requiredFieldError)
            error_check += 1
            //return false
        }

        if (shippmentMethod == SELECT_SHIPPING_METHOD) {
            Toast.makeText(this@AddItemInfo, this.resources.getString(R.string.add_item_info_shipping_warning), Toast.LENGTH_LONG).show()
            error_check += 1
            //return false
        }

        if (addItemInfo_ProductPrice_et.text.toString().contains(".")){
            addItemInfo_ProductPrice_til.error = this.resources.getString(R.string.payout_amount_format_error)
            error_check += 1
            //return false
        }

        if (error_check > 0) {
            error_check = 0
            return false
        }

        return true
    }

//    private fun clearFields() {
//        addItemInfo_ProductNameTF.text.clear()
//        addItemInfo_ProductDescriptionTF.text.clear()
//        addItemInfo_ProductionTimeTF.text.clear()
//        addItemInfo_ProductPriceTF.text.clear()
//        addItemInfo_ProductQuantityTF.text.clear()
//        addItemInfo_ProductShippingSpinner.setSelection(0)
//        Log.i("AddItemInfo", "Clearing fields")
//    }

    private fun updateProduct(product: Product) {
        product.itemName = addItemInfo_ProductName_et.text.toString()
        product.price = addItemInfo_ProductPrice_et.text.toString().toDouble()
        product.description = addItemInfo_ProductDescription_et.text.toString()
        product.itemQuantity = addItemInfo_ProductQuantity_et.text.toString().toInt()
        product.shippingOption = shippmentMethod
        product.productionTime = addItemInfo_ProductionTime_et.text.toString().toInt()

        if (editMode)
        {
        //TODO fix
        } else {

        }
    }
}
