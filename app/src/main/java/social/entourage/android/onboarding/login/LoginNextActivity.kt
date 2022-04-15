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

    private var temporaryPlaceAddress:User.Address? = null
    private var currentPosition = 0
    private var numberOfSteps = 1
    lateinit var alertDialog: CustomProgressDialog
    private var currentUser:User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_next)

        alertDialog = CustomProgressDialog(this)

        currentUser = EntourageApplication.get().authenticationController.me

        if (savedInstanceState == null) {
            changeFragment()
        }

        ui_bt_next?.setOnClickListener {
            sendAddress()
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
        temporaryPlaceAddress?.let {
            OnboardingAPI.getInstance().updateAddress(it, false) { isOK, userResponse ->
                if (isOK) {
                    val authenticationController = EntourageApplication.get().authenticationController
                    val me = authenticationController.me
                    if (me != null && userResponse != null) {
                        userResponse.user.phone = me.phone
                        authenticationController.saveUser(userResponse.user)
                    }
                    Toast.makeText(this, R.string.user_action_zone_send_ok, Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                    goMain()
                } else {
                    alertDialog.dismiss()
                    Toast.makeText(this, R.string.user_action_zone_send_failed, Toast.LENGTH_SHORT).show()
                }
            }
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
        val fragment = LoginPlaceFragment.newInstance()

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
}

//******************************
// Interface
//******************************

interface LoginNextCallback {
    fun updateAddress(placeAddress: User.Address?)
    fun updateButtonNext(isValid:Boolean)
}