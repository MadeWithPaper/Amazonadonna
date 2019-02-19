package com.amazonadonna.view

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.github.gcacace.signaturepad.views.SignaturePad
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Environment
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_payout_signature.*
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.amazonadonna.model.Artisan
import okhttp3.*
import java.io.OutputStreamWriter


class PayoutSignature : AppCompatActivity() {

    private val REQUEST_EXTERNAL_STORAGE = 3
    private val updateURL = "https://7bd92aed.ngrok.io/artisan/edit"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payout_signature)
        val artisan = intent.extras?.getSerializable("artisan") as Artisan
        val amount = intent.getDoubleExtra("payoutAmount", 0.0)
        Log.i("Payoutsignature", "Artisan original balance: ${artisan.balance}")
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
//        val signatureBitmap = signature_pad.signatureBitmap
//        if (addJpgSignatureToGallery(signatureBitmap)) {
//            Toast.makeText(this, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
//        }
//        if (addSvgSignatureToGallery(signature_pad.signatureSvg)) {
//            Toast.makeText(this, "SVG Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Unable to store the SVG signature", Toast.LENGTH_SHORT).show();
//        }
        updateArtisanBalance(artisan, amount)
        //showResponseDialog(artisan, true)
    }

    private fun updateArtisanBalance(artisan: Artisan, amount: Double) {
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
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("PayoutSignature", "failed to do POST request to database" + updateURL)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if (grantResults.isEmpty()
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Cannot write images to external storage", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getAlbumStorageDir(albumName: String): File {
        // Get the directory for the user's public pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName)
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created")
        }
        return file
    }

    @Throws(IOException::class)
    private fun saveBitmapToJPG(bitmap: Bitmap, photo: File) {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        val stream = FileOutputStream(photo)
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        stream.close()
    }

    private fun addJpgSignatureToGallery(signature: Bitmap): Boolean {
        var result = false
        try {
            val photo = File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()))
            saveBitmapToJPG(signature, photo)
            scanMediaFile(photo)
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    private fun scanMediaFile(photo: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(photo)
        mediaScanIntent.data = contentUri
        this@PayoutSignature.sendBroadcast(mediaScanIntent)
    }
}
