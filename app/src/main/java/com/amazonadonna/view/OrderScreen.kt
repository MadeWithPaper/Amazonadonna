package com.amazonadonna.view

import androidx.room.Room
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.App
import com.amazonadonna.model.Order
import com.amazonadonna.model.Product
import com.amazonadonna.sync.OrderSync
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_add_artisan.*
import kotlinx.android.synthetic.main.activity_order_screen.*
import okhttp3.*
import java.io.IOException

class OrderScreen : AppCompatActivity() {
    var orderIdString = ""
    val getItemURL = App.BACKEND_BASE_URL +  "/order/getItems"
    private lateinit var alertDialog : AlertDialog
    lateinit var order : Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_screen)

        setSupportActionBar(orderScreen_toolbar)

        order = intent.extras?.getSerializable("order") as Order
        orderIdString = order.orderId
        populateSelectedOrder(order)

        //TODO fill in editOrder button onClick listener
        val editOrder: Button = findViewById(R.id.orderScreen_editOrder)
        editOrder.setOnClickListener { updateShippingStatus() }

        // TODO implement a fetch of order data once backend route/database are configured
        orderScreen_recyclerView.layoutManager = LinearLayoutManager(this)
        //load an empty list as placeholder before GET request completes
        val emptyItemList : MutableList<Product> = mutableListOf()
        orderScreen_recyclerView.adapter = ListItemsAdapter(this, emptyItemList)
        //TODO make order screen adapter
        orderScreen_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onStart() {
        super.onStart()

        Log.d("OrderScreen", "In order screen")
        runOnUiThread {
            orderScreen_recyclerView.adapter = ListItemsAdapter(applicationContext, order.products)
        }
        //fetchJSON()
    }

    private fun updateShippingStatus() {
        var fulfilledStatus = order.fulfilledStatus
        order.fulfilledStatus = !fulfilledStatus
        OrderSync.updateOrder(applicationContext, order)
        runOnUiThread {
            populateSelectedOrder(order)
        }
        runOnUiThread {
            alertDialog = AlertDialog.Builder(this@OrderScreen).create()
            alertDialog.setTitle("Success!")
            alertDialog.setMessage("Updated fulfilled status to: "+order.fulfilledStatus.toString())
            alertDialog.show()
            Log.i("OrderScreen", "showing alert")
        }
    }


    private fun populateSelectedOrder(order: Order) {
        supportActionBar!!.title = order.orderId
        //val orderIDTextView : TextView = findViewById(R.id.orderScreen_toolbar_input)
        val orderDateTextView : TextView = findViewById(R.id.orderScreen_OrderDate_input)
        val orderFulfilledTextView : TextView = findViewById(R.id.orderScreen_Fulfilled_input)
        val orderCostTextView : TextView = findViewById(R.id.orderScreen_Payout_input)
//        orderIDTextView.text = order.amOrderNumber
       // orderIDTextView.text = order.orderId
        orderDateTextView.text = "1/23/19"
        orderFulfilledTextView.text = order.fulfilledStatus.toString()
        orderCostTextView.text = "$" + order.totalCostDollars.toString()+"."+order.totalCostCents.toString()
    }
}