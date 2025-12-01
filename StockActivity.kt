package px.corp.enterprisenew

import Adapers.ProductAdapter
import Adapers.StockAdapter
import Adapers.StockAdapter1
import Model.Product
import Model.ProductEntity
import Model.ProductOutstanding
import Model.ProductRequest
import Model.ProductResponse
import Model.stock
import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import px.corp.enterprisenew.databinding.ActivityStockBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import px.corp.enterprisenew.databinding.EditproductlayoutBinding
import java.io.File
import java.io.FileOutputStream
import java.util.Date


import java.util.Locale


class StockActivity : AppCompatActivity(),StockAdapter.OnItemClickListener {
    private var stockAdapter1: StockAdapter1? = null
    private lateinit var stockAdapter: StockAdapter
    private lateinit var binding: ActivityStockBinding
    private val nameToIdMap = mutableMapOf<String, String>()
    private var isStockLoaded = false
    private var latestLedgerEntries: List<ProductEntity> = emptyList()
    private lateinit var productAdapter: ProductAdapter
    private var allCustomers: ArrayList<ProductOutstanding> = ArrayList()
    private var stockList: ArrayList<ProductOutstanding> = ArrayList()
    private var stockList1: ArrayList<stock> = ArrayList()
    private var isStockLoaded1 = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockBinding.inflate(layoutInflater)
        binding.recyclerLedger.layoutManager = LinearLayoutManager(this)
        //    binding.recyclerOutstanding.layoutManager = LinearLayoutManager(this)

        stockAdapter1 = StockAdapter1(emptyList())
        binding.recyclerLedger.adapter = stockAdapter1

        productAdapter = ProductAdapter(ArrayList())
        binding.recyclerOutstanding.layoutManager = LinearLayoutManager(this) // ✅ FIX
        binding.recyclerOutstanding.adapter = productAdapter

        stockAdapter = StockAdapter(ArrayList(), this@StockActivity)
        binding.rcycleAlert.layoutManager = LinearLayoutManager(this) // ✅ FIX
        binding.rcycleAlert.adapter = stockAdapter

        setContentView(binding.root)
        setStatusBarColor()
        fetchCustomerList()
        setupSearch()
        setupDatePickers()
        setupTabs()
        export()
        exportStock()
        backtodashboard()
        setupSearchButton()
    }

    private fun setStatusBarColor() {
        val statusBarColor = ContextCompat.getColor(this, R.color.white)
        window.statusBarColor = statusBarColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun backtodashboard(){

            binding.tbHomeFragment.setOnClickListener {
                val intent = Intent(this, Dashboard::class.java)
                intent.putExtra("OPEN_DRAWER", true)
                intent.putExtra("HIGHLIGHT_ITEM", R.id.nav_stock) // pass the menu id
                startActivity(intent)
                finish()
            }
        }

    private fun showLedgerLayout() {
        binding.layoutLedgerDate.visibility = View.VISIBLE
        binding.layoutStock.visibility = View.GONE
        binding.layoutStock1.visibility = View.GONE
        // Highlight selected tab
        binding.tabLedgerDate.setTextColor(ContextCompat.getColor(this, R.color.blue))
        binding.tabLedgerDate.setTypeface(null, Typeface.BOLD)

        binding.txtStock.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.txtStock.setTypeface(null, Typeface.NORMAL)
        binding.txtAlert.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.txtAlert.setTypeface(null, Typeface.NORMAL)
    }

    private fun setupTabs() {
        binding.tabLedgerDate.setOnClickListener {
            showLedgerLayout()

        }

        binding.txtStock.setOnClickListener {
            showStocklayout()

        }
        binding.txtAlert.setOnClickListener {
            showAlertLayout()

        }


    }

    private fun exportStock() {
        binding.ivPdf.setOnClickListener {
            exportStockPdfAndShareWhatsApp()
        }
    }

    private fun showStocklayout() {
        binding.layoutLedgerDate.visibility = View.GONE
        binding.layoutStock1.visibility = View.GONE
        binding.layoutStock.visibility = View.VISIBLE

        // Highlight selected tab
        binding.txtStock.setTextColor(ContextCompat.getColor(this, R.color.blue))
        binding.txtStock.setTypeface(null, Typeface.BOLD)

        binding.tabLedgerDate.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.tabLedgerDate.setTypeface(null, Typeface.NORMAL)
        binding.txtAlert.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.txtAlert.setTypeface(null, Typeface.NORMAL)


        if (allCustomers.isEmpty()) {
            loadStockData()
        } else {
            productAdapter?.updateList(allCustomers)
            // binding.tvTotalOutstanding.text = "Total Balance: %.2f".format(allCustomers.sumOf { it.balance })
        }
    }

    // Only load if list is empty
//        if (allCustomers.isEmpty()) {
//            loadStockData()
//        } else {
//            outstandingAdapter?.updateList(allCustomers)
//            binding.tvTotalOutstanding.text = "Total Balance: %.2f".format(allCustomers.sumOf { it.balance })
//        }}
    private fun showAlertLayout() {
        binding.layoutLedgerDate.visibility = View.GONE
        binding.layoutStock.visibility = View.GONE
        binding.layoutStock1.visibility = View.VISIBLE

        // Highlight selected tab
        binding.txtAlert.setTextColor(ContextCompat.getColor(this, R.color.blue))
        binding.txtAlert.setTypeface(null, Typeface.BOLD)

        binding.tabLedgerDate.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.tabLedgerDate.setTypeface(null, Typeface.NORMAL)
        binding.txtStock.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.txtStock.setTypeface(null, Typeface.NORMAL)
        loadStockAlertData()

        // Only load if list is empty
//        if (allCustomers.isEmpty()) {
//            loadStockData()
//        } else {
//            outstandingAdapter?.updateList(allCustomers)
//            binding.tvTotalOutstanding.text = "Total Balance: %.2f".format(allCustomers.sumOf { it.balance })
//        }
    }


    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            if (binding.txtCategory.text.isNullOrBlank() ||
                binding.etStartDate.text.isNullOrBlank() ||
                binding.etendDate.text.isNullOrBlank()
            ) {
                showToast("Please select all fields")
            } else {
                fetchCustomerLedgerDetails()
            }
        }
    }

    //    private fun searchProductsNew() {
