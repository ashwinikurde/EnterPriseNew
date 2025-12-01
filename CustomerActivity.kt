package px.corp.enterprisenew

import Adapers.LedgerAdapter
import Adapers.OutstandingAdapter
import Model.Customer
import Model.Outsatnding
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import px.corp.enterprisenew.databinding.ActivityCustomerBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding
    private var ledgerAdapter: LedgerAdapter? = null
    private var outstandingAdapter: OutstandingAdapter? = null

    private var allCustomers: List<Outsatnding> = emptyList()
    private var stockList: MutableList<Outsatnding> = mutableListOf()
    private var latestLedgerEntries: List<TransactionEntity> = emptyList()

    private val nameToIdMap = mutableMapOf<String, String>()
    private val nameToMobileMap = mutableMapOf<String, String>()
    private val nameToAddressMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerViews
        binding.recyclerLedger.layoutManager = LinearLayoutManager(this)
        binding.recyclerOutstanding.layoutManager = LinearLayoutManager(this)

        ledgerAdapter = LedgerAdapter(emptyList())
        binding.recyclerLedger.adapter = ledgerAdapter

        outstandingAdapter = OutstandingAdapter(emptyList())
        binding.recyclerOutstanding.adapter = outstandingAdapter

        fetchCustomerList()
        setupSearch()
        setupPdfExport()
        setupTabs()
        setStatusBarColor()
        setupDatePickers()
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

    /** ---------------- UI SETUP ---------------- */
//    private fun setupTabs() {
//        binding.tabLedgerDate.setOnClickListener {
//            binding.tabLedgerDate.setTextColor(getColor(R.color.blue))
//            binding.tabOutstanding.setTextColor(getColor(R.color.blue))
//            binding.layoutLedgerDate.visibility = View.VISIBLE
//            binding.layoutOutstanding.visibility = View.GONE
//        }
//
//        binding.tabOutstanding.setOnClickListener {
//            binding.tabOutstanding.setTextColor(getColor(R.color.blue))
//            binding.tabLedgerDate.setTextColor(getColor(R.color.blue))
//            binding.layoutOutstanding.visibility = View.VISIBLE
//            binding.layoutLedgerDate.visibility = View.GONE
//            loadStockData()
//        }
//    }
    private fun setupTabs() {
        binding.tabLedgerDate.setOnClickListener {
            showLedgerLayout()

        }

        binding.tabOutstanding.setOnClickListener {
            showOutstandingLayout()

        }
    }




    private fun showLedgerLayout() {
        binding.layoutLedgerDate.visibility = View.VISIBLE
        binding.layoutOutstanding.visibility = View.GONE

        // Highlight selected tab
        binding.tabLedgerDate.setTextColor(ContextCompat.getColor(this, R.color.blue))
        binding.tabLedgerDate.setTypeface(null, Typeface.BOLD)

        binding.tabOutstanding.setTextColor(ContextCompat.getColor(this, R.color.blue))
        binding.tabOutstanding.setTypeface(null, Typeface.NORMAL)
    }




    private fun showOutstandingLayout() {
        binding.layoutLedgerDate.visibility = View.GONE
        binding.layoutOutstanding.visibility = View.VISIBLE

        // Highlight selected tab
        binding.tabOutstanding.setTextColor(ContextCompat.getColor(this, R.color.blue))
        binding.tabOutstanding.setTypeface(null, Typeface.BOLD)

        binding.tabLedgerDate.setTextColor(ContextCompat.getColor(this, R.color.blue))
        binding.tabLedgerDate.setTypeface(null, Typeface.NORMAL)

        // Only load if list is empty
        if (allCustomers.isEmpty()) {
            loadStockData()
        } else {
            outstandingAdapter?.updateList(allCustomers)
            binding.tvTotalOutstanding.text = "Total Balance: %.2f".format(allCustomers.sumOf { it.balance })
        }
    }

    private fun backtodashboard() {
        binding.tbHomeFragment.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.putExtra("OPEN_DRAWER", true)
            intent.putExtra("HIGHLIGHT_ITEM", R.id.nav_customer) // pass the menu id
            startActivity(intent)
            finish()
        }
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

    private fun setupSearch() {
        binding.etSearchName.addTextChangedListener { text ->
            lifecycleScope.launch(Dispatchers.Default) {
                val query = text?.toString()?.lowercase() ?: ""
                val filtered = if (query.isEmpty()) allCustomers
                else allCustomers.filter { it.customerName.lowercase().contains(query) }

                val total = filtered.sumOf { it.balance }

                withContext(Dispatchers.Main) {
                    outstandingAdapter?.updateList(filtered)
                    binding.tvTotalOutstanding.text = "Total Balance: %.2f".format(total)
                }
            }
        }
    }

