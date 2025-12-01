package Adapers

import Model.ProductOutstanding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import px.corp.enterprisenew.R

class ProductAdapter(private var originalList: ArrayList<ProductOutstanding>):RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    private var filteredList: ArrayList<ProductOutstanding> = ArrayList(originalList)
    inner class  ProductViewHolder(view: View): RecyclerView.ViewHolder(view)  {




        val tvActive = view.findViewById<TextView>(R.id.textViewStockAlert)
        val name = view.findViewById<TextView>(R.id.textViewName)
        val tvStock = view.findViewById<TextView>(R.id.textViewStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = originalList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = originalList[position]
        holder.tvActive.text = if (item.isActive) "Yes" else "No"
        holder.name.text = item.productName
        holder.tvStock.text = item.stock.toString()

    }

    fun updateList(newList: ArrayList<ProductOutstanding>) {
        originalList = newList
        notifyDataSetChanged()
    }

}