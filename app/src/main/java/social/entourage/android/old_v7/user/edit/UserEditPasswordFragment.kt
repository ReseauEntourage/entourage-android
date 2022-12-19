package social.entourage.android.old_v7.user.edit

import android.graphics.Typeface
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_user_edit_password.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.view.EntSnackbar

class UserEditPasswordFragment  : BaseDialogFragment() {
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_user_edit_password, container, false)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SCREEN_09_4)
        configureView()
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------
    fun onCloseButton()  = dismiss()

    fun onSaveButton() {
        if (validatePassword()) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SCREEN_09_4_SUBMIT)
            val userEditFragment = parentFragmentManager.findFragmentByTag(UserEditFragment.TAG) as UserEditFragment?
            userEditFragment?.saveNewPassword(user_new_password?.text.toString().trim { it <= ' ' })
            dismiss()
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun configureView() {
        user_old_password?.typeface = Typeface.DEFAULT
        user_old_password?.transformationMethod = PasswordTransformationMethod()
        user_new_password?.typeface = Typeface.DEFAULT
        user_new_password?.transformationMethod = PasswordTransformationMethod()
        user_confirm_password?.typeface = Typeface.DEFAULT
        user_confirm_password?.transformationMethod = PasswordTransformationMethod()
        user_old_password?.requestFocus()
        title_close_button.setOnClickListener { onCloseButton() }
        user_edit_password_save_button.setOnClickListener { onSaveButton() }
    }

    private fun validatePassword(): Boolean {
        val oldPassword = user_old_password?.text.toString().trim { it <= ' ' }
        val newPassword = user_new_password?.text.toString().trim { it <= ' ' }
        val confirmPassword = user_confirm_password?.text.toString().trim { it <= ' ' }
        val userEditFragment = parentFragmentManager.findFragmentByTag(UserEditFragment.TAG) as UserEditFragment?
        val userPassword = userEditFragment?.presenter?.editedUser?.smsCode ?: ""
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
        view?.let { EntSnackbar.make(it, stringId, Snackbar.LENGTH_SHORT).show() }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "user_edit_password_fragment"
        const val MIN_PASSWORD_LENGTH = 6
    }
}