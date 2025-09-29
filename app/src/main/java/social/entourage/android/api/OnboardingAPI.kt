package social.entourage.android.api

import android.annotation.SuppressLint
import android.util.Log
import androidx.collection.ArrayMap
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.api.model.User
import social.entourage.android.api.request.*
import social.entourage.android.authentication.AuthenticationController
import timber.log.Timber
import java.io.File
import java.io.IOException
import kotlin.collections.set

/**
 * Created by Jr on 11/05/2020.
 */
class OnboardingAPI {

    private val authenticationController:AuthenticationController = EntourageApplication.get().authenticationController

    private val onboardingService : UserRequest
        get() =  EntourageApplication.get().apiModule.userRequest //service ?: retrofit!!.create(UserRequest::class.java)

    private val loginService : LoginRequest
        get() = EntourageApplication.get().apiModule.loginRequest //retrofit!!.create(LoginRequest::class.java)

    /**********************
     * Create user
     */
    fun createUser(tempUser: User, hasConsent: Boolean, listener: (isOK: Boolean, error: String?) -> Unit) {
        val user: MutableMap<String, Any> = ArrayMap()
        user["phone"] = tempUser.phone ?: ""
        user["first_name"] = tempUser.firstName ?: ""
        user["last_name"] = tempUser.lastName ?: ""
        user["email"] = tempUser.email ?: ""
        user["newsletter_subscription"] = hasConsent

        tempUser.gender?.let { user["gender"] = it }
        tempUser.birthday?.let { user["birthdate"] = it } // ✅ birthdate (clé API)

        val request = ArrayMap<String, Any>()
        request["user"] = user

        val call = onboardingService.registerUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Timber.d("Response ok create user ?")
                    listener(true, null)
                } else {
                    val errorString = response.errorBody()?.string()
                    Timber.d("Response nok create user error : $errorString")
                    listener(false, errorString)
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                listener(false, null)
            }
        })
    }


    /**********************
     * Login
     */
    fun login(phoneNumber: String, smsCode: String, listener: (isOK: Boolean, loginResponse: LoginResponse?, error: String?) -> Unit) {
        loginService.login(LoginWrapper(phoneNumber, smsCode))
                .enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    response.body()?.user?.let {
                        authenticationController.saveUser(it)
                        authenticationController.saveUserPhoneAndCode(phoneNumber, smsCode)
                    }

                    listener(true,response.body(),null)
                } else {
                    val errorString = response.errorBody()?.string()
                    listener(false,null,errorString)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                listener(false,null,null)
            }
        })
    }

    /**********************
     * Send code
     */
    fun requestNewCode(phone:String,listener:(isOK:Boolean, loginResponse: UserResponse?, error:String?) -> Unit) {
        val user: MutableMap<String, String> = ArrayMap()
        user["phone"] = phone

        val code: MutableMap<String, String> = ArrayMap()
        code["action"] = "regenerate"

        val request = ArrayMap<String, Any>()
        request["user"] = user
        request["code"] = code

        val call: Call<UserResponse> = onboardingService.regenerateSecretCode(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    listener(true,response.body(),null)
                } else {
                    val error = ApiError.fromResponse(response)
                    listener(false,null,error.code)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                listener(false,null,null)
            }
        })
    }

    /**********************
     * user
     */
    fun updateAddress(userAddress: User.Address, isSecondary:Boolean, listener:(isOK:Boolean,userResponse: UserResponse?) -> Unit) {

        val address: MutableMap<String, Any> = ArrayMap()
        if (userAddress.googlePlaceId.isNullOrEmpty()) {
            address["latitude"] = userAddress.latitude
            address["longitude"] = userAddress.longitude
            address["place_name"] = userAddress.displayAddress
        }
        else {
            userAddress.googlePlaceId?.let { address["google_place_id"] = it }
        }
        val request = ArrayMap<String, Any>()
        request["address"] = address

        val call:Call<UserResponse> = if (!isSecondary) {
            onboardingService.updatePrimaryAddressLocation(request)
        }
        else {
            onboardingService.updateSecondaryAddressLocation(request)
        }

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                } else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun updateUser(email:String?, listener:(isOK:Boolean, userResponse: UserResponse?) -> Unit) {

        val user = ArrayMap<String, Any>()
        if (email != null) {
            user["email"] = email
        }
        else {
            listener(false,null)
            return
        }

        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = onboardingService.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                }
                else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun updateUserGoal(
        goalString: String,
        email: String?,
        hasConsent: Boolean?,
        gender: String?,
        birthday: String?,          // yyyy-MM-dd
        discoverySource: String?,   // clé ("entreprise", ...)
        companyId: String?,         // ID entreprise
        eventId: String?,           // ID event
        listener: (isOK: Boolean, userResponse: UserResponse?) -> Unit
    ) {
        val user = ArrayMap<String, Any>()
        user["goal"] = goalString

        hasConsent?.let { user["newsletter_subscription"] = it }
        email?.takeIf { it.isNotEmpty() }?.let { user["email"] = it }
        gender?.takeIf { it.isNotEmpty() }?.let { user["gender"] = it }
        birthday?.takeIf { it.isNotEmpty() }?.let { user["birthdate"] = it } // ✅ birthdate

        discoverySource?.takeIf { it.isNotEmpty() }?.let { user["discovery_source"] = it }
        companyId?.takeIf { it.isNotBlank() }?.let { user["company_id"] = it } // ✅ IDs
        eventId?.takeIf { it.isNotBlank() }?.let { user["event_id"] = it }     // ✅ IDs

        val request = ArrayMap<String, Any>()
        request["user"] = user

        val call = onboardingService.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    listener(true, response.body())
                } else {
                    listener(false, null)
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                listener(false, null)
            }
        })
    }







    fun updateUserPhoto(avatarKey:String, listener: (isOK: Boolean, userResponse: UserResponse?) -> Unit) {
        val user = ArrayMap<String, Any>()
        user["avatar_key"] = avatarKey
        val request = ArrayMap<String, Any>()
        request["user"] = user

        val call = onboardingService.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                } else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun updateUserNames(firstname:String,lastname:String, listener:(isOK:Boolean, userResponse: UserResponse?) -> Unit) {
        val user = ArrayMap<String, Any>()
        user["first_name"] = firstname
        user["last_name"] = lastname

        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = onboardingService.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                }
                else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun getUser(userId:Int, listener:(isOK:Boolean,userResponse: UserResponse?) -> Unit) {

        val call = onboardingService.getUser(userId.toString())
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                } else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    /**********************
     * Upload Photo
     */
    fun uploadPhotoFile(presignedUrl: String,file:File,listener: (isOk:Boolean) -> Unit) {
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val request = okhttp3.Request.Builder()
                .url(presignedUrl)
                .put(requestBody)
                .build()

        EntourageApplication.get().apiModule.okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                listener(false)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                listener(true)
            }
        })
    }

    /***********
     * Change phone
     */
    fun changePhone(oldPhone:String, newPhone:String,email:String,listener:(isOK:Int) -> Unit) {

        val user: MutableMap<String, String> = ArrayMap()
        user["current_phone"] = oldPhone
        user["requested_phone"] = newPhone
        user["email"] = email

        val request = ArrayMap<String, Any>()
        request["user"] = user

        onboardingService.changePhone(request)
                .enqueue(object : Callback<ResponseBody>{
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            listener(R.string.login_change_phone_send_ok)
                        }
                        else {
                            checkPhoneChangeError(response.errorBody()?.string() ?:"", listener)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        checkPhoneChangeError("", listener)
                    }
                })
    }

    private fun checkPhoneChangeError(errorBody: String,listener:(isOK:Int) -> Unit) {
        listener(when {
            errorBody.contains("USER_NOT_FOUND") -> R.string.login_change_error_number_not_found
            errorBody.contains("USER_DELETED")  -> R.string.login_change_error_number_deleted
            errorBody.contains("USER_BLOCKED")  -> R.string.login_change_error_number_blocked
            errorBody.contains("IDENTICAL_PHONES")  -> R.string.login_change_error_identical_number
            else -> R.string.login_change_error_generic
            }
        )
    }

    /**********************
     * Onboarding Asso
     */
    fun updateAssoInfos(asso: Partner?, listener:(isOK:Boolean, response:ResponseBody?) -> Unit) {

        if (asso == null) {
            listener(false,null)
            return
        }

        val request = ArrayMap<String, Any>()
        if (asso.id > 0) {
            request["partner_id"] =asso.id
        }

        if (asso.isCreation) {
            request["new_partner_name"] = asso.name
        }

        request["postal_code"] = asso.postalCode
        request["partner_role_title"] = asso.userRoleTitle

        val call = onboardingService.updateAssoInfos(request)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                }
                else {
                    listener(false,null)
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun updateUserInterests(interest:ArrayList<String>, listener:(isOK:Boolean, userResponse: UserResponse?) -> Unit) {

        val user = ArrayMap<String, Any>()
        user["interests"] = interest

        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = onboardingService.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                }
                else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun getAssociationsList(listener:(arrayAssociations:ArrayList<Partner>?) -> Unit) {
        val request = EntourageApplication.get().apiModule.partnerRequest
        request.allPartners.enqueue(object : Callback<PartnersResponse> {
            override fun onResponse(call: Call<PartnersResponse>, response: Response<PartnersResponse>) {
                if (response.isSuccessful) {
                    val arrayPartners = ArrayList<Partner>()
                    response.body()?.partners?.let { arrayPartners.addAll(it)}
                    listener(arrayPartners)
                }
                else {
                    listener(null)
                }
            }
            override fun onFailure(call: Call<PartnersResponse>, t: Throwable) { listener(null) }
        })
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: OnboardingAPI? = null

        @Synchronized
        fun getInstance(): OnboardingAPI {
            return instance ?: OnboardingAPI().also { instance = it}
        }
    }
}