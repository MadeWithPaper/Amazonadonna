package com.amazonadonna.sync

import android.content.Context
import android.util.Log
import com.amazonadonna.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProductSync: Synchronizer(), CoroutineScope {
    private const val TAG = "ProductSync"

    override fun sync(context: Context, cgaId: String) {
        super.sync(context, cgaId)

        Log.i(TAG, "Syncing now!")
        uploadProducts(context)
        Log.i(TAG, "Done uploading, now downloading")
        downloadProducts(context)
        Log.i(TAG, "Done syncing!")

    }

    private fun uploadProducts(context: Context) {

    }

    private fun downloadProducts(context: Context) {

    }

    private suspend fun getUpdatedProducts(context : Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).productDao().getAllBySyncState(SYNC_EDIT)
    }
}