//    private fun searchProductsNew() {
//        val filter = FilteringProducts(outstandingAdapter, stockList)
//        binding.etSearchName.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun afterTextChanged(s: Editable?) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                filter.filter(s.toString().trim())
//            }
//        })
//    }

    private fun setupPdfExport() {
        binding.btnOutstandingPdf.setOnClickListener {
            if (latestLedgerEntries.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    generateAndSharePdf(latestLedgerEntries)
                }
            } else {
                showToast("No data to export")
            }
        }
    }

    /** ---------------- API CALLS ---------------- */
    private fun fetchCustomerList() {
        val token = getToken(this) ?: return showToast("Token missing!")

        RetrofitClient.instance.getCustomers("Bearer $token")
            .enqueue(object : Callback<List<Customer>> {
                override fun onResponse(
                    call: Call<List<Customer>>,
                    response: Response<List<Customer>>
                ) {
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
                                    this@CustomerActivity,
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

                override fun onFailure(call: Call<List<Customer>>, t: Throwable) {
                    if (!isFinishing) showToast("Error: ${t.message}")
                }
            })
    }

    private fun fetchCustomerLedgerDetails() {
        val token = getToken(this) ?: return showToast("Token missing!")
        val customerNameInput = binding.txtCategory.text.toString().trim()
        val customerId = nameToIdMap[customerNameInput.uppercase()]?.toIntOrNull() ?: -1
        if (customerId == -1) return showToast("Please select a valid customer")

        val request = CustomerLedgerRequest(
            id = customerId,
            name = customerNameInput,
            mobile = nameToMobileMap[customerNameInput.uppercase()] ?: "",
            address = nameToAddressMap[customerNameInput.uppercase()] ?: "-",
            startDate = binding.etStartDate.text.toString(),
            endDate = binding.etendDate.text.toString()
        )

        RetrofitClient.instance.getCustomerLedger("Bearer $token", customerId, request)
            .enqueue(object : Callback<CustomerLedgerResponse> {
                override fun onResponse(call: Call<CustomerLedgerResponse>, response: Response<CustomerLedgerResponse>) {
                    if (!isFinishing && response.isSuccessful) {
                        val result = response.body()
                        latestLedgerEntries = result?.entities.orEmpty()
                        ledgerAdapter = LedgerAdapter(latestLedgerEntries)
                        binding.recyclerLedger.adapter = ledgerAdapter
                        calculateTotals(latestLedgerEntries)

                        binding.tvMobile.text = "Mobile: ${result?.customermobile ?: "N/A"}"
                        binding.tvBalance.text = "Balance: ₹${result?.balance ?: 0}"
                    } else {
                        showToast("Ledger fetch failed")
                    }
                }

                override fun onFailure(call: Call<CustomerLedgerResponse>, t: Throwable) {
                    if (!isFinishing) showToast("Network error: ${t.message}")
                }
            })
    }

    private fun loadStockData() {
        val token = getToken(this) ?: return showToast("Token missing!")
        RetrofitClient.instance.getOutstanding("Bearer $token")
            .enqueue(object : Callback<List<Outsatnding>> {
                override fun onResponse(
                    call: Call<List<Outsatnding>>,
                    response: Response<List<Outsatnding>>
                ) {
                    if (!isFinishing && response.isSuccessful) {
                        GlobalScope.launch(Dispatchers.IO) {
                            val newList = response.body()?.toMutableList() ?: mutableListOf()

                            allCustomers = newList   // ✅ Save full list here

                            withContext(Dispatchers.Main) {
                                outstandingAdapter?.updateList(newList)
                                binding.tvTotalOutstanding.text =
                                    "Total Balance: %.2f".format(newList.sumOf { it.balance })
                                showToast("Outstanding list loaded")
                            }
                        }
                    } else {
                        showToast("Outstanding fetch failed")
                    }
                }

                override fun onFailure(call: Call<List<Outsatnding>>, t: Throwable) {
                    if (!isFinishing) showToast("Network error: ${t.message}")
                }
            })
    }


    /** ---------------- PDF EXPORT ---------------- */
    private suspend fun generateAndSharePdf(entries: List<TransactionEntity>) {
        withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            val titlePaint = Paint()

            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var y = 40
            titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            titlePaint.textSize = 16f
            canvas.drawText("Customer Ledger Report", 200f, y.toFloat(), titlePaint)

            y += 40
            paint.textSize = 12f
            paint.typeface = Typeface.DEFAULT_BOLD

            val headers = listOf("DATE", "GRN", "INV NO", "PARTICULAR", "GIVEN", "RECEIVED", "BALANCE")
            val x = listOf(10, 70, 130, 190, 330, 400, 470)

            headers.forEachIndexed { i, text -> canvas.drawText(text, x[i].toFloat(), y.toFloat(), paint) }
            y += 25
            paint.typeface = Typeface.DEFAULT

            var runningBalance = 0.0
            var totalGiven = 0.0
            var totalReceived = 0.0

            for (entry in entries) {
                if (y > 800) break
                val given = entry.given ?: 0.0
                val received = entry.received ?: 0.0
                totalGiven += given
                totalReceived += received
                runningBalance += received - given

                val row = listOf(
                    entry.transDate ?: "",
                    entry.grn ?: "",
                    entry.invNo ?: "",
                    entry.transactionType ?: "",
                    String.format("%.2f", given),
                    String.format("%.2f", received),
                    String.format("%.2f", runningBalance)
                )

                row.forEachIndexed { i, value -> canvas.drawText(value, x[i].toFloat(), y.toFloat(), paint) }
                y += 20
            }

            paint.typeface = Typeface.DEFAULT_BOLD
            y += 20
            canvas.drawText("TOTAL", 190f, y.toFloat(), paint)
            canvas.drawText(String.format("%.2f", totalGiven), 330f, y.toFloat(), paint)
            canvas.drawText(String.format("%.2f", totalReceived), 400f, y.toFloat(), paint)
            canvas.drawText(String.format("%.2f", runningBalance), 470f, y.toFloat(), paint)

            pdfDocument.finishPage(page)

            val file = File(getExternalFilesDir(null), "LedgerReport.pdf")
            try {
                pdfDocument.writeTo(FileOutputStream(file))
                withContext(Dispatchers.Main) { sharePdf(file) }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { showToast("PDF error: ${e.message}") }
            } finally {
                pdfDocument.close()
            }
        }
    }

    private fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Ledger PDF"))
    }

    /** ---------------- HELPERS ---------------- */
    private fun calculateTotals(entries: List<TransactionEntity>) {
        val totalGiven = entries.sumOf { it.given ?: 0.0 }
        val totalReceived = entries.sumOf { it.received ?: 0.0 }
        val totalBalance = totalReceived - totalGiven

        binding.tvTotalGiven.text = formatAmount(totalGiven)
        binding.tvTotalReceived.text = formatAmount(totalReceived)
        binding.tvTotalBalance.text = formatAmount(totalBalance)
    }

    private fun formatAmount(amount: Double) =
        "₹" + java.text.NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(amount)

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val selected = Calendar.getInstance().apply { set(y, m, d) }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            onDateSelected(dateFormat.format(selected.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun getToken(context: Context): String? =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).getString("token", null)

    private fun showToast(msg: String) =
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
}
