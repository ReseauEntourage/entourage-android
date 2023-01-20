package social.entourage.android.onboarding.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_login_change_phone.*
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.base.BaseActivity
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.isValidEmail
import timber.log.Timber

class LoginChangePhoneActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_change_phone)

        ui_layout_change_phone_ok?.visibility = View.GONE
        ui_layout_waiting?.visibility = View.GONE

        icon_back?.setOnClickListener {
            finish()
        }

        change_phone_mainLayout?.setOnTouchListener { view, _ ->
            view.hideKeyboard()
            view.performClick()
            true
        }
        
        ui_changeCode_bt_validate?.setOnClickListener {
            val oldPhone = ui_changeCode_et_old_phone.text.toString()
            val newPhone = ui_changeCode_et_new_phone.text.toString()
            val email = ui_changeCode_et_email.text.toString()

            var isValidated = true
            var titleId = 0
            var messageId = ""

            if (oldPhone.length < 9) {
                isValidated = false
                titleId = R.string.login_change_warning_title
                messageId = getString(R.string.login_change_error_phone)
            }
            if (isValidated && newPhone.length < 9) {
                isValidated = false
                titleId = R.string.login_change_warning_title
                messageId = getString(R.string.login_change_error_phone)
            }
            if (isValidated && email.isNotEmpty() && !email.isValidEmail()) {
                isValidated = false
                titleId = R.string.login_change_warning_title
                messageId = getString(R.string.login_change_error_mail)
            }

            if (!isValidated) {
                showError(titleId,messageId,R.string.button_OK)
                return@setOnClickListener
            }

            ui_layout_waiting?.visibility = View.VISIBLE
            OnboardingAPI.getInstance().changePhone(oldPhone,newPhone,email) { resID ->
                ui_layout_waiting?.visibility = View.GONE
                if (resID==R.string.login_change_phone_send_ok) {
                    ui_layout_change_phone_ok?.visibility = View.VISIBLE
                    ui_changeCode_bt_validate?.visibility = View.INVISIBLE
                }
                else {
                    showError(R.string.login_change_error_return, getString(resID), R.string.button_OK)
                }
            }
        }

        ui_changeCode_bt_return?.setOnClickListener {
            finish()
        }
    }

    fun showError(titleId:Int, message:String,buttonTextId:Int) {
       try {
           AlertDialog.Builder(this)
               .setTitle(titleId)
               .setMessage(message)
               .setPositiveButton(buttonTextId) { _, _ -> }
               .create()
               .show()
       } catch(e: Exception) {
           Timber.e(e)
       }
    }
}
