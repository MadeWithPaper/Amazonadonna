package com.amazonadonna.view

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazonadonna.model.Product
import android.os.Bundle
import android.view.Menu
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.list_item_cell.view.*
import android.support.v7.app.AppCompatActivity

class ListItemsAdapter (private val context: Context, private val products : List<Product>, private val artisan : Artisan? = Artisan("artisanName", "id", "city", "country", "bio", "cogid", 0.0, 0.0, "url", 0.0) ) : RecyclerView.Adapter<ItemsViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.list_item_cell, parent, false)
        return ItemsViewHolder(cellForRow)
    }


    override fun getItemCount(): Int {
        return products.count()
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        val product = products.get(position)
        holder.bindOrder(product)

        holder.view.setOnClickListener{
            val intent = Intent(context, ProductDetails::class.java)
            intent.putExtra("product", product)
            intent.putExtra("selectedArtisan", artisan)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

class ItemsViewHolder (val view : View) : RecyclerView.ViewHolder(view) {
    //TODO fill in cell info from the passes in order
    fun bindOrder(product: Product) {
        view.itemCellName.text = product.itemName
        view.itemCellPrice.text = product.price.toString()
        view.itemCellQuantity.text = product.itemQuantity.toString()
    }
}