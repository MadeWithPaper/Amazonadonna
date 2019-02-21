package com.amazonadonna.sync

import android.content.Context
import com.amazonadonna.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
        launch {
            AppDatabase.getDatabase(context).clearAllTables()
        }
    }
}