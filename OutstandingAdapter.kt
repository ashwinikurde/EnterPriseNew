package Adapers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import Model.Outsatnding
import px.corp.enterprisenew.R

class OutstandingAdapter(private var customers: List<Outsatnding>) :
    RecyclerView.Adapter<OutstandingAdapter.CustomerViewHolder>() {

    inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        val tvBalance: TextView = itemView.findViewById(R.id.tvBalance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_outstanding, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customers[position]
        holder.tvName.text = customer.customerName
        holder.tvMobile.text = customer.mobile
        holder.tvBalance.text = String.format("%.2f", customer.balance)
    }

    override fun getItemCount(): Int = customers.size

    fun updateList(newList: List<Outsatnding>) {
        customers = newList
        notifyDataSetChanged()
    }
}
