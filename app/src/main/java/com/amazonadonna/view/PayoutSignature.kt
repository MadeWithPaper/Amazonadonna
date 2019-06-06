package com.amazonadonna.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.gcacace.signaturepad.views.SignaturePad
import android.content.pm.ActivityInfo
import kotlinx.android.synthetic.main.activity_payout_signature.*
import android.util.Log
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AlertDialog
import com.amazonadonna.model.App
import com.amazonadonna.model.Payout
import com.amazonadonna.sync.PayoutSync
import com.amazonadonna.sync.Synchronizer.Companion.SYNC_NEW
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class PayoutSignature : AppCompatActivity() {

    private var amount = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payout_signature)
        amount = intent.getDoubleExtra("payoutAmount", 0.0)
        Log.i("PayoutSignature", "Artisan original balance: ${App.currentArtisan.balance}")
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
            saveSignature()
        }
    }

    private fun clearSignature() {
        signature_pad.clear()
    }

    private fun saveSignature() {
        val signatureFilePath = saveSignatureToCache()
        var payout = Payout("", amount, System.currentTimeMillis(), App.currentArtisan.artisanId, SYNC_NEW, "Not set", App.currentArtisan.cgaId)
        PayoutSync.addPayout(applicationContext, payout, App.currentArtisan, File(signatureFilePath))
        runOnUiThread{
            showResponseDialog(true)
        }
    }

    private fun saveSignatureToCache() : String {
        val fileName = getCurrentDate()
        val file = File(externalCacheDir, fileName)
        file.createNewFile()

        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(file)
            signature_pad.signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            Log.d("PayoutSignature", "file created: " + fileName + " : " + file.exists())
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileOutputStream?.close()
        }

        return file.path
    }

    private fun getCurrentDate() : String {
        val dateFormat = SimpleDateFormat("yyy_mm_dd_HHmmss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }


    private fun showResponseDialog(status: Boolean) {
        val builder = AlertDialog.Builder(this@PayoutSignature)
        if (status) {
            builder.setTitle("Payout Approved!")
            builder.setMessage("Current Artisan Balance: $ ${App.currentArtisan.balance}")
            builder.setOnDismissListener {
                Log.i("PayoutSignature.kt", "before payout dismiss ${App.currentArtisan.balance}")
                submitDismiss()
            }
        } else {
            builder.setTitle("Payout Failed!")
            builder.setMessage("Current Artisan Balance: $ ${App.currentArtisan.balance}")
            builder.setOnDismissListener {
                //do nothing
            }
        }

        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

    private fun submitDismiss() {
        val intent = Intent(this, ArtisanProfileCGA::class.java)
        startActivity(intent)
        finish()
    }
}
