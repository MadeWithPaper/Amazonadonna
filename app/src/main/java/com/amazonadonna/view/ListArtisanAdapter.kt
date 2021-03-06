package com.amazonadonna.view

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.list_artisan_cell.view.*
import android.util.Log
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.App
import com.amazonadonna.sync.ArtisanSync
import com.amazonadonna.sync.Synchronizer
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.regions.Regions

class ListArtisanAdapter (private val context: Context, private val artisans :MutableList<Artisan>) : RecyclerView.Adapter<ArtisanViewHolder> () {
    private var removedPostion = 0
    private var removedArtisan = Artisan("", "", "", "", false, "", "", "", "", 0.0,0.0,"", Synchronizer.SYNCED,0.0)
    private var userPool = CognitoUserPool(context, context.resources.getString(R.string.userPoolID),context.resources.getString(R.string.clientID), context.resources.getString(R.string.clientScret), Regions.US_EAST_2)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtisanViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.list_artisan_cell, parent, false)
        return ArtisanViewHolder(cellForRow)
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        removedPostion = viewHolder.adapterPosition
        removedArtisan = artisans[viewHolder.adapterPosition]

        //remove functionality
        artisans.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)
        ArtisanSync.deleteArtisan(context, removedArtisan)


        val removeHandler = object : GenericHandler {
            override fun onSuccess() {
                // Delete was successful!
                Log.d("ListArtisan", "successfully removed artisan")
            }

            override fun onFailure(exception: Exception) {
                // Delete failed, probe exception for details
                Log.d("ListArtisan", "failed at removing artisan")
            }
        }

        if (!removedArtisan.email.isNullOrEmpty()) {
            var user = userPool.getUser(removedArtisan.email)
            user.deleteUser(removeHandler)
        }
    }

    override fun getItemCount(): Int {
        return artisans.count()
    }

    override fun onBindViewHolder(holder: ArtisanViewHolder, position: Int) {
        val artisan = artisans.get(position)
        holder.bindArtisian(artisan, context)

        holder.view.setOnClickListener{
            val intent = Intent(context, ArtisanProfileCGA::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.currentArtisan = artisan
            Log.i("ListArtisanAdapter.kt", "new artisan selected, updating global artisan")
            context.startActivity(intent)
        }
    }

}

class ArtisanViewHolder (val view : View) : RecyclerView.ViewHolder(view) {

    fun bindArtisian(artisan: Artisan, context: Context) {
        Log.d("URL:::::", artisan.picURL)

        var isp = ImageStorageProvider(context)
        isp.loadImageIntoUI(artisan.picURL, view.imageView_artisanProfilePic, ImageStorageProvider.ARTISAN_IMAGE_PREFIX, view.context)

        view.textView_artisanName.text = artisan.artisanName
        view.textView_artisanLoc.text = (artisan.city + "," + artisan.country)
    }
}   
