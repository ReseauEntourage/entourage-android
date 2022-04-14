package social.entourage.android.new_v8.profile.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.layout_mainprofile_appversion.*
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentSettingsBinding
import social.entourage.android.new_v8.user.ReportUserModalFragment
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity

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
    }


    private fun initializeView() {
        binding.deleteAccount.divider.visibility = View.GONE
        binding.deleteAccount.arrow.visibility = View.GONE
        binding.signOut.arrow.visibility = View.GONE
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
    }

    private fun addOnClickListeners() {
        binding.share.layout.setOnClickListener {
            shareApplication()
        }
        binding.deleteAccount.layout.setOnClickListener {
            Utils.showAlertDialogButtonClicked(
                requireView(),
                getString(R.string.delete_account_dialog_title),
                getString(R.string.delete_account_dialog_content),
                getString(R.string.delete)
            ) { deleteAccount() }
        }
        binding.signOut.layout.setOnClickListener {
            Utils.showAlertDialogButtonClicked(
                requireView(),
                getString(R.string.sign_out_dialog_title),
                getString(R.string.sign_out_dialog_content),
                getString(R.string.signing_out)
            ) { logout() }
        }
        binding.helpAbout.layout.setOnClickListener {
            HelpAboutFragment.newInstance().show(parentFragmentManager, HelpAboutFragment.TAG)
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

    private fun startPreOnboardingStartActivity() {
        startActivity(Intent(activity, PreOnboardingStartActivity::class.java))
        activity?.finish()
    }

    private fun logout() {
        settingsPresenter.logOut(requireContext())
        startPreOnboardingStartActivity()
    }

    private fun deleteAccount() {
        settingsPresenter.deleteAccount()
        startActivity(Intent(activity, PreOnboardingStartActivity::class.java))
        activity?.finish()
    }


    private fun handleResponse(isSuccess: Boolean) {
        if (isSuccess) {
            settingsPresenter.logOut(requireContext())
        } else {
            Toast.makeText(
                requireContext(),
                R.string.user_delete_account_failure,
                Toast.LENGTH_SHORT
            ).show()

        }
    }
}