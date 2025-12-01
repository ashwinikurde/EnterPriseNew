package Model

data class Detail( val productName: String,
                   val qty: Double,
                   val total: Double,
                   val amount: Double,
                   val rate: Double,
                   val gst: Double,
                   val taxableamt: Double,
                   val status: String,
                   val remark: String)
