package com.amazonadonna.view

import android.arch.persistence.room.Room
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.database.Cursor
import android.os.Bundle
import android.support.v4.content.Loader
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.util.DiffUtil
import android.util.Log
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.Order
import com.amazonadonna.view.R
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_list_orders.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.jakewharton.rxbinding2.widget.textChanges

class ListOrders : AppCompatActivity(), LoaderCallbacks<Cursor> {
    var cgaId : String = "0"
    val listOrderURL = "https://7bd92aed.ngrok.io/order/listAllForCgo"
    val originalOrders : MutableList<Order> = mutableListOf()
    val filteredOrders: MutableList<Order> = mutableListOf()
    val oldFilteredOrders: MutableList<Order> = mutableListOf()

    fun search(query: String): Completable = Completable.create {
        val wanted = originalOrders.filter {
            it.orderId.contains(query) || it.shippingAddress.contains(query) ||
                    it.shippedStatus.toString().contains(query)
        }.toList()

        if (listOrders_Search.text.toString() == "") { // empty search bar
            filteredOrders.clear()
            filteredOrders.addAll(originalOrders)
        }
        else {
            filteredOrders.clear()
            filteredOrders.addAll(wanted)
        }
        Log.d("ListOrders", "editText: "+listOrders_Search.text.toString())
        Log.d("ListOrders", "originalOrders: " +originalOrders.toString())
        Log.d("ListOrders", "filteredOrders: "+filteredOrders.toString())
        it.onComplete()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        filteredOrders.clear()
        originalOrders.clear()
        oldFilteredOrders.clear()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_orders)

        cgaId = intent.extras.getString("cgaId")

        recyclerView_listOrders.layoutManager = LinearLayoutManager(this)

        //load an empty list as placeholder before GET request completes
        recyclerView_listOrders.adapter = ListOrdersAdapter(this, originalOrders)
        recyclerView_listOrders.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        listOrders_Search
                .textChanges()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe {
                   search(it.toString())
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                val diffResult = DiffUtil.calculateDiff(PostsDiffUtilCallback(oldFilteredOrders, filteredOrders))
                                oldFilteredOrders.clear()
                                oldFilteredOrders.addAll(filteredOrders)
                                diffResult.dispatchUpdatesTo((recyclerView_listOrders.adapter as ListOrdersAdapter))
                            }
                }

    }

    override fun onStart() {
        filteredOrders.clear()
        originalOrders.clear()
        oldFilteredOrders.clear()
        super.onStart()
        filteredOrders.clear()
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

                Log.d("ListOrders", body)
                println(body)
                val gson = GsonBuilder().create()
                val orders : List<Order> = gson.fromJson(body,  object : TypeToken<List<Order>>() {}.type)
                originalOrders.addAll(orders)
                oldFilteredOrders.addAll(orders)
                filteredOrders.addAll(orders)

//                artisanDao.insertAll(orders)
                Log.d("ListOrders", "worked")
                runOnUiThread {
                    recyclerView_listOrders.adapter = ListOrdersAdapter(applicationContext, oldFilteredOrders)
                }

            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request")
                Log.e("ListOrders", "Failed to execute GET request to " + listOrderURL)
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

    inner class PostsDiffUtilCallback(private val oldList: List<Order>, private val newList: List<Order>) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].orderId == newList[newItemPosition].orderId

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true // for the sake of simplicity we return true here but it can be changed to reflect a fine-grained control over which part of our views are updated
    }
}
