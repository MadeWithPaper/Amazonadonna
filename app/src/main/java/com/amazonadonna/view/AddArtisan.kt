package com.amazonadonna.view


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_artisan.*
import com.amazonadonna.model.Artisan
import android.annotation.TargetApi
import android.widget.ImageView

import android.text.TextUtils
import android.util.Log
import okhttp3.*
import android.content.Intent
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.app.Activity
import androidx.core.app.ActivityCompat
import android.provider.DocumentsContract
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import java.io.*
import java.security.SecureRandom
import android.graphics.BitmapFactory
import com.amazonadonna.sync.ArtisanSync
import com.amazonadonna.sync.Synchronizer
import android.graphics.Bitmap
import android.view.View
import android.media.ExifInterface
import android.graphics.Matrix
import android.os.Build
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.amazonadonna.model.App
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.amazonaws.regions.Regions
import de.cketti.mailto.EmailIntentBuilder
import kotlinx.android.synthetic.main.activity_login_screen.*
import kotlinx.android.synthetic.main.activity_payout_history.*
import java.io.File
import java.io.IOException

class AddArtisan : AppCompatActivity() {
    private var cgaId : String = "0"
    private var photoFile: File? = null
    private val fileName: String = "output.png"
    private val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    private val CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE = 1046
    private val addArtisanURL = App.BACKEND_BASE_URL + "/artisan/add"
    private val artisanPicURL = App.BACKEND_BASE_URL + "/artisan/updateImage"
    private var userPool = CognitoUserPool(this@AddArtisan, "us-east-2_ViMIOaCbk","4in76ncc44ufi8n1sq6m5uj7p7", "12qfl0nmg81nlft6aunvj6ec0ocejfecdau80biodpubkfuna0ee", Regions.US_EAST_2)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_artisan)

        cgaId = intent.extras.getString("cgaId")
        val PERMISSIONS = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val IMAGE_UPLOADING_PERMISSION = 3
        ActivityCompat.requestPermissions(this, PERMISSIONS, IMAGE_UPLOADING_PERMISSION)
