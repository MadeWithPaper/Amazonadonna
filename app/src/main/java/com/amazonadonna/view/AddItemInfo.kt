package com.amazonadonna.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
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
        val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan
        editMode = intent.extras?.get("editMode") as Boolean

        if (editMode) {
            addItemInfo_ProductNameTF.setText(product.itemName)
            addItemInfo_ProductPriceTF.setText(product.price.toString())
            addItemInfo_ProductDescriptionTF.setText(product.description)
            addItemInfo_ProductQuantityTF.setText(product.itemQuantity.toString())
            addItemInfo_ProductionTimeTF.setText(product.productionTime.toString())
        }

        addItemInfo_continueButton.setOnClickListener {
            addItemInfoContinue(product, artisan)
        }

        addItemInfo_ProductDescriptionTF.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        addItemInfo_ProductDescriptionTF.setRawInputType(InputType.TYPE_CLASS_TEXT)


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
    }

    private fun addItemInfoContinue(product: Product, artisan: Artisan) {
        val intent = Intent(this, AddItemImages::class.java)
        if (validateFields()) {
            updateProduct(product)
            intent.putExtra("product", product)
            intent.putExtra("selectedArtisan", artisan)
            intent.putExtra("editMode", editMode)
            Log.i("AddItemInfo", "product updated 2/4: " + product)
            clearFields()
            startActivity(intent)
            finish()
        }
    }

    //TODO add more checks
    fun validateFields() : Boolean {
        if (TextUtils.isEmpty(addItemInfo_ProductNameTF.text.toString())){
            addItemInfo_ProductNameTF.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (TextUtils.isEmpty(addItemInfo_ProductDescriptionTF.text.toString())) {
            addItemInfo_ProductDescriptionTF.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (TextUtils.isEmpty(addItemInfo_ProductionTimeTF.text.toString())){
            addItemInfo_ProductionTimeTF.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (TextUtils.isEmpty(addItemInfo_ProductPriceTF.text.toString())){
            addItemInfo_ProductPriceTF.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (shippmentMethod == SELECT_SHIPPING_METHOD) {
            Toast.makeText(this@AddItemInfo, this.resources.getString(R.string.add_item_info_shipping_warning), Toast.LENGTH_LONG).show()
            return false
        }

        if (addItemInfo_ProductPriceTF.text.toString() == "."){
            addItemInfo_ProductPriceTF.error = this.resources.getString(R.string.payout_amount_format_error)
            return false
        }

        return true
    }

    private fun clearFields() {
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
        product.itemQuantity = addItemInfo_ProductQuantityTF.text.toString().toInt()
        product.shippingOption = shippmentMethod
        product.productionTime = addItemInfo_ProductionTimeTF.text.toString().toInt()

        if (editMode)
        {
        //TODO fix
        } else {

        }
    }

}
