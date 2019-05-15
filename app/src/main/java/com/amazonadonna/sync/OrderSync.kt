package com.amazonadonna.sync

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.database.PictureListTypeConverter
import com.amazonadonna.model.App
import com.amazonadonna.model.Order
import com.amazonadonna.model.Product
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

object OrderSync: Synchronizer(), CoroutineScope {
    private const val TAG = "OrderSync"
    private val listOrderURL = App.BACKEND_BASE_URL + "/order/listAllForCga"
    private val getItemURL = App.BACKEND_BASE_URL + "/order/getItems"
    private val editOrderURL = App.BACKEND_BASE_URL + "/order/setFulfilledStatus"

    fun sync(context: Context, activity: Activity, cgaId: String) {
        super.sync(context, cgaId)

        Log.i(TAG, "Syncing now!")
        updateOrders(context, activity)
        Log.i(TAG, "Done uploading, now downloading")

    }

    private fun updateOrders(context: Context, activity: Activity) {
        runBlocking {
            val updateOrders = getUpdateOrders(context)
            for (order in updateOrders) {
                updateSingleOrder(context, order)
            }
        }
        downloadOrders(context, activity)
        Log.i(TAG, "Done syncing!")
    }

    private fun updateSingleOrder(context: Context, order: Order) {
        numInProgress++

        val requestBody = FormBody.Builder().add("orderId", order.orderId)
                .add("fulfilledStatus", order.fulfilledStatus.toString())

        val client = OkHttpClient()
        val request = Request.Builder()
                .url(editOrderURL)
                .post(requestBody.build())
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("EditOrder", body)

                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("EditOrder", "failed to do POST request to database" + editOrderURL)
                numInProgress--
            }
        })

    }

    private suspend fun getUpdateOrders(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).orderDao().getAllBySyncState(SYNC_EDIT)
    }

    private fun downloadOrders(context: Context, activity: Activity) {
        numInProgress++
        val requestBody = FormBody.Builder().add("cgaId", mCgaId)
                .build()
        val request = Request.Builder().url(listOrderURL).post(requestBody).build()
        val orderDao = AppDatabase.getDatabase(context).orderDao()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                var orders = listOf<Order>()
                val body = response?.body()?.string()
                val gson = GsonBuilder().create()

                try {
                    orders = gson.fromJson(body, object : TypeToken<List<Order>>() {}.type)
                } catch (e: Exception) {
                    Log.d("OrderSync", "Caught exception")
                    activity.runOnUiThread {
                        Toast.makeText(context,"Please try again later. There may be unexpected behavior until a sync is complete.", Toast.LENGTH_LONG).show()
                    }
                }

                orderDao.deleteAll()
                for (order in orders) {
                    getItemsForOrder(order, context)
                }
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request")
                Log.e(TAG, "Failed to execute GET request to " + listOrderURL)
                numInProgress--
            }
        })
    }

    fun updateOrder(context : Context, order: Order) {
        job = Job()
        order.synced = SYNC_EDIT

        launch {
            updateOrderHelper(context, order)
        }

    }

    private suspend fun updateOrderHelper(context : Context, order : Order) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).orderDao().update(order)
    }

    private fun getItemsForOrder(order : Order, context: Context) {
        numInProgress++
        val requestBody = FormBody.Builder()
                .add("orderId",order.orderId)
                .build()
        val request = Request.Builder().url(getItemURL).post(requestBody).build()
        val productDao = AppDatabase.getDatabase(context).productDao()
        val orderDao = AppDatabase.getDatabase(context).orderDao()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()

                //Log.d("ITEMS", body)
                val gson = GsonBuilder().create()
                val products : MutableList<Product> = gson.fromJson(body,  object : TypeToken<List<Product>>() {}.type)
                for (product in products) {
                    product.pictureURLs = Array(PictureListTypeConverter.NUM_PICS, { i -> "undefined"})
                    product.pictureURLs[0] = product.pic0URL
                    product.pictureURLs[1] = product.pic1URL
                    product.pictureURLs[2] = product.pic2URL
                    product.pictureURLs[3] = product.pic3URL
                    product.pictureURLs[4] = product.pic4URL
                    product.pictureURLs[5] = product.pic5URL
                }
                order.products = products
                orderDao.insert(order)
                numInProgress--
            }

            override fun onFailure(call: Call?, e: IOException?) {
                //println("Failed to execute request")
                Log.d("ERROR", "Failed to execute GET request to " + getItemURL)
                numInProgress--
            }
        })
    }

    private suspend fun getUpdatedOrders(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).orderDao().getAllBySyncState(SYNC_EDIT)
    }
}