package com.amazonadonna.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazonadonna.model.App
import com.amazonadonna.model.Payout
import kotlinx.android.synthetic.main.payout_history_cell.view.*
import java.text.SimpleDateFormat
import java.util.*

class PayoutHistoryAdapter (private val context: Context, private val recalls : List<Payout>) : RecyclerView.Adapter<PayoutHistoryViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayoutHistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.payout_history_cell, parent, false)
        return PayoutHistoryViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return recalls.count()
    }

    override fun onBindViewHolder(holder: PayoutHistoryViewHolder, position: Int) {
        val recall = recalls[position]
        holder.bindRecall(recall)
    }
}

class PayoutHistoryViewHolder (val view : View) : RecyclerView.ViewHolder(view) {

    fun bindRecall(payout: Payout) {
        val date = Date(payout.date)
        val format = SimpleDateFormat("MM/dd/yyyy")

        view.payoutHistory_amount.text = "$${payout.amount}"
        view.payoutHistory_date.text = format.format(date)

        //change amount text color
        if (App.artisanMode && payout.amount > 0) {
            view.payoutHistory_amount.setTextColor(Color.GREEN)
        } else if (App.artisanMode && payout.amount < 0) {
            view.payoutHistory_amount.setTextColor(Color.RED)
        } else if (!App.artisanMode && payout.amount > 0) {
            view.payoutHistory_amount.setTextColor(Color.RED)
        } else {
            view.payoutHistory_amount.setTextColor(Color.GREEN)
        }
    }
}