package px.corp.enterprisenew

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import px.corp.enterprisenew.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase!!.getReference("users")
        binding.regButton.setOnClickListener {
            registerUser()
        }
    }
    fun registerUser() {

        if (!validateEmail() || !validatePhoneNo() || !validatePassword()) return

        val username = binding.userNameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val phone = binding.phoneNoEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        val firebaseHelper = FirebaseHelper(username, email, phone, password)

        // Save to Firebase under /users/username
        databaseReference?.child(username)?.setValue(firebaseHelper)
            ?.addOnSuccessListener {
                Toast.makeText(this, "Your Account is created Successfully", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener {
                Toast.makeText(this, "Failed to create account: ${it.message}", Toast.LENGTH_LONG).show()
            }

    }
    private fun validateEmail(): Boolean {
        val emailInput: String = binding.emailEditText.text.toString()
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()

        return if (emailInput.isEmpty()) {
            binding.emailEditText.error = "Field cannot be empty"
            false
        } else if (!emailInput.matches(emailPattern)) {
            binding.emailEditText.error = "Invalid email address"
            false
        } else {
            binding.emailEditText.error = null
         //   binding.email.isErrorEnabled = false
            true
        }
    }
    private fun validatePhoneNo(): Boolean {
        val `val`: String = binding.phoneNoEditText.text.toString()
        return if (`val`.isEmpty()) {
            binding.phoneNoEditText.setError("Field cannot be empty")
            false
        } else {
            binding.phoneNoEditText.setError(null)
            //binding.phoneNo.setErrorEnabled(false)
            true
        }
    }
    private fun validatePassword(): Boolean {
        val `val`: String = binding.passwordEditText.text.toString()
        val passwordVal = "^" +  //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +  //any letter
                "(?=.*[@#$%^&+=])" +  //at least 1 special character
                "(?=\\S+$)" +  //no white spaces
                ".{4,}" +  //at least 4 characters
                "$"
        return if (`val`.isEmpty()) {
            binding.passwordEditText.setError("Field cannot be empty")
            false
            //   } else if (!val.matches(passwordVal)) {
            //  regPassword.setError("Password is too weak");
            //    return false;
        } else {
            binding.passwordEditText.setError(null)
         //   binding.password.setErrorEnabled(false)
            true
        }
    }
}