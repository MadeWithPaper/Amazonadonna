package com.amazonadonna.view

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.widget.Button
import com.amazonadonna.model.Order
import com.amazonadonna.model.Product
import com.amazonadonna.sync.OrderSync
import kotlinx.android.synthetic.main.activity_order_screen.*

class OrderScreen : AppCompatActivity() {
    var orderIdString = ""
    private lateinit var alertDialog : AlertDialog
    lateinit var order : Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_screen)

        setSupportActionBar(orderScreen_toolbar)

        order = intent.extras?.getSerializable("order") as Order
        orderIdString = order.orderId
        populateSelectedOrder(order)

        val editOrder: Button = findViewById(R.id.orderScreen_editOrder)
        editOrder.setOnClickListener { updateShippingStatus() }

        orderScreen_recyclerView.layoutManager = LinearLayoutManager(this)
        //load an empty list as placeholder before GET request completes
        val emptyItemList : MutableList<Product> = mutableListOf()
        orderScreen_recyclerView.adapter = ListItemsAdapter(this, emptyItemList, true)
        orderScreen_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onStart() {
        super.onStart()

        Log.d("OrderScreen", "In order screen")
        runOnUiThread {
            orderScreen_recyclerView.adapter = ListItemsAdapter(applicationContext, order.products, true)
        }
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
        orderScreen_OrderDate_input.text = "1/23/19"
        orderScreen_Fulfilled_input.text = order.fulfilledStatus.toString()
        orderScreen_Payout_input.text = "$" + order.totalCostDollars.toString()+"."+order.totalCostCents.toString()
    }
}