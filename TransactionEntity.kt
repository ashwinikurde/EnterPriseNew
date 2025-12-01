package px.corp.enterprisenew

import androidx.recyclerview.widget.DiffUtil

data class TransactionEntity(   val customerId: Int,
                                val customerName: String,
                                val mobileNo: String,
                                val grn:String?,
                                val invNo:String?,
                                val challanNo: String?,
                                val transDate: String,
                                val given: Double,
                                val received: Double,
                                val transactionType: String,
                                val firmID: Int?,
                                val description: String,
                                val sequence: Int,
                                val details: List<TransactionDetail>)


object LedgerDiffCallback : DiffUtil.ItemCallback<LedgerEntry>() {
    override fun areItemsTheSame(oldItem: LedgerEntry, newItem: LedgerEntry): Boolean {
        return oldItem.transDate == newItem.transDate && oldItem.grn == newItem.grn
    }

    override fun areContentsTheSame(oldItem: LedgerEntry, newItem: LedgerEntry): Boolean {
        return oldItem == newItem
    }
}