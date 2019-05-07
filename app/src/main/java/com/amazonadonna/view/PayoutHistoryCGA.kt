package com.amazonadonna.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonadonna.model.App
import com.amazonadonna.model.Payout
import kotlinx.android.synthetic.main.activity_payout_history_cga.*

class PayoutHistoryCGA : AppCompatActivity() {

    private val payoutHistoryPath = App.BACKEND_BASE_URL + "/payout/listAllForCga"
    private val payoutHistoryList : MutableList<Payout> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payout_history_cga)

        val cgaID = intent.extras.getString("cgaID")

        fetchHistory(cgaID)

        //TODO remove test data
        payoutHistoryList.add(Payout("test", 2.4, 3, "id", 1, "qwer", "qwer"))

        payoutHistoryRV.layoutManager = LinearLayoutManager(this)
        payoutHistoryRV.adapter = PayoutHistoryAdapter(this, payoutHistoryList)
        payoutHistoryRV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun fetchHistory(cgaID: String) {
        //TODO fetch data from db
    }
}
