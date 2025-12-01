package Model

data class ProductEntity(
    val id: Int,
    val invNo: String?,
    val productName: String?,
    val receivedQty: Double,
    val givenQty: Double,
    val dt: String,
    val accountName: String?,
    val accountId: Int?,
    val status: String,
    val firmId: Int,
    val productComapnyid: Int,
    val financialYearId: Int,
    val isActive: Boolean
) {

}
