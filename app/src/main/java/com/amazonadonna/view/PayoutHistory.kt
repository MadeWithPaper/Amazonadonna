package com.amazonadonna.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.App
import com.amazonadonna.model.Payout
import kotlinx.android.synthetic.main.activity_payout_history.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class PayoutHistory : AppCompatActivity(), CoroutineScope {
    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val payoutHistoryList : MutableList<Payout> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        setContentView(R.layout.activity_payout_history)

        launch {
            val payouts = fetchHistory()
            payoutHistoryList.addAll(payouts)

            runOnUiThread {
                payoutHistoryRV.layoutManager = LinearLayoutManager(applicationContext)
                payoutHistoryRV.adapter = PayoutHistoryAdapter(applicationContext, payoutHistoryList)
                payoutHistoryRV.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
            }
        }

        setSupportActionBar(payoutHistoryToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private suspend fun fetchHistory() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(application).payoutDao().getAllByArtisanId(App.currentArtisan.artisanId) as List<Payout>
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
