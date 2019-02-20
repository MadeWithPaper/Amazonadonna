package com.amazonadonna.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.github.gcacace.signaturepad.views.SignaturePad
import android.content.pm.ActivityInfo
import kotlinx.android.synthetic.main.activity_payout_signature.*
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.amazonadonna.model.Artisan
import okhttp3.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class PayoutSignature : AppCompatActivity() {

    private val REQUEST_EXTERNAL_STORAGE = 3
    private val updateURL = "https://7bd92aed.ngrok.io/artisan/edit"
    private val payoutSignatureURL = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payout_signature)
        val artisan = intent.extras?.getSerializable("artisan") as Artisan
        val amount = intent.getDoubleExtra("payoutAmount", 0.0)
        Log.i("PayoutSignature", "Artisan original balance: ${artisan.balance}")
        Log.i("PayoutSignature", "Processing payout amount of : $amount")

        clearSignature_Button.isEnabled = false
        doneSignature_button.isEnabled = false
        //change screen orientation to landscape mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        signature_pad.setOnSignedListener(object : SignaturePad.OnSignedListener {

            override fun onStartSigning() {
                //Event triggered when the pad is touched
            }

            override fun onSigned() {
                //Event triggered when the pad is signed
                clearSignature_Button.isEnabled = true
                doneSignature_button.isEnabled = true
            }

            override fun onClear() {
                //Event triggered when the pad is cleared
                clearSignature_Button.isEnabled = false
                doneSignature_button.isEnabled = false
            }
        })

        clearSignature_Button.setOnClickListener {
            clearSignature()
        }

        doneSignature_button.setOnClickListener {
            saveSignature(artisan, amount)
        }
    }

    private fun clearSignature() {
        signature_pad.clear()
    }

    private fun saveSignature(artisan: Artisan, amount : Double) {
        val signatureFilePath = saveSignatureToCache(artisan, amount)
        updateArtisanBalance(artisan, amount, signatureFilePath)
        //showResponseDialog(artisan, true)
    }

    private fun saveSignatureToCache(artisan: Artisan, amount: Double) : String {
        val fileName = getUniqueFileName(artisan, amount)
            val f = Uri.parse(fileName)?.lastPathSegment?.let { filename ->
            File.createTempFile(filename, null, this.cacheDir)
            }
            Log.d("PayoutSignature", "file created: " + f!!.absolutePath + " : " + f.exists())
        return f.absolutePath
    }

    private fun getUniqueFileName(artisan: Artisan, amount: Double) : String {
        val dateFormat = SimpleDateFormat("yyy_mm_dd_HHmmss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date) + "_" + artisan.artisanId + amount
    }

    private fun updateArtisanBalance(artisan: Artisan, amount: Double, signatureFilePath : String) {
        Log.i("PayoutSignature", "updating Artisan with new balance of: " + (artisan.balance - amount).toString())
        val requestBody = FormBody.Builder().add("artisanId", artisan.artisanId)
                //.add("cgoId", artisan.cgoId)
                .add("balance", (artisan.balance - amount).toString())

        val client = OkHttpClient()
        val request = Request.Builder()
                .url(updateURL)
                .post(requestBody.build())
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.i("PayoutSignature", body)
                //balance updated now send signature
                submitSignatureToDB(artisan, signatureFilePath)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("PayoutSignature", "failed to do POST request to database $updateURL")
            }
        })

        showResponseDialog(artisan, true)
    }

    private fun showResponseDialog(artisan: Artisan, status: Boolean) {
        val builder = AlertDialog.Builder(this@PayoutSignature)
        if (status) {
            builder.setTitle("Payout Status")
            builder.setMessage("Payout Approved!")
            builder.setOnDismissListener {
                //submitDismiss(artisan)
                val intent = Intent(this, ArtisanProfile::class.java)
                intent.putExtra("artisan", artisan)
                startActivity(intent)
                finish()
            }
        } else
        {
            builder.setTitle("Payout Status")
            builder.setMessage("Payout Failed!")
            builder.setOnDismissListener {
                //submitDismiss(artisan)
            }
        }

        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            REQUEST_EXTERNAL_STORAGE -> {
//                if (grantResults.isEmpty()
//                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "Cannot write images to external storage", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    private fun getAlbumStorageDir(albumName: String): File {
//        // Get the directory for the user's public pictures directory.
//        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName)
//        if (!file.mkdirs()) {
//            Log.e("SignaturePad", "Directory not created")
//        }
//        return file
//    }

    private fun submitSignatureToDB(artisan: Artisan, signatureFilePath: String) {
        val signatureFile = File(signatureFilePath)
        Log.d("PayoutSignature", "submitSignatureToDB file" + signatureFile + " : " + signatureFile.exists())

        val MEDIA_TYPE = MediaType.parse("image/png")

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("artisanId", artisan.artisanId)
                .addFormDataPart("image", "payout.png", RequestBody.create(MEDIA_TYPE, signatureFile))
                .build()

        val request = Request.Builder()
                .url(payoutSignatureURL)
                .post(requestBody)
                .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                Log.d("PayoutSignature", body)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("PayoutSignature", "failed to do POST request to database $payoutSignatureURL")
            }
        })

        //TODO remove cache file after upload
        this.deleteFile(signatureFilePath)
    }
}
