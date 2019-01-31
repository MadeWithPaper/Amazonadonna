package com.amazonadonna.view

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.list_artisan_cell.view.*
import android.os.Bundle



class ListArtisanAdapter (private val context: Context, private val artisans : List<Artisan>) : RecyclerView.Adapter<ArtisanViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtisanViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.list_artisan_cell, parent, false)
        return ArtisanViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return artisans.count()
    }

    override fun onBindViewHolder(holder: ArtisanViewHolder, position: Int) {
        val artisan = artisans.get(position)
        holder.bindArtisian(artisan)

        holder.view.setOnClickListener{
            val intent = Intent(context, ArtisanProfile::class.java)
            val artisanBundle = Bundle()
            intent.putExtra("artisan", artisan)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

}


class ArtisanViewHolder (val view : View) : RecyclerView.ViewHolder(view) {

    fun bindArtisian(artisan: Artisan) {
        view.imageView_artisanProfilePic.setImageResource(R.drawable.placeholder)
        view.textView_artisanName.text = artisan.name
        //view.textView_bio.text = artisan.bio
        view.textView_artisanLoc.text = (artisan.city + "," + artisan.country)
    }
}