//        val filter = FilteringProducts(productAdapter, stockList)
//        binding.etSearchName.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun afterTextChanged(s: Editable?) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                filter.filter(s.toString().trim())
//            }
//        })
//    }
    private fun setupRecyclerView() {
        binding.recyclerOutstanding.layoutManager = LinearLayoutManager(this)
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.add(Calendar.MONTH, -1)
        val oneMonthBefore = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        binding.etStartDate.setText(oneMonthBefore)
        binding.etendDate.setText(today)

        binding.etStartDate.setOnClickListener { showDatePicker { binding.etStartDate.setText(it) } }
        binding.etendDate.setOnClickListener { showDatePicker { binding.etendDate.setText(it) } }
    }

    private fun setupSearch() {
        binding.etSearchName.addTextChangedListener { text ->
            val query = text?.toString()?.trim() ?: ""
            val filtered = if (query.isEmpty()) allCustomers
            else allCustomers.filter { it.productName.contains(query, ignoreCase = true) }

            productAdapter.updateList(ArrayList(filtered))

            val totalStock = filtered.sumOf { it.stock }
            binding.tvTotalOutstanding.text = "Total Stock: %.2f".format(totalStock)

            Log.d("SearchDebug", "Query: $query | Results: ${filtered.size}")
        }
    }

    private fun exportStockPdfAndShareWhatsApp() {
        if (stockList.isEmpty()) {
            Toast.makeText(this, "No stock data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        val paint = Paint().apply { textSize = 14f }
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
            color = Color.BLACK
        }

        var pageNumber = 1

        fun drawHeader(canvas: Canvas, pageNumber: Int): Float {
            canvas.drawText("Stock Report", 230f, 40f, titlePaint)
            canvas.drawText(
                "Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}",
                20f, 60f, paint
            )
            canvas.drawText("Page: $pageNumber", 500f, 60f, paint)

            var y = 100f
            canvas.drawText("Product Name", 20f, y, titlePaint)
            canvas.drawText("Stock", 450f, y, titlePaint)
            y += 30f
            return y
        }

        // First page
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = drawHeader(canvas, pageNumber) // <-- y is initialized here

        val bottomLimit = 780f
        val totalStock =
            stockList.sumOf { it.stock } // if stock is Long, use .sumOf { it.stock.toLong() }.toString()

        for (item in stockList) {
            if (y > bottomLimit) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = drawHeader(canvas, pageNumber) // reset y for new page
            }
            canvas.drawText(item.productName, 20f, y, paint)
            canvas.drawText(item.stock.toString(), 450f, y, paint)
            y += 25f
        }

        y += 40f
        canvas.drawText("Total Stock: $totalStock", 20f, y, titlePaint)

        pdfDocument.finishPage(page)

        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "StockReport.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(shareIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onEditButtonClicked(stock: stock, productEntity: ProductEntity) {
        // Inflate dialog layout
        val editProduct = EditproductlayoutBinding.inflate(LayoutInflater.from(this))

        // Build AlertDialog first ✅
        val dialog = AlertDialog.Builder(this)
            .setView(editProduct.root)
            .setCancelable(true)
            .create()

        editProduct.apply {
            // Set product name in dialog
            tvCustomerTitle.text = stock.name

            // Default today’s date
            val calendar = Calendar.getInstance()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            etStartDate.setText(today)
            etendDate.setText(today)

            // Show date pickers
            etStartDate.setOnClickListener { showDatePicker { etStartDate.setText(it) } }
            etendDate.setOnClickListener { showDatePicker { etendDate.setText(it) } }

            // 🔹 Search button → fetch ledger with dialog dates
            btnSearch.setOnClickListener {

                fetchCustomerLedgerDetails(productEntity, editProduct)  // pass dialog binding
            }

            // 🔹 Close button

            //  btnClose.setOnClickListener { dialog.dismiss() }
        }

        dialog.show() // ✅ finally show
    }
    private fun fetchCustomerLedgerDetails1(
        startDate: String,
        endDate: String,
        stock: stock,
        editProduct: EditproductlayoutBinding
    ) {
        val token = getToken(this) ?: return showToast("Token missing!")

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
                    if (!isFinishing && response.isSuccessful) {
                        val result = response.body()
                        latestLedgerEntries = result?.entities.orEmpty()
                        stockAdapter1 = StockAdapter1(latestLedgerEntries)

                        editProduct.recyclerLedger1.layoutManager = LinearLayoutManager(this@StockActivity)
                        editProduct.recyclerLedger1.adapter = stockAdapter1

                        // Optional extra info
                        // editProduct.tvBalance.text = "Balance: ₹${result?.balance ?: 0}"
                        // editProduct.tvMobile.text = "Mobile: ${result?.customermobile ?: "N/A"}"
                    } else {
                        showToast("Ledger fetch failed")
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    if (!isFinishing) showToast("Network error: ${t.message}")
                }
            })
    }


