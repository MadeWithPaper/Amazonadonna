package com.amazonadonna.view

import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager.LoaderCallbacks
import android.database.Cursor
import android.os.Bundle
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DiffUtil
import android.util.Log
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.Order
import kotlinx.android.synthetic.main.activity_list_orders.*
import java.util.concurrent.TimeUnit
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.jakewharton.rxbinding2.widget.textChanges
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ListOrders : AppCompatActivity(), LoaderCallbacks<Cursor>, CoroutineScope {
    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    var cgaId : String = "0"
    private lateinit var artisanID : String
    val originalOrders : MutableList<Order> = mutableListOf()
    val filteredOrders: MutableList<Order> = mutableListOf()
    val oldFilteredOrders: MutableList<Order> = mutableListOf()

    fun search(query: String): Completable = Completable.create {
        val wanted = originalOrders.filter {
            it.orderId.contains(query, true) || it.shippingAddress.contains(query, true) ||
                    it.fulfilledStatus.toString().contains(query, true)
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_orders)

        cgaId = intent.extras.getString("cgaId")
        if (intent.hasExtra("artisanId")){
            artisanID = intent.extras.getString("artisanId")
        }
        recyclerView_listOrders.layoutManager = LinearLayoutManager(this)

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
        job = Job()

        launch {
            val dbOrders: List<Order> = getOrdersFromDb()
            originalOrders.addAll(dbOrders)
            oldFilteredOrders.addAll(dbOrders)
            filteredOrders.addAll(dbOrders)
            runOnUiThread {
                recyclerView_listOrders.adapter = ListOrdersAdapter(applicationContext, oldFilteredOrders)
            }
        }
    }

    private suspend fun getOrdersFromDb() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(application).orderDao().getAll() as List<Order>
    }

    override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<Cursor> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoadFinished(p0: Loader<Cursor>, p1: Cursor?) {
        //not implemented
    }

    override fun onLoaderReset(p0: Loader<Cursor>) {
        //not implemented
    }

    inner class PostsDiffUtilCallback(private val oldList: List<Order>, private val newList: List<Order>) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].orderId == newList[newItemPosition].orderId

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true // for the sake of simplicity we return true here but it can be changed to reflect a fine-grained control over which part of our views are updated
    }
}
