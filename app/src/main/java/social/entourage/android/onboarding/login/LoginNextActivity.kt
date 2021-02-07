package social.entourage.android.onboarding.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login_next.*
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.User
import social.entourage.android.tools.disable
import social.entourage.android.tools.enable
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.CustomProgressDialog

class LoginNextActivity : AppCompatActivity(),LoginNextCallback {

    private var temporaryEmail:String? = null
    private var temporaryPlaceAddress:User.Address? = null
    private var currentPosition = 0
    private var numberOfSteps = 2
    lateinit var alertDialog: CustomProgressDialog
    private var currentUser:User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_next)

        alertDialog = CustomProgressDialog(this)

        currentUser = EntourageApplication.get().entourageComponent.authenticationController.me

        currentUser?.let { user ->
            if (user.address != null || user.address?.displayAddress?.length ?: 0 > 0) {
                currentPosition = 1
            }
        }

        if (savedInstanceState == null) {
            changeFragment()
        }

        ui_bt_next?.setOnClickListener {
            goNext()
        }

        ui_bt_next?.disable()
    }

    override fun onBackPressed() {
        return
    }

    fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    //******************************
    // Network
    //******************************

    fun sendAddress() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_LOGIN_ACTION_ZONE_SUBMIT)
        OnboardingAPI.getInstance(EntourageApplication.get()).updateAddress(temporaryPlaceAddress!!,false) { isOK, userResponse ->
            if (isOK) {
                val authenticationController = EntourageApplication.get().entourageComponent.authenticationController
                val me = authenticationController.me
                if (me != null && userResponse != null) {
                    userResponse.user.phone = me.phone
                    authenticationController.saveUser(userResponse.user)
                }
                Toast.makeText(this, R.string.user_action_zone_send_ok, Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
                goNextStep()
            }
            else {
                alertDialog.dismiss()
                Toast.makeText(this, R.string.user_action_zone_send_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateUserEmail() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_LOGIN_EMAIL_SUBMIT)
        OnboardingAPI.getInstance(EntourageApplication.get()).updateUser(temporaryEmail) { isOK, userResponse ->
            if (isOK && userResponse != null) {
                val authenticationController = EntourageApplication.get().entourageComponent.authenticationController
                authenticationController.saveUser(userResponse.user)
            }
            else {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ERROR_LOGIN_EMAIL_SUBMIT_ERROR)
            }
            alertDialog.dismiss()
            goMain()
        }
    }

    //********
    // CallBack
    //*********
    override fun updateAddress(placeAddress: User.Address?) {
        temporaryPlaceAddress = placeAddress

        if (placeAddress != null) {
            updateButtonNext(true)
        }
        else {
            updateButtonNext(false)
        }
    }

    override fun updateEmailPwd(email: String?) {
        temporaryEmail = email
        currentUser?.email = temporaryEmail
    }

    override fun updateButtonNext(isValid:Boolean) {
        if (isValid) {
            ui_bt_next?.enable(R.drawable.ic_onboard_bt_next)
        }
        else {
            ui_bt_next?.disable()
        }
    }

    //******************************
    // Navigation
    //******************************

    private fun changeFragment() {
        ui_bt_next?.disable()
        val fragment = if (currentPosition == 0) LoginPlaceFragment.newInstance() else LoginEmailFragment.newInstance()

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.ui_container, fragment)
                .commit()

       updatePercent()
    }

    fun updatePercent() {
        val percent = (currentPosition + 1).toFloat() / numberOfSteps.toFloat() * 100
        ui_view_progress?.updatePercent(percent)
    }

    //**********
    // Navigation Methods
    //***

    fun goNext() {
        if (currentPosition == 0) {
            sendAddress()
        }
        else {
            updateUserEmail()
        }
    }

    fun goNextStep() {
        val authController = EntourageApplication.get().entourageComponent.authenticationController

        if (authController.me?.email == null || authController.me?.email?.length ?: -1 == 0)  {
            currentPosition = 1
            changeFragment()
        }
        else {
            goMain()
        }
    }
}

//******************************
// Interface
//******************************

interface LoginNextCallback {
    fun updateAddress(placeAddress: User.Address?)
    fun updateEmailPwd(email:String?)
    fun updateButtonNext(isValid:Boolean)
}