//    private fun setupSearchButton1(editProduct: EditproductlayoutBinding,selectedStock: stock) {
//        editProduct.btnSearch.setOnClickListener {
//            val category = editProduct.tvCustomerTitle.text.toString().trim()
//            val startDate = editProduct.etStartDate.text.toString().trim()
//            val endDate = editProduct.etendDate.text.toString().trim()
//
//            if (category.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
//                showToast("Please select all fields")
//            } else {
//                // Call ledger fetch with selected stock and dates
//                fetchCustomerLedgerDetails1( startDate, endDate,editProduct,selectedStock)
//            }
//        }
//    }

    private fun loadStockData() {
        if (isStockLoaded) {
            productAdapter.updateList(stockList)

            val totalStock = stockList.sumOf { it.stock }
            binding.tvTotalOutstanding.text = "Total Stock: $totalStock"
            return
        }

        val token = getToken(this)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
            return
        }
Utils.showLoading(this)
        val bearer = "Bearer $token"
        RetrofitClient.instance.getProductOutstanding(bearer)
            .enqueue(object : Callback<List<ProductOutstanding>> {


                override fun onResponse(

                    call: Call<List<ProductOutstanding>>,
                    response: Response<List<ProductOutstanding>>
                ) {
                    Utils.hideLoading()
                    if (response.isSuccessful && response.body() != null) {
                        stockList = ArrayList(response.body()!!)
                        allCustomers = ArrayList(response.body()!!) // ✅ important

                        productAdapter = ProductAdapter(stockList)
                        binding.recyclerOutstanding.adapter = productAdapter

                        val totalStock = stockList.sumOf { it.stock }
                        binding.tvTotalOutstanding.text = "Total Stock: $totalStock"

                        isStockLoaded = true
                        Toast.makeText(this@StockActivity, "Stock list loaded", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this@StockActivity,
                            "Error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("API_ERROR", response.errorBody()?.string() ?: "Unknown error")
                    }
                }

                override fun onFailure(call: Call<List<ProductOutstanding>>, t: Throwable) {
                    Utils.hideLoading()
                    Toast.makeText(this@StockActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("API_FAILURE", "Error: ${t.message}")
                }
            })
    }

    private fun searchProductsNew() {
        val filter = FilteringProducts(stockAdapter, stockList1)
        binding.etSearchAlert.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter.filter(s.toString().trim())
            }
        })
    }

    private fun loadStockAlertData() {
        Utils.showLoading(this)
        if (isStockLoaded1) {
            // Data already loaded, just refresh adapter
            stockAdapter.updateList(stockList1)
            return
        }

        val token = getToken(this)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        val bearer = "Bearer $token"
        RetrofitClient.instance.getStockItems(bearer)
            .enqueue(object : Callback<List<stock>> {
                override fun onResponse(call: Call<List<stock>>, response: Response<List<stock>>) {
                    Utils.hideLoading()
                    if (response.isSuccessful && response.body() != null) {
                        stockList1 = ArrayList(response.body()!!)
                        stockAdapter = StockAdapter(
                            stockList1,
                            this@StockActivity

                        )
                        binding.rcycleAlert.adapter = stockAdapter
                        searchProductsNew()
                        isStockLoaded = true // ✅ Mark as loaded
                        Toast.makeText(this@StockActivity, "Stock list loaded", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this@StockActivity,
                            "Error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("API_ERROR", response.errorBody()?.string() ?: "Unknown error")
                    }
                }

                override fun onFailure(call: Call<List<stock>>, t: Throwable) {
                    Utils.hideLoading()
                    Toast.makeText(this@StockActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("API_FAILURE", "Error: ${t.message}")
                }
            })
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

    private fun export() {
        binding.btnExportPdf.setOnClickListener {
            exportToPdfAndShareOnWhatsApp()
        }
    }


    private fun exportToPdfAndShareOnWhatsApp() {
        if (latestLedgerEntries.isEmpty()) {
            showToast("No data to export")
            return
        }

        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        titlePaint.textSize = 18f
        titlePaint.isFakeBoldText = true

        var pageNumber = 1
        var yPosition = 50
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        // Title
        canvas.drawText("Stock Ledger Report", 200f, yPosition.toFloat(), titlePaint)
        yPosition += 40

        // Headers
        paint.textSize = 14f
        canvas.drawText("Date", 50f, yPosition.toFloat(), paint)
        canvas.drawText("PARTICULAR", 200f, yPosition.toFloat(), paint)
        canvas.drawText("RECEIVED", 350f, yPosition.toFloat(), paint)
        canvas.drawText("GIVEN", 450f, yPosition.toFloat(), paint)
        canvas.drawText("BALQ", 550f, yPosition.toFloat(), paint)
        yPosition += 30

        // Loop through ledger entries
        var balance = 0.0  // running balance

        for (entry in latestLedgerEntries) {
            if (yPosition > 800) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50
            }

            val date = entry.dt ?: "-"
            val product = entry.status ?: "-"
            val givenQty = entry.givenQty ?: 0.0
            val receivedQty = entry.receivedQty ?: 0.0
            // val amt = entry.amount ?: 0.0

            // update balance (Received - Given)
            balance += receivedQty - givenQty

            // Convert to strings
            val qtyGivenStr = givenQty.toInt().toString()
            val qtyReceivedStr = receivedQty.toInt().toString()
            // val amtStr = amt.toString()
            val balanceStr = balance.toInt().toString()

            // Draw text (adjusted X so columns don’t overlap)
            canvas.drawText(date, 50f, yPosition.toFloat(), paint)         // Date
            canvas.drawText(product, 200f, yPosition.toFloat(), paint)     // Product
            canvas.drawText(qtyGivenStr, 350f, yPosition.toFloat(), paint) // Given
            canvas.drawText(qtyReceivedStr, 450f, yPosition.toFloat(), paint) // Received
            canvas.drawText(balanceStr, 550f, yPosition.toFloat(), paint)  // Balance
            //   canvas.drawText(amtStr, 470f, yPosition.toFloat(), paint)      // Amount

            yPosition += 25
        }

        pdfDocument.finishPage(page)

        // Save PDF file
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "LedgerReport.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            // Share via WhatsApp
//            val uri = try {
//                FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)
//            } catch (iae: IllegalArgumentException) {
//                Toast.makeText(this, "FileProvider error — add provider to manifest and file_paths.xml", Toast.LENGTH_LONG).show()
//                return
//            }
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )


            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                setPackage("com.whatsapp") // Ensures only WhatsApp opens
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(shareIntent)

        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error: ${e.message}")
        }
    }

    private fun fetchCustomerLedgerDetails(
        entity: ProductEntity,
        editProduct: EditproductlayoutBinding
    ) {
        val token = getToken(this) ?: return showToast("Token missing!")
        Utils.showLoading(this)
        val customerId = entity.id  // assuming your `stock` has productId or customerId
        if (customerId == -1) return showToast("Invalid product/customer")

        val request = ProductRequest(
            productId = customerId,
            startDt = editProduct.etStartDate.text.toString(),
            endDt = editProduct.etendDate.text.toString()
        )

        RetrofitClient.instance.getProduct("Bearer $token", customerId, request)
            .enqueue(object : Callback<ProductResponse> {
                override fun onResponse(
                    call: Call<ProductResponse>,
                    response: Response<ProductResponse>
                ) {
                    Utils.hideLoading()
                    if (!isFinishing && response.isSuccessful) {
                        val result = response.body()
                        val latestLedgerEntries = result?.entities.orEmpty()

                        // 🔹 Show results in dialog RecyclerView
                        val adapter = StockAdapter1(latestLedgerEntries)
                        editProduct.recyclerLedger1.adapter = adapter

                        // Optional: Show other info in dialog
                        // editProduct.tvBalance.text = "Balance: ₹${result?.balance ?: 0}"
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

    private fun fetchCustomerLedgerDetails() {
        val token = getToken(this) ?: return showToast("Token missing!")
        Utils.showLoading(this)
        val customerNameInput = binding.txtCategory.text.toString().trim()
        val customerId = nameToIdMap[customerNameInput.uppercase()]?.toIntOrNull() ?: -1
        if (customerId == -1) return showToast("Please select a valid customer")

        val request = ProductRequest(
            productId = customerId,


            startDt = binding.etStartDate.text.toString(),
            endDt = binding.etendDate.text.toString()
        )

        RetrofitClient.instance.getProduct("Bearer $token", customerId, request)
            .enqueue(object : Callback<ProductResponse> {
                override fun onResponse(
                    call: Call<ProductResponse>,
                    response: Response<ProductResponse>
                ) {
                    Utils.hideLoading()
                    if (!isFinishing && response.isSuccessful) {
                        val result = response.body()
                        latestLedgerEntries = result?.entities.orEmpty()
                        stockAdapter1 = StockAdapter1(latestLedgerEntries)
                        binding.recyclerLedger.adapter = stockAdapter1
                        // calculateTotals(latestLedgerEntries)

                        //   binding.tvMobile.text = "Mobile: ${result?.customermobile ?: "N/A"}"
                        //   binding.tvBalance.text = "Balance: ₹${result?.balance ?: 0}"
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


    private fun fetchCustomerList() {
        val token = getToken(this) ?: return showToast("Token missing!")

        RetrofitClient.instance.getProducts("Bearer $token")
            .enqueue(object : Callback<List<Product>> {
                override fun onResponse(
                    call: Call<List<Product>>,
                    response: Response<List<Product>>
                ) {
                  //  Utils.hideLoading()
                    if (!isFinishing && response.isSuccessful) {
                        val customerList = response.body().orEmpty()

                        // Offload mapping to background thread for performance
                        GlobalScope.launch(Dispatchers.Default) {
                            val customerNames = customerList.mapNotNull { customer ->
                                val name = customer.name ?: return@mapNotNull null
                                nameToIdMap[name.uppercase()] = customer.id.toString()
                                name
                            }

                            // Back to main thread for UI updates
                            withContext(Dispatchers.Main) {
                                val adapter = ArrayAdapter(
                                    this@StockActivity,
                                    android.R.layout.simple_dropdown_item_1line,
                                    customerNames
                                )
                                binding.txtCategory.setAdapter(adapter)
                                binding.txtCategory.threshold = 1
                            }
                        }
                    } else {
                        Log.e(
                            "CustomerList",
                            "Error: ${response.code()} ${response.errorBody()?.string()}"
                        )
                        showToast("Failed to load customers")
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                   // Utils.hideLoading()
                    if (!isFinishing) showToast("Error: ${t.message}")
                }
            })
    }

    private fun getToken(context: Context): String? =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).getString("token", null)

    private fun showToast(msg: String) =
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }

//    override fun onItemClick(productEntity: ProductEntity) {
//
//        onEditButtonClicked(productEntity)
//        Toast.makeText(this, "Clicked: ${productEntity.productName}", Toast.LENGTH_SHORT).show()
//    }

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
}