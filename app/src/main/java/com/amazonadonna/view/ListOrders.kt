package com.amazonadonna.view

import android.support.v7.app.AppCompatActivity
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.database.Cursor
import android.os.Bundle
import android.support.v4.content.Loader
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_list_orders.*

class ListOrders : AppCompatActivity(), LoaderCallbacks<Cursor> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_orders)

        recyclerView_listOrders.layoutManager = LinearLayoutManager(this)

        // TODO change List<Artisan> to List<Orders> once a model class is created
        //load an empty list as placeholder before GET request completes
        val emptyOrdersList : List<Artisan> = emptyList()
        recyclerView_listOrders.adapter = ListArtisanAdapter(this, emptyOrdersList)
        // TODO implement a fetch of order data once backend route/database are configured
        // TODO implement a ListOrders adapter
        recyclerView_listOrders.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
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
