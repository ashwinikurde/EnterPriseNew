package Model

import com.google.gson.annotations.SerializedName

data class Customer(

val id: String,
val name: String,
//@SerializedName("startDate") val startDate: String,
//@SerializedName("endDate") val endDate: String
//val mobile: String,
//val address: String
)


