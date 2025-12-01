package px.corp.enterprisenew

import Model.Customer

data class CustomerBalance(
    val id: Int,
    val customerId: Int,
    val customer: Customer,
    val firmId: Int,
    val financialId: Int,
    val amount: Double
)
