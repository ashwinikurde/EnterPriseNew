package Model

data class AccountEntity (
    val accountId: Int,
    val account: String?,
    val accountGroup: String?,
    val date: String,
    val remark: String?,
    val grn: String?,
    val invNo: String?,
    val given: Double,
    val received: Double,
    val particular: String,
    val firmID: String?
    )
