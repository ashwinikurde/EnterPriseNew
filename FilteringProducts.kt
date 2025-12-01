package px.corp.enterprisenew

import Adapers.StockAdapter
import Model.stock
import android.widget.Filter
import java.util.Locale

class FilteringProducts(
    private val adapter: StockAdapter,
    private val originalList: ArrayList<stock>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val result = FilterResults()

        if (!constraint.isNullOrEmpty()) {
            val query = constraint.toString().trim().uppercase(Locale.getDefault())
            val filteredList = originalList.filter {
                it.name?.uppercase(Locale.getDefault())?.contains(query) == true
            }

            result.values = ArrayList(filteredList)
            result.count = filteredList.size
        } else {
            result.values = originalList
            result.count = originalList.size
        }

        return result
    }

    override fun publishResults(constraint: CharSequence?, result: FilterResults?) {
        val filteredList = result?.values as? ArrayList<stock> ?: arrayListOf()
        adapter.updateList(filteredList)
    }
}
