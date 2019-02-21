package com.amazonadonna.sync

import android.content.Context
import com.amazonadonna.database.AppDatabase
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class Syncronizer : CoroutineScope {
    companion object {
        const val SYNC_NEW = 1
        const val SYNC_EDIT = 2
        const val SYNCED = 0
    }

    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    open fun sync(context: Context) {
        job = Job()
    }

    fun resetLocalDB(context: Context) {
        job = Job()
        launch {
            resetLocalDBHelper(context)
        }
    }

    private suspend fun resetLocalDBHelper(context: Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).clearAllTables()
    }
}