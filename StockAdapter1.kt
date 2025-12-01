package Adapers

import Model.ProductEntity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import px.corp.enterprisenew.R
import java.text.SimpleDateFormat
import java.util.Locale

class StockAdapter1(private val entries: List<ProductEntity>) :
    RecyclerView.Adapter<StockAdapter1.StockViewHolder>() {
    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val billNo: TextView = itemView.findViewById(R.id.tvBillNo)

        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvGrn: TextView = itemView.findViewById(R.id.tvGrn)
      //  val inNo: TextView = itemView.findViewById(R.id.invNo)
       val status:TextView = itemView.findViewById(R.id.tvStatus)
        // val challNo: TextView = itemView.findViewById(R.id.challNo)
       // val tvParticular: TextView = itemView.findViewById(R.id.tvParticular)
        val tvGiven: TextView = itemView.findViewById(R.id.tvGiven)
        val tvReceived: TextView = itemView.findViewById(R.id.tvReceived)
        val tvBalance: TextView = itemView.findViewById(R.id.tvBalance1)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvDate.text = formatDate1(entry.dt)
        holder.tvGrn.text = entry.id.toString()
        holder.billNo.text = entry.invNo
        holder.status.text = entry.status

        //  holder.tvParticular.text = entry.transactionType

        holder.tvGiven.text = entry.givenQty.toString()
        holder.tvReceived.text = entry.receivedQty.toString()
        //holder.tvBalance.text = (entry.receivedQty - entry.givenQty).toString()

        var balance = 0.0   // make it Double
        for (i in 0..position) {
            balance += entries[i].receivedQty - entries[i].givenQty
        }
        holder.tvBalance.text = balance.toInt().toString()  // if you want whole number
// OR
// holder.tvBalance.text = balance.toString()       // if you want decimal
    }



        //     holder.billNo.text=entry.bi


    override fun getItemCount(): Int = entries.size
}
fun formatDate1(originalDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(originalDate)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        originalDate // fallback to original
    }
}