package com.amazonadonna.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.widget.Button
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_order_screen.*

class OrderScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_screen)

        //TODO fill in editOrder button onClick listener
        val editOrder: Button = findViewById(R.id.orderScreen_editOrder)

        // TODO implement a fetch of order data once backend route/database are configured
        orderScreen_recyclerView.layoutManager = LinearLayoutManager(this)
        //TODO replace with list of orders
        //load an empty list as placeholder before GET request completes
        val emptyArtisanList : List<Artisan> = emptyList()
        orderScreen_recyclerView.adapter = ListArtisanAdapter(this, emptyArtisanList)
        //TODO make order screen adapter
        orderScreen_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
}