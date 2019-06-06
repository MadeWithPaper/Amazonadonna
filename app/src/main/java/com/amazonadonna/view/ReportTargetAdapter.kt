package com.amazonadonna.view

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazonadonna.model.ReportTarget
import kotlinx.android.synthetic.main.report_target_cell.view.*

class ReportTargetAdapter (private val context: Context, private val targets : List<ReportTarget>) : RecyclerView.Adapter<TargetViewHolder> () {

    private val TAG = "ReportTargetAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TargetViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.report_target_cell, parent, false)
        return TargetViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return targets.count()
    }

    override fun onBindViewHolder(holder: TargetViewHolder, position: Int) {
        val target = targets[position]
        holder.bindOrder(target)
        holder.view.setOnClickListener{
            target.selected = !target.selected
            Log.i(TAG, "clicked ${target.selected}")
            notifyItemChanged(position)
        }
    }

}

class TargetViewHolder (val view : View) : RecyclerView.ViewHolder(view) {
    fun bindOrder(target : ReportTarget) {
        view.targetNameTV.text = target.artisan.artisanName
        view.setBackgroundColor(if (target.selected) Color.GREEN else Color.WHITE)
    }
}