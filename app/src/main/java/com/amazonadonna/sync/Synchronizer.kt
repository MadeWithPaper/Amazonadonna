package com.amazonadonna.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.amazonadonna.database.AppDatabase
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class Synchronizer : CoroutineScope {
    companion object {
        const val SYNC_NEW = 1
        const val SYNC_EDIT = 2
        const val SYNCED = 0
    }

    lateinit var job: Job
    lateinit var mCgaId: String
    var numInProgress: Int = 0

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    open fun sync(context: Context, cgaId: String) {
        job = Job()
        mCgaId = cgaId
    }

    fun inProgress() : Boolean {
        return numInProgress != 0
    }

    fun resetLocalDB(context: Context) {
        job = Job()
        launch {
            resetLocalDBHelper(context)
        }
    }

    fun hasInternet(context: Context) : Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    private suspend fun resetLocalDBHelper(context: Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).clearAllTables()
    }
}