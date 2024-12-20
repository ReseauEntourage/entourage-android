package social.entourage.android.profile.editProfile

import android.graphics.Typeface
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.FragmentUserEditPasswordBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar

class EditPasswordActivity : AppCompatActivity() {

    private lateinit var binding: FragmentUserEditPasswordBinding
    private val presenter: EditProfilePresenter by lazy { EditProfilePresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentUserEditPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Comme avant, on configure la vue
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SCREEN_09_4)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        configureView()
    }

    private fun onCloseButton()  = finish()

    private fun onSaveButton() {
        if (validatePassword()) {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_PROFILE_EDITPWD)
            presenter.saveNewPasswordActivity(this, binding.userNewPassword.text.toString().trim { it <= ' ' } ?: "")
        }
    }

    fun onSaveNewPassword() {
        if(!isFinishing){
            Toast.makeText(this, getString(R.string.user_edit_password_changed), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun onSavePasswordError() {
        Toast.makeText(this, getString(R.string.user_text_update_ko), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun configureView() {
        binding.userOldPassword.typeface = Typeface.DEFAULT
        binding.userOldPassword.transformationMethod = PasswordTransformationMethod()
        binding.userNewPassword.typeface = Typeface.DEFAULT
        binding.userNewPassword.transformationMethod = PasswordTransformationMethod()
        binding.userConfirmPassword.typeface = Typeface.DEFAULT
        binding.userConfirmPassword.transformationMethod = PasswordTransformationMethod()
        binding.userOldPassword.requestFocus()

        binding.iconBack.setOnClickListener { onCloseButton() }
        binding.buttonValidate.setOnClickListener { onSaveButton() }
    }

    private fun validatePassword(): Boolean {
        val oldPassword = binding.userOldPassword.text.toString().trim { it <= ' ' }
        val newPassword = binding.userNewPassword.text.toString().trim { it <= ' ' }
        val confirmPassword =  binding.userConfirmPassword.text.toString().trim { it <= ' ' }
        val userPassword = EntourageApplication.get().authenticationController.me?.smsCode ?: ""
        return when {
            oldPassword != userPassword -> {
                displaySnackbar(R.string.user_edit_password_invalid_current_password)
                false
            }
            newPassword.length < MIN_PASSWORD_LENGTH -> {
                displaySnackbar(R.string.user_edit_password_new_password_too_short)
                false
            }
            newPassword != confirmPassword -> {
                displaySnackbar(R.string.user_edit_password_not_match)
                false
            }
            else -> true
        }
    }

    private fun displaySnackbar(@StringRes stringId: Int) {
        EntSnackbar.make(binding.root, stringId, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
