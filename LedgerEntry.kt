package px.corp.enterprisenew

import androidx.recyclerview.widget.DiffUtil
import com.google.gson.annotations.SerializedName

data class LedgerEntry(
    @SerializedName("customerId") val customerId: Int,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("mobileNo") val mobileNo: String,
    @SerializedName("grn") val grn: Int?,
    @SerializedName("invNo") val invNo: Int?,
    @SerializedName("transDate") val transDate: String,
    @SerializedName("transactionType") val transactionType: String,
    @SerializedName("given") val given: Double,
    @SerializedName("received") val received: Double,
   // val balance: Double
)
