package px.corp.enterprisenew

import Adapers.AccountAdapter
import Adapers.ProductAdapter
import Adapers.StockAdapter
import Adapers.StockAdapter1
import Model.Account
import Model.AccountEntity
import Model.AccountRequest
import Model.AccountResponse
import Model.Product
import Model.ProductEntity
import Model.ProductRequest
import Model.ProductResponse
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import px.corp.enterprisenew.databinding.ActivityAccountBinding
import px.corp.enterprisenew.databinding.ActivityStockBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private val nameToIdMap = mutableMapOf<String, String>()
    private var latestLedgerEntries: List<AccountEntity> = emptyList()
    private lateinit var accountAdapter: AccountAdapter
    private var openingBalance: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        accountAdapter = AccountAdapter(ArrayList())
        binding.recyclerLedger.layoutManager = LinearLayoutManager(this) // ✅ FIX
        binding.recyclerLedger.adapter = accountAdapter
        fetchAccountList()
        setupSearchButton()
        setupDatePickers()
        sendReport()
        //backtodashboard()
        setContentView(binding.root)

    }
//    private fun backtodashboard() {
//        binding.tbHomeFragment.setOnClickListener {
//            val intent = Intent(this, Dashboard::class.java)
//            intent.putExtra("OPEN_DRAWER", true)
//            intent.putExtra("HIGHLIGHT_ITEM", R.id.nav_account) // pass the menu id
//            startActivity(intent)
//            finish()
//        }
//    }

    private fun shareOnWhatsApp(entries: List<AccountEntity>, balance: Double) {
        // Create a PdfDocument
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var y = 50
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("📒 Account Ledger Report", 40f, y.toFloat(), paint)

        y += 40
        paint.textSize = 12f
        paint.isFakeBoldText = false

        entries.forEach { entry ->
            canvas.drawText("Date: ${entry.date}", 40f, y.toFloat(), paint)
            y += 20
            canvas.drawText("Particular: ${entry.particular}", 40f, y.toFloat(), paint)
            y += 20
            canvas.drawText("Given: ${entry.given}   Received: ${entry.received}", 40f, y.toFloat(), paint)
            y += 30
            canvas.drawText("------------------------------", 40f, y.toFloat(), paint)
            y += 30
        }

        canvas.drawText("Final Balance: $balance", 40f, y.toFloat(), paint)

        pdfDocument.finishPage(page)

        // Save file in app storage
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "LedgerReport.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error creating PDF!", Toast.LENGTH_SHORT).show()
            return
        } finally {
            pdfDocument.close()
        }

        // Share via WhatsApp
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.whatsapp") // restrict to WhatsApp
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed!", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendReport() {
        binding.btnExportPdf.setOnClickListener {
            if (latestLedgerEntries.isEmpty()) {
                showToast("No ledger data to share")
                return@setOnClickListener
            }
            shareOnWhatsApp(latestLedgerEntries, openingBalance)
        }
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
                fetchAccountLedgerDetails()
            }
        }
    }
    private fun fetchAccountLedgerDetails() {
        val token = getToken(this) ?: return showToast("Token missing!")
        val customerNameInput = binding.txtCategory.text.toString().trim()
        val customerId = nameToIdMap[customerNameInput.uppercase()]?.toIntOrNull() ?: -1
        if (customerId == -1) return showToast("Please select a valid customer")

        val request = AccountRequest(
            accountId = customerId,


            startDt = binding.etStartDate.text.toString(),
            endDt = binding.etendDate.text.toString()
        )

        RetrofitClient.instance.getAccountData("Bearer $token", customerId, request)
            .enqueue(object : Callback<AccountResponse> {
                override fun onResponse(
                    call: Call<AccountResponse>,
                    response: Response<AccountResponse>
                ) {
                    if (!isFinishing && response.isSuccessful) {
                        val result = response.body()
                        latestLedgerEntries = result?.entities.orEmpty()
                        accountAdapter = AccountAdapter(latestLedgerEntries)
                        binding.recyclerLedger.adapter = accountAdapter
                        // calculateTotals(latestLedgerEntries)

                        //   binding.tvMobile.text = "Mobile: ${result?.customermobile ?: "N/A"}"
                        //   binding.tvBalance.text = "Balance: ₹${result?.balance ?: 0}"
                    } else {
                        showToast("Ledger fetch failed")
                    }
                }

                override fun onFailure(call: Call<AccountResponse>, t: Throwable) {
                    if (!isFinishing) showToast("Network error: ${t.message}")
                }
            })
    }

    private fun fetchAccountList() {
        val token = getToken(this) ?: return showToast("Token missing!")

        RetrofitClient.instance.getAccount("Bearer $token")
            .enqueue(object : Callback<List<Account>> {
                override fun onResponse(
                    call: Call<List<Account>>,
                    response: Response<List<Account>>
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
                                    this@AccountActivity,
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

                override fun onFailure(call: Call<List<Account>>, t: Throwable) {
                    if (!isFinishing) showToast("Error: ${t.message}")
                }
            })
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun getToken(context: Context): String? =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).getString("token", null)
}


