package com.amazonadonna.view

import android.arch.persistence.room.Room
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.database.Cursor
import android.os.Bundle
import android.support.v4.content.Loader
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.Order
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_list_orders.*
import okhttp3.*
import java.io.IOException

class ListOrders : AppCompatActivity(), LoaderCallbacks<Cursor> {
    var cgaId : String = "0"
    val listOrderURL = "https://7bd92aed.ngrok.io/order/listAllForCgo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_orders)

        cgaId = intent.extras.getString("cgaId")

        recyclerView_listOrders.layoutManager = LinearLayoutManager(this)

        //load an empty list as placeholder before GET request completes
        val emptyOrdersList : List<Order> = emptyList()
        recyclerView_listOrders.adapter = ListOrdersAdapter(this, emptyOrdersList)
        recyclerView_listOrders.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onStart() {
        super.onStart()

        fetchJSON()
    }

    private fun fetchJSON() {
        val requestBody = FormBody.Builder().add("cgoId", cgaId)
                .build()
        val request = Request.Builder().url(listOrderURL).post(requestBody).build()
        val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "amazonadonna-main"
        ).fallbackToDestructiveMigration().build()
        val artisanDao = db.artisanDao()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()

                Log.d("ORDERS", body)
                println(body)
                val gson = GsonBuilder().create()
                val orders : List<Order> = gson.fromJson(body,  object : TypeToken<List<Order>>() {}.type)

//                artisanDao.insertAll(orders)
                Log.d("ORDERS", "worked")
                runOnUiThread {
                    recyclerView_listOrders.adapter = ListOrdersAdapter(applicationContext, orders)
                }

            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request")
                Log.d("ERROR", "Failed to execute GET request to " + listOrderURL)
            }
        })
    }

    override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<Cursor> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoadFinished(p0: Loader<Cursor>, p1: Cursor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoaderReset(p0: Loader<Cursor>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
