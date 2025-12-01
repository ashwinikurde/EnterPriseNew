package px.corp.enterprisenew

class FirebaseHelper {

    var username: String? = null
    var email: String? = null
    var phoneNo: String? = null
    var password: String? = null

    constructor() {}
    constructor(

        username: String?,
        email: String?,
        phoneNo: String?,
        password: String?
    ) {

        this.username = username
        this.email = email
        this.phoneNo = phoneNo
        this.password = password
    }
}