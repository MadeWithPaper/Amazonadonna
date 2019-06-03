package com.amazonadonna.view

import android.content.Context
import android.content.Intent
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.Product
import com.amazonadonna.model.Artisan
import com.amazonadonna.sync.ProductSync
import kotlinx.android.synthetic.main.list_item_cell.view.*
import com.amazonadonna.sync.Synchronizer
import kotlinx.android.synthetic.main.list_artisan_cell.view.*

class ListItemsAdapter (private val context: Context, private val products : MutableList<Product>, private var fromOrderScreen : Boolean = false) : RecyclerView.Adapter<ItemsViewHolder> () {
    private var removedPostion = 0
    private var removedProduct = Product(0.0, "", "", "", emptyArray(), "", "", "","","", "", "", "", "", "", "", 0, Synchronizer.SYNCED, 0)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.list_item_cell, parent, false)
        return ItemsViewHolder(cellForRow)
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        removedPostion = viewHolder.adapterPosition
        removedProduct = products[viewHolder.adapterPosition]

        //remove functionality
        products.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)
        ProductSync.deleteProduct(context, removedProduct)
        //undo functionality
//        Snackbar.make(viewHolder.itemView, "${removedArtisan.artisanName} deleted.", Snackbar.LENGTH_INDEFINITE).setAction("UNDO") {
//            products.add(removedPostion, removedArtisan)
//            notifyItemInserted(removedPostion)
//        }.show()
    }

    override fun getItemCount(): Int {
        return products.count()
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        val product = products.get(position)
        holder.bindOrder(product, context)

        holder.view.setOnClickListener{
            val intent = Intent(context, ProductDetails::class.java)
            intent.putExtra("product", product)
            intent.putExtra("fromOrderScreen", fromOrderScreen)
            //intent.putExtra("selectedArtisan", artisan)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

class ItemsViewHolder (val view : View) : RecyclerView.ViewHolder(view) {
    //TODO fill in cell info from the passes in order
    fun bindOrder(product: Product, context: Context) {
        var isp = ImageStorageProvider(context)
        Log.i("ListItemAdapter", product.pictureURLs[0])
        isp.loadImageIntoUI(product.pictureURLs[0], view.itemCellPicture, ImageStorageProvider.ITEM_IMAGE_PREFIX, view.context)

        view.itemCellName.text = product.itemName
        view.itemCellPrice.text = "$ ${product.price}"
        view.itemCellQuantity.text = product.itemQuantity.toString()
    }
}