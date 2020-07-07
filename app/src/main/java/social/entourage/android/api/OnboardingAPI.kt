package social.entourage.android.api

import android.annotation.SuppressLint
import androidx.collection.ArrayMap
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.api.model.Partner
import social.entourage.android.api.model.Partner.PartnersWrapper
import social.entourage.android.api.model.User
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.authentication.AuthenticationInterceptor
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

/**
 * Created by Jr on 11/05/2020.
 */
class OnboardingAPI(val application: EntourageApplication) {

    private var service:UserRequest? = null
    private var retrofit:Retrofit? = null
    private lateinit var authenticationController:AuthenticationController

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: OnboardingAPI? = null

        @Synchronized
        fun getInstance(application: EntourageApplication): OnboardingAPI {
            if (instance == null) {
                instance = OnboardingAPI(application)
            }
            instance?.authenticationController = application.entourageComponent.authenticationController
            Timber.d("On create Dagger ? ${instance?.authenticationController}")
            instance?.setupRetrofit()

            return instance!!
        }
    }

    /*****************
     * Setups
     */
    private fun setupRetrofit() {
        if (retrofit != null) return

        val gson = GsonBuilder()
                .addSerializationExclusionStrategy(object : ExclusionStrategy {
                    override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                        val expose = fieldAttributes.getAnnotation(Expose::class.java)
                        return expose != null && !expose.serialize
                    }

                    override fun shouldSkipClass(aClass: Class<*>?): Boolean {
                        return false
                    }
                }).addDeserializationExclusionStrategy(object : ExclusionStrategy {
                    override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                        val expose = fieldAttributes.getAnnotation(Expose::class.java)
                        return expose != null && !expose.deserialize
                    }

                    override fun shouldSkipClass(aClass: Class<*>?): Boolean {
                        return false
                    }
                })
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .registerTypeAdapter(NewsfeedItem::class.java, NewsfeedItem.NewsfeedItemJsonAdapter())
                .create()

        retrofit = Retrofit.Builder().baseUrl(BuildConfig.ENTOURAGE_URL)
                .client(providesOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
    }

    private fun providesOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()

        builder.addInterceptor(AuthenticationInterceptor)
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)
        }
        return builder.build()
    }

    private fun getOnboardingService() : UserRequest {
        if (service != null) return service!!
        return retrofit!!.create(UserRequest::class.java)
    }

    private fun getLoginService() : LoginRequest {
        return retrofit!!.create(LoginRequest::class.java)
    }

    /**********************
     * Create user
     */
    fun createUser(tempUser: User,listener:(isOK:Boolean,error:String?) -> Unit) {

        val user: MutableMap<String, String> = ArrayMap()
        user["phone"] = tempUser.phone
        user["first_name"] = tempUser.firstName
        user["last_name"] = tempUser.lastName

        val request = ArrayMap<String, Any>()
        request["user"] = user

        val call = getOnboardingService().registerUser(request)

        call.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    Timber.d("Response ok create user ?")
                    listener(true,null)
                } else {
                    Timber.d("Response nok create user")
                    if (response.errorBody() != null) {
                        val errorString = response.errorBody()?.string()
                        Timber.d("Response nok create user error : $errorString")
                        listener(false,errorString)
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    /**********************
     * Login
     */
    fun login(phoneNumber:String?,smsCode:String,listener:(isOK:Boolean,loginREsponse:LoginResponse?,error:String?) -> Unit) {
        if (phoneNumber != null) {
            val user = HashMap<String, String>()
            user["phone"] = phoneNumber
            user["sms_code"] = smsCode

            val call: Call<LoginResponse> = getLoginService().login(user)
            call.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        authenticationController.saveUser(response.body()!!.user)
                        authenticationController.saveUserPhoneAndCode(phoneNumber, smsCode)
                        authenticationController.saveUserToursOnly(false)

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
        } else {
            listener(false,null,"INVALID_PHONE_FORMAT")
        }
    }

    /**********************
     * Send code
     */
    fun resendCode(phone:String,listener:(isOK:Boolean,loginResponse:UserResponse?,error:String?) -> Unit) {
        val user: MutableMap<String, String> = ArrayMap()
        user["phone"] = phone

        val code: MutableMap<String, String> = ArrayMap()
        code["action"] = "regenerate"

        val request = ArrayMap<String, Any>()
        request["user"] = user
        request["code"] = code

        val call: Call<UserResponse> = getOnboardingService().regenerateSecretCode(request)
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
    fun updateAddress(userAddress: User.Address, isSecondary:Boolean, listener:(isOK:Boolean,userResponse:UserResponse?) -> Unit) {

        val call:Call<UserResponse>
        val address: MutableMap<String, Any> = ArrayMap()
        if (userAddress.googlePlaceId.isNullOrEmpty()) {
            address["latitude"] = userAddress.latitude
            address["longitude"] = userAddress.longitude
            address["place_name"] = userAddress.displayAddress
        }
        else {
            address["google_place_id"] = userAddress.googlePlaceId
        }
        val request = ArrayMap<String, Any>()
        request["address"] = address

        if (!isSecondary) {
            call = getOnboardingService().updatePrimaryAddressLocation(request)
        }
        else {
            call = getOnboardingService().updateSecondaryAddressLocation(request)
        }

        call.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                } else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun updateUser(email:String?, listener:(isOK:Boolean, userResponse:UserResponse?) -> Unit) {

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
        val call = getOnboardingService().updateUser(request)
        call.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                }
                else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun updateUserGoal(goalString:String, listener:(isOK:Boolean, userResponse:UserResponse?) -> Unit) {

        val user = ArrayMap<String, Any>()
        user["goal"] = goalString

        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = getOnboardingService().updateUser(request)
        call.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                }
                else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun updateUserPhoto(avatarKey:String, listener: (isOK: Boolean, userResponse:UserResponse?) -> Unit) {
        val user = ArrayMap<String, Any>()
        user["avatar_key"] = avatarKey
        val request = ArrayMap<String, Any>()
        request["user"] = user

        val call = getOnboardingService().updateUser(request)
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

    fun getUser(userId:Int, listener:(isOK:Boolean,userResponse:UserResponse?) -> Unit) {

        val call = getOnboardingService().getUser(userId)

        call.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                } else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    /**********************
     * Upload Photo
     */
    fun prepareUploadPhoto(listener: (avatarKey:String?, presignedUrl:String?, error: String?) -> Unit) {
        val request = Request("image/jpeg")
        val call = getOnboardingService().prepareAvatarUpload(request)

        call.enqueue(object : Callback<social.entourage.android.api.Response?> {
            override fun onResponse(call: Call<social.entourage.android.api.Response?>, response: Response<social.entourage.android.api.Response?>) {
                if (response.isSuccessful) {
                    listener(response.body()?.avatarKey, response.body()?.presignedUrl, null)
                }
                else {

                    listener(null,null,null)
                }
            }

            override fun onFailure(call: Call<social.entourage.android.api.Response?>, t: Throwable) {
                listener(null,null,null)
            }
        })
    }

    fun uploadPhotoFile(presignedUrl: String,file:File,listener: (isOk:Boolean) -> Unit) {
        val client = application.entourageComponent.okHttpClient
        val mediaType = MediaType.parse("image/jpeg")
        val requestBody = RequestBody.create(mediaType, file)
        val request = okhttp3.Request.Builder()
                .url(presignedUrl)
                .put(requestBody)
                .build()

        val _call = client.newCall(request)
        _call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                listener(false)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                listener(true)
            }
        })
    }

    /**********************
     * Onboarding Asso
     */
    fun updateAssoInfos(asso:Partner?, listener:(isOK:Boolean, response:ResponseBody?) -> Unit) {

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

        request["postal_code"] = asso.postal_code
        request["partner_role_title"] = asso.userRoleTitle

        val call = getOnboardingService().updateAssoInfos(request)

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                }
                else {
                    listener(false,null)
                }
            }
            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun updateUserInterests(interrest:ArrayList<String>, listener:(isOK:Boolean, userResponse:UserResponse?) -> Unit) {

        val user = ArrayMap<String, Any>()
        user["interests"] = interrest

        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = getOnboardingService().updateUser(request)
        call.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    listener(true,response.body())
                }
                else {
                    listener(false,null)
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    fun getAssociationsList(listener:(arrayAssociations:ArrayList<Partner>?) -> Unit) {
        val request = get(get()).entourageComponent.partnerRequest
        request.allPartners.enqueue(object : Callback<PartnersWrapper> {
            override fun onResponse(call: Call<PartnersWrapper>, response: Response<PartnersWrapper>) {
                if (response.isSuccessful) {
                    val partnerList = response.body()!!.partners
                    val arrayPartners = ArrayList<Partner>(partnerList)
                    listener(arrayPartners)
                }
                else {
                    listener(null)
                }
            }
            override fun onFailure(call: Call<PartnersWrapper>, t: Throwable) { listener(null) }
        })
    }
}

/**********************
 * Class For network
 */
class Request constructor(protected var content_type: String)

class Response(avatarKey: String, presignedUrl: String) {
    @SerializedName("avatar_key")
    var avatarKey: String = avatarKey

    @SerializedName("presigned_url")
    var presignedUrl: String = presignedUrl

}