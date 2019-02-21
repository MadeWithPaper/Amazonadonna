package com.amazonadonna.syncadapter

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class Syncronizer : CoroutineScope {
    companion object {
        const val SYNC_NEW = 0
        const val SYNC_EDIT = 2
        const val SYNCED = 1
    }

    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    open fun sync(context: Context) {
        job = Job()
    }
}