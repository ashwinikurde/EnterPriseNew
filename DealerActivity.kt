package px.corp.enterprisenew

import Adapers.AccountAdapter
import Adapers.DealerAdapter
import Model.Account
import Model.AccountEntity
import Model.AccountRequest
import Model.AccountResponse
import Model.DealerEntity
import Model.DealerId
import Model.DealerRequest
import Model.DealerResponse
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
import px.corp.enterprisenew.databinding.ActivityAccountBinding
import px.corp.enterprisenew.databinding.ActivityDealerBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DealerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDealerBinding
    private lateinit var accountAdapter: DealerAdapter
    private var latestLedgerEntries: List<DealerEntity> = emptyList()
    private val nameToIdMap = mutableMapOf<String, String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealerBinding.inflate(layoutInflater)
        accountAdapter = DealerAdapter(ArrayList())
        binding.recyclerLedger.layoutManager = LinearLayoutManager(this) // ✅ FIX
        binding.recyclerLedger.adapter = accountAdapter
        fetchAccountList()
        setupSearchButton()
        setupDatePickers()
        setContentView(binding.root)
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

        val request = DealerRequest(
            dealerId = customerId,


            startDt = binding.etStartDate.text.toString(),
            endDt = binding.etendDate.text.toString()
        )

        RetrofitClient.instance.getDealerData("Bearer $token", customerId, request)
            .enqueue(object : Callback<DealerResponse> {
                override fun onResponse(
                    call: Call<DealerResponse>,
                    response: Response<DealerResponse>
                ) {
                    if (!isFinishing && response.isSuccessful) {
                        val result = response.body()
                        latestLedgerEntries = result?.entities.orEmpty()
                        accountAdapter = DealerAdapter(latestLedgerEntries)
                        binding.recyclerLedger.adapter = accountAdapter
                        // calculateTotals(latestLedgerEntries)

                        //   binding.tvMobile.text = "Mobile: ${result?.customermobile ?: "N/A"}"
                        //   binding.tvBalance.text = "Balance: ₹${result?.balance ?: 0}"
                    } else {
                        showToast("Ledger fetch failed")
                    }
                }

                override fun onFailure(call: Call<DealerResponse>, t: Throwable) {
                    if (!isFinishing) showToast("Network error: ${t.message}")
                }
            })
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        binding.etStartDate.setText(today)
        binding.etendDate.setText(today)

        binding.etStartDate.setOnClickListener { showDatePicker { binding.etStartDate.setText(it) } }
        binding.etendDate.setOnClickListener { showDatePicker { binding.etendDate.setText(it) } }
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
    private fun fetchAccountList() {
        val token = getToken(this) ?: return showToast("Token missing!")

        RetrofitClient.instance.getDealerList("Bearer $token")
            .enqueue(object : Callback<List<DealerId>> {
                override fun onResponse(
                    call: Call<List<DealerId>>,
                    response: Response<List<DealerId>>
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
                                    this@DealerActivity,
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

                override fun onFailure(call: Call<List<DealerId>>, t: Throwable) {
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


