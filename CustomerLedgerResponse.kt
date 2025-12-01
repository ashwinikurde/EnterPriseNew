package px.corp.enterprisenew

data class CustomerLedgerResponse(
    val receivedTotal: Double,
    val givenTotal: Double,
    val custobalance: CustomerBalance,
    val entities: List<TransactionEntity>,
    val balance: Double,
    val customermobile: String,
    val customerName: String,
    val prevbal: Double,
    val custbal: Double
)
