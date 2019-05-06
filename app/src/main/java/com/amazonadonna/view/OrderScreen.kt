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

        runOnUiThread {
            orderScreen_recyclerView.adapter = ListItemsAdapter(applicationContext, order.products)
        }
        //fetchJSON()
    }

    private fun updateShippingStatus() {
        var shippedStatus = order.shippedStatus
        order.shippedStatus = !shippedStatus
        OrderSync.updateOrder(applicationContext, order)
        runOnUiThread {
            populateSelectedOrder(order)
        }
        runOnUiThread {
            alertDialog = AlertDialog.Builder(this@OrderScreen).create()
            alertDialog.setTitle("Success!")
            alertDialog.setMessage("Updated shipped status to: "+order.shippedStatus.toString())
            alertDialog.show()
            Log.i("OrderScreen", "showing alert")
        }
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
                val products : MutableList<Product> = gson.fromJson(body,  object : TypeToken<List<Product>>() {}.type)

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
        orderCostTextView.text = "$" + order.totalCostDollars.toString()+"."+order.totalCostCents.toString()
    }
}