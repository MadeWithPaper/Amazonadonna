package com.amazonadonna.sync

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope

class TestingArtisanSync: Synchronizer(), CoroutineScope {
    override fun sync(context: Context, activity: Activity, cgaId: String) {
        super.sync(context, activity, cgaId)

        Log.i("ArtisanSync", "Syncing Artisans done")
    }

    override fun syncArtisanMode(context: Context, activity: Activity, artisanId: String) {
        super.syncArtisanMode(context, activity, artisanId)

        Log.i("ArtisanSync", "Syncing Artisans for Artisan mode done")
    }
}