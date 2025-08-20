package social.entourage.android.profile.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.databinding.NewFragmentSettingsBinding
import social.entourage.android.language.LanguageBottomFragment
import social.entourage.android.language.TranslationBottomFragment
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity
import social.entourage.android.profile.editProfile.EditPasswordFragment
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.tools.view.WebViewFragment

class SettingsFragment : Fragment() {

    private var _binding: NewFragmentSettingsBinding? = null
    val binding: NewFragmentSettingsBinding get() = _binding!!
    private val settingsPresenter: SettingsPresenter by lazy { SettingsPresenter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        addOnClickListeners()
        settingsPresenter.accountDeleted.observe(requireActivity(), ::handleResponse)
        AnalyticsEvents.logEvent(AnalyticsEvents.Profile_view_param)
    }

    private fun initializeView() {
        binding.deleteAccount.profileSettingsItemDivider.visibility = View.GONE
        binding.deleteAccount.profileSettingsItemArrow.visibility = View.GONE
        binding.signOut.profileSettingsItemArrow.visibility = View.GONE
        binding.appVersion.text =
            getString(R.string.about_version_format, BuildConfig.VERSION_FULL_NAME)
        if (!BuildConfig.DEBUG) {
            binding.appDebugInfo.visibility = View.INVISIBLE
        } else {
            binding.appDebugInfo.visibility = View.VISIBLE
            binding.appDebugInfo.text = getString(
                R.string.about_debug_info_format, BuildConfig.VERSION_DISPLAY_BRANCH_NAME,
                EntourageApplication.get().sharedPreferences.getString(
                    EntourageApplication.KEY_REGISTRATION_ID,
                    null
                )
            )
        }

        binding.share.profileSettingsItemArrow.visibility = View.INVISIBLE
        binding.suggest.profileSettingsItemArrow.visibility = View.INVISIBLE

    }

    private fun addOnClickListeners() {
        binding.language.profileSettingsItemLayout.setOnClickListener {
            LanguageBottomFragment.newInstance().show(parentFragmentManager, LanguageBottomFragment.TAG)
        }

        binding.translate.profileSettingsItemLayout.setOnClickListener {
            TranslationBottomFragment.newInstance().show(parentFragmentManager, LanguageBottomFragment.TAG)
        }

        binding.notifications.profileSettingsItemLayout.setOnClickListener {
            SettingsNotificationsFragment.newInstance()
                .show(parentFragmentManager, SettingsNotificationsFragment.TAG)
        }
        binding.share.profileSettingsItemLayout.setOnClickListener {
            shareApplication()
        }
        binding.deleteAccount.profileSettingsItemLayout.setOnClickListener {
            CustomAlertDialog.showWithCancelFirst(
                requireContext(),
                getString(R.string.delete_account_dialog_title),
                getString(R.string.delete_account_dialog_content),
                getString(R.string.delete),
            ) { deleteAccount() }
        }
        binding.signOut.profileSettingsItemLayout.setOnClickListener {
            CustomAlertDialog.showWithCancelFirst(
                requireContext(),
                getString(R.string.sign_out_dialog_title),
                getString(R.string.sign_out_dialog_content),
                getString(R.string.signing_out),
            ) { logout() }
        }
        binding.helpAbout.profileSettingsItemLayout.setOnClickListener {
            HelpAboutFragment.newInstance().show(parentFragmentManager, HelpAboutFragment.TAG)
        }

        binding.helpBlock.profileSettingsItemLayout.setOnClickListener {
            UnblockUsersFragment.newInstance().show(parentFragmentManager, UnblockUsersFragment.TAG)
        }
        binding.suggest.profileSettingsItemLayout.setOnClickListener {
            WebViewFragment.newInstance(getString(R.string.url_app_suggest), 0, true)
                .show(parentFragmentManager, WebViewFragment.TAG)
        }
        binding.appVersion.setOnLongClickListener {
            handleLongPress()
        }
        binding.appDebugInfo.setOnLongClickListener {
            handleLongPress()
        }

        binding.changePwd.setOnClickListener {
            EditPasswordFragment().show(parentFragmentManager, EditPasswordFragment.TAG)
        }
    }

    private fun shareApplication() {
        val intent = Intent(Intent.ACTION_SEND)
        val msgBody = getString(R.string.share_app)
        intent.putExtra(Intent.EXTRA_TEXT, msgBody)
        intent.type = "text/plain"
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }

    private fun logout() {
        EntourageApplication.get().logOut()
        activity?.finish()
    }

    private fun deleteAccount() {
        settingsPresenter.deleteAccount()
        startActivity(Intent(activity, PreOnboardingStartActivity::class.java))
        activity?.finish()
    }

    private fun handleResponse(isSuccess: Boolean) {
        if (isSuccess) {
            logout()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.user_delete_account_failure,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleLongPress(): Boolean {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val authenticationController: AuthenticationController = EntourageApplication.get().authenticationController
            val clipboardManager =
                EntourageApplication.get()
                    .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(
                "FirebaseID",
                authenticationController.me?.token
            )
            clipboardManager.setPrimaryClip(clipData)
            EntSnackbar.make(
                binding.settingsCoordinatorLayout,
                R.string.debug_info_clipboard,
                Snackbar.LENGTH_SHORT
            ).show()
        }
        return true
    }

}