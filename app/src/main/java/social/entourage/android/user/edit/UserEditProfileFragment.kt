package social.entourage.android.user.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_user_edit_profile.*
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.User

class UserEditProfileFragment  : DialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var user: User? = null
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_09_5)
        return inflater.inflate(R.layout.fragment_user_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureView()
        user_edit_profile_save.setOnClickListener {onSaveProfile()}
    }

    private fun configureView() {
        val userEditFragment = parentFragmentManager.findFragmentByTag(UserEditFragment.TAG) as UserEditFragment?
                ?: return
        userEditFragment.editedUser?.let { u ->
            user = u
            user_edit_profile_firstname?.setText(u.firstName)
            user_edit_profile_lastname?.setText(u.lastName)
            user_edit_profile_email.setText(u.email)
        } ?: run  { dismiss() }
        arguments?.let {args->
            when (args.getInt(KEY_EDIT_TYPE, EDIT_UNKNOWN)) {
                EDIT_NAME-> user_edit_profile_firstname?.requestFocus()
                EDIT_EMAIL->user_edit_profile_email?.requestFocus()
                else -> {}
            }
        }
    }

    fun onSaveProfile() {
        val firstname = user_edit_profile_firstname?.text.toString().trim { it <= ' ' }
        val lastname = user_edit_profile_lastname?.text.toString().trim { it <= ' ' }
        val email = user_edit_profile_email?.text.toString().trim { it <= ' ' }
        when {
            firstname.isEmpty() -> {
                displayToast(R.string.user_edit_profile_invalid_firstname)
                user_edit_profile_firstname?.requestFocus()
            }
            lastname.isEmpty() -> {
                displayToast(R.string.user_edit_profile_invalid_lastname)
                user_edit_profile_lastname?.requestFocus()
            }
            email.isEmpty() -> {
                displayToast(R.string.user_edit_profile_invalid_email)
                user_edit_profile_email?.requestFocus()
            }
            else -> {
                user?.apply {
                    firstName = firstname
                    lastName = lastname
                    this.email = email
                }
                (parentFragmentManager.findFragmentByTag(UserEditFragment.TAG) as UserEditFragment?)?.initUserData()
                dismiss()
            }
        }
    }

    private fun displayToast(@StringRes stringId: Int) {
        Toast.makeText(activity, stringId, Toast.LENGTH_SHORT).show()
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "user_edit_profile_fragment"
        const val EDIT_UNKNOWN = 0
        const val EDIT_NAME = 1
        const val EDIT_EMAIL = 2
        private const val KEY_EDIT_TYPE = "user_edit_type"
        @JvmStatic
        fun newInstance(editType: Int): UserEditProfileFragment {
            val fragment = UserEditProfileFragment()
            val args = Bundle()
            args.putInt(KEY_EDIT_TYPE, editType)
            fragment.arguments = args
            return fragment
        }
    }
}