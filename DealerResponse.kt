package Model

data class DealerResponse( val mobile: String,
                           val balance: Double,
                           val entities: List<DealerEntity>)
