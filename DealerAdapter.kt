package Adapers

import Model.AccountEntity
import Model.DealerEntity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import px.corp.enterprisenew.R

class DealerAdapter(private val entries: List<DealerEntity>):
    RecyclerView.Adapter<DealerAdapter.DealerViewHolder>() {
    inner class DealerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvparticular: TextView = itemView.findViewById(R.id.tvParticular)
        val tvReceived: TextView = itemView.findViewById(R.id.tvReceived)
        val tvGiven: TextView = itemView.findViewById(R.id.tvgiven)

        val tvBalance: TextView = itemView.findViewById(R.id.tvBalance1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DealerAdapter.DealerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dealer, parent, false)
        return DealerViewHolder(view)
    }

    override fun onBindViewHolder(holder: DealerAdapter.DealerViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvDate.text = formatDate(entry.dt)


        holder.tvparticular.text = entry.particular
        holder.tvReceived.text = entry.received.toString()
        holder.tvGiven.text = entry.given.toString()
        //holder.tvBalance.text = balance.toString()

        // holder.tvBalance.text = (entry.received - entry.given).toString()
        var balance = 0.0
        for (i in 0..position) {
            balance += entries[i].received - entries[i].given
        }
        holder.tvBalance.text = balance.toString()

    }

    override fun getItemCount(): Int = entries.size


}