//        val IMAGE_WRTING_PERMISSION = 4
//        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), IMAGE_WRTING_PERMISSION)

        takePicture.setOnClickListener{
            takePhoto()
        }

        selectPicture.setOnClickListener{
            selectImageInAlbum()
        }

        artisanContact_et.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        addArtisan_scrollViewContents.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                hideKeyboard(v)
                return true
            }
        })

        setSupportActionBar(addnewartisan_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun selectImageInAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val w = 331
        val h = 273
        photoFile = File(externalCacheDir, fileName)



        if(photoFile!!.exists()) {
            photoFile!!.delete()
        }
        photoFile!!.createNewFile()


        val fileProvider = FileProvider.getUriForFile(this@AddArtisan, "com.amazonadonna.amazonhandmade.fileprovider", photoFile!!)


        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
        }
    }

    private fun setImageView() {
        val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
        //Log.d("PLLLLLZZZZZZ",takenImage)
        // RESIZE BITMAP, see section below
        // Load the taken image into a preview
        val ivPreview = findViewById(R.id.imageView_artisanProfilePic) as ImageView
        ivPreview.setImageBitmap(takenImage)
    }
    fun customCompressImage(view: View) {
            // Compress image in main thread using custom Compressor
//            compressedImage = CompressorKt.create(this) {
//                maxWidth { 640f }
//                maxHeight { 480f }
//                quality { 75 }
//                compressFormat { WEBP }
//            }.compressToFile(actualImage)
//
//            Compressor.Builder(this).setMaxWidth(640f).build().compressToFile(actualImage)

    }

    @TargetApi(19)
    private fun createImageFile(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        val w = 331
        val h = 273
        if (DocumentsContract.isDocumentUri(this, uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = imagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = imagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri.scheme, ignoreCase = true)){
            imagePath = imagePath(uri, null)
        }
        else if ("file".equals(uri.scheme, ignoreCase = true)){
            imagePath = uri.path
        }

        photoFile = File(imagePath)

        //pre-scaling bits
        val uri_test = FileProvider.getUriForFile(this@AddArtisan, "com.amazonadonna.amazonhandmade.fileprovider", photoFile!!)
        val bm = loadScaledBitmap(uri_test, w, h)
        val stream = ByteArrayOutputStream()
        bm!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
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

    private fun imagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }

    //helper for preventing unwanted rotation when importing pictures
    private fun rotateBitmap(bitmap: Bitmap, orientation: Int) : Bitmap? {
        val matrix = Matrix();
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        var bmRotated : Bitmap? = null
        try {
            bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
        }
        catch (e: OutOfMemoryError) {
            e.printStackTrace();
        }
        return bmRotated
    }


    @TargetApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    photoFile = File(externalCacheDir, fileName)
                    val dataURI = FileProvider.getUriForFile(this@AddArtisan, "com.amazonadonna.amazonhandmade.fileprovider", photoFile!!)
                    val cr = contentResolver
                    val inputStream = cr.openInputStream(dataURI)
                    //Log.e("AddArtisan.kt", "testing path from onActivityResult $path")

                    try {
                        var bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, dataURI)
                        bitmap = Bitmap.createScaledBitmap(bitmap,331,273,true)
                        val ivPreview = findViewById(R.id.imageView_artisanProfilePic) as ImageView

                        val exif =  ExifInterface(inputStream)
                        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED)
                        val bmRotated = rotateBitmap(bitmap, orientation)


                        ivPreview.setImageBitmap(bmRotated)

                        val stream = ByteArrayOutputStream()
                        bmRotated!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
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
//                    val w = imageView_artisanProfilePic.width
//                    val h = imageView_artisanProfilePic.height
//                    val dataURI = FileProvider.getUriForFile(this@AddArtisan, "com.amazonadonna.amazonhandmade.fileprovider", photoFile!!)
//                        try {
//                            Log.d("Add Artisan post photo", "Success")
//                            Log.d("Add Artisan post photo", "Exists?: " + photoFile!!.exists())
//                            val bm = loadScaledBitmap(dataURI, w, h)
//                            val ivPreview = findViewById(R.id.imageView_artisanProfilePic) as ImageView
//                            ivPreview.setImageBitmap(bm)
//                            //setImageView()
//                        } catch (e: Error) {
//                            Log.d("Add Artisan post Photo", "it failed")
//                        }
                    }
            CHOOSE_PHOTO_ACTIVITY_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val w = imageView_artisanProfilePic.width
                        val h = imageView_artisanProfilePic.height
                        val dataURI = data.data
                        val cr = contentResolver
                        val inputStream = cr.openInputStream(dataURI)
                        Log.d("HEIGHT", h.toString())
                        Log.d("WIDTH", w.toString())
                        Log.d("dataURI", dataURI.toString())
                        createImageFile(data)


                        try {
                            Log.d("Add Artisan post photo", "Success")
                            Log.d("Add Artisan post photo", "Exists?: " + photoFile!!.exists())

                            var bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, dataURI)
                            bitmap = Bitmap.createScaledBitmap(bitmap,331,273,true)
                            val ivPreview = findViewById(R.id.imageView_artisanProfilePic) as ImageView

                            val exif =  ExifInterface(inputStream)
                            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED)
                            val bmRotated = rotateBitmap(bitmap, orientation)

                            ivPreview.setImageBitmap(bmRotated)
                            //setImageView()
                        }
                        catch(e: Error) {
                            Log.d("Add Artisan post Photo", "it failed")
                        }
