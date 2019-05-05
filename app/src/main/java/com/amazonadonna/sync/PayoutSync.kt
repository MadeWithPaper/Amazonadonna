package com.amazonadonna.sync

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.database.ImageStorageProvider
import com.amazonadonna.model.App
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Payout
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.IOException

object PayoutSync : Synchronizer(), CoroutineScope {
    private val payoutHistory = App.BACKEND_BASE_URL + "/payout/add"
    private val payoutSignatureURL = App.BACKEND_BASE_URL + "/payout/updateImage"
    private const val TAG = "PayoutSync"

    override fun sync(context: Context, cgaId: String) {
        super.sync(context, cgaId)

        Log.i(TAG, "Syncing now!")
        uploadNewPayouts(context)
    }

    private fun uploadNewPayouts(context : Context) {
        launch {
            val newPayouts = getNewPayouts(context)
            for (payout in newPayouts) {
                uploadSinglePayout(context, payout)
            }
        }
    }

    fun addPayout(context : Context, payout : Payout, artisan : Artisan, photoFile: File? = null) {
        job = Job()
        stageImageUpdate(context, payout, photoFile)

        artisan.balance -= payout.amount
        ArtisanSync.updateArtisan(context, artisan)

        payout.generateTempID()

        launch {
            addPayoutHelper(context, payout)
        }

    }

    private fun stageImageUpdate(context : Context, payout : Payout, photoFile: File? = null) {
        if (photoFile != null) {
            val sourceFile = photoFile!!
            var fileName = payout.payoutId + ".png"
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
            var isp = ImageStorageProvider(context)
            isp.saveBitmap(bitmap, ImageStorageProvider.PAYOUT_IMAGE_PREFIX + fileName)
            payout.signaturePicURL = fileName
        }
    }

    private suspend fun addPayoutHelper(context : Context, payout : Payout) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).payoutDao().insert(payout)
    }

    private suspend fun getNewPayouts(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).payoutDao().getAllBySyncState(SYNC_NEW)
    }

    private suspend fun setSyncedState(payout : Payout, context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).payoutDao().setSyncedState(payout.payoutId, SYNCED)
        //AppDatabase.getDatabase(context).artisanDao().delete(artisan)
    }

    private fun uploadSinglePayout(context : Context, payout : Payout) {
        numInProgress++
        Log.i(TAG, payout.artisanId)
        val requestBody = FormBody.Builder().add("artisanId", payout.artisanId)
                .add("cgaId", payout.cgaId)
                .add("amount", payout.amount.toString())
                .add("date", payout.date.toString())

        val client = OkHttpClient()
        val request = Request.Builder()
                .url(payoutHistory)
                .post(requestBody.build())
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()!!.string()
                runBlocking {
                    setSyncedState(payout, context)
                }
                payout.payoutId = body!!.substring(1, body!!.length - 1)
                Log.i("PayoutSignature", "payoutid $body")
                //submitSignatureToDB(artisan, body, signatureFilePath, amount)
                uploadPayoutSigImage(context, payout)
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("PayoutSignature", "failed to do POST request to database $payoutHistory")
            }
        })
    }

    private fun uploadPayoutSigImage(context : Context, payout : Payout) {
        val signatureFile : File = context.getFileStreamPath(ImageStorageProvider.PAYOUT_IMAGE_PREFIX + payout.signaturePicURL)

        Log.d("PayoutSignature", "submitSignatureToDB file " + signatureFile + " : " + signatureFile.exists())

        val MEDIA_TYPE = MediaType.parse("image/png")

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("payoutId", payout.payoutId)
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
                Log.d("PayoutSignature", "signature pic success $body")
                //signatureFile.delete()
                Log.d("PayoutSignature", "signature file clean up " + signatureFile + " : " + signatureFile.exists())
                numInProgress--

            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("PayoutSignature", "failed to do POST request to database $payoutSignatureURL")
                //signatureFile.delete()
                Log.d("PayoutSignature", "signature file clean up " + signatureFile + " : " + signatureFile.exists())
                numInProgress--
            }
        })
    }
}