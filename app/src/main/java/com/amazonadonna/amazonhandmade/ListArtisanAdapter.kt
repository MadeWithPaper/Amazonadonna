package com.amazonadonna.amazonhandmade

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import Artisan
import kotlinx.android.synthetic.main.list_artisan_cell.view.*

class ListArtisanAdapter (private val artisans : ArrayList<Artisan>) : RecyclerView.Adapter<ArtisanViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtisanViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.list_artisan_cell, parent, false)
        return ArtisanViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return artisans.size
    }

    override fun onBindViewHolder(holder: ArtisanViewHolder, position: Int) {
        val artisan = artisans.get(position)
        holder.bindArtisian(artisan)
    }
}



class ArtisanViewHolder (val view : View) : RecyclerView.ViewHolder(view) {

    fun bindArtisian(artisan: Artisan) {
        view.textView_artisanName.text = artisan.name
        view.textView_artisanBio.text = "this is a testing bio this is a testing bio this is a testing bio this is a testing bio  his is a testing " +
                "bio this is a testing bio this is a testing bio this is a testing bio  his is a tehis is a testing bio this is a testing bio this is a testing bio this " +
                "is a testing bio  his is a tehis is a testing bio this is a testing bio this is a testing bio this is a testing bio\"\n"
        view.textView_artisanCity.text = "Mexico City"
    }
}