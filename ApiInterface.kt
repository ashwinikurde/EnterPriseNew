package px.corp.enterprisenew

import Model.Account
import Model.AccountRequest
import Model.AccountResponse
import Model.Customer
import Model.DealerId
import Model.DealerRequest
import Model.DealerResponse
import Model.Outsatnding
import Model.Product
import Model.ProductOutstanding
import Model.ProductRequest
import Model.ProductResponse
import Model.stock
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {
    @POST("api/Auth/UserLogin")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("api/Product/GetStockAlert")
    fun getStockItems(@Header("Authorization") token: String): Call<List<stock>>

//    @GET("api/Customer/list")
//    fun getCustomers(): Call<List<Customer>>

    @GET("api/Customer/list")
    fun getCustomers(@Header("Authorization") token: String): Call<List<Customer>>

    @GET("api/Product/list")
    fun getProducts(@Header("Authorization") token: String): Call<List<Product>>

    @GET("api/Account/list")
    fun getAccount(@Header("Authorization") token: String): Call<List<Account>>

    @GET("api/Product/GetProductOutstanding")
    fun getProductOutstanding(@Header("Authorization") token: String): Call<List<ProductOutstanding>>


    @GET("api/Dealer/list")
    fun getDealerList(@Header("Authorization") token: String): Call<List<DealerId>>

//    @GET("api/Customer/getcustomer/{id}")
//    fun getCustomerById(
//        @Header("Authorization") token: String,
//        @Path("id") customerId: String
//    ): Call<CustomerDetail>
//@POST("api/Customer/getcustomer")
//fun getCustomerLedger(
//    @Header("Authorization") token: String,
//    @Body request: CustomerLedgerRequest
//): Call<CustomerDetail>


    @POST("api/Customer/getcustomer/{id}")
    fun getCustomerLedger(
        @Header("Authorization") token: String,
        @Path("id") customerId: Int,
        @Body request: CustomerLedgerRequest
    ): Call<CustomerLedgerResponse>

    @POST("api/Product/getProduct/{id}")
    fun getProduct(
        @Header("Authorization") token: String,
        @Path("id") productId: Int,
        @Body request: ProductRequest
    ): Call<ProductResponse>

    @POST("api/Account/getAccount/{accountId}")
    fun getAccountData(
        @Header("Authorization") token: String,
        @Path("accountId") accountId: Int,
        @Body request: AccountRequest
    ): Call<AccountResponse>


    @GET("api/Customer/GetCustomerOutstanding")
    fun getOutstanding(
        @Header("Authorization") token: String
    ): Call<List<Outsatnding>>

    @POST("api/Dealer/getdealer/{id}")
    fun getDealerData(
        @Header("Authorization") token: String,
        @Path("id") dealerId: Int,
        @Body request: DealerRequest
    ): Call<DealerResponse>

    }




//        @POST("api/Customer/getcustomer")
//        fun getCustomerById(
//
//
//            @Header("Authorization") token: String,
//            @Body request: CustomerLedgerRequest
//        ): Call<CustomerInfoResponse>



