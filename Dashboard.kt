package px.corp.enterprisenew

import Adapers.StockAdapter
import Adapers.StockAdapter1
import Model.ProductEntity
import Model.ProductRequest
import Model.ProductResponse
import Model.stock
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import px.corp.enterprisenew.databinding.ActivityDashboardBinding
import px.corp.enterprisenew.databinding.EditproductlayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Dashboard : AppCompatActivity(), StockAdapter.OnItemClickListener {
    private var stockAdapter1: StockAdapter1? = null
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var stockAdapter: StockAdapter
    private var stockList: ArrayList<stock> = ArrayList()
    private var isStockLoaded = false
    private var latestLedgerEntries: List<ProductEntity> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationDrawer()
        setupRecyclerView()
        setStatusBarColor()
        loadStockData()
        handleDrawerIntent()
    }
    private fun handleDrawerIntent() {
        val openDrawer = intent.getBooleanExtra("OPEN_DRAWER", false)
        val highlightItem = intent.getIntExtra("HIGHLIGHT_ITEM", -1)

        if (openDrawer) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        if (highlightItem != -1) {
            binding.navigationView.setCheckedItem(highlightItem)
        }
    }

    private fun showToast(msg: String) =
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    private fun fetchCustomerLedgerDetails1(
        startDate: String,
        endDate: String,
        stock: stock,
        editProduct: EditproductlayoutBinding
    ) {
        val token = getToken(this) ?: return showToast("Token missing!")
        Utils.showLoading(this)
        // Show customer name
        editProduct.tvCustomerTitle.text = stock.name

        // Get customer ID from stock model
        val customerId = stock.id   // make sure your stock model has an "id" field
        val customerName = stock.name

        if (customerId.isEmpty()) return showToast("Invalid customer ID for $customerName")

        // Build request
        val request = ProductRequest(
            productId = customerId.toInt(),
            startDt = startDate,
            endDt = endDate
        )

        // API Call
        RetrofitClient.instance.getProduct("Bearer $token", customerId.toInt(), request)
            .enqueue(object : Callback<ProductResponse> {
                override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                    Utils.hideLoading()
                    if (!isFinishing && response.isSuccessful) {
                        val result = response.body()
                        latestLedgerEntries = result?.entities.orEmpty()
                        stockAdapter1 = StockAdapter1(latestLedgerEntries)

                        editProduct.recyclerLedger1.layoutManager = LinearLayoutManager(this@Dashboard)
                        editProduct.recyclerLedger1.adapter = stockAdapter1

                        // Optional extra info
                        // editProduct.tvBalance.text = "Balance: ₹${result?.balance ?: 0}"
                        // editProduct.tvMobile.text = "Mobile: ${result?.customermobile ?: "N/A"}"
                    } else {
                        showToast("Ledger fetch failed")
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    Utils.hideLoading()
                    if (!isFinishing) showToast("Network error: ${t.message}")
                }
            })
    }


    private fun setupRecyclerView() {
        binding.recyclerViewStock.layoutManager = LinearLayoutManager(this)
    }
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                val selected = Calendar.getInstance().apply { set(y, m, d) }
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                onDateSelected(dateFormat.format(selected.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> startActivity(Intent(this, Dashboard::class.java))
                R.id.nav_customer -> startActivity(Intent(this, CustomerActivity::class.java))
                R.id.nav_stock -> startActivity(Intent(this, StockActivity::class.java))
              //  R.id.nav_account -> startActivity(Intent(this, AccountActivity::class.java))
               // R.id.nav_Dealer -> startActivity(Intent(this, DealerActivity::class.java))
                R.id.nav_logout -> {
                    showLogoutDialog()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        binding.ivProfile.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun searchProductsNew() {
        val filter = FilteringProducts(stockAdapter, stockList)
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter.filter(s.toString().trim())
            }
        })
    }
    private fun loadStockData() {
        if (isStockLoaded) {
            // Data already loaded, just refresh adapter
            stockAdapter.updateList(stockList)
            return
        }

        val token = getToken(this)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
            return
        }
        Utils.showLoading(this)
        val bearer = "Bearer $token"
        RetrofitClient.instance.getStockItems(bearer)
            .enqueue(object : Callback<List<stock>> {
                override fun onResponse(call: Call<List<stock>>, response: Response<List<stock>>) {
                    Utils.hideLoading()
                    if (response.isSuccessful && response.body() != null) {
                        stockList = ArrayList(response.body()!!)
                        stockAdapter = StockAdapter(
                            stockList,
                            this@Dashboard

                        )
                        binding.recyclerViewStock.adapter = stockAdapter
                        searchProductsNew()
                        isStockLoaded = true // ✅ Mark as loaded
                        Toast.makeText(this@Dashboard, "Stock list loaded", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@Dashboard, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("API_ERROR", response.errorBody()?.string() ?: "Unknown error")
                    }
                }

                override fun onFailure(call: Call<List<stock>>, t: Throwable) {
                    Utils.hideLoading()
                    Toast.makeText(this@Dashboard, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("API_FAILURE", "Error: ${t.message}")
                }
            })
    }

//    private fun loadStockData() {
//        val token = getToken(this)
//        if (token.isNullOrEmpty()) {
//            Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val bearer = "Bearer $token"
//        RetrofitClient.instance.getStockItems(bearer)
//            .enqueue(object : Callback<List<stock>> {
//                override fun onResponse(call: Call<List<stock>>, response: Response<List<stock>>) {
//                    if (response.isSuccessful && response.body() != null) {
//                        stockList = ArrayList(response.body()!!)
//                        stockAdapter = StockAdapter(stockList)
//                        binding.recyclerViewStock.adapter = stockAdapter
//                        searchProductsNew()
//                        Toast.makeText(this@Dashboard, "Stock list loaded", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(this@Dashboard, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
//                        Log.e("API_ERROR", response.errorBody()?.string() ?: "Unknown error")
//                    }
//                }
//
//                override fun onFailure(call: Call<List<stock>>, t: Throwable) {
//                    Toast.makeText(this@Dashboard, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
//                    Log.e("API_FAILURE", "Error: ${t.message}")
//                }
//            })
//    }

    private fun setStatusBarColor() {
        val statusBarColor = ContextCompat.getColor(this, R.color.blue)
        window.statusBarColor = statusBarColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("token", null)
    }

    override fun onItemClick(item: stock) {
        // Inflate dialog binding
        val editProduct = EditproductlayoutBinding.inflate(layoutInflater)
        val calendar = Calendar.getInstance()
        // Build and show the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(editProduct.root)
            .setCancelable(true)
            .create()
        dialog.show()

        // Set customer name
        editProduct.tvCustomerTitle.text = item.name

        // Set default values only if fields are empty
        if (editProduct.etStartDate.text.isNullOrEmpty() || editProduct.etendDate.text.isNullOrEmpty()) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            calendar.add(Calendar.MONTH, -1)
            val oneMonthBefore = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            editProduct.etStartDate.setText(oneMonthBefore)
            editProduct.etendDate.setText(today)
        }

        // Setup date pickers
        editProduct.etStartDate.setOnClickListener {
            showDatePicker { selectedDate ->
                editProduct.etStartDate.setText(selectedDate)
            }
        }

        editProduct.etendDate.setOnClickListener {
            showDatePicker { selectedDate ->
                editProduct.etendDate.setText(selectedDate)
            }
        }

        // Initial fetch with current values (today by default)
        fetchCustomerLedgerDetails1(
            startDate = editProduct.etStartDate.text.toString(),
            endDate = editProduct.etendDate.text.toString(),
            editProduct = editProduct,
            stock = item
        )

        // Setup search button
        editProduct.btnSearch.setOnClickListener {
//            val startDate = editProduct.etStartDate.text.toString().trim()
//            val endDate = editProduct.etendDate.text.toString().trim()
//            val customerName = editProduct.tvCustomerTitle.text.toString().trim()
//
//            if (customerName.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
//                showToast("Please select all fields")
//            } else {
            // Fetch ledger without changing selected dates
            fetchCustomerLedgerDetails1(
                startDate = editProduct.etStartDate.text.toString(),
                endDate = editProduct.etendDate.text.toString(),
                editProduct = editProduct,
                stock = item
            )

            //  }
        }
    }

    fun logOutUser(context: Context) {
        FirebaseAuth.getInstance().signOut()

        // Clear saved phone number from SharedPreferences
        val sharedPref = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }
    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Log Out")
            .setMessage("Do you want to Log Out?")
            .setPositiveButton("Yes") { _, _ ->
                // Call logout function
                logOutUser(this)


                // Redirect to MainActivity (or LoginActivity)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

}



