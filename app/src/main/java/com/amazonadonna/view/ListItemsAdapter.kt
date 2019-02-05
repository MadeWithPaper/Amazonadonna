package com.amazonadonna.view

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazonadonna.model.Product
import android.os.Bundle

class ListItemsAdapter (private val context: Context, private val products : List<Product>) : RecyclerView.Adapter<ItemsViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.list_orders_cell, parent, false)
        return ItemsViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return products.count()
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        val product = products.get(position)
        holder.bindOrder(product)

        holder.view.setOnClickListener{
            val intent = Intent(context, OrderScreen::class.java)
            intent.putExtra("product", product)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

}

class ItemsViewHolder (val view : View) : RecyclerView.ViewHolder(view) {
    //TODO fill in cell info from the passes in order
    fun bindOrder(product: Product) {

    }
}