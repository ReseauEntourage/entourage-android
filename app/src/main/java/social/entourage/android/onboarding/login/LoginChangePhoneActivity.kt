package social.entourage.android.onboarding.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLoginChangePhoneBinding
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.isValidEmail
import timber.log.Timber

class LoginChangePhoneActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginChangePhoneBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginChangePhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uiLayoutChangePhoneOk.visibility = View.GONE
        binding.uiLayoutWaiting.visibility = View.GONE

        binding.iconBack.setOnClickListener {
            finish()
        }

        binding.changePhoneMainLayout.setOnTouchListener { view, _ ->
            view.hideKeyboard()
            view.performClick()
            true
        }

        binding.uiChangeCodeBtValidate.setOnClickListener {
            val oldPhone = binding.uiChangeCodeEtOldPhone.text.toString()
            val newPhone = binding.uiChangeCodeEtNewPhone.text.toString()
            val email = binding.uiChangeCodeEtEmail.text.toString()

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

            binding.uiLayoutWaiting.visibility = View.VISIBLE
            OnboardingAPI.getInstance().changePhone(oldPhone,newPhone,email) { resID ->
                binding.uiLayoutWaiting.visibility = View.GONE
                if (resID==R.string.login_change_phone_send_ok) {
                    binding.uiLayoutChangePhoneOk.visibility = View.VISIBLE
                    binding.uiLayoutChangePhoneOk.visibility = View.INVISIBLE
                }
                else {
                    showError(R.string.login_change_error_return, getString(resID), R.string.button_OK)
                }
            }
        }

        binding.uiChangeCodeBtReturn.setOnClickListener {
            finish()
        }
    }

    private fun showError(titleId:Int, message:String, buttonTextId:Int) {
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
