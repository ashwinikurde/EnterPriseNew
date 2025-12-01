package Adapers

import Model.AccountEntity
import Model.ProductEntity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import px.corp.enterprisenew.R

class AccountAdapter(private val entries: List<AccountEntity>) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {
    inner class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvparticular: TextView = itemView.findViewById(R.id.tvParticular)

        val tvGiven: TextView = itemView.findViewById(R.id.tvGiven)
        val tvReceived: TextView = itemView.findViewById(R.id.tvReceived)
        val tvBalance: TextView = itemView.findViewById(R.id.tvBalance1)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account_ledger, parent, false)
        return AccountViewHolder(view)
    }

    override fun getItemCount(): Int = entries.size

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvDate.text = formatDate(entry.date)


        holder.tvparticular.text = entry.particular

        holder.tvGiven.text = entry.given.toString()
        holder.tvReceived.text = entry.received.toString()
       // holder.tvBalance.text = (entry.received - entry.given).toString()
        var balance = 0.0
        for (i in 0..position) {
            balance += entries[i].received - entries[i].given
        }
        holder.tvBalance.text = balance.toString()
    }
}