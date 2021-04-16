package social.entourage.android.user.edit

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_user_edit_about.*
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.user.UserFragment

/**
 * Fragment to edit an user's about text
 */
class UserEditAboutFragment  : DialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var user: User? = null
    private var userEditFragment: UserEditFragment? = null
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        dialog?.window?.let {window ->
            window.requestFeature(Window.FEATURE_NO_TITLE)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        return inflater.inflate(R.layout.fragment_user_edit_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureView()
    }

    override fun dismiss() {
        userEditFragment?.scrollToOriginalPosition()
        super.dismiss()
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------
    fun onCloseClicked() {
        dismiss()
    }

    private fun onSaveClicked() {
        user_edit_about?.text.toString().let { about ->
            if (about.length > ABOUT_MAX_CHAR_COUNT) {
                Toast.makeText(context, R.string.user_edit_about_error, Toast.LENGTH_SHORT).show()
            } else {
                user?.let {u ->
                    u.about = about.trim { it <= ' ' }
                    userEditFragment?.initUserData() ?: run {
                        (parentFragmentManager.findFragmentByTag(UserFragment.TAG) as UserFragment?)?.saveAccount(u)
                    }
                }
                dismiss()
            }
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun configureView() {
        userEditFragment = parentFragmentManager.findFragmentByTag(UserEditFragment.TAG) as UserEditFragment?
        if (userEditFragment == null) {
            //Try to see if we can find the User View fragment
            (parentFragmentManager.findFragmentByTag(UserFragment.TAG) as UserFragment?)?.let {userFragment ->
                user = userFragment.editedUser
            }
        } else {
            user = userEditFragment?.presenter?.editedUser
        }
        user_edit_about_close_button?.setOnClickListener { onCloseClicked()}
        user_edit_about_save_button?.setOnClickListener {onSaveClicked()}
        user?.let {u ->
            user_edit_about?.let {
                it.setText(u.about)
                it.setSelection(it.length())
                it.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable) {
                        val charCountString = context!!.getString(R.string.entourage_create_title_char_count_format, s.length, ABOUT_MAX_CHAR_COUNT)
                        user_edit_about_char_count?.text = charCountString
                        if (s.length > ABOUT_MAX_CHAR_COUNT) {
                            user_edit_about_char_count?.setTextColor(ResourcesCompat.getColor(resources, R.color.entourage_error, null))
                        } else {
                            user_edit_about_char_count?.setTextColor(ResourcesCompat.getColor(resources, R.color.entourage_ok, null))
                        }
                    }
                })
                val charCountString = requireContext().getString(R.string.entourage_create_title_char_count_format, it.length(), ABOUT_MAX_CHAR_COUNT)
                user_edit_about_char_count?.text = charCountString
            }
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG = UserEditAboutFragment::class.java.simpleName
        private const val ABOUT_MAX_CHAR_COUNT = 200
    }
}