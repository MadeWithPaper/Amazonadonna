package com.amazonadonna.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.list_artisan_cell.view.*
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.squareup.picasso.Callback
import java.net.URL
import com.amazonadonna.database.ImageStorageProvider
import android.support.v7.widget.SearchView


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
            intent.putExtra("artisan", artisan)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

}


class ArtisanViewHolder (val view : View) : RecyclerView.ViewHolder(view) {

    fun bindArtisian(artisan: Artisan) {
        Log.d("URL:::::", artisan.picURL)
       // view.imageView_artisanProfilePic.setImageResource(R.drawable.placeholder)
        var isp = ImageStorageProvider(view.context)

        if (artisan.picURL != "Not set" && artisan.picURL != null) {
            var url = artisan.picURL!!
            var fileName = ImageStorageProvider.ARTISAN_IMAGE_PREFIX +
                    url.substring(url.lastIndexOf('/') + 1, url?.length)

            if (!isp.imageExists(fileName!!)) {
                Picasso.with(view.context).load(artisan.picURL).into(
                        view.imageView_artisanProfilePic,
                        object: com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                var drawable = view.imageView_artisanProfilePic.drawable as BitmapDrawable
                                isp.saveBitmap(drawable.bitmap, fileName)
                            }
                            override fun onError() {

                            }
                        })
            }
            else {
                view.imageView_artisanProfilePic.setImageBitmap(isp.loadBitmap(fileName))
            }
            //Picasso.with(view.context).load(artisan.picURL).into(view.imageView_artisanProfilePic)
            //DownLoadImageTask(view.imageView_artisanProfilePic).execute(artisan.picURL)
        }
        else
            view.imageView_artisanProfilePic.setImageResource(R.drawable.placeholder)

        view.textView_artisanName.text = artisan.name
        //view.textView_bio.text = artisan.bio
        view.textView_artisanLoc.text = (artisan.city + "," + artisan.country)
    }

//    private class DownLoadImageTask(internal val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
//        override fun doInBackground(vararg urls: String): Bitmap? {
//            val urlOfImage = urls[0]
//            return try {
//                val inputStream = URL(urlOfImage).openStream()
//                BitmapFactory.decodeStream(inputStream)
//            } catch (e: Exception) { // Catch the download exception
//                e.printStackTrace()
//                null
//            }
//        }
//        override fun onPostExecute(result: Bitmap?) {
//            if(result!=null){
//                // Display the downloaded image into image view
//                Toast.makeText(imageView.context,"download success",Toast.LENGTH_SHORT).show()
//                imageView.setImageBitmap(result)
//            }else{
//                Toast.makeText(imageView.context,"Error downloading",Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

}   
