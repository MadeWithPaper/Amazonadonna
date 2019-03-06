package com.amazonadonna.sync

import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.database.PictureListTypeConverter
import com.amazonadonna.model.Order
import com.amazonadonna.model.Product
import com.amazonadonna.view.ListItemsAdapter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_order_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException

object OrderSync: Synchronizer(), CoroutineScope {
    private const val TAG = "OrderSync"
    private const val listOrderURL = "https://99956e2a.ngrok.io/order/listAllForCgo"
    private const val getItemURL = "https://99956e2a.ngrok.io/order/getItems"

    override fun sync(context: Context, cgaId: String) {
        super.sync(context, cgaId)

        Log.i(TAG, "Syncing now!")
        updateOrders(context)
        Log.i(TAG, "Done uploading, now downloading")
        downloadOrders(context)
        Log.i(TAG, "Done syncing!")

    }

    private fun updateOrders(context: Context) {

    }

    private fun downloadOrders(context: Context) {
        val requestBody = FormBody.Builder().add("cgoId", mCgaId)
                .build()
        val request = Request.Builder().url(listOrderURL).post(requestBody).build()
        val orderDao = AppDatabase.getDatabase(context).orderDao()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                val gson = GsonBuilder().create()
                val orders : List<Order> = gson.fromJson(body,  object : TypeToken<List<Order>>() {}.type)

                orderDao.deleteAll()
                for (order in orders) {
                    getItemsForOrder(order, context)
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request")
                Log.e(TAG, "Failed to execute GET request to " + listOrderURL)
            }
        })
    }

    private fun getItemsForOrder(order : Order, context: Context) {
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
                val products : List<Product> = gson.fromJson(body,  object : TypeToken<List<Product>>() {}.type)
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
            }

            override fun onFailure(call: Call?, e: IOException?) {
                //println("Failed to execute request")
                Log.d("ERROR", "Failed to execute GET request to " + getItemURL)
            }
        })
    }

    private suspend fun getUpdatedOrders(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).orderDao().getAllBySyncState(SYNC_EDIT)
    }
}