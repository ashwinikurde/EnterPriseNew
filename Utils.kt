package px.corp.enterprisenew

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth



object Utils {
    private var dailog: AlertDialog? = null

    //    fun showDialog(context: Context, message: String) {
//        val progress = ProgressbarBinding.inflate(LayoutInflater.from(context))
//        progress.txtMessage.text = message
//        dailog = AlertDialog.Builder(context).setView(progress.root).setCancelable(false).create()
//        dailog!!.show()
//    }
    fun getPhoneNumberFromFirebase(requireContext: Context): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.phoneNumber // This will return the phone number if the user is authenticated
    }

    fun hideDilag() {
        dailog?.dismiss()
    }

    fun getRandomId(): String {
        return (1..25).map { (('A'..'Z') + ('a'..'z') + ('0'..'9')).random() }.joinToString("")
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    private var firebaseAuthInstance: FirebaseAuth? = null
    fun getAuthInstance(): FirebaseAuth {
        if (firebaseAuthInstance == null) {
            firebaseAuthInstance = FirebaseAuth.getInstance()
        }
        return firebaseAuthInstance!!
    }
//    fun getCurrentUserId():String{
//        return FirebaseAuth.getInstance().currentUser!!.uid
//    }
fun getCurrentUserId1(context: Context): String? {
    val sharedPref = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
    return sharedPref.getString("userPhoneNumber", null)
}
    fun generateUidFromPhone(phone: String): String {
        return phone.filter { it.isDigit() } // Remove + and non-numeric chars
    }
//    fun getSavedUserPhone(context: Context): String? {
//        val sharedPref = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
//        return sharedPref.getString("userPhoneNumber", null)
//    }
fun saveSelectedUserId(context: Context, userId: String) {
    val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPref.edit().putString("selectedUserId", userId).apply()
}

    fun getSelectedUserId(context: Context): String? {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("selectedUserId", null)
    }



    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
     fun hideLoading() {
        dailog?.dismiss()
    }


    fun showLoading(context: Context) {
        if (dailog == null) {
            val linearLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(50, 50, 50, 50)
                gravity = Gravity.CENTER
            }

            val progressBar = ProgressBar(context).apply {
                isIndeterminate = true
            }

            val tvLoading = TextView(context).apply {
                text = "Loading..."
                setTextColor(Color.BLACK)
                textSize = 16f
                setPadding(30, 0, 0, 0)
            }

            linearLayout.addView(progressBar)
            linearLayout.addView(tvLoading)

            dailog = AlertDialog.Builder(context)
                .setView(linearLayout)
                .setCancelable(false)
                .create()
        }

        dailog?.show()
    }


}

