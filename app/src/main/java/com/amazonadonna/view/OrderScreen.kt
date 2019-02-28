package com.amazonadonna.view

import android.arch.persistence.room.Room
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.Order
import com.amazonadonna.model.Product
import com.amazonadonna.view.R
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_order_screen.*
import okhttp3.*
import java.io.IOException

class OrderScreen : AppCompatActivity() {
    var orderIdString = ""
    val getItemURL = "https://99956e2a.ngrok.io/order/getItems"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_screen)

        val order = intent.extras?.getSerializable("order") as Order
        orderIdString = order.orderId
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

    override fun onStart() {
        super.onStart()

        fetchJSON()
    }

    //TODO update "artisanDao" to be productDaogi
    private fun fetchJSON() {
        val requestBody = FormBody.Builder()
                .add("orderId", orderIdString)
                .build()
        val request = Request.Builder().url(getItemURL).post(requestBody).build()
        val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "amazonadonna-main"
        ).fallbackToDestructiveMigration().build()
//        val artisanDao = db.artisanDao()
        Log.d("ORDERID", orderIdString)

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()

                Log.d("ITEMS", body)
                val gson = GsonBuilder().create()
                val products : List<Product> = gson.fromJson(body,  object : TypeToken<List<Product>>() {}.type)

//                artisanDao.insertAll(orders)
                Log.d("ITEMS", "worked")
                runOnUiThread {
                    orderScreen_recyclerView.adapter = ListItemsAdapter(applicationContext, products)
                }

            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request")
                Log.d("ERROR", "Failed to execute GET request to " + getItemURL)
            }
        })
    }

    private fun populateSelectedOrder(order: Order) {
        val orderIDTextView : TextView = findViewById(R.id.orderScreen_toolbar_input)
        val orderDateTextView : TextView = findViewById(R.id.orderScreen_OrderDate_input)
        val orderShippedTextView : TextView = findViewById(R.id.orderScreen_Shipped_input)
        val orderCostTextView : TextView = findViewById(R.id.orderScreen_Payout_input)
//        orderIDTextView.text = order.amOrderNumber
        orderIDTextView.text = order.orderId
        orderDateTextView.text = "1/23/19"
        orderShippedTextView.text = order.shippedStatus.toString()
        orderCostTextView.text = order.totalCostDollars.toString()+"."+order.totalCostCents.toString()
    }
}