package px.corp.enterprisenew

import com.google.gson.annotations.SerializedName

data class LoginRequest(

@SerializedName("uName") val uName: String,
@SerializedName("upassword") val upassword: String
)


