package px.corp.enterprisenew

import com.google.gson.annotations.SerializedName

data class CustomerLedgerRequest(

//@SerializedName("id") val id: Int,
//@SerializedName("startDate") val startDate: String,
//@SerializedName("endDate") val endDate: String,



@SerializedName("id") val id: Int,
@SerializedName("nm") val name: String,
@SerializedName("mobile") val mobile: String,
@SerializedName("address") val address: String,

@SerializedName("endDt") val endDate: String,
@SerializedName("startDt") val startDate: String,
)