//                        createImageFile(data)
//                        Log.d("Add Artisan postGallery", "File:  Exists?: " + photoFile!!.exists())
//                        setImageView()
                    }
                    else {
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

     fun calculateInSampleSize(options : BitmapFactory.Options,
                                      reqWidth : Int, reqHeight : Int): Int {
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



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            3 -> {
                button_addArtisan.setOnClickListener{
                    //Toast.makeText(this@AddArtisan, "add button clicked.", Toast.LENGTH_SHORT).show()
                    Log.d("testing_4",grantResults.isNotEmpty().toString())
                    Log.d("testing_5", PackageManager.PERMISSION_GRANTED.toString())
                    makeNewArtisan()
                }
            }

        }
    }

    private fun generateTempPassword() : String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        var i = 0
        val rnd = SecureRandom.getInstance("SHA1PRNG")
        val sb = StringBuilder(10)

        while (i < 10) {
            val randomInt : Int = rnd.nextInt(charPool.size)
            sb.append(charPool[randomInt])
            i++
        }

        return sb.toString()
    }

    /**
     * Amazon Cognito for Artisans
     */
    private fun signUpNewArtisan(email: String) {
        //disable touch events once log in button is clicked
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        var userAttributes = CognitoUserAttributes()
        userAttributes.addAttribute("email", email)

        var tempPassword = generateTempPassword()
        Log.d("AddArtisan", "tempPass: "+tempPassword)


        var signUpHandler = object : SignUpHandler {
            override fun onSuccess(user: CognitoUser?, signUpConfirmationState: Boolean, cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails?) {
                Log.d("AddArtisan", "in signUpHandler success")
                var success = EmailIntentBuilder.from(this@AddArtisan).to(email)
                        .subject("Invitation to join our community")
                        .body("Welcome to our community! Login to the Amazon Handmade app with " +
                                "the following credentials. \n\nusername: "+email+"\npassword: "+tempPassword)
                        .start()

                if (!success) {
                    Toast.makeText(this@AddArtisan, "Could not send email to '"+email+"'", Toast.LENGTH_SHORT)
                }
            }

            override fun onFailure(exception: Exception?) {
                Log.d("AddArtisan", "in signupHandler fail")
                Log.d("AddArtisan", exception?.message)
            }
        }

        // Sign up this user
        userPool.signUpInBackground(email, tempPassword, userAttributes, null, signUpHandler)
    }

    //TODO clean up
    private fun makeNewArtisan() {
        //validate fields
        if (!validateFields()) {
            return
        }
        val name = artisanName_et.text.toString()
        val bio = artisanBio_et.text.toString()
        val number = artisanContact_et.text.toString()
        val email = artisanEmail_et.text.toString()

        //TODO get cognito id before sending to backend
        val artisanId = ""

        val newArtisan = Artisan(name, artisanId, number, email,"", "", bio, cgaId,0.0,0.0, "Not set", Synchronizer.SYNC_NEW, 3000.00)
        newArtisan.generateTempID()
        //parse location info
        parseLoc(newArtisan)
        Log.i("AddArtisan", "created new Artisan $newArtisan")

        //pop screen and add
        //submitToDB(newArtisan)
        ArtisanSync.addArtisan(applicationContext, newArtisan, photoFile)

        if (artisanEmail_et.text.toString().length > 0) {
            signUpNewArtisan(artisanEmail_et.text.toString())
        }

        //clear all fields
        clearFields()
        super.onBackPressed()

    }

    private fun parseLoc (artisan: Artisan) {
        val rawLoc = artisanLoc_et.text.toString()

        val ind = rawLoc.indexOf(',')

        artisan.city = rawLoc.substring(0, ind)
        artisan.country = rawLoc.substring(ind+1)
    }

    private fun clearFields() {
        artisanName_et.text!!.clear()
        artisanContact_et.text!!.clear()
        artisanBio_et.text!!.clear()
        artisanLoc_et.text!!.clear()
        artisanEmail_et.text!!.clear()

        Log.i("AddArtisan", "Clearing fields")
    }

    //Validate all fields entered
    //TODO add more checks
    private fun validateFields() : Boolean {
        if (artisanName_et.text.toString().isEmpty()){
            artisanName_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (artisanLoc_et.text.toString().isEmpty()) {
            artisanLoc_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (!artisanLoc_et.text.toString().contains(",")) {
            artisanLoc_til.error = this.resources.getString(R.string.loc_missing_comma)
            return false
        }

        if (artisanContact_et.text.toString().isEmpty()){
            artisanContact_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (artisanEmail_et.text.toString().isEmpty()){
            artisanEmail_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }
        
        if (!artisanEmail_et.text.toString().contains(".")){
            artisanEmail_til.error = this.resources.getString(R.string.error_invalid_email)
            return false
        }

        if (artisanBio_et.text.toString().isEmpty()){
            artisanBio_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        return true
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}