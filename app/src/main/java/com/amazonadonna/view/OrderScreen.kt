package com.amazonadonna.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.widget.Button
import android.widget.TextView
import com.amazonadonna.model.Order
import com.amazonadonna.model.Product
import kotlinx.android.synthetic.main.activity_order_screen.*

class OrderScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_screen)

        val order : Order = intent.extras?.getSerializable("order") as Order
        populateSelectedOrder(order)

        //TODO fill in editOrder button onClick listener
        val editOrder: Button = findViewById(R.id.orderScreen_editOrder)

        // TODO implement a fetch of order data once backend route/database are configured
        orderScreen_recyclerView.layoutManager = LinearLayoutManager(this)
        //load an empty list as placeholder before GET request completes
        val emptyItemList : List<Product> = emptyList()
        orderScreen_recyclerView.adapter = ListItemsAdapter(this, emptyItemList)
        //TODO make order screen adapter
        orderScreen_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun populateSelectedOrder(order: Order) {
        val orderIDTextView : TextView = findViewById(R.id.orderScreen_toolbar_input)
        val orderDateTextView : TextView = findViewById(R.id.orderScreen_OrderDate_input)
        val orderShippedTextView : TextView = findViewById(R.id.orderScreen_Shipped_input)
        val orderCostTextView : TextView = findViewById(R.id.orderScreen_Payout_input)
        orderIDTextView.text = order.orderId
        orderDateTextView.text = order.orderDate
        orderShippedTextView.text = order.shippingStatus.toString()
        orderCostTextView.text = order.totalCost.toString()
    }
}