package Model

import com.google.gson.annotations.SerializedName

data class stock(
    @SerializedName("productComapnyid") val id: String,
    @SerializedName("productName") val name: String,
    @SerializedName("productStockAlert") val stock: String,
    @SerializedName("outstandingStock") val alert: String
)



