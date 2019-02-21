package com.amazonadonna.view

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import com.amazonadonna.model.Artisan
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_artisan_profile.*
import android.text.method.ScrollingMovementMethod
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.sync.ArtisanSync
import com.amazonadonna.view.R
import kotlinx.android.synthetic.main.list_artisan_cell.view.*


class ArtisanProfile() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_profile)
        //ArtisanSync.sync(this)
        val artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanProfileBio.setMovementMethod(ScrollingMovementMethod())

        populateSelectedArtisan(artisan)

        artisanProfileItemListButton.setOnClickListener {
            artisanItemList(artisan)
        }

        artisanProfilePayoutButton.setOnClickListener {
            artisanPayout(artisan)
        }

        artisanProfile_edit.setOnClickListener {
            editArtisan(artisan)
        }
    }

    private fun populateSelectedArtisan(artisan : Artisan) {
        /*if (artisan.picURL != "Not set")
            Picasso.with(this).load(artisan.picURL).into(this.artisanProfilePicture)
        //DownLoadImageTask(view.imageView_artisanProfilePic).execute(artisan.picURL)
        else
            this.artisanProfilePicture.setImageResource(R.drawable.placeholder)*/
        var isp = ImageStorageProvider(applicationContext)


        if (artisan.picURL != "Not set" && artisan.picURL != null) {
            var url = artisan.picURL!!

            // If image is already on S3
            if (url.substring(0, 5) == "https") {
                var fileName = ImageStorageProvider.ARTISAN_IMAGE_PREFIX +
                        url.substring(url.lastIndexOf('/') + 1, url?.length)

                if (!isp.imageExists(fileName!!)) {
                    var draw = this.artisanProfilePicture.drawable
                    Picasso.with(applicationContext).load(artisan.picURL).into(
                            this.artisanProfilePicture,
                            object : com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    var drawable = draw as BitmapDrawable
                                    isp.saveBitmap(drawable.bitmap, fileName)
                                }

                                override fun onError() {

                                }
                            })
                } else {
                    this.artisanProfilePicture.setImageBitmap(isp.loadBitmap(fileName))
                }
            }
            else {
                var fileName = ImageStorageProvider.ARTISAN_IMAGE_PREFIX + url
                this.artisanProfilePicture.setImageBitmap(isp.loadBitmap(fileName))
            }
        }
        else
            this.artisanProfilePicture.setImageResource(R.drawable.placeholder)

        artisanProfileName.text = artisan.artisanName
        artisanProfileBio.text = artisan.bio

    }

    private fun artisanItemList(artisan : Artisan){
        val intent = Intent(this, ArtisanItemList::class.java)
        intent.putExtra("selectedArtisan", artisan)
        startActivity(intent)
        finish()
    }

    private fun artisanPayout(artisan: Artisan){
        val intent = Intent(this, ArtisanPayout::class.java)
        intent.putExtra("artisan", artisan)
        startActivity(intent)
        finish()
    }

    private fun editArtisan(artisan: Artisan) {
        val intent = Intent(this, EditArtisan::class.java)
        intent.putExtra("artisan", artisan)
        startActivity(intent)
        finish()
    }
    //TODO rating system
}
