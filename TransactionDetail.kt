package px.corp.enterprisenew

import androidx.recyclerview.widget.DiffUtil

data class TransactionDetail( val productName: String,
                              val qty: Double,
                              val rate: Double,
                              val amount: Double,
                              val remark: String,
                              val dis: Double,
                              val taxamt: Double,
                              val gst: Double,
                              val status: String)
