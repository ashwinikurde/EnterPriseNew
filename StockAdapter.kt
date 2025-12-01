package Adapers

import Model.Product
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import px.corp.enterprisenew.R
import Model.stock

class StockAdapter(
    private val originalList: ArrayList<stock>,
    private val listener: OnItemClickListener,

) :
    RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    private var filteredList: ArrayList<stock> = ArrayList(originalList)

    inner class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.textViewName)
        val stock = view.findViewById<TextView>(R.id.textViewStock)
        val alert = view.findViewById<TextView>(R.id.textViewStockAlert)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(filteredList[position])  // 🔥 trigger callback
                }
            }
        }



    }
    interface OnItemClickListener {
        fun onItemClick(item: stock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val item = filteredList[position]
        holder.name.text = item.name
        holder.stock.text = item.stock.toString()
        holder.alert.text = item.alert.toString()


    }

    override fun getItemCount(): Int = filteredList.size

    fun updateList(newList: ArrayList<stock>) {
        filteredList = newList
        notifyDataSetChanged()
    }
}
