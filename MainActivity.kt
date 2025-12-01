package px.corp.enterprisenew

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import px.corp.enterprisenew.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onSignup()
        isUser()
        setStatusBarColor()
       // istoken()

    }
    private fun setStatusBarColor() {
        val statusBarColor = ContextCompat.getColor(this, R.color.white)
        window.statusBarColor = statusBarColor

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
    fun onSignup() {
        binding.signupScreen.setOnClickListener {
            val intent = Intent(this@MainActivity, SignUp::class.java)

            // Only add non-null pairs
            val pairs = arrayOf(
                Pair(binding.txtUsername as View, "username_tran"),
                Pair(binding.password as View, "password_tran"),
                Pair(binding.loginBtn as View, "button_tran"),
                Pair(binding.signupScreen as View, "login_signup_tran")
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val options =
                    ActivityOptions.makeSceneTransitionAnimation(this@MainActivity, *pairs)
                startActivity(intent, options.toBundle())
            } else {
                // Fallback for older Android versions
                startActivity(intent)
            }
        }
    }

    fun saveToken(token: String, context: Context) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("token", null)
    }

    fun apiLogin(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)

        RetrofitClient.instance.login(loginRequest)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        if (!token.isNullOrEmpty()) {
                            Toast.makeText(this@MainActivity, "Login Success ✅", Toast.LENGTH_SHORT).show()
                            saveToken(token, this@MainActivity)
                            val intent = Intent(this@MainActivity, Dashboard::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@MainActivity, "No token in response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("API_ERROR", response.errorBody()?.string() ?: "Unknown error")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun isUser() {
        binding.loginBtn.setOnClickListener {
            val userEnteredUsername = binding.txtUsername.text.toString().trim()
            val userEnteredPassword = binding.password.text.toString().trim()

            val reference = FirebaseDatabase.getInstance().getReference("users")
            val checkUser = reference.orderByChild("username").equalTo(userEnteredUsername)

            checkUser.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val passwordFromDB =
                                userSnapshot.child("password").getValue(String::class.java)

                            if (passwordFromDB == userEnteredPassword) {
                                // ✅ Firebase login successful
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Login Successful",
//                                    Toast.LENGTH_SHORT
//                                ).show()

                                // 🔐 Now call API login (and pass username/password)
                                apiLogin(userEnteredUsername,userEnteredPassword)
                            } else {
                                binding.password.error = "Wrong Password"
                                binding.password.requestFocus()
                            }
                        }
                    } else {
                        binding.txtUsername.error = "No such user exists"
                        binding.txtUsername.requestFocus()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        applicationContext,
                        "Database error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

}





