package Adapers



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import px.corp.enterprisenew.R
import px.corp.enterprisenew.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Locale


class LedgerAdapter(private val entries: List<TransactionEntity>) :
    RecyclerView.Adapter<LedgerAdapter.LedgerViewHolder>() {

    inner class LedgerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvGrn: TextView = itemView.findViewById(R.id.tvGrn)
        val inNo: TextView = itemView.findViewById(R.id.invNo)
       // val challNo: TextView = itemView.findViewById(R.id.challNo)
        val tvParticular: TextView = itemView.findViewById(R.id.tvParticular)
        val tvGiven: TextView = itemView.findViewById(R.id.tvGiven)
        val tvReceived: TextView = itemView.findViewById(R.id.tvReceived)
        val tvBalance: TextView = itemView.findViewById(R.id.tvBalance1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger_entry, parent, false)
        return LedgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvDate.text = formatDate(entry.transDate)
        holder.tvGrn.text = entry.grn
        holder.inNo.text=entry.invNo
       // holder.challNo.text=entry.challanNo

        holder.tvParticular.text = entry.transactionType

        holder.tvGiven.text = entry.given.toString()
        holder.tvReceived.text = entry.received.toString()
        holder.tvBalance.text = (entry.received - entry.given).toString()
    }

    override fun getItemCount(): Int = entries.size
}
fun formatDate(originalDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(originalDate)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        originalDate // fallback to original
    }
}