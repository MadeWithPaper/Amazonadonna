package com.amazonadonna.view

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazonadonna.model.Order
import kotlinx.android.synthetic.main.list_orders_cell.view.*

class ListOrdersAdapter (private val context: Context, private val orders : List<Order>) : RecyclerView.Adapter<OrdersViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.list_orders_cell, parent, false)
        return OrdersViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return orders.count()
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = orders.get(position)
        holder.bindOrder(order)

        holder.view.setOnClickListener{
            val intent = Intent(context, OrderScreen::class.java)
            intent.putExtra("order", order)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

}

class OrdersViewHolder (val view : View) : RecyclerView.ViewHolder(view) {
    //TODO fill in cell info from the passes in order
    fun bindOrder(order: Order) {
//        view.textView_OrderID_input.text = order.amOrderNumber
//        view.textView_OrderDate.text = order.orderDate
        view.textView_OrderDate.text = "1/23/19"
        view.textView_OrderID_input.text = order.orderId
        view.textView_Fulfilled_input.text = order.fulfilledStatus.toString()
        view.textView_Payout_input.text = "$ ${order.totalCostDollars}.${order.totalCostCents}"
    }
}