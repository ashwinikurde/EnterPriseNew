package Model

data class ProductOutstanding(
    val productComapnyid: Int,
    val productName: String,
    val firmId: Int,
    val stock: Double,
    val isActive: Boolean
)

