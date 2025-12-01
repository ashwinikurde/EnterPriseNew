package Model

data class DealerEntity( val grn: Int,
                         val refInvNo: String,
                         val dt: String,
                         val dealerName: String,
                         val received: Double,
                         val given: Double,
                         val particular: String,
                         val dealerId: Int,
                         val firmId: Int,
                         val financialYearId: Int,
                         val sortOrder: Int,
                         val details: List<Detail>)
