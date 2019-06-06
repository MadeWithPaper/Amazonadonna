package com.amazonadonna.view

import android.app.Activity
import android.content.ContextWrapper
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.telephony.PhoneNumberFormattingTextWatcher
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.amazonadonna.artisanOnlyViews.ArtisanProfile
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.App
import com.amazonadonna.sync.ArtisanSync
import kotlinx.android.synthetic.main.activity_edit_artisan.*
import java.io.*

class EditArtisan : AppCompatActivity() {

    private var photoFile: File? = null
    private val fileName: String = "editProfilePic.png"
    private val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    private val CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE = 1046

    private lateinit var pic : Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_artisan)
        val IMAGE_UPLOADING_PERMISSION = 3
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_UPLOADING_PERMISSION)

        //fill in information from old artisan
        editArtisanBio_et.setText(App.currentArtisan.bio)
        editArtisanLoc_et.setText(App.currentArtisan.city + "," + App.currentArtisan.country)
        editArtisanName_et.setText(App.currentArtisan.artisanName)
        editArtisanContact_et.setText(App.currentArtisan.phoneNumber)

        var isp = ImageStorageProvider(applicationContext)
        isp.loadImageIntoUI(App.currentArtisan.picURL, this.editArtisan_pic, ImageStorageProvider.ARTISAN_IMAGE_PREFIX, applicationContext)

        pic = editArtisan_pic.drawable

        editArisan_SaveButton.setOnClickListener {
            updateArtisan()
        }

        editSelectPicture.setOnClickListener {
            selectPicture()
        }

        editTakePicture.setOnClickListener {
            takePicture()
        }

        editArtisanContact_et.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        editArtisan_scrollViewContents.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                hideKeyboard(v)
                return true
            }
        })

        setSupportActionBar(editArtisanProfileToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun selectPicture() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        photoFile = File(externalCacheDir, fileName)

        if(photoFile!!.exists()) {
            photoFile!!.delete()
        }
        photoFile!!.createNewFile()

        val fileProvider = FileProvider.getUriForFile(this@EditArtisan, "com.amazonadonna.amazonhandmade.fileprovider", photoFile!!)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        Log.d("EditArtisan", "uri: "+fileProvider)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
        }
    }


    private fun bitmapToFile(bitmap:Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(applicationContext)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images",Context.MODE_PRIVATE)
        file = File(file,fileName)

        try{
            // Compress the bitmap and save in jpg format
            val stream:OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }

    private fun scalePhotoFile(uri: Uri,w:Int, h:Int) {
        val bm = loadScaledBitmap(uri, w, h)
        val stream = ByteArrayOutputStream()
        bm!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
        var byteArray = stream.toByteArray()

        try {
            //convert array of bytes into file
            val fileOuputStream = FileOutputStream(photoFile)
            fileOuputStream.write(byteArray)
            fileOuputStream.close()
            Log.d("SKETIT", "lol")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val w = 331
        val h = 273
        when(requestCode) {
            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    photoFile = File(externalCacheDir, fileName)
                    val dataURI = FileProvider.getUriForFile(this@EditArtisan, "com.amazonadonna.amazonhandmade.fileprovider", photoFile!!)
                    val cr = contentResolver
                    try {
                        var bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, dataURI)
                        bitmap = Bitmap.createScaledBitmap(bitmap,331,273,true)
                        val ivPreview = findViewById(R.id.editArtisan_pic) as ImageView
                        ivPreview.setImageBitmap(bitmap)

                        val stream = ByteArrayOutputStream()
                        bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        var byteArray = stream.toByteArray()
                        //byteArray = ByteArray(photoFile!!.length().toInt())

                        try {
                            //convert array of bytes into file
                            val fileOuputStream = FileOutputStream(photoFile)
                            fileOuputStream.write(byteArray)
                            fileOuputStream.close()
                            Log.d("SKETIT", "lol")

                            println("Done")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                    catch (e: Error) {
                        Log.d("Add Artisan post Photo", "it failed")
                    }
                }
            CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val dataURI = data.data

                        try {
                            val bm = loadScaledBitmap(dataURI, w, h)
                            val uri = bitmapToFile(bm!!)
                            photoFile = File(uri.path)
                            scalePhotoFile(dataURI, w, h)

                            val ivPreview = findViewById(R.id.editArtisan_pic) as ImageView
                            ivPreview.setImageBitmap(bm)
                        } catch (e: Error) {
                            Log.d("Add Artisan post Photo", "it failed")
                        }
                    } else {
                        Log.d("Add Artisan postGallery", "Data was null")
                    }
                }
        }
    }


    @Throws(FileNotFoundException::class)
    private fun loadScaledBitmap(src: Uri, req_w: Int, req_h: Int): Bitmap? {

        var bm: Bitmap? = null

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(baseContext.contentResolver.openInputStream(src), null, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, req_w, req_h)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        bm = BitmapFactory.decodeStream(
                baseContext.contentResolver.openInputStream(src), null, options)

        return bm
    }

    private fun calculateInSampleSize(options : BitmapFactory.Options, reqWidth : Int, reqHeight : Int): Int {
        // Raw height and width of image
        val height = options.outHeight;
        val width = options.outWidth;
        var inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            val heightRatio = Math.round((height.toFloat()) / Math.round(reqHeight.toFloat()))
            val widthRatio = Math.round(width.toFloat() / reqHeight.toFloat())

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            if (heightRatio < widthRatio)
                inSampleSize = heightRatio
            else
                inSampleSize = widthRatio
            // inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio
        }

        return inSampleSize
    }

    private fun parseLoc () : Pair<String, String> {
        val rawLoc = editArtisanLoc_et.text.toString()
        val ind = rawLoc.indexOf(',')
        return Pair(rawLoc.substring(0, ind), rawLoc.substring(ind+1))
    }

    private fun updateArtisan() {
        if (!validateFields()) {
            return
        } else {

            App.currentArtisan.artisanName = editArtisanName_et.text.toString()
            App.currentArtisan.bio = editArtisanBio_et.text.toString()
            App.currentArtisan.city = parseLoc().first
            App.currentArtisan.country = parseLoc().second
            App.currentArtisan.phoneNumber = editArtisanContact_et.text.toString()

            var newPhoto: File? = null
            if (editArtisan_pic.drawable != pic) {
                newPhoto = photoFile
            }

            ArtisanSync.updateArtisan(applicationContext, App.currentArtisan, newPhoto)

            var intent = Intent(this, ArtisanProfileCGA::class.java)
            if (App.artisanMode){
                intent = Intent(this, ArtisanProfile::class.java)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun isPhoneNumber(phoneNo: String): Boolean {
        //validate phone numbers of format "1234567890"
        return if (phoneNo.matches("\\d{10}".toRegex()))
            true
        else if (phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}".toRegex()))
            true
        else if (phoneNo.matches("\\d{3} \\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}".toRegex()))
            true
        else if (phoneNo.matches("\\(\\d{3}\\) \\d{3}-\\d{4}".toRegex()))
            true
        else
            false//return false if nothing matches the input
        //validating phone number where area code is in braces ()
        //validating phone number with extension length from 3 to 5
        //validating phone number with -, . or spaces

    }
    //Validate all fields entered
    private fun validateFields() : Boolean {
        if (editArtisanName_et.text.toString().isEmpty()){
            editArtisanName_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (editArtisanLoc_et.text.toString().isEmpty()) {
            editArtisanLoc_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (!editArtisanLoc_et.text.toString().contains(",")) {
            editArtisanLoc_til.error = this.resources.getString(R.string.loc_missing_comma)
            return false
        }

        if (editArtisanContact_et.text.toString().isEmpty()){
            editArtisanContact_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (isPhoneNumber(editArtisanContact_et.text.toString()) == false) {
            editArtisanContact_til.error = this.resources.getString(R.string.invalid_type_for_artisan_number)
            return false
        }

        //TODO add next sprint
//        if (artisanEmail_et.text.toString().isEmpty()){
//            artisanEmail_til.error = this.resources.getString(R.string.requiredFieldError)
//            return false
//        }

//        if (!artisanEmail_et.text.toString().contains(".")){
//            artisanEmail_til.error = this.resources.getString(R.string.error_invalid_email)
//            return false
//        }

        if (editArtisanBio_et.text.toString().isEmpty()){
            editArtisanBio_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        return true
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
